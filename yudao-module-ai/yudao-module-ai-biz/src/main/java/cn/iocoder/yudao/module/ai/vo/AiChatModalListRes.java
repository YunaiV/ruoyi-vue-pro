package cn.iocoder.yudao.module.ai.vo;

import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatModalDO;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "id")
    private Long id;

    @Schema(description = "模型平台 参考 AiPlatformEnum")
    private String platform;

    @Schema(description = "模型类型 参考 YiYanChatModel、XingHuoChatModel")
    private String modal;

    @Schema(description = "模型名字")
    private String name;

    @Schema(description = "模型照片")
    private String image;

    @Schema(description = "禁用 0、正常 1、禁用")
    private Integer disable;

    @Schema(description = "modal 配置")
    private String config;
}
