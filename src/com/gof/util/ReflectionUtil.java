package com.gof.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gof.annotation.ToCsv;
import com.gof.enums.ENamingConvention;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionUtil {
	
	public static  List<String> getColumnList(Class klass,  String filteredValue){
		return	getColumnList(klass, "groupName", filteredValue);
	}
	public static  List<String> getColumnList( Class klass, String filterColumn, String filteredValue){
		return	ReflectionUtil.toCsvField(klass, filterColumn,filteredValue).entrySet().stream()
				.map(entry -> entry.getKey()).collect(Collectors.toList());
	}
	
	
//	********************************** toCSV with Reflection*******************************************
	public static  String toCsvHeader( Class klass,  String filteredValue){
		return toCsvHeader(klass, "groupName", filteredValue);
	}
	
	public static  String toCsvHeader( Class klass,  String filterColumn, String filteredValue){
		return toCsvField(klass, filterColumn, filteredValue).entrySet().stream()
				.map(entry ->ENamingConvention.CAMEL_CASE.convertToScreamSnakeCase(entry.getKey())).collect(Collectors.joining(","));
	}

	
//	********************************** toCSV with Reflection*******************************************
	public static <E extends Object> String toCsv( E obj,  String filteredValue){
		return toCsv(obj, "groupName", filteredValue);
	}
	
	public static <E extends Object> String toCsv( E obj, String filterColumn , String filteredValue){
		return toCsv(obj, toCsvField(obj.getClass(), filterColumn, filteredValue).keySet());
	}
		
	public static <E extends Object> String toCsv( E obj,  List<String> columnNames) {
		return toCsv(obj, new LinkedHashSet<>(columnNames));
	}
	
	public static <E extends Object> String toCsv( E obj,  Set<String> columnNames) {
		StringBuilder builder = new StringBuilder();
		for(String aa : columnNames){
			try {
				Method zz = obj.getClass().getDeclaredMethod("get"+ENamingConvention.CAMEL_CASE.convertToPascalCase(aa));
				Object returnObj = zz.invoke(obj);
				if(returnObj==null){
					builder.append(",").append("");
				}
				else{
					builder.append(",").append(returnObj.toString());
				}
			}
			catch (Exception e) {
				log.error("Reflection Error field{} of type {} : {}", aa, obj.getClass(), e);
			}
		}
		return builder.toString().replaceFirst(",", "");
	}
	
//	*****************************************Base Method**************************************
	public static Map<String, ToCsv> toCsvField(Class klass){
		Map<String, ToCsv> rst = new LinkedHashMap<>();
		for(Field aa : klass.getDeclaredFields()){
			ToCsv anno = aa.getAnnotation(ToCsv.class);
			if(anno!=null){
				rst.put(aa.getName(), anno);
			}
		}
		return rst;
	}
	
	public static Map<String, ToCsv> toCsvField(Class klass, String filterColumn , String filteredValue){
		Map<String, ToCsv> rst = new LinkedHashMap<>();
		for(Field aa : klass.getDeclaredFields()){
			ToCsv anno = aa.getAnnotation(ToCsv.class); 
			if(anno!=null){
				try {
					Method zz = ToCsv.class.getDeclaredMethod(filterColumn);
					Object returnObj = zz.invoke(anno);
					for (int i = 0; i < Array.getLength(returnObj); i ++) {
						Object arrayElement = Array.get(returnObj, i);
						if(filteredValue.equals((String)arrayElement)){
							rst.put(aa.getName(), anno);
							break;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return rst;
	}
}
