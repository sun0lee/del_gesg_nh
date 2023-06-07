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
@Table(name ="E_MV_VOL")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MvVol implements Serializable, EntityIdentifier {
		
	private static final long serialVersionUID = -564546080950002113L;

	@Id	
	private String baseDate;
	
	@Id	
	private String volCalcId;
	
	@Id	
	private String mvId;

	@Id
	private String mvTypCd;
	
	@Id
	private String curCd;	

	private Double mvHisVol;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public MvVol(String mvId, Double mvHisVol) {
		super();
		this.mvId = mvId;
		this.mvHisVol = mvHisVol;
	}	
	
}
