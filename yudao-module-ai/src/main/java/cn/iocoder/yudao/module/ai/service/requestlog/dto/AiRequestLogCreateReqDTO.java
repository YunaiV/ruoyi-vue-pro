package cn.iocoder.yudao.module.ai.service.requestlog.dto;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 请求日志创建 Request DTO
 *
 * 一次业务生成尝试的快照：由 {@link cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService#buildCreateReqDTO} 构建，
 * 调用过程中更新用量与元数据，结束时交给 createRequestLog 持久化
 *
 * @author 芋道源码
 */
@Data
public class AiRequestLogCreateReqDTO {

    /**
     * 请求编号
     *
     * 服务端在调用模型前生成的本地 UUID
     */
    private String requestId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 租户编号
     *
     * 请求开始时捕获；Flux 异步回调中没有租户上下文，插入时按该租户执行
     */
    private Long tenantId;
    /**
     * 业务类型
     *
     * 枚举 {@link AiRequestLogBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 业务编号
     */
    private Long bizId;

    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型编号
     */
    private Long modelId;
    /**
     * 模型标志
     */
    private String model;

    /**
     * 请求状态
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 开始时间
     */
    private LocalDateTime beginTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 输入 Token 数
     */
    private Long inputTokens;
    /**
     * 输出 Token 数
     */
    private Long outputTokens;
    /**
     * 响应元数据
     */
    private Map<String, Object> responseMetadata;

    /**
     * 获得合计 Token 数
     *
     * @return 输入 + 输出；任一未知时为 null
     */
    public Long getTotalTokens() {
        return inputTokens != null && outputTokens != null ? inputTokens + outputTokens : null;
    }

}
