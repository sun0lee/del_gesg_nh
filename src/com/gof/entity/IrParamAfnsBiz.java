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
@Table(name ="E_IR_PARAM_AFNS_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamAfnsBiz implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -6565657307170343742L;
	
	@Id
	private String baseYymm; 
	
	@Id
	private String irModelId;
	
	@Id
	private String irCurveId;
	
	@Id
	private String paramTypCd;
	
	private Double paramVal;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	

}
