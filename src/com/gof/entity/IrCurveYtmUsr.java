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
@Table(name ="E_IR_CURVE_YTM_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class IrCurveYtmUsr implements Serializable, EntityIdentifier {	

	private static final long serialVersionUID = 8728364358808498458L;

	@Id
	private String baseDate;	

	@Id
	private String irCurveId;	
	
	@Id
	private String matCd;
	
	private Double ytm;	
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	
	
	public IrCurveSpot convertToHis() {
		
		IrCurveSpot rst = new IrCurveSpot();
		
		rst.setBaseDate(this.baseDate);
		rst.setIrCurveId(this.irCurveId);
		rst.setMatCd(this.matCd);
		rst.setSpotRate(this.ytm);
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;
	}
	
}