package cn.iocoder.yudao.module.fms.enums.report;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FMS 现金流量表跨报表取数参数类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsCashFlowParamTypeEnum {

    BALANCE("BA", "资产负债取数"),
    INCOME("IN", "利润表取数"),
    EXTEND("EX", "辅助数据取数"),
    LINE("L", "行次取数");

    /**
     * 参数前缀
     */
    private final String prefix;
    /**
     * 名称
     */
    private final String name;

    /**
     * 解析参数前缀对应的取数类型
     *
     * @param param 参数，例如 BA[1,2]、IN32、EX4、L7
     * @return 取数类型，无法识别时返回 null
     */
    public static FmsCashFlowParamTypeEnum of(String param) {
        for (FmsCashFlowParamTypeEnum type : values()) {
            if (StrUtil.startWith(param, type.getPrefix())) {
                return type;
            }
        }
        return null;
    }

}
