package cn.iocoder.yudao.module.trade.framework.delivery.core;

import lombok.Getter;

/**
 * 快递查询服务商枚举
 *
 * @author jason
 */
@Getter
public enum ExpressQueryProviderEnum {
    KD_NIAO("kd-niao", "快递鸟"),
    KD_100("kd-100", "快递100");
    /**
     * 快递服务商唯一编码
     */
    private final String code;

    /**
     * 快递服务商名称
     */
    private final String name;

    ExpressQueryProviderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
