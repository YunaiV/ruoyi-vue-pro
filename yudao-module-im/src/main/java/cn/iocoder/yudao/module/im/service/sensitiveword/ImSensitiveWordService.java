package cn.iocoder.yudao.module.im.service.sensitiveword;

/**
 * IM 敏感词 Service 接口
 *
 * @author 芋道源码
 */
public interface ImSensitiveWordService {

    /**
     * 校验文本是否包含敏感词
     *
     * @param text 待校验文本
     */
    void validateText(String text);

}
