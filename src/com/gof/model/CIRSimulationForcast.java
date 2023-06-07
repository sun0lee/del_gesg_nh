package com.gof.model;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.gof.entity.IrDcntSceStoGnr;
import com.gof.entity.IrParamModelBiz;
import com.gof.util.DateUtil;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class CIRSimulationForcast extends IrModel {
	
	protected double        dt;	
	protected double        rZero;
	protected double        alpha;
	protected double        theta;
	protected double        sigma;	
                   
	protected int           prjYear;
	protected int           prjMonth;
	protected int           scenNum;
	protected int           seed;
	
	protected double[][]    randNum;
	protected double[][]    sRateScen;
	protected double[][]    spotContScen;
	protected double[][]    spotDiscScen;
	protected double[][]    fwdContScen;
	protected double[][]    fwdDiscScen;	
	protected double[][]    dcntFactorScen;
	
	protected double[]      sRateMean;
	protected double[]      spotContMean;
	protected double[]      spotDiscMean;
	protected double[]      fwdContMean;
	protected double[]      fwdDiscMean;
	protected double[]      dcntFactorMean;	
	
	
	@Builder(builderClassName="of", builderMethodName="of")
	public CIRSimulationForcast(String bssd, List<IrParamModelBiz> irParamModelBizList, Double dt, Integer prjYear, Integer scenNum, Integer seed) {				
		super();		
		
		this.baseDate = (baseDate == null ? LocalDate.now() : DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth()));
		this.setIrmodelAttributes(irParamModelBizList);
		this.dt       = (dt      == null ? 1.0 / MONTH_IN_YEAR : dt);
		this.prjYear  = (prjYear == null ? 30        : prjYear);
		this.prjMonth = this.prjYear * MONTH_IN_YEAR;
		this.scenNum  = (scenNum == null ? 2000      : scenNum);		
		this.seed     = (seed    == null ? 470       : seed);
		this.randomNumberGaussian();
	}	
	

	public List<IrDcntSceStoGnr> getSimulationResult() {
		
		List<IrDcntSceStoGnr> rst = new ArrayList<IrDcntSceStoGnr>();
		
		this.calShortRate();
		this.calCirTermStructure();		
		
		for(int i=0; i<this.prjMonth; i++) {
			for(int j=0; j<this.scenNum; j++) {				
				IrDcntSceStoGnr sce = new IrDcntSceStoGnr();
				sce.setMatCd(String.format("%s%04d", "M", i+1));
				sce.setSceNo(j+1);
				sce.setSpotRate(this.sRateScen[i][j]);
				sce.setFwdRate(this.sRateScen[i][j]);
				
				rst.add(sce);				
			}
		}		
		return rst;		
	}	
	
	
	private void setIrmodelAttributes(List<IrParamModelBiz> irParamModelBizList) {
		
		try {
			this.rZero = irParamModelBizList.stream().filter(s -> s.getParamTypCd().equals("R_ZERO")).map(s -> s.getParamVal()).mapToDouble(Double::doubleValue).toArray()[0];
			this.alpha = irParamModelBizList.stream().filter(s -> s.getParamTypCd().equals("ALPHA" )).map(s -> s.getParamVal()).mapToDouble(Double::doubleValue).toArray()[0];
			this.theta = irParamModelBizList.stream().filter(s -> s.getParamTypCd().equals("THETA" )).map(s -> s.getParamVal()).mapToDouble(Double::doubleValue).toArray()[0];
			this.sigma = irParamModelBizList.stream().filter(s -> s.getParamTypCd().equals("SIGMA" )).map(s -> s.getParamVal()).mapToDouble(Double::doubleValue).toArray()[0];
		}
		catch(Exception e) {
			log.error("Check CIR Parameter");
		}
		
		log.info("rZero: {}, alpha: {}, theta: {}, sigma: {}", this.rZero, this.alpha, this.theta, this.sigma);
	}

	
	private void randomNumberGaussian() {		
		this.randNum = new RandomNumberKics(this.prjMonth, this.scenNum, this.seed).mersenneTwisterKics();		
	}
		

	/**
	 * dr_t = k[θ-r(t)]*dt + σ√(r(t))*dW(t)
	 * (r_t-r_(t-1)) / √(r_(t-1)) = (kθ*dt) / √(r_(t-1))- k√(r_(t-1)) * dt + σdW(t)
	 * rt+Δt − rt = k * (θ − rt)Δt + σ√rt * εt
	 */	
	private void calShortRate() {
		
		this.sRateScen = new double[this.prjMonth][this.scenNum];
		
//		double expm1Kdt = Math.exp(-1 * this.alpha * this.dt);
//		double expm2Kdt = Math.exp(-2 * this.alpha * this.dt);
//		double sigmaSq  = Math.pow(this.sigma, 2);
		
		for(int i=0; i<sRateScen.length; i++) {			
			for(int j=0; j<sRateScen[i].length; j++) {

				double sRateBef = Math.max((i==0) ? this.rZero : this.sRateScen[i-1][j], 1E-6);
				this.sRateScen[i][j] = sRateBef + this.alpha * (this.theta - sRateBef) * this.dt + this.sigma * Math.sqrt(sRateBef * this.dt) * this.randNum[i][j];
//				this.sRateScen[i][j] = sRateBef + this.alpha * (this.theta - sRateBef) * this.dt + this.sigma * Math.sqrt(sRateBef * this.dt) * 0.1;
//				this.sRateScen[i][j] = sRateBef + this.alpha * (this.theta - sRateBef)           + this.sigma * Math.sqrt(sRateBef          ) * 0.1;
	
//				this.sRateScen[i][j]  = sRateBef * expm1Kdt + this.theta * (1.0 - expm1Kdt);
//				this.sRateScen[i][j] += Math.sqrt(sRateBef * sigmaSq / this.alpha * (expm1Kdt - expm2Kdt) +  0.5 * this.theta * sigmaSq / this.alpha * Math.pow(1.0 - expm1Kdt, 2)) * (1.0 * this.randNum[i][j] + 0.0);
				
//				log.info("i,j: [{}, {}], sRate: {}, sRateBef: {}, theta-rt: {}, muTerm: {}", i+1, j+1, this.sRateScen[i][j], sRateBef, (this.theta - sRateBef),  this.alpha * (this.theta - sRateBef));
			}
		}		
	}
	
	
	private void calCirTermStructure() {
		
//		this.dcntFactorScen = new double[this.prjMonth][this.scenNum];
//		this.spotContScen   = new double[this.prjMonth][this.scenNum];
//		this.spotDiscScen   = new double[this.prjMonth][this.scenNum];
//		this.fwdContScen    = new double[this.prjMonth][this.scenNum];
//		this.fwdDiscScen    = new double[this.prjMonth][this.scenNum];	
//		
//		for(int j=0; j<this.scenNum; j++) {
//			this.dcntFactorScen[0][j] =  1.0 * Math.exp(-this.sRateScen[0][j] * this.dt);                     //  Df[t0,] = 1 (i.e. P(0,0)), this.dcntFactorScen[-1][j] = 1.0
//			this.spotContScen  [0][j] = -1.0 * Math.log(this.dcntFactorScen[0][j]) / this.timeFactor[0];
//			this.spotDiscScen  [0][j] = irContToDisc(this.spotContScen[0][j]);
//			this.fwdContScen   [0][j] = this.spotContScen[0][j];
//			this.fwdDiscScen   [0][j] = irContToDisc(this.fwdContScen[0][j]);
//		}
//		
//		for(int i=0; i<this.prjMonth-1; i++) {
//			for(int j=0; j<this.scenNum; j++) {
//				this.dcntFactorScen[i+1][j] = this.dcntFactorScen[i][j] * Math.exp(-this.sRateScen[i+1][j] * this.dt);
//				this.spotContScen  [i+1][j] = -1.0 * Math.log(this.dcntFactorScen[i+1][j]) / this.timeFactor[i+1];
//				this.spotDiscScen  [i+1][j] = irContToDisc(this.spotContScen[i+1][j]);
//				this.fwdContScen   [i+1][j] = (this.spotContScen[i+1][j] * this.timeFactor[i+1] - this.spotContScen[i][j] * this.timeFactor[i]) / this.dt;
//				this.fwdDiscScen   [i+1][j] = irContToDisc(this.fwdContScen[i+1][j]);
//			}			
//		}
//		
//		this.dcntFactorMean = matToVecMean(this.dcntFactorScen);
//		this.spotContMean   = matToVecMean(this.spotContScen  );
//		this.spotDiscMean   = matToVecMean(this.spotDiscScen  );
//		this.fwdContMean    = matToVecMean(this.fwdContScen   );
//		this.fwdDiscMean    = matToVecMean(this.fwdDiscScen   );		
//		
//		for(int i=0; i<this.prjMonth; i++) {
////			if(i==719 || i==800 || i==1199) log.info("{}, {}, {}, {}", i+1, this.fwdDiscMean[i]);
////			if((i+1)%120==0) log.info("{}, {}, {}, {}", i+1, this.fwdDiscMean[i]);
//		}		
	}		
	
}		
	