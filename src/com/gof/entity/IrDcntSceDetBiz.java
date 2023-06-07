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
@Table(name ="E_IR_DCNT_SCE_DET_BIZ")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrDcntSceDetBiz implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 3917757332051508436L;

	@Id
	private String baseYymm;

	@Id
	private String applBizDv;
	
    @Id
	private String irModelId;	
	
	@Id	
	private String irCurveId;

    @Id
	private Integer irCurveSceNo;

    @Id
	private String matCd;
	
	private Double spotRate;	
	private Double fwdRate;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;

}