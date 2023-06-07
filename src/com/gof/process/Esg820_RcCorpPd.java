package com.gof.process;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import com.gof.dao.RcCorpPdDao;
import com.gof.entity.RcCorpPd;
import com.gof.entity.RcCorpPdBiz;
import com.gof.entity.RcCorpTm;
import com.gof.enums.ECrdGrd;
import com.gof.enums.EJob;
import com.gof.model.entity.TransMat;
import com.gof.util.SplineInterpolator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg820_RcCorpPd extends Process {	
	
	public static final Esg820_RcCorpPd INSTANCE = new Esg820_RcCorpPd();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);	
	
	public static List<RcCorpPd> createRcCorpPd(String bssd, String crdEvalAgncyCd, Integer projectionYear) {	
		
		List<RcCorpPd> rst = new ArrayList<RcCorpPd>();		
		
		List<RcCorpTm> tmList = RcCorpPdDao.getRcCorpTm(bssd, crdEvalAgncyCd);		
		List<TransMat> tm = tmList.stream().map(s-> TransMat.convertFrom(s)).collect(toList());
//		tmList.stream().forEach(s -> log.info("{}", s.toString()));
//		log.info("{}", tm);
		
		TreeMap<Integer, Map<Integer, Double>> tmMap = new TreeMap<Integer, Map<Integer, Double>>();
		tmMap = tm.stream().collect(Collectors.groupingBy(s -> s.getFromGrdOrder(), TreeMap::new, Collectors.toMap(TransMat::getToGrdOrder, TransMat::getTransProb, (k, v) -> k, TreeMap::new)));				
		 
		TreeMap<Integer, Double> tmMapDef = new TreeMap<Integer, Double>();		
		if(!tmMap.containsKey(ECrdGrd.D.getOrder())) {
			for(Map.Entry<Integer, Double> dummy : tmMap.get(ECrdGrd.AAA.getOrder()).entrySet()) {
				if(!dummy.getKey().equals(ECrdGrd.D.getOrder())) tmMapDef.put(dummy.getKey(), 0.0);
				else tmMapDef.put(dummy.getKey(), 1.0);					
			}
			tmMap.put(ECrdGrd.D.getOrder(), tmMapDef);					
		}
//		log.info("{}", tmMap);

		int[] fromGrd    = tmMap.keySet().stream().mapToInt(Integer::intValue).toArray();                           //AAA:1, AA:3, A:6, BBB:9, BB:12, B:15, D: 18(forced put to Map)						
		int[] toGrd      = tmMap.firstEntry().getValue().keySet().stream().mapToInt(Integer::intValue).toArray();   //AAA:1, AA:3, A:6, BBB:9, BB:12, B:15, D: 18				
		double[][] tmOrg = new double[fromGrd.length][toGrd.length];						
		
		int from = 0;
		for(Map.Entry<Integer, Map<Integer, Double>> tmFrom : tmMap.entrySet()) {
			int to = 0;			
			for(Map.Entry<Integer, Double> tmTo : tmFrom.getValue().entrySet()) {
				tmOrg[from][to] = tmTo.getValue();
				to++;
			}
			from++;
		}		
		RealMatrix   transMatOrg = MatrixUtils.createRealMatrix(tmOrg);
		RealMatrix[] transMatHis = new RealMatrix[projectionYear];		

		for(int k=0; k<transMatHis.length; k++) {
			transMatHis[k] = (k>0) ? transMatHis[k-1].multiply(transMatOrg) : transMatOrg;
			
			for(int i=0; i<fromGrd.length; i++) {					
				RcCorpPd pd = new RcCorpPd();
				
				pd.setBaseYymm(bssd);
				pd.setCrdEvalAgncyCd(crdEvalAgncyCd);
				pd.setCrdGrdCd(String.valueOf(ECrdGrd.getECrdGrdFromOrder(fromGrd[i]).getAlias()));
				pd.setMatCd(String.format("%s%04d", TIME_UNIT_MONTH, (k+1) * MONTH_IN_YEAR));
				
				double cumPdCur = transMatHis[k].getEntry(i, toGrd.length-1);
				double cumPdBef = (k>0) ? transMatHis[k-1].getEntry(i, toGrd.length-1) : 0.0;
				double fwdPdCur = (cumPdCur - cumPdBef) / (1 - cumPdBef);
				pd.setCumPd(cumPdCur);
				pd.setFwdPd((i<fromGrd.length-1) ? fwdPdCur : 1.0);				
//				if (k<4) log.info("pd: {}, {}, {}, {}, {}, {}", k+1, i, fromGrd[i], cumPdCur, cumPdBef, (i<fromGrd.length-1) ? fwdPdCur : 1.0);				
				
				pd.setLastModifiedBy(jobId);
				pd.setLastUpdateDate(LocalDateTime.now());
				
				rst.add(pd);
			}			
		}		
		log.info("{}({}) creates [{}] results of [AGENCY: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), crdEvalAgncyCd, toPhysicalName(RcCorpPd.class.getSimpleName()));
		
		return rst;		
	}
	
	
	public static List<RcCorpPdBiz> createRcCorpPdBiz(String bssd, String applBizDv, String crdEvalAgncyCd, List<RcCorpPd> corpPd) {	
		
		List<RcCorpPdBiz> rst = new ArrayList<RcCorpPdBiz>();		
//		corpPd.stream().filter(s -> Integer.valueOf(s.getMatCd().substring(1)) <= 48).forEach(s -> log.info("{}", s));		
		
		TreeMap<String, TreeMap<Integer, RcCorpPd>> pdMap = new TreeMap<String, TreeMap<Integer, RcCorpPd>>();		
		pdMap = corpPd.stream().collect(Collectors.groupingBy(s -> s.getMatCd(), TreeMap::new, Collectors.toMap(s -> ECrdGrd.getECrdGrdFromAlias(s.getCrdGrdCd()).getOrder(), s -> s, (k, v) -> k, TreeMap::new)));

		int[] splineGrade           = new int[pdMap.firstEntry().getValue().lastEntry().getKey()];   //AAA:1 ~ D:18(including all 18 grade order)//
		double[] splineCumPdCur     = new double[splineGrade.length];
		double[] splineCumPdBef     = new double[splineGrade.length];		
		double[] splineFwdPdCur     = new double[splineGrade.length];
		double[] splineCumPdBefTemp = new double[splineGrade.length];
		
		for(int i=0; i<splineGrade.length; i++) {
			splineGrade[i]        = i + 1;
			splineCumPdCur[i]     = 0.0;
			splineCumPdBef[i]     = 0.0;
			splineFwdPdCur[i]     = 0.0;
			splineCumPdBefTemp[i] = 0.0;
		}		
	
		int mat = 0;
		for(Map.Entry<String, TreeMap<Integer, RcCorpPd>> pds : pdMap.entrySet()) {
			List<Double> order = new ArrayList<Double>();
			List<Double> cumPd = new ArrayList<Double>();
			List<Double> fwdPd = new ArrayList<Double>();

			for(Map.Entry<Integer, RcCorpPd> pd : pds.getValue().entrySet()) {
				order.add(Double.valueOf(pd.getKey()));
				cumPd.add(pd.getValue().getCumPd());				
				fwdPd.add(pd.getValue().getFwdPd());
			}			
			
			for(int i=0; i<splineGrade.length; i++) {				
//				if(pds.getKey().equals("M0300") || pds.getKey().equals("M0312") || pds.getKey().equals("M0324")) log.info("{}, {}, {}, {}", pds.getKey(), cumPd);
				splineCumPdCur[i] = SplineInterpolator.createMonotoneCubicSpline(order, cumPd).interpolate(splineGrade[i]);				
//				splineFwdPdCur[i] = SplineInterpolator.createMonotoneCubicSpline(order, fwdPd).interpolate(splineGrade[i]);

				double cumPdCur   = splineCumPdCur[i];
				double cumPdBef   = (mat>0) ? splineCumPdBef[i] : 0.0;
				double fwdPdCur   = (i<splineGrade.length-1) ? (cumPdCur - cumPdBef) / (1 - cumPdBef) : 1.0;				
				splineFwdPdCur[i] = fwdPdCur;				
//				if(pds.getKey().equals("M0300") || pds.getKey().equals("M0312") || pds.getKey().equals("M0324")) log.info("{}, {}, {}, {}", pds.getKey(), splineGrade[i], splineCumPdCur[i], splineCumPdBef[i]);				
				
				RcCorpPdBiz pdBiz = new RcCorpPdBiz();				
				pdBiz.setBaseYymm(bssd);
				pdBiz.setApplBizDv(applBizDv);
				pdBiz.setCrdGrdCd(String.valueOf(ECrdGrd.getECrdGrdFromOrderFull(splineGrade[i]).getAlias()));
				pdBiz.setMatCd(pds.getKey());
//				pdBiz.setCumPd(splineCumPdCur[i]);
//				pdBiz.setFwdPd(splineFwdPdCur[i]);
				pdBiz.setCumPd(Math.max(splineCumPdCur[i], splineCumPdBefTemp[i]));  //for avoiding reversal value of PD in interpolation(B- and CCC after M0300) 
				pdBiz.setFwdPd(Math.max(splineFwdPdCur[i], 0));
				
				pdBiz.setLastModifiedBy(jobId);
				pdBiz.setLastUpdateDate(LocalDateTime.now());
				
				rst.add(pdBiz);
				splineCumPdBefTemp[i] = Math.max(splineCumPdCur[i], splineCumPdBefTemp[i]);				
				splineCumPdBef[i]     = splineCumPdCur[i];
			}
			mat++;
		}
		log.info("{}({}) creates [{}] results of [{}, AGENCY: {}]. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), rst.size(), applBizDv, crdEvalAgncyCd, toPhysicalName(RcCorpPdBiz.class.getSimpleName()));
		
		return rst;		
	}	
		
}

