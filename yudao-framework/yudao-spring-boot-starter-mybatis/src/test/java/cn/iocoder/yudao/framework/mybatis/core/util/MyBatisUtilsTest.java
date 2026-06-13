package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MyBatisUtils} 的单元测试
 */
public class MyBatisUtilsTest {

    @Test
    public void testBuildPage_sortingFields() {
        // 准备参数
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(2);
        pageParam.setPageSize(20);
        List<SortingField> sortingFields = Arrays.asList(
                new SortingField("userName", SortingField.ORDER_ASC),
                new SortingField("u.id", SortingField.ORDER_DESC),
                new SortingField("name desc", SortingField.ORDER_DESC));

        // 调用
        Page<Object> page = MyBatisUtils.buildPage(pageParam, sortingFields);

        // 断言
        assertEquals(2, page.getCurrent());
        assertEquals(20, page.getSize());
        assertEquals(2, page.orders().size());
        assertOrderItem(page.orders().get(0), "user_name", true);
        assertOrderItem(page.orders().get(1), "u.id", false);
    }

    @Test
    public void testAddOrder_queryWrapper() {
        // 准备参数
        QueryWrapper<Object> query = new QueryWrapper<>();
        List<SortingField> sortingFields = Arrays.asList(
                new SortingField("userName", SortingField.ORDER_ASC),
                new SortingField("u.id", SortingField.ORDER_DESC),
                new SortingField("name;drop", SortingField.ORDER_ASC));

        // 调用
        MyBatisUtils.addOrder(query, sortingFields);

        // 断言
        assertEquals(" ORDER BY user_name ASC,u.id DESC", query.getSqlSegment());
    }

    @Test
    public void testAddOrder_lambdaQueryWrapper() {
        // 准备参数
        LambdaQueryWrapper<Object> query = new LambdaQueryWrapper<>();
        List<SortingField> sortingFields = Arrays.asList(
                new SortingField("userName", SortingField.ORDER_ASC),
                new SortingField("u.id", SortingField.ORDER_DESC),
                new SortingField("name`", SortingField.ORDER_ASC));

        // 调用
        MyBatisUtils.addOrder(query, sortingFields);

        // 断言
        assertEquals(" ORDER BY user_name ASC, u.id DESC", query.getSqlSegment());
    }

    @Test
    public void testAddOrder_lambdaQueryWrapper_invalidSortingFields() {
        // 准备参数
        LambdaQueryWrapper<Object> query = new LambdaQueryWrapper<>();
        List<SortingField> sortingFields = Arrays.asList(
                new SortingField("name desc", SortingField.ORDER_ASC),
                new SortingField("name;drop", SortingField.ORDER_DESC));

        // 调用
        MyBatisUtils.addOrder(query, sortingFields);

        // 断言
        assertEquals("", query.getSqlSegment());
    }

    @Test
    public void testOrderDirection() {
        assertTrue(MyBatisUtils.isAscOrder(SortingField.ORDER_ASC));
        assertFalse(MyBatisUtils.isAscOrder(SortingField.ORDER_DESC));
        assertEquals("ASC", MyBatisUtils.getOrderDirection(SortingField.ORDER_ASC));
        assertEquals("DESC", MyBatisUtils.getOrderDirection(SortingField.ORDER_DESC));
        assertEquals("DESC", MyBatisUtils.getOrderDirection(null));
    }

    @Test
    public void testFindInSet() {
        assertEquals("FIND_IN_SET({0}, websites) <> 0",
                MyBatisUtils.findInSet(DbType.MYSQL, "websites", 0));
        assertEquals("POSITION(',' || CAST({0} AS VARCHAR) || ',' IN ',' || websites || ',') > 0",
                MyBatisUtils.findInSet(DbType.H2, "websites", 0));
        assertEquals("INSTR(',' || t.websites || ',', ',' || {0} || ',') > 0",
                MyBatisUtils.findInSet(DbType.ORACLE, "t.websites", 0));
        assertEquals("POSITION(',' || CAST({1} AS VARCHAR) || ',' IN ',' || websites || ',') > 0",
                MyBatisUtils.findInSet(DbType.POSTGRE_SQL, "websites", 1));
        assertEquals("CHARINDEX(',' + CAST({2} AS varchar(255)) + ',', ',' + websites + ',') > 0",
                MyBatisUtils.findInSet(DbType.SQL_SERVER, "websites", 2));
    }

    @Test
    public void testFindInSet_invalidColumnName() {
        assertThrows(IllegalArgumentException.class,
                () -> MyBatisUtils.findInSet(DbType.MYSQL, "websites;drop table system_tenant", 0));
        assertThrows(IllegalArgumentException.class,
                () -> MyBatisUtils.findInSet(DbType.MYSQL, "FIND_IN_SET(value, websites)", 0));
    }

    @Test
    public void testFindInSet_invalidParamIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> MyBatisUtils.findInSet(DbType.MYSQL, "websites", -1));
    }

    @Test
    public void testFindInSet_applyBindsValue() {
        // 准备参数
        QueryWrapper<Object> query = new QueryWrapper<>();
        String value = "test' OR 1 = 1";

        // 调用
        query.apply(MyBatisUtils.findInSet(DbType.MYSQL, "to_mails", 0), value);

        // 断言：SQL 片段里只有 MyBatis Plus 参数占位，用户输入不会被直接拼接进去
        assertEquals("(FIND_IN_SET(#{ew.paramNameValuePairs.MPGENVAL1}, to_mails) <> 0)",
                query.getSqlSegment());
        assertFalse(query.getSqlSegment().contains(value));
        assertEquals(value, query.getParamNameValuePairs().get("MPGENVAL1"));
    }

    @Test
    public void testFindInSet_applyBindsMultipleValues() {
        // 准备参数
        QueryWrapper<Object> query = new QueryWrapper<>();
        String value1 = "1' OR 1 = 1";
        String value2 = "2' OR 1 = 1";

        // 调用
        query.apply(MyBatisUtils.findInSet(DbType.MYSQL, "tag_ids", 0)
                + " OR " + MyBatisUtils.findInSet(DbType.MYSQL, "tag_ids", 1), value1, value2);

        // 断言：多个参数都由 MyBatis Plus 生成占位符，不拼接用户输入
        assertEquals("(FIND_IN_SET(#{ew.paramNameValuePairs.MPGENVAL1}, tag_ids) <> 0"
                        + " OR FIND_IN_SET(#{ew.paramNameValuePairs.MPGENVAL2}, tag_ids) <> 0)",
                query.getSqlSegment());
        assertFalse(query.getSqlSegment().contains(value1));
        assertFalse(query.getSqlSegment().contains(value2));
        assertEquals(value1, query.getParamNameValuePairs().get("MPGENVAL1"));
        assertEquals(value2, query.getParamNameValuePairs().get("MPGENVAL2"));
    }

    private void assertOrderItem(OrderItem orderItem, String column, boolean asc) {
        assertEquals(column, orderItem.getColumn());
        assertEquals(asc, orderItem.isAsc());
    }

}
