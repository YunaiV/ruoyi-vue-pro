package cn.iocoder.yudao.framework.mybatis.core.interceptor;

import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 按 JSqlParser 的 token 位置修改原 SQL 的工具类
 *
 * 只替换识别出的 token，不使用 JSqlParser 重新生成整条 SQL，保留原有的字符串、注释、提示与格式
 *
 * @author 芋道源码
 */
@UtilityClass
class SqlTokenUtils {

    /**
     * 解析 SQL，保留 token 位置
     *
     * 注意：不使用 {@link JsqlParserGlobal#parse(String)}，其缓存返回反序列化后的语句，不包含 token 位置，也不能被修改
     *
     * @param sql SQL
     * @return 语句
     * @throws JSQLParserException 解析失败
     */
    static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, JsqlParserGlobal.getExecutorService(), null);
    }

    /**
     * 计算每行的起始位置，与 JavaCC 的行号规则一致：\r\n、\r、\n 均为换行
     *
     * @param sql SQL
     * @return 每行的起始位置，下标为行号减 1
     */
    static int[] lineStarts(String sql) {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\r' && i + 1 < sql.length() && sql.charAt(i + 1) == '\n') {
                i++;
                starts.add(i + 1);
            } else if (c == '\r' || c == '\n') {
                starts.add(i + 1);
            }
        }
        return starts.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 获得 token 在 SQL 中的起始位置，并校验原文与 token 一致
     *
     * @param sql        SQL
     * @param lineStarts {@link #lineStarts(String)} 的结果
     * @param token      token
     * @return 起始位置
     */
    static int begin(String sql, int[] lineStarts, Token token) {
        if (token.beginLine < 1 || token.beginLine > lineStarts.length) {
            throw new IllegalStateException("无法定位 SQL 中的 token：" + token.image);
        }
        // JavaCC 的列号从 1 开始，制表符按 1 列计算
        int offset = lineStarts[token.beginLine - 1] + token.beginColumn - 1;
        if (offset < 0 || !sql.startsWith(token.image, offset)) {
            throw new IllegalStateException("无法定位 SQL 中的 token：" + token.image);
        }
        return offset;
    }

    /**
     * 获得 token 在 SQL 中的结束位置（不含）
     *
     * @param sql        SQL
     * @param lineStarts {@link #lineStarts(String)} 的结果
     * @param token      token
     * @return 结束位置
     */
    static int end(String sql, int[] lineStarts, Token token) {
        return begin(sql, lineStarts, token) + token.image.length();
    }

    /**
     * 按位置替换 SQL；相同位置的相同替换只执行一次，其它重叠的替换视为错误
     *
     * @param sql     SQL
     * @param patches 替换
     * @return 替换后的 SQL
     */
    static String apply(String sql, List<Patch> patches) {
        List<Patch> sorted = new ArrayList<>(patches);
        sorted.sort(Comparator.comparingInt(Patch::start).thenComparingInt(Patch::end));
        List<Patch> distinct = new ArrayList<>();
        for (Patch patch : sorted) {
            Patch last = distinct.isEmpty() ? null : distinct.get(distinct.size() - 1);
            if (patch.equals(last)) {
                continue;
            }
            if (last != null && patch.start() < last.end()) {
                throw new IllegalStateException("SQL 修改位置重叠：" + last + "、" + patch);
            }
            distinct.add(patch);
        }
        // 从后向前替换，前面的位置不受影响
        StringBuilder builder = new StringBuilder(sql);
        for (int i = distinct.size() - 1; i >= 0; i--) {
            Patch patch = distinct.get(i);
            builder.replace(patch.start(), patch.end(), patch.replacement());
        }
        return builder.toString();
    }

    /**
     * 替换：将 [start, end) 替换为 replacement；start 等于 end 时为插入
     */
    @Value
    @Accessors(fluent = true)
    static class Patch {

        /**
         * 起始位置
         */
        int start;
        /**
         * 结束位置（不含）
         */
        int end;
        /**
         * 替换的文本
         */
        String replacement;

    }

}
