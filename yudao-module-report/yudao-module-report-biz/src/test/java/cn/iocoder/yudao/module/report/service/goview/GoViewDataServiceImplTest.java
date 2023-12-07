package cn.iocoder.yudao.module.report.service.goview;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import jakarta.annotation.Resource;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(GoViewDataServiceImpl.class)
public class GoViewDataServiceImplTest extends BaseDbUnitTest {

    @Resource
    private GoViewDataServiceImpl goViewDataService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testGetDataBySQL() {
        // 准备参数
        String sql = "SELECT id, name FROM system_users";
        // mock 方法
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(jdbcTemplate.queryForRowSet(eq(sql))).thenReturn(sqlRowSet);
        // mock 元数据
        SqlRowSetMetaData metaData = mock(SqlRowSetMetaData.class);
        when(sqlRowSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnNames()).thenReturn(new String[]{"id", "name"});
        // mock 数据明细
        when(sqlRowSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getObject("id")).thenReturn(1L).thenReturn(2L);
        when(sqlRowSet.getObject("name")).thenReturn("芋道源码").thenReturn("芋道");

        // 调用
        GoViewDataRespVO dataBySQL = goViewDataService.getDataBySQL(sql);
        // 断言
        assertEquals(Arrays.asList("id", "name"), dataBySQL.getDimensions());
        assertEquals(2, dataBySQL.getDimensions().size());
        assertEquals(2, dataBySQL.getSource().get(0).size());
        assertEquals(1L, dataBySQL.getSource().get(0).get("id"));
        assertEquals("芋道源码", dataBySQL.getSource().get(0).get("name"));
        assertEquals(2, dataBySQL.getSource().get(1).size());
        assertEquals(2L, dataBySQL.getSource().get(1).get("id"));
        assertEquals("芋道", dataBySQL.getSource().get(1).get("name"));
    }

}
