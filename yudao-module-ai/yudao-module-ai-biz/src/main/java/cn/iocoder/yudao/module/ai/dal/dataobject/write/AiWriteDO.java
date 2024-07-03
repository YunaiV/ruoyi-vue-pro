package cn.iocoder.yudao.module.ai.dal.dataobject.write;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;

/**
 * AI 写作 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_write", autoResultMap = true)
@Data
public class AiWriteDO extends BaseDO {

    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 写作类型
     * <p>
     * 枚举 {@link AiWriteTypeEnum}
     */
    private Integer type;

    /**
     * 模型
     */
    private String model;

    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;

    /**
     * 生成内容提示
     */
    private String prompt;

    /**
     * 生成的内容
     */
    private String generatedContent;

    /**
     * 原文
     */
    private String originalContent;

    /**
     * 长度提示词
     */
    private Integer length;

    /**
     * 格式提示词
     */
    private Integer format;

    /**
     * 语气提示词
     */
    private Integer tone;

    /**
     * 语言提示词
     */
    private Integer language;

    /**
     * 错误信息
     */
    private String errorMessage;

}