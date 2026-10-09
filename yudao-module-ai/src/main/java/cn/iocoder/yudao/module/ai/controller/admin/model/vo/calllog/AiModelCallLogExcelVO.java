package cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 模型调用日志导出 Excel VO")
@Data
public class AiModelCallLogExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("平台")
    private String platform;

    @ExcelProperty("模型")
    private String model;

    @ExcelProperty("业务类型")
    private String bizType;

    @ExcelProperty("业务主键")
    private Long bizId;

    @ExcelProperty("请求时间")
    private LocalDateTime requestTime;

    @ExcelProperty("耗时(ms)")
    private Integer durationMs;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("输入Token")
    private Integer promptTokens;

    @ExcelProperty("输出Token")
    private Integer completionTokens;

    @ExcelProperty("总Token")
    private Integer totalTokens;

    @ExcelProperty("缓存命中Token")
    private Integer cachedTokens;

    @ExcelProperty("推理Token")
    private Integer reasoningTokens;

    @ExcelProperty("Token来源")
    private String tokenSource;

    @ExcelProperty("费用(微元)")
    private Long costAmount;

    @ExcelProperty("币种")
    private String currency;

    @ExcelProperty("输入单价(微元/百万)")
    private Long priceInPer1m;

    @ExcelProperty("缓存输入单价(微元/百万)")
    private Long priceCachedPer1m;

    @ExcelProperty("输出单价(微元/百万)")
    private Long priceOutPer1m;

    @ExcelProperty("推理输出单价(微元/百万)")
    private Long priceReasoningPer1m;

    @ExcelProperty("是否拦截")
    private Boolean blocked;

    @ExcelProperty("错误信息")
    private String errorMessage;

    @ExcelProperty("厂商请求编号")
    private String requestId;

}
