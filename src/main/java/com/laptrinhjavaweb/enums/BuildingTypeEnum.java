package com.laptrinhjavaweb.enums;

public enum BuildingTypeEnum {
	TAN_TRET("Tầng trệt"),
    NGUYEN_CAN("Nguyên Căn"),
    APNIC("Nội thất");
 

    private String value;

    BuildingTypeEnum(String value) {
        this.value = value;
    }

	public String getValue() {
		return value;
	}
 
}
