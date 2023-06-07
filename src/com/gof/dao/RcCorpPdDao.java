package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.RcCorpTm;
import com.gof.entity.RcCorpTmUsr;
import com.gof.util.HibernateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RcCorpPdDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();	
	
	public static List<RcCorpTm> getRcCorpTm(String bssd, String crdEvalAgncyCd) {
		
		String baseYymm = getMaxBaseYymm(bssd, crdEvalAgncyCd);		
		
		String query = " select a                                  "
				     + "   from RcCorpTm a                         " 
		 		 	 + "  where a.baseYymm = :baseYymm             "
		 		 	 + "    and a.crdEvalAgncyCd = :crdEvalAgncyCd "
		 		 	 ;
		
		return session.createQuery(query, RcCorpTm.class)
					  .setParameter("baseYymm", baseYymm)					  
					  .setParameter("crdEvalAgncyCd", crdEvalAgncyCd)
					  .getResultList()
					  ;
	}
	
	
	public static String getMaxBaseYymm(String bssd, String crdEvalAgncyCd) {
		
		String query = "select max(a.baseYymm)                    "
					 + "  from RcCorpTm a                         "
					 + " where 1=1                                "
					 + "   and a.baseYymm <= :bssd	              "					
					 + "   and a.crdEvalAgncyCd = :crdEvalAgncyCd "
					 ;		
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", bssd)			
								.setParameter("crdEvalAgncyCd", crdEvalAgncyCd)
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("Corporate Transition Matrix is not found [AGENCY: {}] at [{}]", crdEvalAgncyCd, bssd);
			return bssd;
		}		
		
		return maxDate.toString();
	}
	
	
	public static String getMaxBaseYymm(String bssd) {
		
		String query = "select max(a.baseYymm)                    "
					 + "  from RcCorpTm a                         "
					 + " where 1=1                                "
					 + "   and a.baseYymm <= :bssd	              "
					 ;		
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", bssd)								
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("Corporate Transition Matrix is not found at [{}]", bssd);
			return bssd;
		}		
		
		return maxDate.toString();
	}	
	
	
	public static List<String> getAgencyCd(String bssd) {
		
		String baseYymm = getMaxBaseYymm(bssd);
		
		String query = "select distinct(a.crdEvalAgncyCd)         "
					 + "  from RcCorpTm a                         "
					 + " where 1=1                                "
					 + "   and a.baseYymm = :baseYymm	          "					 
					 ;		
		
		@SuppressWarnings("unchecked")
		List<String> agencyList = session.createQuery(query)					
					   				     .setParameter("baseYymm", baseYymm)
									     .getResultList();		
		
		if(agencyList == null) log.warn("Corporate Transition Matrix is not found at [{}]", bssd);
		
		return agencyList;
	}	
	
	
	public static List<RcCorpTmUsr> getRcCorpTmUsr(String bssd, String crdEvalAgncyCd) {
		
		String baseYymm = getMaxBaseYymmUsr(bssd, crdEvalAgncyCd);		
		
		String query = " select a                                  "
				     + "   from RcCorpTmUsr a                      " 
		 		 	 + "  where a.baseYymm = :baseYymm             "
		 		 	 + "    and a.crdEvalAgncyCd = :crdEvalAgncyCd "
		 		 	 ;
		
		return session.createQuery(query, RcCorpTmUsr.class)
					  .setParameter("baseYymm", baseYymm)					  
					  .setParameter("crdEvalAgncyCd", crdEvalAgncyCd)
					  .getResultList()
					  ;
	}

	
	public static String getMaxBaseYymmUsr(String bssd, String crdEvalAgncyCd) {
		
		String query = "select max(a.baseYymm)                    "
					 + "  from RcCorpTmUsr a                      "
					 + " where 1=1                                "
					 + "   and a.baseYymm <= :bssd	              "					
					 + "   and a.crdEvalAgncyCd = :crdEvalAgncyCd "
					 ;		
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", bssd)			
								.setParameter("crdEvalAgncyCd", crdEvalAgncyCd)
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("Corporate Transition Matrix User Defined is not found [AGENCY: {}] at [{}]", crdEvalAgncyCd, bssd);
			return bssd;
		}		
		
		return maxDate.toString();
	}
	
	
	public static String getMaxBaseYymmUsr(String bssd) {
		
		String query = "select max(a.baseYymm)                    "
					 + "  from RcCorpTmUsr a                      "
					 + " where 1=1                                "
					 + "   and a.baseYymm <= :bssd	              "
					 ;		
		
		Object maxDate = session.createQuery(query)					
								.setParameter("bssd", bssd)								
								.uniqueResult();
		
		if(maxDate == null) {
			log.warn("Corporate Transition Matrix User Defined is not found at [{}]", bssd);
			return bssd;
		}		
		
		return maxDate.toString();
	}
	
	
	public static List<String> getAgencyCdUsr(String bssd) {
		
		String baseYymm = getMaxBaseYymmUsr(bssd);
				
		String query = "select distinct(a.crdEvalAgncyCd)         "
					 + "  from RcCorpTmUsr a                      "
					 + " where 1=1                                "
					 + "   and a.baseYymm = :baseYymm	          "					 
					 ;		
		
		@SuppressWarnings("unchecked")
		List<String> agencyList = session.createQuery(query)					
					   				     .setParameter("baseYymm", baseYymm)
									     .getResultList();		
		
		if(agencyList == null) log.warn("Corporate Transition Matrix User Defined is not found at [{}]", bssd);
		
		return agencyList;
	}		
	
}
