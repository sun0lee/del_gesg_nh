package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrDcntSceStoBiz;
import com.gof.entity.IrParamHwRnd;
import com.gof.util.HibernateUtil;

public class IrParamHwRndDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	public static List<IrParamHwRnd> getIrParamHwRndList(String bssd, String irModelId, String irCurveId){
		
		String query = "select a from IrParamHwRnd a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd      "
					 + "   and a.irModelId    = :irModelId "
					 + "   and a.irCurveId    = :irCurveId "
//					 + "   and rownum <= 10                "
					 ;
		
		return session.createQuery(query, IrParamHwRnd.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)
					  .getResultList();
	}
	
	
	public static List<IrParamHwRnd> getIrParamHwRndList(String bssd, String irModelId, String irCurveId, String lastMatCd){
		
		String query = "select a from IrParamHwRnd a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd      "
					 + "   and a.irModelId    = :irModelId "
					 + "   and a.irCurveId    = :irCurveId "
					 + "   and a.matCd       <= :lastMatCd "
//					 + "   and rownum <= 10                "
					 ;
		
		return session.createQuery(query, IrParamHwRnd.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)
			      	  .setParameter("lastMatCd", lastMatCd)
					  .getResultList();
	}	
	
	
	public static List<IrDcntSceStoBiz> getIrDcntSceStoBizList(String bssd, String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo){
		
		String query = "select a from IrDcntSceStoBiz a "
					 + " where 1=1 "
					 + "   and a.baseYymm      = :bssd         "
					 + "   and a.applBizDv     = :applBizDv    "
					 + "   and a.irModelId     = :irModelId    "
					 + "   and a.irCurveId     = :irCurveId    "
					 + "   and a.irCurveSceNo <= :irCurveSceNo "
//					 + "   and rownum <= 10                    "
					 ;
		
		return session.createQuery(query, IrDcntSceStoBiz.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}	
	
}
