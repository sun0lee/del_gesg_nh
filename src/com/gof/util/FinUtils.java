package com.gof.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveSpot;
import com.gof.enums.ECompound;

public class FinUtils {
	
	private final static Logger logger = LoggerFactory.getLogger("FinUtil");
	
	public static String toEndOfMonth(String baseDate) {
		
		if(baseDate.length()==4) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), 12, 1).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else if(baseDate.length()==6) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), Integer.valueOf(baseDate.substring(4,6)), 1).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else if(baseDate.length()==8) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4))
										, Integer.valueOf(baseDate.substring(4,6))
											, Integer.valueOf(baseDate.substring(6,8))).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else {
			logger.error("Convert Date Error : {} is not date format", baseDate);
		}
		return null;
	}

	public static String addMonth(String baseDate, int monNum) {
		
		if(baseDate.length()==4) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), 1, 1).plusMonths(monNum).format(DateTimeFormatter.ofPattern("yyyy"));
		}
		else if(baseDate.length()==6) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), Integer.valueOf(baseDate.substring(4,6)), 1).plusMonths(monNum).format(DateTimeFormatter.ofPattern("yyyyMM"));
		}
		else if(baseDate.length()==8) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4))
										, Integer.valueOf(baseDate.substring(4,6))
											, Integer.valueOf(baseDate.substring(6,8))).plusMonths(monNum).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else {
			logger.error("Convert Date Error : {} is not date format", baseDate);
		}
		return null;
	}
	
	public static int monthBetween(String bssd, String compareDate) {
		String baseBssd = bssd.substring(0,6) + "01";
		String otherBssd = compareDate.substring(0, 6) +"01";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		return (int)ChronoUnit.MONTHS.between(LocalDate.parse(baseBssd, formatter), LocalDate.parse(otherBssd, formatter));
		
	}	

	// Forwarding �� Base Logic ��.	
	public static double getForwardRate(IrCurveSpot nearCurve, IrCurveSpot farCurve) {
	
		int nearNum  = Integer.valueOf(nearCurve.getMatCd().substring(1)) ;
		int farNum  = Integer.valueOf(farCurve.getMatCd().substring(1)) ;
		
//		String matCd = "M" + String.format("%04d", farNum-nearNum);
		
		double nearIrRate = nearCurve.getSpotRate();
		double farIrRate = farCurve.getSpotRate();
		
		
		LocalDate asOfDate =  FinUtils.convertToDate(nearCurve.getBaseDate());
		
		LocalDate nearDate =  asOfDate.plusMonths(nearNum);
		LocalDate farDate  =  asOfDate.plusMonths(farNum);
		
		double fwdDf = ECompound.Monthly.getDf(farIrRate, asOfDate, farDate)  / ECompound.Monthly.getDf(nearIrRate, asOfDate, nearDate);    
		
//		logger.info("FwdDf  :  {},{},{}", ECompound.Monthly.getDf(farIrRate, asOfDate, farDate) , ECompound.Monthly.getDf(nearIrRate, asOfDate, nearDate)
//					,ECompound.Monthly.getDf(farIrRate, asOfDate, farDate) / ECompound.Monthly.getDf(nearIrRate, asOfDate, nearDate));
		
		return  ECompound.Monthly.getIntRateFromDf(nearDate, farDate, fwdDf);
	}
	
	
	
	public static double getForwardRate(Map<String, IrCurveSpot> curveMap, String matCd, int forwardMonNum) {
//		double rst =0.0;
		
//		int nearMatCd  = Integer.valueOf(matCd.substring(1))  ;
//		int farMatCd  = Integer.valueOf(matCd.substring(1)) + forwardMonNum ;
		
//		int matNum  = Integer.valueOf(matCd.substring(1)) ;
		int farNum  = Integer.valueOf(matCd.substring(1)) + forwardMonNum ;
		
		String nearMatCd = "M" + String.format("%04d", forwardMonNum);
		String farMatCd =  "M" + String.format("%04d", Integer.valueOf(matCd.substring(1)) + forwardMonNum);
		
		logger.info("finUitl : {},{}, {}", nearMatCd, farMatCd);
		
		double farIrRate = curveMap.get(farMatCd).getSpotRate();
		double nearIrRate = curveMap.get(nearMatCd).getSpotRate();
		LocalDate asOfDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		LocalDate nearDate =  asOfDate.plusMonths(forwardMonNum);
		LocalDate farDate =  asOfDate.plusMonths(farNum);
		
		double fwdDf = ECompound.Monthly.getDf(farIrRate, asOfDate, farDate)  / ECompound.Monthly.getDf(nearIrRate, asOfDate, nearDate);    
			
		return fwdDf;
	}
	
	
	
	public static double getForwardRateForPV(Map<String, IrCurveSpot> curveMap, String matCd, int forwardMonNum) {
		Map<String, IrCurveSpot> fullCurveMap = getLinearInterpolationCurve(curveMap);
		
		String farMatCd   =  "M" + String.format("%04d", Math.min(1200, Integer.valueOf(matCd.substring(1)) + forwardMonNum)); 
		
		return getForwardRate(fullCurveMap.get(matCd), fullCurveMap.get(farMatCd));
	}
	
	public static Map<String, Double> getForwardRateForPV(Map<String, IrCurveSpot> curveMap) {
		Map<String, Double> fwdMap = new HashMap<>();
		
		int farMonNum ;
		int nearMonNum;
		double fwdRate =0.0;
		double farDf =0.0;
		double nearDf = 0.0;
		
		for(Map.Entry<String, IrCurveSpot> entry: curveMap.entrySet()) {
			farMonNum =  Integer.valueOf(entry.getKey().substring(1));
			nearMonNum = farMonNum -1;
			
			if(entry.getKey().equals("M0001")) {
				fwdRate = entry.getValue().getSpotRate();
			}
			else {
				String nearMatCd   =  "M" + String.format("%04d", nearMonNum);
				farDf = Math.pow((1+ entry.getValue().getSpotRate() / 12), -1 * farMonNum );
				nearDf = Math.pow((1+ curveMap.get(nearMatCd).getSpotRate() / 12), -1 * nearMonNum );
				
				fwdRate = 12 *( Math.pow(farDf/nearDf , -1) -1) ;
			}
			
			fwdMap.put(entry.getKey(), fwdRate);
		}
		return fwdMap;
	}
	
	public static double getForwardRateForTS(Map<String, IrCurveSpot> curveMap, String matCd, int forwardMonNum) {
		Map<String, IrCurveSpot> fullCurveMap = getLinearInterpolationCurve(curveMap);
		
		String nearMatCd  =  "M" + String.format("%04d", forwardMonNum );
		String farMatCd   =  "M" + String.format("%04d", Integer.valueOf(matCd.substring(1)) + forwardMonNum ); 
		
		return getForwardRate(fullCurveMap.get(nearMatCd), fullCurveMap.get(farMatCd));
	}
	
	
	
	/*public static List<SmithWilsonResult> getForwardRateByMaturity(Map<String, IrCurveHis> curveMap, String matCd) {
		List<SmithWilsonResult> rstList = new ArrayList<SmithWilsonResult>();
		SmithWilsonResult temp;
		double intRate =0.0;
		
		int matNum  = Integer.valueOf(matCd.substring(1)) ;
		
		int farNum  ; 
		
		for(int i =0; i<1200; i++) {
			farNum = matNum + i;
			String farMatCd  =  "M" + String.format("%04d", farNum);
			String nearMatCd =  "M" + String.format("%04d", i);
			
			double nearIntFactor = Math.pow(1+ curveMap.get(nearMatCd).getIntRate(), i/12);
			double farIntFactor  = Math.pow(1+ curveMap.get(farMatCd).getIntRate(), farNum/12);
			
			double intFactor = farIntFactor / nearIntFactor;
			
			intRate= Math.pow(intFactor, 12/matNum) - 1;
			
			temp = new SmithWilsonResult();
//			temp.setBaseDate(baseDate);
			temp.setSpotAnnual(intRate);
			temp.setFwdMonthNum(i);
			
			rstList.add(temp);
		}
			
		return rstList;
	}*/
	
	
	/*public static List<BottomupDcnt> getForwardRateByMaturity1(String bssd, List<BottomupDcnt> curveList, String matCd) {
		Map<String, BottomupDcnt> curveMap = curveList.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
		return getForwardRateByMaturity(bssd, curveMap, matCd);
	}*/
	
	public static List<IrCurveSpot> getForwardRateByMaturity(String bssd, List<IrCurveSpot> curveList, String matCd) {
		Map<String, IrCurveSpot> curveMap = curveList.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
		return getForwardRateByMaturity(bssd, curveMap, matCd);
	} 
	
	
	
	public static Map<String, Double> getForwardRateByMaturityZZ(String bssd, Map<String, Double> curveMap, String matCd) {
		Map<String, Double> rstMap = new HashMap<String, Double>();
		
		double intRate =0.0;
		double nearIntFactor =0.0;
		double farIntFactor  =0.0;
		double intFactor  =0.0;
		int matNum  = Integer.valueOf(matCd.substring(1)) ;
		int farNum  ; 
		
		for(int i =1; i<=1200; i++) {
			farNum = matNum + i;
			String nearMatCd =  "M" + String.format("%04d", i);
			String farMatCd  =  "M" + String.format("%04d", farNum);
			
			if(!curveMap.containsKey(nearMatCd)) {
				return rstMap;
			}
			else {
				nearIntFactor = Math.pow(1+ curveMap.get(nearMatCd), i/12.0);
				
				if(curveMap.containsKey(farMatCd)) {
					farIntFactor  = Math.pow(1+ curveMap.get(farMatCd), farNum/12.0);
					
					intFactor = nearIntFactor==0.0? farIntFactor: farIntFactor / nearIntFactor;
					
					intRate= Math.pow(intFactor, 12.0/matNum) - 1.0 ;
				}
				else {
//					intRate = curveMap.get(nearMatCd);
				}
				
				rstMap.put(FinUtils.addMonth(bssd, i), intRate );
			}
		}
			
		return rstMap;
	}
	public static Map<String, Double> getForwardRateByMaturityMatCd(String bssd, Map<String, Double> curveMap, String matCd) {
		Map<String, Double> rstMap = new HashMap<String, Double>();
		
		double intRate =0.0;
		double nearIntFactor =0.0;
		double farIntFactor  =0.0;
		double intFactor  =0.0;
		int matNum  = Integer.valueOf(matCd.substring(1)) ;
		int farNum  ; 
		
		for(int i =1; i<=1200; i++) {
			farNum = matNum + i;
			String nearMatCd =  "M" + String.format("%04d", i);
			String farMatCd  =  "M" + String.format("%04d", farNum);
			
			if(!curveMap.containsKey(nearMatCd)) {
				return rstMap;
			}
			else {
				nearIntFactor = Math.pow(1+ curveMap.getOrDefault(nearMatCd,0.0), i/12.0);
				
				if(curveMap.containsKey(farMatCd)) {
					farIntFactor  = Math.pow(1+ curveMap.get(farMatCd), farNum/12.0);
					
					intFactor = nearIntFactor==0.0? farIntFactor: farIntFactor / nearIntFactor;
					
					intRate= Math.pow(intFactor, 12.0/matNum) - 1.0 ;
				}
				else {
//					intRate = curveMap.get(nearMatCd);
				}
				
				rstMap.put(nearMatCd, intRate );
			}
		}
			
		return rstMap;
	}
	

	public static List<IrCurveSpot> getForwardRateByMaturity(String bssd, Map<String, IrCurveSpot> curveMap, String matCd) {
		List<IrCurveSpot> rstList = new ArrayList<IrCurveSpot>();
		IrCurveSpot temp;
		double intRate =0.0;
		double nearIntFactor =0.0;
		double farIntFactor  =0.0;
		double intFactor  =0.0;
		int matNum  = Integer.valueOf(matCd.substring(1)) ;
		int farNum  ; 
		
		for(int i =1; i<=1200; i++) {
			farNum = matNum + i;
			String nearMatCd =  "M" + String.format("%04d", i);
			String farMatCd  =  "M" + String.format("%04d", farNum);
			
			if(!curveMap.containsKey(nearMatCd)) {
				return rstList;
			}
			else {
				nearIntFactor = Math.pow(1+ curveMap.get(nearMatCd).getSpotRate(), i/12.0);
				
				if(curveMap.containsKey(farMatCd)) {
					farIntFactor  = Math.pow(1+ curveMap.get(farMatCd).getSpotRate(), farNum/12.0);
					
					intFactor = nearIntFactor==0.0? farIntFactor: farIntFactor / nearIntFactor;
					
					intRate= Math.pow(intFactor, 12.0/matNum) - 1.0 ;
				}
				else {
					intRate = curveMap.get(nearMatCd).getSpotRate();
				}
//			logger.info("aaaa : {},{},{},{},{}", curveMap.get(nearMatCd).getIntRate(), curveMap.get(farMatCd).getIntRate(), intFactor, intRate);
				
				temp = new IrCurveSpot();
				
				temp.setBaseDate(bssd);
				temp.setMatCd(matCd);
				temp.setSpotRate(intRate);
				
				rstList.add(temp);
			}
		}
			
		return rstList;
	}
	
	
	public static Map<String, IrCurveSpot> getLinearInterpolationCurve(Map<String, IrCurveSpot> curveMap) {
//		double rst =0.0;
		
		Map<String, IrCurveSpot> rstMap = new HashMap<String, IrCurveSpot>();
		IrCurveSpot baseIrCurvHis = new IrCurveSpot();
		
		if(curveMap.values().stream().findFirst().isPresent()) {
			baseIrCurvHis = curveMap.values().stream().findFirst().get();
//			logger.info("Interpol : {},{},{}", baseIrCurvHis.getIrCurveId(), baseIrCurvHis.getMatCd(), baseIrCurvHis.getBaseDate());
		}
//		logger.info("Interpol : {},{},{}", baseIrCurvHis.getIrCurveId(), baseIrCurvHis.getMatCd(), baseIrCurvHis.getBaseDate());
		
		IrCurveSpot temp;
		
		temp = new IrCurveSpot();
		temp.setBaseDate(baseIrCurvHis.getBaseDate());
		temp.setIrCurveId(baseIrCurvHis.getIrCurveId());
		temp.setIrCurve(baseIrCurvHis.getIrCurve());
		temp.setMatCd("M1200");
		temp.setSpotRate(0.045);
		
		curveMap.put("M1200", temp);
		
		temp = new IrCurveSpot();
		temp.setBaseDate(baseIrCurvHis.getBaseDate());
		temp.setIrCurveId(baseIrCurvHis.getIrCurveId());
		temp.setIrCurve(baseIrCurvHis.getIrCurve());
		temp.setMatCd("M1201");
		temp.setSpotRate(0.045);
		curveMap.put("M1201", temp);
		
		IrCurveSpot leftIrCurve ;;
		IrCurveSpot rightIrCurve= new IrCurveSpot();
		
		int index =1;
		int leftIndex = 1;
		int rightIndex = 1;
		double tempIntRate =0.0;
		String tempMatCd ;
		

		for(int i=1 ; i<= 1201; i++) {
			
			if( curveMap.containsKey("M" + String.format("%04d", i))) {
				leftIrCurve  = rightIrCurve;
				rightIrCurve = curveMap.get("M" + String.format("%04d", i));
				
				if(leftIrCurve.getMatCd()==null) {
					continue;
				}	
				else {
//					logger.info("Interpol : {},{}", leftIrCurve.getIrCurveId(), leftIrCurve.getMatCd());
					leftIndex = Integer.valueOf(leftIrCurve.getMatCd().substring(1));
				}
				
				rightIndex = Integer.valueOf(rightIrCurve.getMatCd().substring(1));
				 
				for(int j = Math.min(index,leftIndex); j < rightIndex; j++) {
					tempIntRate = leftIrCurve.getSpotRate() * (rightIndex - j) / (rightIndex - leftIndex)
							+ rightIrCurve.getSpotRate() * ( j - leftIndex ) /(rightIndex - leftIndex) ;
					
					tempMatCd = "M" + String.format("%04d", j);

					temp = new IrCurveSpot();
					temp.setBaseDate(baseIrCurvHis.getBaseDate());
					temp.setIrCurveId(baseIrCurvHis.getIrCurveId());
					temp.setIrCurve(baseIrCurvHis.getIrCurve());
					temp.setMatCd(tempMatCd);
					temp.setSpotRate(tempIntRate);
					
					rstMap.put(tempMatCd, temp);
				}
				index = rightIndex;
			}
		}

		return rstMap;
	}

	
	private static LocalDate convertToDate(String baseDate) {

		if(baseDate.length()==4) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), 1, 1);
		}
		else if(baseDate.length()==6) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), Integer.valueOf(baseDate.substring(4,6)), 1);
		}
		else if(baseDate.length()==8) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), Integer.valueOf(baseDate.substring(4,6)), Integer.valueOf(baseDate.substring(6,8)));
		}
		else {
			logger.error("Convert Date Error : {} is not date format", baseDate);
		}
		return null;
	}
	
	
	
//	public static List<IrCurveHis> spanFullBucketSmith(String bssd, List<IrCurveHis> curveRst){
//		List<IrCurveHis> rstList = new ArrayList<IrCurveHis>();
//		if(curveRst.isEmpty()) {
//			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", curveRst ,bssd);
//			return curveRst;
//		}
//				
//		IrCurve curveMst = IrCurveHisDao.getIrCurve(curveRst.get(0).getIrCurveId());
//		String curCd = "KRW";
//		if(curveMst!=null) {
//			curCd = curveMst.getCurCd();
//		}
//		
//		double ufr  = EsgConstant.getSmParam().get(curCd).getUfr();
//		double ufrt = EsgConstant.getSmParam().get(curCd).getUfrT();
//		
//		SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
//		SEXP rfRst      = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix: 
//		
//		for(int i =0; i< 1200; i++) {
//			IrCurveHis temp = new IrCurveHis();
//			
//			temp.setBaseDate(bssd);
//			temp.setIrCurveId(curveMst.getIrCurveId());
//			
//			temp.setMatCd("M" + String.format("%04d", i+1));
//			temp.setIntRate(rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
//			
////			temp.setForwardNum(curveMst.getForwardNum());
//			temp.setSceNo("0");
//			
//			rstList.add(temp);
//		}
//		return rstList;
//	}
	
	
//	public static List<IrCurveHis> spanFullBucket(String bssd, List<IrCurveHis> curveRst){
//		List<IrCurveHis> rstList = new ArrayList<IrCurveHis>();
//		if(curveRst.isEmpty()) {
//			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", curveRst ,bssd);
//			return curveRst;
//		}
//		
//		IrCurveHis firstIrCurveHis = curveRst.get(0);
////		List<IrCurve> curveMstList = DaoUtil.getEntities(IrCurve.class, new HashMap<String, Object>());
//		
//		List<IrCurve> curveMstList = IrCurveHisDao.getIrCurveByCrdGrdCd("RF");
////		logger.info("size : {}", curveMstList.size());
//		String curCd ="";
//		for(IrCurve aa : curveMstList) {
//			if( aa.getIrCurveId().equals(firstIrCurveHis.getIrCurveId())) {
//				curCd = aa.getCurCd(); 
//				
//			}
//		}
//		
////		List<SmithWilsonParam> swParam = SmithWilsonDao.getParamList();
////		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
////		
////		double ufr  = swParamMap.containsKey(curCd) ? 0.045: swParamMap.get(curCd).getUfr();
////		double ufrt = swParamMap.containsKey(curCd) ? 60   : swParamMap.get(curCd).getUfrT();
//		
//		double ufr  = EsgConstant.getSmParam().get("KRW").getUfr();
//		double ufrt = EsgConstant.getSmParam().get("KRW").getUfrT();
//
////		List<SmithWilsonParamHis> swParamList = SmithWilsonDao.getParamHisList(bssd).stream()
////				.filter(s->s.getCurCd().equals(curCd))
////				.collect(Collectors.toList());
////		
////		double ufr  = swParamList.isEmpty()? EsgConstant.getNumConstant().get("UFR") : swParamList.get(0).getUfr();
////		double ufrt = swParamList.isEmpty()? EsgConstant.getNumConstant().get("UFRT"): swParamList.get(0).getUfrT();
//		
//		SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
//		SEXP rfRst      = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix: 
//		
//		for(int i =0; i< 1200; i++) {
//			IrCurveHis temp = new IrCurveHis();
//			
//			temp.setBaseDate(bssd);
//			temp.setIrCurveId(firstIrCurveHis.getIrCurveId());
//			
//			temp.setMatCd("M" + String.format("%04d", i+1));
//			temp.setIntRate(rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
//			
//			temp.setForwardNum(firstIrCurveHis.getForwardNum());
//			temp.setSceNo("0");
//			
//			rstList.add(temp);
//			
//		}
//		return rstList;
//	}

	
	
}
