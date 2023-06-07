package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsBiz;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg261_IrDcntRateBu_Ytm extends Process {	
	
	public static final Esg261_IrDcntRateBu_Ytm INSTANCE = new Esg261_IrDcntRateBu_Ytm();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrDcntRateBu> setIrDcntRateBu(String bssd, String irModelId, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap) {	
		
		List<IrDcntRateBu> rst = new ArrayList<IrDcntRateBu>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {		
			
			List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, curveSwMap.getKey());
//			ytmList.forEach(s-> log.info("ytm : {},{}", s.toString()));

			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				List<IrCurveYtm> ytmAddList = ytmList.stream().map(s->s.addSpread(swSce.getValue().getYtmSpread())).collect(Collectors.toList());
				
//				ytmAddList.forEach(s-> log.info("ytm1 : {},{}", s.toString()));
				
				List<IrCurveSpot> spotList = Esg150_YtmToSpotSw.createIrCurveSpot(bssd, curveSwMap.getKey(), ytmAddList, swSce.getValue().getSwAlphaYtm(), swSce.getValue().getFreq())
													.stream()
//													.map(s-> s.convertToCont())
													.collect(Collectors.toList());
				
				spotList.forEach(s-> log.info("zzzz : {},{}", swSce.getKey(), s.toString()));
				TreeMap<String, Double> spotMap = spotList.stream().collect(Collectors.toMap(IrCurveSpot::getMatCd, IrCurveSpot::getSpotRate, (k, v) -> k, TreeMap::new));
				
				if(spotList.isEmpty()) {
					log.warn("No IR Curve Spot Data [BIZ: {}, IR_CURVE_ID: {}] in [{}] for [{}]", applBizDv, curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
					continue;
				}
				
				Map<String, Double> irSprdLpMap = IrSprdDao.getIrSprdLpBizList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey()).stream()
						                                   .collect(Collectors.toMap(IrSprdLpBiz::getMatCd, IrSprdLpBiz::getLiqPrem));

				Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsBizList(bssd, irModelId, curveSwMap.getKey(), StringUtil.objectToPrimitive(swSce.getValue().getShkSprdSceNo(), 1)).stream()
															.collect(Collectors.toMap(IrSprdAfnsBiz::getMatCd, IrSprdAfnsBiz::getShkSprdCont));				
				
				List<IrCurveSpot> spotSceList = spotList.stream().map(s -> s.deepCopy(s)).collect(Collectors.toList());
				
				String fwdMatCd = StringUtil.objectToPrimitive(swSce.getValue().getFwdMatCd(), "M0000");				
				if(!fwdMatCd.equals("M0000")) {					
					Map<String, Double> fwdSpotMap = irSpotDiscToFwdMap(bssd, spotMap, fwdMatCd);					
					spotSceList.stream().forEach(s -> s.setSpotRate(fwdSpotMap.get(s.getMatCd())));					
				}				

				String pvtMatCd = StringUtil.objectToPrimitive(swSce.getValue().getPvtRateMatCd() , "M0000");
				double pvtRate  = StringUtil.objectToPrimitive(spotMap.getOrDefault(pvtMatCd, 0.0), 0.0    );				
//				double pvtMult  = StringUtil.objectToPrimitive(swSce.getValue().getMultPvtRate()  , 1.0    );  // YTM SPREAD 로 칼럼 의미 변경함==> ytmAdd 로 기처리..
				double intMult  = StringUtil.objectToPrimitive(swSce.getValue().getMultIntRate()  , 1.0    );				
				double addSprd  = StringUtil.objectToPrimitive(swSce.getValue().getAddSprd()      , 0.0    );
				int    llp      = StringUtil.objectToPrimitive(swSce.getValue().getLlp()          , 20     );				
				
				log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", applBizDv, curveSwMap.getKey(), swSce.getKey(), pvtMatCd, pvtRate, intMult, addSprd, llp);
				for(IrCurveSpot spot : spotSceList) {				
					if(Integer.valueOf(spot.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrDcntRateBu dcntRateBu = new IrDcntRateBu();						
						
						
						double baseSpot = intMult * (StringUtil.objectToPrimitive(spot.getSpotRate()) - pvtRate) +  pvtRate + addSprd  ;  //pvtRate doesn't have an effect on parallel shift(only addSprd)						
//						double baseSpotCont = baseSpot;	
						double baseSpotCont = irDiscToCont(baseSpot);
						
						double shkCont      = applBizDv.equals("KICS") ? irSprdShkMap.getOrDefault(spot.getMatCd(), 0.0) : 0.0; 	
						double lpDisc       = irSprdLpMap.getOrDefault(spot.getMatCd(), 0.0);
						
						double spotCont     = baseSpotCont + shkCont;
						double spotDisc     = irContToDisc(spotCont);						
						double adjSpotDisc  = spotDisc + lpDisc;
						double adjSpotCont  = irDiscToCont(adjSpotDisc);						
						
						dcntRateBu.setBaseYymm(bssd);
						dcntRateBu.setApplBizDv(applBizDv);
						dcntRateBu.setIrCurveId(curveSwMap.getKey());
						dcntRateBu.setIrCurveSceNo(swSce.getKey());
						dcntRateBu.setMatCd(spot.getMatCd());						
						dcntRateBu.setSpotRateDisc(spotDisc);
						dcntRateBu.setSpotRateCont(spotCont);
						dcntRateBu.setLiqPrem(lpDisc);
						dcntRateBu.setAdjSpotRateDisc(adjSpotDisc);
						dcntRateBu.setAdjSpotRateCont(adjSpotCont);
						dcntRateBu.setAddSprd(addSprd);
						dcntRateBu.setLastModifiedBy(jobId);						
						dcntRateBu.setLastUpdateDate(LocalDateTime.now());						
						
						rst.add(dcntRateBu);
					}					
				}
			}
		}		
		log.info("{}({}) creates [{}] results of [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrDcntRateBu.class.getSimpleName()));
		
		return rst;		
	}		

}

