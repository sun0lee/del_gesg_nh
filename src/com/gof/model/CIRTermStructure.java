package com.gof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.gof.entity.IrCurveSpot;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import umontreal.ssj.probdist.ChiSquareNoncentralDist;

@Getter
@Setter
@Slf4j
public class CIRTermStructure extends IrModel {
	
	protected double        initKappa;
	protected double        initTheta;
	protected double        initSigma;
	protected double        dt;
	
	protected double        kappa;
	protected double        theta;
	protected double        sigma;
	
	protected int[]         pmtIdxBaseRate;
	protected double[]      priceBaseRate;
	protected double[]      spotContBaseRate;
	protected double[]      fwdContBaseRate;	
                   
	protected double        ltfr;	
	protected int           ltfrT;  
	protected int           prjYear;
	protected int           prjInterval;	
	protected double        accuracy;
	protected int           itrMax;
	
	protected double[]      depenVar;
	protected double[][]    indepVar;	
	protected double[]      indepVar1;
	protected double[]      indepVar2;

	protected List<SmithWilsonRslt> swRsltList = new ArrayList<SmithWilsonRslt>();
	
	
	public static void main(String[] args) throws Exception {		
		
		String[]   matCd       = new String[] {"M0003", "M0006", "M0009", "M0012", "M0018", "M0024", "M0030", "M0036", "M0048", "M0060", "M0084", "M0120", "M0240"};
		double[]   baseRate    = new double[] {0.0152, 0.0166, 0.0179, 0.0187, 0.0201, 0.0209, 0.0215, 0.0215, 0.0231, 0.0238, 0.0244, 0.0246, 0.0244};    //FY2017 RF_SPOT
		
//		String[]   matCd       = new String[] {"M0003", "M0006", "M0009", "M0012", "M0018", "M0024", "M0030", "M0036", "M0048", "M0060", "M0084", "M0120", "M0180", "M0240"};
//		double[]   baseRate    = new double[] {0.00541094484478832, 0.00648046522498502, 0.00871426771066752, 0.00957012108585276, 0.01155037857853425, 0.01306160221245922, 0.01388401499446035
//				                             , 0.0146180119810746 , 0.01610774927339009, 0.01761371602422424, 0.01990500084649605, 0.0213226811489029 , 0.02237566974987093, 0.02236847998415925};    //FY2021 06 IR_CURVE_SPOT
		
		List<IrCurveSpot> curveList = new ArrayList<IrCurveSpot>();		
		for(int i=0; i<matCd.length; i++) {
			IrCurveSpot curve = new IrCurveSpot();			
			curve.setMatCd(matCd[i]);
			curve.setSpotRate(baseRate[i]);
			curveList.add(curve);
		}	
		
		CIRTermStructure calib = new CIRTermStructure.of()
												 	 .bssd("202106")
												 	 .iRateBaseList(curveList)
												 	 .prjYear(20)
												 	 .accuracy(1.0E-8)
												 	 .build();

		calib.getCalibrationResult();
		
//		calib.getHw1fCalibrationResultList().stream().filter(s -> s.getMonthSeq() % 12 == 0 && s.getMonthSeq() <= 140).forEach(s -> log.info("{}", s.toString()));		
		log.info("_____________________________\n");
	}	

	
	@Builder(builderClassName="of", builderMethodName="of")
	public CIRTermStructure(String bssd, List<IrCurveSpot> iRateBaseList, Character cmpdType, Boolean isRealNumber, Integer prjYear, Integer prjInterval, Double ltfr, Integer ltfrT, Double liqPrem, Integer dayCountBasis, Integer itrMax, Double accuracy) {				
		super();		
		
		this.baseDate = (baseDate == null ? LocalDate.now() : DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth()));
		this.cmpdType = (cmpdType == null ? CMPD_MTD_DISC : cmpdType);
		this.isRealNumber = (isRealNumber == null ? true : isRealNumber);
		
		this.prjYear = (prjYear == null ? 100 : prjYear);		
		this.prjInterval = (prjInterval == null ? 1 : prjInterval);		
		this.ltfr = (ltfr == null ? 0.0495 : ltfr);
		this.ltfrT = (ltfrT == null ? 60 : ltfrT);	
		this.dayCountBasis = (dayCountBasis == null ? DCB_MON_DIF : dayCountBasis);
		this.itrMax = (itrMax == null ? 100 : itrMax);
		this.accuracy = (accuracy == null ? 1.0E-8 : accuracy);
		
		this.dt = this.prjInterval / 12.0;		
		this.setTermStructureBase(iRateBaseList);
		this.setIrmodelAttributes();		
	}	
	
	
	/**
	 * dr_t = k[θ-r(t)]*dt + σ√(r(t))*dW(t)
	 * (r_t-r_(t-1)) / √(r_(t-1)) = (kθ*dt) / √(r_(t-1))- k√(r_(t-1)) * dt + σdW(t)
	 * rt+Δt − rt = k * (θ − rt)Δt + σ√rt * εt
	 */
	public void getCalibrationResult() {
		
		this.applySmithWilsonInterpoloation(this.ltfrT, 1);		
//		this.applySmithWilsonInterpoloation(this.prjYear, this.prjInterval);
		this.initialEstimate();		
		
	}
	
	
	public void getSimulationResult() {
		
	}
	
	
	private void setIrmodelAttributes() {		

		double toRealScale = this.isRealNumber ? 1 : 0.01;
		for(int j=0; j<this.iRateBase.length; j++) this.iRateBase[j] = (this.cmpdType == CMPD_MTD_DISC) ? toRealScale*this.iRateBase[j] : irContToDisc(toRealScale*this.iRateBase[j]);				
		this.ltfr = (cmpdType == CMPD_MTD_DISC ? this.iRateBase[this.iRateBase.length-1] : irContToDisc(this.iRateBase[this.iRateBase.length-1]));
		this.ltfrT = (int) this.tenor[this.tenor.length-1];		
	}
	
	
//	1st Step 1-1: Preparation of Swap Cashflow from Smith-Wilson Result from baseCurve
	private void applySmithWilsonInterpoloation(int prjYear, int prjInterval) {
		
		Map<Double, Double> ts = new TreeMap<Double, Double>();
		for(int i=0; i<this.tenor.length; i++) ts.put(this.tenor[i], this.iRateBase[i]);

		SmithWilsonKics sw = new SmithWilsonKics(this.baseDate, ts, this.cmpdType, this.isRealNumber, this.ltfr, this.ltfrT, prjYear, prjInterval, this.itrMax, this.dayCountBasis);			
		this.swRsltList = sw.getSmithWilsonResultList();		

		this.pmtIdxBaseRate   = this.swRsltList.stream().map(s -> s.getMatTerm()).mapToInt(Double::intValue).toArray();
		this.priceBaseRate    = this.swRsltList.stream().map(s -> s.getDcntFactor()).mapToDouble(Double::doubleValue).toArray();		
		this.spotContBaseRate = this.swRsltList.stream().map(s -> s.getSpotCont()).mapToDouble(Double::doubleValue).toArray();
		this.fwdContBaseRate  = this.swRsltList.stream().map(s -> s.getFwdCont()).mapToDouble(Double::doubleValue).toArray();		
		
		this.spotContBaseRate = irDiscToCont(this.iRateBase);		
//		for(int i=0; i<this.spotContBaseRate.length; i++) log.info("{}", this.spotContBaseRate[i]);
	
//		swRsltList.stream().filter(s-> Double.parseDouble(s.getMatCd().substring(1, 5)) % 6 == 0)
//        				   .forEach(s->log.info("{}, {}, {} , {}, {}, {}, {}", s.getBaseDate(), s.getResultType(), s.getMatCd(), s.getSpotDisc(), s.getSpotCont(), s.getFwdDisc(), s.getDcntFactor(), s.getMatTerm()));
	}	
	

	private void initialEstimate() {
	
		double[] rateDiff = diffVector(this.spotContBaseRate);
		
		this.depenVar     = new double[rateDiff.length];   // (r_[t]-r_[t-1]) / sqrt(r_[t-1])
		this.indepVar1    = new double[rateDiff.length];   // dt / sqrt(r_[t-1])  -> weight kθ
		this.indepVar2    = new double[rateDiff.length];   // sqrt(r_[t-1]) * dt  -> weight -k
		this.indepVar     = new double[rateDiff.length][2];
		
		for(int i=0; i<this.depenVar.length; i++) {			
			this.depenVar [i] = rateDiff[i] / Math.sqrt(this.spotContBaseRate[i]);
//			this.depenVar [i] = rateDiff[i];
			
			this.indepVar1[i] = this.dt     / Math.sqrt(this.spotContBaseRate[i]);
			this.indepVar2[i] = this.dt     * Math.sqrt(this.spotContBaseRate[i]);
			
			this.indepVar [i][0] = this.indepVar1[i];
			this.indepVar [i][1] = this.indepVar2[i];			
			
//			log.info("{}, {}, {}", this.depenVar[i], this.indepVar1[i], this.indepVar2[i]);
		}
		
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();	
		reg.setNoIntercept(true);
		reg.newSampleData(this.depenVar, this.indepVar);
		
		double[] rslt = reg.estimateRegressionParameters();
		log.info("reg: {}, {}, {}, {}, {}, {}", rslt[0], rslt[1], reg.estimateRegressionStandardError(), reg.calculateResidualSumOfSquares());
		
		this.initKappa = -rslt[1];
		this.initTheta = -rslt[0] / rslt[1];		
		this.initSigma = vectSd(reg.estimateResiduals()) / Math.sqrt(this.dt);
		
		this.initKappa = constraints(this.initKappa, this.initTheta, this.initSigma, 0);
		this.initTheta = constraints(this.initKappa, this.initTheta, this.initSigma, 1);
		log.info("Initial Parameter [Kappa:{}, Theta: {}, Sigma: {}]", this.initKappa, this.initTheta, this.initSigma);		
		
		mleOptimize(new double[] {this.initKappa, this.initTheta, this.initSigma});
//		mleOptimize(new double[] {0.01, 0.01, 0.01});
//		Initial Parameter [Kappa:0.5071375506131514, Theta: 0.024208933366278076, Sigma: 8.932925561192419E-4]
//		2022-03-20T17:40:18.441, KappaL 0.5175755410829372, theta: 0.024209986386784774, sigma: 9.078508144804114E-4
		
		simulation();
	}
	

	private void simulation() {
		
		int prjMonth = 10 * 12;
		int scenNum  = 1;		
		
		double r0 = priceBaseRate[0];
		double[][] rt       = new double[prjMonth][scenNum];
//		double[]   prjTenor = new double[prjMonth];
		
		double expm1Kdt = Math.exp(-1 * this.kappa * this.dt);
		double expm2Kdt = Math.exp(-2 * this.kappa * this.dt);
		double sigmaSq  = Math.pow(this.sigma, 2);
		
		for(int i=0; i<rt.length; i++) {			
			for(int j=0; j<rt[i].length; j++) {
				
				if(i==0) rt[i][j] = r0;
				else {	
					rt[i][j]  = rt[i-1][j] * expm1Kdt + this.theta * (1.0 - expm1Kdt);
					rt[i][j] += Math.sqrt(rt[i-1][j] * sigmaSq / this.kappa * (expm1Kdt - expm2Kdt) +  0.5 * this.theta * sigmaSq / this.kappa * Math.pow(1.0 - expm1Kdt, 2)) * 0.2;
				}
//				log.info("i,j: [{}, {}], rt: {}, {}, {}, {}", i+1, j+1, rt[i][j], this.kappa, this.theta, this.sigma);
			}
		}		
	}
		
	
	private void mleOptimize(double[] paras) {
		
		MultivariateFunction fp = new MultivariateFunction() {			
			public double value(double[] inputParas) {
				return logLikelihood(inputParas);
			}
		};			
		
		double[] fpLower = new double[paras.length];
		double[] fpUpper = new double[paras.length];
		double[] fpScale = new double[paras.length];
		
		for(int i=0; i<paras.length; i++) {			
			fpLower[i] = 1.0E-4;
			fpUpper[i] = 1.0E+5;			
			fpScale[i] = 1000;
		}
		
	    MultivariateFunctionPenaltyAdapter fpConstr = new MultivariateFunctionPenaltyAdapter(fp, fpLower, fpUpper, 1000, fpScale);
		
		double[] calibParas = paras;
		double   calibValue = 0.0;	
		
		log.info("{}, {}, {}", LocalDateTime.now(), paras);		
		try {			
			for(int i=0; i<this.itrMax; i++) {
				
				SimplexOptimizer optimizer = new SimplexOptimizer(1e-12, 1e-12);
				AbstractSimplex  ndsimplex = new NelderMeadSimplex(nelderMeadStep(calibParas, 0.001));
				PointValuePair   result    = optimizer.optimize(new MaxEval(100000)				                                      
						                                      , new ObjectiveFunction(fpConstr)
						                                      , ndsimplex
						                                      , GoalType.MINIMIZE
						                                      , new InitialGuess(calibParas));

				log.info("{}, {}, {}, {}", i, result.getValue(), LocalDateTime.now(), result.getPoint());			

				if(Math.abs(result.getValue() - calibValue) < this.accuracy) break;	
				calibParas   = constraints(result.getPoint());				
				calibValue   = result.getValue();				
			}			
			this.kappa = calibParas[0]; this.theta = calibParas[1]; this.sigma = calibParas[2];
		}
		catch (Exception e) {
			log.error("Error in finding Maximum Likelihood Estimation in CIR module");
			e.printStackTrace();
		}		
		log.info("{}, KappaL {}, theta: {}, sigma: {}", LocalDateTime.now(), this.kappa, this.theta, this.sigma);
	}	


	private double[] nelderMeadStep(double[] inputParas, double scale) {
		
		double[] step = new double[inputParas.length];
		for(int i=0; i<step.length; i++) {
			step[i] = Math.max(Math.abs(inputParas[i] * scale), SIMPLEX_STEP_MIN);
		}
//		log.info("step: {}", step);
		return step;		
	}
	

	private double logLikelihood(double[] inputParas) {
		
		double logLike = 0.0;
		double[] paras = constraints(inputParas.clone());		
		
		double kappa = paras[0];
		double theta = paras[1];
		double sigma = paras[2];		
		
		double c = (2 * kappa) / (Math.pow(sigma, 2) * (1.0 - Math.exp(-kappa * this.dt)));
		double q = (2 * kappa * theta) / Math.pow(sigma, 2) -1.0;
		
		double[] uVec = new double[this.depenVar.length];
		double[] vVec = new double[this.depenVar.length];			
		
		for(int i=0; i<uVec.length; i++) {
			uVec[i] = c * Math.exp(-kappa * this.dt) * this.spotContBaseRate[i];
			vVec[i] = c * this.spotContBaseRate[i+1];
			
			ChiSquareNoncentralDist chi = new ChiSquareNoncentralDist(2*q+2, 2*uVec[i]);
			Double gpdf = chi.density(2*vVec[i]);
			Double ppdf = (gpdf.isInfinite() || gpdf.isNaN()) ? ZERO_DOUBLE : 2 * c * gpdf;
			
			logLike += -Math.log(ppdf);			 
//			log.info("{}, c: {}, q: {}, u: {}, v: {}, gpdf: {}, ppdf: {}, LL: {}", i+1, c, q, u[i], v[i], gpdf, Math.log(2*c*gpdf), logLike);			
		}		
		if(2.0 * kappa * theta < Math.pow(sigma, 2)) logLike = 1.0 / ZERO_DOUBLE;
		
		return logLike;
	}
	
	
	private double[] diffVector(double[] vec) {
		
		if(vec.length <= 1) return vec;
		
		double[] diff = new double[vec.length-1];		
		for(int i=0; i<diff.length; i++) diff[i] = vec[i+1] - vec[i];
		
		return diff;		
	}
	
	
	private double constraints(double kappa, double theta, double sigma, int idx) {
		
		double kappaAdj = Math.abs(kappa);
		double thetaAdj = Math.abs(theta);		
		double constr   = 2.0 * kappa * theta - Math.pow(sigma, 2);
		
		if(constr > 0) {
			if(idx==0) return kappaAdj;
			else       return thetaAdj;			
		}
		else {
			if(idx==0) return 0.5 * Math.pow(sigma, 2) / thetaAdj;
			else       return thetaAdj;
		}
	}

	
	private double[] constraints(double[] paras) {
		
		double[] parasCon = paras.clone();
		
		for(int i=0; i<parasCon.length; i++) {
			if(i<2) parasCon[i] = Math.abs(parasCon[i]);
			if(parasCon[0] * parasCon[1] < 0.5 * Math.pow(parasCon[2], 2)) parasCon[0] = 0.5 * Math.pow(parasCon[2], 2) / parasCon[1];
		}		
		return parasCon;
	}		
	
}		
	