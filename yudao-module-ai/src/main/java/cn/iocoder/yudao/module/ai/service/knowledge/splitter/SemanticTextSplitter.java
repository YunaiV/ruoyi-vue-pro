package cn.iocoder.yudao.module.ai.service.knowledge.splitter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义化文本切片器
 *
 * <p>功能特点：
 * <ul>
 *   <li>优先在段落边界（双换行）处切分</li>
 *   <li>其次在句子边界（句号、问号、感叹号）处切分</li>
 *   <li>避免在句子中间截断，保持语义完整性</li>
 *   <li>支持中英文标点符号识别</li>
 * </ul>
 *
 * @author runzhen
 */
@Slf4j
public class SemanticTextSplitter extends TextSplitter {

    /**
     * 分段的最大 Token 数
     */
    private final int chunkSize;

    /**
     * 段落重叠大小（用于保持上下文连贯性）
     */
    private final int chunkOverlap;

    /**
     * 段落分隔符（按优先级排序）
     */
    private static final List<String> PARAGRAPH_SEPARATORS = Arrays.asList(
            "\n\n\n",    // 三个换行
            "\n\n",      // 双换行
            "\n"         // 单换行
    );

    /**
     * 句子结束标记（中英文标点）
     */
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile(
            "[。！？.!?]+[\\s\"'）)】\\]]*"
    );

    /**
     * Token 估算器
     */
    private final MarkdownQaSplitter.TokenEstimator tokenEstimator;

    public SemanticTextSplitter(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = Math.min(chunkOverlap, chunkSize / 2); // 重叠不超过一半
        this.tokenEstimator = new SimpleTokenEstimator();
    }

    public SemanticTextSplitter(int chunkSize) {
        this(chunkSize, 50); // 默认重叠 50 个 Token
    }

    @Override
    protected List<String> splitText(String text) {
        if (StrUtil.isEmpty(text)) {
            return Collections.emptyList();
        }
        return splitTextRecursive(text);
    }

    /**
     * 切分文本（递归策略）
     *
     * @param text 待切分文本
     * @return 切分后的文本块列表
     */
    private List<String> splitTextRecursive(String text) {
        List<String> chunks = new ArrayList<>();

        // 如果文本不超过限制，直接返回
        int textTokens = tokenEstimator.estimate(text);
        if (textTokens <= chunkSize) {
            chunks.add(text.trim());
            return chunks;
        }

        // 尝试按不同分隔符切分
        List<String> splits = null;
        String usedSeparator = null;
        for (String separator : PARAGRAPH_SEPARATORS) {
            if (text.contains(separator)) {
                splits = Arrays.asList(text.split(Pattern.quote(separator)));
                usedSeparator = separator;
                break;
            }
        }

        // 如果没有找到段落分隔符，按句子切分
        if (splits == null || splits.size() == 1) {
            splits = splitBySentences(text);
            usedSeparator = ""; // 句子切分不需要分隔符
        }

        // 合并小片段
        chunks = mergeSplits(splits, usedSeparator);
        return chunks;
    }

    /**
     * 按句子切分
     *
     * @param text 待切分文本
     * @return 句子列表
     */
    private List<String> splitBySentences(String text) {
        // 使用正则表达式匹配句子结束位置
        List<String> sentences = new ArrayList<>();
        int lastEnd = 0;
        Matcher matcher = SENTENCE_END_PATTERN.matcher(text);
        while (matcher.find()) {
            String sentence = text.substring(lastEnd, matcher.end()).trim();
            if (StrUtil.isNotEmpty(sentence)) {
                sentences.add(sentence);
            }
            lastEnd = matcher.end();
        }

        // 添加剩余部分
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd).trim();
            if (StrUtil.isNotEmpty(remaining)) {
                sentences.add(remaining);
            }
        }
        return sentences.isEmpty() ? Collections.singletonList(text) : sentences;
    }

    /**
     * 合并切分后的小片段
     *
     * @param splits 切分后的片段列表
     * @param separator 片段间的分隔符
     * @return 合并后的文本块列表
     */
    private List<String> mergeSplits(List<String> splits, String separator) {
        List<String> chunks = new ArrayList<>();
        List<String> currentChunks = new ArrayList<>();
        int currentLength = 0;

        for (String split : splits) {
            if (StrUtil.isEmpty(split)) {
                continue;
            }
            int splitTokens = tokenEstimator.estimate(split);
            // 如果单个片段就超过限制，进一步递归切分
            if (splitTokens > chunkSize) {
                // 先保存当前累积的块
                if (!currentChunks.isEmpty()) {
                    String chunkText = String.join(separator, currentChunks);
                    chunks.add(chunkText.trim());
                    currentChunks.clear();
                    currentLength = 0;
                }
                // 递归切分大片段
                if (!separator.isEmpty()) {
                    // 如果是段落分隔符，尝试按句子切分
                    chunks.addAll(splitTextRecursive(split));
                } else {
                    // 如果已经是句子级别，强制按字符切分
                    chunks.addAll(forceSplitLongText(split));
                }
                continue;
            }
            // 计算加上分隔符的 Token 数
            int separatorTokens = StrUtil.isEmpty(separator) ? 0 : tokenEstimator.estimate(separator);
            // 如果加上这个片段会超过限制
            if (!currentChunks.isEmpty() && currentLength + splitTokens + separatorTokens > chunkSize) {
                // 保存当前块
                String chunkText = String.join(separator, currentChunks);
                chunks.add(chunkText.trim());

                // 处理重叠：保留最后几个片段
                currentChunks = getOverlappingChunks(currentChunks, separator);
                currentLength = estimateTokens(currentChunks, separator);
            }
            // 添加当前片段
            currentChunks.add(split);
            currentLength += splitTokens + separatorTokens;
        }

        // 添加最后一块
        if (!currentChunks.isEmpty()) {
            String chunkText = String.join(separator, currentChunks);
            chunks.add(chunkText.trim());
        }
        return chunks;
    }

    /**
     * 获取重叠的片段（用于保持上下文）
     *
     * @param chunks 当前片段列表
     * @param separator 片段间的分隔符
     * @return 重叠的片段列表
     */
    private List<String> getOverlappingChunks(List<String> chunks, String separator) {
        if (chunkOverlap == 0 || chunks.isEmpty()) {
            return new ArrayList<>();
        }

        // 从后往前取片段，直到达到重叠大小
        List<String> overlapping = new ArrayList<>();
        int tokens = 0;
        for (int i = chunks.size() - 1; i >= 0; i--) {
            String chunk = chunks.get(i);
            int chunkTokens = tokenEstimator.estimate(chunk);
            if (tokens + chunkTokens > chunkOverlap) {
                break;
            }
            // 添加到重叠列表前端
            overlapping.add(0, chunk);
            tokens += chunkTokens + (StrUtil.isEmpty(separator) ? 0 : tokenEstimator.estimate(separator));
        }
        return overlapping;
    }

    /**
     * 估算片段列表的总 Token 数
     *
     * @param chunks 片段列表
     * @param separator 片段间的分隔符
     * @return 总 Token 数
     */
    private int estimateTokens(List<String> chunks, String separator) {
        int total = 0;
        for (int i = 0; i < chunks.size(); i++) {
            total += tokenEstimator.estimate(chunks.get(i));
            if (i < chunks.size() - 1 && StrUtil.isNotEmpty(separator)) {
                total += tokenEstimator.estimate(separator);
            }
        }
        return total;
    }

    /**
     * 强制切分长文本（当语义切分失败时）
     *
     * @param text 待切分文本
     * @return 切分后的文本块列表
     */
    private List<String> forceSplitLongText(String text) {
        List<String> chunks = new ArrayList<>();
        int charsPerChunk = (int) (chunkSize * 0.8); // 保守估计
        for (int i = 0; i < text.length(); i += charsPerChunk) {
            int end = Math.min(i + charsPerChunk, text.length());
            String chunk = text.substring(i, end);
            chunks.add(chunk.trim());
        }
        log.warn("文本过长，已强制按字符切分，可能影响语义完整性");
        return chunks;
    }

    /**
     * 简单的 Token 估算器实现
     */
    private static class SimpleTokenEstimator implements MarkdownQaSplitter.TokenEstimator {

        @Override
        public int estimate(String text) {
            if (StrUtil.isEmpty(text)) {
                return 0;
            }

            int chineseChars = 0;
            int englishWords = 0;
            // 简单统计中英文
            for (char c : text.toCharArray()) {
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    chineseChars++;
                }
            }
            // 英文单词估算
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches(".*[a-zA-Z].*")) {
                    englishWords++;
                }
            }
            return chineseChars + (int) (englishWords * 1.3);
        }
    }

}
