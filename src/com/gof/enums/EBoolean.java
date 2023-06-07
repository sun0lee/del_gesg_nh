package com.gof.enums;

public enum EBoolean {
	
	Y    (true)
,	YES  (true)
,   y    (true)
,   N    (false)	
,   NO   (false)
,   n    (false)
;
	
	private boolean trueFalse;
	
	private EBoolean(boolean trueFalse) {		
		this.trueFalse = trueFalse;
	}	
	
	public boolean isTrueFalse() {
		return trueFalse;
	}
		
}