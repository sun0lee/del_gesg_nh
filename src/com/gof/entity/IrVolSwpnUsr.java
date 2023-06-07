package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
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
@Table(name ="E_IR_VOL_SWPN_USR")
@FilterDef(name="eqBaseDate", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrVolSwpnUsr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4010423589739075634L;

	@Id
	private String baseDate; 

	@Id
	private String irCurveId;
	
	@Id
	private String swpnMat;
	
	@Column(name = "VOL_SWPN_Y1") private Double volSwpnY1;
	@Column(name = "VOL_SWPN_Y2") private Double volSwpnY2;
	@Column(name = "VOL_SWPN_Y3") private Double volSwpnY3;
	@Column(name = "VOL_SWPN_Y4") private Double volSwpnY4;
	@Column(name = "VOL_SWPN_Y5") private Double volSwpnY5;
	@Column(name = "VOL_SWPN_Y7") private Double volSwpnY7;
	@Column(name = "VOL_SWPN_Y10") private Double volSwpnY10;
	@Column(name = "VOL_SWPN_Y12") private Double volSwpnY12;
	@Column(name = "VOL_SWPN_Y15") private Double volSwpnY15;
	@Column(name = "VOL_SWPN_Y20") private Double volSwpnY20;
	@Column(name = "VOL_SWPN_Y25") private Double volSwpnY25;
	@Column(name = "VOL_SWPN_Y30") private Double volSwpnY30;	
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
}
