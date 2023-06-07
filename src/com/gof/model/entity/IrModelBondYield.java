package com.gof.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gof.entity.StdAsstIrSceSto;

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
public class IrModelBondYield implements Serializable {
	
	private static final long serialVersionUID = 8283356676488673425L;

	private String  baseDate;	
	private String  irModelId;	
	private String  irCurveId;	
	private Integer sceNo;	
	private String  matCd;	
	private Integer monthSeq;
	
	private Double bondYieldCont;	
	private Double bondYieldDisc;
	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;
	
	public StdAsstIrSceSto convert(String applBizDv, String stdAsstCd, Integer sceTypCd, String jobId) {
		
		StdAsstIrSceSto rst = new StdAsstIrSceSto();		
		
		rst.setBaseYymm(this.baseDate.substring(0,6));
		rst.setApplBizDv(applBizDv);
		rst.setStdAsstCd(stdAsstCd);		
		rst.setSceTypCd(sceTypCd);
		rst.setSceNo(this.sceNo);
		rst.setMatCd(this.matCd);		
		rst.setAsstYield(this.bondYieldDisc);				
		rst.setLastModifiedBy(jobId);
		rst.setLastUpdateDate(LocalDateTime.now());

		return rst;
	}	

}
