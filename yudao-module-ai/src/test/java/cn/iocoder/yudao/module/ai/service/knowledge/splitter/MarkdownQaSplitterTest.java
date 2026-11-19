package cn.iocoder.yudao.module.ai.service.knowledge.splitter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

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
        String first = "## 问题一\n上半段\n## 普通文本\n下半段";
        String second = "## 问题二\n答案";
        assertEquals(List.of(first, second), splitter.splitText(first + "\n" + second));
    }

    @Test
    void testSplitText_mixedSeparatorsTrimmedFromQuestion() {
        // 准备参数：标题后混合多个空格/制表符，答案超过限制触发切分
        MarkdownQaSplitter splitter = new MarkdownQaSplitter(30);
        String paragraph1 = "一二三四五六七八九十一二三四五";
        String paragraph2 = "五四三二一十九八七六五四三二一";
        String text = "## \t  \t问题\r\n" + paragraph1 + "\n\n" + paragraph2;

        // 调用，并断言：切分后的片段使用去除前导空白的问题
        assertEquals(List.of("## 问题\n" + paragraph1, "## 问题\n" + paragraph2),
                splitter.splitText(text));
    }

    @Test
    void testSplitText_longSeparatorRun() {
        MarkdownQaSplitter splitter = new MarkdownQaSplitter(100);
        String text = "##" + " \t".repeat(50_000) + "问题\r\n答案";
        List<String> result = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> splitter.splitText(text));
        assertEquals(List.of(text), result);
    }

    @Test
    void testSplitText_longBareHeadingLinesNotHeading() {
        // 准备参数：大量没有分隔符的 ## 行，以及 NBSP 分隔的长行，都不应识别为标题
        MarkdownQaSplitter splitter = new MarkdownQaSplitter(100_000);
        String body = "##\n".repeat(20_000) + "## " + " ".repeat(50_000) + "正文";
        String text = body + "\n## 问题\n答案";

        List<String> result = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> splitter.splitText(text));
        assertEquals(List.of("## 问题\n答案"), result);
    }

}
