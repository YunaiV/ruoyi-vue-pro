package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void testEscapeSqlValue() {
        // 正常值不改变
        assertEquals("hello", MyBatisUtils.escapeSqlValue("hello"));
        assertEquals("test@example.com", MyBatisUtils.escapeSqlValue("test@example.com"));

        // 单引号被转义
        assertEquals("''", MyBatisUtils.escapeSqlValue("'"));
        assertEquals("it''s", MyBatisUtils.escapeSqlValue("it's"));
        assertEquals("a''b''c", MyBatisUtils.escapeSqlValue("a'b'c"));

        // null 返回 null
        assertEquals(null, MyBatisUtils.escapeSqlValue(null));

        // 空字符串
        assertEquals("", MyBatisUtils.escapeSqlValue(""));

        // SQL 注入尝试被转义
        assertEquals("''); DROP TABLE users; --", MyBatisUtils.escapeSqlValue("'); DROP TABLE users; --"));
        assertEquals("admin''--", MyBatisUtils.escapeSqlValue("admin'--"));
    }

    private void assertOrderItem(OrderItem orderItem, String column, boolean asc) {
        assertEquals(column, orderItem.getColumn());
        assertEquals(asc, orderItem.isAsc());
    }

}
