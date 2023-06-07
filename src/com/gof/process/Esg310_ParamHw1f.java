package com.gof.process;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.IrValidParamHw;
import com.gof.entity.IrVolSwpn;
import com.gof.enums.EJob;
import com.gof.model.Hw1fCalibrationKics;
import com.gof.model.entity.SwpnVolInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg310_ParamHw1f extends Process {
	
	public static final Esg310_ParamHw1f INSTANCE = new Esg310_ParamHw1f();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);
	
	public static Map<String, List<?>> createParamHw1fNonSplitMap(String bssd, String irModelId, String irCurveId, List<IrCurveSpot> spotList, List<IrVolSwpn> swpnVolList, double[] initParas, Integer freq, double errTol, int[] alphaPiece, int[] sigmaPiece) {
		
		Map<String, List<?>>  irParamHw1fMap  = new TreeMap<String, List<?>>();
		List<IrParamHwCalc>   paramCalc       = new ArrayList<IrParamHwCalc>();
		List<IrValidParamHw>  validParam      = new ArrayList<IrValidParamHw>();		
				
		freq = Math.max(freq, 1);		
		List<SwpnVolInfo> volInfo  = swpnVolList.stream().map(s-> SwpnVolInfo.convertFrom(s)).collect(toList());		
		
		Hw1fCalibrationKics calib = new Hw1fCalibrationKics(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
//		Hw1fCalibrationKicsNs calib = new Hw1fCalibrationKicsNs(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
		paramCalc                 = calib.getHw1fCalibrationResultList().stream().map(s -> s.convertNonSplit(irModelId, irCurveId))
																			     .flatMap(s-> s.stream())
																			     .collect(toList());

		paramCalc.stream().forEach(s -> s.setLastModifiedBy(jobId));
		paramCalc.stream().forEach(s -> s.setLastUpdateDate(LocalDateTime.now()));
		
		validParam = calib.getValidationResult();
		validParam.stream().forEach(s -> s.setIrModelId(irModelId));
		validParam.stream().forEach(s -> s.setIrCurveId(irCurveId));
		validParam.stream().forEach(s -> s.setLastModifiedBy(jobId));
		validParam.stream().forEach(s -> s.setLastUpdateDate(LocalDateTime.now()));
		
		irParamHw1fMap.put("PARAM",  paramCalc);
		irParamHw1fMap.put("VALID",  validParam);
		
//		paramCalc.stream().forEach(s-> log.info("Calibration Result: {}", s.toString()));
//		validParam.stream().forEach(s-> log.info("Validation Result: {}", s.toString()));
		
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramCalc.size(), irModelId, toPhysicalName(IrParamHwCalc.class.getSimpleName()));
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), validParam.size(), irModelId, toPhysicalName(IrValidParamHw.class.getSimpleName()));
		
		return irParamHw1fMap;
	}
	
	
	public static Map<String, List<?>> createParamHw1fSplitMap(String bssd, String irModelId, String irCurveId, List<IrCurveSpot> spotList, List<IrVolSwpn> swpnVolList, double[] initParas, Integer freq, double errTol, int[] alphaPiece, int[] sigmaPiece) {
		
		Map<String, List<?>>  irParamHw1fMap  = new TreeMap<String, List<?>>();
		List<IrParamHwCalc>   paramCalc       = new ArrayList<IrParamHwCalc>();
//		List<IrValidParamHw>  validParam      = new ArrayList<IrValidParamHw>();		
				
		freq = Math.max(freq, 1);		
		List<SwpnVolInfo> volInfo  = swpnVolList.stream().map(s-> SwpnVolInfo.convertFrom(s)).collect(toList());		
		
		Hw1fCalibrationKics calib = new Hw1fCalibrationKics(bssd, spotList, volInfo, alphaPiece, sigmaPiece, initParas, freq, errTol);
		paramCalc                 = calib.getHw1fCalibrationResultList().stream().map(s -> s.convertSplit(irModelId, irCurveId))
																			     .flatMap(s-> s.stream())
																			     .collect(toList());

		paramCalc.stream().forEach(s -> s.setLastModifiedBy(jobId));
		paramCalc.stream().forEach(s -> s.setLastUpdateDate(LocalDateTime.now()));		
		
		irParamHw1fMap.put("PARAM",  paramCalc);
		
//		paramCalc.stream().forEach(s-> log.info("Calibration Result: {}", s.toString()));
//		validParam.stream().forEach(s-> log.info("Validation Result: {}", s.toString()));
		
		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), paramCalc.size(), irModelId, toPhysicalName(IrParamHwCalc.class.getSimpleName()));
//		log.info("{}({}) creates {} results of [MODEL: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), validParam.size(), irModelId, toPhysicalName(IrValidParamHw.class.getSimpleName()));
		
		return irParamHw1fMap;
	}	

}