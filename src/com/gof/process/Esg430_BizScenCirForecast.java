package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.gof.entity.IrDcntSceStoGnr;
import com.gof.entity.IrParamModelBiz;
import com.gof.entity.IrParamModelRnd;
import com.gof.entity.IrQvalSce;
import com.gof.enums.EJob;
import com.gof.model.CIRSimulationForcast;
import com.gof.model.RandomNumberKics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg430_BizScenCirForecast extends Process {	
	
	public static final Esg430_BizScenCirForecast INSTANCE = new Esg430_BizScenCirForecast();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntSceStoGnr> createScenCir(String bssd, String irModelId, String irCurveId, List<IrParamModelBiz> irParamModelBizList, Double dt, Integer prjYear, Integer scenNum, Integer seed) {	

		List<IrDcntSceStoGnr> rst = new ArrayList<IrDcntSceStoGnr>();
		
		if(irParamModelBizList.isEmpty()) {
			log.warn("No IR Model Parameter of [MODEL:{}, ID:{}] in [{}]", irModelId, irCurveId, jobId);			
			return rst;
		}
		
		CIRSimulationForcast cir = CIRSimulationForcast.of()
				                   					   .bssd(bssd)
				                   					   .irParamModelBizList(irParamModelBizList)
				                   					   .dt(dt)                                   // Monthly Projection
				                   					   .prjYear(prjYear)
				                   					   .scenNum(scenNum)
				                   					   .seed(seed)				                   					   
				                   					   .build();

		rst = cir.getSimulationResult();
	
		rst.stream().forEach(s -> s.setBaseYymm(bssd));
		rst.stream().forEach(s -> s.setApplBizDv("IBIZ"));		
		rst.stream().forEach(s -> s.setIrModelId(irModelId));
		rst.stream().forEach(s -> s.setIrCurveId(irCurveId));
		rst.stream().forEach(s -> s.setIrCurveSceNo(1));
		rst.stream().forEach(s -> s.setLastModifiedBy(jobId));
		rst.stream().forEach(s -> s.setLastUpdateDate(LocalDateTime.now()));

		log.info("{}({}) creates [{}] results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), toPhysicalName(IrDcntSceStoGnr.class.getSimpleName()));
		
		return rst;		
	}
	
	
	public static List<IrParamModelRnd> createRandCir(String bssd, String irModelId, String irCurveId, Integer prjYear, Integer scenNum, Integer seed) {	

		List<IrParamModelRnd> rst = new ArrayList<IrParamModelRnd>();		
		
		double[][] randNum = new RandomNumberKics(prjYear * 12, scenNum, seed).mersenneTwisterKics();						
//		double[][] randNum = new RandomNumberKics(1440 + 12 * 0, 1000, seed).mersenneTwisterKics();		

		//TODO:
//		if(true) {			
//			String pathDir = "C:/Users/NHfire.DESKTOP-J5J0BJV/Desktop/";
//			String path0 = pathDir + "CIR_RND_"       + irModelId + "_" + irCurveId  + ".csv";
//			
//			try {
//				writeArraytoCSV(matTranspose(randNum), path0);									
//			} catch (Exception e) {
//				e.printStackTrace();
//			}					
//		}
		
		
		for(int i=0; i<randNum.length; i++) {
			for(int j=0; j<randNum[i].length; j++) {								
				IrParamModelRnd rand = new IrParamModelRnd();								
				rand.setBaseYymm(bssd);
				rand.setIrModelId(irModelId);
				rand.setIrCurveId(irCurveId);
				rand.setSceNo(j+1);
				rand.setMatCd(String.format("%s%04d", "M", i+1));
				rand.setRndNum(randNum[i][j]);
				rand.setLastModifiedBy(jobId);
				rand.setLastUpdateDate(LocalDateTime.now());
				
				rst.add(rand);
			}
		}
		log.info("{}({}) creates [{}] results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), toPhysicalName(IrParamModelRnd.class.getSimpleName()));
		
		return rst;		
	}	
	
	
	public static List<IrQvalSce> createQuantileValue(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, TreeMap<Integer, TreeMap<Integer, Double>> cirSceMap) {		
		
		List<IrQvalSce> rst = new ArrayList<IrQvalSce>();		
		
		if(cirSceMap.isEmpty()) {
			log.warn("Quantile Value: No Stochastic Discount Rate Data of [{}] [BIZ: {}, ID: {}, SCE: {}] for [{}]", irModelId, applBizDv, irCurveId, irCurveSceNo, bssd);
			return rst;		
		}

		int[]      monthIdx = cirSceMap.keySet().stream().mapToInt(Integer::intValue).toArray();
		double[][] cirShort = new double[cirSceMap.keySet().size()][cirSceMap.firstEntry().getValue().size()];		
		
		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> stoSce : cirSceMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> sto : stoSce.getValue().entrySet()) {			
				cirShort[mat][sce] = sto.getValue();
				sce++;
			} 
			mat++;
		}				
		
		for(int i=0; i<cirShort.length; i++) {
			
			IrQvalSce cir = new IrQvalSce();
			cir.setBaseYymm(bssd);
			cir.setApplBizDv(applBizDv);
			cir.setIrModelId(irModelId);
			cir.setIrCurveId(irCurveId);
			cir.setIrCurveSceNo(irCurveSceNo);			
			cir.setQvalDv("CIR_QUANTILE");
			
			cir.setQvalSeq(Integer.valueOf(monthIdx[i]));			
			cir.setQval1 (new Percentile().evaluate(cirShort[i],  99.0));
			cir.setQval2 (new Percentile().evaluate(cirShort[i],  95.0));
			cir.setQval3 (new Percentile().evaluate(cirShort[i],  90.0));
			cir.setQval4 (new Percentile().evaluate(cirShort[i],  85.0));
			cir.setQval5 (new Percentile().evaluate(cirShort[i],  80.0));			
			cir.setQval6 (new Percentile().evaluate(cirShort[i],  75.0));
			cir.setQval7 (new Percentile().evaluate(cirShort[i],  60.0));
			cir.setQval8 (new Percentile().evaluate(cirShort[i],  50.0));
			cir.setQval9 (new Percentile().evaluate(cirShort[i],  40.0));
			cir.setQval10(new Percentile().evaluate(cirShort[i],  25.0));			
			cir.setQval11(new Percentile().evaluate(cirShort[i],  20.0));
			cir.setQval12(new Percentile().evaluate(cirShort[i],  15.0));
			cir.setQval13(new Percentile().evaluate(cirShort[i],  10.0));
			cir.setQval14(new Percentile().evaluate(cirShort[i],  5.00));
			cir.setQval15(new Percentile().evaluate(cirShort[i],  1.00));			

			cir.setLastModifiedBy(jobId);
			cir.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(cir);			
		}
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, irCurveId, irCurveSceNo, toPhysicalName(IrQvalSce.class.getSimpleName()));

		return rst;
	}
		
}

