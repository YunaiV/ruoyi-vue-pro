package com.somle.esb.enums.oms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ShopTypeEnum {
    ONLINE(0, "线上"),
    OFFLINE(1, "线下");

    private Integer type;
    private String name;
}
