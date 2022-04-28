package cn.iocoder.yudao.module.infra.service.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;
import cn.iocoder.yudao.module.infra.dal.mysql.db.DatabaseTableDAO;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;

/**
 * 数据库表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class DatabaseTableServiceImpl implements DatabaseTableService {

    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Resource
    private List<DatabaseTableDAO> databaseTableDAOs;

    @Override
    @SneakyThrows
    public List<DatabaseTableDO> getTableList(Long dataSourceConfigId, String tableNameLike, String tableCommentLike) {
        try (Connection connection = getConnection(dataSourceConfigId)) {
            return getDatabaseTableDAO(dataSourceConfigId).selectTableList(connection, tableNameLike, tableCommentLike);
        }
    }

    @Override
    @SneakyThrows
    public DatabaseTableDO getTable(Long dataSourceConfigId, String tableName) {
        try (Connection connection = getConnection(dataSourceConfigId)) {
            return getDatabaseTableDAO(dataSourceConfigId).selectTable(connection, tableName);
        }
    }

    @Override
    @SneakyThrows
    public List<DatabaseColumnDO> getColumnList(Long dataSourceConfigId, String tableName) {
        try (Connection connection = getConnection(dataSourceConfigId)) {
            return getDatabaseTableDAO(dataSourceConfigId).selectColumnList(connection, tableName);
        }
    }

    private Connection getConnection(Long dataSourceConfigId) {
        DataSourceConfigDO config = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        Assert.notNull(config, "数据源({}) 不存在！", dataSourceConfigId);
        return JdbcUtils.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }

    private DatabaseTableDAO getDatabaseTableDAO(Long dataSourceConfigId) {
        DataSourceConfigDO config = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        Assert.notNull(config, "数据源({}) 不存在！", dataSourceConfigId);
        // 获得 dbType
        DbType dbType = JdbcUtils.getDbType(config.getUrl());
        Assert.notNull(config, "数据源类型({}) 不存在！", config.getUrl());
        // 获得 DatabaseTableDAO
        DatabaseTableDAO dao = CollUtil.findOne(databaseTableDAOs, databaseTableDAO -> databaseTableDAO.getType().equals(dbType));
        Assert.notNull(dao, "DAO({}) 查找不到实现！", dbType);
        return dao;
    }

}
