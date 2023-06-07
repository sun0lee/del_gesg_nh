package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gof.dao.IrDcntRateDao;
import com.gof.dao.IrParamHwDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrDcntRateBu;
import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamModel;
import com.gof.entity.IrParamSw;
import com.gof.entity.StdAsstIrSceSto;
import com.gof.enums.EJob;
import com.gof.model.Hw1fSimulationKics;
import com.gof.model.entity.Hw1fCalibParas;
import com.gof.model.entity.IrModelBondYield;
import com.gof.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg350_BizBondYieldHw1f extends Process {
	
	public static final Esg350_BizBondYieldHw1f INSTANCE = new Esg350_BizBondYieldHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);

	public static List<StdAsstIrSceSto> createBondYieldHw1f(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, Map<String, Map<Integer, IrParamSw>> paramSwMap, Map<String, IrParamModel> modelMstMap, Integer projectionYear, Double targetDuration) {		
		
		List<StdAsstIrSceSto> rst  = new ArrayList<StdAsstIrSceSto>();
		
		for(Map.Entry<String, Map<Integer, IrParamSw>> curveSwMap : paramSwMap.entrySet()) {
			for(Map.Entry<Integer, IrParamSw> swSce : curveSwMap.getValue().entrySet()) {
				
				if(!StringUtil.objectToPrimitive(swSce.getValue().getStoSceGenYn(), "N").toUpperCase().equals("Y")) continue;
				
				if(!curveSwMap.getKey().equals(irCurveId) || !swSce.getKey().equals(irCurveSceNo)) continue;				
//				log.info("IR_CURVE_ID: [{}], IR_CURVE_SCE_NO: [{}]", curveSwMap.getKey(), swSce.getKey());
				
				if(!modelMstMap.containsKey(curveSwMap.getKey())) {
					log.warn("No Model Attribute of [{}] for [{}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamModel.class.getSimpleName()));
					continue;
				}
				
				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToAdjSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());				
//				List<IrCurveSpot> adjSpotRate = IrDcntRateDao.getIrDcntRateBuToBaseSpotList(bssd, applBizDv, curveSwMap.getKey(), swSce.getKey());
				if(adjSpotRate.isEmpty()) {
					log.warn("No Spot Rate Data [ID: {}, SCE_NO: {}] for [{}] in [{}] Table", curveSwMap.getKey(), swSce.getKey(), bssd, Process.toPhysicalName(IrDcntRateBu.class.getSimpleName()));
					continue;
				}				
									
				List<IrParamHwBiz> paramHw = IrParamHwDao.getIrParamHwBizList(bssd, applBizDv, irModelId, curveSwMap.getKey());					
				if(paramHw.isEmpty()) {
					log.warn("No HW1F Model Parameter exist in [MODEL: {}] [IR_CURVE_ID: {}] in [{}] Table", irModelId, curveSwMap.getKey(), Process.toPhysicalName(IrParamHwBiz.class.getSimpleName()));
					continue;
				}
				List<Hw1fCalibParas> hwParasList = Hw1fCalibParas.convertFrom(paramHw);
				
				int[] alphaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("ALPHA") && s.getMatCd().equals("M0240"))
										  	       .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();
				int[] sigmaPiece = paramHw.stream().filter(s->s.getParamTypCd().equals("SIGMA") && !s.getMatCd().equals("M1200") && !s.getMatCd().equals("M0240"))
												   .mapToInt(s-> Integer.valueOf(s.getMatCd().split("M")[1])/12).toArray();				
				
				boolean priceAdj      = false;
				int     randomGenType = 1;
				int     sceNum        = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getTotalSceNo()), SCEN_NUM);						
				int     seedNum       = StringUtil.objectToPrimitive(Integer.valueOf(modelMstMap.get(curveSwMap.getKey()).getRndSeed())   , RANDOM_SEED);
				double  ltfr          = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfr()    , 0.0495);
				int     ltfrCp        = StringUtil.objectToPrimitive(paramSwMap.get(curveSwMap.getKey()).get(swSce.getKey()).getLtfrCp()  , 60);				
//				log.info("seedNum: {}, {}", seedNum, bssd);		

				Hw1fSimulationKics hw1f = new Hw1fSimulationKics(bssd, adjSpotRate, hwParasList, alphaPiece, sigmaPiece, priceAdj, sceNum, ltfr, ltfrCp, projectionYear, randomGenType, seedNum);
				List<IrModelBondYield> bondYield     = hw1f.getIrModelHw1fBondYield(hw1f.getIrModelHw1fList(), targetDuration);
				List<StdAsstIrSceSto>  bondYieldList = bondYield.stream().map(s -> s.convert(applBizDv, irCurveId, irCurveSceNo, jobId)).collect(Collectors.toList());
				
				rst.addAll(bondYieldList);				
			}
		}
		log.info("{}({}) creates [{}] results of [{}] [ID: {}, SCE: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, irCurveId, irCurveSceNo, toPhysicalName(StdAsstIrSceSto.class.getSimpleName()));
		
		return rst;		
	}	
	

}