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
@Table(name ="E_IR_PARAM_MODEL_RND")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamModelRnd implements Serializable, EntityIdentifier {
		
	private static final long serialVersionUID = -1452217231888509501L;

	@Id	
	private String baseYymm;
	
	@Id	
	private String irModelId;
	
	@Id	
	private String irCurveId;
	
	@Id	
	private Integer sceNo;
	
	@Id	
	private String matCd;
	
	private Double rndNum;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	

}



