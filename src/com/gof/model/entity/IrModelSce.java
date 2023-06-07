package com.gof.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gof.entity.IrDcntSceStoBiz;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrModelSce implements Serializable {
	
	private static final long serialVersionUID = -5971256119173516419L;

	private String  baseDate;	
	private Integer sceNo;	
	private String  matCd;	
	private Integer monthSeq;	
	
	private Double  spotRateDisc;	
	private Double  spotRateCont;	
	private Double  fwdRateDisc;	
	private Double  fwdRateCont;	
	private Double  dcntFactor;	
	private double  theta;
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
	public IrDcntSceStoBiz convert(String applBizDv, String irModelId, String irCurveId, Integer irCurveSceNo, String jobId) {
		IrDcntSceStoBiz rst = new IrDcntSceStoBiz();
		
		rst.setBaseYymm(this.baseDate.substring(0,6));		
		rst.setApplBizDv(applBizDv);
		rst.setIrModelId(irModelId);
		rst.setIrCurveId(irCurveId);
		rst.setIrCurveSceNo(irCurveSceNo);
		rst.setSceNo(this.sceNo);
		rst.setMatCd(this.matCd);		
		rst.setSpotRate(this.spotRateDisc);
		rst.setFwdRate(this.fwdRateDisc);	
		rst.setLastModifiedBy(jobId);
		rst.setLastUpdateDate(LocalDateTime.now());

		return rst;
	}

}
