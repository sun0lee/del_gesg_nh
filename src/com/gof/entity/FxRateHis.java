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
@Table(name ="E_FX_RATE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class FxRateHis implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 7749489060564117278L;
	
	@Id 
	private String baseDate;	
	
	@Id
	private String curCd;
	
	private Double bslBseRt;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	
	
}
