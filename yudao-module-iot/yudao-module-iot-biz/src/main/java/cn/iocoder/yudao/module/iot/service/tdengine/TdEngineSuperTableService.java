package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;

import java.util.List;
import java.util.Map;

/**
 * TD 引擎的超级表 Service 接口
 */
public interface TdEngineSuperTableService {

    /**
     * 创建超级表
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   columns        列信息
     *                   tags           标签信息
     */
    void createSuperTable(TdTableDO superTable);

    /**
     * 查看超级表 - 显示当前数据库下的所有超级表信息
     *
     * @param superTable 超级表信息
     *                   dataBaseName 数据库名称
     *                   superTableName 超级表名称
     */

    List<Map<String, Object>> showSuperTables(TdTableDO superTable);

    /**
     * 查看超级表 - 获取超级表的结构信息
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     */
    List<Map<String, Object>> describeSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 增加列
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    void addColumnForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 删除列
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    void dropColumnForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 修改列宽
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    void modifyColumnWidthForSuperTable(TdTableDO superTable);


    /**
     * 修改超级表 - 为超级表添加标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    void addTagForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 为超级表删除标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    void dropTagForSuperTable(TdTableDO superTable);

    /**
     * 检查超级表是否存在
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     * @return 超级表数量
     */
    Integer checkSuperTableExists(TdTableDO superTable);

    /**
     * 为超级表添加列
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   columns        列信息
     */
    void addColumnsForSuperTable(TdTableDO superTable);

    /**
     * 为超级表删除列
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   columns        列信息
     */
    void dropColumnsForSuperTable(TdTableDO superTable);
}
