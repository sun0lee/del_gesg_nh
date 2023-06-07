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
@Table(name ="E_IR_SPRD_LP")
@FilterDef(name="eqBaseYymm", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrSprdLp implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 1004575080756955856L;

	@Id	
	private String baseYymm; 
	
	@Id
	private String dcntApplModelCd;

	@Id
	private String applBizDv;
	
	@Id
	private String irCurveId;

	@Id
	private Integer irCurveSceNo;

	@Id
	private String matCd;	
	
	private Double liqPrem;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	

	public IrSprdLpBiz convert() {
		
		IrSprdLpBiz lpBiz = new IrSprdLpBiz();
		
		lpBiz.setBaseYymm(this.baseYymm);
		lpBiz.setApplBizDv(this.applBizDv);
		lpBiz.setIrCurveId(this.irCurveId);
		lpBiz.setIrCurveSceNo(this.irCurveSceNo);
		lpBiz.setMatCd(this.matCd);
		lpBiz.setLiqPrem(this.liqPrem);		
		lpBiz.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		lpBiz.setLastUpdateDate(LocalDateTime.now());
		
		return lpBiz;
	}	
	
}
