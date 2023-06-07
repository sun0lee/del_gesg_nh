package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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

@Entity
@Table(name ="E_STD_ASST")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class StdAsst implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -2698064271365825157L;

	@Id
	private String stdAsstCd;
	
	private String stdAsstNm;	
	private int seq;
	private String stdAsstTypCd;
	private String curCd;
	private String hisTable;	
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;
	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;	

}
