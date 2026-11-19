package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.type.OracleClobTypeHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 日期统计、逗号分隔列表关联、JSON 数组匹配、JSON 字段取值与比较、行数限制、分批删除、HTML 标签去除、布尔判断、日期运算 SQL 片段，以及 LIKE 参数转义的方言工具类
 *
 * 1. 每次生成 SQL 时，按当前执行时路由到的数据源选择方言（按数据源实例缓存），不使用启动时固定的 databaseId 或主库
 * 2. 只有明确识别的 PostgreSQL（含人大金仓、openGauss、GaussDB、瀚高、MogDB 等兼容 PostgreSQL 的数据库）、Oracle、
 *    SQL Server 2012+ 输出新片段；MySQL、MariaDB、OceanBase、H2 保持原有 SQL 片段；达梦、神通单独识别，
 *    只有原有片段经实库确认不可用的片段单独处理，其它仍为原有片段，见 {@link Dialect#DM}、{@link Dialect#OSCAR}；
 *    SQL Server 2012 以下（原有的 DATE_FORMAT、LIMIT 等无法执行）与完全未识别的数据库抛出异常，见 {@link #resolveDialect(DatabaseMetaData)}。
 *    例外：JSON 数组匹配单独识别，只有 PostgreSQL 官方驱动、瀚高原生驱动、人大金仓、openGauss、GaussDB（含 MogDB）使用 PostgreSQL 片段，
 *    产品名为 PostgreSQL 的人大金仓原生驱动等仍保持原有片段，H2 使用严格的平面 Long 数组片段，见 {@link #resolveJsonDialect(DatabaseMetaData)}；
 *    JSON 字段取值与比较（{@link #jsonText(String, String)} 等）按 {@link #resolveJsonValueDialect(DatabaseMetaData)} 输出：兼容 PostgreSQL 的数据库在 PostgreSQL 兼容模式下
 *    没有 JSON_EXTRACT 等 MySQL JSON 函数，原有片段明知不可用，因此统一使用 jsonb 写法（openGauss、GaussDB、MogDB 待实库验收）；
 *    JSON 片段的识别结果不随普通片段的达梦、神通、SQL Server 2012 以下调整而变化；
 *    布尔判断、日期运算按数据库类型输出，
 *    见 {@link #isTrue(String)}、{@link #dateDecayScore(String)}、{@link #setMillisVersion(String)}
 * 3. 参数只能是代码中固定的列名，经过白名单校验；JSON 对象的键只能是校验格式后的模型字段编码或 Long 编号（见 {@link #JSON_KEY_PATTERN}）；
 *    格式均为常量，业务值必须继续使用 #{} 或 Wrapper 的编号参数绑定
 *
 * 在 XML 或注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@formatDate('create_time')}
 *
 * @author 芋道源码
 */
@SuppressWarnings("EnhancedSwitchMigration")
public class SqlDialectUtils {

    /**
     * SQL Server 2012 的主版本号
     */
    private static final int SQL_SERVER_2012_MAJOR_VERSION = 11;

    /**
     * 按原有 SQL 片段识别的数据库类型；普通片段再单独识别达梦、神通，拒绝 SQL Server 2012 以下，见 {@link #resolveDialect(DatabaseMetaData)}
     */
    private static final Set<DbType> ORIGINAL_DB_TYPES = EnumSet.of(DbType.MYSQL, DbType.MARIADB, DbType.OCEAN_BASE,
            DbType.H2, DbType.DM, DbType.OSCAR, DbType.SQL_SERVER2005);

    /**
     * 兼容 PostgreSQL、按 PostgreSQL 片段处理的数据库类型；openGauss、GaussDB 需要使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）创建的数据库
     */
    private static final Set<DbType> POSTGRESQL_VARIANT_DB_TYPES = EnumSet.of(DbType.KINGBASE_ES,
            DbType.OPENGAUSS, DbType.GAUSS_DB);

    /**
     * 兼容 PostgreSQL 的原生驱动 URL：瀚高、openGauss、GaussDB、MogDB、人大金仓；产品名为 PostgreSQL 或未登记时，按 PostgreSQL 片段处理
     *
     * 只匹配 URL 开头的 JDBC 协议（允许 p6spy 包装），库名、路径、参数、fragment 中出现的 :highgo: 等不参与识别
     */
    private static final Pattern POSTGRESQL_VARIANT_URL_PATTERN =
            Pattern.compile("^jdbc:(p6spy:)?(highgo|opengauss|gaussdb|mogdb|kingbase\\d*):");

    /**
     * MyBatis 参数名：参数或其属性路径，例如 limit、reqVO.limit
     */
    private static final Pattern PARAM_NAME_PATTERN =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    /**
     * 数据源与方言的缓存；弱引用数据源，动态移除的数据源可被回收
     */
    private static final Map<DataSource, Dialect> DIALECT_CACHE =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 数据源与 JSON 方言的缓存；与 {@link #DIALECT_CACHE} 分开，H2、产品名为 PostgreSQL 的人大金仓原生驱动等的 JSON 片段使用单独的写法
     */
    private static final Map<DataSource, Dialect> JSON_DIALECT_CACHE =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 数据源与 JSON 字段值方言的缓存；与 {@link #DIALECT_CACHE} 分开，普通片段单独识别神通、拒绝 SQL Server 2012 以下时，
     * JSON 字段值保持原有的识别结果
     */
    private static final Map<DataSource, Dialect> JSON_VALUE_DIALECT_CACHE =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 神通日期衰减得分的数值类型：NUMERIC 缺省标度为 0（0.5 转为 1），NUMBER 将 1E-300 转为 0，FLOAT8 的 POWER 下溢时报错；
     * NUMERIC(1000, 500) 可保留 Double 参数 1E-300，POWER(0.5, 2000) 仍为正数
     */
    private static final String OSCAR_SCORE_DECIMAL = "NUMERIC(1000, 500)";

    /**
     * MySQL DOUBLE 的下溢边界：ln(2^-1075) = -1075 * ln 2。衰减部分不大于 2^-1075 时，MySQL 的 POWER 舍入为 0，
     * 大于时至少为最小正数 2^-1074（Math.pow(0.5, 1075) 为 0，Math.pow(0.5, 1074.9999) 为 4.9E-324）
     */
    private static final String DOUBLE_UNDERFLOW_LN = "-745.1332191019412";

    /**
     * Double.MIN_NORMAL（2^-1022）：神通的 FLOAT8 不接受次正规数（4.9E-324 转换时报错），LN(NUMERIC) 内部也转为 FLOAT8，
     * 小于该值的衰减系数需先乘以 1E300 归一化再取对数
     */
    private static final String DOUBLE_MIN_NORMAL = "2.2250738585072014E-308";

    /**
     * ln(2^1074) = 1074 * ln 2：衰减部分除以 Double 最小单位 2^-1074 的比值为 EXP(对数积 + 该值)
     */
    private static final String DOUBLE_MIN_UNIT_SHIFT_LN = "744.4400719213812";

    /**
     * 神通日期衰减按 Double 最小单位舍入的对数积边界：EXP(-708) 约 3.3E-308，为 FLOAT8 可表示的正规数，且小于 2^-1021，
     * 对数积小于该值时 Double 的间隔与次正规数相同，均为 2^-1074
     */
    private static final int OSCAR_UNIT_GRID_LN = -708;

    /**
     * 2^52：比值不大于 2^52 时，FLOAT8 的 (比值 + 2^52) - 2^52 按 IEEE 就近舍入为整数，平分时取偶数（神通实库 2.5 为 2）；
     * CAST(FLOAT8 AS BIGINT) 平分时远离零（2.5 为 3），不用于舍入
     */
    private static final String DOUBLE_TIES_EVEN_SHIFT = "4503599627370496";

    /**
     * 瀚高原生驱动 URL 的前缀
     */
    private static final String HIGHGO_URL_PREFIX = "jdbc:highgo:";

    /**
     * 仅 JSON 片段按 PostgreSQL 处理的数据库类型：人大金仓，以及使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）的 openGauss、GaussDB；
     * MogDB 使用 openGauss 原生驱动时，产品名同为 openGauss
     */
    private static final Set<DbType> JSON_POSTGRESQL_DB_TYPES = EnumSet.of(DbType.KINGBASE_ES,
            DbType.OPENGAUSS, DbType.GAUSS_DB);

    /**
     * openGauss、GaussDB、MogDB 原生驱动 URL 的前缀；产品名为 PostgreSQL 或未登记时，仅 JSON 片段按 PostgreSQL 处理
     */
    private static final String[] OPENGAUSS_URL_PREFIXES = {"jdbc:opengauss:", "jdbc:gaussdb:", "jdbc:mogdb:"};

    /**
     * Oracle 平面 Long / null 数组的整体格式，TAB、LF、CR 需先替换为空格：
     * 元素为 0、不含前导零的 1~19 位整数或小写 null，元素两侧可有空格；字符串、嵌套数组、对象、小数、指数、空元素均不匹配
     */
    static final String ORACLE_LONG_ARRAY_PATTERN =
            "^ *\\[ *((0|-?[1-9][0-9]{0,18}|null) *(, *(0|-?[1-9][0-9]{0,18}|null) *)*)?\\] *$";

    /**
     * H2 平面 Long / null 数组的整体格式，规则同 {@link #ORACLE_LONG_ARRAY_PATTERN}
     *
     * 注意：H2 的 REGEXP_LIKE 使用 Java 正则，重复的捕获组按递归匹配，长数组（例如 1400 个元素）会栈溢出，
     * 因此使用占有量词（*+）与非捕获组；Oracle 的 POSIX 正则不支持该语法，两者不能混用
     */
    static final String H2_LONG_ARRAY_PATTERN =
            "^ *+\\[ *+(?:(?:0|-?[1-9][0-9]{0,18}|null) *+(?:, *+(?:0|-?[1-9][0-9]{0,18}|null) *+)*+)?\\] *+$";

    /**
     * 超出 Long 范围的 19 位整数：负数超过 -9223372036854775808，或正数超过 9223372036854775807
     *
     * 只使用分组、字符类与固定次数，Oracle 的 POSIX 正则与 H2 的 Java 正则语义一致，两者共用
     */
    static final String LONG_OVERFLOW_PATTERN = "(^|[^0-9])" + greaterDigitsPattern("9223372036854775808")
            + "|(^|[^-0-9])9223372036854775808";

    /**
     * SQL Server 校验整个 XML（&lt;r&gt;&lt;i&gt;元素&lt;/i&gt;...&lt;/r&gt;）为平面 Long / null 数组的 XQuery：
     * 存在元素，且每个元素去掉两侧空白后非空，并且为 null，或转为 xs:long 再转回文本与原文完全相同（拒绝前导零、溢出等）。
     * 转换失败时 cast as xs:long? 返回空序列，string() 后为空串，与原文不等，因此非法元素会使整体不匹配
     */
    private static final String SQL_SERVER_LONG_ARRAY_XQUERY = "/r[i and not(i[string-length(string(xs:token(string(.)))) = 0"
            + " or (string(xs:token(string(.))) != \"null\""
            + " and string(xs:token(string(.))) != string((string(xs:token(string(.))) cast as xs:long?)))])]";

    /**
     * JSON 对象的键：模型字段编码（与 CMS 模型字段编码的校验一致：字母或下划线开头，只含字母、数字、下划线，最长 64 位），
     * 或 Long 编号的十进制文本（例如问卷题目编号）。键会拼接为 JSON 路径字面量，只允许这两种格式
     */
    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,63}|-?[0-9]{1,19}");

    /**
     * 可比较的 JSON 数字文本：十进制数，或尾数只有一位整数（或以小数点开头）、指数最多两位的科学计数法，
     * 例如 -5、99.99、.5、1.0E+7（BigDecimal 负标度的序列化结果）、1e-7；JSON 数字与数字字符串均按此格式识别
     *
     * 配合长度不超过 {@link #JSON_NUMBER_MAX_LENGTH}，绝对值小于 1E+100，各数据库转换时不会溢出报错（Oracle NUMBER 上限约 1E+126）。
     * 只使用分组、字符类与 ?、+ 量词，MySQL、PostgreSQL、Oracle 的正则语义一致；不含 {n} 量词，避免被 Wrapper 当作编号参数
     */
    static final String JSON_NUMBER_PATTERN =
            "^-?([0-9]+([.][0-9]+)?|[.][0-9]+|([0-9]([.][0-9]+)?|[.][0-9]+)[eE][-+]?[0-9][0-9]?)$";

    /**
     * JSON 数字文本的最大长度
     */
    private static final int JSON_NUMBER_MAX_LENGTH = 64;

    /**
     * JSON 数组参数中数字按十进制文本写入的最大长度，与 CMS 字典项值的最大长度一致；超过时写为科学计数法
     */
    private static final int JSON_NUMBER_PLAIN_MAX_LENGTH = 100;

    /**
     * 可比较的 JSON 日期时间文本：yyyy-MM-dd，可带 [ T]HH:mm、:ss 与 1~9 位小数秒；年份 0001~9999，按公历校验月份天数与闰年 2 月 29 日，
     * 时 00~23、分秒 00~59；不接受时区偏移（例如 +08:00、Z）
     *
     * 格式与日历均在正则中校验，转换前即可排除 2026-02-30、25:00 等非法值，PostgreSQL、Oracle 的转换不会报错；
     * 只使用分组、字符类与 ? 量词，不含 {n} 量词，长度小于 Oracle 正则的 512 字节上限
     */
    static final String JSON_DATE_TIME_PATTERN = "^("
            // 年-月-日：大月 31 天、小月 30 天、2 月 28 天
            + "([1-9][0-9][0-9][0-9]|0[1-9][0-9][0-9]|00[1-9][0-9]|000[1-9])"
            + "-((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01])|(0[469]|11)-(0[1-9]|[12][0-9]|30)|02-(0[1-9]|1[0-9]|2[0-8]))"
            // 闰年 2 月 29 日：能被 4 整除且不能被 100 整除，或能被 400 整除，不含 0000 年
            + "|([0-9][0-9](0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[13579][26])00)-02-29)"
            + "([ T]([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9]([.][0-9][0-9]?[0-9]?[0-9]?[0-9]?[0-9]?[0-9]?[0-9]?[0-9]?)?)?)?$";

    /**
     * 达梦可转换为 DECIMAL(30, 10) 的 JSON 数字文本：在 {@link #JSON_NUMBER_PATTERN} 的基础上，十进制数的整数部分最多 20 位，
     * 科学计数法的正指数最多为 19（尾数只有一位整数），即绝对值小于 1E+20。
     * 达梦转换溢出时报错（MySQL 截断为最大值），超出范围的值按非法值处理为 NULL；同样不含 {n} 量词
     */
    static final String DM_NUMBER_PATTERN = "^-?([0-9]" + "[0-9]?".repeat(19) + "([.][0-9]+)?|[.][0-9]+"
            + "|([0-9]([.][0-9]+)?|[.][0-9]+)[eE]([-][0-9][0-9]?|[+]?1?[0-9]))$";

    /**
     * 达梦 DECIMAL(30, 10) 的范围：绝对值小于 1E+20，使用整数字面量，比较不经过浮点数
     */
    private static final String DM_NUMBER_LIMIT = "100000000000000000000";

    /**
     * 问答文本的空白字符：Unicode White_Space 的 25 个字符（含空格），与 MySQL 8 的 ICU [[:space:]] 一致；
     * SQL Server、PostgreSQL、达梦直接写入字面量的字符类，各数据库口径相同，不依赖 locale 的 POSIX [:space:]
     *
     * 直接写入字面量：JSqlParser 5.4 将 CHAR(n)、NCHAR(n) 解析为数据类型的隐式转换，不能拼接函数调用；
     * 字符中没有单引号、反斜杠与字符类的特殊字符（]、^、-），无需转义。按码位生成，源码中不出现不可见字符与 U+2028、U+2029
     */
    private static final String JSON_WHITE_SPACES = new String(new int[]{
            0x20, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x85, 0xA0, 0x1680,
            0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
            0x2028, 0x2029, 0x202F, 0x205F, 0x3000}, 0, 25);

    /**
     * Oracle JSON 路径中的空白字符：同 {@link #JSON_WHITE_SPACES}，空格以外的字符写为 JSON 字符串的 Unicode 转义
     * （反斜杠、u 与 4 位十六进制），路径字符串的控制字符必须转义，SQL 文本也只含 ASCII；
     * 转义在 JSON 路径中还原为字符后再按 like_regex 匹配，正则为 25 个字符，远小于 512 字节上限
     */
    private static final String ORACLE_JSON_WHITE_SPACES = JSON_WHITE_SPACES.codePoints()
            .mapToObj(codePoint -> codePoint == ' ' ? " " : "\\" + 'u' + String.format("%04X", codePoint))
            .collect(Collectors.joining());

    /**
     * Oracle JSON_TABLE 的列：t 为元素的 type()（string、number、boolean、null、array、object），
     * v 为 VARCHAR2(4000) 文本（超过 4000 字节、数组与对象为 NULL），r 为 FORMAT JSON WITH WRAPPER 的原文（例如 [1.00E0]），
     * 数字按 r 的原文比较，不经过 NUMBER，见 {@link #oracleJsonNumberKey(String)}
     */
    private static final String ORACLE_JSON_TABLE_COLUMNS = "t VARCHAR2(16) PATH '$.type()', v VARCHAR2(4000) PATH '$',"
            + " r VARCHAR2(4000) FORMAT JSON WITH WRAPPER PATH '$'";

    /**
     * Oracle 包装为数组的 JSON 数字原文，例如 [-12.50e+3]：分组 1 为符号，2 为整数部分，4 为小数部分，6 为指数（可带符号）；
     * 只用于 type() 为 number 的元素，整数与小数部分不会同时为空；整数部分为空（例如 [.5]）时按 0 位整数计算。
     * 方括号用反斜杠转义（Oracle 与 Java 正则语义一致），只使用分组、字符类与 ?、*、+ 量词，不含 {n} 量词，避免被 Wrapper 当作编号参数
     */
    static final String ORACLE_JSON_NUMBER_RAW_PATTERN = "^\\[ *(-?)([0-9]*)([.]([0-9]+))?([eE]([-+]?[0-9]+))? *\\]$";

    /**
     * 方言
     */
    enum Dialect {

        /**
         * 原有 SQL 片段
         */
        ORIGINAL,
        POSTGRESQL,
        ORACLE,
        SQLSERVER,
        /**
         * H2：仅 JSON 片段使用，见 {@link #resolveJsonDialect(DatabaseMetaData)}；其它片段的 H2 仍为 {@link #ORIGINAL}
         */
        H2,
        /**
         * 达梦：JSON 字段值（见 {@link #resolveJsonValueDialect(DatabaseMetaData)}）与普通片段使用，JSON 数组匹配仍为 {@link #ORIGINAL}；
         * 普通片段只单独处理 {@link #findInSetColumn(String, String)}（JOIN ON 中按 &lt;&gt; 0 判断），其它与原有片段相同；
         * 达梦 8.1.3 实库中，原有的 DATE_FORMAT、CAST AS DATE、YEAR、MONTH、DAYOFMONTH、HOUR、TIMESTAMPDIFF、TIMESTAMPADD、
         * TRUNCATE、REGEXP_REPLACE、LIMIT（含绑定参数）、DELETE ... LIMIT、LIKE ... ESCAPE 均可执行
         */
        DM,
        /**
         * 神通：仅普通片段使用。没有 DATE_FORMAT、TIMESTAMPDIFF、FIND_IN_SET、DAY 函数，CAST AS DATE 保留时分秒，DELETE 不支持 LIMIT，
         * 这些片段单独处理；HOUR、无 FROM 的 SELECT、SELECT 的 LIMIT（含 0 与绑定参数）、REGEXP_REPLACE（三参数默认全局替换）与原有片段相同。
         * JSON 片段仍为 {@link #ORIGINAL}（未验收）。
         * 本组原生验证的是神通 7.0.14、oscarJDBC8-4.1.152（元数据中的驱动版本为 7.20），其它版本、兼容模式仍需验收
         */
        OSCAR

    }

    // ========== 日期字符串 ==========

    /**
     * 按天格式化为 yyyy-MM-dd 字符串；原有片段为 DATE_FORMAT(column, '%Y-%m-%d')
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String formatDate(String column) {
        return formatDate(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyy-MM-dd 字符串，保留各分支原有格式。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatDate(Dialect dialect, String column) {
        return formatDatePattern(dialect, column, "YYYY-MM-DD", 23);
    }

    /**
     * 按月格式化为 yyyyMM 字符串；原有片段为 DATE_FORMAT(column, '%Y%m')
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String formatYearMonth(String column) {
        return formatYearMonth(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyyMM 字符串，供 CRM 按月统计使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatYearMonth(Dialect dialect, String column) {
        return formatDatePattern(dialect, column, "YYYYMM", 112);
    }

    /**
     * 按月格式化为 yyyy-MM 字符串；原有片段为 DATE_FORMAT(column, '%Y-%m')，与 {@link #formatYearMonth(String)} 同族，月份补零，列为 NULL 时为 NULL
     *
     * 注意：此前的 CONCAT(EXTRACT(YEAR ...), '-', LPAD(CONCAT(EXTRACT(MONTH ...)), 2, '0')) 中，单参数的 CONCAT 在达梦无法解析
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String formatYearDashMonth(String column) {
        return formatYearDashMonth(getDialect(), column);
    }

    /**
     * 按指定方言生成 yyyy-MM 字符串，供交易统计月份标签使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String formatYearDashMonth(Dialect dialect, String column) {
        return formatDatePattern(dialect, column, "YYYY-MM", 120);
    }

    /**
     * 按日期格式生成日期字符串：PostgreSQL、Oracle、神通直接 TO_CHAR；SQL Server 按样式 CONVERT，并截取为格式的长度；
     * 原有片段将格式中的 YYYY、MM、DD 换为 DATE_FORMAT 的 %Y、%m、%d
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @param pattern 由 YYYY、MM、DD 与分隔符组成的格式，例如 YYYY-MM-DD
     * @param sqlServerStyle SQL Server CONVERT 的样式，其结果截取格式的长度后与格式一致
     * @return SQL 片段
     */
    private static String formatDatePattern(Dialect dialect, String column, String pattern, int sqlServerStyle) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
            case ORACLE:
            case OSCAR:
                return "TO_CHAR(" + column + ", '" + pattern + "')";
            case SQLSERVER:
                return "CONVERT(varchar(" + pattern.length() + "), " + column + ", " + sqlServerStyle + ")";
            default:
                return "DATE_FORMAT(" + column + ", '"
                        + pattern.replace("YYYY", "%Y").replace("MM", "%m").replace("DD", "%d") + "')";
        }
    }

    /**
     * 按天分组；原有片段为 CAST(column AS DATE)，新方言输出 yyyy-MM-dd 字符串
     *
     * 适用于同时兼容日期值与 yyyy-MM-dd 字符串的消费者
     *
     * 注意：此前原有片段为 DATE(column)，达梦不支持 DATE 函数；MySQL 中两者等价
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String date(String column) {
        return date(getDialect(), column);
    }

    /**
     * 生成按天分组表达式；与 {@link #castDateText(Dialect, String)} 相同，原路径为 CAST AS DATE，新分支返回日期文本。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String date(Dialect dialect, String column) {
        return castDateText(dialect, column);
    }

    /**
     * 按天分组；原有片段为 CAST(column AS DATE)，新方言输出 yyyy-MM-dd 字符串
     *
     * 适用于 String 类型的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String castDateText(String column) {
        return castDateText(getDialect(), column);
    }

    /**
     * 生成 String 日期消费者所需的转换表达式，保留原路径 CAST AS DATE。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String castDateText(Dialect dialect, String column) {
        checkColumn(column);
        if (isOriginal(dialect)) {
            return "CAST(" + column + " AS DATE)";
        }
        return formatDate(dialect, column);
    }

    // ========== 日期值 ==========

    /**
     * 按天截断为日期值；原有片段为 CAST(column AS DATE)
     *
     * 适用于 LocalDate 类型的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String castDate(String column) {
        return castDate(getDialect(), column);
    }

    /**
     * 生成 LocalDate 消费者所需的日期值；Oracle 用 TRUNC 去掉时分秒。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String castDate(Dialect dialect, String column) {
        checkColumn(column);
        if (dialect == Dialect.ORACLE || dialect == Dialect.OSCAR) {
            // Oracle、神通的 DATE 包含时分秒，CAST AS DATE 不会截断
            return "TRUNC(" + column + ")";
        }
        return "CAST(" + column + " AS DATE)";
    }

    /**
     * 按天截断为当天零点的日期时间值；原有片段为 CAST(column AS DATE)
     *
     * 适用于 LocalDateTime 类型的消费者
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String dayStart(String column) {
        return dayStart(getDialect(), column);
    }

    /**
     * 生成 LocalDateTime 消费者所需的当天零点：新方言截断后再转为时间戳类型，避免驱动无法将 DATE 映射为 LocalDateTime；
     * 原有路径沿用 CAST AS DATE，由原有驱动处理映射。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 片段
     */
    static String dayStart(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "CAST(CAST(" + column + " AS DATE) AS TIMESTAMP)";
            case ORACLE:
            case OSCAR:
                return "CAST(TRUNC(" + column + ") AS TIMESTAMP)";
            case SQLSERVER:
                return "CAST(CAST(" + column + " AS DATE) AS datetime2)";
            default:
                return "CAST(" + column + " AS DATE)";
        }
    }

    // ========== 日期部件 ==========

    /**
     * 月份（1-12）；原有片段为 MONTH(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String month(String column) {
        return month(getDialect(), column);
    }

    /**
     * 按指定方言提取月份。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 月份 SQL 表达式
     */
    static String month(Dialect dialect, String column) {
        return datePart(dialect, column, "MONTH");
    }

    /**
     * 年份；原有片段为 YEAR(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String year(String column) {
        return year(getDialect(), column);
    }

    /**
     * 按指定方言提取年份，供 CRM 业绩目标筛选使用。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 年份 SQL 表达式
     */
    static String year(Dialect dialect, String column) {
        return datePart(dialect, column, "YEAR");
    }

    /**
     * 日（1-31）；原有片段为 DAYOFMONTH(column)
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String dayOfMonth(String column) {
        return dayOfMonth(getDialect(), column);
    }

    /**
     * 按指定方言提取月内日，原路径保留 DAYOFMONTH。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 日 SQL 表达式
     */
    static String dayOfMonth(Dialect dialect, String column) {
        checkColumn(column);
        if (isOriginal(dialect)) {
            return "DAYOFMONTH(" + column + ")";
        }
        return datePart(dialect, column, "DAY");
    }

    /**
     * 小时（0-23）；原有片段为 HOUR(column)
     *
     * 仅适用于不带时区的日期时间列（LocalDateTime 契约）
     *
     * @param column 列名
     * @return SQL 片段
     */
    public static String hour(String column) {
        return hour(getDialect(), column);
    }

    /**
     * 按指定方言提取小时：Oracle 的 DATE 不支持 EXTRACT(HOUR)，需先转为 TIMESTAMP(9)，避免默认精度 6 将小数秒进位到下一小时；
     * SQL Server 没有 HOUR 函数。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return 小时 SQL 表达式
     */
    static String hour(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "CAST(EXTRACT(HOUR FROM " + column + ") AS INTEGER)";
            case ORACLE:
                return "EXTRACT(HOUR FROM CAST(" + column + " AS TIMESTAMP(9)))";
            case SQLSERVER:
                return "DATEPART(HOUR, " + column + ")";
            default:
                return "HOUR(" + column + ")";
        }
    }

    /**
     * 日期部件：PostgreSQL 的 EXTRACT 返回 numeric，需要转为整数；神通没有 DAY 函数，与 Oracle 一致使用 EXTRACT；
     * 原有片段与 SQL Server 均为 field(column)
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @param field 代码中固定的 YEAR、MONTH 或 DAY
     * @return 日期部件 SQL 表达式
     */
    private static String datePart(Dialect dialect, String column, String field) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "CAST(EXTRACT(" + field + " FROM " + column + ") AS INTEGER)";
            case ORACLE:
            case OSCAR:
                return "EXTRACT(" + field + " FROM " + column + ")";
            default:
                return field + "(" + column + ")";
        }
    }

    // ========== 聚合 ==========

    /**
     * 平均成交周期（天）：两个时间之间的完整天数（向零截断）求平均，保留 1 位小数（截断），无数据时为 0
     *
     * 原有片段为 COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, begin, end)), 1), 0)。
     * 仅适用于不带时区的日期时间列（LocalDateTime 契约）；带时区的 TIMESTAMP WITH TIME ZONE、datetimeoffset 不在适用范围内
     *
     * 1. Oracle：两列均升格为 TIMESTAMP(9)，DATE、TIMESTAMP(0~9) 均不丢失小数秒，INTERVAL 的 DAY 部分即向零截断的完整天数
     * 2. SQL Server 2012+：DATEDIFF(DAY) 按日期边界计数，按时刻修正为完整天数；先转 datetime2(7) 再取 time(7)，
     *    兼容 date、datetime、datetime2；AVG 使用 DECIMAL 避免整数平均，ROUND(..., 1, 1) 为截断
     * 3. 神通：没有 TIMESTAMPDIFF，INTERVAL 不支持 EXTRACT；同 SQL Server 按日期差、time(6) 的时刻修正为完整天数，
     *    日期差取 TRUNC 后两个零点之间的 DATEDIFF('DAY')，与其按边界还是按完整天数计数无关；AVG 使用 NUMBER
     *    （NUMERIC 缺省标度为 0），TRUNC(..., 1) 为截断
     *
     * @param beginColumn 开始时间列名
     * @param endColumn   结束时间列名
     * @return SQL 片段
     */
    public static String customerDealCycle(String beginColumn, String endColumn) {
        return customerDealCycle(getDialect(), beginColumn, endColumn);
    }

    /**
     * 按指定方言生成平均完整天数；原有路径继续使用原表达式。
     *
     * @param dialect 已识别的数据库方言
     * @param beginColumn 代码中固定的开始时间列名
     * @param endColumn 代码中固定的结束时间列名
     * @return 平均成交周期 SQL 表达式
     */
    static String customerDealCycle(Dialect dialect, String beginColumn, String endColumn) {
        checkColumn(beginColumn);
        checkColumn(endColumn);
        switch (dialect) {
            case POSTGRESQL:
                return "COALESCE(TRUNC(AVG(CAST(EXTRACT(DAY FROM (CAST(" + endColumn + " AS TIMESTAMP) - CAST("
                        + beginColumn + " AS TIMESTAMP))) AS NUMERIC)), 1), 0)";
            case ORACLE:
                return "COALESCE(TRUNC(AVG(EXTRACT(DAY FROM (CAST(" + endColumn + " AS TIMESTAMP(9)) - CAST("
                        + beginColumn + " AS TIMESTAMP(9))))), 1), 0)";
            case SQLSERVER: {
                String beginTime = "CAST(CAST(" + beginColumn + " AS datetime2(7)) AS time(7))";
                String endTime = "CAST(CAST(" + endColumn + " AS datetime2(7)) AS time(7))";
                String days = "DATEDIFF(DAY, " + beginColumn + ", " + endColumn + ")";
                // 正向时，结束时刻早于开始时刻说明最后一天不完整；反向时同理，均向零修正 1 天
                return "COALESCE(ROUND(AVG(CAST((CASE WHEN " + endColumn + " >= " + beginColumn
                        + " THEN " + days + " - CASE WHEN " + endTime + " < " + beginTime + " THEN 1 ELSE 0 END"
                        + " ELSE " + days + " + CASE WHEN " + endTime + " > " + beginTime + " THEN 1 ELSE 0 END END)"
                        + " AS DECIMAL(19, 4))), 1, 1), 0)";
            }
            case OSCAR: {
                String beginTime = "CAST(" + beginColumn + " AS TIME(6))";
                String endTime = "CAST(" + endColumn + " AS TIME(6))";
                String days = "DATEDIFF('DAY', TRUNC(" + beginColumn + "), TRUNC(" + endColumn + "))";
                return "COALESCE(TRUNC(AVG(CAST((CASE WHEN " + endColumn + " >= " + beginColumn
                        + " THEN " + days + " - CASE WHEN " + endTime + " < " + beginTime + " THEN 1 ELSE 0 END"
                        + " ELSE " + days + " + CASE WHEN " + endTime + " > " + beginTime + " THEN 1 ELSE 0 END END)"
                        + " AS NUMBER)), 1), 0)";
            }
            default:
                return "COALESCE(TRUNCATE(AVG(TIMESTAMPDIFF(DAY, " + beginColumn + ", " + endColumn + ")), 1), 0)";
        }
    }

    // ========== 其它 ==========

    /**
     * 顶层 SELECT 无 FROM 时的补充：Oracle 为 FROM DUAL，其它为空
     *
     * @return SQL 片段
     */
    public static String fromDual() {
        return fromDual(getDialect());
    }

    /**
     * 为无顶层 FROM 的标量汇总补充 Oracle DUAL，其他分支保持空文本。
     *
     * @param dialect 已识别的数据库方言
     * @return Oracle 为 FROM DUAL，其他为空字符串
     */
    static String fromDual(Dialect dialect) {
        return dialect == Dialect.ORACLE ? "FROM DUAL" : "";
    }

    // TODO @AI：后续 creator 和 updater 改成 Long 后，移除用户编号转换及相关适配。
    /**
     * 创建者、更新者等文本列中的用户编号，用于与用户主键等值关联，例如 u.id = auditUserId("t.creator")
     *
     * 1. 只转换全为数字的文本，前导零按数值匹配（0030 为 30）；NULL、空串、含非数字字符时为 NULL，不与任何用户匹配
     * 2. 结果与主键同为整数类型，主键保持裸列比较，可按主键查找：PostgreSQL、SQL Server、达梦、神通为 BIGINT，
     *    Oracle 为 NUMBER(20)；原有片段为 DECIMAL(20, 0)（MySQL 没有 CAST AS BIGINT，H2 的 SIGNED 为 INT）
     * 3. CAST 写在 CASE 之外，CASE 只返回校验后的文本：达梦会提前计算 CASE 分支中的 CAST
     * 4. 超出 BIGINT 的数字文本不在支持范围：PostgreSQL、达梦、神通、H2 及 Oracle（超过 20 位）报错，其它不匹配；
     *    SQL Server 的 TRY_CAST 允许首尾空格与正负号，例如 ' 30'、'+30' 匹配 30
     *
     * @param column 代码中固定的文本列名，例如 t.creator
     * @return 用户编号的 SQL 片段，非用户编号时为 NULL
     */
    public static String auditUserId(String column) {
        return auditUserId(getDialect(), column);
    }

    /**
     * 按指定方言生成文本列中的用户编号
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的文本列名
     * @return 用户编号的 SQL 片段
     */
    static String auditUserId(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                // PostgreSQL 的 $ 只匹配文本末尾；转换为 NUMERIC 与 BIGINT 主键比较时不能按主键查找
                return "CAST(CASE WHEN " + column + " ~ '^[0-9]+$' THEN " + column + " END AS BIGINT)";
            case ORACLE:
                // Oracle 的空串即 NULL
                return "CAST(CASE WHEN NOT REGEXP_LIKE(" + column + ", '[^0-9]') THEN " + column + " END AS NUMBER(20))";
            case SQLSERVER:
                // TRY_CAST 的空串为 0，先转为 NULL；比较时忽略尾部空格，空白串同样为 NULL
                return "TRY_CAST(NULLIF(" + column + ", '') AS BIGINT)";
            case DM:
            case OSCAR:
                return "CAST(CASE WHEN NOT REGEXP_LIKE(" + column + ", '[^0-9]') THEN NULLIF(" + column + ", '') END AS BIGINT)";
            default:
                // 按“不含非数字”判断，避免 MySQL 的 $ 匹配末尾换行之前的位置
                return "CAST(CASE WHEN " + column + " NOT REGEXP '[^0-9]' THEN NULLIF(" + column + ", '') END AS DECIMAL(20, 0))";
        }
    }

    // ========== 逗号分隔列表 ==========

    /**
     * 两列之间的 FIND_IN_SET 关联条件：数值 ID 列的值是否为逗号分隔列表列中的一个元素；原有片段为 FIND_IN_SET(valueColumn, listColumn)
     *
     * 1. 列表按英文逗号分隔、元素两侧不含空格，与 FIND_IN_SET 一致；元素重复时，同一行仍只匹配一次
     * 2. 任一列为 NULL 时不匹配
     * 3. 原有片段在 MySQL 的 ON / WHERE 中按非 0 为真；达梦不把数值当作条件，与 {@link DbTypeEnum#DM} 的模板一致追加 &lt;&gt; 0；
     *    神通没有 FIND_IN_SET 函数，使用 POSITION，ID 转为 TEXT 拼接（不指定长度的 VARCHAR 为 VARCHAR(1)，大编号溢出报错）
     *
     * 列与参数值比较时，使用 {@link MyBatisUtils#findInSet(String, java.util.Collection)}
     *
     * @param valueColumn 数值类型的 ID 列名，例如 cc.id、contact_id
     * @param listColumn  逗号分隔的列表列名，例如 cfur.contact_ids
     * @return SQL 条件片段
     */
    public static String findInSetColumn(String valueColumn, String listColumn) {
        return findInSetColumn(getDialect(), valueColumn, listColumn);
    }

    /**
     * 按指定方言生成两列之间的 FIND_IN_SET 关联条件；两侧补充逗号，避免 1 匹配到 11、21 等相邻 ID。
     *
     * @param dialect 已识别的数据库方言
     * @param valueColumn 代码中固定的数值 ID 列名，SQL Server 按 varchar(20) 转换，可容纳 bigint
     * @param listColumn 代码中固定的逗号分隔列表列名
     * @return SQL 条件片段
     */
    static String findInSetColumn(Dialect dialect, String valueColumn, String listColumn) {
        checkIdColumn(valueColumn);
        checkColumn(listColumn);
        switch (dialect) {
            case POSTGRESQL:
                return "POSITION(',' || CAST(" + valueColumn + " AS VARCHAR) || ',' IN ',' || " + listColumn + " || ',') > 0";
            case ORACLE:
                // Oracle 拼接时 NULL 按空串处理，需要排除 NULL，否则会匹配到列表中的空元素
                return "(" + valueColumn + " IS NOT NULL AND INSTR(',' || " + listColumn + " || ',', ',' || "
                        + valueColumn + " || ',') > 0)";
            case SQLSERVER:
                // SQL Server 2012–2016 可关闭 CONCAT_NULL_YIELDS_NULL，显式排除 NULL ID，避免匹配列表中的空元素
                return "(" + valueColumn + " IS NOT NULL AND CHARINDEX(',' + CAST(" + valueColumn
                        + " AS varchar(20)) + ',', ',' + " + listColumn + " + ',') > 0)";
            case DM:
                return "FIND_IN_SET(" + valueColumn + ", " + listColumn + ") <> 0";
            case OSCAR:
                // 拼接 NULL 的结果未验收，与 Oracle 一致显式排除 NULL ID，避免匹配列表中的空元素
                return "(" + valueColumn + " IS NOT NULL AND POSITION(',' || CAST(" + valueColumn + " AS TEXT) || ',' IN ',' || "
                        + listColumn + " || ',') > 0)";
            default:
                return "FIND_IN_SET(" + valueColumn + ", " + listColumn + ")";
        }
    }

    // ========== JSON 数组 ==========

    /**
     * JSON 数组列是否包含 {0} 绑定的数值 ID，供 Wrapper 的 apply 使用，ID 必须作为 apply 的参数绑定
     *
     * 1. 原有片段：wrapArray 为 false 时为 JSON_CONTAINS(column, {0})，参数为 ID 的数字文本；
     *    为 true 时为 JSON_CONTAINS(column, JSON_ARRAY({0}))，参数为 Long
     * 2. PostgreSQL、瀚高（原生驱动 jdbc:highgo:）、人大金仓、openGauss、GaussDB（含 MogDB）：
     *    CAST(column AS jsonb) @&gt; CAST(CONCAT('[', COALESCE(CAST(CAST({0} AS BIGINT) AS TEXT), 'null'), ']') AS jsonb)，
     *    兼容 jsonb、json 与 JSON 文本列；参数先转为 BIGINT（非整数文本报错），再拼成单元素 JSON 数组，按数字匹配，
     *    数组中的字符串 "1" 不匹配 1，超出 2^53 的相邻编号不会互相匹配；
     *    参数为 null 时拼成 [null]，匹配数组中的 null 元素，与原有 JSON_CONTAINS(column, JSON_ARRAY(NULL)) 一致；列为 NULL 时不匹配。
     *    不使用 jsonb_build_array：openGauss、GaussDB 没有该函数；不使用 || 拼接：openGauss 的 A、B 兼容模式下，
     *    || 会忽略 NULL 或作为逻辑或，因此使用 CONCAT，并由 COALESCE 保证拼接的值不为 NULL
     * 3. Oracle、SQL Server 2012+、H2：仅支持平面 Long / null 数组（应用以 List&lt;Long&gt; 写入的数据），元素两侧允许空白；
     *    整个数组含字符串、嵌套数组、对象、小数、空元素、超出 Long 范围等非法元素时整体不匹配，不等价于通用 JSON_CONTAINS。
     *    参数为 null 时匹配数组中的 null 元素，与原有 JSON_CONTAINS(column, JSON_ARRAY(NULL)) 一致；列为 NULL 时不匹配
     * 4. 达梦、神通等其它数据库保持原有片段
     *
     * @param column    JSON 数组列名，元素为数值 ID
     * @param wrapArray 原有片段是否使用 JSON_ARRAY 包装参数
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    public static String jsonArrayContains(String column, boolean wrapArray) {
        return jsonArrayContains(getJsonDialect(), column, wrapArray);
    }

    /**
     * 按指定方言生成 JSON 数组包含数值 ID 的条件；原有路径保持 JSON_CONTAINS 片段，{0} 在片段中只出现一次。
     *
     * @param dialect 已识别的 JSON 方言
     * @param column 代码中固定的 JSON 数组列名
     * @param wrapArray 原有片段是否使用 JSON_ARRAY 包装参数
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    static String jsonArrayContains(Dialect dialect, String column, boolean wrapArray) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "CAST(" + column + " AS jsonb) @> CAST(CONCAT('[', COALESCE(CAST(CAST({0} AS BIGINT) AS TEXT), 'null'), ']') AS jsonb)";
            case ORACLE:
                // 数值按 TM9 格式与固定小数点字符转为文本，不受 NLS 影响
                return longArrayContains(column, ORACLE_LONG_ARRAY_PATTERN,
                        "TO_CHAR(CAST({0} AS NUMBER(19, 0)), 'TM9', 'NLS_NUMERIC_CHARACTERS = ''.,''')");
            case SQLSERVER:
                return sqlServerJsonLongArrayContains(column);
            case H2:
                return longArrayContains(column, H2_LONG_ARRAY_PATTERN, "CAST(CAST({0} AS BIGINT) AS VARCHAR)");
            default:
                return wrapArray ? "JSON_CONTAINS(" + column + ", JSON_ARRAY({0}))" : "JSON_CONTAINS(" + column + ", {0})";
        }
    }

    /**
     * Oracle、H2：先用 REGEXP_LIKE 校验整个值为平面 Long / null 数组，再去掉空白、将首尾括号换为逗号，按 ,元素, 精确匹配。
     * 参数为 null 时匹配 null 元素，避免 Oracle 空串即 NULL 的拼接陷阱
     *
     * @param column 代码中固定的 JSON 数组列名
     * @param arrayPattern 整体格式的正则：{@link #ORACLE_LONG_ARRAY_PATTERN} 或 H2 专用的 {@link #H2_LONG_ARRAY_PATTERN}
     * @param idText {0} 绑定的编号转为十进制文本的表达式
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    private static String longArrayContains(String column, String arrayPattern, String idText) {
        // 注意：不能使用 TRANSLATE，Oracle 的 CLOB 列超过 4000 字节时会报 ORA-22835；REPLACE 对 CLOB 返回 CLOB
        String text = "REPLACE(REPLACE(REPLACE(" + column + ", CHR(9), ' '), CHR(10), ' '), CHR(13), ' ')";
        return "(REGEXP_LIKE(" + text + ", '" + arrayPattern + "')"
                + " AND NOT REGEXP_LIKE(" + column + ", '" + LONG_OVERFLOW_PATTERN + "')"
                + " AND INSTR(REPLACE(REPLACE(REPLACE(" + text + ", ' ', ''), '[', ','), ']', ','),"
                + " ',' || COALESCE(" + idText + ", 'null') || ',') > 0)";
    }

    /**
     * SQL Server 2012+：不使用 OPENJSON（要求 2016+ 且兼容级别 130+），用 XML 校验元素
     *
     * 1. TAB、LF、CR 替换为空格后修剪整个值，要求首尾为 [ ]；括号内只允许数字、-、逗号、空格及 null 的小写字母（BIN2 比较），
     *    此时不含 XML 元字符，按逗号拆成 &lt;r&gt;&lt;i&gt;...&lt;/i&gt;&lt;/r&gt;，其它值拆成空的 &lt;r/&gt;
     * 2. 用 .query('.') 物化一份 XML 副本，再对整个文档执行一次 exist 校验（见 {@link #SQL_SERVER_LONG_ARRAY_XQUERY}）：
     *    直接对内联 CAST 的 XML 执行时可能重复解析，本次验证中 1400 个元素的数组会明显变慢
     * 3. 校验通过后，去掉空格、首尾补逗号，按 ,元素, 用 CHARINDEX 匹配；参数在 SQL 内转为 bigint 再转文本，
     *    兼容 String、Long 参数；为 null 时匹配 null 元素
     *
     * 注意：最低按 SQL Server 2012 的语法编写，实际只在 SQL Server 2017（兼容级别 110）上验收。
     * 表别名使用 yd_j 前缀，避免遮蔽外层查询的别名
     *
     * @param column 代码中固定的 JSON 数组列名
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    private static String sqlServerJsonLongArrayContains(String column) {
        return "EXISTS (SELECT 1"
                + " FROM (SELECT LTRIM(RTRIM(REPLACE(REPLACE(REPLACE(CAST(" + column + " AS nvarchar(max)),"
                + "CHAR(9),N' '),CHAR(10),N' '),CHAR(13),N' '))) AS doc) yd_jd"
                + " CROSS APPLY (SELECT CASE WHEN LEFT(yd_jd.doc,1)=N'[' AND RIGHT(yd_jd.doc,1)=N']'"
                + " THEN SUBSTRING(yd_jd.doc,2,CASE WHEN LEN(yd_jd.doc)>=2 THEN LEN(yd_jd.doc)-2 ELSE 0 END)"
                + " ELSE NULL END AS body) yd_jb"
                + " CROSS APPLY (SELECT CAST(CASE WHEN yd_jb.body IS NOT NULL"
                + " AND PATINDEX(N'%[^0-9,nul -]%',COALESCE(yd_jb.body,N'') COLLATE Latin1_General_100_BIN2)=0"
                + " AND LTRIM(RTRIM(yd_jb.body))<>N''"
                + " THEN N'<r><i>'+REPLACE(yd_jb.body,N',',N'</i><i>')+N'</i></r>' ELSE N'<r/>' END AS XML) AS tokens) yd_jx"
                + " CROSS APPLY (SELECT yd_jx.tokens.query('.') AS tokens) yd_jm"
                + " WHERE yd_jm.tokens.exist('" + SQL_SERVER_LONG_ARRAY_XQUERY + "') = 1"
                + " AND CHARINDEX(N','+COALESCE(CONVERT(nvarchar(20),CAST({0} AS BIGINT)),N'null')+N',',"
                + "N','+REPLACE(yd_jb.body,N' ',N'')+N',')>0)";
    }

    /**
     * 生成与 max 位数相同、数值更大的十进制数字串的正则，例如 max 为 18 时为 (1[9-9]|[2-9][0-9]{1})
     *
     * @param max 不含符号的十进制数字串
     * @return 正则片段，不含 {0} 这样会被 Wrapper 当作占位符的文本
     */
    static String greaterDigitsPattern(String max) {
        String pattern = null;
        for (int i = max.length() - 1; i >= 0; i--) {
            char digit = max.charAt(i);
            int rest = max.length() - 1 - i;
            String greater = digit == '9' ? null
                    : "[" + (char) (digit + 1) + "-9]" + (rest > 0 ? "[0-9]{" + rest + "}" : "");
            String same = pattern == null ? null : digit + pattern;
            if (same == null) {
                pattern = greater;
            } else {
                pattern = greater == null ? same : "(" + same + "|" + greater + ")";
            }
        }
        return pattern;
    }

    // ========== JSON 字段值 ==========
    //
    // 1. 列为 JSON 对象文本（例如 CMS 模型的 model_values、问卷的 answer_json），按键取标量值后比较或排序；
    //    键只允许 {@link #JSON_KEY_PATTERN} 的格式，拼接为路径字面量（Oracle、SQL Server 2016 的 JSON 路径必须是字面量），业务值均为编号参数
    // 2. 统一语义：键缺失、JSON null、列为 NULL 均为 SQL NULL，比较不命中、排序归入空值；数字、日期按格式识别，非法值为 NULL，不报错
    // 3. 方言按 {@link #resolveJsonValueDialect(DatabaseMetaData)} 识别：在数据库产品方言的基础上单独识别达梦，神通、SQL Server 2012 以下仍为原有片段；
    //    H2 输出原有片段，仅用于单元测试校验 SQL 文本
    // 4. 原有片段：MySQL 8.0 待实库验收；MariaDB 没有 CAST(... AS JSON)，神通、OceanBase、TiDB 未验收
    // 5. 达梦：没有 MySQL 的中缀 REGEXP，JSON_UNQUOTE 的结果不能直接转为 DECIMAL，绑定的 Long 不能 CAST(... AS JSON)，
    //    数字、日期改用 JSON_VALUE 与 REGEXP_LIKE，编号、数组改用 JSON_OVERLAPS，其它与原有片段相同；
    //    达梦可能先于 CASE 条件执行转换，格式校验放在 CAST 的输入中（非法值先成为 NULL），不依赖 AND、CASE 的短路
    // 6. Oracle：使用 JSON_VALUE、JSON_EXISTS 与路径中的 type() 方法，文本使用 RETURNING CLOB（不截断），数组逐元素按类型比较使用 JSON_TABLE，
    //    需要 Oracle 18c+；RETURNING 与 JSON_TABLE 需要 JSqlParser 5.4+（租户、数据权限插件解析 SQL），不使用 ON ERROR、PASSING 子句
    // 7. SQL Server：JSON_VALUE 需要 2016+；按类型比较的片段使用 OPENJSON，还需要数据库兼容级别 130+，见各方法说明

    /**
     * JSON 对象列中键对应的标量文本：字符串为原文，数字、布尔为其 JSON 文本；键缺失、JSON null、数组、对象均为 NULL
     *
     * 1. 原有片段：CASE WHEN JSON_TYPE(...) NOT IN ('NULL', 'ARRAY', 'OBJECT') THEN JSON_UNQUOTE(JSON_EXTRACT(...)) END，
     *    排除原 JSON_UNQUOTE 将 JSON null 转为的文本 'null'
     * 2. PostgreSQL：jsonb_typeof 为 string、number、boolean 时取 jsonb_extract_path_text
     * 3. Oracle：JSON_VALUE(... RETURNING CLOB)，长文本不截断；CLOB 可以 LIKE，但不能直接排序，排序见 {@link #jsonOrderBy(String, String, Class)}
     * 4. SQL Server：OPENJSON 按键取值（nvarchar(max)，不截断），需要兼容级别 130+
     *
     * 比较、排序使用各数据库的默认排序规则，例如 SQL Server 默认不区分大小写，与 MySQL 的 utf8mb4_bin 不同
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return SQL 表达式，不含编号参数
     */
    public static String jsonText(String column, String key) {
        return jsonText(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON 标量文本表达式。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return SQL 表达式
     */
    static String jsonText(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL:
                return "CASE WHEN jsonb_typeof(" + postgreSQLJsonNode(column, key) + ") IN ('string', 'number', 'boolean')"
                        + " THEN " + postgreSQLJsonText(column, key) + " END";
            case ORACLE:
                return "JSON_VALUE(" + column + ", " + jsonPathLiteral(key, "") + " RETURNING CLOB)";
            case SQLSERVER:
                return "(SELECT yd_jo.\"value\"" + sqlServerJsonMember(column, key) + " AND yd_jo.\"type\" IN (1, 2, 3))";
            default:
                return "CASE WHEN JSON_TYPE(" + mysqlJsonExtract(column, key) + ") NOT IN ('NULL', 'ARRAY', 'OBJECT')"
                        + " THEN " + mysqlJsonUnquote(column, key) + " END";
        }
    }

    /**
     * JSON 对象列中键对应的数值：JSON 数字或数字字符串，文本符合 {@link #JSON_NUMBER_PATTERN} 且不超过 64 个字符时转为数值，其它为 NULL
     *
     * 1. 原有片段：DECIMAL(30, 10)，与原有精度一致，整数部分超过 20 位时按 MySQL 规则截断为最大值；JSON null、布尔、
     *    非数字字符串不再被转为 0
     * 2. PostgreSQL：NUMERIC；Oracle：TO_NUMBER，小数点替换为会话的小数点字符，不受 NLS_NUMERIC_CHARACTERS 影响
     * 3. SQL Server：JSON_VALUE（2016+，不要求兼容级别 130），按首字符与字符集初筛后转为 DECIMAL(38, 10)，
     *    科学计数法按文本精确展开，见 {@link #sqlServerDecimal(String)}；十进制数的 TRY_CAST 比正则宽松，例如 1. 也能转换，
     *    整数部分超过 28 位时为 NULL
     * 4. 达梦：JSON_VALUE 的文本按 REGEXP_LIKE（{@link #DM_NUMBER_PATTERN}）校验后转为 DECIMAL(30, 10)，校验在 CAST 的输入中，
     *    绝对值不小于 1E+20 的值为 NULL
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return SQL 表达式，不含编号参数
     */
    public static String jsonNumber(String column, String key) {
        return jsonNumber(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON 数值表达式；转换只在格式校验通过后执行，非法历史数据不会使查询报错。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return SQL 表达式
     */
    static String jsonNumber(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL: {
                String text = postgreSQLJsonText(column, key);
                return "CASE WHEN " + text + " ~ '" + JSON_NUMBER_PATTERN + "' AND LENGTH(" + text + ") <= "
                        + JSON_NUMBER_MAX_LENGTH + " THEN CAST(" + text + " AS NUMERIC) END";
            }
            case ORACLE: {
                String text = oracleJsonValue(column, key, "");
                return "CASE WHEN REGEXP_LIKE(" + text + ", '" + JSON_NUMBER_PATTERN + "') AND LENGTH(" + text + ") <= "
                        + JSON_NUMBER_MAX_LENGTH + " THEN TO_NUMBER(REPLACE(" + text
                        + ", '.', SUBSTR(TO_CHAR(0.5, 'FM0D0'), 2, 1))) END";
            }
            case SQLSERVER: {
                String text = sqlServerJsonValue(column, key);
                return "CASE WHEN LEN(" + text + ") <= " + JSON_NUMBER_MAX_LENGTH
                        + " AND (" + text + " LIKE N'[0-9]%' OR " + text + " LIKE N'-[0-9]%' OR " + text + " LIKE N'.[0-9]%'"
                        + " OR " + text + " LIKE N'-.[0-9]%')"
                        + " AND SUBSTRING(" + text + ", 2, " + JSON_NUMBER_MAX_LENGTH + ") NOT LIKE N'%[^0-9.Ee+-]%'"
                        + " THEN " + sqlServerDecimal(text) + " END";
            }
            case DM: {
                // 先转为 DECIMAL(38, 10)（格式校验后不会溢出），按范围校验后再转为 DECIMAL(30, 10)：
                // 例如 99999999999999999999.99999999995 舍入到 10 位小数后为 1E+20，直接转 DECIMAL(30, 10) 会溢出报错；
                // 两次校验都在 CAST 的输入中，比较使用精确的整数字面量，不经过浮点数
                String text = dmJsonValue(column, key);
                String decimal = "CAST(CASE WHEN REGEXP_LIKE(" + text + ", '" + DM_NUMBER_PATTERN + "') AND LENGTH(" + text + ") <= "
                        + JSON_NUMBER_MAX_LENGTH + " THEN " + text + " END AS DECIMAL(38, 10))";
                return "CAST(CASE WHEN " + decimal + " > -" + DM_NUMBER_LIMIT + " AND " + decimal + " < " + DM_NUMBER_LIMIT
                        + " THEN " + decimal + " END AS DECIMAL(30, 10))";
            }
            default: {
                String text = mysqlJsonUnquote(column, key);
                return "CASE WHEN " + text + " REGEXP '" + JSON_NUMBER_PATTERN + "' AND CHAR_LENGTH(" + text + ") <= "
                        + JSON_NUMBER_MAX_LENGTH + " THEN CAST(" + text + " AS DECIMAL(30, 10)) END";
            }
        }
    }

    /**
     * JSON 对象列中键对应的日期时间：字符串符合 {@link #JSON_DATE_TIME_PATTERN} 时转为日期时间，其它（含带时区偏移的文本）为 NULL
     *
     * 1. 原有片段：DATETIME(6)，原有的 DATETIME 会把小数秒进位到下一秒；PostgreSQL：TIMESTAMP（6 位小数秒）
     * 2. Oracle：按长度选择 TO_TIMESTAMP 的格式（9 位小数秒），格式不受 NLS 影响
     * 3. SQL Server：JSON_VALUE（2016+），按 LIKE 校验格式后 TRY_CAST 为 datetime2(7)，日历由 TRY_CAST 校验；小数秒超过 7 位时截断
     * 4. 达梦：JSON_VALUE 的文本按 REGEXP_LIKE 校验后转为 DATETIME(6)，校验在 CAST 的输入中；小数秒超过 6 位时截断
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return SQL 表达式，不含编号参数
     */
    public static String jsonDateTime(String column, String key) {
        return jsonDateTime(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON 日期时间表达式；转换只在格式与日历校验通过后执行，非法历史数据不会使查询报错。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return SQL 表达式
     */
    static String jsonDateTime(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL: {
                String text = postgreSQLJsonText(column, key);
                return "CASE WHEN " + text + " ~ '" + JSON_DATE_TIME_PATTERN + "' THEN CAST(" + text + " AS TIMESTAMP) END";
            }
            case ORACLE: {
                String text = oracleJsonValue(column, key, "");
                String dateTime = "REPLACE(" + text + ", 'T', ' ')";
                return "CASE WHEN REGEXP_LIKE(" + text + ", '" + JSON_DATE_TIME_PATTERN + "') THEN CASE LENGTH(" + text + ")"
                        + " WHEN 10 THEN TO_TIMESTAMP(" + text + ", 'YYYY-MM-DD')"
                        + " WHEN 16 THEN TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI')"
                        + " WHEN 19 THEN TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI:SS')"
                        + " ELSE TO_TIMESTAMP(" + dateTime + ", 'YYYY-MM-DD HH24:MI:SS.FF') END END";
            }
            case SQLSERVER: {
                String text = sqlServerJsonValue(column, key);
                String date = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]";
                String minute = date + "[ T][0-9][0-9]:[0-9][0-9]";
                String second = minute + ":[0-9][0-9]";
                return "CASE WHEN " + text + " LIKE N'" + date + "' OR " + text + " LIKE N'" + minute + "'"
                        + " OR " + text + " LIKE N'" + second + "' OR (" + text + " LIKE N'" + second + ".[0-9]%'"
                        + " AND LEN(" + text + ") <= 29 AND SUBSTRING(" + text + ", 21, 9) NOT LIKE N'%[^0-9]%')"
                        + " THEN TRY_CAST(LEFT(REPLACE(" + text + ", N'T', N' '), 27) AS datetime2(7)) END";
            }
            case DM: {
                String text = dmJsonValue(column, key);
                return "CAST(CASE WHEN REGEXP_LIKE(" + text + ", '" + JSON_DATE_TIME_PATTERN + "')"
                        + " THEN SUBSTR(REPLACE(" + text + ", 'T', ' '), 1, 26) END AS DATETIME(6))";
            }
            default: {
                String text = mysqlJsonUnquote(column, key);
                return "CASE WHEN " + text + " REGEXP '" + JSON_DATE_TIME_PATTERN + "' THEN CAST(" + text + " AS DATETIME(6)) END";
            }
        }
    }

    /**
     * 按 JSON 对象列中键对应的值排序的 ORDER BY 表达式，依次追加，方向相同：
     * 第一个表达式为空值分组（值为 NULL 时为 0），使空值（含非法值）在升序时排在最前、降序时排在最后，与 MySQL 的 NULL 顺序一致
     * （PostgreSQL、Oracle 默认 NULL 排在最后）；第二个表达式为值本身
     *
     * 1. valueType 为 BigDecimal：按 {@link #jsonNumber(String, String)} 分组与排序；非法值（JSON null、布尔、非数字字符串）为 NULL，
     *    不被转为 0，与空值组一致
     * 2. valueType 为 LocalDateTime：按 {@link #jsonDateTime(String, String)}
     * 3. valueType 为 String：按 {@link #jsonText(String, String)}，排序规则见该方法；
     *    Oracle 的 CLOB 不能排序，分组与值均为 JSON_VALUE ... RETURNING VARCHAR2(32767)（MAX_STRING_SIZE 为 STANDARD 时也可用），
     *    完整比较 32767 字节以内的文本；超过 32767 字节时 JSON_VALUE 为 NULL（默认 NULL ON ERROR），归入空值组，不按文本排序；
     *    Oracle 的空字符串即 NULL，同样归入空值组。筛选（LIKE）仍使用完整的 CLOB
     *
     * @param column    JSON 对象列名
     * @param key       键
     * @param valueType 值类型：String、BigDecimal、LocalDateTime
     * @return 两个 ORDER BY 表达式
     */
    public static List<String> jsonOrderBy(String column, String key, Class<?> valueType) {
        return jsonOrderBy(getJsonValueDialect(), column, key, valueType);
    }

    /**
     * 按指定方言生成 JSON 值排序表达式。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @param valueType 值类型
     * @return 空值分组与值两个 ORDER BY 表达式
     */
    static List<String> jsonOrderBy(Dialect dialect, String column, String key, Class<?> valueType) {
        String value;
        if (valueType == BigDecimal.class) {
            value = jsonNumber(dialect, column, key);
        } else if (valueType == LocalDateTime.class) {
            value = jsonDateTime(dialect, column, key);
        } else if (valueType == String.class && dialect == Dialect.ORACLE) {
            // CLOB 不能排序：改为 VARCHAR2(32767)，空值分组使用同一表达式，空字符串、超长文本与 NULL 一致
            checkColumn(column);
            checkJsonKey(key);
            value = "JSON_VALUE(" + column + ", " + jsonPathLiteral(key, "") + " RETURNING VARCHAR2(32767))";
        } else if (valueType == String.class) {
            value = jsonText(dialect, column, key);
        } else {
            throw new IllegalArgumentException("Invalid json value type: " + valueType);
        }
        return Arrays.asList(orderByExpression("CASE WHEN " + value + " IS NULL THEN 0 ELSE 1 END"),
                orderByExpression(value));
    }

    /**
     * ORDER BY 表达式中正则的 ? 量词改为等价的 {0,1}
     *
     * MyBatis Plus 分页的 count 优化（PaginationInnerInterceptor#autoCountSql）把 toString() 含 ? 的排序表达式当作带绑定参数，
     * 保留 ORDER BY 后将查询列替换为 COUNT(*)，PostgreSQL 因此报 GROUP BY 错误。排序表达式不含编号参数（{0} 等），
     * 其中的 ? 只能是数字、日期正则的量词；Wrapper 的 orderBy 不解析编号参数，{0,1} 原样输出。
     * 只用于排序表达式，筛选条件经 Wrapper.apply 解析编号参数，仍使用原正则
     *
     * @param expression 排序表达式
     * @return 不含 ? 的排序表达式
     */
    private static String orderByExpression(String expression) {
        return expression.replace("?", "{0,1}");
    }

    /**
     * JSON 对象列中键对应的值是否为 {0} 绑定的布尔值，参数为 "true" 或 "false"；按 JSON 布尔类型比较，字符串 "true" 不匹配
     *
     * 1. 原有片段：JSON_EXTRACT(...) = CAST({0} AS JSON)；PostgreSQL：jsonb_extract_path(...) = CAST({0} AS jsonb)
     * 2. Oracle：路径方法 type() 为 boolean，且 JSON_VALUE 等于参数
     * 3. SQL Server：OPENJSON 的 type 为 3（true、false），需要兼容级别 130+
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    public static String jsonBooleanEquals(String column, String key) {
        return jsonBooleanEquals(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON 布尔值相等条件。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    static String jsonBooleanEquals(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL:
                return postgreSQLJsonNode(column, key) + " = CAST({0} AS jsonb)";
            case ORACLE:
                return "(" + oracleJsonValue(column, key, ".type()") + " = 'boolean' AND "
                        + oracleJsonValue(column, key, "") + " = {0})";
            case SQLSERVER:
                return "EXISTS (SELECT 1" + sqlServerJsonMember(column, key) + " AND yd_jo.\"type\" = 3 AND yd_jo.\"value\" = {0})";
            default:
                return mysqlJsonExtract(column, key) + " = CAST({0} AS JSON)";
        }
    }

    /**
     * JSON 对象列中键对应的标量值是否等于 {0} 绑定的 JSON 数组中的任一元素，按 JSON 类型比较；
     * 参数为 {@link #toJsonArrayParam(Class, Collection)} 的结果，空数组不匹配。存储值为数组、对象时不匹配
     *
     * 1. 原有片段与达梦：JSON_CONTAINS({0}, JSON_EXTRACT(...))，JSON 数组参数直接作为 JSON 文本消费，不 CAST
     *    （MariaDB 的 CAST 不支持 JSON 类型；MariaDB 待实库验证）
     * 2. PostgreSQL：jsonb_array_elements 展开参数后按 jsonb 相等比较
     * 3. Oracle：JSON_TABLE 展开参数（CLOB 绑定，见 {@link OracleClobTypeHandler}），按 type() 比较；字符串、布尔比较文本，
     *    数字比较 JSON_QUERY ... WITH WRAPPER 原文的规范键（见 {@link #oracleJsonNumberKey(String)}），不经过 NUMBER
     * 4. SQL Server：OPENJSON 展开参数，按 type 与值比较，见 {@link #sqlServerJsonParamMatch(String, Class)}；需要兼容级别 130+
     *
     * @param column    JSON 对象列名
     * @param key       键
     * @param valueType 值类型：String、BigDecimal、Boolean
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    public static String jsonIn(String column, String key, Class<?> valueType) {
        return jsonIn(getJsonValueDialect(), column, key, valueType);
    }

    /**
     * 按指定方言生成 JSON 标量值 IN 条件，{0} 为 JSON 数组参数。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @param valueType 值类型
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    static String jsonIn(Dialect dialect, String column, String key, Class<?> valueType) {
        checkColumn(column);
        checkJsonKey(key);
        checkJsonValueType(valueType);
        switch (dialect) {
            case POSTGRESQL:
                return "EXISTS (SELECT 1 FROM " + postgreSQLJsonArrayParam() + " WHERE yd_jc.\"value\" = "
                        + postgreSQLJsonNode(column, key) + ")";
            case ORACLE: {
                // 数字按原文的规范键比较，不经过 NUMBER（42 位的相邻整数会舍入相等）；字符串、布尔按文本比较
                String type = oracleJsonType(valueType);
                String match = valueType == BigDecimal.class
                        ? oracleJsonNumberKey("yd_jc.r") + " = " + oracleJsonNumberKey("JSON_QUERY(" + column + ", "
                                + jsonPathLiteral(key, "") + " RETURNING VARCHAR2(4000) WITH WRAPPER)")
                        : "yd_jc.v = " + oracleJsonValue(column, key, "");
                return "(" + oracleJsonValue(column, key, ".type()") + " = '" + type + "' AND EXISTS (SELECT 1 FROM "
                        + oracleJsonArrayParam() + " WHERE yd_jc.t = '" + type + "' AND " + match + "))";
            }
            case SQLSERVER:
                // 参数只展开一次；存储值只匹配标量（type 1 ~ 3），数组、对象、null 不匹配
                return "EXISTS (SELECT 1" + sqlServerJsonMember(column, key) + " AND EXISTS (SELECT 1 FROM "
                        + sqlServerJsonArrayParam() + " WHERE " + sqlServerJsonParamMatch("yd_jo", valueType) + "))";
            default: {
                // 原有片段与达梦相同：JSON_CONTAINS 直接消费绑定的 JSON 文本，不 CAST（MariaDB 的 CAST 不支持 JSON 类型，待实库验证）
                String extract = mysqlJsonExtract(column, key);
                return "(JSON_TYPE(" + extract + ") <> 'ARRAY' AND JSON_CONTAINS({0}, " + extract + "))";
            }
        }
    }

    /**
     * JSON 对象列中键对应的数组是否包含 {0} 绑定的 JSON 数组中的任一元素；存储值为标量时按单元素数组处理，
     * 只比较直接元素（嵌套数组不展开），按 JSON 类型比较；参数为 {@link #toJsonArrayParam(Class, Collection)} 的结果，空数组不匹配
     *
     * 1. 原有片段与达梦：JSON_OVERLAPS(JSON_EXTRACT(...), {0})，需要 MySQL 8.0.17+；JSON 数组参数直接作为 JSON 文本消费，不 CAST
     *    （MariaDB 的 CAST 不支持 JSON 类型；MariaDB 待实库验证）
     * 2. PostgreSQL：存储值为数组时展开一层，否则包装为单元素数组，与 jsonb_array_elements 展开的参数按 jsonb 相等比较
     * 3. Oracle：JSON_TABLE 分别展开存储值与参数（CLOB 绑定），按 type() 比较；字符串、布尔比较文本，数字比较原文 r 的规范键，
     *    不经过 NUMBER；字符串元素超过 4000 字节时不匹配
     * 4. SQL Server：OPENJSON 展开参数一次，存储值的标量与数组直接元素分别比较；只有数组才再次 OPENJSON，
     *    见 {@link #sqlServerJsonArrayElements()}；需要兼容级别 130+
     *
     * @param column    JSON 对象列名
     * @param key       键
     * @param valueType 值类型：String、BigDecimal、Boolean
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    public static String jsonArrayOverlaps(String column, String key, Class<?> valueType) {
        return jsonArrayOverlaps(getJsonValueDialect(), column, key, valueType);
    }

    /**
     * 按指定方言生成 JSON 数组任一匹配条件，{0} 为 JSON 数组参数。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @param valueType 值类型
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    static String jsonArrayOverlaps(Dialect dialect, String column, String key, Class<?> valueType) {
        checkColumn(column);
        checkJsonKey(key);
        checkJsonValueType(valueType);
        switch (dialect) {
            case POSTGRESQL: {
                // 存储值为 SQL NULL、对象等时包装为 [null]、[{...}]，与标量参数均不相等
                String node = postgreSQLJsonNode(column, key);
                return "EXISTS (SELECT 1 FROM jsonb_array_elements(CASE WHEN jsonb_typeof(" + node + ") = 'array' THEN " + node
                        + " ELSE jsonb_build_array(" + node + ") END) yd_je, " + postgreSQLJsonArrayParam()
                        + " WHERE yd_je.\"value\" = yd_jc.\"value\")";
            }
            case ORACLE: {
                // 数字按原文的规范键比较，不经过 NUMBER；字符串、布尔按文本比较
                String type = oracleJsonType(valueType);
                String match = valueType == BigDecimal.class
                        ? oracleJsonNumberKey("yd_jt.r") + " = " + oracleJsonNumberKey("yd_jc.r") : "yd_jt.v = yd_jc.v";
                return "EXISTS (SELECT 1" + oracleJsonElements(column, key) + ", " + oracleJsonArrayParam()
                        + " WHERE yd_jt.t = '" + type + "' AND yd_jc.t = '" + type + "' AND " + match + ")";
            }
            case SQLSERVER:
                // 参数只展开一次，标量与数组两个分支共用；数组只展开一层，嵌套数组、对象元素的 type 不匹配
                return "EXISTS (SELECT 1" + sqlServerJsonMember(column, key) + " AND EXISTS (SELECT 1 FROM "
                        + sqlServerJsonArrayParam() + " WHERE " + sqlServerJsonParamMatch("yd_jo", valueType)
                        + " OR (yd_jo.\"type\" = 4 AND EXISTS (SELECT 1 FROM " + sqlServerJsonArrayElements() + " WHERE "
                        + sqlServerJsonParamMatch("yd_je", valueType) + "))))";
            default:
                // 原有片段与达梦相同：JSON_OVERLAPS 直接消费绑定的 JSON 文本，不 CAST（MariaDB 的 CAST 不支持 JSON 类型）
                return "JSON_OVERLAPS(" + mysqlJsonExtract(column, key) + ", {0})";
        }
    }

    /**
     * PostgreSQL：展开 {0} 绑定的 JSON 数组参数，每个元素为一行，列为 value。
     *
     * @return FROM 之后的表片段，表别名为 yd_jc
     */
    private static String postgreSQLJsonArrayParam() {
        return "jsonb_array_elements(CAST({0} AS jsonb)) yd_jc";
    }

    /**
     * Oracle：JSON_TABLE 展开 {0} 绑定的 JSON 数组参数，列同 {@link #oracleJsonElements(String, String)}；
     * 参数按 CLOB 绑定（{@link OracleClobTypeHandler}），超过 32767 字符的数组也不报 ORA-01460
     *
     * @return FROM 之后的表片段，表别名为 yd_jc
     */
    private static String oracleJsonArrayParam() {
        return "JSON_TABLE({0,jdbcType=CLOB,typeHandler=" + OracleClobTypeHandler.class.getName() + "}, '$[*]'"
                + " COLUMNS (" + ORACLE_JSON_TABLE_COLUMNS + ")) yd_jc";
    }

    /**
     * Oracle：JSON 数字原文的规范键，数值相等时键相同，不限精度与小数位数，不经过 NUMBER（NUMBER 最多约 40 位有效数字，
     * 42 位的相邻整数会舍入相等）：符号、去掉前导与末尾 0 的有效数字、十进制指数（原指数 + 整数位数 - 去掉末尾 0 后的数字位数），
     * 例如 1、1.0、10e-1、1e0 均为 1e0，-0、0、0.0 均为 0；1e-11 与 2e-11、相邻的大整数不会相同
     *
     * 1. 输入为 FORMAT JSON WITH WRAPPER 的原文，例如 [1.00E0]，按 {@link #ORACLE_JSON_NUMBER_RAW_PATTERN} 校验；
     *    字符串、对象、数组、NULL 等不符合时为 NULL，不匹配
     * 2. 格式校验通过后才按分组 REGEXP_REPLACE 取符号、数字与指数；指数不超过 20 个字符时才 TO_NUMBER
     *    （整数文本不含小数点，不受 NLS_NUMERIC_CHARACTERS 影响），否则为 NULL；不按指数补零，不生成超长文本
     * 3. Oracle 的空字符串即 NULL：数字全为 0 时去掉末尾 0 后为 NULL，统一为 '0'
     *
     * @param raw 数字原文的表达式，只能为内部固定的列或表达式
     * @return SQL 表达式
     */
    private static String oracleJsonNumberKey(String raw) {
        String pattern = "'" + ORACLE_JSON_NUMBER_RAW_PATTERN + "'";
        // digits 为去掉小数点与末尾 0 的数字（可含前导 0），exponent 为指数文本（没有时为 NULL）
        String digits = "RTRIM(REGEXP_REPLACE(" + raw + ", " + pattern + ", '\\2\\4'), '0')";
        String exponent = "REGEXP_REPLACE(" + raw + ", " + pattern + ", '\\6')";
        // 尾数至少有一位数字（[]、[-]、[e5] 不是数字）：Oracle 的空字符串即 NULL
        return "CASE WHEN REGEXP_LIKE(" + raw + ", " + pattern + ") AND REGEXP_REPLACE(" + raw + ", " + pattern + ", '\\2\\4') IS NOT NULL"
                + " AND NVL(LENGTH(" + exponent + "), 0) <= 20"
                + " THEN CASE WHEN " + digits + " IS NULL THEN '0'"
                + " ELSE REGEXP_REPLACE(" + raw + ", " + pattern + ", '\\1') || LTRIM(" + digits + ", '0') || 'e'"
                + " || TO_CHAR(NVL(TO_NUMBER(" + exponent + "), 0) + NVL(LENGTH(REGEXP_REPLACE(" + raw + ", " + pattern + ", '\\2')), 0)"
                + " - LENGTH(" + digits + "), 'TM9') END END";
    }

    /**
     * SQL Server：OPENJSON 展开 {0} 绑定的 JSON 数组参数，每个元素为一行。
     *
     * @return FROM 之后的表片段，表别名为 yd_jc
     */
    private static String sqlServerJsonArrayParam() {
        return "OPENJSON(CAST({0} AS nvarchar(max))) yd_jc";
    }

    /**
     * SQL Server：OPENJSON 的一行与参数元素 yd_jc 按类型与值匹配；type 为 1 字符串、2 数字、3 布尔，两侧类型相同才比较。
     *
     * 1. 字符串：BIN2 排序规则，两侧追加同一个字符 N'.'，大小写与尾部空格均参与比较
     * 2. 数字：按 {@link #sqlServerJsonNumberKey(String)} 的规范键比较，不经过固定精度的 DECIMAL 与 FLOAT
     *
     * @param alias OPENJSON 的表别名，只能为内部固定的别名
     * @param valueType 值类型
     * @return SQL 条件片段，带括号
     */
    private static String sqlServerJsonParamMatch(String alias, Class<?> valueType) {
        String value = alias + ".\"value\"";
        if (valueType == BigDecimal.class) {
            return "(" + alias + ".\"type\" = 2 AND yd_jc.\"type\" = 2 AND " + sqlServerJsonNumberKey(alias)
                    + " = " + sqlServerJsonNumberKey("yd_jc") + ")";
        }
        if (valueType == Boolean.class) {
            return "(" + alias + ".\"type\" = 3 AND yd_jc.\"type\" = 3 AND " + value + " = yd_jc.\"value\")";
        }
        return "(" + alias + ".\"type\" = 1 AND yd_jc.\"type\" = 1 AND (" + value + " + N'.') COLLATE Latin1_General_100_BIN2"
                + " = (yd_jc.\"value\" + N'.') COLLATE Latin1_General_100_BIN2)";
    }

    /**
     * SQL Server：OPENJSON 的一行与 Long 编号 yd_ji 匹配，用于 {@link #jsonLongContains(String, String)}。
     *
     * 1. 字符串（type 1）：与编号的十进制文本按原文精确相等；BIN2 排序规则（全角数字不当作编号），两侧追加 N'.'
     *    （尾部空格参与比较，"123 " 不当作编号 123）；"11.0"、"1.1e1" 不匹配
     * 2. 数字（type 2）：按 {@link #sqlServerJsonNumberKey(String)} 的规范键与 yd_jk.k 比较，11.0、1.1e1 匹配编号 11，
     *    11.00000000001 不匹配；不经过固定精度的 DECIMAL、FLOAT 与 BIGINT 转换
     *
     * @param alias OPENJSON 的表别名，只能为内部固定的别名
     * @return SQL 条件片段，带括号
     */
    private static String sqlServerJsonLongMatch(String alias) {
        String value = alias + ".\"value\"";
        return "((" + alias + ".\"type\" = 1 AND (" + value + " + N'.') COLLATE Latin1_General_100_BIN2"
                + " = (yd_ji.\"value\" + N'.') COLLATE Latin1_General_100_BIN2)"
                + " OR (" + alias + ".\"type\" = 2 AND " + sqlServerJsonNumberKey(alias) + " = yd_jk.k))";
    }

    /**
     * SQL Server：JSON 数字原文的规范键，数值相等时键相同，不限精度与小数位数：
     * 符号、去掉前导与末尾 0 的有效数字、十进制指数（原指数 - 小数位数 + 去掉的末尾 0 个数），例如 1、1.0、10e-1、1e0 均为 1e0，
     * -0、0、0.0 均为 0；1e-11 与 2e-11、相邻的大 Long 不会相同
     *
     * 1. 文本先 COALESCE 为空串并转小写，BIN2 排序规则，键按二进制精确比较
     * 2. 各级 CROSS APPLY 只做取子串等不会报错的计算：SUBSTRING、LEFT 的长度均不为负数，PATINDEX 追加非 0 字符，
     *    即使优化器对其它类型的值提前计算也不会报错；不按指数补零，不生成超长文本
     * 3. 指数按 TRY_CAST(... AS INT) 转换后再转为 BIGINT 计算位移，超出 INT 时为 NULL，不匹配；
     *    使用 TRY_CAST 而不是 TRY_CONVERT，与 {@link #sqlServerDecimal(String)} 一致，便于 JSqlParser 解析
     *
     * @param alias OPENJSON 的表别名，只能为内部固定的别名
     * @return 标量子查询，表别名使用 yd_nk 前缀
     */
    private static String sqlServerJsonNumberKey(String alias) {
        // t 为小写文本，e 为指数符号的位置，n 为是否负数；m 为不含符号的尾数，x 为指数；
        // d 为去掉小数点的数字，f 为小数位数；r 为去掉前导 0 的数字，z 为 r 末尾 0 的个数
        String r = "yd_nk5.r";
        return "(SELECT CASE WHEN LEN(" + r + ") = 0 THEN N'0' WHEN yd_nk3.x IS NOT NULL"
                + " THEN CASE WHEN yd_nk2.n = 1 THEN N'-' ELSE N'' END"
                + " + LEFT(" + r + ", CASE WHEN LEN(" + r + ") > yd_nk6.z THEN LEN(" + r + ") - yd_nk6.z ELSE 0 END)"
                + " + N'e' + CONVERT(nvarchar(20), yd_nk3.x - yd_nk4.f + yd_nk6.z) END"
                + " FROM (SELECT LOWER(COALESCE(" + alias + ".\"value\", N'') COLLATE Latin1_General_100_BIN2) AS t) yd_nk1"
                + " CROSS APPLY (SELECT CHARINDEX(N'e', yd_nk1.t) AS e,"
                + " CASE WHEN LEFT(yd_nk1.t, 1) = N'-' THEN 1 ELSE 0 END AS n) yd_nk2"
                + " CROSS APPLY (SELECT SUBSTRING(yd_nk1.t, yd_nk2.n + 1, CASE WHEN yd_nk2.e > yd_nk2.n THEN yd_nk2.e - yd_nk2.n - 1"
                + " WHEN yd_nk2.e = 0 THEN LEN(yd_nk1.t) ELSE 0 END) AS m,"
                + " CASE WHEN yd_nk2.e > 0 THEN CAST(TRY_CAST(SUBSTRING(yd_nk1.t, yd_nk2.e + 1, LEN(yd_nk1.t)) AS INT) AS BIGINT)"
                + " ELSE CAST(0 AS bigint) END AS x) yd_nk3"
                + " CROSS APPLY (SELECT REPLACE(yd_nk3.m, N'.', N'') AS d, CASE WHEN CHARINDEX(N'.', yd_nk3.m) > 0"
                + " THEN LEN(yd_nk3.m) - CHARINDEX(N'.', yd_nk3.m) ELSE 0 END AS f) yd_nk4"
                + " CROSS APPLY (SELECT SUBSTRING(yd_nk4.d, PATINDEX(N'%[^0]%', yd_nk4.d + N'1'), LEN(yd_nk4.d)) AS r) yd_nk5"
                + " CROSS APPLY (SELECT PATINDEX(N'%[^0]%', REVERSE(" + r + ") + N'1') - 1 AS z) yd_nk6)";
    }

    /**
     * 将选项值集合转为一个 JSON 数组字符串，作为 JSON 选项条件的单个绑定参数
     *
     * 1. BigDecimal：接受数值或合法的数字文本，去掉末尾的 0 后按十进制文本写为 JSON 数字（不经过全局 ObjectMapper，
     *    避免大 Long 等被序列化为字符串），例如 2.50 为 2.5、1E+3 为 1000；十进制文本超过
     *    {@link #JSON_NUMBER_PLAIN_MAX_LENGTH} 个字符时（例如 1e999999999）写为科学计数法，不展开为超长文本
     * 2. Boolean：接受 Boolean 或严格的 true、false 文本，写为 JSON 布尔
     * 3. String：接受字符串、数值、布尔，按 JSON 字符串转义；对象、数组、集合等非标量拒绝
     *
     * 空集合为 []，不匹配任何值；不限制数量，也不截断
     *
     * @param valueType 值类型：String、BigDecimal、Boolean
     * @param values    选项值，集合与元素均不能为 null
     * @return JSON 数组字符串
     */
    public static String toJsonArrayParam(Class<?> valueType, Collection<?> values) {
        checkJsonValueType(valueType);
        if (values == null) {
            throw new IllegalArgumentException("Json values must not be null");
        }
        return values.stream().map(value -> toJsonArrayElement(valueType, value))
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * 将单个选项值转为 JSON 数组的元素文本，规则见 {@link #toJsonArrayParam(Class, Collection)}
     *
     * @param valueType 值类型：String、BigDecimal、Boolean
     * @param value     选项值
     * @return JSON 元素文本
     */
    private static String toJsonArrayElement(Class<?> valueType, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Json value must not be null");
        }
        if (valueType == BigDecimal.class) {
            if (!(value instanceof Number) && !(value instanceof String)) {
                throw new IllegalArgumentException("Invalid json number: " + value.getClass());
            }
            try {
                BigDecimal number = (value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString()))
                        .stripTrailingZeros();
                // 先计算十进制文本的长度，不实际展开：例如 1e999999999 展开后约 1GB，超长时改用科学计数法
                return NumberUtils.getPlainStringLength(number) <= JSON_NUMBER_PLAIN_MAX_LENGTH
                        ? number.toPlainString() : number.toString();
            } catch (NumberFormatException | ArithmeticException ex) {
                throw new IllegalArgumentException("Invalid json number: " + value);
            }
        }
        if (valueType == Boolean.class) {
            if (value instanceof Boolean || "true".equals(value) || "false".equals(value)) {
                return value.toString();
            }
            throw new IllegalArgumentException("Invalid json boolean: " + value);
        }
        if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid json string: " + value.getClass());
        }
        return JsonUtils.toJsonString(value.toString());
    }

    /**
     * JSON 对象列中键对应的值是否为 {0} 绑定的 Long 编号，或包含该编号的数组；编号可以是 JSON 数字，也可以是十进制文本的字符串
     * （主 ObjectMapper 将绝对值不小于 2^53 - 1 的 Long 序列化为字符串），用于问卷选项等编号答案
     *
     * 1. 原有片段：JSON_OVERLAPS 与数字、字符串两种形式的编号比较，只比较直接元素，需要 MySQL 8.0.17+；
     *    原 JSON_CONTAINS(..., CAST({0} AS JSON)) 会递归匹配嵌套数组，[[11]] 也会命中
     * 2. PostgreSQL：jsonb 的 @&gt;，分别按数字、字符串匹配
     * 3. Oracle：JSON_TABLE 逐个元素（标量作为单个元素）按 type() 比较，编号只绑定一次，数字按原文的规范键比较（不经过 NUMBER），
     *    字符串与编号的十进制文本精确比较；
     *    不依赖 JSON 的序列化格式与数组长度，需要 JSqlParser 5.4+
     * 4. SQL Server：OPENJSON 逐个元素比较，需要兼容级别 130+；只有数组才再次 OPENJSON，见 {@link #sqlServerJsonArrayElements()}；
     *    编号只绑定一次，数字按规范键比较，字符串按十进制文本精确比较，见 {@link #sqlServerJsonLongMatch(String)}
     * 5. 达梦：JSON_OVERLAPS 与数字、字符串两种形式的编号比较；达梦的 CAST(? AS JSON) 不接受 Long 参数，因此不复用原有片段
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    public static String jsonLongContains(String column, String key) {
        return jsonLongContains(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON Long 编号包含条件。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return 包含 {0} 占位符的 SQL 条件片段
     */
    static String jsonLongContains(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL: {
                String node = postgreSQLJsonNode(column, key);
                return "(" + node + " @> to_jsonb(CAST({0} AS BIGINT)) OR " + node
                        + " @> to_jsonb(CAST(CAST({0} AS BIGINT) AS VARCHAR)))";
            }
            case ORACLE:
                // 编号只绑定一次：yd_ji.v 为编号的十进制文本，参数为 NULL 时不匹配；
                // 数字按原文的规范键比较（不经过 NUMBER），字符串与十进制文本精确比较
                return "EXISTS (SELECT 1" + oracleJsonElements(column, key) + ", (SELECT TO_CHAR(CAST({0} AS NUMBER(19, 0)), 'TM9',"
                        + " 'NLS_NUMERIC_CHARACTERS = ''.,''') AS v FROM DUAL) yd_ji"
                        + " WHERE yd_ji.v IS NOT NULL AND ((yd_jt.t = 'number' AND " + oracleJsonNumberKey("yd_jt.r") + " = "
                        + oracleJsonNumberKey("'[' || yd_ji.v || ']'") + ") OR (yd_jt.t = 'string' AND yd_jt.v = yd_ji.v)))";
            case SQLSERVER:
                // 编号只绑定一次：yd_ji 为编号的十进制文本，yd_jk 为其数字规范键，标量与数组两个分支共用；
                // 参数为 NULL 时不匹配（规范键会把 NULL 当作 0）
                return "EXISTS (SELECT 1 FROM (SELECT CONVERT(nvarchar(20), CAST({0} AS BIGINT)) AS \"value\") yd_ji"
                        + " CROSS APPLY (SELECT " + sqlServerJsonNumberKey("yd_ji") + " AS k) yd_jk"
                        + " WHERE yd_ji.\"value\" IS NOT NULL AND EXISTS (SELECT 1" + sqlServerJsonMember(column, key)
                        + " AND (" + sqlServerJsonLongMatch("yd_jo") + " OR (yd_jo.\"type\" = 4 AND EXISTS (SELECT 1 FROM "
                        + sqlServerJsonArrayElements() + " WHERE " + sqlServerJsonLongMatch("yd_je") + ")))))";
            case DM:
                return "JSON_OVERLAPS(" + mysqlJsonExtract(column, key) + ", JSON_ARRAY(CAST({0} AS BIGINT),"
                        + " CAST(CAST({0} AS BIGINT) AS VARCHAR(20))))";
            default:
                // JSON_OVERLAPS 只比较直接元素（JSON_CONTAINS 会递归匹配嵌套数组 [[11]]），数字与字符串两种形式的编号
                return "JSON_OVERLAPS(" + mysqlJsonExtract(column, key) + ", JSON_ARRAY(CAST({0} AS SIGNED),"
                        + " CAST(CAST({0} AS SIGNED) AS CHAR)))";
        }
    }

    /**
     * JSON 对象列中键对应的值是否为含非空白字符的字符串，用于问答题的回答筛选
     *
     * 1. 原有片段：JSON_TYPE(...) = 'STRING' AND JSON_UNQUOTE(...) REGEXP '[^[:space:]]'
     * 2. PostgreSQL：jsonb_typeof 为 string，且 ~ 列举 {@link #JSON_WHITE_SPACES} 的字符类（[:space:] 取决于 locale，
     *    实库漏 U+0085、U+00A0、U+2007、U+202F）
     * 3. Oracle：路径方法 type() 为 string，且 JSON_EXISTS 的过滤条件 like_regex 命中，不受 4000 字节限制；
     *    只用 like_regex 时，宽松模式会把数字转为文本、把数组展开为元素，数字 123、数组 ["回答"] 也会命中，因此需要 type() 限定；
     *    空白字符见 {@link #ORACLE_JSON_WHITE_SPACES}
     * 4. SQL Server：OPENJSON 的 type 为 1（字符串），按 {@link #JSON_WHITE_SPACES} 列举空白字符，需要兼容级别 130+
     * 5. 达梦：不支持中缀 REGEXP，改为 REGEXP_LIKE，同样列举 {@link #JSON_WHITE_SPACES}（[:space:] 实库漏 19 个字符）；
     *    文本使用 JSON_UNQUOTE（CLOB，长文本不截断），JSON_VALUE 会截断长文本
     *
     * @param column JSON 对象列名
     * @param key    键
     * @return SQL 条件片段，不含编号参数
     */
    public static String jsonTextNotBlank(String column, String key) {
        return jsonTextNotBlank(getJsonValueDialect(), column, key);
    }

    /**
     * 按指定方言生成 JSON 字符串非空白条件。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return SQL 条件片段
     */
    static String jsonTextNotBlank(Dialect dialect, String column, String key) {
        checkColumn(column);
        checkJsonKey(key);
        switch (dialect) {
            case POSTGRESQL:
                return "(jsonb_typeof(" + postgreSQLJsonNode(column, key) + ") = 'string' AND "
                        + postgreSQLJsonText(column, key) + " ~ '[^" + JSON_WHITE_SPACES + "]')";
            case ORACLE:
                return "(" + oracleJsonValue(column, key, ".type()") + " = 'string' AND JSON_EXISTS(" + column + ", "
                        + jsonPathLiteral(key, "?(@ like_regex \"[^" + ORACLE_JSON_WHITE_SPACES + "]\")") + "))";
            case SQLSERVER:
                return "EXISTS (SELECT 1" + sqlServerJsonMember(column, key) + " AND yd_jo.\"type\" = 1"
                        + " AND yd_jo.\"value\" COLLATE Latin1_General_100_BIN2 LIKE N'%[^" + JSON_WHITE_SPACES + "]%')";
            case DM:
                return "(JSON_TYPE(" + mysqlJsonExtract(column, key) + ") = 'STRING' AND REGEXP_LIKE("
                        + mysqlJsonUnquote(column, key) + ", '[^" + JSON_WHITE_SPACES + "]'))";
            default:
                return "(JSON_TYPE(" + mysqlJsonExtract(column, key) + ") = 'STRING' AND "
                        + mysqlJsonUnquote(column, key) + " REGEXP '[^[:space:]]')";
        }
    }

    /**
     * 达梦：JSON_VALUE 取标量的文本，对象、数组返回 NULL。
     *
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return SQL 表达式
     */
    private static String dmJsonValue(String column, String key) {
        return "JSON_VALUE(" + column + ", " + mysqlJsonPath(key) + ")";
    }

    /**
     * 原有片段的 JSON 路径：字段编码不加引号，与原有片段一致；编号需加引号。
     *
     * @param key 已校验格式的键
     * @return SQL 字符串字面量
     */
    private static String mysqlJsonPath(String key) {
        char first = key.charAt(0);
        return first == '-' || Character.isDigit(first) ? jsonPathLiteral(key, "") : "'$." + key + "'";
    }

    private static String mysqlJsonExtract(String column, String key) {
        return "JSON_EXTRACT(" + column + ", " + mysqlJsonPath(key) + ")";
    }

    private static String mysqlJsonUnquote(String column, String key) {
        return "JSON_UNQUOTE(" + mysqlJsonExtract(column, key) + ")";
    }

    private static String postgreSQLJsonNode(String column, String key) {
        return "jsonb_extract_path(CAST(" + column + " AS jsonb), '" + key + "')";
    }

    private static String postgreSQLJsonText(String column, String key) {
        return "jsonb_extract_path_text(CAST(" + column + " AS jsonb), '" + key + "')";
    }

    /**
     * Oracle、SQL Server 的 JSON 路径字面量：键加双引号，可选追加路径方法或过滤条件。
     *
     * @param key 已校验格式的键
     * @param suffix 追加在键后的路径片段
     * @return SQL 字符串字面量
     */
    private static String jsonPathLiteral(String key, String suffix) {
        return "'$.\"" + key + "\"" + suffix + "'";
    }

    private static String oracleJsonValue(String column, String key, String suffix) {
        return "JSON_VALUE(" + column + ", " + jsonPathLiteral(key, suffix) + ")";
    }

    /**
     * Oracle：JSON_TABLE 展开键对应的值，每个数组元素为一行；路径为宽松模式，标量（含对象）作为单个元素，键缺失时没有行。
     * 列见 {@link #ORACLE_JSON_TABLE_COLUMNS}；只按 t 校验类型后比较 v，或按 r 的规范键比较数字
     *
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return FROM 片段，以空格开头，表别名为 yd_jt
     */
    private static String oracleJsonElements(String column, String key) {
        return " FROM JSON_TABLE(" + column + ", " + jsonPathLiteral(key, "[*]") + " COLUMNS (" + ORACLE_JSON_TABLE_COLUMNS + ")) yd_jt";
    }

    private static String oracleJsonType(Class<?> valueType) {
        return valueType == BigDecimal.class ? "number" : valueType == Boolean.class ? "boolean" : "string";
    }

    private static String sqlServerJsonValue(String column, String key) {
        return "JSON_VALUE(CAST(" + column + " AS nvarchar(max)), " + jsonPathLiteral(key, "") + ")";
    }

    /**
     * SQL Server：OPENJSON 展开 JSON 对象的直接成员并按键过滤；键列为 BIN2 排序规则，区分大小写。
     * 表别名使用 yd_j 前缀，避免遮蔽外层查询的别名
     *
     * @param column 代码中固定的 JSON 对象列名
     * @param key 已校验格式的键
     * @return FROM ... WHERE 片段，以空格开头
     */
    private static String sqlServerJsonMember(String column, String key) {
        return " FROM OPENJSON(CAST(" + column + " AS nvarchar(max))) yd_jo WHERE yd_jo.\"key\" = N'" + key + "'";
    }

    /**
     * SQL Server：OPENJSON 展开外层 yd_jo 行的数组元素，表别名为 yd_je。
     * 只有 yd_jo 的 type 为 4（数组）时才展开其值，其它输入替换为空数组：优化器可能先于外层的 type 条件执行 OPENJSON，
     * 标量文本（例如数字 9007199254740992）作为 OPENJSON 的输入会报 JSON 格式错误
     *
     * @return FROM 之后的表片段
     */
    private static String sqlServerJsonArrayElements() {
        return "OPENJSON(CASE WHEN yd_jo.\"type\" = 4 THEN yd_jo.\"value\" ELSE N'[]' END) yd_je";
    }

    /**
     * SQL Server：数字文本转为 DECIMAL(38, 10)，不经过 FLOAT；精度为 DECIMAL(38, 10)，超过 10 位的小数按其转换规则舍入，
     * 整数部分超过 28 位时为 NULL。
     *
     * 1. 不含 e、E 的十进制文本：TRY_CAST
     * 2. 科学计数法：尾数为一位整数（可带小数）或以小数点开头、指数最多两位（与 {@link #JSON_NUMBER_PATTERN} 一致），
     *    去掉尾数的小数点后按指数移动小数点、补零，展开为十进制文本再 TRY_CAST，例如 9.007199254740993e15 为 9007199254740993，
     *    1e-7 为 0.0000001；格式不符时为 NULL
     * 3. 各级 CROSS APPLY 只做取子串等不会报错的计算；LEFT、SUBSTRING 的长度参数均不为负数（REPLICATE 为负数时返回 NULL），
     *    即使优化器提前计算 CASE 的分支也不会报错
     *
     * 注意：极小的值（例如 1e-99）展开后的小数位多于 10 位，按 DECIMAL(38, 10) 的转换规则舍入，待实库验证
     *
     * @param text 数字文本表达式
     * @return 标量子查询，表别名使用 yd_jn 前缀
     */
    private static String sqlServerDecimal(String text) {
        // 尾数 m（不含符号）、指数 xt；d 为去掉小数点的数字，i 为小数点前的位数，p 为展开后小数点前的位数
        String m = "yd_jns.m";
        String xt = "yd_jns.xt";
        String d = "yd_jnd.d";
        String p = "(yd_jnd.i + yd_jnd.x)";
        return "(SELECT CASE WHEN yd_jne.e = 0 THEN TRY_CAST(yd_jnt.t AS DECIMAL(38, 10))"
                + " WHEN (" + m + " LIKE N'[0-9]' OR (" + m + " LIKE N'[0-9].[0-9]%' AND SUBSTRING(" + m + ", 3, 64) NOT LIKE N'%[^0-9]%')"
                + " OR (" + m + " LIKE N'.[0-9]%' AND SUBSTRING(" + m + ", 2, 64) NOT LIKE N'%[^0-9]%'))"
                + " AND (" + xt + " LIKE N'[0-9]' OR " + xt + " LIKE N'[0-9][0-9]' OR " + xt + " LIKE N'[+-][0-9]'"
                + " OR " + xt + " LIKE N'[+-][0-9][0-9]')"
                + " THEN TRY_CAST(yd_jns.s + CASE WHEN " + p + " >= LEN(" + d + ") THEN " + d + " + REPLICATE(N'0', " + p + " - LEN(" + d + "))"
                + " WHEN " + p + " <= 0 THEN N'0.' + REPLICATE(N'0', -" + p + ") + " + d
                + " ELSE LEFT(" + d + ", ABS" + p + ") + N'.' + SUBSTRING(" + d + ", " + p + " + 1, 64) END AS DECIMAL(38, 10)) END"
                + " FROM (SELECT " + text + " AS t) yd_jnt"
                + " CROSS APPLY (SELECT PATINDEX(N'%[eE]%', yd_jnt.t) AS e, CASE WHEN LEFT(yd_jnt.t, 1) = N'-' THEN 1 ELSE 0 END AS n) yd_jne"
                + " CROSS APPLY (SELECT CASE WHEN yd_jne.n = 1 THEN N'-' ELSE N'' END AS s,"
                + " SUBSTRING(yd_jnt.t, yd_jne.n + 1, CASE WHEN yd_jne.e > yd_jne.n + 1 THEN yd_jne.e - yd_jne.n - 1 ELSE 0 END) AS m,"
                + " SUBSTRING(yd_jnt.t, yd_jne.e + 1, 64) AS xt) yd_jns"
                + " CROSS APPLY (SELECT REPLACE(yd_jns.m, N'.', N'') AS d, CASE WHEN CHARINDEX(N'.', yd_jns.m) = 0 THEN LEN(yd_jns.m)"
                + " ELSE CHARINDEX(N'.', yd_jns.m) - 1 END AS i, TRY_CAST(yd_jns.xt AS INT) AS x) yd_jnd)";
    }

    // ========== 行数限制 ==========

    /**
     * 只返回前 rows 行，追加在语句末尾（ORDER BY 之后）；原有片段为 LIMIT rows
     *
     * 仅用于 XML 或注解中手写的 SQL；使用 Wrapper 查询时，使用 BaseMapperX 的 selectListLimitN(queryWrapper, limit) 分页查询
     *
     * 1. Oracle：FETCH FIRST rows ROWS ONLY，要求 Oracle 12c+
     * 2. SQL Server 2012+：OFFSET 0 ROWS FETCH NEXT rows ROWS ONLY；OFFSET 必须跟在 ORDER BY 之后，
     *    语句没有排序时补充 ORDER BY (SELECT NULL)，即不保证返回哪几行，与原有 LIMIT 无排序时一致
     *
     * @param rows    行数，不能为负数；SQL Server 的 FETCH 不支持 0 行
     * @param ordered 语句是否已有 ORDER BY
     * @return SQL 片段
     */
    public static String limitRows(int rows, boolean ordered) {
        return limitRows(getDialect(), rows, ordered);
    }

    /**
     * 生成固定行数限制；负数拒绝，SQL Server 的 FETCH 不接受 0 行。
     *
     * @param dialect 已识别的数据库方言
     * @param rows 固定行数
     * @param ordered 原查询是否已有排序
     * @return 追加在查询末尾的 SQL 片段
     */
    static String limitRows(Dialect dialect, int rows, boolean ordered) {
        if (rows < 0) {
            throw new IllegalArgumentException("行数不能为负数：" + rows);
        }
        if (rows == 0 && dialect == Dialect.SQLSERVER) {
            throw new IllegalArgumentException("SQL Server 的 FETCH 行数必须大于 0");
        }
        return limit(dialect, String.valueOf(rows), ordered);
    }

    /**
     * 只返回前 #{param} 行，行数使用 #{} 绑定；规则同 {@link #limitRows(int, boolean)}
     *
     * 在 XML 或注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@limitParam('limit', true)}
     *
     * @param param   行数的参数名，例如 limit
     * @param ordered 语句是否已有 ORDER BY
     * @return SQL 片段
     */
    public static String limitParam(String param, boolean ordered) {
        return limitParam(getDialect(), param, ordered);
    }

    /**
     * 生成绑定参数的行数限制，参数名通过白名单校验。
     *
     * @param dialect 已识别的数据库方言
     * @param param 行数参数名或属性路径
     * @param ordered 原查询是否已有排序
     * @return 包含 MyBatis 参数占位的 SQL 片段
     */
    static String limitParam(Dialect dialect, String param, boolean ordered) {
        checkParam(param);
        return limit(dialect, "#{" + param + "}", ordered);
    }

    /**
     * 拼装行数限制；rows 仅来自已校验的整数或 MyBatis 参数占位。
     *
     * @param dialect 已识别的数据库方言
     * @param rows 固定整数文本或已校验的参数占位
     * @param ordered 原查询是否已有排序
     * @return 行数限制 SQL 片段
     */
    private static String limit(Dialect dialect, String rows, boolean ordered) {
        switch (dialect) {
            case ORACLE:
                return "FETCH FIRST " + rows + " ROWS ONLY";
            case SQLSERVER:
                return (ordered ? "" : "ORDER BY (SELECT NULL) ") + "OFFSET 0 ROWS FETCH NEXT " + rows + " ROWS ONLY";
            default:
                return "LIMIT " + rows;
        }
    }

    // ========== 分批删除 ==========

    /**
     * 分批删除 column &lt; #{valueParam} 的记录，每次最多删除 #{limitParam} 行；
     * 原有语句为 DELETE FROM table WHERE column &lt; #{valueParam} LIMIT #{limitParam}
     *
     * 1. PostgreSQL、神通：DELETE 不支持 LIMIT（神通报语法错误），按主键 id 的子查询选出一批
     * 2. Oracle：ROWNUM &lt;= #{limitParam}
     * 3. SQL Server 2012+：按主键 id 的子查询 SELECT TOP (#{limitParam})。
     *    不使用 DELETE TOP (n)，因为 JSqlParser 无法解析，租户、数据权限插件解析 DELETE 时会失败
     *
     * 要求表的主键列为 id；表名、列名只能是代码中固定的常量，值均使用 #{} 绑定
     *
     * 在注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@deleteBatchByLessThan('infra_job_log', 'create_time', 'createTime', 'limit')}
     *
     * @param table      表名
     * @param column     比较的列名
     * @param valueParam 比较值的参数名
     * @param limitParam 每批行数的参数名
     * @return SQL 语句
     */
    public static String deleteBatchByLessThan(String table, String column, String valueParam, String limitParam) {
        return deleteBatchByLessThan(getDialect(), table, column, valueParam, limitParam);
    }

    /**
     * 按方言生成主键为 id 的分批物理删除；当前用于忽略租户的全局清理任务。
     *
     * @param dialect 已识别的数据库方言
     * @param table 代码中固定的表名
     * @param column 代码中固定的时间列名
     * @param valueParam 时间阈值参数名
     * @param limitParam 每批行数参数名
     * @return 参数绑定的 DELETE 语句
     */
    static String deleteBatchByLessThan(Dialect dialect, String table, String column,
                                        String valueParam, String limitParam) {
        checkColumn(table);
        checkColumn(column);
        checkParam(valueParam);
        checkParam(limitParam);
        String condition = column + " < #{" + valueParam + "}";
        String limit = "#{" + limitParam + "}";
        switch (dialect) {
            case POSTGRESQL:
            case OSCAR:
                return "DELETE FROM " + table + " WHERE id IN (SELECT id FROM " + table
                        + " WHERE " + condition + " LIMIT " + limit + ")";
            case ORACLE:
                return "DELETE FROM " + table + " WHERE " + condition + " AND ROWNUM <= " + limit;
            case SQLSERVER:
                return "DELETE FROM " + table + " WHERE id IN (SELECT TOP (" + limit + ") id FROM " + table
                        + " WHERE " + condition + ")";
            default:
                return "DELETE FROM " + table + " WHERE " + condition + " LIMIT " + limit;
        }
    }

    // ========== HTML 标签 ==========

    /**
     * 去除 HTML 标签：删除每一处 &lt;[^&gt;]*&gt;（不跨越 &gt;），未闭合的 &lt; 及其后文本保持原样，column 为 NULL 时结果为 NULL；
     * 原有片段为 REGEXP_REPLACE(column, '&lt;[^&gt;]*&gt;', '')
     *
     * 1. PostgreSQL（含人大金仓、openGauss、GaussDB、瀚高、MogDB 等兼容 PostgreSQL 的数据库）：REGEXP_REPLACE 默认只替换第一处，
     *    传 'g' 全局替换（openGauss、GaussDB、MogDB 待实库验收）
     * 2. Oracle、神通、原有片段：默认全局替换，且第 4 个参数含义不同（MySQL、Oracle、达梦为起始位置，H2 为匹配选项，神通不接受 'g'），
     *    保持原有写法
     * 3. SQL Server：没有 REGEXP_REPLACE，按 &gt; 拆分为 JSON 数组，OPENJSON 逐段处理后用 STRING_AGG 按段序拼接。
     *    仅适用于 SQL Server 2017+ 且数据库兼容级别 130+（STRING_AGG、OPENJSON），2012 ~ 2016 及兼容级别更低的数据库执行时报错；
     *    column 需为 nvarchar(max) 等字符类型，不支持 ntext、text。
     *    拆分与截取统一使用二进制排序规则，按 UTF-16 码元定位，不受全角 ＜＞、增补字符影响；拼接结果恢复为数据库默认排序规则，
     *    外层 LIKE 的大小写语义同数据库默认。列自定义排序规则与数据库默认不同时，比较按数据库默认排序规则进行
     *
     * @param column 列名
     * @return SQL 表达式
     */
    public static String stripHtmlTags(String column) {
        return stripHtmlTags(getDialect(), column);
    }

    /**
     * 按指定方言生成去除 HTML 标签的表达式。
     *
     * @param dialect 已识别的数据库方言
     * @param column 代码中固定的安全列名
     * @return SQL 表达式
     */
    static String stripHtmlTags(Dialect dialect, String column) {
        checkColumn(column);
        switch (dialect) {
            case POSTGRESQL:
                return "REGEXP_REPLACE(" + column + ", '<[^>]*>', '', 'g')";
            case SQLSERVER: {
                // 1. STRING_ESCAPE 转为 JSON 字符串内容（不转义 < 和 >），再以 > 为分隔符拼成 JSON 数组；OPENJSON 的 key 为段序号
                String segments = "OPENJSON(N'[\"' + REPLACE(STRING_ESCAPE(" + column + ", 'json')"
                        + " COLLATE Latin1_General_100_BIN2, N'>', N'\",\"') + N'\"]')";
                // 2. 非最后一段：标签始于段内第一个 <，保留其之前的文本；没有 < 时整段保留并补回分隔的 >。
                // 最后一段之后没有 >，原样保留。CHARINDEX 与 LEFT 使用同一个二进制排序规则，位置均按 UTF-16 码元计算
                String value = "yd_h.value COLLATE Latin1_General_100_BIN2";
                String part = "CASE WHEN yd_h.idx = yd_h.cnt - 1 THEN yd_h.value"
                        + " WHEN CHARINDEX(N'<', " + value + ") > 0 THEN LEFT(" + value + ", CHARINDEX(N'<', " + value + ") - 1)"
                        + " ELSE yd_h.value + N'>' END";
                // 3. 按段序拼接为 nvarchar(max)，避免 8000 字节截断；拼接结果恢复为数据库默认排序规则
                return "CAST((SELECT STRING_AGG(CAST(" + part + " AS nvarchar(max)), N'') WITHIN GROUP (ORDER BY yd_h.idx)"
                        + " FROM (SELECT CAST([key] AS int) AS idx, value, COUNT(*) OVER () AS cnt FROM " + segments + ") yd_h)"
                        + " AS nvarchar(max)) COLLATE DATABASE_DEFAULT";
            }
            default:
                return "REGEXP_REPLACE(" + column + ", '<[^>]*>', '')";
        }
    }

    // ========== LIKE 转义 ==========

    // DONE @AI：应该放到 sql utils 里？（处理：已从 CmsCommonUtils 移到本类，与其它按当前数据库类型处理的 SQL 方言工具放在一起，调用方直接使用本方法）
    /**
     * 转义 SQL LIKE 的通配符，使文本按字面量匹配
     *
     * 以 ! 作为转义符，依次转义 !、%、_；SQL 中需配合 ESCAPE '!' 使用。
     * SQL Server 的 LIKE 还会将 [...] 解释为字符类，文本含 [ 时按当前数据库类型再将 [ 转义为 ![；
     * 其它数据库的 [ 本身就是字面量，不转义（Oracle 的转义符后只能跟 %、_ 或转义符本身）
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    public static String escapeLike(String value) {
        String escaped = value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        // 不含 [ 时无需识别数据库类型
        if (escaped.indexOf('[') < 0 || !JdbcUtils.isSQLServer(JdbcUtils.getDbType())) {
            return escaped;
        }
        return escaped.replace("[", "![");
    }

    // ========== 布尔 ==========

    /**
     * 布尔列为真的条件：PostgreSQL、人大金仓、openGauss、GaussDB、神通、H2 为 column = TRUE，
     * MySQL、MariaDB、OceanBase、Oracle、SQL Server、达梦为 column = 1
     *
     * 1. 仅适用于按项目标准 DDL 映射的 Boolean 业务字段：MySQL 为 BIT(1)，经 sql/tools/convertor.py 转换后
     *    PostgreSQL 为 bool、Oracle 为 NUMBER(1, 0)、SQL Server 与达梦为 bit，人大金仓、openGauss、瀚高复用 PostgreSQL 的类型，
     *    神通按 openGauss 脚本导入；不适用于 used、deleted 等数值列，或自定义类型的列
     * 2. 按 {@link JdbcUtils#getDbType()} 识别：事务内复用已绑定连接，其它按当前路由到的数据源缓存；
     *    瀚高、通过 PostgreSQL 驱动连接的 MogDB 归为 PostgreSQL；未识别的数据库抛出异常，不按 MySQL 处理
     * 3. 只输出常量片段、不绑定参数，同一查询的 SELECT、GROUP BY 多次使用时表达式一致
     *
     * 在 XML 或注解中使用：${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@isTrue('t.new_visitor')}
     *
     * @param column 列名
     * @return SQL 条件片段
     */
    public static String isTrue(String column) {
        return isTrue(JdbcUtils.getDbType(), column);
    }

    /**
     * 按指定数据库类型生成布尔列为真的条件。
     *
     * @param dbType {@link JdbcUtils#getDbType()} 识别的数据库类型
     * @param column 代码中固定的安全列名
     * @return SQL 条件片段
     */
    static String isTrue(DbType dbType, String column) {
        checkColumn(column);
        if (dbType == null) {
            throw new IllegalStateException("布尔判断不支持的数据库类型：null");
        }
        switch (dbType) {
            case POSTGRE_SQL:
            case KINGBASE_ES:
            case OPENGAUSS:
            case GAUSS_DB:
            case OSCAR:
            case H2:
                return column + " = TRUE";
            case MYSQL:
            case MARIADB:
            case OCEAN_BASE:
            case ORACLE:
            case SQL_SERVER:
            case SQL_SERVER2005:
            case DM:
                return column + " = 1";
            default:
                throw new IllegalStateException("布尔判断不支持的数据库类型：" + dbType);
        }
    }

    // ========== 日期运算 ==========

    /**
     * 日期衰减得分：{0} 权重 * {1} 衰减系数 ^ (max(|{2} 评分时间 - column| 的天数 - {3} 偏移天数, 0) / {4} 衰减半径天数)；
     * 原有片段为 {0} * POWER({1}, GREATEST(ABS(TIMESTAMPDIFF(SECOND, column, {2})) / 86400E0 - {3}, 0) / {4})
     *
     * 1. {1} 需在 (0, 1) 之间，{4} 需大于 0；五个参数均须作为 Wrapper 的编号参数绑定传入，{2} 在片段中会出现多次。
     *    天数为向零截断的整秒（同 MySQL 的 TIMESTAMPDIFF）除以 86400 的浮点数，未来日期同样按距离衰减；column 为 NULL 时得分为 NULL
     * 2. MySQL、MariaDB、OceanBase、H2：原有片段。H2 仅用于单元测试，其 TIMESTAMPDIFF 按秒的边界计数，亚秒差与 MySQL 不同
     * 3. 达梦：TIMESTAMPDIFF 与 MySQL 一致，但除以 86400E0 按整数除法，需显式转为 DOUBLE；浮点运算的极小结果（约 1E-38 以下）为 0
     * 4. PostgreSQL（含瀚高、MogDB）、人大金仓、openGauss、GaussDB：EXTRACT(EPOCH) 后 TRUNC 截断；GREATEST 忽略 NULL，
     *    需显式保留 NULL；FLOAT8 的 POWER、乘法下溢时报错（MySQL 返回 0），因此权重、衰减系数、天数均转为 NUMERIC 计算，
     *    极远日期的得分仍保持大小次序，不按阈值压平
     * 5. Oracle：两列均升格为 TIMESTAMP(9) 相减，按 INTERVAL 的各部分求秒，不丢失小数秒；按 BINARY_DOUBLE 计算
     * 6. SQL Server 2012+：DATEDIFF(SECOND) 超过约 68 年会溢出，按天数差、日内秒差求整秒，再按小数秒向零修正；
     *    没有 GREATEST，用 CASE 保留 NULL；按 FLOAT 计算
     * 7. 神通：没有 TIMESTAMPDIFF，INTERVAL 不支持 EXTRACT，TIMESTAMP 最大精度为 6。同 SQL Server 按日期差、日内整秒差求整秒，
     *    日期差取 TRUNC 后两个零点之间的 DATEDIFF('DAY')，再按 TO_CHAR(..., 'US') 的六位微秒文本向零修正；column 为 NULL 时显式为 NULL。
     *    指数按 FLOAT8 计算，与 MySQL 的 DOUBLE 一致：宽 NUMERIC 的除法只保留约 9 位小数（1 / 86400 为 0.000011574），
     *    1 秒的得分即与 MySQL 相差约 5E-11。
     *    衰减部分按对数计算：神通的 POWER 在指数为 NUMERIC、BIGINT 时内部转为 FLOAT8，结果低于 FLOAT8 的正规数范围
     *    （例如 0.5 的 1074 次方）即报下溢错误，因此不对衰减系数直接 POWER。
     *    对数积 = 指数 * LN(衰减系数)；衰减系数小于 {@link #DOUBLE_MIN_NORMAL}（次正规数）时 FLOAT8 无法表示，
     *    按 {@link #OSCAR_SCORE_DECIMAL} 乘以 1E300 归一化后取对数，正规数仍按 LN(FLOAT8)，不损失精度。
     *    神通对未执行的 CASE 分支也会先转换参数，因此衰减系数只转为宽 NUMERIC，不直接转为 FLOAT8：
     *    正规数分支转换的是“次正规数取 1，否则取原值”的 CASE 结果，提前求值时也不会转换次正规数。
     *    同 MySQL 的 POWER 先得到 Double，衰减部分先舍入为 Double，再乘以权重：
     *    对数积不大于 {@link #DOUBLE_UNDERFLOW_LN}（MySQL DOUBLE 舍入为 0）时得分为 0，与 MySQL 一致，该边界以上的远日期仍按大小区分；
     *    小于 {@link #OSCAR_UNIT_GRID_LN} 时，衰减部分与 Double 最小单位 2^-1074 的比值 EXP(对数积 + {@link #DOUBLE_MIN_UNIT_SHIFT_LN})
     *    按 {@link #DOUBLE_TIES_EVEN_SHIFT} 就近舍入（平分取偶）为整数，乘以宽 NUMERIC 的 POWER(0.5, 1074)（整数常量指数，精确到
     *    500 位小数，不用 4.9E-324 的近似值）；否则衰减部分为 FLOAT8 的 EXP(对数积)。
     *    例如 0.5 的 1074.5 次方约为 0.707 个最小单位，与 MySQL 同为 1 个最小单位，再乘以权重。
     *    各分支 EXP 的参数先以 GREATEST 限定（次正规区为 -0.7 ~ 36.5，正规区不小于 -708），神通提前求值未执行的分支时也不会上溢或下溢。
     *    权重与乘积按 {@link #OSCAR_SCORE_DECIMAL} 计算，极小的权重、衰减系数（例如 4.9E-324）不丢失（NUMBER 将极小值转为 0），
     *    乘积不再舍入，读取为 Double 时舍入一次，与 MySQL 的 DOUBLE 乘法一致；
     *    精度：对数积按 FLOAT8 计算，衰减部分与 MySQL 的相对误差约为 |对数积| * 1E-16 量级（不超过约 1E-13）；
     *    次正规区先有该相对误差（不超过约 3E-13），再按最小单位舍入，因此可能多差 1 个最小单位；平分点只在比值准确为 0.5 的整数倍时与 MySQL 一致，
     *    比值超过 2^52（衰减部分为 2^-1022 ~ 3.3E-308）时取偶的间隔为 2，可能多差 1 个最小单位（相对误差不超过 2^-52）。
     *    本组原生验证的是神通 7.0.14、oscarJDBC8-4.1.152（元数据中的驱动版本为 7.20），其它版本、兼容模式仍需验收
     * 8. SQL Server 2005 等其它数据库未验收，抛出异常，不按 MySQL 处理
     *
     * 片段不含 %，可用于 MPJ 的 selectFunc（其模板会再经过 String.format）
     *
     * @param column 日期时间列名，例如 t.publish_time
     * @return 包含 {0} ~ {4} 占位符的 SQL 片段
     */
    public static String dateDecayScore(String column) {
        return dateDecayScore(JdbcUtils.getDbType(), column);
    }

    /**
     * 按指定数据库类型生成日期衰减得分。
     *
     * @param dbType {@link JdbcUtils#getDbType()} 识别的数据库类型
     * @param column 代码中固定的日期时间列名
     * @return 包含 {0} ~ {4} 占位符的 SQL 片段
     */
    static String dateDecayScore(DbType dbType, String column) {
        checkColumn(column);
        if (dbType == null) {
            throw new IllegalStateException("日期运算不支持的数据库类型：null");
        }
        switch (dbType) {
            case MYSQL:
            case MARIADB:
            case OCEAN_BASE:
            case H2:
                return "{0} * POWER({1}, GREATEST(ABS(TIMESTAMPDIFF(SECOND, " + column + ", {2})) / 86400E0 - {3}, 0) / {4})";
            case DM:
                return "{0} * POWER(CAST({1} AS DOUBLE), GREATEST(CAST(ABS(TIMESTAMPDIFF(SECOND, " + column + ", {2}))"
                        + " AS DOUBLE) / 86400 - {3}, 0) / {4})";
            case POSTGRE_SQL:
            case KINGBASE_ES:
            case OPENGAUSS:
            case GAUSS_DB: {
                // PostgreSQL 14 以下、openGauss 等的 EXTRACT 返回 FLOAT8，整秒截断后再转为 NUMERIC；
                // 权重也转为 NUMERIC，避免 Double 参数把乘积重新提升为 FLOAT8
                String days = "CAST(ABS(TRUNC(EXTRACT(EPOCH FROM (CAST({2} AS TIMESTAMP) - CAST(" + column
                        + " AS TIMESTAMP))))) AS NUMERIC) / 86400";
                return "CASE WHEN " + column + " IS NOT NULL THEN CAST({0} AS NUMERIC) * POWER(CAST({1} AS NUMERIC),"
                        + " GREATEST(" + days + " - {3}, 0) / {4}) END";
            }
            case ORACLE: {
                String interval = "(CAST({2} AS TIMESTAMP(9)) - CAST(" + column + " AS TIMESTAMP(9)))";
                String seconds = "EXTRACT(DAY FROM " + interval + ") * 86400 + EXTRACT(HOUR FROM " + interval + ") * 3600"
                        + " + EXTRACT(MINUTE FROM " + interval + ") * 60 + EXTRACT(SECOND FROM " + interval + ")";
                return "{0} * POWER(CAST({1} AS BINARY_DOUBLE), GREATEST(CAST(ABS(TRUNC(" + seconds + ")) AS BINARY_DOUBLE)"
                        + " / 86400 - {3}, 0) / {4})";
            }
            case SQL_SERVER: {
                String begin = "CAST(" + column + " AS datetime2(7))";
                String end = "CAST({2} AS datetime2(7))";
                // column 为 NULL 时比较结果未知，走 ELSE 分支得到 NULL
                String days = "CAST(CASE WHEN " + end + " >= " + begin + " THEN " + sqlServerWholeSeconds(begin, end)
                        + " ELSE " + sqlServerWholeSeconds(end, begin) + " END AS FLOAT) / 86400 - {3}";
                return "{0} * POWER(CAST({1} AS FLOAT), (CASE WHEN " + days + " < 0 THEN 0 ELSE " + days + " END) / {4})";
            }
            case OSCAR: {
                String begin = "CAST(" + column + " AS TIMESTAMP(6))";
                String end = "CAST({2} AS TIMESTAMP(6))";
                // 指数按 FLOAT8 计算：宽 NUMERIC 的除法只保留约 9 位小数；整秒为整数，FLOAT8 可准确表示
                String exponent = "GREATEST(CAST(CASE WHEN " + end + " >= " + begin + " THEN " + oscarWholeSeconds(begin, end)
                        + " ELSE " + oscarWholeSeconds(end, begin) + " END AS FLOAT8) / 86400 - CAST({3} AS FLOAT8), 0)"
                        + " / CAST({4} AS FLOAT8)";
                // 衰减系数的自然对数：次正规数不能转为 FLOAT8，按宽 NUMERIC 乘以 1E300 归一化后再取对数
                // 神通即使不执行 ELSE 分支也会先转换其中的参数，{1} 只转为宽 NUMERIC：ELSE 分支对次正规数改为转换 1
                String decay = "CAST({1} AS " + OSCAR_SCORE_DECIMAL + ")";
                String subnormal = decay + " < CAST(" + DOUBLE_MIN_NORMAL + " AS " + OSCAR_SCORE_DECIMAL + ")";
                String lnDecay = "CASE WHEN " + subnormal
                        + " THEN LN(" + decay + " * CAST(1E300 AS " + OSCAR_SCORE_DECIMAL + ")) - LN(CAST(1E300 AS FLOAT8))"
                        + " ELSE LN(CAST(CASE WHEN " + subnormal + " THEN 1 ELSE " + decay + " END AS FLOAT8)) END";
                String lnScore = "(" + exponent + ") * (" + lnDecay + ")";
                // 同 MySQL 先将衰减部分舍入为 Double，再乘以权重。各分支的 EXP 参数均先限定在安全范围内，提前求值时也不会上溢、下溢
                // 1. 正规区：EXP 的 FLOAT8 结果即为舍入后的 Double
                String normal = "CAST(EXP(GREATEST(" + lnScore + ", CAST(" + OSCAR_UNIT_GRID_LN + " AS FLOAT8))) AS "
                        + OSCAR_SCORE_DECIMAL + ")";
                // 2. 次正规区：FLOAT8 无法表示，按与最小单位 2^-1074 的比值就近舍入（平分取偶）为整数，再乘以宽 NUMERIC 的 2^-1074
                String ratio = "EXP(CAST(" + DOUBLE_MIN_UNIT_SHIFT_LN + " AS FLOAT8) - GREATEST(CAST(" + -OSCAR_UNIT_GRID_LN
                        + " AS FLOAT8), 0 - GREATEST(" + lnScore + ", CAST(" + DOUBLE_UNDERFLOW_LN + " AS FLOAT8))))";
                String tiesEven = "CAST(" + DOUBLE_TIES_EVEN_SHIFT + " AS FLOAT8)";
                String units = "CAST(CAST((" + ratio + " + " + tiesEven + ") - " + tiesEven + " AS BIGINT) AS "
                        + OSCAR_SCORE_DECIMAL + ") * POWER(CAST(0.5 AS " + OSCAR_SCORE_DECIMAL + "), 1074)";
                // column 为 NULL 时显式为 NULL，不依赖 GREATEST 的 NULL 语义；按 MySQL DOUBLE 下溢为 0 时不计算 EXP
                return "CASE WHEN " + column + " IS NULL THEN NULL"
                        + " WHEN " + lnScore + " <= " + DOUBLE_UNDERFLOW_LN + " THEN 0"
                        + " WHEN " + lnScore + " < " + OSCAR_UNIT_GRID_LN
                        + " THEN CAST({0} AS " + OSCAR_SCORE_DECIMAL + ") * (" + units + ")"
                        + " ELSE CAST({0} AS " + OSCAR_SCORE_DECIMAL + ") * " + normal + " END";
            }
            default:
                throw new IllegalStateException("日期运算不支持的数据库类型：" + dbType);
        }
    }

    /**
     * SQL Server：begin 不晚于 end 时，两者之间向零截断的整秒数，不受 DATEDIFF(SECOND) 的 int 范围限制。
     * 天数差、time(7) 的日内秒差均按边界计数，合计为两者整秒部分之差；end 的小数秒小于 begin 时，最后一秒不完整，减 1
     *
     * @param begin 较早的 datetime2(7) 表达式
     * @param end   较晚的 datetime2(7) 表达式
     * @return bigint 整秒 SQL 表达式
     */
    private static String sqlServerWholeSeconds(String begin, String end) {
        return "(CAST(DATEDIFF(DAY, " + begin + ", " + end + ") AS BIGINT) * 86400"
                + " + DATEDIFF(SECOND, CAST(" + begin + " AS time(7)), CAST(" + end + " AS time(7)))"
                + " - CASE WHEN DATEPART(NANOSECOND, " + end + ") < DATEPART(NANOSECOND, " + begin + ") THEN 1 ELSE 0 END)";
    }

    /**
     * 神通：begin 不晚于 end 时，两者之间向零截断的整秒数。
     * 两个零点之间的 DATEDIFF('DAY') 为准确的日期差，加上时、分、整秒之差，合计为两者整秒部分之差；
     * end 的微秒小于 begin 时，最后一秒不完整，减 1。微秒按 TO_CHAR 的六位文本比较，不使用 EXTRACT(SECOND) 的浮点小数
     *
     * @param begin 较早的 TIMESTAMP(6) 表达式
     * @param end   较晚的 TIMESTAMP(6) 表达式
     * @return 整秒 SQL 表达式
     */
    private static String oscarWholeSeconds(String begin, String end) {
        return "(CAST(DATEDIFF('DAY', TRUNC(" + begin + "), TRUNC(" + end + ")) AS BIGINT) * 86400"
                + " + (EXTRACT(HOUR FROM " + end + ") - EXTRACT(HOUR FROM " + begin + ")) * 3600"
                + " + (EXTRACT(MINUTE FROM " + end + ") - EXTRACT(MINUTE FROM " + begin + ")) * 60"
                + " + TRUNC(EXTRACT(SECOND FROM " + end + ")) - TRUNC(EXTRACT(SECOND FROM " + begin + "))"
                + " - CASE WHEN TO_CHAR(" + end + ", 'US') < TO_CHAR(" + begin + ", 'US') THEN 1 ELSE 0 END)";
    }

    /**
     * 毫秒递增版本的 UPDATE 赋值片段：column = max({0}, column + 1 毫秒)，column 为 NULL 时为 {0}，供 UpdateWrapper 的 setSql 使用；
     * 原有片段为 column = GREATEST(CAST({0} AS DATETIME(3)), COALESCE(TIMESTAMPADD(MICROSECOND, 1000, column), CAST({0} AS DATETIME(3))))
     *
     * 1. {0} 为截断到毫秒的当前时间，须作为编号参数绑定传入，在片段中会出现多次；在单条 UPDATE 内基于原值计算，
     *    时钟回拨、同一毫秒内的并发更新，版本仍严格递增
     * 2. MySQL、MariaDB、OceanBase、H2、达梦：原有片段
     * 3. PostgreSQL（含瀚高、MogDB）、人大金仓、openGauss、GaussDB：参数显式转为 TIMESTAMP(3)，加 INTERVAL '0.001' SECOND
     * 4. Oracle：转为 TIMESTAMP(9) 计算，兼容 DATE、TIMESTAMP 列；注意 DATE 列只保存到秒，无法严格递增，需使用 TIMESTAMP(3) 及以上精度的列
     * 5. SQL Server 2012+：没有 GREATEST，用 CASE 与 DATEADD(MILLISECOND) 计算；需使用 datetime2 列，datetime 的精度约为 3.33 毫秒
     * 6. 神通：没有 TIMESTAMPADD；同 SQL Server 用 CASE 计算，不依赖 GREATEST 的 NULL 语义，加 INTERVAL '0.001' SECOND
     * 7. SQL Server 2005 等其它数据库未验收，抛出异常，不按 MySQL 处理
     *
     * 注意：严格递增依赖列能保存毫秒，片段不改变列的精度。MySQL 需为 DATETIME(3) 及以上精度（DATETIME 会舍入到秒）；
     * Oracle 的 DATE、SQL Server 的 datetime 列写回时按列的精度舍弃或舍入毫秒，同一秒（或 3.33 毫秒）内的更新不再递增，
     * 需将列迁移为 TIMESTAMP(3)、datetime2(3) 及以上精度，已有的旧表不会因本片段而升级
     *
     * 在 Wrapper 中使用：setSql(SqlDialectUtils.setMillisVersion("live_update_time"), now)
     *
     * @param column 日期时间列名
     * @return 包含 {0} 占位符的 SET 片段
     */
    public static String setMillisVersion(String column) {
        return setMillisVersion(JdbcUtils.getDbType(), column);
    }

    /**
     * 按指定数据库类型生成毫秒递增版本的 UPDATE 赋值片段。
     *
     * @param dbType {@link JdbcUtils#getDbType()} 识别的数据库类型
     * @param column 代码中固定的日期时间列名
     * @return 包含 {0} 占位符的 SET 片段
     */
    static String setMillisVersion(DbType dbType, String column) {
        checkColumn(column);
        if (dbType == null) {
            throw new IllegalStateException("日期运算不支持的数据库类型：null");
        }
        switch (dbType) {
            case MYSQL:
            case MARIADB:
            case OCEAN_BASE:
            case H2:
            case DM:
                return column + " = GREATEST(CAST({0} AS DATETIME(3)), COALESCE(TIMESTAMPADD(MICROSECOND, 1000, "
                        + column + "), CAST({0} AS DATETIME(3))))";
            case POSTGRE_SQL:
            case KINGBASE_ES:
            case OPENGAUSS:
            case GAUSS_DB:
                return column + " = GREATEST(CAST({0} AS TIMESTAMP(3)), COALESCE(" + column
                        + " + INTERVAL '0.001' SECOND, CAST({0} AS TIMESTAMP(3))))";
            case ORACLE:
                return column + " = GREATEST(CAST({0} AS TIMESTAMP(9)), COALESCE(CAST(" + column
                        + " AS TIMESTAMP(9)) + INTERVAL '0.001' SECOND, CAST({0} AS TIMESTAMP(9))))";
            case SQL_SERVER: {
                String next = "DATEADD(MILLISECOND, 1, " + column + ")";
                return column + " = CASE WHEN " + column + " IS NULL OR " + next + " < CAST({0} AS datetime2(3))"
                        + " THEN CAST({0} AS datetime2(3)) ELSE " + next + " END";
            }
            case OSCAR: {
                String next = column + " + INTERVAL '0.001' SECOND";
                return column + " = CASE WHEN " + column + " IS NULL OR " + next + " < CAST({0} AS TIMESTAMP(3))"
                        + " THEN CAST({0} AS TIMESTAMP(3)) ELSE " + next + " END";
            }
            default:
                throw new IllegalStateException("日期运算不支持的数据库类型：" + dbType);
        }
    }

    // ========== 方言识别 ==========

    /**
     * 是否输出原有普通片段：原有方言，以及只在个别片段单独处理的 H2、达梦
     *
     * @param dialect 已识别的数据库方言
     * @return 是否为原有片段
     */
    private static boolean isOriginal(Dialect dialect) {
        return dialect == Dialect.ORIGINAL || dialect == Dialect.H2 || dialect == Dialect.DM;
    }

    /**
     * 按数据库元数据识别方言的规则：{@link #resolveDialect(DatabaseMetaData)}、{@link #resolveJsonDialect(DatabaseMetaData)}
     * 或 {@link #resolveJsonValueDialect(DatabaseMetaData)}
     */
    @FunctionalInterface
    private interface DialectResolver {

        Dialect resolve(DatabaseMetaData metaData) throws SQLException;

    }

    /**
     * 取得当前执行方言；连接与缓存规则见 {@link #getDialect(Map, DialectResolver)}
     *
     * @return 当前执行方言
     */
    static Dialect getDialect() {
        return getDialect(DIALECT_CACHE, SqlDialectUtils::resolveDialect);
    }

    /**
     * 按数据源实例缓存方言；识别失败时不写入缓存。
     *
     * @param dataSource 实际路由到的数据源
     * @return 数据库方言
     */
    static Dialect getDialect(DataSource dataSource) {
        return DIALECT_CACHE.computeIfAbsent(dataSource, key -> detectDialect(key, SqlDialectUtils::resolveDialect));
    }

    /**
     * 取得当前执行的 JSON 方言，仅 JSON 数组片段使用；连接与缓存规则见 {@link #getDialect(Map, DialectResolver)}
     *
     * @return JSON 方言
     */
    static Dialect getJsonDialect() {
        return getDialect(JSON_DIALECT_CACHE, SqlDialectUtils::resolveJsonDialect);
    }

    /**
     * 取得当前执行的 JSON 字段值方言；连接与缓存规则见 {@link #getDialect(Map, DialectResolver)}
     *
     * @return JSON 字段值方言
     */
    private static Dialect getJsonValueDialect() {
        return getDialect(JSON_VALUE_DIALECT_CACHE, SqlDialectUtils::resolveJsonValueDialect);
    }

    /**
     * 按规则识别当前执行方言：事务内复用已绑定连接，不缓存；非事务按路由后的数据源实例缓存，识别失败（含规则拒绝的数据库）时不写入缓存。
     * 各规则使用各自的缓存，同一数据源的普通片段、JSON 片段可以识别为不同方言
     *
     * @param cache 该规则的方言缓存
     * @param resolver 识别规则
     * @return 数据库方言
     */
    private static Dialect getDialect(Map<DataSource, Dialect> cache, DialectResolver resolver) {
        Connection connection = JdbcUtils.getTransactionalConnection();
        if (connection == null) {
            return cache.computeIfAbsent(JdbcUtils.getDataSource(), dataSource -> detectDialect(dataSource, resolver));
        }
        try {
            // 使用真正执行 SQL 的事务连接；不能将其方言缓存到事务内新切换的路由键上
            return resolver.resolve(connection.getMetaData());
        } catch (SQLException e) {
            throw new IllegalStateException("获取数据库方言失败", e);
        }
    }

    /**
     * 借用一个连接识别数据源方言，并关闭该次自行获取的连接。
     *
     * @param dataSource 待识别的数据源
     * @param resolver 识别规则
     * @return 数据库方言
     */
    private static Dialect detectDialect(DataSource dataSource, DialectResolver resolver) {
        try (Connection conn = dataSource.getConnection()) {
            return resolver.resolve(conn.getMetaData());
        } catch (SQLException e) {
            throw new IllegalStateException("获取数据库方言失败", e);
        }
    }

    /**
     * 识别 JSON 方言，其它与 {@link #resolveProductDialect(DatabaseMetaData)} 一致：
     * 1. 兼容 PostgreSQL 的数据库（见 {@link #isPostgreSQLVariant(DbType, String)}）的普通片段按 PostgreSQL 处理，
     *    JSON 片段只有以下数据库按 PostgreSQL 处理，其它（例如产品名为 PostgreSQL 的人大金仓原生驱动）未验收，保持原有片段：
     *    1.1 瀚高原生驱动（jdbc:highgo:）、人大金仓（产品名 KingbaseES）：已验收 jsonb 写法、且不支持 JSON_CONTAINS
     *    1.2 openGauss、GaussDB（产品名 openGauss、GaussDB，或产品名为 PostgreSQL、未登记时的原生驱动 jdbc:opengauss:、jdbc:gaussdb:、
     *        jdbc:mogdb:）：项目要求 PG 兼容模式，原有 JSON_CONTAINS 不存在、或对超出 2^53 的相邻编号匹配不精确
     * 2. H2：不支持 JSON_CONTAINS，使用 {@link Dialect#H2} 的严格平面 Long 数组片段
     * 3. 达梦、神通、SQL Server 2012 以下：保持原有片段，不随 {@link #resolveDialect(DatabaseMetaData)} 的单独识别变化
     *
     * @param metaData 数据库元数据
     * @return JSON 方言
     * @throws SQLException 读取元数据失败
     */
    static Dialect resolveJsonDialect(DatabaseMetaData metaData) throws SQLException {
        Dialect dialect = resolveProductDialect(metaData);
        String url = StrUtil.nullToEmpty(metaData.getURL()).toLowerCase(Locale.ROOT);
        DbType dbType = DbTypeEnum.find(metaData.getDatabaseProductName());
        if (isPostgreSQLVariant(dbType, url)) {
            if ((url.startsWith(HIGHGO_URL_PREFIX) || StrUtil.startWithAny(url, OPENGAUSS_URL_PREFIXES))
                    && (dbType == null || dbType == DbType.POSTGRE_SQL)) {
                return Dialect.POSTGRESQL;
            }
            if (JSON_POSTGRESQL_DB_TYPES.contains(dbType)) {
                return Dialect.POSTGRESQL;
            }
            return Dialect.ORIGINAL;
        }
        if (dbType == DbType.H2) {
            return Dialect.H2;
        }
        return dialect;
    }

    /**
     * 识别 JSON 字段值的方言：在 {@link #resolveProductDialect(DatabaseMetaData)} 的基础上，单独识别达梦。
     * 达梦不支持中缀 REGEXP，CAST(JSON_UNQUOTE(...) AS DECIMAL) 报错，CAST(? AS JSON) 不接受 Long 参数，不能使用原有片段；
     * 神通、SQL Server 2012 以下保持原有片段，不随 {@link #resolveDialect(DatabaseMetaData)} 的单独识别变化
     *
     * @param metaData 数据库元数据
     * @return JSON 字段值方言
     * @throws SQLException 读取元数据失败
     */
    static Dialect resolveJsonValueDialect(DatabaseMetaData metaData) throws SQLException {
        Dialect dialect = resolveProductDialect(metaData);
        return dialect == Dialect.ORIGINAL && DbTypeEnum.find(metaData.getDatabaseProductName()) == DbType.DM
                ? Dialect.DM : dialect;
    }

    /**
     * 根据数据库元数据识别普通片段的方言：在 {@link #resolveProductDialect(DatabaseMetaData)} 的基础上
     *
     * 1. 达梦、神通单独识别为 {@link Dialect#DM}、{@link Dialect#OSCAR}，未单独处理的片段与原有片段相同
     * 2. SQL Server 2012 以下（含产品名为 Microsoft SQL Server 2005 的驱动）：原有的 DATE_FORMAT、LIMIT、FIND_IN_SET 等
     *    MySQL 片段无法执行，未验收，抛出异常，不按 MySQL 处理
     *
     * @param metaData 数据库元数据
     * @return 方言
     * @throws SQLException 读取元数据失败
     */
    static Dialect resolveDialect(DatabaseMetaData metaData) throws SQLException {
        Dialect dialect = resolveProductDialect(metaData);
        if (dialect != Dialect.ORIGINAL) {
            return dialect;
        }
        String productName = metaData.getDatabaseProductName();
        DbType dbType = DbTypeEnum.find(productName);
        if (dbType == DbType.DM) {
            return Dialect.DM;
        }
        if (dbType == DbType.OSCAR) {
            return Dialect.OSCAR;
        }
        if (JdbcUtils.isSQLServer(dbType)) {
            throw new IllegalStateException("SQL 方言不支持 SQL Server 2012 以下的版本：" + productName
                    + "，主版本号 " + metaData.getDatabaseMajorVersion());
        }
        return dialect;
    }

    /**
     * 根据数据库元数据识别数据库产品对应的方言，JSON 片段以此为准，普通片段见 {@link #resolveDialect(DatabaseMetaData)}
     *
     * 1. 兼容 PostgreSQL 的数据库（见 {@link #isPostgreSQLVariant(DbType, String)}）按 PostgreSQL 处理；
     *    与 {@link JdbcUtils#getDbType(String)} 将瀚高、openGauss、GaussDB 原生驱动归为 PostgreSQL 一致，人大金仓按产品名、原生驱动 URL 识别
     * 2. 产品名为 PostgreSQL 时，URL 需为 PostgreSQL 官方驱动或已登记的原生驱动，其它（例如 Vastbase）未验收，抛出异常
     * 3. MySQL、MariaDB、OceanBase、H2、达梦、神通，以及 SQL Server 2012 以下为 {@link Dialect#ORIGINAL}
     *
     * 注意：使用 PostgreSQL 官方驱动（jdbc:postgresql:）连接 openGauss、瀚高等兼容 PostgreSQL 的数据库时，
     * 产品名与 URL 均与 PostgreSQL 相同，无法区分，会按 PostgreSQL 处理
     *
     * @param metaData 数据库元数据
     * @return 方言
     * @throws SQLException 读取元数据失败
     */
    private static Dialect resolveProductDialect(DatabaseMetaData metaData) throws SQLException {
        String productName = metaData.getDatabaseProductName();
        String url = StrUtil.nullToEmpty(metaData.getURL()).toLowerCase(Locale.ROOT);
        DbType dbType = DbTypeEnum.find(productName);
        if (dbType == DbType.ORACLE) {
            return Dialect.ORACLE;
        }
        if (dbType == DbType.SQL_SERVER) {
            return metaData.getDatabaseMajorVersion() >= SQL_SERVER_2012_MAJOR_VERSION
                    ? Dialect.SQLSERVER : Dialect.ORIGINAL;
        }
        if (isPostgreSQLVariant(dbType, url)) {
            return Dialect.POSTGRESQL;
        }
        if (dbType == DbType.POSTGRE_SQL) {
            if (url.contains(":postgresql:")) {
                return Dialect.POSTGRESQL;
            }
            throw unsupported(productName);
        }
        if (ORIGINAL_DB_TYPES.contains(dbType)) {
            return Dialect.ORIGINAL;
        }
        // 产品名未登记时，仅按明确的原生驱动 URL 识别 MariaDB（与 JdbcUtils 一致）；瀚高、MogDB 等见 isPostgreSQLVariant
        if (dbType == null && url.contains(":mariadb:")) {
            return Dialect.ORIGINAL;
        }
        throw unsupported(productName);
    }

    /**
     * 判断是否为兼容 PostgreSQL 的数据库（不含 PostgreSQL 官方驱动连接的 PostgreSQL 本身）：
     * 产品名为人大金仓、openGauss、GaussDB；或产品名为 PostgreSQL、未登记时，URL 的 JDBC 协议为已登记的原生驱动（瀚高、openGauss、GaussDB、MogDB、人大金仓），
     * 见 {@link #POSTGRESQL_VARIANT_URL_PATTERN}。
     * 其它产品名（例如 MySQL、达梦）即使 URL 命中，也不按 PostgreSQL 处理
     *
     * @param dbType 按产品名识别的数据库类型，未登记时为 null
     * @param url 转为小写的 JDBC URL
     * @return 是否兼容 PostgreSQL
     */
    private static boolean isPostgreSQLVariant(DbType dbType, String url) {
        if (POSTGRESQL_VARIANT_DB_TYPES.contains(dbType)) {
            return true;
        }
        return (dbType == null || dbType == DbType.POSTGRE_SQL) && POSTGRESQL_VARIANT_URL_PATTERN.matcher(url).find();
    }

    /**
     * 创建未识别产品的异常，不将完整 JDBC URL 写入消息。
     *
     * @param productName 数据库产品名
     * @return 不支持该产品的异常
     */
    private static IllegalStateException unsupported(String productName) {
        return new IllegalStateException("SQL 方言不支持的数据库产品：" + productName);
    }

    /**
     * 校验代码中固定的表名或列名，拒绝表达式与语句片段。
     *
     * @param column 待校验的表名或列名
     */
    private static void checkColumn(String column) {
        if (!MyBatisUtils.isSafeColumnName(column)) {
            throw new IllegalArgumentException("Invalid column name: " + column);
        }
    }

    /**
     * 校验 JSON 对象的键，见 {@link #JSON_KEY_PATTERN}；键会拼接为 JSON 路径字面量，拒绝引号、点号等路径与 SQL 字符。
     *
     * @param key 待校验的键
     */
    private static void checkJsonKey(String key) {
        if (key == null || !JSON_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid json key: " + key);
        }
    }

    /**
     * 校验 JSON 值比较的值类型。
     *
     * @param valueType 值类型：String、BigDecimal、Boolean
     */
    private static void checkJsonValueType(Class<?> valueType) {
        if (valueType != String.class && valueType != BigDecimal.class && valueType != Boolean.class) {
            throw new IllegalArgumentException("Invalid json value type: " + valueType);
        }
    }

    /**
     * 校验代码中固定的 ID 列名：在列名校验的基础上，最后一段需要为 id 或以 _id 结尾，避免用于任意业务字符串列。
     *
     * @param column 待校验的列名，可带表别名前缀
     */
    private static void checkIdColumn(String column) {
        checkColumn(column);
        String name = column.substring(column.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!name.equals("id") && !name.endsWith("_id")) {
            throw new IllegalArgumentException("Invalid id column name: " + column);
        }
    }

    /**
     * 校验 MyBatis 参数名或属性路径，拒绝占位符逃逸与 SQL 片段。
     *
     * @param param 待校验的参数名
     */
    private static void checkParam(String param) {
        if (param == null || !PARAM_NAME_PATTERN.matcher(param).matches()) {
            throw new IllegalArgumentException("Invalid param name: " + param);
        }
    }

}
