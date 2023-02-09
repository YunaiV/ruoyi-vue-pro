package cn.iocoder.yudao.module.report.service.goview;

import cn.iocoder.yudao.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * GoView 数据 Service 实现类
 *
 * 补充说明：
 * 1. 目前默认使用 jdbcTemplate 查询项目配置的数据源。如果你想查询其它数据源，可以新建对应数据源的 jdbcTemplate 来实现。
 * 2. 默认数据源是 MySQL 关系数据源，可能数据量比较大的情况下，会比较慢，可以考虑后续使用 Click House 等等。
 *
 * @author 芋道源码
 */
@Service
@Validated
public class GoViewDataServiceImpl implements GoViewDataService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public GoViewDataRespVO getDataBySQL(String sql) {
        // 1. 执行查询
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

        // 2. 构建返回结果
        GoViewDataRespVO respVO = new GoViewDataRespVO();
        // 2.1 解析元数据
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        String[] columnNames = metaData.getColumnNames();
        respVO.setDimensions(Arrays.asList(columnNames));
        // 2.2 解析数据明细
        respVO.setSource(new LinkedList<>()); // 由于数据量不确认，使用 LinkedList 虽然内存占用大一点，但是不存在扩容复制的问题
        while (sqlRowSet.next()) {
            Map<String, Object> data = Maps.newHashMapWithExpectedSize(columnNames.length);
            for (String columnName : columnNames) {
                data.put(columnName, sqlRowSet.getObject(columnName));
            }
            respVO.getSource().add(data);
        }
        return respVO;
    }

}
