package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrParamAfnsCalc;
import com.gof.util.HibernateUtil;

public class IrParamAfnsDao extends DaoUtil {
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	// 초기모수 읽어오기
	public static List<IrParamAfnsCalc> getIrParamAfnsCalcInitList(String bssd, String irModelId, String irCurveId) {
		
		String query = " select a from IrParamAfnsCalc a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				
				 	 + "    and a.irModelId =:param2 "
				 	 + "    and a.irCurveId =:param3 "
				 	 + "    and a.lastModifiedBy like :param4 "
				 	 ;		
		
		return session.createQuery(query, IrParamAfnsCalc.class)
				      .setParameter("bssd", bssd)
         			  .setParameter("param2", irModelId)
         			  .setParameter("param3", irCurveId)
         			  .setParameter("param4", "%"+"710"+"%")

				      .getResultList();
	}	
	
	// 최적화 모수 읽어오기
	public static List<IrParamAfnsCalc> getIrParamAfnsCalcList(String bssd, String irModelId, String irCurveId) {
		
		String query = " select a from IrParamAfnsCalc a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				
				 	 + "    and a.irModelId =:param2 "
				 	 + "    and a.irCurveId =:param3 "
				 	 + "    and a.lastModifiedBy like :param4 "
				 	 ;		
		
		return session.createQuery(query, IrParamAfnsCalc.class)
				      .setParameter("bssd", bssd)
         			  .setParameter("param2", irModelId)
         			  .setParameter("param3", irCurveId)
         			  .setParameter("param4", "%"+"720"+"%")

				      .getResultList();
	}
}
