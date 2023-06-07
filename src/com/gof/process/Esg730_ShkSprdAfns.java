package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.gof.entity.IrSprdAfnsCalc;
import com.gof.enums.EJob;
import com.gof.entity.IrParamAfnsCalc;
import com.gof.entity.IrCurveSpot;
import com.gof.model.AFNelsonSiegel;
import com.gof.model.IrModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg730_ShkSprdAfns extends Process {	
	
	public static final Esg730_ShkSprdAfns INSTANCE = new Esg730_ShkSprdAfns();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리오 생성 </br> 
	 * @param 
	 * @See getAfnsResultList
	 * */
	public static Map<String, List<?>> createAfnsShockScenario(String bssd, String mode, List<IrCurveSpot> curveHisList, List<IrCurveSpot> curveBaseList, List<String> tenorList, 
	           double dt, double initSigma, double ltfr, double ltfrT, int prjYear, double errorTolerance, int itrMax, double confInterval, double epsilon
	           , List<IrParamAfnsCalc> optInput  // add 
	           )	
	{		
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		
		List<IrParamAfnsCalc> inOptParam = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("paras"))
			                               .collect(Collectors.toList());
		
		List<IrParamAfnsCalc> inOptLsc  = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("LSC"))
                .collect(Collectors.toList());
		
		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
                true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
                0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);
		
		afns.genAfnsShock(inOptParam , inOptLsc);
		irShock.       addAll(afns.getAfnsShockList());
		
		// fk 값 추가 
		irShock.     stream().forEach(s -> s.setLastModifiedBy(jobId));

		resultMap.put("SHOCK",  irShock);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
		
		return resultMap;
	}
	
	
}
