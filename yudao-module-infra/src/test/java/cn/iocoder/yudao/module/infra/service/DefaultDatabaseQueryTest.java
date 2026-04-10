package cn.iocoder.yudao.module.infra.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.query.DefaultQuery;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;

import java.util.List;

public class DefaultDatabaseQueryTest {

    public static void main(String[] args) {
//        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder("jdbc:oracle:thin:@127.0.0.1:1521:xe",
//                "root", "123456").build();
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder("jdbc:postgresql://127.0.0.1:5432/ruoyi-vue-pro",
                "root", "123456").build();
//        StrategyConfig strategyConfig = new StrategyConfig.Builder().build();

        ConfigBuilder builder = new ConfigBuilder(null, dataSourceConfig, null, null, null, null);

        DefaultQuery query = new DefaultQuery(builder);

        long time = System.currentTimeMillis();
        List<TableInfo> tableInfos = query.queryTables();
        for (TableInfo tableInfo : tableInfos) {
            if (StrUtil.startWithAny(tableInfo.getName().toLowerCase(), "act_", "flw_", "qrtz_")) {
                continue;
            }
            System.out.println(String.format("CREATE SEQUENCE %s_seq MINVALUE 1;", tableInfo.getName()));
//            System.out.println(String.format("DELETE FROM %s WHERE deleted = '1';", tableInfo.getName()));
        }
        System.out.println(tableInfos.size());
        System.out.println(System.currentTimeMillis() - time);
    }

}
