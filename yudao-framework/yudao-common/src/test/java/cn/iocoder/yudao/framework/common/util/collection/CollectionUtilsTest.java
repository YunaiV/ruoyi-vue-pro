package cn.iocoder.yudao.framework.common.util.collection;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionUtilsTest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class Dog {

        private Integer id;
        private String name;

    }

    @Test
    public void testContainsAny() {
        assertTrue(CollectionUtils.containsAny(10L, 10L, 20L)); // 存在
        assertFalse(CollectionUtils.containsAny(0L, 10L, 20L)); // 不存在
    }

    @Test
    public void testIsAnyEmpty() {
        assertTrue(CollectionUtils.isAnyEmpty(emptyList(), singleton(2L)));
        assertFalse(CollectionUtils.isAnyEmpty(singleton(1L), singleton(2L)));
    }

    @Test
    public void testFilterList() {
        // 准备参数
        Collection<Integer> from = asList(10, 20);
        Predicate<Integer> predicate = num -> num.equals(10);

        // 调用
        List<Integer> result = CollectionUtils.filterList(from, predicate);
        // 断言
        assertEquals(1, result.size());
        assertEquals(10, result.get(0));
    }

    @Test
    public void testDistinct() {
        // 准备参数
        List<Dog> from = Arrays.asList(
                new Dog(1, "zhu"), new Dog(2, "zhu"));
        Function<Dog, String> keyMapper = Dog::getName;

        // 调用
        List<Dog> result = CollectionUtils.distinct(from, keyMapper);
        // 断言
        assertEquals(1, result.size());
        assertEquals(from.get(0), result.get(0));
    }

    @Test
    public void testConvertList() {
        // 准备参数
        List<Dog> from = Arrays.asList(
                new Dog(1, "zhu"), new Dog(2, "zhu"));
        Function<Dog, Integer> func =  Dog::getId;

        // 调用
        List<Integer> result = CollectionUtils.convertList(from, func);
        // 断言
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

    @Test
    public void testConvertSet() {
        // 准备参数
        List<Dog> from = Arrays.asList(
                new Dog(1, "zhu"), new Dog(2, "zhu"));
        Function<Dog, Integer> func =  Dog::getId;

        // 调用
        Set<Integer> result = CollectionUtils.convertSet(from, func);
        // 断言
        assertEquals(2, result.size());
        assertEquals(1, CollUtil.get(result, 0));
        assertEquals(2, CollUtil.get(result, 1));
    }

    @Test
    public void testConvertMap() {
        // 准备参数
        List<Dog> from = Arrays.asList(
                new Dog(1, "zhu"), new Dog(2, "zhu"));
        Function<Dog, Integer> func =  Dog::getId;

        // 调用
        Map<Integer, Dog> result = CollectionUtils.convertMap(from, func);
        // 断言
        assertEquals(2, result.size());
        assertEquals(from.get(0), result.get(1));
        assertEquals(from.get(1), result.get(2));
    }
}
