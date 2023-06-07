package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrParamModelCalc;
import com.gof.enums.EJob;
import com.gof.model.CIRCalibrationForcast;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg410_ParamCirForecast extends Process {	
	
	public static final Esg410_ParamCirForecast INSTANCE = new Esg410_ParamCirForecast();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrParamModelCalc> createCirParam(String bssd, String irModelId, String irCurveId, Double dt, List<IrCurveSpot> spotList, Double accuracy) {	
		
		List<IrParamModelCalc> rst = new ArrayList<IrParamModelCalc>();
		
		if(spotList.isEmpty()) {
			log.warn("No IR History Data of [MODEL:{}, ID:{}] in [{}]", irModelId, irCurveId, jobId);
			return rst;
		}
		
		CIRCalibrationForcast cir = CIRCalibrationForcast.of()
				                   						 .bssd(bssd)
				                   						 .dt(dt)              //Calibration: daily spot rate
				                   						 .iRateHisList(spotList)				                   						 
				                   						 .accuracy(accuracy)
				                   						 .build();

		rst = cir.getCalibrationResult();
	
		rst.stream().forEach(s -> s.setBaseYymm(bssd));
		rst.stream().forEach(s -> s.setIrModelId(irModelId));
		rst.stream().forEach(s -> s.setIrCurveId(irCurveId));
		rst.stream().forEach(s -> s.setLastModifiedBy(jobId));
		rst.stream().forEach(s -> s.setLastUpdateDate(LocalDateTime.now()));

		log.info("{}({}) creates [{}] results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), toPhysicalName(IrParamModelCalc.class.getSimpleName()));
		
		return rst;		
	}
		
}

