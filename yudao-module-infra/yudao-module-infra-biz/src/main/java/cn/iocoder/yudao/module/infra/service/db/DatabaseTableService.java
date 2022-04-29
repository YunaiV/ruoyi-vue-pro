package cn.iocoder.yudao.module.infra.service.db;

import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;

import java.util.List;

/**
 * 数据库表 Service
 *
 * @author 芋道源码
 */
public interface DatabaseTableService {

    /**
     * 获得表列表，基于表名称 + 表描述进行模糊匹配
     *
     * @param dataSourceConfigId 数据源配置的编号
     * @param tableNameLike 表名称，模糊匹配
     * @param tableCommentLike 表描述，模糊匹配
     * @return 表列表
     */
    List<DatabaseTableDO> getTableList(Long dataSourceConfigId, String tableNameLike, String tableCommentLike);

    /**
     * 获得指定表名
     *
     * @param dataSourceConfigId 数据源配置的编号
     * @param tableName 表名称
     * @return 表
     */
    DatabaseTableDO getTable(Long dataSourceConfigId, String tableName);

    /**
     * 获得指定表的字段列表
     *
     * @param dataSourceConfigId 数据源配置的编号
     * @param tableName 表名称
     * @return 字段列表
     */
    List<DatabaseColumnDO> getColumnList(Long dataSourceConfigId, String tableName);


    /**
     * 获得表列表，基于表名称 + 表描述进行模糊匹配
     *
     * @param dataSourceConfigId 数据源配置的编号
     * @param tableNameLike 表名称，模糊匹配
     * @param tableCommentLike 表描述，模糊匹配
     * @return 表列表
     */
    List<TableInfo> getTableList2(Long dataSourceConfigId, String tableNameLike, String tableCommentLike);

    /**
     * 获得指定表名
     *
     * @param dataSourceConfigId 数据源配置的编号
     * @param tableName 表名称
     * @return 表
     */
    TableInfo getTable2(Long dataSourceConfigId, String tableName);

}
