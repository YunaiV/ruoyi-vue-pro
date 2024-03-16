package cn.iocoder.yudao.framework.ai.chatxinghuo;

import cn.iocoder.yudao.framework.ai.chat.prompt.ChatOptions;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 讯飞星火
 *
 * author: fansili
 * time: 2024/3/16 20:29
 */
@Data
@Accessors(chain = true)
public class XingHuoOptions implements ChatOptions {

    /**
     * https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
     *
     * 指定访问的领域:
     * general指向V1.5版本;
     * generalv2指向V2版本;
     * generalv3指向V3版本;
     * generalv3.5指向V3.5版本;
     * 注意：不同的取值对应的url也不一样！
     */
    private XingHuoChatModel domain = XingHuoChatModel.XING_HUO_3_5;
    /**
     * 取值范围 (0，1] ，默认值0.5
     */
    private Float temperature;
    /**
     * V1.5取值为[1,4096]
     * V2.0、V3.0和V3.5取值为[1,8192]，默认为2048。
     */
    private Integer max_tokens;
    /**
     * 取值为[1，6],默认为4
     */
    private Integer top_k;
    /**
     * 	需要保障用户下的唯一性，用于关联用户会话
     */
    private String chat_id;


    @Override
    public Float getTemperature() {
        return null;
    }

    @Override
    public void setTemperature(Float temperature) {

    }

    @Override
    public Float getTopP() {
        return null;
    }

    @Override
    public void setTopP(Float topP) {

    }

    @Override
    public Integer getTopK() {
        return null;
    }

    @Override
    public void setTopK(Integer topK) {

    }
}
