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

@Entity
@Table(name ="E_MV_CORR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MvCorr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -2507101178171773843L;
	
	@Id	
	private String baseDate;
	
	@Id	
	private String volCalcId;
	
	@Id	
	private String mvId;
	
	@Id	
	private String refMvId;	
	
	private Double mvHisCov;
	private Double mvHisCorr;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public MvCorr(String mvId, String refMvId, Double mvHisCov,Double mvHisCorr) {
		this.mvId = mvId;
		this.refMvId = refMvId;
		this.mvHisCov = mvHisCov;
		this.mvHisCorr = mvHisCorr;
	}
		
}
