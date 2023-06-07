package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gof.dao.IrParamModelDao;
import com.gof.entity.IrParamModelBiz;
import com.gof.entity.IrParamModelCalc;
import com.gof.entity.IrParamModelUsr;
import com.gof.enums.EJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg420_BizParamCirForecast extends Process {	
	
	public static final Esg420_BizParamCirForecast INSTANCE = new Esg420_BizParamCirForecast();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<IrParamModelBiz> createBizCirForecastParam(String bssd, String irModelId, String irCurveId) {
		
		List<IrParamModelBiz>  paramBiz  = new ArrayList<IrParamModelBiz>();
		List<IrParamModelUsr>  paramUsr  = IrParamModelDao.getIrParamModelUsrList(bssd, irModelId, irCurveId);		
		List<IrParamModelCalc> paramCalc = IrParamModelDao.getIrParamModelCalcList(bssd, irModelId, irCurveId);
		
		if(!paramUsr.isEmpty()) {			
			paramBiz = paramUsr.stream().map(s -> s.convert(bssd)).collect(Collectors.toList());
			log.info("{}({}) creates {} results from [{}] in [Model:{}, ID:{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramBiz.size(), toPhysicalName(IrParamModelUsr.class.getSimpleName()), irModelId, irCurveId, toPhysicalName(IrParamModelBiz.class.getSimpleName()));			
		}
		else if(!paramCalc.isEmpty()) {			
			paramBiz = paramCalc.stream().map(s -> s.convert()).collect(Collectors.toList());			
			log.info("{}({}) creates {} results from [{}] in [Model:{}, ID:{}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramBiz.size(), toPhysicalName(IrParamModelCalc.class.getSimpleName()), irModelId, irCurveId, toPhysicalName(IrParamModelBiz.class.getSimpleName()));
		}
		else {
			log.warn("{}({}) No Model Parameter from CIR Forecast Model in [Model:{}, ID:{}]", jobId, EJob.valueOf(jobId).getJobName(), irModelId, irCurveId);						
		}		
		return paramBiz;
	}	
		
}

