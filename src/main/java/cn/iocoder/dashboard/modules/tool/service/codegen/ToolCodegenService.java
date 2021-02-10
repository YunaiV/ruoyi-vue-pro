package cn.iocoder.dashboard.modules.tool.service.codegen;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;

import java.util.List;
import java.util.Map;

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
    Long createCodegen(String tableName);

    /**
     * 更新数据库的表和字段定义
     *
     * @param updateReqVO 更新信息
     */
    void updateCodegen(ToolCodegenUpdateReqVO updateReqVO);

    /**
     * 获得表定义分页
     *
     * @param pageReqVO 分页条件
     * @return 表定义分页
     */
    PageResult<ToolCodegenTableDO> getCodeGenTablePage(ToolCodegenTablePageReqVO pageReqVO);

    /**
     * 获得表定义
     *
     * @param id 表编号
     * @return 表定义
     */
    ToolCodegenTableDO getCodeGenTablePage(Long id);

    /**
     * 获得指定表的字段定义数组
     *
     * @param tableId 表编号
     * @return 字段定义数组
     */
    List<ToolCodegenColumnDO> getCodegenColumnListByTableId(Long tableId);

    /**
     * 执行指定表的代码生成
     *
     * @param tableId 表编号
     * @return 生成结果。key 为文件路径，value 为对应的代码内容
     */
    Map<String, String> generationCodes(Long tableId);

}
