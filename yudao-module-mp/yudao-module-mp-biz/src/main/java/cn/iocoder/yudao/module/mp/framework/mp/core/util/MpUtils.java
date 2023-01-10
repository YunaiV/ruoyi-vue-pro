package cn.iocoder.yudao.module.mp.framework.mp.core.util;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;

import javax.validation.Validator;

/**
 * 公众号工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class MpUtils {

    /**
     * Text 类型的消息，参数校验 Group
     */
    public interface TextGroup {}

    /**
     * Image 类型的消息，参数校验 Group
     */
    public interface ImageGroup {}

    /**
     * Voice 类型的消息，参数校验 Group
     */
    public interface VoiceGroup {}

    /**
     * Video 类型的消息，参数校验 Group
     */
    public interface VideoGroup {}

    /**
     * News 类型的消息，参数校验 Group
     */
    public interface NewsGroup {}

    /**
     * Music 类型的消息，参数校验 Group
     */
    public interface MusicGroup {}

    /**
     * 校验消息的格式是否符合要求
     *
     * @param type 类型
     * @param message 消息
     */
    public static void validateMessage(Validator validator, String type, Object message) {
        // 获得对应的校验 group
        Class<?> group;
        switch (type) {
            case WxConsts.XmlMsgType.TEXT:
                group = TextGroup.class;
                break;
            case WxConsts.XmlMsgType.IMAGE:
                group = ImageGroup.class;
                break;
            case WxConsts.XmlMsgType.VOICE:
                group = VoiceGroup.class;
                break;
            case WxConsts.XmlMsgType.VIDEO:
                group = VideoGroup.class;
                break;
            case WxConsts.XmlMsgType.NEWS:
                group = NewsGroup.class;
                break;
            case WxConsts.XmlMsgType.MUSIC:
                group = MusicGroup.class;
                break;
            default:
                log.error("[validateMessage][未知的消息类型({})]", message);
                throw new IllegalArgumentException("不支持的消息类型：" + type);
        }
        // 执行校验
        ValidationUtils.validate(validator, message, group);
    }

}
