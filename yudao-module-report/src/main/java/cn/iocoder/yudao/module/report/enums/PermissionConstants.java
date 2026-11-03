package cn.iocoder.yudao.module.report.enums;

/**
 * 报表模块的权限标识常量
 *
 * 只收录业务代码中动态校验的权限标识，与菜单配置的权限标识保持一致；接口上的 {@code @PreAuthorize} 仍直接书写权限标识
 *
 * @author 芋道源码
 */
public interface PermissionConstants {

    /**
     * UReport2 查看权限：查看报表列表，预览、打印、导出已保存的报表
     *
     * 对应 [报表管理 -> UReport2 报表] 菜单
     */
    String UREPORT2_QUERY = "report:ureport:query";
    /**
     * UReport2 设计权限：新建、设计、保存、删除报表，配置数据源；同时包含查看权限的全部功能
     *
     * 对应 [报表管理 -> UReport2 报表 -> 报表设计] 按钮
     */
    String UREPORT2_DESIGN = "report:ureport:design";

}
