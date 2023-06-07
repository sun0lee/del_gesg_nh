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
@Table(name ="E_IR_SPRD_AFNS_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class IrSprdAfnsUsr implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -8160719685730683413L;

	@Id
	private String baseYymm; 
	
	@Id
	private String irModelId;
	
	@Id
	private String irCurveId;	

	@Id
	private String matCd;	

	private Double meanSprd;
	private Double upSprd;
	private Double downSprd;
	private Double flatSprd;
	private Double steepSprd;		
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;
	
}
