package com.gof.process;

import com.gof.enums.ENamingConvention;
import com.gof.model.IrModel;

public abstract class Process extends IrModel {
	
	public static final Integer ENTITY_LENGTH = 6;
	
	public static String toPhysicalName(String simpleName) {		
		return "E" + ENamingConvention.CAMEL_CASE.convertToScreamSnakeCase(simpleName);		
	}		
	
}
	
	
