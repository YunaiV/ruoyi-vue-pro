package com.somle.eccang.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangShipBatchReqVo {

    // 订单信息
    private String orderCode;        // 订单号
    private String tpCode;           // 下单单号
    private String receivingCode;    // 入库单号
    private String userId;           // 当前登录账号ID

    // 状态相关
    private Integer receivingStatus; // 收货状态: 0作废,1填满,2确认,3待审核,4审核,5未收货,6收货中,7已收货,99超期

    // 时间范围
    private LocalDate dateFor;          // 开始创建时间(格式: 2021-01-01)
    private LocalDate dateTo;           // 截止创建时间(格式: 2021-01-01)
    private LocalDateTime updateFor; // 开始更新时间
    private LocalDateTime updateTo;  // 截止更新时间

    // 分页参数
    private Integer page;       // 页码，默认1
    private Integer pageSize;  // 每页数量，默认50，最大100

    // 费用相关
    private Integer isFee;           // 是否返回费用：0不返回(默认)，1返回
}
