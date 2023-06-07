package com.gof.model.entity;

import java.io.Serializable;

import com.gof.entity.RcCorpTm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TransMat implements Serializable {	
	
	private static final long serialVersionUID = 4512622607494166728L;	
	
	private Integer fromGrdOrder;
	private Integer toGrdOrder;	
	private Double  transProb;	
	
	public static TransMat convertFrom(RcCorpTm rcCorpTm) {
		
		TransMat rst  = new TransMat();		
		rst.fromGrdOrder = rcCorpTm.getFromGradeEnum().getOrder();
		rst.toGrdOrder = rcCorpTm.getToGradeEnum().getOrder();
		rst.transProb = rcCorpTm.getTransProb();
		
		return rst;
	}
}
