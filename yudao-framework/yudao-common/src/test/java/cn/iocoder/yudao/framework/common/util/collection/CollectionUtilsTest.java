package cn.iocoder.yudao.framework.common.util.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CollectionUtils} 的单元测试
 */
public class CollectionUtilsTest {

    @Data
    @AllArgsConstructor
    private static class Dog {

        private Integer id;
        private String name;
        private String code;

    }

    @Test
    public void testSum() {
        assertEquals(6L, CollectionUtils.sum(Arrays.asList(1L, 2L, 3L), Long::longValue));
        assertEquals(0L, CollectionUtils.sum(Collections.<Long>emptyList(), Long::longValue));
    }

    @Test
    public void testCount() {
        assertEquals(2L, CollectionUtils.count(Arrays.asList(1, 2, 3, 4), value -> value % 2 == 0));
        assertEquals(0L, CollectionUtils.count(Collections.emptyList(), value -> true));
    }

    @Test
    public void testDistinctCount() {
        assertEquals(3L, CollectionUtils.distinctCount(Arrays.asList("a", "b", "a", "c"), value -> value));
        assertEquals(0L, CollectionUtils.distinctCount(Collections.emptyList(), value -> value));
    }

    @Test
    public void testConvertSet_withSupplier() {
        // 调用，并断言：按指定 Set 类型完成转换、去重和排序
        TreeSet<Integer> result = CollectionUtils.convertSetBySupplier(
                Arrays.asList("3", "1", null, "3", "2"),
                value -> value == null ? null : Integer.valueOf(value), TreeSet::new);
        assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(result.toArray(new Integer[0])));

        // 调用，并断言：空集合仍返回指定 Set 类型
        result = CollectionUtils.convertSetBySupplier(Collections.<String>emptyList(),
                Integer::valueOf, TreeSet::new);
        assertEquals(TreeSet.class, result.getClass());
    }

    @Test
    public void testDiffList() {
        // 准备参数
        Collection<Dog> oldList = Arrays.asList(
                new Dog(1, "花花", "hh"),
                new Dog(2, "旺财", "wc")
        );
        Collection<Dog> newList = Arrays.asList(
                new Dog(null, "花花2", "hh"),
                new Dog(null, "小白", "xb")
        );
        BiFunction<Dog, Dog, Boolean> sameFunc = (oldObj, newObj) -> {
            boolean same = oldObj.getCode().equals(newObj.getCode());
            // 如果相等的情况下，需要设置下 id，后续好更新
            if (same) {
                newObj.setId(oldObj.getId());
            }
            return same;
        };

        // 调用
        List<List<Dog>> result = CollectionUtils.diffList(oldList, newList, sameFunc);
        // 断言
        assertEquals(result.size(), 3);
        // 断言 create
        assertEquals(result.get(0).size(), 1);
        assertEquals(result.get(0).get(0), new Dog(null, "小白", "xb"));
        // 断言 update
        assertEquals(result.get(1).size(), 1);
        assertEquals(result.get(1).get(0), new Dog(1, "花花2", "hh"));
        // 断言 delete
        assertEquals(result.get(2).size(), 1);
        assertEquals(result.get(2).get(0), new Dog(2, "旺财", "wc"));
    }

}
