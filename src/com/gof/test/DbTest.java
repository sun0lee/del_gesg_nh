package com.gof.test;

import com.gof.dao.IrCurveDao;
import com.gof.entity.IrCurve;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbTest {
	public static void main(String[] args) {
		IrCurveDao.getIrCurveList().forEach(s-> log.info("aaaa : {}", s));
		
//		IrCurve aa = new IrCurve();
//		aa.setIrCurveId("aaa");
//		IrCurve bb;
//		
//		IrCurve cc = new IrCurve();
//		cc.setIrCurveId("aaa");
//		
//		bb = aa;
////		bb.setIrCurveId("bbb");
//		
//		if(aa == cc) {
//			log.info("aaaa : {}", aa);
//		}
//		else {
//			log.info("bbbb : {}", aa);
//		}
//		
//		if(aa.equals(cc)) {
//			log.info("aaaa : {}", aa);
//		}
//		else {
//			log.info("bbbb : {}", aa);
//		}
	}
}
