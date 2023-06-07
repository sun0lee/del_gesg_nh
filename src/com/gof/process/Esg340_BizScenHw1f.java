package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntSceStoBiz;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwRnd;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrValidSceSto;
import com.gof.enums.EJob;
import com.gof.model.Hw1fSimulationKics;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg340_BizScenHw1f extends Process {
	
	public static final Esg340_BizScenHw1f INSTANCE = new Esg340_BizScenHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);

	public static Map<String, List<?>> createScenHw1f(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, Map<String, Map<Integer, IrParamSw>> paramSwMap, Map<String, IrParamModel> modelMstMap, Integer projectionYear) {
		
		Map<String, List<?>>  rst     = new TreeMap<String, List<?>>();
		List<IrDcntSceStoBiz> sceRst  = new ArrayList<IrDcntSceStoBiz>();
		List<IrParamHwRnd>    randRst = new ArrayList<IrParamHwRnd>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
//				
				if(!StringUtil.objectToPrimitive(swSce.getValue().getStoSceGenYn(), "N").toUpperCase().equals("Y")) continue;				
//				if(!applBizDv.equals("KICS") || !swSce.getKey().equals(1)) continue;
				
				if(!curveSwMap.getKey().equals(irCurveId) || !swSce.getKey().equals(irCurveSceNo)) continue;				
//				log.info("IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", curveSwMap.getKey(), swSce.getKey());
				
				if(!modelMstMap.containsKey(curveSwMap.getKey())) {
					log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamModel.class.getSimpleName()));
					continue;
				}
				
				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());				
//				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToBaseSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());  //Stochastic Scenarios of Base Spot are not necessary
//				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());     //Do Not USE this Huge Array(SW reslt itself). Its Accuracy also have problems.				
//				adjSpotRate.stream().forEach(s -> log.info("{}", s));
				
				if(adjSpotRate.isEmpty()) {
					log.warn("No Spot Rate Data [ID: {}, SCE_NO: {}] for [{}] in [{}] Table", curveSwMap.getKey(), swSce.getKey(), bssd, Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()));
					continue;
				}				
									
				List<IrParamHwBiz> paramHw = IrParamHwDao.getIrParamHwBizList(bssd, applBizDv, irModelId, curveSwMap.getKey());					
				if(paramHw.isEmpty()) {
					log.warn("No HW1F Model Parameter exist in [MODEL: {}] [IR_CURVE_ID: {}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()));
					continue;
				}
				
				int[] alphaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("ALPHA") && s.getMatCd().equals("M0240"))
										  	       .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();				
				
				int[] sigmaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("SIGMA") && !s.getMatCd().equals("M1200") && !s.getMatCd().equals("M0240"))
												   .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();	
				log.info("{}, {}", alphaPiece, sigmaPiece);				

				List<Hw1fCalibParas> hwParasList = Hw1fCalibParas.convertFrom(paramHw);
//				hwParasList.stream().forEach(s -> log.info("hwParasList: {}", s));
				
				boolean priceAdj      = false;
				int     randomGenType = 1;
				int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getTotalSceNo()), SCEN_NUM);						
				int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getRndSeed())   , RANDOM_SEED);
				double  ltfr          = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfr()    , 0.0495);
				int     ltfrCp        = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfrCp()  , 60);
				log.info("seedNum: {}, {}", seedNum, bssd);
				
				Hw1fSimulationKics hw1f = new Hw1fSimulationKics(bssd, adjSpotRate, hwParasList, alphaPiece, sigmaPiece, priceAdj, sceNum, ltfr, ltfrCp, projectionYear, randomGenType, seedNum);
				List<IrDcntSceStoBiz> stoBizList  = hw1f.getIrModelHw1fList().stream().map(s -> s.convert(applBizDv, irModelId, curveSwMap.getKey(), swSce.getKey(), jobId)).collect(Collectors.toList());
				List<IrParamHwRnd>    randNumList = new ArrayList<IrParamHwRnd>();				
				
				//TODO:
				if(applBizDv.equals("1KICS") && curveSwMap.getKey().equals("1010000") && swSce.getKey().equals(1)) {
					
//					String pathDir = "C:/Users/NHfire.DESKTOP-J5J0BJV/Desktop/";
					String pathDir = "C:/Users/gof/Desktop/";
					String path0 = pathDir + "SW_FWD_"        + curveSwMap.getKey() + "_" + swSce.getKey() + ".csv";
					String path1 = pathDir + "HW_FWD_DISC_"   + curveSwMap.getKey() + "_" + swSce.getKey() + ".csv";
					String path2 = pathDir + "HW_RANDOM_"     + curveSwMap.getKey() + "_" + swSce.getKey() + ".csv";
					String path3 = pathDir + "HW_YIELD_DISC_" + curveSwMap.getKey() + "_" + swSce.getKey() + ".csv";
					
					try {
						double[][] sw = new double[hw1f.getFwdDiscBase().length][3];
						for(int i=0; i<sw.length; i++) {
							sw[i][0] = i+1;
							sw[i][1] = hw1f.getSpotDiscBase()[i];
							sw[i][2] = hw1f.getFwdDiscBase()[i];
						}			
						writeArraytoCSV(sw, path0);  //matTranspose(sw)
						writeArraytoCSV(hw1f.getFwdDiscScen(), path1);
//						writeArraytoCSV(matTranspose(hw1f.getFwdDiscScen()), path1);
						if(swSce.getKey().equals(1)) writeArraytoCSV(hw1f.getRandNum(), path2);
						
						hw1f.getIrModelHw1fBondYield(hw1f.getIrModelHw1fList(), 3.0);
						writeArraytoCSV(hw1f.getBondYieldDisc(), path3);
											
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
				
				if(swSce.getKey().equals(1)) {
					randNumList = hw1f.getRandomScenList().stream().map(s -> s.setKeys(irModelId, curveSwMap.getKey(), jobId)).collect(Collectors.toList());	
				}				
				sceRst.addAll(stoBizList);
				randRst.addAll(randNumList);
			}
		}
		rst.put("SCE", sceRst);
		rst.put("RND", randRst);
		
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.get("SCE").size(), applBizDv, irCurveId, irCurveSceNo, toPhysicalName(IrDcntSceStoBiz.class.getSimpleName()));
		if(applBizDv.equals("KICS") && rst.get("RND").size() > 0) {
			log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.get("RND").size(), applBizDv, irCurveId, irCurveSceNo, toPhysicalName(IrParamHwRnd.class.getSimpleName()));	
		}
		
		return rst;		
	}		
	
	
	public static List<IrValidSceSto> createQuantileValue(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, TreeMap<Integer, TreeMap<Integer, Double>> stoSceMap) {		
		
		List<IrValidSceSto> rst = new ArrayList<IrValidSceSto>();		
		
		if(stoSceMap.isEmpty()) {
			log.warn("Quantile Value: No Stochastic Discount Rate Data of [{}] [BIZ: {}, ID: {}, SCE: {}] for [{}]", irModelId, applBizDv, irCurveId, irCurveSceNo, bssd);
			return rst;		
		}

		int[]      monthIdx        = stoSceMap.keySet().stream().mapToInt(Integer::intValue).toArray();
		double     dt              = 1.0 / MONTH_IN_YEAR;

		double[][] stoDcntRate     = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];		
		double[][] stoPrice        = new double[stoSceMap.keySet().size()][stoSceMap.firstEntry().getValue().size() - 1];
		double[]   stoDcntRateMean = new double[stoSceMap.keySet().size()];  //Simple Average DiscountRate Group by Month		

		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> stoSce : stoSceMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> sto : stoSce.getValue().entrySet()) {				
				if(sce > 0) {
					stoDcntRate[mat][sce-1] = sto.getValue();
					stoPrice   [mat][sce-1] = (mat > 0) ? stoPrice[mat-1][sce-1] / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt) : 1.0 / Math.pow(1.0 + stoDcntRate[mat][sce-1], dt);
				}				
				sce++;
			} 
			mat++;
		}		
		stoDcntRateMean = matToVecMean(stoDcntRate);		
		
		//TODO: for specific maturity, finding sce_no for corresponding quantile of fwd dcntRate 
//		Map<Integer, Double> stoQuantile = stoSceMap.lastEntry().getValue();
//		Map<Integer, Double> stoQuantile = stoSceMap.get(20);
//		stoQuantile.remove(0);		//remove detDcntRate		
//		double[] quantile = stoQuantile.values().stream().mapToDouble(Double::doubleValue).toArray();
				
		Map<Integer, Double> stoQuantile = new TreeMap<Integer, Double>();
		double[] quantile = stoPrice[stoPrice.length-1];		
//		double[] quantile = stoPrice[1199];
		for(int i=0; i<quantile.length; i++) stoQuantile.put(i+1, quantile[i]);
		
		double p000 = new Percentile().evaluate(quantile,  0.01);
		double p025 = new Percentile().evaluate(quantile,  25.0);
		double p050 = new Percentile().evaluate(quantile,  50.0);
		double p075 = new Percentile().evaluate(quantile,  75.0);
		double p100 = new Percentile().evaluate(quantile, 100.0);
		
		double v000 = nearValue(quantile, p000);
		double v025 = nearValue(quantile, p025);
		double v050 = nearValue(quantile, p050);
		double v075 = nearValue(quantile, p075);
		double v100 = nearValue(quantile, p100);

		int    q000 = 1;
		int    q025 = 1;
		int    q050 = 1;
		int    q075 = 1;
		int    q100 = 1;

		for(Map.Entry<Integer, Double> sto : stoQuantile.entrySet()) {
			if(Math.abs(sto.getValue() - v000) < ZERO_DOUBLE / 1.00) q000 = sto.getKey();
			if(Math.abs(sto.getValue() - v025) < ZERO_DOUBLE / 1.00) q025 = sto.getKey();
			if(Math.abs(sto.getValue() - v050) < ZERO_DOUBLE / 1.00) q050 = sto.getKey();
			if(Math.abs(sto.getValue() - v075) < ZERO_DOUBLE / 1.00) q075 = sto.getKey();
			if(Math.abs(sto.getValue() - v100) < ZERO_DOUBLE / 1.00) q100 = sto.getKey();			
		}		

//		log.info("quantile: {}, {}, {}, {}, {}", p000, p025, p050, p075, p100);
//		log.info("quantile: {}, {}, {}, {}, {}", v000, v025, v050, v075, v100);
		log.info("[{}, {}, {}, {}], [QUANTILE SCE_NO: 0%: {}, 25%: {}, 50%: {}, 75%: {}, 100%: {}]", bssd, applBizDv, irCurveId, irCurveSceNo, q000, q025, q050, q075, q100);		
		

		for(int i=0; i<stoDcntRate.length; i++) {
			
			IrValidSceSto fwd = new IrValidSceSto();
			fwd.setBaseYymm(bssd);
			fwd.setApplBizDv(applBizDv);
			fwd.setIrModelId(irModelId);
			fwd.setIrCurveId(irCurveId);
			fwd.setIrCurveSceNo(irCurveSceNo);			
			fwd.setValidDv("FWD_QUANTILE");
			
			fwd.setValidSeq(Integer.valueOf(monthIdx[i]));

			// stoDcntRate Idx
//			fwd.setValidVal1(stoDcntRate[i][q000-1]);
//			fwd.setValidVal2(stoDcntRate[i][q025-1]);
//			fwd.setValidVal3(stoDcntRate[i][q050-1]);
//			fwd.setValidVal4(stoDcntRate[i][q075-1]);
//			fwd.setValidVal5(stoDcntRate[i][q100-1]);			
			
			// ZcbPrice Idx asc -> DcntRate Idx desc 
			fwd.setValidVal1(stoDcntRate[i][q100-1]);
			fwd.setValidVal2(stoDcntRate[i][q075-1]);
//			fwd.setValidVal3(stoDcntRate[i][q050-1]);
			fwd.setValidVal3(stoDcntRateMean[i]);
			fwd.setValidVal4(stoDcntRate[i][q025-1]);
			fwd.setValidVal5(stoDcntRate[i][q000-1]);			
			
//			fwd.setValidVal1(stoPrice[i][q000-1]);
//			fwd.setValidVal2(stoPrice[i][q025-1]);
//			fwd.setValidVal3(stoPrice[i][q050-1]);
//			fwd.setValidVal4(stoPrice[i][q075-1]);
//			fwd.setValidVal5(stoPrice[i][q100-1]);						

			fwd.setLastModifiedBy(jobId);
			fwd.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(fwd);			
		}
		
		
		for(int i=0; i<stoDcntRate.length; i++) {
			
			IrValidSceSto fwd = new IrValidSceSto();
			fwd.setBaseYymm(bssd);
			fwd.setApplBizDv(applBizDv);
			fwd.setIrModelId(irModelId);
			fwd.setIrCurveId(irCurveId);
			fwd.setIrCurveSceNo(irCurveSceNo);			
			fwd.setValidDv("FWD_QUANTILE2");
			
			fwd.setValidSeq(Integer.valueOf(monthIdx[i]));			
			fwd.setValidVal1(new Percentile().evaluate(stoDcntRate[i],  0.01));
			fwd.setValidVal2(new Percentile().evaluate(stoDcntRate[i],  25.0));
			fwd.setValidVal3(new Percentile().evaluate(stoDcntRate[i],  50.0));
			fwd.setValidVal4(new Percentile().evaluate(stoDcntRate[i],  75.0));
			fwd.setValidVal5(new Percentile().evaluate(stoDcntRate[i], 100.0));			

			fwd.setLastModifiedBy(jobId);
			fwd.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(fwd);			
		}
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, irCurveId, irCurveSceNo, toPhysicalName(IrValidSceSto.class.getSimpleName()));

		return rst;
	}

}