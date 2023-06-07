package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gof.entity.IrParamAfnsCalc;
import com.gof.enums.EJob;
import com.gof.entity.IrCurveSpot;
import com.gof.model.AFNelsonSiegel;
import com.gof.model.IrModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg710_SetAfnsInitParam extends Process {	
	
	public static final Esg710_SetAfnsInitParam INSTANCE = new Esg710_SetAfnsInitParam();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리오 : 초기모수 산출 </br> 
	 * @param 
	 * */
	public static Map<String, List<?>> setAfnsInitParam(String bssd, String mode, List<IrCurveSpot> curveHisList, List<IrCurveSpot> curveBaseList, List<String> tenorList, 
	           double dt, double initSigma, double ltfr, double ltfrT, int prjYear, double errorTolerance, int itrMax, double confInterval, double epsilon)	
	{
	
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>(); 
		List<IrParamAfnsCalc> irAfnsInitParam    = new ArrayList<IrParamAfnsCalc>();
		
		
		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
                true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
                0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon);
		
		// 초기 모수 산출
		afns.getinitialAfnsParas();
		
		// 산출한 초기모수를 IrParamAfnsCalc 에 담기
		irAfnsInitParam.  addAll(afns.setAfnsInitParamList());
		
		// fk 값 추가 
		irAfnsInitParam.stream().forEach(s -> s.setLastModifiedBy(jobId));

		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irAfnsInitParam.size(), toPhysicalName(IrParamAfnsCalc.class.getSimpleName()));

		resultMap.put("PARAM", irAfnsInitParam);
		
		return resultMap;
	}
	
	
}
