package cn.iocoder.yudao.module.ai.service.knowledge.splitter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO @AI：单测写完整，所有的；然后代码风格，对齐 system user test；
/**
 * Markdown 问答标题识别测试
 *
 * @author 芋道源码
 */
class MarkdownQaSplitterTest {

    @Test
    void testSplitText_doesNotTreatNextLineAsHeading() {
        MarkdownQaSplitter splitter = new MarkdownQaSplitter(100);
        String question = "##\t有效标题\r\n回答";
        assertEquals(List.of(question), splitter.splitText("##\r\n正文\r\n\r\n" + question));
    }

    @Test
    void testSplitText_nonBreakingSpaceStaysInAnswer() {
        MarkdownQaSplitter splitter = new MarkdownQaSplitter(200);
        String first = "## 问题一\n上半段\n##\u00a0普通文本\n下半段";
        String second = "## 问题二\n答案";
        assertEquals(List.of(first, second), splitter.splitText(first + "\n" + second));
    }

}
