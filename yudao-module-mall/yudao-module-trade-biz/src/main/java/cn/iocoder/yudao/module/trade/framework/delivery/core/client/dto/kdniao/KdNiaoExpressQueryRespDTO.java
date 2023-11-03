package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kdniao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 快递鸟快递查询 Resp DTO
 *
 * 参见 <a href="https://www.yuque.com/kdnjishuzhichi/dfcrg1/wugo6k">快递鸟接口文档</a>
 *
 * @author jason
 */
@Data
public class KdNiaoExpressQueryRespDTO {

    /**
     * 快递公司编码
     */
    @JsonProperty("ShipperCode")
    private String shipperCode;

    /**
     * 快递单号
     */
    @JsonProperty("LogisticCode")
    private String logisticsNo;

    /**
     * 订单编号
     */
    @JsonProperty("OrderCode")
    private String orderNo;

    /**
     * 用户 ID
     */
    @JsonProperty("EBusinessID")
    private String businessId;

    /**
     * 普通物流状态
     *
     * 0 - 暂无轨迹信息
     * 1 - 已揽收
     * 2 - 在途中
     * 3 - 签收
     * 4 - 问题件
     * 5 - 转寄
     * 6 - 清关
     */
    @JsonProperty("State")
    private String state;

    /**
     * 成功与否
     */
    @JsonProperty("Success")
    private Boolean success;
    /**
     * 失败原因
     */
    @JsonProperty("Reason")
    private String reason;

    /**
     * 轨迹数组
     */
    @JsonProperty("Traces")
    private List<ExpressTrack> tracks;

    @Data
    public static class ExpressTrack {

        /**
         * 发生时间
         */
        @JsonProperty("AcceptTime")
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime acceptTime;

        /**
         * 轨迹描述
         */
        @JsonProperty("AcceptStation")
        private String acceptStation;

    }

}
