package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gof.dao.IrCurveSpotDao;
import com.gof.dao.IrSprdDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdCurve;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpUsr;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg240_LpSprd extends Process {	
	
	public static final Esg240_LpSprd INSTANCE = new Esg240_LpSprd();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static List<IrSprdLp> setLpFromSwMap(String bssd, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap) {
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			
			List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, curveSwMap.getKey());
			if(tenorList.isEmpty()) {
				log.warn("No IR Curve Data [IR_CURVE_ID: {}] in [{}] for [{}]", curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}
			
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {				
				
				int llp = StringUtil.objectToPrimitive(swSce.getValue().getLlp(), 20);
				for(String tenor : tenorList) {					
//					log.info("tenor: {}, {}, {}", tenor, tenor.substring(1), swSce.getValue().getLlp());					

					if(Integer.valueOf(tenor.substring(1)) <=  llp * MONTH_IN_YEAR) {						
						
						IrSprdLp lp1 = new IrSprdLp();
						
						lp1.setBaseYymm(bssd);
						lp1.setDcntApplModelCd("BU1");
						lp1.setApplBizDv(applBizDv);
						lp1.setIrCurveId(curveSwMap.getKey());
						lp1.setIrCurveSceNo(swSce.getKey());
						lp1.setMatCd(tenor);
						lp1.setLiqPrem(swSce.getValue().getLiqPrem());
						lp1.setLastModifiedBy(jobId);						
						lp1.setLastUpdateDate(LocalDateTime.now());
						
						rst.add(lp1);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from SW Param). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}
	
	
	public static List<IrSprdLp> setLpFromCrdSprd(String bssd, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap, String lpCurveId) {
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {			
			
			List<String> tenorList = IrCurveSpotDao.getIrCurveTenorList(bssd, curveSwMap.getKey());
			if(tenorList.isEmpty()) {
				log.warn("No IR Curve Data [IR_CURVE_ID: {}] in [{}] for [{}]", curveSwMap.getKey(), toPhysicalName(IrCurveSpot.class.getSimpleName()), bssd);
				continue;
			}
			
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {

				int llp = StringUtil.objectToPrimitive(swSce.getValue().getLlp(), 20);				
				for(IrSprdCurve lpCrv : IrCurveSpotDao.getIrSprdCurve(bssd, lpCurveId)) {
					if(Integer.valueOf(lpCrv.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrSprdLp lp2 = new IrSprdLp();
						
						lp2.setBaseYymm(bssd);
						lp2.setDcntApplModelCd("BU2");
						lp2.setApplBizDv(applBizDv);
						lp2.setIrCurveId(curveSwMap.getKey());
						lp2.setIrCurveSceNo(swSce.getKey());
						lp2.setMatCd(lpCrv.getMatCd());
						lp2.setLiqPrem(lpCrv.getCrdSprd());
						lp2.setLastModifiedBy(jobId);						
						lp2.setLastUpdateDate(LocalDateTime.now());
						
						rst.add(lp2);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from Credit Spread). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}
	
	
	public static List<IrSprdLp> setLpFromUsr(String bssd, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap) {
		
		List<IrSprdLp> rst = new ArrayList<IrSprdLp>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {			
			
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {

				int llp = StringUtil.objectToPrimitive(swSce.getValue().getLlp(), 20);				
				List<IrSprdLpUsr> lpUsr = IrSprdDao.getIrSprdLpUsrList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());
				
				for(IrSprdLpUsr usr : lpUsr) {
					if(Integer.valueOf(usr.getMatCd().substring(1)) <= llp * MONTH_IN_YEAR) {
						
						IrSprdLp lp3 = new IrSprdLp();
						
						lp3.setBaseYymm(bssd);
						lp3.setDcntApplModelCd("BU3");
						lp3.setApplBizDv(applBizDv);
						lp3.setIrCurveId(curveSwMap.getKey());
						lp3.setIrCurveSceNo(swSce.getKey());
						lp3.setMatCd(usr.getMatCd());
						lp3.setLiqPrem(usr.getLiqPrem());
						lp3.setLastModifiedBy(jobId);						
						lp3.setLastUpdateDate(LocalDateTime.now());
						
						rst.add(lp3);
					}					
				}
			}
		}
		log.info("{}({}) creates [{}] results of [{}] (from User Defined). They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLp.class.getSimpleName()));
		
		return rst;
	}	

}

