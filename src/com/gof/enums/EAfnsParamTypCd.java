package com.gof.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum EAfnsParamTypCd {
	  
  // AFNS optParaNames 
    LAMBDA   (0,  "paras") 
  , THETA_1  (1,  "paras") 
  , THETA_2  (2,  "paras") 
  , THETA_3  (3,  "paras") 
  , KAPPA_1  (4,  "paras") 
  , KAPPA_2  (5,  "paras")
  , KAPPA_3  (6,  "paras") 
  , SIGMA_11 (7,  "paras") 
  , SIGMA_21 (8,  "paras") 
  , SIGMA_22 (9,  "paras") 
  , SIGMA_31 (10, "paras") 
  , SIGMA_32 (11, "paras") 
  , SIGMA_33 (12, "paras") 
  , EPSILON  (13, "paras") 
  
  // hetero todo 당장은 안씀 
//  , EPSILON1 (13, "paras",null,"Hetero")
//  , EPSILON2 (14, "paras",null,"Hetero")
//  , EPSILON3 (15, "paras",null,"Hetero")
//  , EPSILON4 (16, "paras",null,"Hetero")
//  , EPSILON5 (17, "paras",null,"Hetero")
//  , EPSILON6 (18, "paras",null,"Hetero")
//  , EPSILON7 (19, "paras",null,"Hetero")
//  , EPSILON8 (20, "paras",null,"Hetero")
//  , EPSILON9 (21, "paras",null,"Hetero")
//  , EPSILON10 (22, "paras",null,"Hetero")
//  , EPSILON11 (23, "paras",null,"Hetero")
//  , EPSILON12 (24, "paras",null,"Hetero")
//  , EPSILON13 (25, "paras",null,"Hetero")
//  , EPSILON14 (26, "paras",null,"Hetero")
  
    
  // AFNS optLSCNames
  , L0       (0, "LSC")
  , S0       (1, "LSC")
  , C0       (2, "LSC")

  
// AFNS optParaNames 초기모수 
  ,INIT_LAMBDA   (0,  "paras", LAMBDA) // EAfnsParamTypCd.LAMBDA.ordinal()
  ,INIT_THETA_1  (1,  "paras", THETA_1)
  ,INIT_THETA_2  (2,  "paras", THETA_2)
  ,INIT_THETA_3  (3,  "paras", THETA_3)
  ,INIT_KAPPA_1  (4,  "paras", KAPPA_1)
  ,INIT_KAPPA_2  (5,  "paras", KAPPA_2)
  ,INIT_KAPPA_3  (6,  "paras", KAPPA_3)
  ,INIT_SIGMA_11 (7,  "paras", SIGMA_11)
  ,INIT_SIGMA_21 (8,  "paras", SIGMA_21)
  ,INIT_SIGMA_22 (9,  "paras", SIGMA_22)
  ,INIT_SIGMA_31 (10, "paras", SIGMA_31)
  ,INIT_SIGMA_32 (11, "paras", SIGMA_32)
  ,INIT_SIGMA_33 (12, "paras", SIGMA_33)
  ,INIT_EPSILON  (13, "paras", EPSILON)
  
  ;

	
	private  int idx ;
 	private  String paramDv ;
 	private EAfnsParamTypCd paramTyp ; 
	
    EAfnsParamTypCd(int idx, String paramDv) {
        this(idx, paramDv, null);
    }

    EAfnsParamTypCd(int idx, String paramDv, EAfnsParamTypCd paramTyp) {
        this.idx = idx;
        this.paramDv = paramDv;
        this.paramTyp = paramTyp;
    }

    // hetero 
//	EAfnsParamTypCd(int idx, String paramDv, EAfnsParamTypCd paramTyp, String string2) {
//		// TODO Auto-generated constructor stub
//	}

	public static List<EAfnsParamTypCd> getAfnsParamList( String inParamDv) {
		List<EAfnsParamTypCd> paramList = new ArrayList<EAfnsParamTypCd>();
		paramList = Stream.of(EAfnsParamTypCd.values()).filter(s -> s.paramDv == inParamDv).collect(Collectors.toList());
		return paramList ;
	}
	
	// 초기모수 추출 
	public static List<EAfnsParamTypCd> getInitParamNames() {
	    List<EAfnsParamTypCd> initParams = new ArrayList<>();
	    for (EAfnsParamTypCd param : EAfnsParamTypCd.values()) {
	        if (param.name().startsWith("INIT_")) {
	            initParams.add(param);
	        }
	    }
	    return initParams;
	}
	
	// 최적모수 추출 -> init 이 아닌 파라메타 목록 paramDv 에 따라 paras / LSC 를 구분해서 받기 
	public static List<EAfnsParamTypCd> getOptParamNames(String paramDv) {
	    List<EAfnsParamTypCd> optParams = new ArrayList<>();
	    for (EAfnsParamTypCd param : EAfnsParamTypCd.values()) {
	        if (!param.name().startsWith("INIT_") && param.paramDv.equals(paramDv)) {
	            optParams.add(param);
	        }
	    }
	    return optParams;
	}

}
