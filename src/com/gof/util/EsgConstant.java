package com.gof.util;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsgConstant {
	
	public static String TABLE_SCHEMA;
	public static String TABLE_PREFIX;
	
	private static Map<String, String> strConstant = new HashMap<String, String>();
	private static Map<String, Double> numConstant = new HashMap<String, Double>();
	
	public static Map<String, String> getStrConstant() {
		return strConstant;
	}

	public static Map<String, Double> getNumConstant() {
		return numConstant;
	}
	
	

}
