package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gof.dao.IrSprdDao;
import com.gof.entity.IrParamSw;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpBiz;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg250_BizLpSprd extends Process {	
	
	public static final Esg250_BizLpSprd INSTANCE = new Esg250_BizLpSprd();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static List<IrSprdLpBiz> setLpSprdBiz(String bssd, String applBizDv, Map<String, Map<Integer, IrParamSw>> paramSwMap) {
		
		List<IrSprdLpBiz> rst = new ArrayList<IrSprdLpBiz>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {		

			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {				
				
				String dcntApplModelCd = "BU" + StringUtil.objectToPrimitive(swSce.getValue().getLiqPremApplDv(), "1");
				
				List<IrSprdLp> sprdLpList = IrSprdDao.getIrSprdLpList(bssd, dcntApplModelCd, applBizDv, curveSwMap.getKey(), swSce.getKey());
				if(sprdLpList.isEmpty()) {
					log.warn("No IR Spread Data [IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] in [{}] for [{}]", curveSwMap.getKey(), swSce.getKey(), toPhysicalName(IrSprdLp.class.getSimpleName()), bssd);
					continue;
				}

				for(IrSprdLp sprdLp : sprdLpList) {
					IrSprdLpBiz sprdLpBiz = new IrSprdLpBiz();
					
					sprdLpBiz.setBaseYymm(bssd);						
					sprdLpBiz.setApplBizDv(applBizDv);
					sprdLpBiz.setIrCurveId(curveSwMap.getKey());
					sprdLpBiz.setIrCurveSceNo(swSce.getKey());
					sprdLpBiz.setMatCd(sprdLp.getMatCd());
					sprdLpBiz.setLiqPrem(sprdLp.getLiqPrem());
					sprdLpBiz.setLastModifiedBy(jobId);						
					sprdLpBiz.setLastUpdateDate(LocalDateTime.now());
					
					rst.add(sprdLpBiz);
				}
			}
		}		
		log.info("{}({}) creates [{}] results of {}. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, toPhysicalName(IrSprdLpBiz.class.getSimpleName()));
		
		return rst;		
	}	

}

