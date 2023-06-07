package com.gof.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrSprdCurve;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveSpot;
import com.gof.enums.EBoolean;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveSpotDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	public static List<IrCurve> getIrCurveByCrdGrdCd(String crdCrdCd) {
		
		String query = "select a from IrCurve a "
					 + "where 1=1 "
					 + "and a.creditGrate = :crdGrdCd "
					 + "and a.useYn		  = :useYn "
					 + "and a.applMethDv  <> '6' "					
					 + "and a.refCurveId  is null "
					 ;
		
		return   session.createQuery(query, IrCurve.class)
								 .setParameter("crdGrdCd",crdCrdCd )				
								 .setParameter("useYn", EBoolean.Y )				
								 .getResultList();
								 
	}
	
	
	public static List<IrCurve> getBottomUpIrCurve() {
		return getIrCurveByGenMethod("4");
	}
	
	
	public static List<IrCurve> getIrCurveByGenMethod(String applMethDv) {
		
		String query = "select a from IrCurve a "
					 + "where 1=1 "
					 + "and a.applMethDv = :applMethDv "
					 + "and a.useYn = :useYn"
					 ;
		
		return   session.createQuery(query, IrCurve.class)
								 .setParameter("applMethDv",applMethDv)			// Bond Gen : 3, BottomUp : 4 , TopDown : 6, KICS : 5 SwapRate : 7
								 .setParameter("useYn", EBoolean.Y)				
								 .getResultList();
	}
	
	
	public static Map<String, String> getEomMap(String bssd, String irCurveId) {
		
		String query = "select substring(a.baseDate, 0, 6), max(a.baseDate) "
					 + "from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.irCurveId = :irCurveId "
					 + "and a.baseDate <= :bssd	"
					 + "group by substring(a.baseDate, 0, 6)"
					 ;
		
		@SuppressWarnings("unchecked")
		List<Object[]> maxDate = session.createQuery(query)
				 						.setParameter("irCurveId", irCurveId)			
				 						.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				 						.getResultList();
		
//		if(maxDate == null) {
//			log.warn("IR Curve History Data is not found {} at {}" , irCurveId, FinUtils.toEndOfMonth(bssd));
//			return new hashMap<String, String>;
//		}
		
		Map<String, String> rstMap = new HashMap<String, String>();
		for(Object[] aa : maxDate) {
			rstMap.put(aa[0].toString(), aa[1].toString());
		}
		return rstMap;
	}
	
	
	public static String getEomDate(String bssd, String irCurveId) {
		
		String query = "select max(a.baseDate) "
				     + "from IrCurveSpot a "
				     + "where 1=1 "
				     + "and a.irCurveId = :irCurveId "
				     + "and a.baseDate >= :bom	"
				     + "and a.baseDate <= :eom	"
				     ;
		
		Object maxDate = session.createQuery(query)
				 				.setParameter("irCurveId", irCurveId)			
				 				.setParameter("bom", bssd)
								.setParameter("eom", FinUtils.toEndOfMonth(bssd))
								.uniqueResult();
		if(maxDate == null) {
			log.warn("IR Curve History Data is not found {} at {}" , irCurveId, bssd);
			return bssd;
		}
		return maxDate.toString();
	}
	
	
	public static String getMaxBaseDate(String bssd, String irCurveId) {
		
		String query = "select max(a.baseDate) "
					 + "from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.baseDate <= :bssd	"
					 + "and substr(a.baseDate,1,6) = substr(:bssd,1,6) "
					 + "and a.irCurveId = :irCurveId "					 
					 ;
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				 			 	.setParameter("irCurveId", irCurveId)
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("IR Curve History Data is not found {} at {}" , irCurveId, FinUtils.toEndOfMonth(bssd));
			return bssd;
		}		
		
		return maxDate.toString();
	}
	
	
	public static List<IrCurveSpot> getIrCurveSpotHis(String bssd, String irCurveId, int monthNum, String matCd) {
		
		String query = "select a from IrCurveSpot a                   "
					 + " where 1=1                                    "
					 + "   and substr(a.baseDate, 1, 6) >  :stYymm    "
					 + "   and substr(a.baseDate, 1, 6) <= :endYymm   "
					 + "   and a.irCurveId               = :irCurveId "					 
					 + "   and a.matCd                   = :matCd     "
					 + " order by a.baseDate                          " 
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("stYymm", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endYymm", bssd)
					  .setParameter("irCurveId", irCurveId)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}		
	

	public static List<IrCurveSpot> getCurveHisBetween(String bssd, String stBssd, String curveId){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate <= :bssd	"
				+ "and a.baseDate >= :stBssd "
				+ "and a.irCurveId =:param1 "
//				+ "and a.matCd not in (:matCd1, :matCd2, :matCd3) "
				+ "order by a.baseDate"
				;
		
		List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", curveId)
				.setParameter("bssd", FinUtils.addMonth(bssd, 1))
				.setParameter("stBssd", stBssd)
//				.setParameter("matCd1", "M0018")
//				.setParameter("matCd2", "M0030")
//				.setParameter("matCd3", "M0048")
				.getResultList();		
		
//		Map<String, Map<String, IrCurveHis>> curveMap = curveRst.stream().collect(Collectors.groupingBy(s -> s.getMatCd()
//				, Collectors.toMap(s-> s.getBaseYymm(), Function.identity(), (s,u)->u)));
//		curveMap.entrySet().forEach(s -> log.info("aaa : {},{},{}", s.getKey(), s.getValue()));
		return curveRst;
	}
	
	
	public static List<IrCurveSpot> getShortRateBtw(String stBssd, String bssd, String curveId){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate <= :bssd	"
				+ "and a.baseDate >= :stBssd "
				+ "and a.irCurveId =:param1 "
				+ "and a.matCd = :matCd "
				+ "order by a.baseDate desc"
				;
		
		List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", curveId)
				.setParameter("stBssd", stBssd+"01")
				.setParameter("bssd", bssd+"31")
				.setParameter("matCd", "M0003")
				.getResultList();		
		
//		Map<String, Map<String, IrCurveHis>> curveMap = curveRst.stream().collect(Collectors.groupingBy(s -> s.getMatCd()
//				, Collectors.toMap(s-> s.getBaseYymm(), Function.identity(), (s,u)->u)));
//		curveMap.entrySet().forEach(s -> log.info("aaa : {},{},{}", s.getKey(), s.getValue()));
		return curveRst;
	}
	
	
	public static IrCurveSpot getShortRateHis(String bssd, String curveId){
		String query = "select a from IrCurveSpot a "
				+ "where 1=1 "
				+ "and a.baseDate = :bssd	"
				+ "and a.irCurveId =:param1 "
				+ "and a.matCd = :matCd "
				+ "order by a.baseDate"
				;
		
		IrCurveSpot curveRst =  session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", curveId)
				.setParameter("bssd", getMaxBaseDate(bssd, curveId))
				.setParameter("matCd", "M0003")
				.getSingleResult()
				;		
		

		return curveRst;
	}
	
	public static List<IrCurveSpot> getIrCurveHisByMaturityHis(String bssd, int monthNum, String irCurveId, String matCd) {
		
		String query = "select a from IrCurveSpot a "
					 + "where 1=1 "
					 + "and a.irCurveId =:param1 "
					 + "and a.baseDate >=:stBssd "
					 + "and a.baseDate <=:bssd "
					 + "and a.matCd =:param2 ";
					 ;
		
		return   session.createQuery(query, IrCurveSpot.class)
				.setParameter("param1", irCurveId)
				.setParameter("stBssd", FinUtils.addMonth(bssd, monthNum)+"01")
				.setParameter("bssd", bssd+"31")
				.setParameter("param2", matCd)				
				.getResultList();
	}
	

	public static List<IrCurveSpot> getKTBMaturityHis(String bssd, String matCds){
//	public static List<IrCurveHis> getKTBMaturityHis(String bssd, String matCd1, String matCd2){
		String matCd1 = matCds.split(",")[0].trim();
		String matCd2 ="";
		if(matCds.split(",").length==2) {
			matCd2 =matCds.split(",")[1].trim();
		}
		
		String query = 	"select new com.gof.entity.IrCurveSpot (substr(a.baseDate,1,6), a.matCd, avg(a.intRate)) "
					+ "from IrCurveSpot a "
					+ "where 1=1 "
					+ "and a.baseDate <= :bssd	"
					+ "and a.irCurveId =:param1 "
					+ "and a.matCd in (:param2, :param3) "
					+ "group by substr(a.baseDate,1,6), a.matCd "
					;
		
		List<IrCurveSpot> curveRst = session.createQuery(query, IrCurveSpot.class)
											.setParameter("param1", "A100")
											.setParameter("param2", matCd1)
											.setParameter("param3", matCd2)
											.setParameter("bssd", FinUtils.addMonth(bssd, 1))
											.getResultList();
		
		return curveRst;
	}
	
	public static List<IrCurveSpot> getKTBMaturityHis(String bssd, String matCd1, String matCd2) {
			
			String query = "select new com.gof.entity.IrCurveSpot (substr(a.baseDate,1,6), a.matCd, avg(a.intRate)) "
						+ "from IrCurveSpot a "
						+ "where 1=1 "
						+ "and a.baseDate <= :bssd	"
						+ "and a.irCurveId =:param1 "
						+ "and a.matCd in (:param2, :param3) "
						+ "group by substr(a.baseDate,1,6), a.matCd "
						;
			
			List<IrCurveSpot> curveRst =  session.createQuery(query, IrCurveSpot.class)
					.setParameter("param1", "A100")
					.setParameter("param2", matCd1)
					.setParameter("param3", matCd2)
					.setParameter("bssd", FinUtils.addMonth(bssd, 1))
					.getResultList();		
			return curveRst;
	}
	
	
	public static Map<String, List<IrCurveSpot>> getIrCurveListTermStructure(String bssd, String stBssd, String irCurveId) {
		
		String query =" select a from IrCurveSpot a " 
					+ "where a.irCurveId =:irCurveId "			
					+ "and a.baseDate >= :stBssd "
					+ "and a.baseDate <= :bssd "
					+ "and a.matCd in (:matCdList)"
					+ "order by a.baseDate, a.matCd "
					;
		
		return session.createQuery(query, IrCurveSpot.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("stBssd", stBssd)
				.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				.stream()
//				.collect(Collectors.groupingBy(s ->s.getBaseDate(), TreeMap::new, Collectors.toList()))
				.collect(Collectors.groupingBy(s ->s.getMatCd(), TreeMap::new, Collectors.toList()))
				;
	}
	

//	public static List<_IrSce> getIrCurveSce(String bssd, String irCurveId) {
//		
//		String query ="select a from IrSce a " 
//					+ "where a.irCurveId =:irCurveId "			
//					+ "and a.baseDate = :bssd "
//				;
//		
//		return session.createQuery(query, _IrSce.class)
//				.setParameter("irCurveId", irCurveId)
//				.setParameter("bssd", bssd)
//				.setHint(QueryHints.HINT_READONLY, true)
//				.getResultList()
//				;
//	}


	
	public static List<IrCurveSpot> getEomTimeSeries(String bssd, String irCurveId, String matCd, int monNum) {
		
		Collection<String> eomList = getEomMap(bssd, irCurveId).values(); 

		String query = "select a from IrCurveSpot a "
				 	 + "where a.irCurveId = :irCurveId "
				 	 + "and a.baseDate > :stBssd "
				 	 + "and a.baseDate < :bssd "
				 	 + "and a.baseDate in :eomList "
				 	 + "and a.matCd = :matCd "
				 	 + "order by a.baseDate desc "
				 	 ;
		
		return   session.createQuery(query, IrCurveSpot.class)
				 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))			
				 .setParameter("stBssd", FinUtils.toEndOfMonth( FinUtils.addMonth(bssd, monNum)))				
				 .setParameter("irCurveId", irCurveId)				
				 .setParameter("matCd", matCd)				
				 .setParameter("eomList", eomList)	
				 .getResultList();
	}	


	public static List<IrCurveSpot> getIrCurveSpotListHis(String bssd, String stBssd, String irCurveId) {
		
		String query = " select a from IrCurveSpot a    " 
					 + "  where a.irCurveId =:irCurveId "			
					 + "    and a.baseDate >= :stBssd   "
					 + "    and a.baseDate <= :bssd     "
					 + "  order by a.baseDate, a.matCd  "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("irCurveId", irCurveId)
					  .setParameter("stBssd", stBssd)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .getResultList()
					  ;
	}	
	

	public static List<IrCurveSpot> getIrCurveSpotListHis(String bssd, String stBssd, String irCurveId, List<String> tenorList) {
		
		String query = " select a from IrCurveSpot a    " 
					 + "  where a.irCurveId =:irCurveId "			
					 + "    and a.baseDate >= :stBssd   "
					 + "    and a.baseDate <= :bssd     "
					 + "    and a.matCd in (:matCdList) "
					 + "  order by a.baseDate, a.matCd  "
					 ;	
		
		return session.createQuery(query, IrCurveSpot.class)
					  .setParameter("irCurveId", irCurveId)
					  .setParameter("stBssd", stBssd)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .setParameterList("matCdList", tenorList)
					  .getResultList()
					  ;
	}
	

	public static List<IrCurveSpot> getIrCurveSpot(String bssd, String irCurveId, List<String> tenorList) {
		
		session.clear();
		
		String query = "select a from IrCurveSpot a    "
					 + " where 1=1                     "
					 + "   and a.irCurveId =:irCurveId "
					 + "   and a.baseDate  = :bssd	   "
					 + "   and a.matCd in (:matCdList) "
					 + " order by a.matCd              "
					 ;
		
		List<IrCurveSpot> curveRst = session.createQuery(query, IrCurveSpot.class)
											.setParameter("irCurveId", irCurveId)
											.setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
											.setParameterList("matCdList", tenorList)
											.getResultList()
											;
		return curveRst;
	}	
	
	//Warning for delete command...  It resolve to append "'" ?  (.append("'")) 
	public static void deleteIrCurveSpotMonth(String bssd, String irCurveId) {		
		
		StringBuilder sb = new StringBuilder();		
		sb.append("delete IrCurveSpot a where 1=1 ")
		  .append(" and ").append(" substr(a.baseDate,1,6) ").append(" = ").append("'").append(bssd).append("'")
		  .append(" and ").append(" a.irCurveId ").append(" = ").append("'").append(irCurveId).append("'")
		  ;
	
//		log.info("Delete Qry: {}", sb);
	    session.beginTransaction();		
		session.createQuery(sb.toString()).executeUpdate();		
		
		session.getTransaction().commit();
		log.info("{} have been Deleted: [BASE_YYMM: {}, IR_CURVE_ID: {}]", log.getName(), bssd, irCurveId);		
	}
	
	
	public static void deleteIrCurveSpot(String baseYmd, String irCurveId) {		
		
		StringBuilder sb = new StringBuilder();		
		sb.append("delete IrCurveSpot a where 1=1 ")
		  .append(" and ").append(" a.baseDate ").append(" = ").append("'").append(baseYmd).append("'")
		  .append(" and ").append(" a.irCurveId ").append(" = ").append("'").append(irCurveId).append("'")
		  ;
	
	    session.beginTransaction();		
		session.createQuery(sb.toString()).executeUpdate();		
		
		session.getTransaction().commit();
		log.info("{} have been Deleted: [BASE_DATE: {}, IR_CURVE_ID: {}]", log.getName(), baseYmd, irCurveId);		
	}

	
	public static List<String> getIrCurveTenorList(String bssd, String irCurveId){
		
		String query = "select a.matCd from IrCurveSpot a "
					 + " where 1=1                        "
					 + "   and a.baseDate  =:baseYmd      "
					 + "   and a.irCurveId =:irCurveId    "
					 ;
		
		return session.createQuery(query, String.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveId))
				 	  .setParameter("irCurveId", irCurveId)
					  .getResultList();
	}
	
	
	public static List<String> getIrCurveTenorList(String bssd, String irCurveId, Integer llp){
		
		String query = "select a.matCd from IrCurveSpot a                 "
					 + " where 1=1                                        "
					 + "   and a.baseDate  =:baseYmd                      "
					 + "   and a.irCurveId =:irCurveId                    "
					 + "   and to_number(substr(a.matCd, 2)) <= :llp * 12 "
					 ;
		
		return session.createQuery(query, String.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveId))
				 	  .setParameter("irCurveId", irCurveId)
				 	  .setParameter("llp", llp)
					  .getResultList();
	}
	
	
	public static List<IrCurveSpot> getIrCurveSpot(String bssd, String irCurveId, Integer llp){
		
		String query = "select a from IrCurveSpot a                       "
					 + " where 1=1                                        "
					 + "   and a.baseDate  = :baseYmd                     "
					 + "   and a.irCurveId = :irCurveId                   "
					 + "   and to_number(substr(a.matCd, 2)) <= :llp * 12 "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveId))
				 	  .setParameter("irCurveId", irCurveId)
				 	  .setParameter("llp", llp)
					  .getResultList();
	}

	
	public static List<IrCurveSpot> getIrCurveSpot(String bssd, String irCurveId) {
		
		String query = "select a from IrCurveSpot a     "
					 + " where 1=1                      "
					 + "   and a.baseDate  = :baseYmd	"
					 + "   and a.irCurveId = :irCurveId "
					 + " order by a.matCd               "
					 ;
		
		return session.createQuery(query, IrCurveSpot.class)
				      .setParameter("baseYmd", getMaxBaseDate(bssd, irCurveId))
				      .setParameter("irCurveId", irCurveId)
				      .getResultList();		
	}	
		
	
	public static List<IrSprdCurve> getIrSprdCurve(String bssd, String irCurveId){
		
		String query = "select a from IrSprdCurve a "
					 + " where 1=1 "
					 + "   and a.baseYymm  =:bssd "
					 + "   and a.irCurveId =:irCurveId "
					 + "   and a.irTypDvCd =:irTypDvCd "
					 ;
		
		return session.createQuery(query, IrSprdCurve.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irCurveId", irCurveId)
			      	  .setParameter("irTypDvCd", "1")
					  .getResultList();
	}	
	

//	public static String getMaxBaseDateEom (String bssd, String irCurveId) {
//		String query = "select max(a.baseDate) "
//					 + "from IrCurveSpot a "
//					 + "where 1=1 "
//					 + "and a.irCurveId = :irCurveId "
//					 + "and a.baseDate <= :bssd	"
//					 + "and substr(a.baseDate,1,6) = :bssd "
//					 ;
//		Object maxDate =  session.createQuery(query)
//				 				 .setParameter("irCurveId", irCurveId)			
//								 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
//								 .uniqueResult();
//		if(maxDate==null) {
//			log.warn("IR Curve History Data is not found {} at {}" , irCurveId, FinUtils.toEndOfMonth(bssd));
//			return FinUtils.toEndOfMonth(bssd);
//		}		
//		
//		return maxDate.toString();
//	}	
	
}
