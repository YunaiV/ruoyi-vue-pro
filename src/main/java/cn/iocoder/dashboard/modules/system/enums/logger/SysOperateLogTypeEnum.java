package cn.iocoder.dashboard.modules.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作日志的操作类型
 *
 * @author ruoyi
 */
@Getter
@AllArgsConstructor
public enum SysOperateLogTypeEnum {

    /**
     * 新增
     */
    CREATE(1),
    /**
     * 修改
     */
    UPDATE(2),
    /**
     * 删除
     */
    DELETE(3),
    /**
     * 导出
     */
    EXPORT(4),
    /**
     * 导入
     */
    IMPORT(5),
    /**
     * 其它
     *
     * 在无法归类时，可以选择使用其它。因为还有操作名可以进一步标识
     */
    OTHER(0);

    /**
     * 类型
     */
    private final Integer type;

}
