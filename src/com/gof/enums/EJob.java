package com.gof.enums;

public enum EJob {
	
	  ESG100 ("Pre-Process")
	, ESG110 ("Set Smith-Wilson Attribute")
	, ESG120 ("Set Swaption Volatility")	
	, ESG130 ("Set YTM TermStructure")
	
	, ESG150 ("YTM to SPOT by Smith-Wilson Method")
	, ESG151 ("YTM to SPOT by Smith-Wilson Method Migration")
	
	, ESG210 ("AFNS Weekly Input TermStructure Setup")
	, ESG220 ("AFNS Shock Spread")
	, ESG230 ("Biz Applied AFNS Shock Spread")
	
	, ESG240 ("Set Liquidity Premium")	
	, ESG250 ("Biz Applied Liquidity Premium")	
	
	, ESG260 ("BottomUp Risk Free TermStructure with Liquidity Premium")	
	, ESG261 ("BottomUp Risk Free TermStructure with Liquidity Premium")	
	, ESG270 ("Interpolated TermStructure by SW")
	, ESG271 ("Interpolated TermStructure by SW")
	, ESG280 ("Biz Applied TermStructure by SW")	
	
	, ESG310 ("Calibration and Validation of HW1F Model Parameter")
	, ESG320 ("Calibration Stress Test of HW1F Model Parameter")
	, ESG330 ("Biz Applied HW1F Model Parameter")		
	
	, ESG340 ("HW1F Discount Rate Scenario of Biz TermStructure")	
	, ESG350 ("HW1F Bond Yield Scenario of Biz TermStructure")
	, ESG360 ("Validation for Random number of HW1F Scenario")
	, ESG370 ("Validation for Market Consistency of HW1F Scenario")	
	
	, ESG410 ("Calibration of CIR Forcasting Model")
	, ESG420 ("Biz Applied CIR Forcasting Model Parameter")
	, ESG430 ("Stochastic Scenario of CIR Forcasting Model")	
	
	, ESG710 ("init param")
	, ESG720 ("opti param")
	, ESG730 ("DNS Shock(det.5) Spread")
	, ESG740 ("DNS Shock(sto.1000) Spread")
	
	, ESG760 ("BottomUp Risk Free TermStructure with Liquidity Premium")
	, ESG770 ("Interpolated TermStructure by SW")

	, ESG810 ("Set Transition Matrix")
	, ESG820 ("Corporate PD from Transition Matrix")
	;
	
	
	private String jobName;

	private EJob(String jobName) {
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}
	
}
