package cn.iocoder.yudao.module.ai.dal.dataobject.write;

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
    private Integer writeType;

    /**
     * 撰写内容提示
     */
    private String contentPrompt;

    /**
     * 生成的撰写内容
     */
    private String generatedContent;

    /**
     * 原文
     */
    private String originalContent;

    /**
     * 回复内容提示
     */
    private String replyContentPrompt;

    /**
     * 生成的回复内容
     */
    private String generatedReplyContent;

    /**
     * 长度提示词
     */
    private String length;

    /**
     * 格式提示词
     */
    private String format;

    /**
     * 语气提示词
     */
    private String tone;

    /**
     * 语言提示词
     */
    private String language;

    /**
     * 模型
     */
    private String model;

    /**
     * 平台
     */
    private String platform;

    /**
     * 错误信息
     */
    private String errorMessage;

}