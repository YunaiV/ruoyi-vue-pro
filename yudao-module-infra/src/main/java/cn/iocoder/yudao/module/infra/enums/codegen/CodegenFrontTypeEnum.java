package cn.iocoder.yudao.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成的前端类型枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CodegenFrontTypeEnum {

    VUE2_ELEMENT_UI(10), // Vue2 Element UI 标准模版

    VUE3_ELEMENT_PLUS(20), // Vue3 Element Plus 标准模版

    VUE3_VBEN2_ANTD_SCHEMA(30), // Vue3 VBEN2 + ANTD + Schema 模版

    VUE3_VBEN5_ANTD_SCHEMA(40), // Vue3 VBEN5 + ANTD + schema 模版
    VUE3_VBEN5_ANTD_GENERAL(41), // Vue3 VBEN5 + ANTD 标准模版

    VUE3_VBEN5_EP_SCHEMA(50), // Vue3 VBEN5 + EP + schema 模版
    VUE3_VBEN5_EP_GENERAL(51), // Vue3 VBEN5 + EP 标准模版
    ;

    /**
     * 类型
     */
    private final Integer type;

}
