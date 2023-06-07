package com.gof.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import lombok.extern.slf4j.Slf4j;
@Slf4j

public class DateUtil {
	
	public static boolean isGreaterThan(String yyyymm, String otherYymm) {
		if(yyyymm.compareTo(otherYymm) >= 1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isGreaterOrEqual(String yyyymm, String otherYymm) {
		if(yyyymm.compareTo(otherYymm) >= 0	) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
    public static LocalDate stringToDate(String dateString) {
    	
		if(dateString != null && dateString.length() == 8) {
			
			int year  = Integer.parseInt(dateString.substring(0, 4));
			int month = Integer.parseInt(dateString.substring(4, 6));
			int day   = Integer.parseInt(dateString.substring(6, 8));			

			return LocalDate.of(year, month, day);    		
		}
		return null;    
    }
	
	
    public static String dateToString(LocalDate date) {    	
    	
    	if(date != null) {    		    		
    		return    String.format("%04d", date.getYear())
    				+ String.format("%02d", date.getMonthValue())
    				+ String.format("%02d", date.getDayOfMonth());
    	}    	
    	return null;    	
    }
	
	public static String toEndOfMonth(String baseDate) {
		
		if(baseDate.length()==4) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), 12, 1).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else if(baseDate.length()==6) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4)), Integer.valueOf(baseDate.substring(4,6)), 1).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else if(baseDate.length()==8) {
			return LocalDate.of(Integer.valueOf(baseDate.substring(0,4))
										, Integer.valueOf(baseDate.substring(4,6))
											, Integer.valueOf(baseDate.substring(6,8))).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		else {
			log.error("Convert Date Error : {} is not date format", baseDate);
		}
		return null;
	}

	public static LocalDate convertFrom(String yyyymmdd) {
		String bssd =  yyyymmdd.replaceAll("-", "").replaceAll("/", "");
		
		if(bssd.length()==4) {
			return  LocalDate.parse(bssd+"0101", DateTimeFormatter.BASIC_ISO_DATE);
		}
		else if(bssd.length()==6) {
			return  LocalDate.parse(bssd+"01", DateTimeFormatter.BASIC_ISO_DATE);
		}
		else if(bssd.length()==8) {
			return  LocalDate.parse(bssd, DateTimeFormatter.BASIC_ISO_DATE);
		}
		else {
			log.error("Date Convert Error : {} at DateUtil", yyyymmdd);
			System.exit(0);
		}
		return null;
	}
	
	public static int monthBetween(String from, String to) {		
//		return  Period.between(convertFrom(from), convertFrom(to)).getMonths();
		return Period.between(convertFrom(from), convertFrom(to)).getYears() * 12 + Period.between(convertFrom(from), convertFrom(to)).getMonths();
	}
	
	
	 public static LocalDate addMonth(String yyyymmdd, int addNum) {
		 return convertFrom(yyyymmdd).plusMonths(addNum); 
	 }
	 
	 public static String addMonthToString(String yyyymmdd, int addNum) {
		 if(yyyymmdd.length()==6) {
			 return addMonth(yyyymmdd, addNum).format(DateTimeFormatter.ofPattern("yyyyMM"));
		 }
		 if(yyyymmdd.length()==8) {
			 return addMonth(yyyymmdd, addNum).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		 }
		 else {
			 return addMonth(yyyymmdd, addNum).format(DateTimeFormatter.ofPattern("YYYYMMDD"));
			 
		 }
	 }
	 
	 
//	 public static Date convertDateFrom(String baseYymm) {
//		 return Date.valueOf(baseYymm);
//	 }
}
