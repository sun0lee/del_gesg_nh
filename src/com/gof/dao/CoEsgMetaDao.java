package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.CoEsgMeta;
import com.gof.enums.EBoolean;
import com.gof.util.HibernateUtil;

public class CoEsgMetaDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<CoEsgMeta> getCoEsgMeta() {
		
		String q = "select a from CoEsgMeta a "	;		
		return session.createQuery(q, CoEsgMeta.class).getResultList();
	}

	
	public static List<CoEsgMeta> getCoEsgMeta(String groupId) {
		
		String q = " select a from CoEsgMeta a "
				+ "   where 1=1 "
				+ "	  and a.groupId = :groupId	" 
				+ "   and a.useYn = :param"
				;
		
		return session.createQuery(q, CoEsgMeta.class)
				.setParameter("groupId", groupId)
				.setParameter("param", EBoolean.Y)
				.getResultList();
	}
	
}
