package com.gof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import com.gof.entity.IrParamModelCalc;
import com.gof.util.DateUtil;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import umontreal.ssj.probdist.ChiSquareNoncentralDist;

@Getter
@Setter
@Slf4j
public class CIRCalibrationForcast extends IrModel {
	
	protected double        initAlpha;
	protected double        initTheta;
	protected double        initSigma;
	protected double        dt;
	
	protected double        rZero;
	protected double        alpha;
	protected double        theta;
	protected double        sigma;                   
	protected double        accuracy;
	protected int           itrMax;

	protected double[]      depenVar;
	protected double[][]    indepVar;	
	protected double[]      indepVar1;
	protected double[]      indepVar2;
	
	protected Map<String, Double> iRateHisMap = new TreeMap<String, Double>();
	
	
	@Builder(builderClassName="of", builderMethodName="of")
	public CIRCalibrationForcast(String bssd, List<IrCurveSpot> iRateHisList, Double dt, Integer itrMax, Double accuracy) {				
		super();		
		
		this.baseDate = (baseDate == null ? LocalDate.now() : DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth()));
		this.setIrmodelAttributes(iRateHisList);		
		this.dt       = (dt == null ? 1.0 / 250 : dt);
		this.itrMax   = (itrMax == null ? 100 : itrMax);
		this.accuracy = (accuracy == null ? 1.0E-8 : accuracy);		
	}	
	
	/**
	 * dr_t = k[θ-r(t)]*dt + σ√(r(t))*dW(t)
	 * (r_t-r_(t-1)) / √(r_(t-1)) = (kθ*dt) / √(r_(t-1))- k√(r_(t-1)) * dt + σdW(t)
	 * rt+Δt − rt = k * (θ − rt)Δt + σ√rt * εt
	 */
	public List<IrParamModelCalc> getCalibrationResult() {
		
		List<IrParamModelCalc> rst = new ArrayList<IrParamModelCalc>();
		
		this.initialEstimate();
		this.mleOptimize(new double[] {this.initAlpha, this.initTheta, this.initSigma});
		
		IrParamModelCalc rZero = new IrParamModelCalc();
		rZero.setParamTypCd("R_ZERO");
		rZero.setParamVal(this.rZero);
		rst.add(rZero);
		
		IrParamModelCalc alpha = new IrParamModelCalc();
		alpha.setParamTypCd("ALPHA");
		alpha.setParamVal(this.alpha);
		rst.add(alpha);

		IrParamModelCalc theta = new IrParamModelCalc();
		theta.setParamTypCd("THETA");
		theta.setParamVal(this.theta);
		rst.add(theta);

		IrParamModelCalc sigma = new IrParamModelCalc();
		sigma.setParamTypCd("SIGMA");
		sigma.setParamVal(this.sigma);
		rst.add(sigma);
		
		return rst;		
	}	
	
	
	private void setIrmodelAttributes(List<IrCurveSpot> iRateHisList) {	
		
		this.iRateHisMap = iRateHisList.stream().collect(Collectors.toMap(IrCurveSpot::getBaseDate, IrCurveSpot::getSpotRate, (k, v) -> k, TreeMap::new));
		this.iRateBase   = this.iRateHisMap.entrySet().stream().mapToDouble(s -> s.getValue()).toArray();
		this.rZero       = vectMean(this.iRateBase);
	}


	private void initialEstimate() {
	
		double[] rateDiff = diffVector(this.iRateBase);
		
		this.depenVar     = new double[rateDiff.length];   // (r_[t]-r_[t-1]) / sqrt(r_[t-1])
		this.indepVar1    = new double[rateDiff.length];   // dt / sqrt(r_[t-1])  -> weight kθ
		this.indepVar2    = new double[rateDiff.length];   // sqrt(r_[t-1]) * dt  -> weight -k
		this.indepVar     = new double[rateDiff.length][2];
		
		for(int i=0; i<this.depenVar.length; i++) {			
			this.depenVar [i] = rateDiff[i] / Math.sqrt(this.iRateBase[i]);			
			this.indepVar1[i] = this.dt     / Math.sqrt(this.iRateBase[i]);
			this.indepVar2[i] = this.dt     * Math.sqrt(this.iRateBase[i]);
			
			this.indepVar [i][0] = this.indepVar1[i];
			this.indepVar [i][1] = this.indepVar2[i];			
			
//			log.info("{}, {}, {}, {}, {}, {}", this.iRateBase[i], this.dt, this.depenVar[i], this.indepVar1[i], this.indepVar2[i]);
		}
		
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();	
		reg.setNoIntercept(true);
		reg.newSampleData(this.depenVar, this.indepVar);
		
		double[] rslt = reg.estimateRegressionParameters();
//		log.info("reg: {}, {}, {}, {}, {}, {}", rslt[0], rslt[1], reg.estimateRegressionStandardError(), reg.calculateResidualSumOfSquares());
		
		this.initAlpha = -rslt[1];
		this.initTheta = -rslt[0] / rslt[1];		
		this.initSigma = vectSd(reg.estimateResiduals()) / Math.sqrt(this.dt);
		
		this.initAlpha = constraints(this.initAlpha, this.initTheta, this.initSigma, 0);
		this.initTheta = constraints(this.initAlpha, this.initTheta, this.initSigma, 1);
		log.info("Initial Parameter [rZero: {}, Alpha:{}, Theta: {}, Sigma: {}]", this.rZero, this.initAlpha, this.initTheta, this.initSigma);	
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
			this.alpha = calibParas[0]; this.theta = calibParas[1]; this.sigma = calibParas[2];
		}
		catch (Exception e) {
			log.error("Error in finding Maximum Likelihood Estimation in CIR module");
			e.printStackTrace();
		}		
		log.info("Optimal Parameter {}, [rZero: {}, alpha: {}, theta: {}, sigma: {}]", LocalDateTime.now(), this.rZero, this.alpha, this.theta, this.sigma);
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
			uVec[i] = c * Math.exp(-kappa * this.dt) * this.iRateBase[i];
			vVec[i] = c * this.iRateBase[i+1];
			
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
	