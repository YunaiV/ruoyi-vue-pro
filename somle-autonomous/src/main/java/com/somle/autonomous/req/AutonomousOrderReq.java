package com.somle.autonomous.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutonomousOrderReq {

    @JsonProperty("sort_order")
    private Integer sortOrder;

    @JsonProperty("sorted_field")
    private String sortedField;

    @JsonProperty("date_from")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String dateFrom;

    @JsonProperty("date_to")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String dateTo;

    @JsonProperty("limit")
    private Integer limit;

    //订单状态集合
    @JsonProperty("statuses[]")
    private Integer statuses;
}
