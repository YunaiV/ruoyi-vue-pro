package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kd100;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 快递 100 实时快递查询 Resp DTO
 *
 * 参见  <a href="https://api.kuaidi100.com/document/5f0ffb5ebc8da837cbd8aefc">快递 100 文档</a>
 *
 * @author jason
 */
@Data
public class Kd100ExpressQueryRespDTO {

    /**
     * 快递公司编码
     */
    @JsonProperty("com")
    private String expressCompanyCode;
    /**
     * 快递单号
     */
    @JsonProperty("nu")
    private String logisticsNo;
    /**
     * 快递单当前状态
     */
    private String state;

    /**
     * 查询结果
     *
     * 失败返回 "false"
     */
    private String result;
    /**
     * 查询结果失败时的错误信息
     */
    private String message;

    /**
     * 轨迹数组
     */
    @JsonProperty("data")
    private List<ExpressTrack> tracks;

    @Data
    public static class ExpressTrack {

        /**
         * 轨迹发生时间
         */
        @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
        private LocalDateTime time;

        /**
         * 轨迹描述
         */
        private String context;

    }

}
