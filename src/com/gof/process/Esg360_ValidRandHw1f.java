package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwRnd;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrValidRnd;
import com.gof.model.Hw1fSimulationKics;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg360_ValidRandHw1f extends Process {
	
	public static final Esg360_ValidRandHw1f INSTANCE = new Esg360_ValidRandHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static List<IrParamHwRnd> createValidInputHw1f(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, Map<String, Map<Integer, IrParamSw>> paramSwMap, Map<String, IrParamModel> modelMstMap, Integer projectionYear, Double targetDuration) {

		List<IrParamHwRnd> randRst = new ArrayList<IrParamHwRnd>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
//				
				if(!StringUtil.objectToPrimitive(swSce.getValue().getStoSceGenYn(), "N").toUpperCase().equals("Y")) continue;
				
				if(!curveSwMap.getKey().equals(irCurveId) || !swSce.getKey().equals(irCurveSceNo)) continue;				
//				log.info("IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", curveSwMap.getKey(), swSce.getKey());
				
				if(!modelMstMap.containsKey(curveSwMap.getKey())) {
					log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamModel.class.getSimpleName()));
					continue;
				}
				
				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());				
//				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToBaseSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());
				if(adjSpotRate.isEmpty()) {
					log.warn("No Spot Rate Data [ID: {}, SCE_NO: {}] for [{}] in [{}] Table", curveSwMap.getKey(), swSce.getKey(), bssd, Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()));
					continue;
				}				
									
				List<IrParamHwBiz> paramHw = IrParamHwDao.getIrParamHwBizList(bssd, applBizDv, irModelId, curveSwMap.getKey());					
				if(paramHw.isEmpty()) {
					log.warn("No HW1F Model Parameter exist in [MODEL: {}] [IR_CURVE_ID: {}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()));
					continue;
				}
				List<Hw1fCalibParas> hwParasList = Hw1fCalibParas.convertFrom(paramHw);
//				log.info("{}, {}", hwParasList);
				
				int[] alphaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("ALPHA") && s.getMatCd().equals("M0240"))
										  	       .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();
				int[] sigmaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("SIGMA") && !s.getMatCd().equals("M1200") && !s.getMatCd().equals("M0240"))
												   .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();	
	//			log.info("{}, {}", alphaPiece, sigmaPiece);				
				
				boolean priceAdj      = false;
				int     randomGenType = 1;
				int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getTotalSceNo()), SCEN_NUM);						
				int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getRndSeed())   , RANDOM_SEED);
				double  ltfr          = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfr()    , 0.0495);
				int     ltfrCp        = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfrCp()  , 60);
				log.info("seedNum: {}, {}", seedNum, bssd);		

				Hw1fSimulationKics hw1f = new Hw1fSimulationKics(bssd, adjSpotRate, hwParasList, alphaPiece, sigmaPiece, priceAdj, sceNum, ltfr, ltfrCp, projectionYear, randomGenType, seedNum);
//				List<IrModelSce>   hwResult    = hw1f.getIrModelHw1fList();
				List<IrParamHwRnd> randNumList = hw1f.getRandomScenList();				

				randRst.addAll(randNumList);				
			}
		}		
		return randRst;		
	}		
	
	
	public static List<IrValidRnd> testRandNormality(String bssd, String irModelId, String irCurveId, TreeMap<Integer, TreeMap<Integer, Double>> randNumMap, Double sigLevel) {		
		
		List<IrValidRnd> rst = new ArrayList<IrValidRnd>();		
		
		if(randNumMap.isEmpty()) {
			log.warn("No Random Number Data for [{}] Stochastic Simulation [ID: {}] for [{}]", irModelId, irCurveId, bssd);
			return rst;		
		}		 
		
		double[][] randNum = new double[randNumMap.keySet().size()][randNumMap.firstEntry().getValue().size()];

		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> rand : randNumMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> rnd : rand.getValue().entrySet()) {
				randNum[mat][sce] = rnd.getValue();
				sce++;
			}
			mat++;
		}		

		//TODO: Jarque-Bera Test
		
		double[]   jbTest  = new double[randNum.length];
		double[]   pValue  = new double[randNum.length];
		double[]   qValue  = new double[randNum.length];
		double[]   pValue2 = new double[randNum.length];
		double[]   qValue2 = new double[randNum.length];
		
//		double pValue1 = 1.0 - new ChiSquaredDistribution(2).cumulativeProbability(jbTestValue);      //CHISQ.DIST.RT(val, 2)        in EXCEL
//		double pValue2 = new ChiSquaredDistribution(2).cumulativeProbability(jbTestValue);            //CHISQ.DIST   (val, 2, TRUE ) in EXCEL
//		double pValue3 = new ChiSquaredDistribution(2).density(jbTestValue);                          //CHISQ.DIST   (val, 2, FALSE) in EXCEL		
//		log.info("pValue1: {}, pValue2: {}, pValue3: {}", pValue1, pValue2, pValue3);		
//
//		double qValue1 = new ChiSquaredDistribution(2).inverseCumulativeProbability(1.0 - pValue1);   //CHISQ.INV.RT (val, 2)        in EXCEL
//		double qValue2 = new ChiSquaredDistribution(2).inverseCumulativeProbability(      pValue2);   //CHISQ.INV    (val, 2)        in EXCEL
//		double qValue3 = 0.0;		
//		log.info("qValue1: {}, qValue2: {}, qValue3: {}", qValue1, qValue2, qValue3);
		
		double     pCheck     = sigLevel;
		double     qCheck     = new ChiSquaredDistribution(2).inverseCumulativeProbability(1.0 - pCheck);	
		
		for(int i=0; i<randNum.length; i++) {
			jbTest[i] = vecJBtest(randNum[i]);			

			pValue[i] = 1.0 - new ChiSquaredDistribution(2).cumulativeProbability(jbTest[i]);         //CHISQ.DIST.RT(val, 2)        in EXCEL
			qValue[i] = new ChiSquaredDistribution(2).inverseCumulativeProbability(1.0 - pValue[i]);  //CHISQ.INV.RT (val, 2)        in EXCEL
			
			pValue2[i] = new ChiSquaredDistribution(2).cumulativeProbability(jbTest[i]);              //CHISQ.DIST   (val, 2, TRUE ) in EXCEL			
			qValue2[i] = new ChiSquaredDistribution(2).inverseCumulativeProbability(pValue2[i]);      //CHISQ.INV    (val, 2)        in EXCEL			
			
//			if(i<2) log.info("Idx: {}, JB TestValue: {}, pValue: {}, pValue2: {}, pcheck:{}, qValue: {}, qCheck: {}", i+1, jbTest[i], pValue[i], pValue2[i], pCheck, qValue[i], qCheck);
			
			IrValidRnd rnd = new IrValidRnd();
			rnd.setBaseYymm(bssd);
			rnd.setIrModelId(irModelId);
			rnd.setIrCurveId(irCurveId);
			rnd.setValidDv("JB_TEST");
			
			rnd.setValidSeq(Integer.valueOf(i+1));
			rnd.setValidVal1(pValue[i]);
			rnd.setValidVal2(pCheck);
			rnd.setValidVal3(jbTest[i]);
			rnd.setValidVal4(qCheck);			
			rnd.setValidVal5((jbTest[i] < qCheck) ? 1.0 : 0.0);
			
			rnd.setLastModifiedBy(jobId);
			rnd.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(rnd);			
		}
		
		
		//TODO: Kolmogorov-Smirnov Test
		double    alpha    = sigLevel;
		double[]  distance = new double[randNum.length];
		double[]  kspValue = new double[randNum.length];
		boolean[] ksResult = new boolean[randNum.length];
		double    distanceCheck = Math.sqrt(-0.5 * Math.log(0.5 * alpha)) * Math.sqrt(1.0 / randNum[0].length);
		
		for(int i=0; i<randNum.length; i++) {
			KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
			NormalDistribution    pnorm  = new NormalDistribution();
			
			distance[i] = ksTest.kolmogorovSmirnovStatistic(pnorm, randNum[i]);
			kspValue[i] = ksTest.kolmogorovSmirnovTest(pnorm, randNum[i]);
			ksResult[i] = ksTest.kolmogorovSmirnovTest(pnorm, randNum[i], alpha);
						
//			if(i<10) log.info("Idx: {}, pValue: {}, KS distance: {}, boolean:{}", i+1, kspValue[i], distance[i], ksResult[i]);
			
			IrValidRnd rnd = new IrValidRnd();
			rnd.setBaseYymm(bssd);
			rnd.setIrModelId(irModelId);
			rnd.setIrCurveId(irCurveId);
			rnd.setValidDv("KS_TEST");
			
			rnd.setValidSeq(Integer.valueOf(i+1));
			rnd.setValidVal1(kspValue[i]);
			rnd.setValidVal2(alpha);
			rnd.setValidVal3(distance[i]);
			rnd.setValidVal4(distanceCheck);
			rnd.setValidVal5((distance[i] < distanceCheck ? 1.0 : 0.0));
//			rnd.setValidVal5((kspValue[i] > alpha ? 1.0 : 0.0));			
			
			rnd.setLastModifiedBy(jobId);
			rnd.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(rnd);			
		}
		
		return rst;
	}
	
	//TODO: Runs Test
	public static List<IrValidRnd> testRandIndependency(String bssd, String irModelId, String irCurveId, TreeMap<Integer, TreeMap<Integer, Double>> randNumMap, Double sigLevel) {		
		
		List<IrValidRnd> rst = new ArrayList<IrValidRnd>();		
		
		if(randNumMap.isEmpty()) {
			log.warn("No Random Number Data for [{}] Stochastic Simulation [ID: {}] for [{}]", irModelId, irCurveId, bssd);
			return rst;		
		}		 
		
		double[][] randNum  = new double[randNumMap.keySet().size()][randNumMap.firstEntry().getValue().size()];		

		int mat = 0;
		for(Entry<Integer, TreeMap<Integer, Double>> rand : randNumMap.entrySet()) {
			int sce = 0;
			for(Map.Entry<Integer, Double> rnd : rand.getValue().entrySet()) {
				randNum[mat][sce] = rnd.getValue();
				sce++;
			}
			mat++;
		}		
		
		double[][] randNumT = matTranspose(randNum);		
		for(int j=0; j<randNumT.length; j++) {			
			
			int    N        = randNumT[j].length;
			double mu       = (2.0 * N - 1.0) / 3.0;
			double sd       = Math.sqrt((16.0 * N - 29.0) / 90.0);
			double runsCnt  = runsCnt(randNumT[j]);
			double zStar    = (runsCnt - mu) / sd;

			double pValue   = new NormalDistribution().cumulativeProbability(zStar);
			double pValue2  = 1.0 - new NormalDistribution().cumulativeProbability(Math.abs(zStar));			
			
//			if(j<10) log.info("Idx: {}, runsCnt: {}, mu: {}, sd: {}, zStar: {}, pValue: {}, pValueTwoSide: {}, sigLevel: {}", j+1, runsCnt, mu, sd, zStar, pValue, pValue2, sigLevel / 2);			
//			if(pValue2 < sigLevel / 2.0) log.info("Idx: {}, runsCnt: {}, mu: {}, sd: {}, zStar: {}, pValue: {}, pValueTwoSide: {}, sigLevel: {}", j+1, runsCnt, mu, sd, zStar, pValue, pValue2, sigLevel / 2);			
			
			IrValidRnd rnd = new IrValidRnd();
			rnd.setBaseYymm(bssd);
			rnd.setIrModelId(irModelId);
			rnd.setIrCurveId(irCurveId);
			rnd.setValidDv("RUNS_TEST");
			
			rnd.setValidSeq(Integer.valueOf(j+1));
			rnd.setValidVal1(pValue);
			rnd.setValidVal2(pValue >= 0.5 ? (1-sigLevel/2.0) : (sigLevel/2.0));
//			rnd.setValidVal1(pValue2);
//			rnd.setValidVal2(sigLevel);			
			rnd.setValidVal3(zStar);
			rnd.setValidVal4(new NormalDistribution().inverseCumulativeProbability(1-sigLevel/2.0) * (zStar > 0 ? 1.0 : -1.0));
			rnd.setValidVal5((pValue2 > sigLevel / 2.0) ? 1.0 : 0.0);    //same as below line
//			rnd.setValidVal5(( (pValue> sigLevel / 2.0) && (pValue < 1.0 - sigLevel / 2.0) ) ? 1.0 : 0.0);			

			rnd.setLastModifiedBy(jobId);
			rnd.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(rnd);			
		}		
		return rst;
	}

}