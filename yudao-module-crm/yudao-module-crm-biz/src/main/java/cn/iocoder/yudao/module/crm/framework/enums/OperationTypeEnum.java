package cn.iocoder.yudao.module.crm.framework.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO @puhui999：是不是可以和 PermissionTypeEnum 合并，就是 CrmPermissionEnum，负责人、读取、读写；目前阶段，不用做的特别细致；类似 linux 的 acl；
/**
 * Crm 数据操作类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum OperationTypeEnum {

    // TODO @puhui999：抽象上，就分三种，会更合理。一个 OWNER 负责人，一个 READ 读，一个 WRITE 写；
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
