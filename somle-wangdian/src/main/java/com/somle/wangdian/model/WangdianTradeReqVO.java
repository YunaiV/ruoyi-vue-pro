package com.somle.wangdian.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WangdianTradeReqVO {

    /**
     * 订单状态
     * - 如果不传该字段，则查询所有订单
     * - 可选值:
     *   5: 已取消
     *   10: 待付款
     *   12: 待尾款
     *   13: 待选仓
     *   15: 等未付
     *   16: 延时审核
     *   19: 预订单前处理
     *   20: 前处理(赠品，合并，拆分)
     *   21: 委外前处理
     *   22: 抢单前处理
     *   25: 预订单
     *   27: 待抢单
     *   30: 待客审
     *   35: 待财审
     *   40: 待递交仓库
     *   45: 递交仓库中
     *   50: 已递交仓库
     *   53: 未确认
     *   55: 已确认(已审核)
     *   95: 已发货
     *   105: 部分打款
     *   110: 已完成
     *   113: 异常发货
     */
    private Integer status;

    /**
     * 开始时间 (按最后修改时间增量获取数据)
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间 (按最后修改时间增量获取数据)
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 分页大小 (每页返回的数据条数，输入值范围1~100，默认40)
     */
    private Integer pageSize;

    /**
     * 页号 (默认从0页开始)
     */
    private Integer pageNo;

    /**
     * 原始单号 (如果使用原始单号，其余参数不起效)
     */
    private String srcTid;

    /**
     * 订单编号 (如果使用订单编号，其余参数不起效)
     */
    private String tradeNo;

    /**
     * 店铺编号 (代表店铺唯一编码，用于获取指定店铺单据数据信息)
     */
    private String shopNo;

    /**
     * 仓库编号 (代表仓库唯一编码，用于获取指定仓库单据数据信息, 不支持一次推送多个仓库编码)
     */
    private String warehouseNo;

    /**
     * 使用税率 (0: 使用订单中的税率, 1: 使用单品中的税率, 默认0)
     */
    private Integer goodstax;

    /**
     * 物流单号限制
     * - 0: 没有任何限制 (默认值)
     * - 1: 物流单号不为空才返回
     * - 2: 只返回物流单号为空的
     */
    private Integer hasLogisticsNo;

    /**
     * 是否模糊查询 (仅在原始单号srcTid查询时生效, 0: 精确, 1: 模糊, 默认为0)
     */
    private Integer isFuzzy;

    /**
     * 是否返回交易流水号、付款状态、付款时间 (1: 返回, 0: 不返回, 默认0)
     */
    private Integer src;

    /**
     * 物流单号 (如果使用物流单号，其余参数不起效)
     */
    private String logisticsNo;

    /**
     * 店铺编号（批量） (多个店铺编号用英文逗号隔开，最多指定20个店铺)
     */
    private String shopNos;
}
