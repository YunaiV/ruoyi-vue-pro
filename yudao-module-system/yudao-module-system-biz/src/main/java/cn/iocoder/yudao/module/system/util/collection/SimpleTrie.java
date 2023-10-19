package cn.iocoder.yudao.module.system.util.collection;

import cn.hutool.core.collection.CollUtil;

import java.util.*;

/**
 * 基于前缀树，实现敏感词的校验
 * <p>
 * 相比 Apache Common 提供的 PatriciaTrie 来说，性能可能会更加好一些。
 *
 * @author 芋道源码
 */
@SuppressWarnings("unchecked")
public class SimpleTrie {

    /**
     * 一个敏感词结束后对应的 key
     */
    private static final Character CHARACTER_END = '\0';

    /**
     * 使用敏感词，构建的前缀树
     */
    private final Map<Character, Object> children;

    /**
     * 基于字符串，构建前缀树
     *
     * @param strs 字符串数组
     */
    public SimpleTrie(Collection<String> strs) {
        // 排序，优先使用较短的前缀
        strs = CollUtil.sort(strs, String::compareTo);
        // 构建树
        children = new HashMap<>();
        for (String str : strs) {
            Map<Character, Object> child = children;
            // 遍历每个字符
            for (Character c : str.toCharArray()) {
                // 如果已经到达结束，就没必要在添加更长的敏感词。
                // 例如说，有两个敏感词是：吃饭啊、吃饭。输入一句话是 “我要吃饭啊”，则只要匹配到 “吃饭” 这个敏感词即可。
                if (child.containsKey(CHARACTER_END)) {
                    break;
                }
                if (!child.containsKey(c)) {
                    child.put(c, new HashMap<>());
                }
                child = (Map<Character, Object>) child.get(c);
            }
            // 结束
            child.put(CHARACTER_END, null);
        }
    }

    /**
     * 验证文本是否合法，即不包含敏感词
     *
     * @param text 文本
     * @return 是否 true-合法 false-不合法
     */
    public boolean isValid(String text) {
        // 遍历 text，使用每一个 [i, n) 段的字符串，使用 children 前缀树匹配，是否包含敏感词
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> child = (Map<Character, Object>) children.get(text.charAt(i));
            if (child == null) {
                continue;
            }
            boolean ok = recursion(text, i + 1, child);
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证文本从指定位置开始，是否不包含某个敏感词
     *
     * @param text  文本
     * @param index 开始位置
     * @param child 节点（当前遍历到的）
     * @return 是否不包含 true-不包含 false-包含
     */
    private boolean recursion(String text, int index, Map<Character, Object> child) {
        if (child.containsKey(CHARACTER_END)) {
            return false;
        }
        if (index == text.length()) {
            return true;
        }
        child = (Map<Character, Object>) child.get(text.charAt(index));
        return child == null || !child.containsKey(CHARACTER_END) && recursion(text, ++index, child);
    }

    /**
     * 获得文本所包含的不合法的敏感词
     *
     * 注意，才当即最短匹配原则。例如说：当敏感词存在 “煞笔”，“煞笔二货 ”时，只会返回 “煞笔”。
     *
     * @param text 文本
     * @return 匹配的敏感词
     */
    public List<String> validate(String text) {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < text.length(); i++) {
            Character c = text.charAt(i);
            Map<Character, Object> child = (Map<Character, Object>) children.get(c);
            if (child == null) {
                continue;
            }
            StringBuilder result = new StringBuilder().append(c);
            boolean ok = recursionWithResult(text, i + 1, child, result);
            if (!ok) {
                results.add(result.toString());
            }
        }
        return new ArrayList<>(results);
    }

    /**
     * 返回文本从 index 开始的敏感词，并使用 StringBuilder 参数进行返回
     *
     * 逻辑和 {@link #recursion(String, int, Map)} 是一致，只是多了 result 返回结果
     *
     * @param text   文本
     * @param index  开始未知
     * @param child  节点（当前遍历到的）
     * @param result 返回敏感词
     * @return 是否有敏感词
     */
    @SuppressWarnings("unchecked")
    private static boolean recursionWithResult(String text, int index, Map<Character, Object> child, StringBuilder result) {
        if (child.containsKey(CHARACTER_END)) {
            return false;
        }
        if (index == text.length()) {
            return true;
        }
        Character c = text.charAt(index);
        child = (Map<Character, Object>) child.get(c);
        if (child == null) {
            return true;
        }
        if (child.containsKey(CHARACTER_END)) {
            result.append(c);
            return false;
        }
        return recursionWithResult(text, ++index, child, result.append(c));
    }

}
