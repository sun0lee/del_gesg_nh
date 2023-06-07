package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrCurveSpotWeek;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveSpotWeekDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static String getMaxBaseDate (String bssd, String irCurveId) {
		
		String query = "select max(a.baseDate) "
					 + "  from IrCurveSpotWeek a "
					 + " where 1=1 "
					 + "   and a.irCurveId = :irCurveId "
					 + "   and a.baseDate <= :bssd	"
					 + "   and substr(a.baseDate,1,6) = substr(:bssd,1,6) "
					 ;
		
		Object maxDate = session.createQuery(query)
								.setParameter("irCurveId", irCurveId)			
								.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
								.uniqueResult();
		
		if(maxDate==null) {
			log.warn("IR Curve Week Data is not found {} at {}" , irCurveId, FinUtils.toEndOfMonth(bssd));
			return FinUtils.toEndOfMonth(bssd);
		}
		return maxDate.toString();
	}
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeek(String bssd, String irCurveId){
		String query = "select a from IrCurveSpotWeek a "
					 + " where 1=1                      "
					 + "   and a.baseDate  = :bssd	    "
					 + "   and a.irCurveId = :irCurveId "
					 + " order by a.matCd               "
					 ;
		
		List<IrCurveSpotWeek> curveRst = session.createQuery(query, IrCurveSpotWeek.class)
												.setParameter("irCurveId", irCurveId)
												.setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
												.getResultList();
		return curveRst;
	}
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeek(String bssd, String irCurveId, List<String> tenorList) {
		
		String query = "select a from IrCurveSpotWeek a "
					 + " where 1=1 "
					 + "   and a.baseDate = :bssd	    "
					 + "   and a.irCurveId = :irCurveId "
					 + "   and a.matCd in (:matCdList)  "
					 + " order by a.matCd"
					 ;
		
		List<IrCurveSpotWeek> curveRst = session.createQuery(query, IrCurveSpotWeek.class)
										    	.setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
												.setParameter("irCurveId", irCurveId)				
												.setParameterList("matCdList", tenorList)
												.getResultList();
		return curveRst;
	}	
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeekMonth(String bssd, String irCurveId) {
		
		String query = "select a from IrCurveSpotWeek a "
					 + " where 1=1 "
					 + "   and a.irCurveId = :irCurveId "
					 + "   and substr(a.baseDate,1,6) = :bssd "
					 + " order by a.matCd "
					 ;
		
		List<IrCurveSpotWeek> curveRst = session.createQuery(query, IrCurveSpotWeek.class)
												.setParameter("bssd", bssd)								
												.setParameter("irCurveId", irCurveId)												
												.getResultList();		
		
		return curveRst;
	}	
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeekMonth(String bssd, String irCurveId, List<String> tenorList) {
		
		String query = "select a from IrCurveSpotWeek a "
					 + " where 1=1                             "
					 + "   and substr(a.baseDate,1,6)  = :bssd "
					 + "   and a.irCurveId =:irCurveId         "					 
					 + "   and a.matCd in (:matCdList)         "
					 + " order by a.matCd                      "
					 ;
		
		List<IrCurveSpotWeek> curveRst = session.createQuery(query, IrCurveSpotWeek.class)
											 	.setParameter("bssd", bssd)
												.setParameter("irCurveId", irCurveId)				
												.setParameterList("matCdList", tenorList)
												.getResultList();		
		
		return curveRst;
	}
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeekHis(String bssd, String stBssd, String irCurveId, List<String> tenorList) {
		return getIrCurveSpotWeekHis(bssd, stBssd, irCurveId, tenorList, 5, false);
	}
	
	
	public static List<IrCurveSpotWeek> getIrCurveSpotWeekHis(String bssd, String stBssd, String irCurveId, List<String> tenorList, Integer weekDay) {
		return getIrCurveSpotWeekHis(bssd, stBssd, irCurveId, tenorList, weekDay, false);
	}
	

	public static List<IrCurveSpotWeek> getIrCurveSpotWeekHis(String bssd, String stBssd, String irCurveId, List<String> tenorList, Integer weekDay, Boolean bizDayOnly){
		
		String weekName = getWeekDayName(weekDay); 
		String query = null;
		
		if(bizDayOnly) {		
			query       = " select a from IrCurveSpotWeek a " 
						+ "  where 1=1					    "			
						+ "    and a.baseDate <= :bssd      "
						+ "    and a.baseDate >= :stBssd    "
						+ "    and a.irCurveId = :irCurveId "
						+ "    and a.matCd in (:matCdList)  "
						+ "    and a.dayOfWeek = :weekName  "
						+ "    and a.bizDayType = 'Y'       "
						+ "  order by a.baseDate, a.matCd   "
						;
		}
		else {
			query       = " select a from IrCurveSpotWeek a " 
						+ "  where 1=1					    "			
						+ "    and a.baseDate <= :bssd      "
						+ "    and a.baseDate >= :stBssd    "
						+ "    and a.irCurveId = :irCurveId "
						+ "    and a.matCd in (:matCdList)  "
						+ "    and a.dayOfWeek = :weekName  "
						+ "  order by a.baseDate, a.matCd   "
						;
		}		
		
		return session.createQuery(query, IrCurveSpotWeek.class)
					  .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
					  .setParameter("stBssd", stBssd)
					  .setParameter("irCurveId", irCurveId)								
					  .setParameterList("matCdList", tenorList)
					  .setParameter("weekName", weekName)
					  .getResultList()
					  ;
	}
	
	
	private static String getWeekDayName(int weekDay) {
		
		switch(weekDay) {
			case 1:  return "MONDAY";
			case 2:  return "TUESDAY";
			case 3:  return "WEDNESDAY";
			case 4:  return "THURSDAY";
			case 5:  return "FRIDAY";
			case 6:  return "SATURDAY";
			case 7:  return "SUNDAY";
			default: return "FRIDAY";			
		}
	}
	
}
