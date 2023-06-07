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
@Table(name ="E_CO_JOB_LIST")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CoJobList implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -8427725540244046466L;
	
	@Id 
	private String jobId;	
	private String jobNm;	
	private String useYn;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	

}