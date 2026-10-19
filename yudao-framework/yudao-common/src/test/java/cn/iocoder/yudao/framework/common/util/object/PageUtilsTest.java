package cn.iocoder.yudao.framework.common.util.object;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PageUtils} 的单元测试
 */
public class PageUtilsTest {

    @Test
    public void testBuildPageResult() {
        // 准备参数
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(2);
        List<Integer> list = Arrays.asList(1, 2, 3);

        // 调用并断言首页和末页
        PageResult<Integer> result = PageUtils.buildPageResult(pageParam, list);
        assertEquals(3L, result.getTotal());
        assertEquals(Arrays.asList(1, 2), result.getList());
        pageParam.setPageNo(2);
        result = PageUtils.buildPageResult(pageParam, list);
        assertEquals(3L, result.getTotal());
        assertEquals(Collections.singletonList(3), result.getList());
        assertEquals(Arrays.asList(1, 2, 3), list);
    }

    @Test
    public void testBuildPageResult_empty() {
        PageParam pageParam = new PageParam();
        assertEquals(0L, PageUtils.buildPageResult(pageParam, Collections.emptyList()).getTotal());
        assertTrue(PageUtils.buildPageResult(pageParam, null).getList().isEmpty());
    }

    @Test
    public void testBuildPageResult_pastLastPage() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(Integer.MAX_VALUE);
        pageParam.setPageSize(200);

        PageResult<Integer> result = PageUtils.buildPageResult(pageParam, Arrays.asList(1, 2, 3));

        assertEquals(3L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    public void testBuildPageResult_noPagination() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(2);
        pageParam.setPageSize(PageParam.PAGE_SIZE_NONE);

        PageResult<Integer> result = PageUtils.buildPageResult(pageParam, Arrays.asList(1, 2, 3));

        assertEquals(3L, result.getTotal());
        assertEquals(Arrays.asList(1, 2, 3), result.getList());
    }

}
