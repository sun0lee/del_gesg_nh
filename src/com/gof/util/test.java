package com.gof.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {

	public static void main(String[] args) {
		System.out.println("zzzz");
		log.info("aaa: {}");
		log.info("aaaa : {}", String.format("%4s", "M0004".split("M")[1]));
		log.info("aaaa : {}", Double.parseDouble("M0004".split("M")[1]));
	}

}
