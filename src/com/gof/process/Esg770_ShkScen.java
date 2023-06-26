package com.gof.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gof.enums.EJob;
import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrCurveYtmDao;
import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrDcntRateBuIm;
import com.gof.entity.IrDcntSceIm;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdAfnsCalc;
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

			// 자산 sw 보간 목적 ltfr
			List<IrCurveYtm> ytmList = IrCurveYtmDao.getIrCurveYtm(bssd, curveSwMap.getKey());
			List<IrCurveSpot> spotList = IrCurveSpotDao.getIrCurveSpot(bssd, curveSwMap.getKey());

			

			if (swSce != null) {
				
			// 생성해야 하는 결과는 det, sto 시나리오 둘 다 있음. ( irModel에 따라 달라짐 )
//			int scenCnt = 5 ;
//			int scenCnt = Math.min(IrSprdDao.getIrSprdAfnsCalcScenCnt(bssd, irModelId, curveSwMap.getKey()),10);		
			int scenCnt = IrSprdDao.getIrSprdAfnsCalcScenCnt(bssd, irModelId, curveSwMap.getKey());		
	
			for (int sceNo = 1; sceNo <= scenCnt; sceNo++) {
			
			Map<String, Double> irSprdShkMap = IrSprdDao.getIrSprdAfnsCalcList(bssd, irModelId, curveSwMap.getKey(), sceNo).stream()
					.collect(Collectors.toMap(IrSprdAfnsCalc::getMatCd, IrSprdAfnsCalc::getShkSprdCont));				
	
			List<IrCurveSpot> irDcntRateBuImList = IrDcntRateDao.getIrDcntRateBuImToAdjSpotList(bssd, applBizDv, irModelId, curveSwMap.getKey(), sceNo);
			if(irDcntRateBuImList.size()==0) {
				log.warn("No IR Dcnt Rate Data [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", applBizDv, curveSwMap.getKey(),sceNo , toPhysicalName(IrDcntRateBuIm.class.getSimpleName()), bssd);
				continue;
			}
			
			// sw 보간/보외 check 연속복리이율을 가져왔으므로 CMPD_MTD_CONT
//			SmithWilsonKics sw = new SmithWilsonKics(baseDate, irDcntRateBuImList, CMPD_MTD_CONT, true, swSce.getLtfr(), swSce.getLtfrCp(), projectionYear, 1, 100, DCB_MON_DIF);
//			List<SmithWilsonRslt> swRslt = sw.getSmithWilsonResultList();	
			
			// 자산 할인율, 부채 할인율 모두 보간 
			SmithWilsonKics sw;
			List<SmithWilsonRslt> swRslt;

			if (applBizDv.equals("KICS_A")) {
				String matCd = ytmList.get(ytmList.size() - 1).getMatCd();
				int matMonths = Integer.parseInt(matCd.substring(1));
				
				// 23.06.26 : ytm vs. spot? 
				double   ytm    = ytmList.get(ytmList.size() - 1).getYtm(); 
				int      ltfrTA = matMonths / 12; 
				double   spot   = spotList.get(ytmList.size() - 1).getSpotRate();   // disc.
				
				// TODO : 임시 코드확인용/ spread를 시장에서 관측되는 데이터의 최장만기 기준으로 만들어야 하는데 지금은 없음. 
				String tmpMatCd = curveSwMap.getKey().equals("1010000") ? "M0240" : "M0360";
				
				double spread = irSprdShkMap.get(tmpMatCd); // TODO tmpMatCd -> matCd
			    double ltfrA = irDiscToCont(spot) + spread ; // TODO check !! 이렇게 정의하면 시나리오마다 spread값이 달라지므로 ltfrA값이 시나리오번호마다 달라짐. 
			    
//				if (sceNo == 1) log.info("irCurveId :{}, ltfrA :{}, ltfrTA:{}, ytm :{} , spot :{}, spread:{} ", curveSwMap.getKey(), ltfrA, ltfrTA, ytm, spot, spread);
				log.info("sceNo :{}, irCurveId :{}, ltfrA :{}, ltfrTA:{}, ytm :{} , spot :{}, spread:{} ",sceNo,  curveSwMap.getKey(), ltfrA, ltfrTA, ytm, spot, spread);

				
				sw = new SmithWilsonKics(baseDate, irDcntRateBuImList, CMPD_MTD_CONT, true, ltfrA, ltfrTA, projectionYear, 1, 100, DCB_MON_DIF);
				swRslt = sw.getSmithWilsonResultList();
//	
			} 
			else { //KICS_L
			    sw = new SmithWilsonKics(baseDate, irDcntRateBuImList, CMPD_MTD_CONT, true, swSce.getLtfr(), swSce.getLtfrCp(), projectionYear, 1, 100, DCB_MON_DIF);
			    swRslt = sw.getSmithWilsonResultList();
			}

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
			log.info("applBizDv:{}, irCurveID:{}, scenNo: {}, swAlpha: {}",applBizDv, curveSwMap.getKey(), sceNo, sw.getAlphaApplied());
		  }		
		}
	   }	
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irScenarioList.size(), toPhysicalName(IrDcntSceIm.class.getSimpleName()));
		
		return irScenarioList;
		}	
	
}
