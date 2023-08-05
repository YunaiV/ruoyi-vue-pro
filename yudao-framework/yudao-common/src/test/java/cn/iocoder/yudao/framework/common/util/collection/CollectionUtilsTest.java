package cn.iocoder.yudao.framework.common.util.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
