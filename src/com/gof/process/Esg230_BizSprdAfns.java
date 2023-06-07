package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gof.dao.IrSprdDao;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.entity.IrSprdAfnsUsr;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;
import com.gof.entity.IrSprdAfnsBiz;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg230_BizSprdAfns extends Process {	
	
	public static final Esg230_BizSprdAfns INSTANCE = new Esg230_BizSprdAfns();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static List<IrSprdAfnsBiz> createBizAfnsShockScenario(String bssd, String irModelId, String irCurveId) {
				
		List<IrSprdAfnsBiz>  irShockBiz  = new ArrayList<IrSprdAfnsBiz>();
		List<IrSprdAfnsCalc> irShockCalc = IrSprdDao.getIrSprdAfnsCalcList(bssd, irModelId, irCurveId);		
		List<IrSprdAfnsUsr>  irShockUsr  = IrSprdDao.getIrSprdAfnsUsrList(bssd, irModelId, irCurveId);
	
		if(!irShockUsr.isEmpty()) {
			
			for(IrSprdAfnsUsr usr : irShockUsr) {				
				for(int i=0; i<6; i++) {
					IrSprdAfnsBiz biz = new IrSprdAfnsBiz();			
					
					biz.setBaseYymm(usr.getBaseYymm());
					biz.setIrModelId(usr.getIrModelId());
					biz.setIrCurveId(usr.getIrCurveId());
					biz.setIrCurveSceNo(i+1);
					biz.setMatCd(usr.getMatCd());
					
					if(i==0) { 
						biz.setShkSprdCont(0.0);
					}
					else if(i==1) {
						biz.setShkSprdCont(StringUtil.objectToPrimitive(usr.getMeanSprd(), 0.0));
					}
					else if(i==2) {
						biz.setShkSprdCont(StringUtil.objectToPrimitive(usr.getUpSprd(), 0.0));
					}
					else if(i==3) {
						biz.setShkSprdCont(StringUtil.objectToPrimitive(usr.getDownSprd(), 0.0));
					}
					else if(i==4) {
						biz.setShkSprdCont(StringUtil.objectToPrimitive(usr.getFlatSprd(), 0.0));
					}
					else {
						biz.setShkSprdCont(StringUtil.objectToPrimitive(usr.getSteepSprd(), 0.0));
					}
					
					biz.setLastModifiedBy(jobId);
					biz.setLastUpdateDate(LocalDateTime.now());
										
					irShockBiz.add(biz);
				}				
			}
			log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockBiz.size(), toPhysicalName(IrSprdAfnsUsr.class.getSimpleName()), toPhysicalName(IrSprdAfnsBiz.class.getSimpleName()));
		}
		else {
			if(!irShockCalc.isEmpty()) {				
				irShockBiz = irShockCalc.stream().map(s -> s.convert()).collect(Collectors.toList());
				log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockBiz.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()), toPhysicalName(IrSprdAfnsBiz.class.getSimpleName()));
			}
			else {
				log.warn("{}({}) No Shock Spread from Model in [Model:{}, ID:{}]", jobId, EJob.valueOf(jobId).getJobName(), irModelId, irCurveId);				
			}			
		}
		return irShockBiz;
	}	
	
}
