package cn.iocoder.yudao.module.ai1.service.provider.bo;

import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

// TODO @AI：项目里，一般是 bo 还是 dto 呢？
// TODO @AI：这个类名，感觉还是体现 model 还是啥会不会好点？就是按照项目的风格；（不要直接改，我们先讨论；）
/**
 * AI1 Provider 模型运行时快照
 *
 * 供 Agent 对话、知识库向量化构建 Spring AI 模型使用
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class Ai1ProviderRuntime {

    /**
     * Provider 编号
     */
    private Long providerId;
    /**
     * Provider 名称
     */
    private String providerName;
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
    /**
     * 接口地址
     */
    private String baseUrl;
    /**
     * API 密钥
     */
    private String apiKey;
    /**
     * 请求附属 Header，每项包含 key、value；value 可包含 {session} 占位符
     */
    private List<Map<String, String>> headers;

}
