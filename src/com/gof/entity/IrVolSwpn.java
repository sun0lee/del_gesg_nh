package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_IR_VOL_SWPN")
@FilterDef(name="eqBaseDate", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrVolSwpn implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4010423589739075634L;

	@Id
	private String baseYymm; 

	@Id
	private String irCurveId;
	
	@Id
	private Integer swpnMatNum;
	
	@Id
	private Integer swapTenNum;
	
	private Double vol;
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
}
