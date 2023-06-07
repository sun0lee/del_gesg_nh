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

@Entity
@Table(name ="E_CO_ESG_META")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CoEsgMeta implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -474699114755027684L;
	
	@Id	
	private String groupId;
	
	@Id	
	private String paramKey;	
	private String paramValue;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	

	@Override
	public String toString() {
		return toString(",");
	}

	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		builder.append(groupId).append(delimeter)
				.append(paramKey).append(delimeter)
				.append(paramValue).append(delimeter)
				.append(useYn)
				;

		return builder.toString();
	}	
}


