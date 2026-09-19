package cn.iocoder.yudao.framework.common.util.object;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import org.springframework.util.Assert;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * {@link cn.iocoder.yudao.framework.common.pojo.PageParam} 工具类
 *
 * @author 芋道源码
 */
public class PageUtils {

    private static final Object[] ORDER_TYPES = new String[]{SortingField.ORDER_ASC, SortingField.ORDER_DESC};

    public static int getStart(PageParam pageParam) {
        return (pageParam.getPageNo() - 1) * pageParam.getPageSize();
    }

    /**
     * 对内存列表分页，不改变列表顺序
     *
     * @param pageParam 分页参数，支持 {@link PageParam#PAGE_SIZE_NONE} 返回全部数据
     * @param list 已排序的完整列表
     * @param <T> 数据类型
     * @return 分页结果，超出末页时保留总数并返回空列表
     */
    public static <T> PageResult<T> buildPageResult(PageParam pageParam, List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return PageResult.empty();
        }
        long total = list.size();
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            return new PageResult<>(list, total);
        }
        long offset = (long) (pageParam.getPageNo() - 1) * pageParam.getPageSize();
        int fromIndex = (int) Math.min(offset, total);
        int toIndex = (int) Math.min((long) fromIndex + pageParam.getPageSize(), total);
        return new PageResult<>(list.subList(fromIndex, toIndex), total);
    }

    /**
     * 构建排序字段（默认倒序）
     *
     * @param func 排序字段的 Lambda 表达式
     * @param <T>  排序字段所属的类型
     * @return 排序字段
     */
    public static <T> SortingField buildSortingField(Func1<T, ?> func) {
        return buildSortingField(func, SortingField.ORDER_DESC);
    }

    /**
     * 构建排序字段
     *
     * @param func  排序字段的 Lambda 表达式
     * @param order 排序类型 {@link SortingField#ORDER_ASC} {@link SortingField#ORDER_DESC}
     * @param <T>   排序字段所属的类型
     * @return 排序字段
     */
    public static <T> SortingField buildSortingField(Func1<T, ?> func, String order) {
        Assert.isTrue(ArrayUtil.contains(ORDER_TYPES, order), String.format("字段的排序类型只能是 %s/%s", ORDER_TYPES));

        String fieldName = LambdaUtil.getFieldName(func);
        return new SortingField(fieldName, order);
    }

    /**
     * 构建默认的排序字段
     * 如果排序字段为空，则设置排序字段；否则忽略
     *
     * @param sortablePageParam 排序分页查询参数
     * @param func              排序字段的 Lambda 表达式
     * @param <T>               排序字段所属的类型
     */
    public static <T> void buildDefaultSortingField(SortablePageParam sortablePageParam, Func1<T, ?> func) {
        if (sortablePageParam != null && CollUtil.isEmpty(sortablePageParam.getSortingFields())) {
            sortablePageParam.setSortingFields(singletonList(buildSortingField(func)));
        }
    }

}
