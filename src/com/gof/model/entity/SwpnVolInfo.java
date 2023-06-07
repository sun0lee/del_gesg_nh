package com.gof.model.entity;

import java.io.Serializable;

import com.gof.entity.IrVolSwpn;

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
public class SwpnVolInfo implements Serializable {
	
	private static final long serialVersionUID = -6291459869154222962L;

	private String baseYymm;
	
	private Integer swpnMat;
	
	private Integer swapTenor;
	
	private Double vol;	

	
	public static SwpnVolInfo convertFrom(IrVolSwpn swapVol) {
		SwpnVolInfo rst  = new SwpnVolInfo();
		
		rst.baseYymm  = swapVol.getBaseYymm();
		rst.swpnMat   = Integer.valueOf(swapVol.getSwpnMatNum().intValue());
		rst.swapTenor = Integer.valueOf(swapVol.getSwapTenNum().intValue());
		rst.vol       = swapVol.getVol();
		
		return rst;
	}
	
}
