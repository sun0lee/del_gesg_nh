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
@Table(name ="E_IR_PARAM_HW_USR")
@FilterDef(name="paramApplyEqBaseYymm", parameters= { @ParamDef(name="baseYymm", type="string") })
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamHwUsr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1524655691890282755L;

	@Id			
	private String baseYymm;	
	
	@Id		
	private String applBizDv;
	
	@Id
	private String irModelId;
	
	@Id	
	private String irCurveId;
	
	@Id
	private String matCd;

	@Id
	private String paramTypCd;	
	
	private Double paramVal;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public IrParamHwBiz convert() {		
		
		IrParamHwBiz paramHwBiz = new IrParamHwBiz();			
		
		paramHwBiz.setBaseYymm(this.baseYymm);
		paramHwBiz.setApplBizDv(this.applBizDv);
		paramHwBiz.setIrModelId(this.irModelId);
		paramHwBiz.setIrCurveId(this.irCurveId);
		paramHwBiz.setMatCd(this.matCd);
		paramHwBiz.setParamTypCd(this.paramTypCd);
		paramHwBiz.setParamVal(this.paramVal);
		paramHwBiz.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramHwBiz.setLastUpdateDate(LocalDateTime.now());		
		
		return paramHwBiz;
	}	
		
}


