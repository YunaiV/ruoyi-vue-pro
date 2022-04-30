package cn.iocoder.yudao.module.infra.service;

import com.baomidou.mybatisplus.generator.IDatabaseQuery.DefaultDatabaseQuery;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;

import java.util.List;

public class DefaultDatabaseQueryTest {

    public static void main(String[] args) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder("jdbc:oracle:thin:@127.0.0.1:1521:xe",
                "root", "123456").build();
//        StrategyConfig strategyConfig = new StrategyConfig.Builder().build();

        ConfigBuilder builder = new ConfigBuilder(null, dataSourceConfig, null, null, null, null);

        DefaultDatabaseQuery query = new DefaultDatabaseQuery(builder);

        long time = System.currentTimeMillis();
        List<TableInfo> tableInfos = query.queryTables();
        System.out.println(tableInfos.size());
        System.out.println(System.currentTimeMillis() - time);
    }

}
