package cn.iocoder.yudao.module.im.framework.sensitiveword;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IM 敏感词检测配置
 *
 * @author 芋道源码
 */
@Configuration
public class ImSensitiveWordConfiguration {

    // TODO @AI：可能需要按需创建；因为多租户。
    /**
     * 装配 {@link SensitiveWordBs}：
     * 词库来源 = {@link ImSensitiveWordDeny}（DB 里启用的敏感词），并开启常用规范化能力。
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs(ImSensitiveWordDeny imSensitiveWordDeny) {
        return SensitiveWordBs.newInstance()
                .wordDeny(imSensitiveWordDeny)
                .ignoreCase(true)
                .ignoreWidth(true)         // 忽略全/半角
                .ignoreNumStyle(true)      // 忽略数字风格（中文/阿拉伯）
                .ignoreChineseStyle(true)  // 忽略繁简体
                .enableWordCheck(true)
                .init();
    }

}
