package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_STD_ASST_IR_SCE_STO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class StdAsstIrSceSto implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4432625052946670655L;
	
	@Id	
	private String baseYymm;
    
	@Id	
	private String applBizDv;
    
	@Id 
	private String stdAsstCd;
	
	@Id
	private Integer sceTypCd;
    
	@Id	
	private Integer sceNo;
    
	@Id	
	private String matCd;

	private Double asstYield;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	
	
}


