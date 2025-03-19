package com.somle.framework.common.util.string;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author 芋道源码
 */
public class StrUtils {
    // 定义静态的 HanyuPinyinOutputFormat 变量
    private static final HanyuPinyinOutputFormat PINYIN_FORMAT;

    // 使用 static 代码块初始化静态变量.在整个类中复用，减少内存开销和提高性能。
    static {
        PINYIN_FORMAT = new HanyuPinyinOutputFormat();
        PINYIN_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        PINYIN_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //拼音中的u输出为v 例如lv
        PINYIN_FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNonEmpty(String str) {
        return !isEmpty(str);
    }

    public static String maxLength(CharSequence str, int maxLength) {
        return StrUtil.maxLength(str, maxLength - 3); // -3 的原因，是该方法会补充 ... 恰好
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回 false
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串
     * @since 3.0.6
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (StrUtil.isEmpty(str) || ArrayUtil.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence suffix : prefixes) {
            if (StrUtil.startWith(str, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }

    public static Set<Long> splitToLongSet(String value) {
        return splitToLongSet(value, StrPool.COMMA);
    }

    public static Set<Long> splitToLongSet(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toSet());
    }

    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        int[] integers = StrUtil.splitToInt(value, separator);
        return Arrays.stream(integers).boxed().collect(Collectors.toList());
    }

    /**
     * 移除字符串中，包含指定字符串的行
     *
     * @param content 字符串
     * @param sequence 包含的字符串
     * @return 移除后的字符串
     */
    public static String removeLineContains(String content, String sequence) {
        if (StrUtil.isEmpty(content) || StrUtil.isEmpty(sequence)) {
            return content;
        }
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.contains(sequence))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 获取汉字的全拼
     *
     * @param chinese 汉字字符串
     * @return 拼音字符串
     */
    public static String getPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, PINYIN_FORMAT);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    pinyin.append(pinyinArray[0]);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                throw new RuntimeException("将字符转换为拼音时出错: " + c, e);
            }
        }
        return pinyin.toString();
    }

    /**
     * 将各种格式的字符串转换为驼峰命名
     * hello-world
     * hello.world
     * hello world
     * hello_world
     * 其他可能的分隔符格式（如空格、连字符、下划线等）
     * TODO 以后遇见其他格式，在下方新增
     * @param input 输入字符串
     * @return 驼峰命名的字符串
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 将所有特殊字符替换为下划线
        String replacedInput = input.replaceAll("[\\s.-]", "_");

        // 将整个字符串转换为小写
        String lowerCaseInput = replacedInput.toLowerCase();

        // 使用 Hutool 的 toCamelCase 方法转换为驼峰命名
        return StrUtil.toCamelCase(lowerCaseInput);
    }

}
