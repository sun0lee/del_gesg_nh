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
@Table(name ="E_IR_PARAM_SW_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrParamSwUsr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 4818870209511307188L;

	@Id
	private String applStYymm;
	private String applEdYymm;	

	@Id
	private String applBizDv;	

	@Id	
	private String irCurveId;
	
	@Id	
	private Integer irCurveSceNo;
	
	private String irCurveSceNm;	
	private String curCd;
	private Integer freq;
	private Integer llp;
	private Double ltfr;
	private Integer ltfrCp;
	private Double liqPrem;
	private String liqPremApplDv;
	private Integer shkSprdSceNo;
	private Double swAlphaYtm;
	private String stoSceGenYn;	
	private String fwdMatCd;	
	private Double multIntRate;
	private Double addSprd;	
	private String pvtRateMatCd;	
	private Double multPvtRate;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;		
	

	public IrParamSw convert(String bssd) {		
			
		IrParamSw paramSw = new IrParamSw();			
		
		paramSw.setBaseYymm(bssd);
		paramSw.setApplBizDv(this.applBizDv);
		paramSw.setIrCurveId(this.irCurveId);
		paramSw.setIrCurveSceNo(this.irCurveSceNo);
		paramSw.setIrCurveSceNm(this.irCurveSceNm);
		paramSw.setCurCd(this.curCd);
		paramSw.setFreq(this.freq);
		paramSw.setLlp(this.llp);
		paramSw.setLtfr(this.ltfr);
		paramSw.setLtfrCp(this.ltfrCp);
		paramSw.setLiqPrem(this.liqPrem);
		paramSw.setLiqPremApplDv(this.liqPremApplDv);
		paramSw.setShkSprdSceNo(this.shkSprdSceNo);
		paramSw.setSwAlphaYtm(this.swAlphaYtm);
		paramSw.setStoSceGenYn(this.stoSceGenYn);
		paramSw.setFwdMatCd(this.fwdMatCd);
		paramSw.setMultIntRate(this.multIntRate);		
		paramSw.setAddSprd(this.addSprd);
		paramSw.setPvtRateMatCd(this.pvtRateMatCd);
		paramSw.setMultPvtRate(this.multPvtRate);		
		paramSw.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		paramSw.setLastUpdateDate(LocalDateTime.now());		
		
		return paramSw;
	}	

}


