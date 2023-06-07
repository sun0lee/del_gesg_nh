package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="E_IR_CURVE_SPOT")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class IrCurveSpot implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 8405894865559378104L;
	
	@Id	
	private String baseDate; 
	
	@Id	
	@Column(name ="IR_CURVE_ID")
	private String irCurveId;
	
	@Id
	private String matCd;	
	
	private Double spotRate;		
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
	@ManyToOne
	@JoinColumn(name ="IR_CURVE_ID", insertable=false, updatable= false)
	private IrCurve irCurve;	
	
	public IrCurveSpot(String baseDate, String irCurveId, String matCd, Integer sceNo, Double intRate) {
		this.baseDate = baseDate;
		this.irCurveId = irCurveId;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	public IrCurveSpot(String baseDate, String matCd, Double intRate) {
		this.baseDate = baseDate;
		this.matCd = matCd;
		this.spotRate = intRate;
	}
	
	public IrCurveSpot(String bssd, IrCurveSpot curveHis) {
		this.baseDate = curveHis.getBaseDate();
		this.irCurveId = curveHis.getIrCurveId();
		this.matCd = curveHis.getMatCd();
		this.spotRate = curveHis.getSpotRate();				
	}
	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseDate).append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(spotRate).append(delimeter)
			   ;

		return builder.toString();
	}
	
	public IrCurveSpot deepCopy(IrCurveSpot org) {
		IrCurveSpot copy = new IrCurveSpot();
		
		copy.setBaseDate(org.getBaseDate());
		copy.setIrCurveId(org.getIrCurveId());
		copy.setMatCd(org.getMatCd());
		copy.setSpotRate(org.getSpotRate());
		
		return copy;
//		return org;
	}
	

//******************************************************Biz Method**************************************
//	@Transient
	public int getMatNum() {
		return Integer.parseInt(matCd.substring(1));
	}
	public double getMatYear() {
		return Integer.parseInt(matCd.substring(1))/12.0;
	}
	
	public IrCurveSpot addForwardTerm(String bssd) {
		return new IrCurveSpot(bssd, this);
	}
	
	public String getBaseYymm() {
		return getBaseDate().substring(0,6);
	}
	public boolean isBaseTerm() {
		if(matCd.equals("M0003") 
				|| matCd.equals("M0006") 
				|| matCd.equals("M0009")
				|| matCd.equals("M0012")
				|| matCd.equals("M0024")
				|| matCd.equals("M0036")
				|| matCd.equals("M0060")
				|| matCd.equals("M0084")
				|| matCd.equals("M0120")
				|| matCd.equals("M0240")
				) {
			return true;
		}
		return false;	
			
	}
	
	
	public IrCurveYtm convertSimpleYtm() {
		
		IrCurveYtm ytm = new IrCurveYtm();
		
		ytm.setBaseDate(this.baseDate);		
		ytm.setIrCurveId(this.irCurveId);		
		ytm.setMatCd(this.matCd);
		ytm.setYtm(this.spotRate);		
		ytm.setLastModifiedBy("GESG_" + this.getClass().getSimpleName());
		ytm.setLastUpdateDate(LocalDateTime.now());
		
		return ytm;
	}		
	

	public IrCurveSpotWeek convertToWeek() {
		IrCurveSpotWeek rst = new IrCurveSpotWeek();
		
		String dayOfWeek = LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE).getDayOfWeek().name();
		rst.setBaseDate(this.baseDate);
		rst.setIrCurveId(this.irCurveId);
		rst.setMatCd(this.matCd);
		rst.setSpotRate(this.spotRate);
		rst.setDayOfWeek(dayOfWeek);
		rst.setBizDayType("Y");
//		rst.setIrCurve(this.irCurve);
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;		
	}		
	
	public IrCurveSpot addSpread(double discSpread) {
		IrCurveSpot copy = new IrCurveSpot();
		
		copy.setBaseDate(this.baseDate);
		copy.setIrCurveId(this.irCurveId);
		copy.setMatCd(this.matCd);
		copy.setSpotRate(Math.log(Math.exp(this.spotRate)+ discSpread));
		
		return copy;
	}
	
	 
	 
	public IrCurveSpot convertToCont() {
		IrCurveSpot copy = new  IrCurveSpot(baseDate, irCurveId, matCd,  1,  Math.log(1.0 + spotRate)); 
		return copy;
	}
	
	
	public IrCurveSpot convertToDisc() {
		IrCurveSpot copy = new  IrCurveSpot(baseDate, irCurveId, matCd,  1,   Math.exp(spotRate) - 1.0); 
		return copy;
	}
	
}
