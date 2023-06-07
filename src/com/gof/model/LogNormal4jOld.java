package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import com.gof.entity.StdAsstIrSceSto;
import com.gof.entity.IrParamHwRnd;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogNormal4jOld {

	private String baseYymm;
	private Map<Integer, Double> driftMap ;
	private Map<Integer, Double> sigmaMap ;
	
	private double[][] randomNum;
	private int seedNum;
	private int tenorSize; 
	private int sceNum; 
	
	public LogNormal4jOld() {
	
	}

	public LogNormal4jOld(String baseYymm, Map<Integer, Double> driftMap, Map<Integer, Double> sigmaMap, int tenorSize, int batchNum) {
		this.baseYymm = baseYymm;
		
		this.driftMap  = driftMap;
		this.sigmaMap  = sigmaMap;
		this.sceNum    = batchNum * 100 ;
		this.tenorSize = tenorSize;
		
		this.randomNum = createRandom(sceNum +1, tenorSize);
	}
	
	
	public LogNormal4jOld(String baseYymm, Map<Integer, Double> driftMap, Map<Integer, Double> sigmaMap, int tenorSize, int batchNum, int seedNum) {
		this.baseYymm = baseYymm;
		
		this.driftMap  = driftMap;
		this.sigmaMap  = sigmaMap;
		this.sceNum    = batchNum * 100 ;
		this.tenorSize = tenorSize;
		this.seedNum   = seedNum;
		
		this.randomNum = createRandomSeed(sceNum + 1, tenorSize);
	}	
	
	public List<StdAsstIrSceSto> getBizStockScenario(String bssd, String assetCd, String bizDv, int batch){
		List<StdAsstIrSceSto> rstList = new ArrayList<StdAsstIrSceSto>();
		
		double yieldSce =0.0;
		double dt = 1/12.0;
		
		for(int j = (batch-1) * 100 + 1 ; j <= batch * 100; j++) {
			for(int k =1 ; k<tenorSize; k++) {
				String matCd ="M"+ String.format("%04d", k);
				String sceNo =String.valueOf(j);
				
				double drift = driftMap.get(k);
				double sigma = sigmaMap.get(k);
//				double drift = drift[k];
//				double sigma = sigma[k];
			
//				currStockSce = prevStockSce * Math.exp( ( drift -  0.5* sigma * sigma ) * dt   + sigma * Math.sqrt(dt) * randomNum[j][k]);
//				prevStockSce = currStockSce;
				
				//TODO: check indexes notation!!!
//				yieldSce =  ( drift ) * dt   + sigma * Math.sqrt(dt) *  randomNum[j][k];   //old indexes notation(not applying seedNum)
				yieldSce =  ( drift ) * dt   + sigma * Math.sqrt(dt) *  randomNum[k][j];   //new indexes notation(applying seedNum)
				
//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", j, k, bssd, bizDv, sceNo, matCd, assetCd, drift, sigma, randomNum[j][k], yieldSce, dt);
				rstList.add(StdAsstIrSceSto.builder()
						.baseYymm(bssd)
						.applBizDv(bizDv)
						.sceNo(Integer.valueOf(sceNo))
//						.sceNo(String.valueOf(aa.getSceNo()))
//						.matCd( "M" + String.format("%04d", k))
						.matCd( matCd)
						.stdAsstCd(assetCd)
//						.asstYield(yieldSce)
//						.asstYield(Math.exp(yieldSce / dt)-1 )
//						.asstYield(Math.exp(yieldSce / dt)-1 )
						.asstYield( yieldSce / (12.0 * dt) )			//TODO :Check !!!!!
						.lastModifiedBy("ESG")
						.lastUpdateDate(LocalDateTime.now())
						.build()
						);
			}
		}
		log.info("BizStocktSce : {},{}", batch, rstList.size());
		return rstList;
	}
	
	
	public List<IrParamHwRnd> getRandomScenList() {
		
		List<IrParamHwRnd> randList = new ArrayList<IrParamHwRnd>();
			
		for(int i=0; i<this.randomNum.length; i++) {
			for(int j=0; j<this.randomNum[i].length; j++) {
				
				IrParamHwRnd rand = new IrParamHwRnd();				
				rand.setBaseYymm(this.baseYymm);
				rand.setIrModelId("KOSPI200");
				rand.setIrCurveId("KOSPI200");
				rand.setMatCd(String.format("%s%04d", IrModel.TIME_UNIT_MONTH, (i+1)));				
				rand.setSceNo(Integer.valueOf(j+1));				
				rand.setRndNum(this.randomNum[i][j]);
				rand.setLastModifiedBy("ESG");
				rand.setLastUpdateDate(LocalDateTime.now());
				
				randList.add(rand);				
			}
		}		
		return randList;
	}		
	
	
	private double[][] createRandomSeed(int sceNo, int projectionTerm) {
		
		double JB_TEST_TOL = 6.0;
		double RUNS_TEST_TOL = 0.020;
		
		GaussianRandomGenerator grg = new GaussianRandomGenerator(new MersenneTwister(Long.valueOf(this.seedNum)));		
		this.randomNum   = new double[projectionTerm][sceNo];
		
		for(int i=0; i<this.randomNum.length; i++) {
			for(int j=0; j<this.randomNum[i].length; j++) {
				this.randomNum[i][j] = grg.nextNormalizedDouble();
			}
		}			
		for(int i=0; i<this.randomNum.length; i++) this.randomNum[i] = IrModel.vecToZeroMean(this.randomNum[i]);
		
		for(int k=0; k<1; k++) {
			//TODO:JB Test and re-generation
			for(int i=0; i<this.randomNum.length; i++) {
				if(IrModel.vecJBtest(this.randomNum[i]) > JB_TEST_TOL) {
//					log.info("JB {}th, i={}th, {}", k+1, i, IrmodelHw.vecJBtest(this.randomNum[i]));				
					for(int j=0; j<sceNo; j++) {
						this.randomNum[i][j] = grg.nextNormalizedDouble();
					}				
				}
			}

			//Runs Test and re-generation
			double[][] randNumT = IrModel.matTranspose(this.randomNum);			
			for(int j=0; j<sceNo; j++) {		
				if(IrModel.runsTest(randNumT[j]) < RUNS_TEST_TOL) {
//					log.info("Runs {}th, j={}th, {}", k+1, j+1, IrmodelHw.runsTest(randNumT[j]));
					for(int i=0; i<projectionTerm; i++) {
						this.randomNum[i][j] = grg.nextNormalizedDouble();
					}
				}
			}
//			for(int i=0; i<projectionTerm; i++) this.randomNum[i] = IrmodelHw.vecToZeroMean(this.randomNum[i]);
		}
		for(int i=0; i<this.randomNum.length; i++) this.randomNum[i] = IrModel.vecToZeroMean(this.randomNum[i]);
		
		return this.randomNum;
	}	
	
	
	private double[][] createRandom(int sceNo, int projectionTerm) {
		int genSceNum =  (sceNo - 1)/2;
		double[][] random = new double[sceNo][projectionTerm];
		
		GaussianRandomGenerator gen = new GaussianRandomGenerator(new MersenneTwister(Long.valueOf(baseYymm)));
		
		for(int i =1 ; i<= genSceNum ; i++) {
			for(int j=1 ; j< projectionTerm; j++) {
				double randomNum = gen.nextNormalizedDouble();
				random[2*i-1][j] = randomNum;
				random[2*i][j] = -1.0 * randomNum;
			}
		}
		return random;
	}
	
}
