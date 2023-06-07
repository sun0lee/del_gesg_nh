package com.gof.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gof.entity.IrDcntRate;

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
public class SmithWilsonRslt implements Serializable {
	
	private static final long serialVersionUID = 3248965187822308189L;

	//@Id
	private String baseDate;
	
	private String resultType;
	
	private String scenType;
	
	private String matCd;
	
	private Double matTerm;
	
//	private LocalDate matDate;
	
	private Double dcntFactor;
	
	private Double spotCont;
	
	private Double spotDisc;
	
	private Double fwdCont;
	
	private Double fwdDisc;
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;
		
		
	public IrDcntRate convert() {

		IrDcntRate rslt = new IrDcntRate();		
				
		rslt.setMatCd(this.matCd);			
		rslt.setAdjSpotRate(this.spotDisc);
		rslt.setAdjFwdRate(this.fwdDisc);		
		rslt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		rslt.setLastUpdateDate(LocalDateTime.now());
		
		return rslt;		
	}
	
	
	
	public IrDcntRate convertForAsset() {

		IrDcntRate rslt = new IrDcntRate();		
				
		rslt.setMatCd(this.matCd);			
		rslt.setSpotRate(this.spotDisc);
		rslt.setFwdRate(this.fwdDisc);		
		rslt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		rslt.setLastUpdateDate(LocalDateTime.now());
		
		return rslt;		
	}
	
}

