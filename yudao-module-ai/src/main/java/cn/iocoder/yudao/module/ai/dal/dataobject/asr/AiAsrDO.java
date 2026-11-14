package cn.iocoder.yudao.module.ai.dal.dataobject.asr;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.asr.AiAsrStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 语音识别 DO
 *
 * 一次识别一行：成功、失败都记录；重新识别是新的一行，不修改已有的记录
 *
 * @author 芋道源码
 */
@TableName("ai_asr")
@KeySequence("ai_asr_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiAsrDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()} 字段
     */
    private Long modelId;
    /**
     * 模型名字，冗余 {@link AiModelDO#getName()}
     */
    private String modelName;
    /**
     * 平台，冗余 {@link AiModelDO#getPlatform()}
     */
    private String platform;
    /**
     * 模型标识，冗余 {@link AiModelDO#getModel()}
     */
    private String model;

    /**
     * 音频文件名
     */
    private String fileName;
    /**
     * 音频文件地址
     */
    private String audioUrl;

    /**
     * 识别状态
     * <p>
     * 枚举 {@link AiAsrStatusEnum}
     */
    private Integer status;
    /**
     * 识别出的原文；音频中没有语音时为空字符串
     */
    private String originalText;
    /**
     * 失败原因
     */
    private String errorMessage;
    /**
     * 识别耗时，单位：毫秒
     */
    private Integer duration;

}
