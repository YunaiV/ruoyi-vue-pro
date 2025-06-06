package com.somle.autonomous.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutonomousProductReq {

    private Integer page;
    private Integer limit;
}
