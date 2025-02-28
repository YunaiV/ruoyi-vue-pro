package cn.iocoder.yudao.framework.common.util.string;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
     * 是否为空白
     * @param str 字符串
     * @return 是否有内容
     */
    public static boolean isBlank(String str) {
        final int strLen = str==null ? 0 : str.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 异常转字符串
     * @param e Throwable
     * @return String
     * */
    public static String toString(Throwable e) {
        if(e==null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String content = sw.toString();
        try {
            sw.close();
            pw.close();
        } catch (IOException e1) {
        }
        return content;
    }

    /**
     * 把各个部分拼接成地址,连接符号按操作系统 斜杠或反斜杠
     * @param parts 路径的各个部分
     * @return 拼接后的路径
     */
    public static String joinPath(String... parts) {
        return joinPathInternal(File.separator, parts);
    }

    /**
     * 把各个部分拼接成地址 始终使用斜杠
     * @param parts 路径的各个部分
     * @return 拼接后的路径
     */
    public static String joinUrl(String... parts) {
        String url = joinPathInternal("/", parts);
        return url.replace('\\', '/');
    }

    /**
     * 把各个部分拼接成地址
     */
    private static String joinPathInternal(String sep, String... part) {
        String p = null;
        StringBuilder pa = new StringBuilder();
        for (int i = 0; i < part.length; i++) {
            if(StrUtils.isBlank(part[i])) {
                continue;
                //throw new IllegalArgumentException("子路径不允许为空");
            }
            p = part[i] + "";
            if (i == 0) {
                p = p.trim();
                p = StrUtils.removeLast(p, "/");
                p = StrUtils.removeLast(p, "\\");
                pa.append(p);
            } else {
                p = p.trim();
                p = StrUtils.removeLast(p, "/");
                p = StrUtils.removeLast(p, "\\");
                p = StrUtils.removeFirst(p, "/");
                p = StrUtils.removeFirst(p, "\\");
                pa.append(sep + p);
            }
        }
        return pa.toString();
    }


    /**
     * 移除字符串str中第一个匹配的字符串c
     * @param str 源文本
     * @param c	将要移除的内容
     * @return 处理后的字符串
     * */
    public static String removeFirst(String str, String c) {

        if (str == null || c == null) {
            return str;
        }
        while (str.startsWith(c)) {
            str = str.substring(c.length());
        }
        return str;
    }

    /**
     * 移除字符串str中最后一个匹配的字符串c
     * @param str 源文本
     * @param c	将要移除的内容
     * @return 处理后的字符串
     * */
    public static String removeLast(String str, String c) {
        if (str == null || c == null) {
            return str;
        }
        while (str.endsWith(c)) {
            str = str.substring(0, str.length() - c.length());
        }
        return str;
    }



    /**
     * 把数组拼接成字符串，默认用逗号隔开
     * @param array 数组
     * @return 拼接后的字符串
     * */
    public static String join(Object... array)
    {
        return ArrayUtils.join(array);
    }

    /**
     * 把数组拼接成字符串
     * @param array 数组
     * @param sep 分隔符
     * @return 拼接后的字符串
     * */
    public static String join(Object[] array,String sep)
    {
        return ArrayUtils.join(array,sep,"");
    }


    /**
     * 把数组拼接成字符串
     * @param array 数组
     * @param sep 分隔符
     * @param quote 引号
     * @return 拼接后的字符串
     * */
    public static String join(Object[] array,String sep,String quote)
    {
        return ArrayUtils.join(array,sep,quote);
    }


    /**
     * 把数组拼接成字符串，默认用逗号隔开
     * @param list 元素清单，toString()后再拼接
     * @return 拼接后的字符串
     * */
    @SuppressWarnings("rawtypes")
    public static String join(Collection list)
    {
        return ArrayUtils.join(list.toArray());
    }

    /**
     * 把数组拼接成字符串
     * @param list 元素清单，toString()后再拼接
     * @param sep 分隔符
     * @param quote 引号
     * @return 拼接后的字符串
     * */
    @SuppressWarnings("rawtypes")
    public static String join(Collection list,String sep,String quote)
    {
        return ArrayUtils.join(list.toArray(),sep,quote);
    }

    /**
     * 把数组拼接成字符串
     * @param list 元素清单，toString()后再拼接
     * @param sep 分隔符
     * @return 拼接后的字符串
     * */
    @SuppressWarnings("rawtypes")
    public static String join(Collection list,String sep)
    {
        if(list==null) return null;
        return ArrayUtils.join(list.toArray(),sep,"");
    }

}
