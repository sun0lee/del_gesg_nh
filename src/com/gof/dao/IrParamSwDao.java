package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.gof.entity.IrParamSw;
import com.gof.entity.IrParamSwUsr;
import com.gof.util.HibernateUtil;

public class IrParamSwDao extends DaoUtil {
	
	private static Session session = HibernateUtil.getSessionFactory().openSession();	
	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd) {
		
		String query = " select a from IrParamSwUsr a 						"
				 	 + "  where 1=1 										"
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm "
				 	 + "  order by a.irCurveId, a.irCurveSceNo 				"
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .getResultList();
	}	
	

	public static List<IrParamSw> getIrParamSwList(String bssd) {		
		return getIrParamSwUsrList(bssd).stream().map(s -> s.convert(bssd)).collect(Collectors.toList());
	}		

	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd, List<String> irCurveIdList) {
		
		String query = " select a from IrParamSwUsr a 						              "
				 	 + "  where 1=1 										              " 
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm               "
				 	 + "    and a.irCurveId in (:irCurveIdList) 			              "
				 	 + "  order by a.applStYymm, a.applBizDv, a.irCurveId, a.irCurveSceNo "
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .setParameterList("irCurveIdList", irCurveIdList)
				      .getResultList();
	}	
	
	
	//Currently this method is only used for job110	
	public static List<IrParamSw> getIrParamSwList(String bssd, List<String> irCurveIdList) {
		return getIrParamSwUsrList(bssd, irCurveIdList).stream().map(s -> s.convert(bssd)).collect(Collectors.toList());
	}

	
	public static List<IrParamSwUsr> getIrParamSwUsrList(String bssd, String applBizDv, String irCurveId, Integer irCurveSceNo) {
		
		String applStYymm = getAppliedYymm(bssd, applBizDv, irCurveId, irCurveSceNo);
		
		String query = " select a from IrParamSwUsr a 						"
				 	 + "  where 1=1 										" 
				 	 + "    and :bssd between a.applStYymm and a.applEdYymm "
				 	 + "    and a.applBizDv    = :applBizDv    				"
				 	 + "    and a.irCurveId    = :irCurveId    				"
				 	 + "    and a.irCurveSceNo = :irCurveSceNo 				"
				 	 + "    and a.applStYymm   = :applStYymm 				"
				 	 + "  order by a.irCurveId, a.irCurveSceNo 				"
				 	 ;		
		
		return session.createQuery(query, IrParamSwUsr.class)
				      .setParameter("bssd", bssd)
				      .setParameter("applBizDv", applBizDv)
				      .setParameter("irCurveId", irCurveId)
				      .setParameter("irCurveSceNo", irCurveSceNo)
				      .setParameter("applStYymm", applStYymm)
				      .getResultList();
	}		
	
	
	public static List<IrParamSw> getIrParamSwList(String bssd, String applBizDv, String irCurveId, Integer irCurveSceNo) {
		return getIrParamSwUsrList(bssd, applBizDv, irCurveId, irCurveSceNo).stream().map(s -> s.convert(bssd)).collect(Collectors.toList());		
	}


	private static String getAppliedYymm(String bssd, String applBizDv, String irCurveId, Integer irCurveSceNo) {		
		
		String query = " select max(a.applStYymm) from IrParamSwUsr a 		"
			     	 + "  where 1=1											"
			     	 + "    and :bssd between a.applStYymm and a.applEdYymm "
			     	 + "	and a.applBizDv    = :applBizDv 				"
			     	 + "	and a.irCurveId    = :irCurveId 				"
			     	 + "	and a.irCurveSceNo = :irCurveSceNo 				"
			     	 ;
	
		Object appYymm = session.createQuery(query)					
								.setParameter("bssd", bssd)
								.setParameter("applBizDv"   , applBizDv)
								.setParameter("irCurveId"   , irCurveId)
								.setParameter("irCurveSceNo", irCurveSceNo)
								.uniqueResult();
	
//		log.info("{}, {}, {}, {}, {}", bssd, applBizDv, irCurveId, irCurveSceNo, appYymm);
		if(appYymm == null) {
//			log.warn("Apply YYYYMM for IrParamSwUsr is not found [BIZ: {}, IR_CURVE_ID: {}, IR_CURVE_SCE_NO: {}] at {}", applBizDv, irCurveId, irCurveSceNo, bssd);
			return bssd;
		}		
		return appYymm.toString();		
	}

}
