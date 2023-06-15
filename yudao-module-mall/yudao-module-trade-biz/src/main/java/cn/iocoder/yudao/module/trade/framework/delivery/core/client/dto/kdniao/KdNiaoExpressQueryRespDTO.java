package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kdniao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 快递鸟快递查询 Resp DTO 参见  <a href="https://www.yuque.com/kdnjishuzhichi/dfcrg1/wugo6k">快递鸟接口文档</a>
 *
 * @author jason
 */
@Data
public class KdNiaoExpressQueryRespDTO {

    /**
     * 快递公司编码
     */
    @JsonProperty("ShipperCode")
    private String expressCompanyCode;

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

    @JsonProperty("EBusinessID")
    private String businessId;
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

    @JsonProperty("Traces")
    private List<ExpressTrack> tracks;

    @Data
    public static class ExpressTrack {
        /**
         * 轨迹发生时间
         */
        @JsonProperty("AcceptTime")
        private String time;
        /**
         * 轨迹描述
         */
        @JsonProperty("AcceptStation")
        private String state;
    }

//    {
//        "EBusinessID": "1237100",
//            "Traces": [],
//        "State": "0",
//            "ShipperCode": "STO",
//            "LogisticCode": "638650888018",
//            "Success": true,
//            "Reason": "暂无轨迹信息"
//    }
}
