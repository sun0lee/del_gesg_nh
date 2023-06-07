package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.interfaces.EntityIdentifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="E_CO_JOB_INFO")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CoJobInfo implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 7781683831757307417L;
	
	@Id 
	private String jobId;	
	private String jobNm;
	
	@Id
	private String baseYymm;
	
	@Id
	private String calcDate;
	
	@Id	
	private String calcStart;
	
	@Transient
	private LocalDateTime jobStart;
	
	private String calcEnd;
	private String calcElps;
	private String calcScd;	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;	
	
}
