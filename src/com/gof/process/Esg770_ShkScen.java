package com.gof.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.gof.enums.EJob;
import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBuIm;
import com.gof.entity.IrDcntSceIm;
import com.gof.entity.IrParamSw;
import com.gof.model.SmithWilsonKics;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg770_ShkScen extends Process {	
	
	public static final Esg770_ShkScen INSTANCE = new Esg770_ShkScen();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	

	public static List<IrDcntSceIm> createAfnsShockScenario(String bssd
			                                              , String irModelId
			                                              , String applBizDv
			                                              , Map<String, Map<Integer, IrParamSw>> paramSwMap
			                                              , Integer projectionYear)	
{		
		
		List<IrDcntSceIm> irScenarioList  = new ArrayList<IrDcntSceIm>();				
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
								
			int detScen = 1 ; //swSce.getKey()
			Map<Integer, IrParamSw> swMap = curveSwMap.getValue();
			IrParamSw swSce = swMap.get(detScen);
			LocalDate baseDate = DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth());

			if (swSce != null) {
				
			// 생성해야 하는 결과는 det, sto 시나리오 둘 다 있음. ( irModel에 따라 달라짐 )
//			int scenCnt = 5 ;
//			int scenCnt = Math.min(IrSprdDao.getIrSprdAfnsCalcScenCnt(bssd, irModelId, curveSwMap.getKey()),10);		
			int scenCnt = IrSprdDao.getIrSprdAfnsCalcScenCnt(bssd, irModelId, curveSwMap.getKey());		
	
			for (int sceNo = 1; sceNo <= scenCnt; sceNo++) {
			
			List<IrCurveSpot> irDcntRateBuImList = IrDcntRateDao.getIrDcntRateBuImToAdjSpotList(bssd, applBizDv, irModelId, curveSwMap.getKey(), sceNo);
			if(irDcntRateBuImList.size()==0) {
				log.warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", applBizDv, curveSwMap.getKey(),sceNo , toPhysicalName(IrDcntRateBuIm.class.getSimpleName()), bssd);
				continue;
			}
			
			// sw 보간/보외 check 연속복리이율을 가져왔으므로 CMPD_MTD_CONT
			SmithWilsonKics sw = new SmithWilsonKics(baseDate, irDcntRateBuImList, CMPD_MTD_CONT, true, swSce.getLtfr(), swSce.getLtfrCp(), projectionYear, 1, 100, DCB_MON_DIF);
			List<SmithWilsonRslt> swRslt = sw.getSmithWilsonResultList();			

			// 결과 쓰기 
			for(SmithWilsonRslt rslt : swRslt) {				
				
				IrDcntSceIm ir  = new IrDcntSceIm();
				
				ir.setBaseYymm(bssd);
			    ir.setApplBizDv(applBizDv);
				ir.setIrModelId(irModelId);
				ir.setIrCurveId(curveSwMap.getKey());
				ir.setMatCd(rslt.getMatCd());
				ir.setIrCurveSceNo(sceNo);
				ir.setSpotRate(rslt.getSpotDisc());
				ir.setFwdRate(0.0);
				ir.setLastModifiedBy("GESG_" + jobId);
				ir.setLastUpdateDate(LocalDateTime.now());				
				irScenarioList.add(ir);
			}						
			log.info("scenNo: {}, swAlpha: {}", sceNo, sw.getAlphaApplied());
		  }		
		}
	   }	
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irScenarioList.size(), toPhysicalName(IrDcntSceIm.class.getSimpleName()));
		
		return irScenarioList;
		}	
	
}
