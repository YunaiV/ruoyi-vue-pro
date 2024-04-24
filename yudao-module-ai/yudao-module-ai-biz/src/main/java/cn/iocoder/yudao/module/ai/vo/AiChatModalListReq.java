package cn.iocoder.yudao.module.ai.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
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
public class AiChatModalListReq extends PageParam {

    @Schema(description = "名字搜搜")
    private String search;
}
