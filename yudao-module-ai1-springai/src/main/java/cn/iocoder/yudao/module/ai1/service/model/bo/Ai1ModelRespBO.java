package cn.iocoder.yudao.module.ai1.service.model.bo;

import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * AI1 模型调用参数
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class Ai1ModelRespBO {

    /**
     * 模型编号
     */
    private Long modelId;
    /**
     * 模型标识，例如说 gpt-4o
     */
    private String model;
    /**
     * 模型类型
     *
     * 枚举 {@link Ai1ModelTypeEnum}
     */
    private Integer modelType;

    // ==================== 供应商 ====================

    /**
     * 供应商编号
     */
    private Long providerId;
    /**
     * 接口地址
     */
    private String baseUrl;
    /**
     * API 密钥
     */
    private String apiKey;
    /**
     * 请求 Header，value 可包含 {session} 占位符
     */
    private Map<String, String> headers;

}
