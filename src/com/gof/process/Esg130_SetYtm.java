package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gof.dao.IrCurveYtmDao;
import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrCurveYtmUsr;
import com.gof.entity.IrCurveYtmUsrHis;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg130_SetYtm extends Process {	
	
	public static final Esg130_SetYtm INSTANCE = new Esg130_SetYtm();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static List<IrCurveYtm> createYtmFromUsrHis(String bssd, String irCurveId) {
				
		List<IrCurveYtm>       ytmList    = new ArrayList<IrCurveYtm>();		
		List<String>           ytmTen     = Arrays.asList("M0003", "M0006", "M0009", "M0012", "M0018", "M0024", "M0030", "M0036", "M0048", "M0060", "M0084", "M0120", "M0180", "M0240", "M0360", "M0600");		
		List<IrCurveYtmUsrHis> ytmUsrList = IrCurveYtmDao.getIrCurveYtmUsrHis(bssd, irCurveId);
		
		//Using Round Method: for avoiding truncation error in converting toReal Dimension
		double toReal = 0.01;
		int    digit  = 7;    
//		ytmUsrList.stream().forEach(s -> log.info("{}", s));		
			
		for(IrCurveYtmUsrHis usr : ytmUsrList) {
			
			for(int i=0; i<16; i++) {
				IrCurveYtm ytm = new IrCurveYtm();			
				
				ytm.setBaseDate(usr.getBaseDate());				
				ytm.setIrCurveId(irCurveId);				
				
				if(i==0) { 
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0003(), 0.0) * toReal, digit));
				}
				else if(i==1) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0006(), 0.0) * toReal, digit));
				}
				else if(i==2) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0009(), 0.0) * toReal, digit));
				}
				else if(i==3) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0012(), 0.0) * toReal, digit));
				}
				else if(i==4) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0018(), 0.0) * toReal, digit));
				}
				else if(i==5) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0024(), 0.0) * toReal, digit));
				}
				else if(i==6) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0030(), 0.0) * toReal, digit));
				}
				else if(i==7) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0036(), 0.0) * toReal, digit));
				}
				else if(i==8) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0048(), 0.0) * toReal, digit));
				}
				else if(i==9) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0060(), 0.0) * toReal, digit));
				}
				else if(i==10) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0084(), 0.0) * toReal, digit));
				}
				else if(i==11) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0120(), 0.0) * toReal, digit));
				}
				else if(i==12) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0180(), 0.0) * toReal, digit));
				}
				else if(i==13) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0240(), 0.0) * toReal, digit));
				}
				else if(i==14) {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0360(), 0.0) * toReal, digit));
				}
				else {
					ytm.setMatCd(ytmTen.get(i));
					ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtmM0600(), 0.0) * toReal, digit));
				}

				ytm.setLastModifiedBy(jobId);
				ytm.setLastUpdateDate(LocalDateTime.now());
									
				ytmList.add(ytm);
			}				
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), ytmList.size(), toPhysicalName(IrCurveYtmUsrHis.class.getSimpleName()), toPhysicalName(IrCurveYtm.class.getSimpleName()));
		
		return ytmList;
	}	
	
	
	public static List<IrCurveYtm> createYtmFromUsr(String bssd, String irCurveId) {
		
		List<IrCurveYtm>    ytmList    = new ArrayList<IrCurveYtm>();		
		List<IrCurveYtmUsr> ytmUsrList = IrCurveYtmDao.getIrCurveYtmUsr(bssd, irCurveId);
		
		//Using Round Method: for avoiding truncation error in converting toReal Dimension
		double toReal = 1;
		int    digit  = 7;    
//		ytmUsrList.stream().forEach(s -> log.info("{}", s));		
			
		for(IrCurveYtmUsr usr : ytmUsrList) {
			
			IrCurveYtm ytm = new IrCurveYtm();				
			ytm.setBaseDate(usr.getBaseDate());				
			ytm.setIrCurveId(irCurveId);				

			ytm.setMatCd(usr.getMatCd());
			ytm.setYtm(round(StringUtil.objectToPrimitive(usr.getYtm(), 0.0) * toReal, digit));

			ytm.setLastModifiedBy(jobId);
			ytm.setLastUpdateDate(LocalDateTime.now());
								
			ytmList.add(ytm);
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), ytmList.size(), toPhysicalName(IrCurveYtmUsr.class.getSimpleName()), toPhysicalName(IrCurveYtm.class.getSimpleName()));
		
		return ytmList;
	}		
	
}
