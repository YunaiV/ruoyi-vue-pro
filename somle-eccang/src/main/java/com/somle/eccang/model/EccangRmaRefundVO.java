package com.somle.eccang.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @className: RmaRefundVO
 * @author: Wqh
 * @date: 2024/12/19 16:39
 * @Version: 1.0
 * @description:
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangRmaRefundVO {
    // 退款状态0:存草稿1:待审核2:已审核3:已退款 4:退款失败5:作废
    private String exRmaStatus;

    // 退款方式1:系统2:标记退款
    private String exRmaRefundType;

    // 数据来源见表1
    private String exRmaDataSource;

    // 店铺账号
    private String[] exRmaUserAccount;

    // 仓库退件1:存在0:暂无
    private String roCodeExist;

    // 创建人
    private String rmaCreatorId;

    // 站点
    private String site;

    // 创建时间首
    private LocalDateTime createDateForm;

    // 创建时间尾
    private LocalDateTime createDateTo;

    // 审核时间首
    private LocalDateTime verifyDateForm;

    // 审核时间尾
    private LocalDateTime verifyDateTo;

    // 付款时间首
    private LocalDateTime paidDateForm;

    // 付款时间尾
    private LocalDateTime paidDateTo;

    // 退款时间首
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundDateForm;

    // 退款时间尾
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundDateTo;

    // 出库时间首
    private LocalDateTime dateWarehouseShippingFrom;

    // 出库时间尾
    private LocalDateTime dateWarehouseShippingTo;

    // 1:转换汇率0:不转换
    private String isConver;

    // 页
    private Integer page;

    // 每页数量(最大100)
    private Integer pageSize;
}
