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
@Table(name ="E_IR_PARAM_HW_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamHwBiz implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1524655691890282755L;

	@Id
	private String baseYymm;
	
	@Id	
	private String applBizDv;
	
	@Id
	private String irModelId;

	@Id	
	private String irCurveId;

	@Id
	private String matCd;
	
	@Id
	private String paramTypCd;	
	
	private Double paramVal;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	

}


