package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrParamHwBiz;
import com.gof.entity.IrParamHwCalc;
import com.gof.entity.IrParamHwUsr;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

public class IrParamHwDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<IrParamHwUsr> getIrParamHwUsrList(String bssd) {
		
		String query = " select a from IrParamHwUsr a "
				 	 + "  where 1=1 "
				 	 + "    and a.baseYymm = :bssd    "				 
				 	 ;		
		
		return session.createQuery(query, IrParamHwUsr.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	
	
	public static List<IrParamHwUsr> getIrParamHwUsrList(String bssd, String applBizDv, String irModelId, String irCurveId) {
		
		String query = " select a from IrParamHwUsr a    "
				 	 + "  where 1=1                      " 
				 	 + "    and a.baseYymm  = :bssd      "
				 	 + "    and a.applBizDv = :applBizDv "
				 	 + "    and a.irModelId = :irModelId "
				 	 + "    and a.irCurveId = :irCurveId "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwUsr.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .setParameter("irModelId", irModelId)
				      .setParameter("irCurveId", irCurveId)
				      .getResultList();
	}		
	

	public static List<IrParamHwBiz> getIrParamHwBizFromUsrList(String bssd) {		
		return getIrParamHwUsrList(bssd).stream().map(s -> s.convert()).collect(Collectors.toList());
	}		

	
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd) {
		
		String query = " select a from IrParamHwCalc a "
				 	 + "  where 1=1                    "
				 	 + "    and a.baseYymm = :bssd     "				 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	
	
	//both HW1F_NSP and HW1F_SP(just Counting in JOB: Esg330)
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd, String irCurveId) {
		
		String query = " select a from IrParamHwCalc a      "
				 	 + "  where 1=1                         " 
				 	 + "    and a.baseYymm  = :bssd         "
				 	 + "    and a.irCurveId = :irCurveId    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)			
				      .setParameter("irCurveId", irCurveId)
				      .getResultList();
	}
	
	
	public static List<IrParamHwCalc> getIrParamHwCalcList(String bssd, String irModelId, String irCurveId) {
		
		String query = " select a from IrParamHwCalc a      "
				 	 + "  where 1=1                         " 
				 	 + "    and a.baseYymm  = :bssd         "
				 	 + "    and a.irModelId  = :irModelId   "
//				 	 + "    and a.irModelId like :irModelId "
				 	 + "    and a.irCurveId = :irCurveId    "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwCalc.class)
				      .setParameter("bssd", bssd)			
				      .setParameter("irModelId", irModelId)
//				      .setParameter("irModelId", "%"+irModelId+"%")
				      .setParameter("irCurveId", irCurveId)
				      .getResultList();
	}
	
	
	public static List<IrParamHwCalc> getIrParamHwCalcHisList(String bssd, String irModelId, String irCurveId, String paramTypCd, int monthNum, String matCd) {
		
		String query = "select a from IrParamHwCalc a      "
					 + " where 1=1                         "
					 + "   and a.baseYymm   > :stDate      "
					 + "   and a.baseYymm  <= :endDate     "
					 + "   and a.irModelId  = :irModelId   "
//					 + "   and a.irModelId like :irModelId "
					 + "   and a.irCurveId  = :irCurveId   "
					 + "   and a.paramTypCd = :paramTypCd  "					 
					 + "   and a.matCd      = :matCd       " 
					 ;
		
		return session.createQuery(query, IrParamHwCalc.class)
					  .setParameter("stDate", FinUtils.addMonth(bssd, monthNum))
					  .setParameter("endDate", bssd)
					  .setParameter("irModelId", irModelId)
//					  .setParameter("irModelId", "%"+irModelId+"%")
					  .setParameter("irCurveId", irCurveId)
					  .setParameter("paramTypCd", paramTypCd)
					  .setParameter("matCd", matCd)
					  .getResultList();
	}	
	
	
	public static List<IrParamHwBiz> getIrParamHwBizList(String bssd, String applBizDv, String irModelId, String irCurveId) {
		
		String query = " select a from IrParamHwBiz a    "
				 	 + "  where 1=1                      " 
				 	 + "    and a.baseYymm  = :bssd      "
				 	 + "    and a.applBizDv = :applBizDv "
				 	 + "    and a.irModelId = :irModelId "
				 	 + "    and a.irCurveId = :irCurveId "				 	 
				 	 ;		
		
		return session.createQuery(query, IrParamHwBiz.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .setParameter("irModelId", irModelId)
				      .setParameter("irCurveId", irCurveId)
				      .getResultList();
	}
	
}
