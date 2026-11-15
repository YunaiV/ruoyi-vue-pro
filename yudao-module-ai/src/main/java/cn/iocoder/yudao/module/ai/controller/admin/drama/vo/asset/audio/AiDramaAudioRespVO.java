package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// DONE @AI：参考下 vm，检查下 ai 模块的类注释；（按 vm 模板，类说明只放在 @Schema，类注释只保留原作者署名；短剧、语音识别、文字识别、语音合成、视频、PPT 的 VO 已对齐；音乐有其它会话在改，未动）
/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧音频 Response VO")
@Data
public class AiDramaAudioRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "角色编号", example = "1")
    private Long characterId;

    // DONE @AI：inenum 把？不用写：“：1 角色样音 2 选用样音 3 旁白” swagger 了；其它 ai 模块类似的，都查下；（按 system 用户 VO 的写法改为“参见 XxxEnum 枚举类”；Response VO 不做校验，@InEnum 只加在 Request VO 上；短剧其它枚举字段、视频生成的类型与方式一并调整）
    @Schema(description = "类型，参见 AiDramaAudioTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp3")
    private String fileUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
