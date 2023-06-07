package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gof.dao.IrVolSwpnDao;
import com.gof.entity.IrVolSwpn;
import com.gof.entity.IrVolSwpnUsr;
import com.gof.enums.EJob;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg120_SetVolSwpn extends Process {	
	
	public static final Esg120_SetVolSwpn INSTANCE = new Esg120_SetVolSwpn();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static List<IrVolSwpn> createVolSwpnFromUsr(String bssd, String irCurveId) {
				
		List<IrVolSwpn>    volSwpn    = new ArrayList<IrVolSwpn>();		
		List<String>       swpnTen    = Arrays.asList("M0012", "M0024", "M0036", "M0060", "M0084", "M0120");		
		List<IrVolSwpnUsr> volSwpnUsr = IrVolSwpnDao.getSwpnVolUsr(bssd, irCurveId, swpnTen);
		
		double toReal = 0.01;
		int    digit  = 7;  
//		volSwpnUsr.stream().forEach(s -> log.info("{}", s));		
			
		for(IrVolSwpnUsr volUsr : volSwpnUsr) {
			
			for(int i=0; i<6; i++) {
				IrVolSwpn vol = new IrVolSwpn();			
				
				vol.setBaseYymm(bssd);				
				vol.setIrCurveId(irCurveId);				
				
				if(i==0) { 
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(1));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY1(), 25.0) * toReal, digit));
				}
				else if(i==1) {
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(2));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY2(), 25.0) * toReal, digit));
				}
				else if(i==2) {
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(3));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY3(), 25.0) * toReal, digit));
				}
				else if(i==3) {
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(5));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY5(), 25.0) * toReal, digit));					
				}
				else if(i==4) {
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(7));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY7(), 25.0) * toReal, digit));
				}
				else {
					vol.setSwpnMatNum(Integer.valueOf(volUsr.getSwpnMat().substring(1)) / MONTH_IN_YEAR);
					vol.setSwapTenNum(Integer.valueOf(10));
					vol.setVol(round(StringUtil.objectToPrimitive(volUsr.getVolSwpnY10(), 25.0) * toReal, digit));
				}
				
				vol.setLastModifiedBy(jobId);
				vol.setLastUpdateDate(LocalDateTime.now());
									
				volSwpn.add(vol);
			}				
		}
		log.info("{}({}) creates {} results from [{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), volSwpn.size(), toPhysicalName(IrVolSwpnUsr.class.getSimpleName()), toPhysicalName(IrVolSwpn.class.getSimpleName()));
		
		return volSwpn;
	}	
	
}
