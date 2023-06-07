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
@Table(name ="E_IR_QVAL_SCE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrQvalSce implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1660055914228437117L;

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
	private String qvalDv;
	
	@Id
	private Integer qvalSeq;	
	
	private Double qval1;
	private Double qval2;
	private Double qval3;
	private Double qval4;
	private Double qval5;	
	private Double qval6;
	private Double qval7;
	private Double qval8;
	private Double qval9;
	private Double qval10;	
	private Double qval11;
	private Double qval12;
	private Double qval13;
	private Double qval14;
	private Double qval15;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
}