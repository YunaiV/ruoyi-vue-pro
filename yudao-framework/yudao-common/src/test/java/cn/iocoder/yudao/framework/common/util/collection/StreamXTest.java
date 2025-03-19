package cn.iocoder.yudao.framework.common.util.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StreamX} 的单元测试
 */
@Slf4j
public class StreamXTest {

    @Test
    public void test() {
        assertArrayEquals(
            List.of(1,2,3,4,5).toArray(),
            StreamX.iterate(
                1,
                num -> num < 5,
                num -> num + 1).toArray()
        );
    }

    @Test
    public void test2() {
        assertArrayEquals(
            List.of(1,2,3,4).toArray(),
            Stream.iterate(
                1,
                num -> num < 5,
                num -> num + 1).toArray()
        );
    }

}