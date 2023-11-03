package cn.iocoder.yudao.module.crm.framework.enums;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Crm 类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum CrmBizTypeEnum implements IntArrayValuable {

    CRM_LEADS(1, "线索"),
    CRM_CUSTOMER(2, "客户"),
    CRM_CONTACTS(3, "联系人"),
    CRM_BUSINESS(5, "商机"),
    CRM_CONTRACT(6, "合同");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmBizTypeEnum::getType).toArray();
    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static String getNameByType(Integer type) {
        // TODO @puhui999：可以 findone，更简洁；另外，不存在返回 null 即可啦；
        for (CrmBizTypeEnum crmBizTypeEnum : CrmBizTypeEnum.values()) {
            if (ObjUtil.equal(crmBizTypeEnum.type, type)) {
                return crmBizTypeEnum.name;
            }
        }
        return "";
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
