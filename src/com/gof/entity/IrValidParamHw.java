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
@Table(name ="E_IR_VALID_PARAM_HW")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrValidParamHw implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -7621117603455851091L;

	@Id
	private String baseYymm;
	
    @Id
	private String irModelId;	
	
	@Id
	private String irCurveId;

	@Id
	private Double swpnMatNum;
	
	@Id
	private Double swapTenNum;
	
	@Id
	private String validDv;
	
	private Double validVal1;
	private Double validVal2;
	private Double validVal3;
	private Double validVal4;	
	private Double validVal5;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	
	
}