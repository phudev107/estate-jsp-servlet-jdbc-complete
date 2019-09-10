package com.laptrinhjavaweb.entity;

import com.laptrinhjavaweb.annotation.Column;
import com.laptrinhjavaweb.annotation.Entity;
import com.laptrinhjavaweb.annotation.Table;

@Entity
@Table(name="rentarea")
public class RentArea extends BaseEntity {
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "buidingid")
	private Long buidingId;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getBuidingId() {
		return buidingId;
	}

	public void setBuidingId(Long buidingId) {
		this.buidingId = buidingId;
	}
	
}
