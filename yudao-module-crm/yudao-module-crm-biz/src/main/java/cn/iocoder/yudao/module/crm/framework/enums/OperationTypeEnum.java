package cn.iocoder.yudao.module.crm.framework.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Crm 数据操作类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum OperationTypeEnum {

    DELETE(1, "删除"),
    UPDATE(2, "修改"),
    READ(3, "查询");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名称
     */
    private final String name;

    public static boolean isRead(Integer type) {
        return ObjUtil.equal(type, READ.getType());
    }

    public static boolean isEdit(Integer type) {
        return ObjUtil.equal(type, UPDATE.getType()) || ObjUtil.equal(type, DELETE.getType());
    }

}
