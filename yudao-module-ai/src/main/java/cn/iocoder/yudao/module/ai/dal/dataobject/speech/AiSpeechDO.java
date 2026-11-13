package cn.iocoder.yudao.module.ai.dal.dataobject.speech;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.speech.AiSpeechStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 语音 DO
 *
 * @author 芋道源码
 */
@TableName("ai_speech")
@KeySequence("ai_speech_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiSpeechDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;

    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 平台：提交时的快照
     *
     * 枚举 {@link cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型标识：提交时的快照
     */
    private String model;
    /**
     * API 密钥编号：提交时绑定，异步任务后续按它查询
     *
     * 关联 {@link AiApiKeyDO#getId()}
     */
    private Long apiKeyId;

    /**
     * 朗读文本
     */
    private String inputText;
    /**
     * 音色
     */
    private String voice;
    /**
     * 音频格式
     *
     * 例如说：mp3、wav、flac、ogg_opus
     */
    private String format;
    /**
     * 语速倍数，最多两位小数；为空时为 1
     */
    private Double speed;

    /**
     * 状态
     *
     * 枚举 {@link AiSpeechStatusEnum}
     */
    private Integer status;
    /**
     * 服务商的任务编号：异步任务（腾讯云）可轮询的编号；同步合成为空
     */
    private String taskId;
    // TODO DONE @AI：“：文件服务的原始地址，访问时再生成签名地址”这个注释不需要把；其他也检查下；（已收敛为「音频地址」，与 AiMusicDO 一致；AiSpeechRespVO 的 @Schema、本地建表 SQL 的列注释同步收敛）
    /**
     * 音频地址
     */
    private String audioUrl;
    /**
     * 音频大小，单位：字节
     */
    private Integer audioSize;
    /**
     * 错误信息：已脱敏
     */
    private String errorMessage;
    /**
     * 完成时间：已完成、已失败时记录
     */
    private LocalDateTime finishTime;

}
