package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EJob;
import com.gof.entity.IrParamAfnsBiz;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrDcntSceDetBiz;
import com.gof.model.AFNelsonSiegel;
import com.gof.model.IrModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg220_ShkSprdAfns extends Process {	
	
	public static final Esg220_ShkSprdAfns INSTANCE = new Esg220_ShkSprdAfns();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	

	public static Map<String, List<?>> createAfnsShockScenario(String bssd, String mode, List<IrCurveSpot> curveHisList, List<IrCurveSpot> curveBaseList, List<String> tenorList, 
													           double dt, double initSigma, double ltfr, double ltfrT, int prjYear, double errorTolerance, int itrMax, double confInterval, double epsilon)	
	{		
		Map<String, List<?>>  irShockSenario  = new TreeMap<String, List<?>>();
		List<IrParamAfnsCalc> irShockParam    = new ArrayList<IrParamAfnsCalc>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		List<IrDcntSceDetBiz> irScenarioList  = new ArrayList<IrDcntSceDetBiz>();				

		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
				                                 true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
				                                 0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);

//		AFNelsonSiegelHetero afns = new AFNelsonSiegelHetero(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
//                								 	   		 true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
//                								 	   		 0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);
		
		irScenarioList.addAll(afns.getAfnsResultList());
		irShockParam.  addAll(afns.getAfnsParamList());
		irShock.       addAll(afns.getAfnsShockList());
		
		irShockParam.stream().forEach(s -> s.setLastModifiedBy(jobId));
		irShock.     stream().forEach(s -> s.setLastModifiedBy(jobId));

//		irShockSenario.put("CURVE",  irScenarioList);
		irShockSenario.put("PARAM",  irShockParam);
		irShockSenario.put("SHOCK",  irShock);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockParam.size(), toPhysicalName(IrParamAfnsCalc.class.getSimpleName()));
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
		
		return irShockSenario;
	}
	
	//for inputParas
	public static Map<String, List<?>> createAfnsShockScenarioByParam(String bssd, String mode, List<IrParamAfnsBiz> inputParas, List<IrCurveSpot> curveBaseList, List<String> tenorList, 
													           	      double dt, double initSigma, double ltfr, double ltfrT, int prjYear, double errorTolerance, int itrMax, double confInterval, double epsilon)	
	{		
		Map<String, List<?>>  irShockSenario  = new TreeMap<String, List<?>>();
		List<IrParamAfnsCalc> irShockParam    = new ArrayList<IrParamAfnsCalc>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		List<IrDcntSceDetBiz> irScenarioList  = new ArrayList<IrDcntSceDetBiz>();				
		
		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), mode, inputParas, curveBaseList, 
				                                 true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
				                                 0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);

		irScenarioList.addAll(afns.getAfnsResultList());
		irShockParam.  addAll(afns.getAfnsParamList());
		irShock.       addAll(afns.getAfnsShockList());
		
//		log.info("{}", irShockParam.toString());
//		log.info("{}", irShock.toString());
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShockParam.size(), toPhysicalName(IrParamAfnsBiz.class.getSimpleName()));
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
//		irScenarioList.stream().filter(s -> s.getSceNo().equals(1)).filter(s -> Integer.valueOf(s.getMatCd().substring(1, 5)) <= 12).forEach(s->log.warn("Arbitrage Free Nelson Siegle Scenario Result : {}", s.toString()));		

//		irShockSenario.put("CURVE",  irScenarioList);
		irShockSenario.put("PARAM",  irShockParam);
		irShockSenario.put("SHOCK",  irShock);
		
		return irShockSenario;
	}	
	
}
