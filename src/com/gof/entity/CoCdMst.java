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

@Entity
@Table(name ="E_CO_CD_MST")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CoCdMst implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 7049237275411088860L;

	@Id 
	private String grpCd;	
	private String grpNm;	
	
	@Id
	private String cd;	
	private String cdNm;	
	private Integer codeOrd;		
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
}
