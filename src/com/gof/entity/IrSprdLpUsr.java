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
@Table(name ="E_IR_SPRD_LP_USR")
@FilterDef(name="eqBaseYymm", parameters= @ParamDef(name ="bssd",  type="string"))
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrSprdLpUsr implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -5544681492762015145L;

	@Id
	private String applStYymm;

	@Id
	private String applEdYymm;
	
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
	

	public IrSprdLpBiz convert(String bssd) {
		
		IrSprdLpBiz lpBiz = new IrSprdLpBiz();
		
		lpBiz.setBaseYymm(bssd);
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
