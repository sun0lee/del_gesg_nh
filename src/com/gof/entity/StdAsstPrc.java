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
@Table(name ="E_STD_ASST_PRC")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class StdAsstPrc implements Serializable, EntityIdentifier { 
	
	private static final long serialVersionUID = 8822660082355727894L;
	
	@Id	
	private String baseDate;
	
	@Id	
	private String stdAsstCd;	

	private Double stdAsstPrice;	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;
	
}
