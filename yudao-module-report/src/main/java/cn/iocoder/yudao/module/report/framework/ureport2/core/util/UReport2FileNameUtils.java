package cn.iocoder.yudao.module.report.framework.ureport2.core.util;

/**
 * UReport2 模板名称的工具类
 *
 * report_ureport_file.name 字段使用 MySQL 的 utf8mb4_bin 排序规则，比较时区分大小写，但属于 PAD SPACE：
 * 尾部的 ASCII 空格会被忽略，例如说 demo.ureport.xml 与「demo.ureport.xml 」是同一条记录。
 * 如果缓存、Redis 版本号直接使用原始名称，同一条记录的不同写法会对应不同的 key，
 * 导致通过别名删除、保存后，原名称的缓存没有失效。
 *
 * 因此，所有使用模板名称作为身份的地方（数据库读写、模板缓存、Redis 版本号），都需要先通过 {@link #normalize(String)} 规范化。
 * 只去掉尾部的 ASCII 空格，与数据库的比较规则一致：不转换大小写、不去掉开头的空格和其它空白字符
 *
 * @author 芋道源码
 */
public class UReport2FileNameUtils {

    /**
     * 规范化模板名称：去掉尾部的 ASCII 空格
     *
     * 既可以处理不包含存储前缀的名称（例如说 demo.ureport.xml），也可以处理包含存储前缀的名称（例如说 db:demo.ureport.xml）
     *
     * @param name 模板名称
     * @return 规范化后的模板名称；为 null 时返回 null
     */
    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        int end = name.length();
        while (end > 0 && name.charAt(end - 1) == ' ') {
            end--;
        }
        return end == name.length() ? name : name.substring(0, end);
    }

}
