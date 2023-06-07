package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
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
@Table(name ="E_RC_CORP_TM_USR")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RcCorpTmUsr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 1874314758316989856L;

	@Id
	private String baseYymm;
	
	@Id
	private String crdEvalAgncyCd;
	
	@Id
	private String fromCrdGrdCd;	

	@Column(name = "TRANS_PROB_1") private Double transProb1;
	@Column(name = "TRANS_PROB_2") private Double transProb2;
	@Column(name = "TRANS_PROB_3") private Double transProb3;
	@Column(name = "TRANS_PROB_4") private Double transProb4;
	@Column(name = "TRANS_PROB_5") private Double transProb5;
	@Column(name = "TRANS_PROB_6") private Double transProb6;
	@Column(name = "TRANS_PROB_7") private Double transProb7;

	private String lastModifiedBy;	
	private LocalDateTime lastUpdateDate;
	
}
