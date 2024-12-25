package com.somle.autonomous.resp;

import lombok.Data;

@Data
public class AutonomousCommonResp<T> {
    private Integer status;
    private T data;
}
