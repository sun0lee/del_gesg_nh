package com.gof.dao;

import java.util.List;

import org.hibernate.Session;

import com.gof.entity.IrSprdAfnsBiz;
import com.gof.entity.IrSprdAfnsCalc;
import com.gof.entity.IrSprdAfnsUsr;
import com.gof.entity.IrSprdLp;
import com.gof.entity.IrSprdLpBiz;
import com.gof.entity.IrSprdLpUsr;
import com.gof.util.HibernateUtil;

public class IrSprdDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrSprdLpBiz> getIrSprdLpBizList(String bssd, String applBizDv, String irCurveId, Integer irCurveSceNo){
		
		String query = "select a from IrSprdLpBiz a           "
					 + " where 1=1                            "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.applBizDv    = :applBizDv    "
					 + "   and a.irCurveId    = :irCurveId    "
					 + "   and a.irCurveSceNo = :irCurveSceNo "
					 + "  order by a.matCd                    "
					 ;
		
		return session.createQuery(query, IrSprdLpBiz.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveId", irCurveId)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}	
	
	
	public static List<IrSprdLp> getIrSprdLpList(String bssd, String dcntApplModelCd, String applBizDv, String irCurveId, Integer irCurveSceNo) {
		
		String query = " select a from IrSprdLp a                  "
				 	 + "  where 1=1                                " 
				 	 + "    and baseYymm        = :bssd            "
				 	 + "    and dcntApplModelCd = :dcntApplModelCd "
					 + "    and a.applBizDv     = :applBizDv       "
					 + "    and a.irCurveId     = :irCurveId       "
					 + "    and a.irCurveSceNo  = :irCurveSceNo    "
					 + "  order by a.matCd                         "
					 ;				 	 
		
		return session.createQuery(query, IrSprdLp.class)
				      .setParameter("bssd", bssd)
				      .setParameter("dcntApplModelCd", dcntApplModelCd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveId", irCurveId)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)				      
				      .getResultList();
	}		
	
	
	public static List<IrSprdLpUsr> getIrSprdLpUsrList(String bssd, String applBizDv, String irCurveId, Integer irCurveSceNo){
		
		String query = "select a from IrSprdLpUsr a                        "
					 + " where 1=1                                         "
					 + "   and :bssd between a.applStYymm and a.applEdYymm "
					 + "   and a.applBizDv    = :applBizDv                 "
					 + "   and a.irCurveId    = :irCurveId                 "
					 + "   and a.irCurveSceNo = :irCurveSceNo              "
					 + " order by a.matCd                                  "
					 ;
		
		return session.createQuery(query, IrSprdLpUsr.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("applBizDv", applBizDv)
			      	  .setParameter("irCurveId", irCurveId)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}
	

	public static List<IrSprdAfnsBiz> getIrSprdAfnsBizList(String bssd, String irModelId, String irCurveId, Integer irCurveSceNo){
		
		String query = "select a from IrSprdAfnsBiz a         "
					 + " where 1=1                            "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.irModelId    = :irModelId    "
					 + "   and a.irCurveId    = :irCurveId    "
					 + "   and a.irCurveSceNo = :irCurveSceNo "
					 + " order by a.matCd                     "
					 ;
		
		return session.createQuery(query, IrSprdAfnsBiz.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)			      	  
			      	  .setParameter("irCurveSceNo", irCurveSceNo)
					  .getResultList();
	}	
	
	
	public static List<IrSprdAfnsCalc> getIrSprdAfnsCalcList(String bssd, String irModelId, String irCurveId){
		
		String query = "select a from IrSprdAfnsCalc a        "
					 + " where 1=1                            "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.irModelId    = :irModelId    "
					 + "   and a.irCurveId    = :irCurveId    "				
					 + " order by a.irCurveSceNo, a.matCd     "
					 ;
		
		return session.createQuery(query, IrSprdAfnsCalc.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)
					  .getResultList();
	}	
	
	
	public static List<IrSprdAfnsUsr> getIrSprdAfnsUsrList(String bssd, String irModelId, String irCurveId){
		
		String query = "select a from IrSprdAfnsUsr a "
					 + " where 1=1 "
					 + "   and a.baseYymm     = :bssd         "
					 + "   and a.irModelId    = :irModelId    "
					 + "   and a.irCurveId    = :irCurveId    "
					 + " order by a.matCd                     "
					 ;
		
		return session.createQuery(query, IrSprdAfnsUsr.class)
			      	  .setParameter("bssd", bssd)
			      	  .setParameter("irModelId", irModelId)
			      	  .setParameter("irCurveId", irCurveId)
					  .getResultList();
	}	
	
}
