package com.somle.autonomous.resp;

import lombok.Data;

import java.util.List;

@Data
public class AutonomousOrderResp {
    List<Object> order_details;
    Integer total_records;
}
