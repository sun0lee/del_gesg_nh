package com.gof.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrDcntRateDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrDcntRate;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamSw;
import com.gof.enums.EJob;
import com.gof.model.SmithWilsonKics;
//import com.gof.model.SmithWilsonKics;
import com.gof.model.SmithWilsonKicsBts;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg270_IrDcntRate extends Process {	
	
	public static final Esg270_IrDcntRate INSTANCE = new Esg270_IrDcntRate();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntRate> createIrDcntRate(String bssd, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap, Integer projectionYear) {	
		
		List<IrDcntRate> rst = new ArrayList<IrDcntRate>();
//		log.info("zzzz : {}", paramSwMap.get("1010000").get(18).toString());
		paramSwMap.entrySet().forEach(s-> log.info("aaaa : {},{}", s.getKey(), s.getValue()));
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			
			Map<String, IrDcntRate> adjRateSce1Map       = new TreeMap<String, IrDcntRate>();
			Map<String, SmithWilsonRslt> baseRateSce1Map = new TreeMap<String, SmithWilsonRslt>();  			//for using SmithWilsonKicsBts not SmithWilsonKics
			List<IrDcntRate> adjRateSce1List             = new ArrayList<IrDcntRate>();			                //hereafter for KICS SCE_NO 7 and 8(totalShift)
			double ltfr1 = 0.0;
			double shift = 0.0;
			
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {				
				
				log.info("BIZ: [{}], IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", applBizDv, curveSwMap.getKey(), swSce.getKey());
				List<IrCurveSpot> irCurveSpotList = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());
				
//				irCurveSpotList.forEach(s-> log.info("aaaaa : {}", s.toString()));
				if(irCurveSpotList.size()==0) {
					log.warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", applBizDv, curveSwMap.getKey(), swSce.getKey(), toPhysicalName(IrDcntRateBu.class.getSimpleName()), bssd);
					continue;
				}				
				
				
				LocalDate baseDate = DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth());
				log.info("Check Curve : {}, {}, {},{}", swSce.getKey(), swSce.getValue().getLtfr(), swSce.getValue().getLtfrCp(), projectionYear);
				log.info("sce Id : {},{}",  curveSwMap,swSce.getKey());
				
				SmithWilsonKics swKics = new SmithWilsonKics(baseDate, irCurveSpotList, CMPD_MTD_DISC, true, swSce.getValue().getLtfr(), swSce.getValue().getLtfrCp(), projectionYear, 1, 100, DCB_MON_DIF);				
				List<IrDcntRate> adjRateList = swKics.getSmithWilsonResultList().stream().map(s -> s.convert()).collect(Collectors.toList());
				
				
				// 20221006 : 만기 긴부분 조정 
//				double adjTempRate =0.01;
//				double spotTemptRate=0.01;
////				log.info("aaa00000000000 : {}", adjRateList.size());
//				for(IrDcntRate dcnt :adjRateList) {
//					log.info("aaa00000000000 : {}", dcnt.toString());
////					if(dcnt.getSpotRate().isNaN() || dcnt.getSpotRate().isInfinite() || dcnt.getAdjSpotRate().isNaN() || dcnt.getAdjSpotRate().isInfinite()) {
//					if(dcnt.getAdjSpotRate().isNaN() || dcnt.getAdjSpotRate().isInfinite() ) {
////						dcnt.setSpotRate(spotTemptRate);
//						dcnt.setAdjSpotRate(adjTempRate);
//					}
//					else {
////						spotTemptRate = dcnt.getSpotRate();
//						adjTempRate = dcnt.getAdjSpotRate();
//					}
//				}
//				//Deprecated after KICS 6.0 // for KICS SCE_NO 7 and 8(totalShift): adjRateSce1List and ltfr1 are initialized at Sce#1. At Sce#7 & #8 adjRateList is adjusted using adjRateSce1List and shift
//				if(applBizDv.equals("KICS") && swSce.getKey().equals(1)) {
//					
//					//TODO: shallow copy vs deep copy
////					adjRateSce1List = adjRateList;
//					adjRateSce1List = adjRateList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());				
//					ltfr1 = StringUtil.objectToPrimitive(swSce.getValue().getLtfr(), 0.0495);					
//				}
////				log.info("adjRateSce1List: size:{}", adjRateSce1List.size());  //only KICS SCEN#1 for SCEN# 7 & 8
//				
//				if(applBizDv.equals("KICS") && (swSce.getKey().equals(7) || swSce.getKey().equals(8))) {
//					
//					TreeMap<String, Double> adjSpotRateMap = new TreeMap<String, Double>();
//					TreeMap<String, Double> adjFwdRateMap  = new TreeMap<String, Double>();
//
//					shift = StringUtil.objectToPrimitive(swSce.getValue().getLtfr(), 0.0495) - ltfr1;					
//					for(IrDcntRate rslt : adjRateSce1List) {						
//						String matCd = rslt.getMatCd();
//						double adjRate = rslt.getAdjSpotRate() + shift;						
//						adjSpotRateMap.put(matCd, adjRate);						
//					}					
//					adjFwdRateMap = irSpotDiscToFwdM1Map(adjSpotRateMap);
//					for(IrDcntRate rslt : adjRateList) {
//						rslt.setAdjSpotRate(adjSpotRateMap.get(rslt.getMatCd()).doubleValue());
//						rslt.setAdjFwdRate(adjFwdRateMap.get(rslt.getMatCd()).doubleValue());
//					}
//				}
				
				
//				if(swSce.getKey().equals(1)) {
//					swKics.getSmithWilsonResultList().stream().filter(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) <= 240).forEach(s -> log.info("SW TS: {}, {}, {}", s.getMatCd(), s.getSpotDisc(), s.getFwdDisc()));					
//				}
				
				/*
				 * Map<String, IrDcntRate> adjRateMap =
				 * adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd,
				 * Function.identity(), (k, v) -> k, TreeMap::new)); TreeSet<Double> tenorList =
				 * adjRateList.stream().map(s -> Double.valueOf(1.0 *
				 * Integer.valueOf(s.getMatCd().substring(1)) /
				 * MONTH_IN_YEAR)).collect(Collectors.toCollection(TreeSet::new)); double[]
				 * prjTenor = tenorList.stream().mapToDouble(Double::doubleValue).toArray();
				 * 
				 * //for Creating Asset Discount Rate // if(applBizDv.equals("KICS") &&
				 * swSce.getKey().equals(1) || !applBizDv.equals("KICS")) {
				 * if(applBizDv.equals("KICS") && swSce.getKey().equals(1)) {
				 * 
				 * adjRateSce1Map =
				 * adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd,
				 * Function.identity(), (k, v) -> k, TreeMap::new));
				 * 
				 * List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd,
				 * curveSwMap.getKey()); if(ytmList.size()==0) {
				 * log.warn("No Historical YTM Data exist for [{}, {}] in [{}]", bssd,
				 * curveSwMap.getKey(), jobId); continue; }
				 * 
				 * SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of() .baseDate(baseDate)
				 * .ytmCurveHisList(ytmList)
				 * .alphaApplied(StringUtil.objectToPrimitive(swSce.getValue().getSwAlphaYtm(),
				 * 0.1)) .freq(StringUtil.objectToPrimitive(swSce.getValue().getFreq(), 2))
				 * .build();
				 * 
				 * // swBts.getSmithWilsonResultList(prjTenor).stream().filter(s ->
				 * Double.parseDouble(s.getMatCd().substring(1, 5)) <= 240).forEach(s ->
				 * log.info("{}, {}, {}", s.getMatCd(), s.getSpotDisc(), s.getFwdDisc()));
				 * baseRateSce1Map =
				 * swBts.getSmithWilsonResultList(prjTenor).stream().collect(Collectors.toMap(
				 * SmithWilsonRslt::getMatCd, Function.identity()));
				 * 
				 * for(IrDcntRate rslt : adjRateList) {
				 * rslt.setSpotRate(baseRateSce1Map.get(rslt.getMatCd()).getSpotDisc());
				 * rslt.setFwdRate (baseRateSce1Map.get(rslt.getMatCd()).getFwdDisc()); } } //
				 * else if(!applBizDv.equals("KICS") && swSce.getKey().equals(1)) { else
				 * if(!applBizDv.equals("KICS")) {
				 * 
				 * adjRateSce1Map =
				 * adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd,
				 * Function.identity(), (k, v) -> k, TreeMap::new));
				 * 
				 * List<IrCurveYtm> ytmList = IrDcntRateDao.getIrDcntRateBuToBaseSpotList(bssd,
				 * applBizDv, curveSwMap.getKey(), swSce.getKey()).stream().map(s ->
				 * s.convertSimpleYtm()).collect(Collectors.toList()); if(ytmList.size()==0) {
				 * log.
				 * warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]"
				 * , applBizDv, curveSwMap.getKey(), swSce.getKey(),
				 * toPhysicalName(IrDcntRateBu.class.getSimpleName()), bssd); continue; }
				 * 
				 * SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of() .baseDate(baseDate)
				 * .ytmCurveHisList(ytmList)
				 * .alphaApplied(StringUtil.objectToPrimitive(swSce.getValue().getSwAlphaYtm(),
				 * 0.1)) .freq(0) .build();
				 * 
				 * // swBts.getSmithWilsonResultList(prjTenor).stream().filter(s ->
				 * Double.parseDouble(s.getMatCd().substring(1, 5)) <= 240).forEach(s ->
				 * log.info("{}, {}, {}", s.getMatCd(), s.getSpotDisc(), s.getFwdDisc()));
				 * baseRateSce1Map =
				 * swBts.getSmithWilsonResultList(prjTenor).stream().collect(Collectors.toMap(
				 * SmithWilsonRslt::getMatCd, Function.identity()));
				 * 
				 * for(IrDcntRate rslt : adjRateList) {
				 * rslt.setSpotRate(baseRateSce1Map.get(rslt.getMatCd()).getSpotDisc());
				 * rslt.setFwdRate (baseRateSce1Map.get(rslt.getMatCd()).getFwdDisc()); } }
				 * //for KICS: Asset Discount Rate Scenario after scen#1 is generated from Above
				 * Insurance Discount Rate + Difference Rate of Insurance - Asset at SCE#1 else
				 * { TreeMap<String, Double> spotRateMap = new TreeMap<String, Double>();
				 * TreeMap<String, Double> fwdRateMap = new TreeMap<String, Double>();
				 * 
				 * for(IrDcntRate rslt : adjRateList) { String matCd = rslt.getMatCd(); double
				 * adjRate = adjRateMap.get(matCd).getAdjSpotRate(); double adjDiff =
				 * baseRateSce1Map.get(matCd).getSpotDisc() -
				 * adjRateSce1Map.get(matCd).getAdjSpotRate();
				 * 
				 * rslt.setSpotRate(adjRate + adjDiff); spotRateMap.put(matCd, adjRate +
				 * adjDiff); } fwdRateMap = irSpotDiscToFwdM1Map(spotRateMap);
				 * 
				 * for(IrDcntRate rslt : adjRateList) {
				 * rslt.setFwdRate(fwdRateMap.get(rslt.getMatCd()).doubleValue()); } }
				 */

				if(applBizDv.equals("KICS") && swSce.getKey().equals(1)) {
					
					//TODO: shallow copy vs deep copy
//					adjRateSce1List = adjRateList;
					adjRateSce1List = adjRateList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());				
					ltfr1 = StringUtil.objectToPrimitive(swSce.getValue().getLtfr(), 0.0495);					
				}
//				log.info("adjRateSce1List: size:{}", adjRateSce1List.size());  //only KICS SCEN#1 for SCEN# 7 & 8 
				
//				if(applBizDv.equals("KICS") && (swSce.getKey().equals(7) || swSce.getKey().equals(8))) {
//					
//					TreeMap<String, Double> adjSpotRateMap = new TreeMap<String, Double>();
//					TreeMap<String, Double> adjFwdRateMap  = new TreeMap<String, Double>();
//
//					shift = StringUtil.objectToPrimitive(swSce.getValue().getLtfr(), 0.0495) - ltfr1;					
//					for(IrDcntRate rslt : adjRateSce1List) {						
//						String matCd = rslt.getMatCd();
//						double adjRate = rslt.getAdjSpotRate() + shift;						
//						adjSpotRateMap.put(matCd, adjRate);						
//					}					
//					adjFwdRateMap = irSpotDiscToFwdM1Map(adjSpotRateMap);
//					for(IrDcntRate rslt : adjRateList) {
//						rslt.setAdjSpotRate(adjSpotRateMap.get(rslt.getMatCd()).doubleValue());
//						rslt.setAdjFwdRate(adjFwdRateMap.get(rslt.getMatCd()).doubleValue());
//					}
//				}
				
				Map<String, IrDcntRate> adjRateMap = adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd, Function.identity(), (k, v) -> k, TreeMap::new));				
				TreeSet<Double> tenorList = adjRateList.stream().map(s -> Double.valueOf(1.0 * Integer.valueOf(s.getMatCd().substring(1)) / MONTH_IN_YEAR)).collect(Collectors.toCollection(TreeSet::new));
				double[] prjTenor = tenorList.stream().mapToDouble(Double::doubleValue).toArray();				
				
				//for Creating Asset Discount Rate
				if(applBizDv.equals("KICS") && swSce.getKey().equals(1) || !applBizDv.equals("KICS")) {
					
					adjRateSce1Map = adjRateList.stream().collect(Collectors.toMap(IrDcntRate::getMatCd, Function.identity(), (k, v) -> k, TreeMap::new));										
					
					List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, curveSwMap.getKey());
					if(ytmList.size()==0) {
						log.warn("No Historical YTM Data exist for [{}, {}] in [{}]", bssd, curveSwMap.getKey(), jobId);
						continue;
					}				
					
					SmithWilsonKicsBts swBts = SmithWilsonKicsBts.of()
																 .baseDate(baseDate)					
																 .ytmCurveHisList(ytmList)
																 .alphaApplied(swSce.getValue().getSwAlphaYtm())													 
																 .freq(swSce.getValue().getFreq())
																 .build();						
					
//					swBts.getSmithWilsonResultList(prjTenor).stream().filter(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) <= 240).forEach(s -> log.info("{}, {}, {}", s.getMatCd(), s.getSpotDisc(), s.getFwdDisc()));
					baseRateSce1Map = swBts.getSmithWilsonResultList(prjTenor).stream().collect(Collectors.toMap(SmithWilsonRslt::getMatCd, Function.identity()));

					for(IrDcntRate rslt : adjRateList) {						
						rslt.setSpotRate(baseRateSce1Map.get(rslt.getMatCd()).getSpotDisc());
						rslt.setFwdRate (baseRateSce1Map.get(rslt.getMatCd()).getFwdDisc());
					}					
				}
				
				//for KICS: Asset Discount Rate Scenario 2~10 is generated from Above Insurance Discount Rate plus Difference Rate of Insurance - Asset at SCE#1
				else {					
					TreeMap<String, Double> spotRateMap = new TreeMap<String, Double>();
					TreeMap<String, Double> fwdRateMap  = new TreeMap<String, Double>();
					
					for(IrDcntRate rslt : adjRateList) {						
						String matCd   = rslt.getMatCd();						
						double adjRate = adjRateMap.get(matCd).getAdjSpotRate();
						double adjDiff = baseRateSce1Map.get(matCd).getSpotDisc() - adjRateSce1Map.get(matCd).getAdjSpotRate();						
						
						rslt.setSpotRate(adjRate + adjDiff);						
						spotRateMap.put(matCd, adjRate + adjDiff);
					}					
					fwdRateMap = irSpotDiscToFwdM1Map(spotRateMap);					

					for(IrDcntRate rslt : adjRateList) {
						rslt.setFwdRate(fwdRateMap.get(rslt.getMatCd()).doubleValue());
					}					 
				}
				
				for(IrDcntRate rslt : adjRateList) {
					rslt.setBaseYymm(bssd);
					rslt.setApplBizDv(applBizDv);
					rslt.setIrCurveId(curveSwMap.getKey());
					rslt.setIrCurveSceNo(swSce.getKey());
					rslt.setLastModifiedBy(jobId);
					rslt.setLastUpdateDate(LocalDateTime.now());
				}				
				
				
				double tempSpot =0.0;
				double tempFwd =0.0;
				double tempAdjSpot =0.0;
				double tempAdjFwd =0.0;
				
				for(IrDcntRate dcnt : adjRateList) {
//					log.info("aaaa : {},{}", dcnt.toString());
//					log.info("aaaa : {},{}", dcnt.toString(), dcnt.getSpotRate(), dcnt.getAdjSpotRate());
//					if(dcnt.getIrCurveSceNo() == 18  &&  dcnt.getMatCd().contentEquals("M0700")) {
//						log.info("aaaa11111 : {},{},{}", dcnt.toString());
//						
//					}
					
					tempSpot 	= dcnt.getSpotRate();
					tempAdjSpot = dcnt.getAdjSpotRate();
					tempFwd 	= dcnt.getFwdRate();
					tempAdjFwd 	= dcnt.getAdjFwdRate();
					
					if(dcnt.getFwdRate() > 0.2) {
//						dcnt.setAdjFwdRate(0.0);
//						dcnt.setFwdRate(0.0);
					}
						
					if(dcnt.getSpotRate().isNaN() || dcnt.getSpotRate().isInfinite() || dcnt.getAdjSpotRate().isNaN() || dcnt.getAdjSpotRate().isInfinite()) {
//						log.info("{}, {}, {}", curveSwMap.getKey(), swSce.getKey(), dcnt);
						dcnt.setSpotRate(tempSpot);
						dcnt.setAdjSpotRate(tempAdjSpot);
						dcnt.setFwdRate(0.0);
						dcnt.setAdjFwdRate(0.0);
						
//						log.error("Smith-Wilson Interpolation is failed. Check Shock Spread Data in [{}] Table for [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}]", Process.toPhysicalName(IrCurveYtm.class.getSimpleName()), applBizDv, curveSwMap.getKey(), swSce.getKey(), bssd);
//						try {
//							throw new Exception();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						return new ArrayList<IrDcntRate>();
					}
				}
				
				rst.addAll(adjRateList);
			}
		}		
		log.info("{}({}) creates [{}] results of [{}] {}. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, paramSwMap.keySet(), toPhysicalName(IrDcntRate.class.getSimpleName()));
		
		return rst;		
	}	

}

