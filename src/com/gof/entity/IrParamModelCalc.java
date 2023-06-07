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
@Table(name ="E_IR_PARAM_MODEL_CALC")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamModelCalc implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 3998120244491066027L;

	@Id
	private String baseYymm;

	@Id	
	private String irModelId;

	@Id	
	private String irCurveId;

	@Id
	private String paramTypCd;	
	
	private Double paramVal;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public IrParamModelBiz convert() {
		
		IrParamModelBiz paramBiz = new IrParamModelBiz();			
		
		paramBiz.setBaseYymm(this.baseYymm);
		paramBiz.setIrModelId(this.irModelId);
		paramBiz.setIrCurveId(this.irCurveId);
		paramBiz.setParamTypCd(this.paramTypCd);
		paramBiz.setParamVal(this.paramVal);
		paramBiz.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramBiz.setLastUpdateDate(LocalDateTime.now());		
		
		return paramBiz;
	}			

}


