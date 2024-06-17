package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL) // TODO @xin：不用加这个哈
public class SunoReqVO {
    /**
     * 用于生成音乐音频的提示
     */
    private String prompt;
    // TODO @xin：Boolean，不使用基本类型。
    /**
     *  是否纯音乐
     */
    private boolean makeInstrumental;
    /**
     * //todo 首次请求返回的模型是对的，后续更新音频返回的模型又变成v3.5了
     * 模型版本  {@link cn.iocoder.yudao.module.ai.enums.AiModelEnum} Suno
     */
    private String mv;
}