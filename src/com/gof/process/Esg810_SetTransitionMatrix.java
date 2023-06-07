package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gof.dao.RcCorpPdDao;
import com.gof.entity.RcCorpTm;
import com.gof.entity.RcCorpTmUsr;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg810_SetTransitionMatrix extends Process {	
	
	public static final Esg810_SetTransitionMatrix INSTANCE = new Esg810_SetTransitionMatrix();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);

	public static List<RcCorpTm> createCorpTmFromUsr(String bssd, String crdEvalAgncyCd) {
				
		List<RcCorpTm>    corpTm    = new ArrayList<RcCorpTm>();
		List<RcCorpTmUsr> corpTmUsr = RcCorpPdDao.getRcCorpTmUsr(bssd, crdEvalAgncyCd);
		
		double toReal = 0.01;
		int    digit  = 7;  
//		corpTmUsr.stream().forEach(s -> log.info("{}", s));		
			
		for(RcCorpTmUsr tmUsr : corpTmUsr) {
			
			for(int i=0; i<7; i++) {
				RcCorpTm tm = new RcCorpTm();			
				
				tm.setBaseYymm(bssd);				
				tm.setCrdEvalAgncyCd(crdEvalAgncyCd);
				tm.setFromCrdGrdCd(tmUsr.getFromCrdGrdCd());
				
				if(i==0) { 
					tm.setToCrdGrdCd("AAA");					
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb1(), 0.0) * toReal, digit));
				}
				else if(i==1) {
					tm.setToCrdGrdCd("AA");
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb2(), 0.0) * toReal, digit));
				}
				else if(i==2) {
					tm.setToCrdGrdCd("A");
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb3(), 0.0) * toReal, digit));
				}
				else if(i==3) {
					tm.setToCrdGrdCd("BBB");					
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb4(), 0.0) * toReal, digit));
				}
				else if(i==4) {
					tm.setToCrdGrdCd("BB");
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb5(), 0.0) * toReal, digit));
				}
				else if(i==5) {
					tm.setToCrdGrdCd("B");
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb6(), 0.0) * toReal, digit));
				}				
				else {
					tm.setToCrdGrdCd("D");
					tm.setTransProb(round(StringUtil.objectToPrimitive(tmUsr.getTransProb7(), 0.0) * toReal, digit));
				}
				
				tm.setLastModifiedBy(jobId);
				tm.setLastUpdateDate(LocalDateTime.now());
									
				corpTm.add(tm);
			}				
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), corpTm.size(), toPhysicalName(RcCorpTmUsr.class.getSimpleName()), toPhysicalName(RcCorpTm.class.getSimpleName()));
		
		return corpTm;
	}	
	
}
