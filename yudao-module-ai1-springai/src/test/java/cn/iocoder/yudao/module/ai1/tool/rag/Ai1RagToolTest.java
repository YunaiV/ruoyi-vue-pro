package cn.iocoder.yudao.module.ai1.tool.rag;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Ai1RagTool} 的单元测试：文本分片
 *
 * @author 芋道源码
 */
public class Ai1RagToolTest {

    @Test
    public void testSplit_withOverlap() {
        assertEquals(Arrays.asList("abcd", "cdef", "efgh", "ghij"), Ai1RagTool.split("abcdefghij", 4, 2));
    }

    @Test
    public void testSplit_overlapLimitedToChunkSizeMinusOne() {
        // 重叠大于等于分片大小时，限制为分片大小 - 1，保证每次至少前进一个字符
        assertEquals(Arrays.asList("abc", "bcd", "cde"), Ai1RagTool.split("abcde", 3, 10));
    }

    @Test
    public void testSplit_empty() {
        assertEquals(Collections.emptyList(), Ai1RagTool.split("", 500, 50));
        assertEquals(Collections.singletonList("abc"), Ai1RagTool.split("abc", 500, 50));
    }

}
