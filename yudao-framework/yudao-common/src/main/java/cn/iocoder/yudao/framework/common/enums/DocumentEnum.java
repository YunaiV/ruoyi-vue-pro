package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档地址
 *
 * @author deepay
 */
@Getter
@AllArgsConstructor
public enum DocumentEnum {

    REDIS_INSTALL("https://gitee.com/zhijiantianya/sdsdsdas/issues/I4VCSJ", "Redis 安装文档"),
    TENANT("https://admin.deepay.srl", "SaaS 多租户文档");

    private final String url;
    private final String memo;

}
