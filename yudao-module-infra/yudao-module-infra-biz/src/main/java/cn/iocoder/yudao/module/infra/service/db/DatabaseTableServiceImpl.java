package cn.iocoder.yudao.module.infra.service.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class DatabaseTableServiceImpl implements DatabaseTableService {

    @Resource
    private DataSourceConfigService dataSourceConfigService;

    private final Map<Long, ConfigBuilder> ConfigBuilderMap = new HashMap<>();

    @PostConstruct
    public void initConfigBuilderMap() {
        for (DataSourceConfigDO config : dataSourceConfigService.getDataSourceConfigList()) {
            // 使用 MyBatis Plus Generator 解析表结构
            DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(config.getUrl(), config.getUsername(),
                    config.getPassword()).build();
            StrategyConfig.Builder strategyConfig = new StrategyConfig.Builder();
            // 移除工作流和定时任务前缀的表名 // TODO 未来做成可配置
            strategyConfig.addExclude("ACT_[\\S\\s]+|QRTZ_[\\S\\s]+|FLW_[\\S\\s]+");

            GlobalConfig globalConfig = new GlobalConfig.Builder().dateType(DateType.TIME_PACK).build(); // 只使用 Date 类型，不使用 LocalDate
            ConfigBuilder builder = new ConfigBuilder(null, dataSourceConfig, strategyConfig.build(),
                    null, globalConfig, null);

            ConfigBuilderMap.put(config.getId(), builder);
        }
    }

    @Override
    public List<TableInfo> getTableList(Long dataSourceConfigId, String nameLike, String commentLike) {
        List<TableInfo> tables = getTableList0(dataSourceConfigId, null);
        return tables.stream().filter(tableInfo -> (StrUtil.isEmpty(nameLike) || tableInfo.getName().contains(nameLike))
                        && (StrUtil.isEmpty(commentLike) || tableInfo.getComment().contains(commentLike)))
                .collect(Collectors.toList());
    }

    @Override
    public TableInfo getTable(Long dataSourceConfigId, String name) {
        return CollUtil.getFirst(getTableList0(dataSourceConfigId, name));
    }

    public List<TableInfo> getTableList0(Long dataSourceConfigId, String name) {
        ConfigBuilder builder = ConfigBuilderMap.get(dataSourceConfigId);
        Assert.notNull(builder, "数据源({}) 不存在！", dataSourceConfigId);

        // 按照名字排序
        List<TableInfo> tables = builder.getTableInfoList();
        if (StrUtil.isBlank(name)) {
            tables.sort(Comparator.comparing(TableInfo::getName));
            return tables;
        } else {
            return CollUtil.filter(tables, tableInfo -> tableInfo.getName().equals(name));
        }
    }
}
