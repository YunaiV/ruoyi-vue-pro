package cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成 table 表定义
 *
 * @author 芋道源码
 */
@TableName(value = "tool_codegen_table", autoResultMap = true)
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ToolCodegenTableDO extends BaseDO {

    /**
     * ID 编号
     */
    private Long id;

    // ========== 表相关字段 ==========

    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表描述
     */
    private String tableComment;
    /**
     * 备注
     */
    private String remark;

    // ========== 类相关字段 ==========

    /**
     * 模块名，即一级目录
     *
     * 例如说，system、infra、tool 等等
     */
    private String moduleName;
    /**
     * 业务名，即二级目录
     *
     * 例如说，user、permission、dict 等等
     */
    private String businessName;
    /**
     * 业务包，自定义二级目录
     *
     * 例如说，我们希望将 dictType 和 dictData 归类成 dict 业务
     *
     * 如果不需要的情况下，businessName 和 businessPackage 是等价的
     */
    private String businessPackage;
    /**
     * 类名称（首字母大写）
     *
     * 例如说，SysUser、SysMenu、SysDictData 等等
     */
    private String className;
    /**
     * 类描述
     */
    private String classComment;
    /**
     * 作者
     */
    private String author;

    // ========== 生成相关字段 ==========

    /**
     * 模板类型
     *
     * 枚举 {@link ToolCodegenTemplateTypeEnum}
     */
    private Integer templateType;

    // ========== 菜单相关字段 ==========

}
