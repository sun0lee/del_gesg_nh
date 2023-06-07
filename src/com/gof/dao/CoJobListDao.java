package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.CoJobList;
import com.gof.util.HibernateUtil;

public class CoJobListDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<CoJobList> getCoJobList() {
		
		String q = " select a from CoJobList a     "
				+ "   where 1=1                    "				 
				+ "     and upper(a.useYn) =:useYn "
				+ "   order by a.jobId             "
				;
		
		return session.createQuery(q, CoJobList.class)				
					  .setParameter("useYn", "Y")
					  .getResultList();
	}
	
	public static List<CoJobList> getCoJobList(String jobType) {
		
		String q = " select a from CoJobList a     "
				+ "   where 1=1                    "
				+ "     and jobNm like :jobNm      "
				+ "   order by a.jobId             "
				;
		
		return session.createQuery(q, CoJobList.class)				
					  .setParameter("jobNm", "%"+jobType+"%")
					  .getResultList();
	}	
	
}
