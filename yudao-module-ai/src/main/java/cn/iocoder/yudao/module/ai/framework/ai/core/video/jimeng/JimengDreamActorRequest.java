package cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng;

import lombok.Data;

/**
 * 即梦动作模仿（DreamActor）的请求：人物图 + 动作模板视频
 *
 * 1. 动作模仿（jimeng_dream_actor_m1_gen_video_cv）：image_url、video_url，可选 callback_url
 * 2. 动作模仿 2.0（jimeng_dreamactor_m20_gen_video）：image_urls（1 张）、video_url，可选 cut_result_first_second_switch
 *
 * 输入视频不超过 30 秒；两者均为 CVSync2AsyncSubmitTask / CVSync2AsyncGetResult
 *
 * @author 芋道源码
 */
@Data
public class JimengDreamActorRequest {

    private String reqKey;
    /**
     * 人物图片地址（公网可访问）
     */
    private String imageUrl;
    /**
     * 动作模板视频地址（公网可访问）
     */
    private String videoUrl;
    /**
     * 回调地址：只用于 M1
     */
    private String callbackUrl;
    /**
     * 是否裁剪结果视频开头 1 秒的过渡：只用于 2.0，服务端默认 true
     */
    private Boolean cutResultFirstSecondSwitch;

}
