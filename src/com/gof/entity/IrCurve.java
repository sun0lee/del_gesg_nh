package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.enums.EBoolean;
import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_IR_CURVE")
@NoArgsConstructor
@Getter
//@Setter
@EqualsAndHashCode
@ToString
public class IrCurve implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -7079607534247603390L;

	@Id
	private String irCurveId;	
	
	private String irCurveNm;	
	private String curCd;	
	private String applMethDv;
	private String crdGrdCd;	
	private String intpMethCd;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;

//	@Override
//	public boolean equals(Object obj) {
//		if(obj instanceof IrCurve) {
//			return this.irCurveId.equals(((IrCurve)obj).irCurveId);
//		}
//		return false;
//	}
	
	
}
