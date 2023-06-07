package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
@Table(name ="E_IR_DCNT_RATE")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrDcntRate implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -4252300668894647002L;
	
	@Id	
	private String baseYymm;
	
	@Id	
	private String applBizDv;
	
	@Id	
	private String irCurveId;
	
	@Id	
	private Integer irCurveSceNo;

	@Id	
	private String matCd;
	
	private Double spotRate;
	private Double fwdRate;	
	private Double adjSpotRate;
	private Double adjFwdRate;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public double getSpread() {
		return adjSpotRate - spotRate;
	}
	
	public IrDcntRateBiz convertAdj() {
		
		IrDcntRateBiz adjDcnt = new IrDcntRateBiz();
		
		adjDcnt.setBaseYymm(this.baseYymm);		
		adjDcnt.setApplBizDv(this.applBizDv  + "_L");
		adjDcnt.setIrCurveId(this.irCurveId);
		adjDcnt.setIrCurveSceNo(this.irCurveSceNo);
		adjDcnt.setMatCd(this.matCd);			
		adjDcnt.setSpotRate(this.adjSpotRate);
		adjDcnt.setFwdRate(this.adjFwdRate);
		adjDcnt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjDcnt.setLastUpdateDate(LocalDateTime.now());
		
		return adjDcnt;
	}
	
	
	public IrDcntRateBiz convertAssetAdj(Map<String, Map<String, Double>> spreadMap) {
		
		Map<String, Double> spMap = spreadMap.getOrDefault(irCurveId, new HashMap<String, Double>());
		
		double spread = spMap.getOrDefault(matCd, 0.0);
				
		IrDcntRateBiz adjDcnt = new IrDcntRateBiz();
		
		adjDcnt.setBaseYymm(this.baseYymm);		
		adjDcnt.setApplBizDv(this.applBizDv  + "_A");
		adjDcnt.setIrCurveId(this.irCurveId);
		adjDcnt.setIrCurveSceNo(this.irCurveSceNo);
		adjDcnt.setMatCd(this.matCd);			
		adjDcnt.setSpotRate(this.adjSpotRate - spread);
		adjDcnt.setFwdRate(this.adjFwdRate - spread);
		adjDcnt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjDcnt.setLastUpdateDate(LocalDateTime.now());
		
		return adjDcnt;
	}
	
	public IrDcntRateBiz convertBase() {
		
		IrDcntRateBiz baseDcnt = new IrDcntRateBiz();
		
		baseDcnt.setBaseYymm(this.baseYymm);		
		baseDcnt.setApplBizDv(this.applBizDv + "_A");
		baseDcnt.setIrCurveId(this.irCurveId);
		baseDcnt.setIrCurveSceNo(this.irCurveSceNo);		
		baseDcnt.setMatCd(this.matCd);			
		baseDcnt.setSpotRate(this.spotRate);
		baseDcnt.setFwdRate(this.fwdRate);
		baseDcnt.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		baseDcnt.setLastUpdateDate(LocalDateTime.now());
		
		return baseDcnt;
	}	
	
	
	public IrDcntRate deepCopy(IrDcntRate org) {
		IrDcntRate copy = new IrDcntRate();
		
		copy.setBaseYymm(org.getBaseYymm());
		copy.setApplBizDv(org.getApplBizDv());
		copy.setIrCurveId(org.getIrCurveId());
		copy.setIrCurveSceNo(org.getIrCurveSceNo());
		copy.setMatCd(org.getMatCd());
		copy.setSpotRate(org.getSpotRate());
		copy.setFwdRate(org.getFwdRate());
		copy.setAdjSpotRate(org.getAdjSpotRate());
		copy.setAdjFwdRate(org.getAdjFwdRate());
		
		return copy;
	}	
	
	
	public IrCurveSpot convertAdjSpot() {
		
		IrCurveSpot adjSpot = new IrCurveSpot();
		
		adjSpot.setBaseDate(this.baseYymm);		
		adjSpot.setIrCurveId(this.irCurveId);		
		adjSpot.setMatCd(this.matCd);			
		adjSpot.setSpotRate(this.adjSpotRate);
		adjSpot.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjSpot.setLastUpdateDate(LocalDateTime.now());
		
		return adjSpot;
	}
	
	
	public IrCurveSpot convertBaseSpot() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseYymm);		
		spot.setIrCurveId(this.irCurveId);		
		spot.setMatCd(this.matCd);			
		spot.setSpotRate(this.spotRate);
		spot.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		spot.setLastUpdateDate(LocalDateTime.now());
		
		return spot;
	}		

}