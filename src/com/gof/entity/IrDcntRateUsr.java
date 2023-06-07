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
@Table(name ="E_IR_DCNT_RATE_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrDcntRateUsr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4252300668894647002L;
	
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
	private Double adjSpotRate;
	private Double adjFwdRate;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public IrDcntRate convert() {		
		
		IrDcntRate dcnt = new IrDcntRate();			
		
		dcnt.setBaseYymm(this.baseYymm);
		dcnt.setApplBizDv(this.applBizDv);
		dcnt.setIrCurveId(this.irCurveId);
		dcnt.setIrCurveSceNo(this.irCurveSceNo);
		dcnt.setMatCd(this.matCd);
		dcnt.setSpotRate(this.spotRate);
		dcnt.setFwdRate(this.fwdRate);
		dcnt.setAdjSpotRate(this.adjSpotRate);
		dcnt.setAdjFwdRate(this.adjFwdRate);

		dcnt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		dcnt.setLastUpdateDate(LocalDateTime.now());		
		
		return dcnt;
	}	
		
}