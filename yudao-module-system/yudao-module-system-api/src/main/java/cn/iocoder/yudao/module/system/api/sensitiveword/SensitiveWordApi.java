package cn.iocoder.yudao.module.system.api.sensitiveword;

import java.util.List;

/**
 * 敏感词 API 接口
 *
 * @author 永不言败
 */
public interface SensitiveWordApi {

    /**
     * 获得文本所包含的不合法的敏感词数组
     *
     * @param text 文本
     * @param tags 标签数组
     * @return 不合法的敏感词数组
     */
    List<String> validateText(String text, List<String> tags);

    /**
     * 判断文本是否包含敏感词
     *
     * @param text 文本
     * @param tags 表述数组
     * @return 是否包含
     */
    boolean isTextValid(String text, List<String> tags);

}
