package cn.iocoder.yudao.framework.mybatis.core.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link QueryWrapperX} 的单元测试
 *
 * @author 芋道源码
 */
public class QueryWrapperXTest {

    @Test
    public void testBetweenIfPresent_array() {
        // 调用，并断言：空范围不追加条件
        assertEquals("", new QueryWrapperX<>().betweenIfPresent("create_time", (Object[]) null).getSqlSegment());
        assertEquals("", new QueryWrapperX<>().betweenIfPresent("create_time", new Object[0]).getSqlSegment());

        // 调用，并断言：单边范围安全降级为大于等于或小于等于
        assertTrue(new QueryWrapperX<>().betweenIfPresent("create_time", new Object[]{1})
                .getSqlSegment().contains("create_time >="));
        assertTrue(new QueryWrapperX<>().betweenIfPresent("create_time", new Object[]{null, 2})
                .getSqlSegment().contains("create_time <="));

        // 调用，并断言：双边范围使用左右闭合的 BETWEEN
        assertTrue(new QueryWrapperX<>().betweenIfPresent("create_time", new Object[]{1, 2})
                .getSqlSegment().contains("create_time BETWEEN"));
    }

}
