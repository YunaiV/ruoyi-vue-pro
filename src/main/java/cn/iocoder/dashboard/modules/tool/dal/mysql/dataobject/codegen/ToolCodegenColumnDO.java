package cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成 column 字段定义
 *
 * @author 芋道源码
 */
@TableName(value = "tool_codegen_table_column", autoResultMap = true)
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ToolCodegenColumnDO extends BaseDO {



}
