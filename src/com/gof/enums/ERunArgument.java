package com.gof.enums;

public enum ERunArgument {
	  time ( "TIME")
//	  , t( "TIME")
	, job ( "JOB")
	, properties ( "PROPERTIES")
//	, p ("PROPERTIES")
	, encrypt("ENCRYPT")
	;
	
	
	private String alias;

	private ERunArgument(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	
}
