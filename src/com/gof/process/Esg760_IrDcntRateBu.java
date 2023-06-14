package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrDcntRateBuIm;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg760_IrDcntRateBu extends Process {	
	
	public static final Esg760_IrDcntRateBu INSTANCE = new Esg760_IrDcntRateBu();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntRateBuIm> setIrDcntRateBu(String bssd, String irModelId, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap) {	
		
		List<IrDcntRateBuIm> rst = new ArrayList<IrDcntRateBuIm>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			
			
			List<IrCurveSpot> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, curveSwMap.getKey());
			
			TreeMap<String, Double> spotMap = spotList.stream().collect(Collectors.toMap(IrCurveSpot::getMatCd, IrCurveSpot::getSpotRate, (k, v) -> k, TreeMap::new));
			
			if(spotList.isEmpty()) {
				log.warn("No IR Curve Spot Data [BIZ: {}, IR_CURVE_ID: {}] in [{}] for [{}]", applBizDv, curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}

			int detScen = 1 ; //swSce.getKey()
			Map<Integer, IrParamSw> swMap = curveSwMap.getValue();
			IrParamSw swSce = swMap.get(detScen);

			if (swSce != null) {
						// 23.06.13 조건수정. 내부모형의 목적으로, 추가적으로 paramSw을 추가/수정/관리 하지 않고 처리할 수 있도록 기존 설정을 사용함.
						// swSce 에서 KICS 의 1번 시나리오 설정 데이터를 사용함. 
						// 시나리오 갯수는 det / sto를 구분해서 각각 정의함. 
					
				// 유동성 프리미엄은 KICS의 결정론 시나리오 생성시 사용하는 수준을 사용함. 
					Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, curveSwMap.getKey(), detScen).stream()
							                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));
	
					// 생성해야 하는 결과는 det, sto 시나리오 둘 다 있음. ( irModel에 따라 달라짐 )
					int scenCnt = IrSprdDao.getIrSprdAfnsCalcScenCnt(bssd, irModelId, curveSwMap.getKey());			
	
	
					for (int sceNo = 1; sceNo <= scenCnt; sceNo++) {
					Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsCalcList(bssd, irModelId, curveSwMap.getKey(), sceNo).stream()
																.collect(Collectors.toMap(IrSprdAfnsCalc::getMatCd, IrSprdAfnsCalc::getShkSprdCont));				
					
	//				log.info("sceNo :{}, irSprdShkMap:{}" ,sceNo, irSprdShkMap );
					
					List<IrCurveSpot> spotSceList = spotList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());
					
					String fwdMatCd = StringUtil.objectToPrimitive(swSce.getFwdMatCd(), "M0000");				
					if(!fwdMatCd.equals("M0000")) {					
						Map<String, Double> fwdSpotMap = irSpotDiscToFwdMap(bssd, spotMap, fwdMatCd);					
						spotSceList.stream().forEach(s -> s.setSpotRate(fwdSpotMap.get(s.getMatCd())));					
					}				
	
					String pvtMatCd = StringUtil.objectToPrimitive(swSce.getPvtRateMatCd() , "M0000");
					double pvtRate  = StringUtil.objectToPrimitive(spotMap.getOrDefault(pvtMatCd, 0.0), 0.0    );				
	//				double pvtMult  = StringUtil.objectToPrimitive(swSce.getValue().getMultPvtRate()  , 0.0    );				
					double intMult  = StringUtil.objectToPrimitive(swSce.getMultIntRate()  , 1.0    );				
					double addSprd  = StringUtil.objectToPrimitive(swSce.getAddSprd()      , 0.0    );
					int    llp      = StringUtil.objectToPrimitive(swSce.getLlp()          , 20     );				
					
	//				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, curveSwMap.getKey(), detScen, pvtMatCd, pvtRate, pvtMult, intMult, addSprd, llp);
					for(IrCurveSpot spot : spotSceList) {				
						if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
							
							IrDcntRateBuIm dcntRateBuIm = new IrDcntRateBuIm();						
							
							double baseSpot = intMult * (StringUtil.objectToPrimitive(spot.getSpotRate()) -  pvtRate) + addSprd + pvtRate;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
							double baseSpotCont = irDiscToCont(baseSpot);					
							
							double shkCont      = irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0);
							double lpDisc       = irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0);
							
//							double spotCont     = baseSpotCont + shkCont;
//							double spotDisc     = irContToDisc(spotCont);						
//							double adjSpotDisc  = spotDisc + lpDisc;
//							double adjSpotCont  = irDiscToCont(adjSpotDisc);	
							
							// 조정 반영 전 후 비교를 위해 기존 코드에서 수정함. 
							double spotCont     = baseSpotCont ; // 조정 반영 전 
							double spotDisc     = irContToDisc(spotCont);	// 조정 반영 전 					
							double adjSpotDisc  = spotDisc + lpDisc; // 유동성 프리미엄 반영 
							double adjSpotCont  = irDiscToCont(adjSpotDisc) + shkCont; 	// 유동성 프리미엄 + shock 반영 					
							
							dcntRateBuIm.setBaseYymm(bssd);
							dcntRateBuIm.setApplBizDv(applBizDv);
							dcntRateBuIm.setIrModelId(irModelId);
							dcntRateBuIm.setIrCurveId(curveSwMap.getKey());
							dcntRateBuIm.setIrCurveSceNo(sceNo);
							dcntRateBuIm.setMatCd(spot.getMatCd());						
							dcntRateBuIm.setSpotRateDisc(spotDisc);
							dcntRateBuIm.setSpotRateCont(spotCont);
							dcntRateBuIm.setLiqPrem(lpDisc);
							dcntRateBuIm.setAdjSpotRateDisc(adjSpotDisc);
							dcntRateBuIm.setAdjSpotRateCont(adjSpotCont);
							dcntRateBuIm.setAddSprd(shkCont); // addSprd
							dcntRateBuIm.setLastModifiedBy(jobId);						
							dcntRateBuIm.setLastUpdateDate(LocalDateTime.now());						
							
							rst.add(dcntRateBuIm);
						  }
						}					
				}
		  }
		}		
		log.info("{}({}) creates [{}] results of [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrDcntRateBu.class.getSimpleName()));
		
		return rst;		
	}		

}

