package cn.iocoder.yudao.module.ai.dal.dataobject.video;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoStatusEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 视频 DO
 *
 * 一行对应一次生成，视频生成、数字人口播、动作模仿三种类型共用
 *
 * @author 芋道源码
 */
@TableName(value = "ai_video", autoResultMap = true)
@KeySequence("ai_video_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiVideoDO extends BaseDO {

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
     * 创作类型
     * <p>
     * 枚举 {@link AiVideoTypeEnum}
     */
    private Integer type;
    /**
     * 生成方式：只有视频生成使用
     * <p>
     * 枚举 {@link AiVideoModeEnum}
     */
    private String mode;

    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型标识，冗余 {@link AiModelDO#getModel()}；即梦为 req_key
     */
    private String model;
    /**
     * API 密钥编号（提交时绑定）
     * <p>
     * 关联 {@link AiApiKeyDO#getId()}；查询按它访问服务商，不跟随模型之后的关联变化
     */
    private Long apiKeyId;

    /**
     * 提示词：数字人口播、动作模仿可为空
     */
    private String prompt;
    /**
     * 分辨率，例如 720p、1080P（大小写按模型）
     */
    private String resolution;
    /**
     * 画面比例，例如 16:9、9:16
     */
    private String ratio;
    /**
     * 时长（秒）：未指定时，完成后取服务商返回的时长
     */
    private Integer duration;
    /**
     * 其它生成参数快照：「做同款」带回表单
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Options options;
    /**
     * 输入文件：键为角色，值为文件服务的地址
     * <p>
     * 键为 {@link AiVideoInputRoleEnum#getRole()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, List<String>> inputUrls;

    /**
     * 状态
     * <p>
     * 枚举 {@link AiVideoStatusEnum}
     */
    private Integer status;
    /**
     * 进度（0-100）：服务商不返回进度，目前只在完成时置为 100
     */
    private Integer progress;
    /**
     * 服务商的任务编号
     */
    private String taskId;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 视频地址：转存到文件服务后的地址，不保存服务商的临时地址
     */
    private String videoUrl;
    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 是否公开
     */
    private Boolean publicStatus;

    /**
     * 其它生成参数：只有部分服务商支持，提交时按模型取用；不支持的参数由客户端校验报错
     */
    @Data
    public static class Options {

        /**
         * 尺寸，例如 1280*720：只用于通义万相的旧版文生模型
         */
        private String size;
        /**
         * 随机种子
         */
        private Long seed;
        /**
         * 是否添加水印
         */
        private Boolean watermark;
        /**
         * 是否生成音频
         */
        private Boolean generateAudio;
        /**
         * 是否固定镜头：豆包 Seedance
         */
        private Boolean cameraFixed;
        /**
         * 反向提示词：可灵、通义万相
         */
        private String negativePrompt;
        /**
         * 是否自动优化提示词：通义万相、MiniMax 海螺
         */
        private Boolean promptExtend;
        /**
         * 提示词扩写模式：MiniMax-H3，disabled、balanced、quality
         */
        private String promptExpansionMode;
        /**
         * 生成模式：可灵旧版接口，std、pro
         */
        private String quality;
        /**
         * 镜头类型：通义万相，single、multi
         */
        private String shotType;
        /**
         * 输出分辨率：即梦数字人，例如 720、1080
         */
        private Integer outputResolution;

    }

}
