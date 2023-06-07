package com.gof.process;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.gof.dao.IrCurveSpotDao;
import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveSpotWeek;
import com.gof.enums.EJob;
import com.gof.util.DateUtil;
import com.gof.util.FinUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Esg210_SpotWeek extends Process {		
	
	public static final Esg210_SpotWeek INSTANCE = new Esg210_SpotWeek();
	public static final String jobId = INSTANCE.getClass().getSimpleName().toUpperCase().substring(0, ENTITY_LENGTH);

	public static List<IrCurveSpotWeek> setupIrCurveSpotWeek(String bssd, String stBssd, String irCurveId, List<String> tenorList) {	
		
		List<IrCurveSpotWeek> rstList  = new ArrayList<IrCurveSpotWeek>();
		List<IrCurveSpot> curveHisList = IrCurveSpotDao.getIrCurveSpotListHis(bssd, stBssd, irCurveId, tenorList);
		
		if(curveHisList.size()==0) {
			log.warn("IR Curve History of {} Data is not found at from {} to {}", irCurveId, stBssd, bssd);
			return rstList;
		}	
		
		if(curveHisList.size() < 1000) {
			log.warn("Weekly SpotRate Data is not Enough [ID: {}, SIZE: {}] from {} to {}", irCurveId, curveHisList.size(), stBssd, FinUtils.toEndOfMonth(bssd));			
			return rstList;
		}			
		
		TreeSet<LocalDate> dateSet  =  new TreeSet<LocalDate>(curveHisList.stream().map(s-> LocalDate.parse(s.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE)).collect(Collectors.toSet()));
		TreeSet<LocalDate> emptySet =  new TreeSet<LocalDate>();
		
//		log.info("dateSet: {}", dateSet.size());
		
		LocalDate sttDate = dateSet.first();
		LocalDate endDate = DateUtil.stringToDate(DateUtil.toEndOfMonth(bssd));
		int dayDiff       = (int) ChronoUnit.DAYS.between(sttDate, endDate);		
		
		for(int i=0; i<=dayDiff; i++) {			
			LocalDate curDate = sttDate.plusDays(i);
			if(!dateSet.contains(curDate)) {
				dateSet.add(curDate);
				emptySet.add(curDate);
			}
		}		
//		log.info("dateSet: {}", dateSet.size());		
		
		rstList = curveHisList.stream().map(s-> s.convertToWeek()).collect(Collectors.toList());
		
		TreeMap<String, List<IrCurveSpotWeek>> weekHisMap = new TreeMap<String, List<IrCurveSpotWeek>>();		
		weekHisMap = rstList.stream().collect(Collectors.groupingBy(s -> s.getBaseDate(), TreeMap::new, Collectors.toList()));		
		
		List<IrCurveSpotWeek> curData = weekHisMap.firstEntry().getValue();
		
		for(LocalDate date : dateSet) {			
			String dateStr = DateUtil.dateToString(date);
			
//			List<IrCurveWeek> curData = new ArrayList<IrCurveWeek>();
			
			if(weekHisMap.containsKey(dateStr)) {				
				curData = weekHisMap.get(dateStr);			
//				log.info("ExistDate: {}, {}", date, curData);
			}
			else {
				List<IrCurveSpotWeek> cloneData = new ArrayList<IrCurveSpotWeek>();
				
				for(IrCurveSpotWeek data : curData) cloneData.add(new IrCurveSpotWeek(data));
//				cloneData.addAll(curData);    //shallow copy...
				
				cloneData.forEach(s -> s.setBaseDate(dateStr));
				cloneData.forEach(s -> s.setDayOfWeek(date.getDayOfWeek().name()));
				cloneData.forEach(s -> s.setBizDayType("N"));	
				
				weekHisMap.put(dateStr, cloneData);				
//				log.info("emptyDate: {}, {}", date, cloneData);
			}
		}		
		log.info("{}({}) creates {} results. They are inserted into [{}] Table", jobId, EJob.valueOf(jobId).getJobName(), weekHisMap.size() * weekHisMap.firstEntry().getValue().size(), toPhysicalName(IrCurveSpotWeek.class.getSimpleName()));		
		return weekHisMap.values().stream().flatMap(s -> s.stream()).collect(Collectors.toList());		
	}
	
	
	public static List<IrCurveSpotWeek> setupIrCurveSpotWeek(String bssd, String stBssd, String irCurveId, List<String> tenorList, DayOfWeek dayOfWeek) {		

		List<IrCurveSpot> curveHisList  = IrCurveSpotDao.getIrCurveSpotListHis(bssd, stBssd, irCurveId, tenorList);
		
		if(curveHisList.size()==0) {
			log.warn("IR Curve History of {} Data is not found at from {} to {}", irCurveId, stBssd, bssd);
			return null;
		}
		
		log.info("curveList: {}", curveHisList);
		
		WeekFields wf = WeekFields.of(Locale.KOREA);
		List<String> dayOfWeekSet = new ArrayList<String>();
		
		Set<LocalDate> dateSet = curveHisList.stream()
											.map(s-> LocalDate.parse(s.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE))
//											.sorted(Comparator.reverseOrder())
											.collect(Collectors.toSet());
		
		List<LocalDate> dateList = dateSet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
		
		boolean needToInsert = true;
		int currWeekOfYear  = 0;
		for( LocalDate aa : dateList) {
			log.info("date1 :  {},{}",aa.get(wf.weekOfWeekBasedYear()),  aa.format(DateTimeFormatter.BASIC_ISO_DATE));
			if(currWeekOfYear!= aa.get(wf.weekOfWeekBasedYear())) {
				needToInsert = true;
			}
			
			if(aa.getDayOfWeek().equals(dayOfWeek)) {
				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
				currWeekOfYear = aa.get(wf.weekOfWeekBasedYear());
				needToInsert= false;
			}
			else if( needToInsert && currWeekOfYear != aa.get(wf.weekOfYear())){
				dayOfWeekSet.add(aa.format(DateTimeFormatter.BASIC_ISO_DATE));
				currWeekOfYear = aa.get(wf.weekOfWeekBasedYear());
				needToInsert= false;
			}
			else {
				
			}
		}
		dayOfWeekSet.stream().sorted().forEach(s-> log.info("qqqq : {}", s));
		
		return curveHisList.stream().filter(s-> dayOfWeekSet.contains(s.getBaseDate()))
								    .map(s-> s.convertToWeek())
								    .collect(Collectors.toList());		
	}	
	
}

