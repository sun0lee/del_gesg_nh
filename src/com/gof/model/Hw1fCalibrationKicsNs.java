package com.gof.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrValidParamHw;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.model.entity.SwpnVolInfo;
import com.gof.util.DateUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Hw1fCalibrationKicsNs extends IrModel {	
	
	protected int[]         swpnMat;
	protected int[]         swapTenor;
	protected double[][]    swpnVolMkt;
	
	protected int[][]       swapMatTenor;	
	protected double[][]    swapDfSum;
	protected double[][]    swapRate;
	protected double[][]    swpnPriceAtm;
	protected double[][]    swpnPriceHw;
	
	protected double[]      initParas;	
	protected double[]      optParas;
	protected double        costValue;
	protected int[]         alphaPiece;
	protected int[]         sigmaPiece;		
	protected double        alphaMin = 1e-4;
	protected double        sigmaMin = 1e-5;

	protected int[]         pmtIdxBaseRate;
	protected double[]      priceBaseRate;
	protected double[]      spotContBaseRate;
	protected double[]      fwdContBaseRate;		

	protected int           freq;
	protected double        notional;	
	protected double        accuracy;
	protected int           itrMax;	
		                        
	protected double        ltfr;	
	protected int           ltfrT;
	protected int           prjInterval;	
	protected double        lastLiquidPoint;
	
	protected List<SmithWilsonRslt> swRsltList = new ArrayList<SmithWilsonRslt>();	

	public Hw1fCalibrationKicsNs(String bssd, List<IrCurveSpot> iRateBaseList, List<SwpnVolInfo> swpnVolInfo, int[] alphaPiece, int[] sigmaPiece, double[] initParas, int freq, double accuracy) {				
		
		super();		
		this.baseDate = DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth());
		this.isRealNumber = true;		
		this.setTermStructureBase(iRateBaseList);
		this.setSwpnVolInfo(swpnVolInfo);		
		
		Arrays.sort(alphaPiece);
		Arrays.sort(sigmaPiece);		
		this.alphaPiece = alphaPiece;
		this.sigmaPiece = sigmaPiece;
		
		this.initParas = initParas;
		this.freq = freq;
		this.notional = 100;		
//		this.cmpdType = CMPD_MTD_CONT;
		this.cmpdType = CMPD_MTD_DISC;
		
		this.ltfr = (cmpdType == CMPD_MTD_DISC ? this.iRateBase[this.iRateBase.length-1] : irContToDisc(this.iRateBase[this.iRateBase.length-1]));
		this.ltfrT = (int) this.tenor[this.tenor.length-1];
		
		//TODO: prjInterval Means SW prjInteval itself, not payment term (e.g. if ZCB bond then freq == 0 But prjInterval need to set 12M)!!!
		this.prjInterval = MONTH_IN_YEAR / this.freq;		
		this.dayCountBasis = DCB_MON_DIF;
		this.itrMax = 200;
		this.accuracy = accuracy;
		this.setLastLiquidPoint(this.tenor[this.tenor.length-1]);
	}			
	
	public Hw1fCalibrationKicsNs(LocalDate baseDate, List<IrCurveSpot> iRateBaseList, List<SwpnVolInfo> swpnVolInfo, int[] alphaPiece, int[] sigmaPiece, double[] initParas, int freq, double notional,
			               char cmpdType, boolean isRealNumber, int prjInterval, int dayCountBasis, int itrMax, double accuracy) {				
		super();		
		this.baseDate = baseDate;
		this.isRealNumber = isRealNumber;
		this.setTermStructureBase(iRateBaseList);
		this.setSwpnVolInfo(swpnVolInfo);
		
		Arrays.sort(alphaPiece);
		Arrays.sort(sigmaPiece);		
		this.alphaPiece = alphaPiece;
		this.sigmaPiece = sigmaPiece;
		
		this.initParas = initParas;
		this.freq = freq;
		this.notional = notional;		
		this.cmpdType = cmpdType;
		
		this.ltfr = this.iRateBase[this.iRateBase.length-1];
		this.ltfrT = (int) this.tenor[this.tenor.length-1];

		this.prjInterval = prjInterval;
		this.dayCountBasis = dayCountBasis;
		this.itrMax = itrMax;
		this.accuracy = accuracy;
		this.setLastLiquidPoint(this.tenor[this.tenor.length-1]);
	}		
	

	public List<Hw1fCalibParas> getHw1fCalibrationResultList() {
		return getHw1fCalibrationResultList("");
	}
	
//	TODO: Main Function for Calibration Parameter
	public List<Hw1fCalibParas> getHw1fCalibrationResultList(String mode) {
		
		applySmithWilsonInterpoloation(this.prjInterval, this.lastLiquidPoint);		
		calSwpnSwapRate();		
		calSwpnPriceAtm();		
		optParasHw();		
//		checkSwpnPriceHw();
//		checkSwpnVolDiff();

		List<Hw1fCalibParas> hw1fParam = new ArrayList<Hw1fCalibParas>();
		
//		for(int i=0; i<this.optParas.length; i++) log.info("{}, {}", i, this.optParas[i]);
		
		for(int i=0; i<this.sigmaPiece.length+1; i++) {			
			Hw1fCalibParas param = new Hw1fCalibParas();
			
			param.setBaseDate(dateToString(this.baseDate));
			int outerPiece = (int) this.lastLiquidPoint;
			
			if(i<this.sigmaPiece.length) {				
				param.setMonthSeq(this.sigmaPiece[i] * MONTH_IN_YEAR);
				param.setMatCd(String.format("%s%04d", TIME_UNIT_MONTH, this.sigmaPiece[i] * MONTH_IN_YEAR));
				param.setAlpha(Math.max(this.optParas[0], this.alphaMin));
				param.setSigma(Math.max(this.optParas[i+1], this.sigmaMin));
				param.setCost(this.costValue);
			}
			else {
				param.setMonthSeq(outerPiece * MONTH_IN_YEAR);
				param.setMatCd(String.format("%s%04d", TIME_UNIT_MONTH, outerPiece * MONTH_IN_YEAR));
				param.setAlpha(Math.max( (this.alphaPiece[0] < outerPiece ? this.optParas[0] : this.optParas[0]), this.alphaMin));
				param.setSigma(Math.max( (this.sigmaPiece[0] < outerPiece ? this.optParas[6] : this.optParas[1]), this.sigmaMin));
				param.setCost(this.costValue);
			}			
			hw1fParam.add(param);			
		}
//		log.info("{}", hw1fParam);
		return hw1fParam;		
	}	
	
//	TODO: Main Function for Validation
	public List<IrValidParamHw> getValidationResult() {
		
		List<IrValidParamHw> validRslt = new ArrayList<IrValidParamHw>();		
		
		double[][] volHw = new double[this.swpnMat.length][this.swapTenor.length];		
		double[][] prcHw = new double[this.swpnMat.length][this.swapTenor.length];		
		double[]   alpha = new double[] {this.optParas[0]};
		double[]   sigma = new double[] {this.optParas[1], this.optParas[2], this.optParas[3], this.optParas[4], this.optParas[5], this.optParas[6]};		
		
		double     errRelPrcSum = 0.0; 
		double     errRelPrc    = 0.0;		
		double     errRelVolSum = 0.0;
		double     errRelVol    = 0.0;		

		double     errAbsPrc    = 0.0;
		double     errAbsVol    = 0.0;
		
		for(int i=0; i<this.swpnMat.length; i++) {
			for(int j=0; j<this.swapTenor.length; j++) {				
	
				prcHw[i][j] = swpnPriceHw(this.swpnMat[i], this.swapMatTenor[i][j], alpha, sigma, this.swapRate[i][j]);	
				volHw[i][j] = swpnVolHw  (this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], prcHw[i][j]);
				
				errRelPrc     = Math.pow( (this.swpnPriceAtm[i][j] - prcHw[i][j]) / this.swpnPriceAtm[i][j], 2);
				errRelPrcSum += errRelPrc;				
				errRelVol     = Math.pow( (this.swpnVolMkt[i][j] - volHw[i][j]) / this.swpnVolMkt[i][j], 2);
				errRelVolSum += errRelVol;
				
				errAbsPrc     = this.swpnPriceAtm[i][j] - prcHw[i][j];
				errAbsVol     = this.swpnVolMkt[i][j]   - volHw[i][j];
				
//				double check1 = calSwpnPriceAtmFromHwVol(this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], volHw[i][j]);
//				double check2 = calSwpnPriceAtmFromHwVol(this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], this.swpnVolMkt[i][j]);
//				log.info("T1:{}, T2:{}, PRC_BK:{}, PRC_HW:{}, VOL_BK:{}, VOL_HW:{}, PRC_HW_CK1:{}, PRC_HW_CK2: {}", swpnMat[i], swapMatTenor[i][j], swpnPriceAtm[i][j], prcHw[i][j], swpnVolMkt[i][j], volHw[i][j], check1, check2);
				
				IrValidParamHw prc = new IrValidParamHw();				
				prc.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				prc.setSwpnMatNum((double) this.swpnMat[i]);
				prc.setSwapTenNum((double) this.swapTenor[j]);
				prc.setValidDv("PRICE");
				prc.setValidVal1(this.swpnPriceAtm[i][j] / this.notional);
				prc.setValidVal2(prcHw[i][j] / this.notional);
				prc.setValidVal3(errAbsPrc / this.notional);				
				prc.setValidVal4(errRelPrc);
				
				IrValidParamHw vol = new IrValidParamHw();
				vol.setBaseYymm(dateToString(this.baseDate).substring(0,6));
				vol.setSwpnMatNum((double) this.swpnMat[i]);
				vol.setSwapTenNum((double) this.swapTenor[j]);
				vol.setValidDv("VOL");
				vol.setValidVal1(this.swpnVolMkt[i][j]);
				vol.setValidVal2(volHw[i][j]);
				vol.setValidVal3(errAbsVol);				
				vol.setValidVal4(errRelVol);
				
				validRslt.add(prc);
				validRslt.add(vol);
			}
		}
		log.info("Total RMS Error: {}, {}", errRelPrcSum, errRelVolSum);	
		
		return validRslt;
	}	
	

	private void setSwpnVolInfo(List<SwpnVolInfo> swpnVolInfo) {		

		TreeMap<Integer, Map<Integer, Double>> volMap = new TreeMap<Integer, Map<Integer, Double>>();
//		volMap = swpnVolInfo.stream().collect(Collectors.groupingBy(s -> s.getSwpnMat(), TreeMap::new, Collectors.toMap(SwpnVolInfo::getSwapTenor, SwpnVolInfo::getVol)));
		volMap = swpnVolInfo.stream().collect(Collectors.groupingBy(s -> s.getSwpnMat(), TreeMap::new, Collectors.toMap(SwpnVolInfo::getSwapTenor, SwpnVolInfo::getVol, (k, v) -> k, TreeMap::new)));
		
		this.swpnMat      = volMap.keySet().stream().mapToInt(Integer::intValue).toArray();
		this.swapTenor    = volMap.firstEntry().getValue().keySet().stream().mapToInt(Integer::intValue).toArray();
		this.swapMatTenor = new int[this.swpnMat.length][this.swapTenor.length];
		this.swpnVolMkt   = new double[this.swpnMat.length][this.swapTenor.length];
		
		int mat = 0;
		for(Map.Entry<Integer, Map<Integer, Double>> volArg : volMap.entrySet()) {
			int ten = 0;			
			for(Map.Entry<Integer, Double> arg : volArg.getValue().entrySet()) {
				this.swapMatTenor[mat][ten] = volArg.getKey() + arg.getKey();
				this.swpnVolMkt[mat][ten] = arg.getValue();				
				ten++;
			}
			mat++;
		}		
//		log.info("{}, {}, {}", this.swpnMat, this.swapTenor, this.swpnVolMkt);
//		log.info("tenor:{}, iRate:{}", this.tenor, this.iRateBase);
	}
	
//	1st Step 1-1: Preparation of Swap Cashflow from Smith-Wilson Result from baseCurve
	private void applySmithWilsonInterpoloation(int prjInterval, double lastLiquidPoint) {
		
		Map<Double, Double> ts = new TreeMap<Double, Double>();
		for(int i=0; i<this.tenor.length; i++) ts.put(this.tenor[i], this.iRateBase[i]);

		SmithWilsonKics sw = new SmithWilsonKics(this.baseDate, ts, this.cmpdType, this.isRealNumber, this.ltfr, this.ltfrT, (int) Math.round(lastLiquidPoint), prjInterval, 100, this.dayCountBasis);			
		this.swRsltList = sw.getSmithWilsonResultList();		

		this.pmtIdxBaseRate   = this.swRsltList.stream().map(s -> s.getMatTerm() * this.freq).mapToInt(Double::intValue).toArray();
		this.priceBaseRate    = this.swRsltList.stream().map(s -> s.getDcntFactor()).mapToDouble(Double::doubleValue).toArray();		
		this.spotContBaseRate = this.swRsltList.stream().map(s -> s.getSpotCont()).mapToDouble(Double::doubleValue).toArray();
		this.fwdContBaseRate  = this.swRsltList.stream().map(s -> s.getFwdCont()).mapToDouble(Double::doubleValue).toArray();		
	
//		swRsltList.stream().filter(s-> Double.parseDouble(s.getMatCd().substring(1, 5)) % 6 == 0)
//        				   .forEach(s->log.info("{}, {}, {} , {}, {}, {}, {}", s.getBaseDate(), s.getResultType(), s.getMatCd(), s.getSpotDisc(), s.getSpotCont(), s.getFwdDisc(), s.getDcntFactor(), s.getMatTerm()));
	}	

//	1st Step 1-2: Calculate Exercise Rate of Swaption and PV of Swap Cashflow
	private void calSwpnSwapRate() {
		
		this.swapRate      = new double[this.swpnMat.length][this.swapTenor.length];
		this.swapDfSum     = new double[this.swpnMat.length][this.swapTenor.length];		
				
		for(int i=0; i<this.swpnMat.length; i++) {
			for(int j=0; j<this.swapTenor.length; j++) {
				this.swapDfSum[i][j] = 0.0;		
				
				for(int k=0; k<pmtIdxBaseRate.length; k++) {								
					if((k+1) > this.swpnMat[i] * this.freq && (k+1) <= this.swapMatTenor[i][j] * this.freq) {
						this.swapDfSum[i][j] = this.swapDfSum[i][j] + this.priceBaseRate[k]; 
					}
				}
//				log.info("{}, {}, {}", this.swapDfSum[i][j], this.swpnMat[i]* this.freq, this.swapMatTenor[i][j] * this.freq);
//				(P(0,T1) - P(0,T2)) / (Term * SUM(P(0, T_i)) where Term(dt in year) = 1/freq)
				this.swapRate[i][j] = (this.priceBaseRate[this.swpnMat[i]* this.freq - 1] - this.priceBaseRate[this.swapMatTenor[i][j] * this.freq - 1]) / (this.swapDfSum[i][j] / this.freq);				
			}				
		}
//		log.info("{}, {}", this.swapRate);
//		log.info("{}, {}", this.swapDfSum);
	}
	
//	1st Step 1-3: Calculate At The Money Price of of Swaption	 
	private void calSwpnPriceAtm() {
		
		this.swpnPriceAtm = new double[this.swpnMat.length][this.swapTenor.length];
		
		for(int i=0; i<this.swpnMat.length; i++) {
			for(int j=0; j<this.swapTenor.length; j++) {
				
//				Only swaption maturity(irrelevant to Swap Tenor)
				double T    = this.swpnMat[i];
				double dPos = ( Math.log(this.swapRate[i][j]/this.swapRate[i][j]) + 0.5 * Math.pow(this.swpnVolMkt[i][j], 2) ) * T / (this.swpnVolMkt[i][j] * Math.sqrt(T));
				double dNeg = dPos - (this.swpnVolMkt[i][j] * Math.sqrt(T));
				
				this.swpnPriceAtm[i][j] = this.notional * (this.swapDfSum[i][j] / this.freq) 
						                                * (  this.swapRate[i][j] * new NormalDistribution().cumulativeProbability(dPos)
						                                   - this.swapRate[i][j] * new NormalDistribution().cumulativeProbability(dNeg) );    // for payer swaption				
				if(Math.abs(this.swpnPriceAtm[i][j]) < ZERO_DOUBLE) this.swpnPriceAtm[i][j] = 0.1;
			}		
		}
//		log.info("{}", this.swpnPriceAtm);
//		log.info("{}, {}", this.notional, this.swpnVolMkt);
	}
	
//	TODO: 2nd Step of Main Process: Find Optimal HW Calibration Parameters
	protected void optParasHw() {
		optParasHw(new int[] {0, 1, 2, 3, 4, 5}, new int[] {0, 1, 2, 3, 4, 5});
	}	
	
	
	private void optParasHw(int[] swpnMatIdx, int[] swapTenorIdx) {
		
		this.optParas = calibOptim(swpnMatIdx, swapTenorIdx, this.initParas);

//		if(this.alphaPiece[0] == 20) {
//			this.optParas = new double[] {1.0000000002413867E-4, 1.5119894642097673E-4,                               //for gesg 202106(single test(not averaging)) cost: 0.14237991395865218@step4
//										  0.005126022507930043, 0.005690356846430337, 0.006494641682371284, 0.006467138334194449, 0.004750355949848512, 0.0049345861641219855};   //for FY201912 alphaPiece = 20
//			this.costValue = 0.14237991395865218;
//		}
//		if(this.alphaPiece[0] == 10) {		
//			this.optParas = new double[] {1.0000000068705988E-4, 1.0000000765166482E-4,                               //for gesg 202106(single test(not averaging)) cost: 0.14238227739328224@step8
//					 					  0.005125302687553165, 0.005690841500495152, 0.006500628075446153, 0.006458247443144752, 0.0047640339845994445, 0.004932460351313572};   //for FY201912 alphaPiece = 10
//			this.costValue = 0.14238227739328224;
//		}

//		this.optParas = new double[] {1.0000000010471885E-4, 2.0820000153992988E-2,   // 1.0000000135115319E-4
//									  0.004565115436375785, 0.004892184858700254, 0.006470078751480329, 0.0062714723266829, 0.00454586154403205, 0.0057704779539927906};      //for FY201912 alphaPiece = 10
//
//		this.optParas = new double[] {1.0000000002474886E-4, 2.0820000153992988E-2,   // 1.0000000153992988E-4 
//				  					  0.004376612344832952, 0.004938664979161124, 0.006882974719902386, 0.007013850810454117, 0.006283631473349515, 0.005806346757368729};    //for FY202012 alphaPiece = 10
//
//		this.optParas = new double[] {1.0000000096329606E-4, 1.0000000087788117E-4,                                //for gesgh 201912 as of 20210831(CAL)
//									  0.004565250516420857, 0.004890708041952311, 0.006460821352955575, 0.006267501479722056, 0.004496497971815339, 0.0056377010920240864};   //for FY201912 alphaPiece = 10
//
//		this.optParas = new double[] {0.0001, 0.0182222546,                                                        //for gesgh 201912 as of 20210831(BIZ)
//									  0.0045652505, 0.004890708, 0.0064608214, 0.0062675015, 0.004496498, 0.0056377011, 0.0047397239};  //for FY201912 alphaPiece = 10		
//
//		this.optParas = new double[] {0.0001, 0.0001,                                                              //for gesgh 201912(single test(not averaging))
//				  					  0.0045652505, 0.004890708, 0.0064608214, 0.0062675015, 0.004496498, 0.0056377011, 0.0056377011};  //for FY201912 alphaPiece = 10		
	}	
	

	protected double[] calibOptim(int[] swpnMatIdx, int[] swapTenorIdx, double[] paras) {

		MultivariateFunction fp = new MultivariateFunction() {
			public double value(double[] paras) {
				return calibErrFn(swpnMatIdx, swapTenorIdx, paras);
			}
		};
		
		double[] fpLower = new double[paras.length];
		double[] fpUpper = new double[paras.length];
		double[] fpScale = new double[paras.length];
		
		for(int i=0; i<paras.length; i++) {
			fpLower[i] = (i<=0) ? this.alphaMin : this.sigmaMin;
			fpUpper[i] = (i<=0) ? 2e-1 : 2e-2;
			fpScale[i] = 1000;
		}		
	    MultivariateFunctionPenaltyAdapter fpConstr = new MultivariateFunctionPenaltyAdapter(fp, fpLower, fpUpper, 1000, fpScale);
		
		double[] calibParas = paras;
		double   calibValue = 0.0;		
		
		log.info("{}, {}, {}", LocalDateTime.now(), this.accuracy, paras);		
		try {			
			for(int i=0; i<this.itrMax; i++) {		
				
				SimplexOptimizer optimizer = new SimplexOptimizer(1e-12, 1e-12);
				AbstractSimplex  ndsimplex = new NelderMeadSimplex(nelderMeadStep(calibParas, 0.001));
				PointValuePair   result    = optimizer.optimize(new MaxEval(100000)
//						                                      , new ObjectiveFunction(fp)
						                                      , new ObjectiveFunction(fpConstr)
						                                      , ndsimplex
						                                      , GoalType.MINIMIZE
						                                      , new InitialGuess(calibParas));				
				
				log.info("{}, {}, {}, {}", i, result.getValue(), LocalDateTime.now(), result.getPoint());		
				calibParas = result.getPoint();
				this.costValue = result.getValue();
				
				if(Math.abs(result.getValue() - calibValue) < this.accuracy) break;	
				calibValue = result.getValue();
				
			}
		}
		catch (Exception e) {
			log.error("Error in Cailibration of Hull-White 1 Factor Model Parameters [Calibration Mode = KICS]");
			e.printStackTrace();
		}		
		log.info("{}, {}", LocalDateTime.now(), calibParas);		
	
		return calibParas;
	}
	
	
	private double[] nelderMeadStep(double[] inputParas, double scale) {
		
		double[] step = new double[inputParas.length];
		for(int i=0; i<step.length; i++) {
			step[i] = Math.max(Math.abs(inputParas[i] * scale), SIMPLEX_STEP_MIN);
		}
//		log.info("step: {}", step);
		return step;		
	}
	
	
	private double calibErrFn(int[] swpnMatIdx, int[] swapTenorIdx, double[] paras) {
		
		double[][] swpnPrcHw = new double[swpnMatIdx.length][swapTenorIdx.length];
		double err = 0.0;
		double t1  = 0.0;
		double t2  = 0.0;
		int    k   = 0;     
		int    l   = 0;
		
		double[] alpha = new double[] {paras[0]};
		double[] sigma = new double[] {paras[1], paras[2], paras[3], paras[4], paras[5], paras[6]};

		for(int i=0; i<swpnMatIdx.length; i++) {
			for(int j=0; j<swapTenorIdx.length; j++) {
				k  = swpnMatIdx[i];
				l  = swapTenorIdx[j];
				t1 = swpnMat[k];
				t2 = swapMatTenor[k][l];				
			
				swpnPrcHw[i][j] =  swpnPriceHw(t1, t2, alpha, sigma, this.swapRate[k][l]);
                err += Math.pow( (this.swpnPriceAtm[k][l] - swpnPrcHw[i][j]) / this.swpnPriceAtm[k][l], 2);                 
//                log.info("k:{}, l:{}, t1:{}, t2:{}, alpha:{}, sigma:{}, swpnPrcHw[i][j]:{}, swpnPriceAtm[k][l]:{}, err:{}", k, l, t1, t2, alpha, sigma, swpnPrcHw[i][j], this.swpnPriceAtm[k][l], err);
			}
		}
//		log.info("err:{}, paras:{}", err, paras);    
		return err;
	}	
	
	
	private double swpnPriceHw(double t1, double t2, double[] alpha, double[] sigma, double K) {
		
		double dt     = 1.0 / this.freq;
		double rOptim = 0.0;
		
		UnivariateFunction fp = new UnivariateFunction() {
			public double value(double sRate) {
				return rOptimSwpnErrFn(t1, t2, alpha, sigma, dt, K, sRate);
			}
		};
			
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
		rOptim = optimizer.optimize(new MaxEval(10000)
				                  , new UnivariateObjectiveFunction(fp)
				                  , GoalType.MINIMIZE
				                  , new SearchInterval(-0.9, 1.0)).getPoint();
		
//		Main Process w/ Jamshidian Decomposition for Receiver Swaption)		
		double[] swpnDt      = swpnDt(t1, t2, dt);
		double[] swpnCf      = swpnCf(t1, t2, dt, K);		
		double[] swpnSigma   = swpnSigma(t1, t2, alpha, sigma, dt);
		double   swpnPriceHw = 0.0;        

		for(int i=0; i<swpnDt.length; i++) {
        	
        	double xZbc  = hwZcbPrice(t1, swpnDt[i], alpha, sigma, rOptim);
        	double pt0   = hwZcbPrice( 0,        t1, alpha, sigma, this.fwdContBaseRate[0]);
        	double pti   = hwZcbPrice( 0, swpnDt[i], alpha, sigma, this.fwdContBaseRate[0]);
        	double dPos  = Math.log(pti / xZbc / pt0) / swpnSigma[i] + 0.5 * swpnSigma[i];
        	double dNeg  = dPos - swpnSigma[i];
        	swpnPriceHw += this.notional * swpnCf[i] * (pti * new NormalDistribution().cumulativeProbability(dPos) - xZbc * pt0 * new NormalDistribution().cumulativeProbability(dNeg));
        	
//        	log.info("i: {}, P_T0: {}, P_Ti: {}, xZbc: {}, dPos: {}, dNeg: {}, swpnPriceHw: {}", i, pt0, pti, xZbc, dPos, dNeg, swpnPriceHw);
        }        
        return swpnPriceHw;
	}
	
	
	private double rOptimSwpnErrFn(double t1, double t2, double[] alpha, double[] sigma, double dt, double K, double sRate) {
		
		double[] swpnDt = swpnDt(t1, t2, dt);
		double[] swpnCf = swpnCf(t1, t2, dt, K);
		double   sum    = 0.0;		
		
		for(int i=0; i<swpnCf.length; i++) {
			sum += swpnCf[i] * hwZcbPrice(t1, swpnDt[i], alpha, sigma, sRate);	      // Sum[(CashFlow * DiscountFactor] must be equal to 1		
		}
		return Math.pow(sum - 1.0, 2);                                      
	}
	
	
	private double hwZcbPrice(double t1, double t2, double[] alpha, double[] sigma, double sRate) {		
		return coefA(t1, t2, alpha, sigma) * Math.exp(-coefB(t1, t2, alpha) * sRate);				
	}
	
	//TODO:
	private double coefA(double t1, double t2, double[] alpha, double[] sigma) {
		
		double prc1   = 0.0;
		double prc2   = 0.0;
		double fwd12  = 0.0;
		double coefA  = 0.0;
		
		double fwd0   = this.fwdContBaseRate[0];
		double prc0   = 1.0;		
		int    idx1   = (int) Math.round(t1 * this.freq) - 1;
		int    idx2   = (int) Math.round(t2 * this.freq) - 1;

		if(idx1 < 0) {
			prc1  = prc0;
			fwd12 = fwd0;
		}
		else {			
			prc1  = this.priceBaseRate[idx1]; 
			fwd12 = this.fwdContBaseRate[idx1];
		}
		prc2  = this.priceBaseRate[idx2];				
		coefA = (prc2/prc1) * Math.exp( (coefB(t1, t2, alpha)*fwd12) - 0.5 * Math.pow(coefB(t1, t2, alpha), 2) * coefZeta(t1, alpha, sigma) );
//		log.info("t1: {}, t2: {}, alpha: {}, sigma: {}, coefA: {}, coefB: {}, sigmaP: {}", t1, t2, alpha, sigma, coefA, coefB(t1, t2, alpha),  0.5 * Math.pow(coefB(t1, t2, alpha), 2) * coefZeta(t1, alpha, sigma));
		
		return coefA;
	}	
	
//	[Integral sigma(s)^2 * exp(-2 *a(s) * (t1-s))] * ds for swaption maturity t1	
	protected double coefZeta(double t1, double[] alpha, double sigma[]) {
		
		int tau      = this.alphaPiece[0];
		double zeta  = 0.0;		
		
		if(compareDbltoInt(t1, tau) < 1) {			
			
			for(int i=0; i<this.sigmaPiece.length; i++) {				
				double sigmaPiece_0 = ((i==0) ? 0 : sigmaPiece[i-1]);
				
//				double t1 is greater then or equal to sigmaPiece, then return 1 or 0 
				if(compareDbltoInt(t1, this.sigmaPiece[i]) > -1) {
					zeta += Math.exp(-2*alpha[0]*t1) * Math.pow(sigma[i], 2) * (Math.exp(2*alpha[0]*this.sigmaPiece[i]) - Math.exp(2*alpha[0]*sigmaPiece_0)) / (2*alpha[0]);		
//					log.info("t1: {}, sigmaPiece: {}, compare: {}", t1, this.sigmaPiece[i], compareDbltoInt(t1, this.sigmaPiece[i]));
				}						
			}
			if(Math.abs(zeta) < ZERO_DOUBLE) {
				zeta  = Math.exp(-2*alpha[0]*t1) * Math.pow(sigma[0], 2) * (Math.exp(2*alpha[0]*t1) - 1.0) / (2*alpha[0]);
			}
		}
		else {
//			log.warn("Unreachable coefZeta in KICS requirement");
			zeta = Math.exp(-2*alpha[0]*t1) * Math.pow(sigma[0], 2) * (Math.exp(2*alpha[0]*t1) - 1.0) / (2*alpha[0]);
		}
//		log.info("zeta: {}, cal_zeta: {}", zeta, Math.exp(-2*alpha[0]*t1) * Math.pow(sigma[0], 2) * (Math.exp(2*alpha[0]*t1) - 1.0) / (2*alpha[0]);
		return zeta;
	}	
			
	//TODO:	compareDbltoInt(A, B) if A>B then return 1, else if A=B then return 0, else(A<B) return -1 		
	private double coefB(double t1, double t2, double[] alpha) {
		
		int tau = this.alphaPiece[0];
		
//		double t2 is less than or equal to integer tau, then return -1 or 0
		if(compareDbltoInt(t2,  tau) < 1) {
			return 1/alpha[0] * (1 - Math.exp(-alpha[0]*(t2 - t1)));			
		}
//		double t1 is greater than integer tau then return 1
		else if(compareDbltoInt(t1,  tau) > 0) {
//			log.warn("Unreachable coefB  in KICS requirement");
			return 1/alpha[1] * (1 - Math.exp(-alpha[1]*(t2 - t1)));
		}
//		tau is in between t1 and t2
		else {
//			log.warn("Unreachable coefB in KICS requirement in Non-Split Calibration");
			return 1/alpha[0] * (1 - Math.exp(-alpha[0]*(tau - t1))) + Math.exp(-alpha[0]*(tau - t1)) / alpha[1] * (1 - Math.exp(-alpha[0]*(t2 - tau)));
//			return 1/alpha[0] * (1 - Math.exp(-alpha[0]*(tau - t1))) + 1 / alpha[1] * (1 - Math.exp(-alpha[1]*(t2 - tau)));
		}		
	}
	

	private double[] swpnDt(double t1, double t2, double dt) {
		
		int      idx    = (int) Math.round((t2-t1) / dt);
		double[] swpnDt = new double[idx];		
		for(int i=0; i<swpnDt.length; i++) swpnDt[i] = t1 + dt * (i+1);
		
		return swpnDt;
	}
	
	
	private double[] swpnCf(double t1, double t2, double dt, double K) {
		
		double[] swpnDt = swpnDt(t1, t2, dt);
		double[] swpnCf = new double[swpnDt.length];
		
		for(int i=0; i<swpnCf.length; i++) swpnCf[i] = K * dt;
		swpnCf[swpnCf.length-1] = swpnCf[swpnCf.length-1] + 1.0;            //principal cashflow is added
		
		return swpnCf;
	}	
	
	
	private double[] swpnSigma(double t1, double t2, double[] alpha, double[] sigma, double dt) {
		
		double[] swpnDt    = swpnDt(t1, t2, dt);
		double[] swpnSigma = new double[swpnDt.length];		

		for(int i=0; i<swpnSigma.length; i++) {	
			swpnSigma[i] = Math.sqrt( Math.pow(coefB(t1, swpnDt[i], alpha), 2) * coefZeta(t1, alpha, sigma) );
//			log.info("i: {}, t1: {}, t2: {}, swpnDt[i]: {}, B^2(t1, t2): {}, Zeta: {}, swpnSigma: {}", i, t1, t2, swpnDt[i], Math.pow(coefB(t1, swpnDt[i], alpha), 2), coefZeta(t1, alpha, sigma), swpnSigma[i]);
		}		
		return swpnSigma;		
	}
	
	
	protected void checkSwpnVolDiff() {		
		
		double[][] volHw = new double[this.swpnMat.length][this.swapTenor.length];		
		double[][] prcHw = new double[this.swpnMat.length][this.swapTenor.length];
		
		double[]   alpha = new double[] {this.optParas[0]};
		double[]   sigma = new double[] {this.optParas[1], this.optParas[2], this.optParas[3], this.optParas[4], this.optParas[5], this.optParas[6]};		
		double     err   = 0.0; 
		
		for(int i=0; i<this.swpnMat.length; i++) {
			for(int j=0; j<this.swapTenor.length; j++) {				
	
				prcHw[i][j] = swpnPriceHw(this.swpnMat[i], this.swapMatTenor[i][j], alpha, sigma, this.swapRate[i][j]);	
				volHw[i][j] = swpnVolHw  (this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], prcHw[i][j]);
				err += Math.pow( (this.swpnPriceAtm[i][j] - prcHw[i][j]) / this.swpnPriceAtm[i][j], 2);	
				
				double check1 = calSwpnPriceAtmFromHwVol(this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], volHw[i][j]);
				double check2 = calSwpnPriceAtmFromHwVol(this.swpnMat[i], this.swapRate[i][j], this.swapDfSum[i][j], this.swpnVolMkt[i][j]);
				log.info("T1:{}, T2:{}, PRC_BK:{}, PRC_HW:{}, VOL_MK:{}, VOL_HW:{}, PRC_HW_CK1:{}, PRC_HW_CK2: {}", swpnMat[i], swapMatTenor[i][j], swpnPriceAtm[i][j], prcHw[i][j], swpnVolMkt[i][j], volHw[i][j], check1, check2);
			}
		}
		log.info("Total RMS Error: {}", err);
	}

	
	private double swpnVolHw(int swpnMat, double swapRate, double swapDfSum, double swpnPriceHw) {
		
		double vol = 0.0;
		
		UnivariateFunction fp = new UnivariateFunction() {
			public double value(double swpnVolHw) {
				return swpnPrcOptimErrFn(swpnMat, swapRate, swapDfSum, swpnVolHw, swpnPriceHw);
			}
		};
			
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
		vol = optimizer.optimize(new MaxEval(10000)
			                   , new UnivariateObjectiveFunction(fp)
			                   , GoalType.MINIMIZE
			                   , new SearchInterval(1e-4, 1.0)).getPoint();	
		
		return vol;
	}
	

	private double swpnPrcOptimErrFn(int swpnMat, double swapRate, double swapDfSum, double swpnVolHw, double swpnPriceHw) {
		
		double T = swpnMat;
		double dPos = 0.5 * Math.pow(swpnVolHw, 2) * T / (swpnVolHw * Math.sqrt(T));
		double dNeg = dPos - (swpnVolHw * Math.sqrt(T));
		
		double swpnPrice = this.notional * (  swapDfSum / this.freq )
				                         * (  swapRate * new NormalDistribution().cumulativeProbability(dPos)
				                            - swapRate * new NormalDistribution().cumulativeProbability(dNeg) );
		
		return Math.pow(swpnPrice - swpnPriceHw, 2);	
	}
	
	
	private double calSwpnPriceAtmFromHwVol(int swpnMat, double swapRate, double swapDfSum, double swpnVolHw) {
		
		double T = swpnMat;
		double dPos = 0.5 * Math.pow(swpnVolHw, 2) * T / (swpnVolHw * Math.sqrt(T));
		double dNeg = dPos - (swpnVolHw * Math.sqrt(T));
		
		double swpnPriceAtm = this.notional * (  swapDfSum / this.freq )
				                            * (  swapRate * new NormalDistribution().cumulativeProbability(dPos)
				                               - swapRate * new NormalDistribution().cumulativeProbability(dNeg) );
		
		return swpnPriceAtm;	
	}
	
	
	protected void checkSwpnPriceHw() {
	
		double[][] zcb     = new double[this.swpnMat.length][this.swapTenor.length];
		double[][] vol     = new double[this.swpnMat.length][this.swapTenor.length];
		this.swpnPriceHw   = new double[this.swpnMat.length][this.swapTenor.length];
		
		double[] initParas = new double[] {0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02};
		double[] alpha     = new double[] {initParas[0]};
		double[] sigma     = new double[] {initParas[1], initParas[2], initParas[3], initParas[4], initParas[5], initParas[6]};

		
		for(int i=0; i<this.swpnMat.length; i++) {
			for(int j=0; j<this.swapTenor.length; j++) {				
				
				double t1 = this.swpnMat[i];
				double t2 = this.swapMatTenor[i][j];
				double K  = this.swapRate[i][j];
				
				zcb[i][j] = hwZcbPrice(t1, t2, alpha, sigma, this.fwdContBaseRate[0]);
				vol[i][j] = Math.sqrt( Math.pow(coefB(t1, t2, alpha), 2) * coefZeta(t1, alpha, sigma) );				
				this.swpnPriceHw[i][j] = swpnPriceHw(t1, t2, alpha, sigma, K);
				
//				log.info("T1: {}, T2: {}, ZCB: {}, VOL: {}, PRC: {}, K: {}", t1, t2, zcb[i][j], vol[i][j], this.swpnPriceHw[i][j], K);
			}
		}
	}

}		
	