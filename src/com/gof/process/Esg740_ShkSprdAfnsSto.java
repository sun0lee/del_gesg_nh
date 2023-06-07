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
public class Esg740_ShkSprdAfnsSto extends Process {	
	
	public static final Esg740_ShkSprdAfnsSto INSTANCE = new Esg740_ShkSprdAfnsSto();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	/** <p> AFNS 충격시나리 스프레드 생성 for TVOG 산출 </br> 
	 * @param 
	 * @See getAfnsResultList
	 * */
	public static Map<String, List<?>> createAfnsShockScenario
			( String bssd
			, String mode
			, List<IrCurveSpot> curveHisList
			, List<IrCurveSpot> curveBaseList
			, List<String> tenorList
			, double dt
			, double initSigma
			, double ltfr
			, double ltfrT
			, int prjYear
			, double errorTolerance
			, int itrMax
			, double confInterval
			, double epsilon
	        , List<IrParamAfnsCalc> optInput  // add 
	           )	
	{		
		Map<String, List<?>>  resultMap  = new TreeMap<String, List<?>>();
		List<IrSprdAfnsCalc>  irShock         = new ArrayList<IrSprdAfnsCalc>();		
		
		List<IrParamAfnsCalc> inOptParam = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("paras"))
			                               .collect(Collectors.toList());
		
		List<IrParamAfnsCalc> inOptLsc  = optInput.stream().filter(param -> param.getParamTypCd().getParamDv().equals("LSC"))
                .collect(Collectors.toList());

		int     randomGenType = 1;
		int     seedNum       = 470 ;
		// TODO: mstModel에 설정하는 시나리오 넘버는 결정론적 시나리오 갯수임. 내부모형에서 tvog산출목적의 시나리오갯수는 바깥에서 정의할 데가 없음. => 1000개 고정 
//		int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getTotalSceNo()), SCEN_NUM);	
		// TODO : AFNS 에서 그동안 난수가 필요 없었으므로 정의하지 않았음. 필요 시, 정의하기로 약속을 먼저 해야 함. => 일단 하드코딩    
//		int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMst.getRndSeed())   , RANDOM_SEED);

		AFNelsonSiegel afns = new AFNelsonSiegel(IrModel.stringToDate(bssd), mode, null, curveHisList, curveBaseList,
                true, 'D', dt, initSigma, DCB_MON_DIF, ltfr, 0, (int) ltfrT, 0.0, 1.0 / 12, 
                0.05, 2.0, 3, prjYear, errorTolerance, itrMax, confInterval, epsilon
                , randomGenType ,seedNum);
		
		afns.genAfnsStoShock(inOptParam , inOptLsc);
		irShock.      addAll(afns.getAfnsShockList());
		
		// fk 값 추가 
		irShock.     stream().forEach(s -> s.setLastModifiedBy(jobId));

		resultMap.put("SHOCK",  irShock);
		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), irShock.size(), toPhysicalName(IrSprdAfnsCalc.class.getSimpleName()));
		
		return resultMap;
	}
	
	
}
