package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
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
@Table(name ="E_IR_CURVE_YTM_USR_HIS")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrCurveYtmUsrHis implements Serializable, EntityIdentifier {	

	private static final long serialVersionUID = 6426252298359459167L;

	@Id
	private String baseDate;	

	@Id
	private String irCurveId;	
	
	@Column(name = "YTM_M0003") private Double ytmM0003;
	@Column(name = "YTM_M0006") private Double ytmM0006;
	@Column(name = "YTM_M0009") private Double ytmM0009;
	@Column(name = "YTM_M0012") private Double ytmM0012;
	@Column(name = "YTM_M0018") private Double ytmM0018;
	@Column(name = "YTM_M0024") private Double ytmM0024;
	@Column(name = "YTM_M0030") private Double ytmM0030;
	@Column(name = "YTM_M0036") private Double ytmM0036;
	@Column(name = "YTM_M0048") private Double ytmM0048;
	@Column(name = "YTM_M0060") private Double ytmM0060;
	@Column(name = "YTM_M0084") private Double ytmM0084;
	@Column(name = "YTM_M0120") private Double ytmM0120;
	@Column(name = "YTM_M0180") private Double ytmM0180;
	@Column(name = "YTM_M0240") private Double ytmM0240;
	@Column(name = "YTM_M0360") private Double ytmM0360;
	@Column(name = "YTM_M0600") private Double ytmM0600;
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
}