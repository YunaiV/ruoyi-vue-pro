package cn.iocoder.yudao.module.infra.service.codegen;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;

import java.util.List;
import java.util.Map;

/**
 * 代码生成 Service 接口
 *
 * @author 芋道源码
 */
public interface CodegenService {

    /**
     * 基于 SQL 建表语句，创建代码生成器的表定义
     *
     * @param userId 用户编号
     * @param sql SQL 建表语句
     * @return 创建的表定义的编号
     */
    Long createCodegenListFromSQL(Long userId, String sql);

    /**
     * 基于数据库的表结构，创建代码生成器的表定义
     *
     * @param userId 用户编号
     * @param tableName 表名称
     * @return 创建的表定义的编号
     */
    Long createCodegen(Long userId, String tableName);

    /**
     * 基于 {@link #createCodegen(Long, String)} 的批量创建
     *
     * @param userId 用户编号
     * @param tableNames 表名称数组
     * @return 创建的表定义的编号数组
     */
    List<Long> createCodegenListFromDB(Long userId, List<String> tableNames);

    /**
     * 更新数据库的表和字段定义
     *
     * @param updateReqVO 更新信息
     */
    void updateCodegen(CodegenUpdateReqVO updateReqVO);

    /**
     * 基于数据库的表结构，同步数据库的表和字段定义
     *
     * @param tableId 表编号
     */
    void syncCodegenFromDB(Long tableId);

    /**
     * 基于 SQL 建表语句，同步数据库的表和字段定义
     *
     * @param tableId 表编号
     * @param sql SQL 建表语句
     */
    void syncCodegenFromSQL(Long tableId, String sql);

    /**
     * 删除数据库的表和字段定义
     *
     * @param tableId 数据编号
     */
    void deleteCodegen(Long tableId);

    /**
     * 获得表定义分页
     *
     * @param pageReqVO 分页条件
     * @return 表定义分页
     */
    PageResult<CodegenTableDO> getCodegenTablePage(CodegenTablePageReqVO pageReqVO);

    /**
     * 获得表定义
     *
     * @param id 表编号
     * @return 表定义
     */
    CodegenTableDO getCodegenTablePage(Long id);

    /**
     * 获得全部表定义
     *
     * @return 表定义数组
     */
    List<CodegenTableDO> getCodeGenTableList();

    /**
     * 获得指定表的字段定义数组
     *
     * @param tableId 表编号
     * @return 字段定义数组
     */
    List<CodegenColumnDO> getCodegenColumnListByTableId(Long tableId);

    /**
     * 执行指定表的代码生成
     *
     * @param tableId 表编号
     * @return 生成结果。key 为文件路径，value 为对应的代码内容
     */
    Map<String, String> generationCodes(Long tableId);

    /**
     * 获得数据库自带的表定义列表
     *
     * @param tableName 表名称
     * @param tableComment 表描述
     * @return 表定义列表
     */
    List<DatabaseTableDO> getSchemaTableList(String tableName, String tableComment);

}
