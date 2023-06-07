package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_IR_DCNT_RATE_BU")
@NoArgsConstructor
@FilterDef(name="IR_FILTER", parameters= { @ParamDef(name="baseYymm", type="string"), @ParamDef(name="irCurveId", type="string") })
@Filters( { @Filter(name ="IR_FILTER", condition="BASE_YYMM = :baseYymm"),  @Filter(name ="IR_FILTER", condition="IR_CURVE_ID like :irCurveId") } )
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrDcntRateBu implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4644199390958760035L;

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
	
	private Double spotRateDisc;
	private Double spotRateCont;
	private Double liqPrem;
	private Double adjSpotRateDisc;
	private Double adjSpotRateCont;	
	private Double addSprd;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public IrCurveSpot convertAdj() {
		
		IrCurveSpot adjSpot = new IrCurveSpot();
		
		adjSpot.setBaseDate(this.baseYymm);		
		adjSpot.setIrCurveId(this.irCurveId);		
		adjSpot.setMatCd(this.matCd);			
		adjSpot.setSpotRate(this.adjSpotRateDisc);
		adjSpot.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		adjSpot.setLastUpdateDate(LocalDateTime.now());
		
		return adjSpot;
	}
	
	
	public IrCurveSpot convertBase() {
		
		IrCurveSpot spot = new IrCurveSpot();
		
		spot.setBaseDate(this.baseYymm);		
		spot.setIrCurveId(this.irCurveId);		
		spot.setMatCd(this.matCd);			
		spot.setSpotRate(this.spotRateDisc);
		spot.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		spot.setLastUpdateDate(LocalDateTime.now());
		
		return spot;
	}	

}