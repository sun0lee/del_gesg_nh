package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrCurveYtm;
import com.gof.entity.IrCurveYtmUsr;
import com.gof.entity.IrCurveYtmUsrHis;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrCurveYtmDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static String getMaxBaseDate(String bssd, String irCurveId) {
		
		String query = "select max(a.baseDate) "
					 + "  from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveId = :irCurveId                   "
					 + "   and a.baseDate <= :bssd                        "	
					 + "   and substr(a.baseDate,1,6) = substr(:bssd,1,6) "
					 ;
		
		Object maxDate =  session.createQuery(query)
				 				 .setParameter("irCurveId", irCurveId)			
								 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
								 .uniqueResult();
		
		if(maxDate==null) {
			log.warn("IR Curve YTM History Data is not found {} at {}" , irCurveId, FinUtils.toEndOfMonth(bssd));
			return FinUtils.toEndOfMonth(bssd);
		}
		
		return maxDate.toString();
	}
	
	//Used in job ESG270(SW BootStrapping Interpolation of Asset Discount Rate) 
	public static List<IrCurveYtm> getIrCurveYtm(String bssd, String irCurveId) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveId = :irCurveId "
					 + "   and a.baseDate  = :bssd	    "
					 + "   and a.ytm is not null        "
					 + "   order by a.matCd             "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveId", irCurveId)
										   .setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
										   .getResultList();
		
//		log.info("maxDate : {}, curveSize : {}", getMaxBaseDate(bssd, irCurveId),curveRst.size());
		return curveRst;
	}
	
	
	public static List<IrCurveYtm> getIrCurveYtm(String bssd, String irCurveId, List<String> tenorList) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and a.irCurveId = :irCurveId "
					 + "   and a.baseDate  = :bssd	    "
					 + "   and a.ytm is not null        "
					 + "   and a.matCd in (:matCdList)  "
					 + " order by a.matCd               "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveId", irCurveId)
										   .setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
										   .setParameterList("matCdList", tenorList)
										   .getResultList();		

		return curveRst;
	}	
	
	
	public static List<IrCurveYtm> getIrCurveYtmHis(String bssd, String irCurveId, int monthNum, String matCd) {
		
		String query = "select a from IrCurveYtm a                    "
					 + " where 1=1                                    "
					 + "   and substr(a.baseDate, 1, 6) >  :stYymm    "
					 + "   and substr(a.baseDate, 1, 6) <= :endYymm   "
					 + "   and a.irCurveId               = :irCurveId "					 
					 + "   and a.matCd                   = :matCd     "
					 + " order by a.baseDate                          " 
					 ;
		
		return session.createQuery(query, IrCurveYtm.class)
					  .setParameter("stYymm", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endYymm", bssd)
					  .setParameter("irCurveId", irCurveId)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}		
	
	
	public static List<IrCurveYtm> getIrCurveYtmMonth(String bssd, String irCurveId) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "
					 + "   and substr(a.baseDate,1,6) = :bssd "
					 + "   and a.irCurveId = :irCurveId       "
					 + "   and a.ytm is not null              "
					 + " order by a.matCd                     "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
									 	   .setParameter("irCurveId", irCurveId)
										   .setParameter("bssd", bssd)
										   .getResultList();
		
		return curveRst;
	}	
	
	
	public static List<IrCurveYtm> getIrCurveYtmMonth(String bssd, String irCurveId, List<String> tenorList) {
		
		String query = "select a from IrCurveYtm a "
					 + " where 1=1 "					 
					 + "   and substr(a.baseDate,1,6)  = :bssd "
					 + "   and a.irCurveId = :irCurveId        "
					 + "   and a.ytm is not null               "
					 + "   and a.matCd in (:matCdList)         "
					 + " order by a.matCd                      "
					 ;
		
		List<IrCurveYtm> curveRst = session.createQuery(query, IrCurveYtm.class)
										   .setParameter("irCurveId", irCurveId)
										   .setParameter("bssd", bssd)
										   .setParameterList("matCdList", tenorList)
										   .getResultList();		
		
		return curveRst;
	}	
	
	
	public static List<IrCurveYtmUsrHis> getIrCurveYtmUsrHis(String bssd, String irCurveId) {
		
		String query = " select a from IrCurveYtmUsrHis a      " 
					 + "  where 1=1                            "
					 + "    and substr(a.baseDate,1,6) = :bssd "
					 + "    and a.irCurveId = :irCurveId       "
					 + "  order by a.baseDate                  "				
		 		 	 ;
		
		return session.createQuery(query, IrCurveYtmUsrHis.class)
					  .setParameter("bssd", bssd)
					  .setParameter("irCurveId", irCurveId)
					  .getResultList()
					  ;
	}	

	
	public static List<IrCurveYtmUsr> getIrCurveYtmUsr(String bssd, String irCurveId) {
		
		String query = " select a from IrCurveYtmUsr a         " 
					 + "  where 1=1                            "
					 + "    and substr(a.baseDate,1,6) = :bssd "
					 + "    and a.irCurveId = :irCurveId       "
					 + "  order by a.baseDate                  "				
		 		 	 ;
		
		return session.createQuery(query, IrCurveYtmUsr.class)
					  .setParameter("bssd", bssd)
					  .setParameter("irCurveId", irCurveId)
					  .getResultList()
					  ;
	}	
	
}
