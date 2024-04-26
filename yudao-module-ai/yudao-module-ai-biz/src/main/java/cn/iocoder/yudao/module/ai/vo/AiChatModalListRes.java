package cn.iocoder.yudao.module.ai.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * modal list
 *
 * @author fansili
 * @time 2024/4/24 19:56
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatModalListRes {
    /**
     * 编号
     */
    private Long id;
    /**
     * 模型名字
     */
    private String modelName;
    /**
     * 模型类型(qianwen、yiyan、xinghuo、openai)
     */
    private String modelType;
    /**
     * 模型照片
     */
    private String modalImage;
    /**
     * 模型配置JSON
     */
    private String modelConfig;
    /**
     * 禁用 0、正常 1、禁用
     */
    private Integer disable;

}
