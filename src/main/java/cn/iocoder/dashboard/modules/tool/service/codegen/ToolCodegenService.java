package cn.iocoder.dashboard.modules.tool.service.codegen;

/**
 * 代码生成 Service 接口
 *
 * @author 芋道源码
 */
public interface ToolCodegenService {

    /**
     * 基于数据库的表结构，创建代码生成器的表定义
     *
     * @param tableName 表名称
     * @return 表定义的编号
     */
    Long createCodegenTable(String tableName);

}
