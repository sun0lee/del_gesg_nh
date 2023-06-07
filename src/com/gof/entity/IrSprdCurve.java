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
@Table(name ="E_IR_SPRD_CURVE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrSprdCurve implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 8770367862233153559L;

	@Id	
	private String baseYymm; 
	
	@Id	
	private String irCurveId;
	
	@Id
	private String irTypDvCd;
	
	@Id
	private String matCd;	
	
	private Double intRate;	
	private Double crdSprd;	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
}
