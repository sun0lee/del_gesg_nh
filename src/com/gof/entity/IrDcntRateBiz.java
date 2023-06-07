package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_IR_DCNT_RATE_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrDcntRateBiz implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 9213714569868056834L;
	
	@Id	
	private String baseYymm; 
	
	@Id	
	private String applBizDv; 
	
	@Id	
	private String irCurveId;
	
	@Id	
	private Integer irCurveSceNo;
	
	@Id	
	private String matCd;
	
	private Double spotRate;	
	private Double fwdRate;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	

	public int getMatNum() {
		return Integer.parseInt(matCd.substring(1));
	}
	
	public double getDf() {
		return Math.pow(1+spotRate, -1.0 * getMatNum()/12);
	}
	
	public double getContForwardRate() {
		return Math.log(1+fwdRate);
	}
	
}
