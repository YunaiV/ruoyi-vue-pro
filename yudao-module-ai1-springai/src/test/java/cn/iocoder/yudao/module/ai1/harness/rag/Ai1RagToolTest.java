package cn.iocoder.yudao.module.ai1.harness.rag;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Ai1RagTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1RagToolTest {

    @Test
    public void testSplit_withOverlap() {
        // 准备参数
        String content = "abcdefghij";

        // 调用
        List<String> chunks = Ai1RagTool.split(content, 4, 2);
        // 断言
        assertEquals(Arrays.asList("abcd", "cdef", "efgh", "ghij"), chunks);
    }

    @Test
    public void testSplit_overlapLimitedToChunkSizeMinusOne() {
        // 准备参数
        String content = "abcde";

        // 调用
        List<String> chunks = Ai1RagTool.split(content, 3, 10);
        // 断言重叠长度不超过分片大小 - 1
        assertEquals(Arrays.asList("abc", "bcd", "cde"), chunks);
    }

    @Test
    public void testSplit_empty() {
        // 调用
        List<String> emptyChunks = Ai1RagTool.split("", 500, 50);
        List<String> shortChunks = Ai1RagTool.split("abc", 500, 50);
        // 断言
        assertEquals(Collections.emptyList(), emptyChunks);
        assertEquals(Collections.singletonList("abc"), shortChunks);
    }

}
