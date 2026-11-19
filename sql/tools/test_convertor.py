# encoding=utf8
"""convertor.py 的单元测试

cd sql/tools
uv run --with simple-ddl-parser python -m unittest test_convertor -v
"""

import contextlib
import copy
import io
import os
import re
import subprocess
import sys
import tempfile
import unittest

from convertor import (
    DM8Convertor,
    HighGoConvertor,
    KingbaseConvertor,
    OpengaussConvertor,
    OracleConvertor,
    PostgreSQLConvertor,
    SQLServerConvertor,
    boolean_value,
    convert_current_time_default,
    ddl_comments,
    decimal_type,
    decode_mysql_string,
    float_type,
    integer_type,
    protect_default_numbers,
    split_statements,
    table_options_end,
)
from simple_ddl_parser import DDLParser

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
MYSQL_DIR = os.path.join(TOOLS_DIR, "..", "mysql")

HEADER = """/*
 Navicat Premium Data Transfer

 Source Server Type    : MySQL
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
"""

DEMO_TABLE = """
-- ----------------------------
-- Table structure for demo
-- ----------------------------
DROP TABLE IF EXISTS `demo`;
CREATE TABLE `demo`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '名称;别名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '演示表';

-- ----------------------------
-- Records of demo
-- ----------------------------
BEGIN;
INSERT INTO `demo` (`id`, `name`) VALUES (1, 'a;b');
COMMIT;
"""

DEMO_DOC_TABLE = """
DROP TABLE IF EXISTS `demo_doc`;
CREATE TABLE `demo_doc`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档表';
"""

# INSERT 数据中的建表文本：转义换行（issue #1273 的实际场景）
ESCAPED_NEWLINE_INSERT = r"""
INSERT INTO `demo_doc` (`id`, `content`) VALUES (1, 'Seata:\nCREATE TABLE `undo_log` (\n  `id` bigint NOT NULL,\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB;\n');
"""

# INSERT 数据中的建表文本：真实换行
LITERAL_NEWLINE_INSERT = """
INSERT INTO `demo_doc` (`id`, `content`) VALUES (2, 'line1
CREATE TABLE `ghost` (
  `id` bigint NOT NULL
);
line3');
"""

# 注释中的建表文本
COMMENTED_DDL = """
/* CREATE TABLE `ghost_block` (`id` bigint NOT NULL); */
-- CREATE TABLE `ghost_line` (`id` bigint NOT NULL);
# CREATE TABLE `ghost_hash` (`id` bigint NOT NULL);
"""


def table_names(table_script_list):
    return [re.match(r"CREATE\s+TABLE\s+`([^`]+)`", script).group(1) for script in table_script_list]


def convert(convertor):
    """执行转换，返回 stdout、stderr 的内容"""
    out, err = io.StringIO(), io.StringIO()
    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
        convertor.print()
    return out.getvalue(), err.getvalue()


def sql_string(value, prefix=""):
    """解码后的字符串值 -> 标准 SQL 字符串：单引号双写，反斜杠、换行等字符保持原样；SQL Server 带 N 前缀"""
    return prefix + "'" + value.replace("'", "''") + "'"


class SplitStatementsTest(unittest.TestCase):

    def test_semicolon_in_quotes(self):
        sql = r"""INSERT INTO `t` VALUES ('a;b', "c;d", 'it\'s;', 'x''y;', "q\";", 'C:\\');
INSERT INTO `we;ird` VALUES (1);"""
        self.assertEqual(sql.split("\n"), split_statements(sql))

    def test_semicolon_in_comments(self):
        sql = """-- comment; here
# hash; it's
/* block; here */ SELECT 1;
CREATE TABLE `c` (
  `id` bigint -- 编号; 注释
);"""
        self.assertEqual(["SELECT 1;", "CREATE TABLE `c` (\n  `id` bigint -- 编号; 注释\n);"], split_statements(sql))

    def test_double_dash_without_space(self):
        # MySQL 中 -- 后面必须跟空白，才是注释
        self.assertEqual(["SELECT 5--2;"], split_statements("SELECT 5--2;"))

    def test_multiple_statements_one_line(self):
        self.assertEqual(["SET NAMES utf8mb4;", "SET FOREIGN_KEY_CHECKS = 0;"],
                         split_statements("SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS = 0;"))

    def test_unterminated(self):
        # 未闭合的引号、缺少分号的结尾，都不作为语句
        self.assertEqual(["SELECT 1;"], split_statements("SELECT 1; INSERT INTO `t` VALUES ('a;"))
        self.assertEqual(["SELECT 1;"], split_statements("SELECT 1;\nSELECT 2"))


class ConvertorTableScanTest(unittest.TestCase):

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_create_table_in_insert_ignored(self):
        path = self.write_sql(HEADER + DEMO_DOC_TABLE + ESCAPED_NEWLINE_INSERT + LITERAL_NEWLINE_INSERT
                              + COMMENTED_DDL + DEMO_TABLE)
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                OracleConvertor, SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                self.assertEqual(["demo_doc", "demo"], table_names(convertor.table_script_list))

                out, err = convert(convertor)
                self.assertNotIn("无法正常解析", err)
                self.assertIn("Table structure for demo_doc", out)
                self.assertIn("Table structure for demo\n", out)
                for ghost in ("undo_log", "ghost", "ghost_block", "ghost_line", "ghost_hash"):
                    self.assertNotIn(f"Table structure for {ghost}\n", out)

    def test_comment_from_real_table(self):
        # 前面 INSERT 数据中存在同名的建表文本，不能影响真实建表语句的注释
        fake_insert = """
INSERT INTO `demo_doc` (`id`, `content`) VALUES (1, 'CREATE TABLE `demo`  (
  `name` varchar(64) NOT NULL COMMENT \\'假名称\\',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = \\'假表\\';
');
"""
        path = self.write_sql(HEADER + DEMO_DOC_TABLE + fake_insert + DEMO_TABLE)
        convertor = PostgreSQLConvertor(path)
        self.assertEqual(["demo_doc", "demo"], table_names(convertor.table_script_list))
        # 保留原始建表语句
        self.assertIn("COMMENT '名称;别名'", convertor.original_table_script_list[1])

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertIn("COMMENT ON COLUMN demo.id IS '编号';", out)
        self.assertIn("COMMENT ON COLUMN demo.name IS '名称;别名';", out)
        self.assertIn("COMMENT ON TABLE demo IS '演示表';", out)
        self.assertIn("COMMENT ON TABLE demo_doc IS '文档表';", out)
        comment_lines = [line for line in out.split("\n") if line.startswith("COMMENT ON")]
        self.assertFalse([line for line in comment_lines if "假" in line])

    def test_indented_and_same_line_create_table(self):
        sql = HEADER + """
    CREATE TABLE `indented`  (
      `id` bigint NOT NULL COMMENT '编号',
      PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB COMMENT = '缩进表';
CREATE TABLE `one` (`id` bigint NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB; CREATE TABLE `two` (`id` bigint NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;
"""
        convertor = PostgreSQLConvertor(self.write_sql(sql))
        self.assertEqual(["indented", "one", "two"], table_names(convertor.table_script_list))

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertIn("COMMENT ON COLUMN indented.id IS '编号';", out)
        self.assertIn("COMMENT ON TABLE indented IS '缩进表';", out)
        self.assertIn("CREATE TABLE one (", out)
        self.assertIn("CREATE TABLE two (", out)

    def test_multiple_tables_with_inserts(self):
        demo_item = """
DROP TABLE IF EXISTS `demo_item`;
CREATE TABLE `demo_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `demo_id` bigint NOT NULL COMMENT '演示编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '演示项';

BEGIN;
INSERT INTO `demo_item` (`id`, `demo_id`) VALUES (7, 1);
INSERT INTO `demo_item` (`id`, `demo_id`) VALUES (8, 1);
COMMIT;
"""
        convertor = PostgreSQLConvertor(self.write_sql(HEADER + DEMO_TABLE + demo_item))
        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertIn("INSERT INTO demo (id, name) VALUES (1, 'a;b');", out)
        self.assertIn("INSERT INTO demo_item (id, demo_id) VALUES (7, 1);\n"
                      "INSERT INTO demo_item (id, demo_id) VALUES (8, 1);", out)
        self.assertIn("CREATE SEQUENCE demo_seq\n    START 2;", out)
        self.assertIn("CREATE SEQUENCE demo_item_seq\n    START 9;", out)

    def test_postgres_char(self):
        sql = HEADER + """
CREATE TABLE `demo_char`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `flag` char NOT NULL DEFAULT 'N' COMMENT '标记',
  `code` char(2) NULL DEFAULT NULL COMMENT '编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '字符表';
"""
        out, err = convert(PostgreSQLConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        self.assertIn("flag varchar(1) NOT NULL", out)
        self.assertIn("code varchar(2) NULL", out)

    def test_utf8_bom(self):
        # 文件开头的 BOM 紧挨着首条 CREATE TABLE；字符串中的 BOM 需原样保留
        sql = "\ufeff" + """CREATE TABLE `demo_bom`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = 'BOM 表';
INSERT INTO `demo_bom` (`id`, `name`) VALUES (1, 'a\ufeffb');
"""
        path = self.write_sql(sql)
        with open(path, "rb") as f:
            self.assertTrue(f.read().startswith(b"\xef\xbb\xbf"))

        convertor = PostgreSQLConvertor(path)
        self.assertEqual(["demo_bom"], table_names(convertor.table_script_list))
        self.assertTrue(convertor.original_table_script_list[0].startswith("CREATE TABLE `demo_bom`"))

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertIn("CREATE TABLE demo_bom (", out)
        self.assertIn("name varchar(64) NOT NULL", out)
        self.assertIn("COMMENT ON COLUMN demo_bom.name IS '名称';", out)
        self.assertIn("COMMENT ON TABLE demo_bom IS 'BOM 表';", out)
        self.assertIn("INSERT INTO demo_bom (id, name) VALUES (1, 'a\ufeffb');", out)

    def test_cli(self):
        # 等价于：python convertor.py <type> dump.sql
        sql = HEADER + DEMO_DOC_TABLE + ESCAPED_NEWLINE_INSERT + LITERAL_NEWLINE_INSERT + DEMO_TABLE
        path = self.write_sql(sql)

        # OceanBase：原样输出
        result = subprocess.run([sys.executable, "convertor.py", "oceanbase", path],
                                cwd=TOOLS_DIR, capture_output=True)
        self.assertEqual(0, result.returncode, result.stderr.decode("utf-8"))
        self.assertEqual(sql, result.stdout.decode("utf-8"))

        # PostgreSQL：不误识别 INSERT 数据中的建表文本
        result = subprocess.run([sys.executable, "convertor.py", "postgres", path],
                                cwd=TOOLS_DIR, capture_output=True)
        out, err = result.stdout.decode("utf-8"), result.stderr.decode("utf-8")
        self.assertEqual(0, result.returncode, err)
        self.assertNotIn("无法正常解析", err)
        self.assertIn("Table structure for demo_doc", out)
        self.assertIn("Table structure for demo\n", out)
        self.assertNotIn("Table structure for undo_log", out)
        self.assertNotIn("Table structure for ghost", out)


class SQLServerConvertorTest(unittest.TestCase):
    # MySQL 整数取值范围：https://dev.mysql.com/doc/refman/8.4/en/integer-types.html
    MYSQL_INTEGER_RANGES = {
        "tinyint": (-128, 127),
        "tinyint unsigned": (0, 255),
        "smallint": (-32768, 32767),
        "smallint unsigned": (0, 65535),
    }
    # SQL Server 整数取值范围：https://learn.microsoft.com/sql/t-sql/data-types/int-bigint-smallint-and-tinyint-transact-sql
    SQLSERVER_INTEGER_RANGES = {
        "tinyint": (0, 255),
        "smallint": (-32768, 32767),
        "int": (-2 ** 31, 2 ** 31 - 1),
        "bigint": (-2 ** 63, 2 ** 63 - 1),
    }

    TYPE_TABLE = """
DROP TABLE IF EXISTS `demo_type`;
CREATE TABLE `demo_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `grade` tinyint NOT NULL DEFAULT 0 COMMENT '有符号 tinyint',
  `sex` tinyint(1) NOT NULL DEFAULT 0 COMMENT '性别',
  `score` smallint NOT NULL DEFAULT 0 COMMENT '有符号 smallint',
  `ratio` tinyint unsigned NOT NULL DEFAULT 255 COMMENT '无符号 tinyint',
  `port` smallint unsigned NOT NULL DEFAULT 65535 COMMENT '无符号 smallint',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `visible` bit(1) NULL DEFAULT NULL COMMENT '是否可见',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '类型表';

BEGIN;
INSERT INTO `demo_type` (`id`, `grade`, `sex`, `score`, `ratio`, `port`, `enabled`, `visible`, `deleted`) VALUES (1, -128, 1, -32768, 255, 65535, b'0', NULL, b'0');
INSERT INTO `demo_type` (`id`, `grade`, `sex`, `score`, `ratio`, `port`, `enabled`, `visible`, `deleted`) VALUES (2, 127, 0, 32767, 0, 0, b'1', b'1', b'0');
COMMIT;
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_translate_type_integer_range(self):
        convertor = SQLServerConvertor(self.write_sql(HEADER + DEMO_TABLE))
        for mysql_type, (mysql_min, mysql_max) in self.MYSQL_INTEGER_RANGES.items():
            with self.subTest(mysql_type):
                target_type = convertor.translate_type(mysql_type, None)
                target_min, target_max = self.SQLSERVER_INTEGER_RANGES[target_type]
                # 目标类型需要容纳负数、大于 255 等全部取值
                self.assertLessEqual(target_min, mysql_min)
                self.assertGreaterEqual(target_max, mysql_max)
        # tinyint(1) 的显示宽度不影响取值范围
        self.assertEqual("smallint", convertor.translate_type("tinyint", 1))
        # 无符号 tinyint 保持 tinyint，不扩大类型
        self.assertEqual("tinyint", convertor.translate_type("TINYINT UNSIGNED", None))

    def test_translate_type_bit(self):
        convertor = SQLServerConvertor(self.write_sql(HEADER + DEMO_TABLE))
        self.assertEqual("bit", convertor.translate_type("bit", 1))
        self.assertEqual("bit", convertor.translate_type("bit", None))
        # 多位的位串不是布尔值，不能转换为 bit
        with self.assertRaises(ValueError):
            convertor.translate_type("bit", 8)

    def test_integer_and_bit(self):
        out, err = convert(SQLServerConvertor(self.write_sql(HEADER + self.TYPE_TABLE)))
        self.assertNotIn("无法正常解析", err)
        # 有符号 tinyint、smallint：smallint
        self.assertIn("grade smallint DEFAULT 0 NOT NULL", out)
        self.assertIn("sex smallint DEFAULT 0 NOT NULL", out)
        self.assertIn("score smallint DEFAULT 0 NOT NULL", out)
        # 无符号 tinyint：tinyint；无符号 smallint：int
        self.assertIn("ratio tinyint DEFAULT 255 NOT NULL", out)
        self.assertIn("port int DEFAULT 65535 NOT NULL", out)
        # bit(1)：bit，默认值为数字常量，允许 NULL
        self.assertIn("enabled bit DEFAULT 1 NOT NULL", out)
        self.assertIn("visible bit DEFAULT NULL NULL", out)
        self.assertIn("deleted bit DEFAULT 0 NOT NULL", out)
        self.assertNotIn("varchar(1)", out)
        # INSERT 保留负数、大于 255 的取值；bit 的 '0'、'1' 由 SQL Server 隐式转换
        self.assertIn("INSERT INTO demo_type (id, grade, sex, score, ratio, port, enabled, visible, deleted)"
                      " VALUES (1, -128, 1, -32768, 255, 65535, N'0', NULL, N'0')\nGO", out)
        self.assertIn("INSERT INTO demo_type (id, grade, sex, score, ratio, port, enabled, visible, deleted)"
                      " VALUES (2, 127, 0, 32767, 0, 0, N'1', N'1', N'0')\nGO", out)

    def test_bit_multiple(self):
        sql = HEADER + """
CREATE TABLE `demo_flags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `flags` bit(8) NOT NULL COMMENT '位串',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '位串表';
"""
        # 明确失败，不把多位的位串当成布尔值
        with self.assertRaises(ValueError):
            convert(SQLServerConvertor(self.write_sql(sql)))

    def test_ruoyi_vue_pro(self):
        convertor = SQLServerConvertor(os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql"))
        source_bits, source_integers = [], []
        for table_sql in convertor.original_table_script_list:
            source_bits += re.findall(r"^\s*`(\w+)`\s+bit\b[^\n]*", table_sql, flags=re.MULTILINE)
            source_integers += re.findall(r"^\s*`\w+`\s+(?:tinyint|smallint)\b[^\n]*", table_sql, flags=re.MULTILINE)
        self.assertTrue(source_bits)
        self.assertTrue(source_integers)
        # 官方脚本只有 bit(1)、有符号的 tinyint / smallint
        for table_sql in convertor.original_table_script_list:
            self.assertFalse(re.findall(r"^\s*`\w+`\s+bit\((?!1\))", table_sql, flags=re.MULTILINE))
        self.assertFalse([line for line in source_integers if "unsigned" in line.lower()])

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertNotIn("varchar(1)", out)
        self.assertEqual(len(source_bits), len(re.findall(r"^    \w+ bit\b", out, flags=re.MULTILINE)))
        self.assertEqual(len(source_integers), len(re.findall(r"^    \w+ smallint\b", out, flags=re.MULTILINE)))
        self.assertFalse(re.findall(r"^    \w+ tinyint\b", out, flags=re.MULTILINE))

    def test_national_strings(self):
        # 按词法单元为每个完整字符串加一个大写 N；内容中的逗号、''、括号、注释样式文本不变，每一行 VALUES 均处理；
        # 已有的 N、n 统一为一个大写 N（SQL Server 只支持大写 N）；x''、_utf8mb4'' 等引导符、注释、双引号、未闭合的引号保持原样
        for source, expected in (
                ("INSERT INTO t (a, id) VALUES ('a', 1);", "INSERT INTO t (a, id) VALUES (N'a', 1);"),
                ("INSERT INTO t VALUES (1, 'TRUE, ''FALSE''', 'x');", "INSERT INTO t VALUES (1, N'TRUE, ''FALSE''', N'x');"),
                ("INSERT INTO t VALUES (1, '中文'), ('二', 2);", "INSERT INTO t VALUES (1, N'中文'), (N'二', 2);"),
                ("INSERT INTO t VALUES (N'a', n'b', 'c');", "INSERT INTO t VALUES (N'a', N'b', N'c');"),
                ("INSERT INTO t VALUES (1,'a');", "INSERT INTO t VALUES (1,N'a');"),
                ("INSERT INTO t VALUES (1, 'VALUES (''a'')', '/* x */ -- y');",
                 "INSERT INTO t VALUES (1, N'VALUES (''a'')', N'/* x */ -- y');"),
                (r"INSERT INTO t VALUES (1, 'it\'s, ok');", r"INSERT INTO t VALUES (1, N'it\'s, ok');"),
                ("INSERT INTO t VALUES (1, 'a') /* 'b' */;", "INSERT INTO t VALUES (1, N'a') /* 'b' */;"),
                ("INSERT INTO t VALUES (x'0A', _utf8mb4'a', 0x10);", "INSERT INTO t VALUES (x'0A', _utf8mb4'a', 0x10);"),
                ('INSERT INTO t VALUES (1, "a");', 'INSERT INTO t VALUES (1, "a");'),
                ("INSERT INTO t VALUES (1, 'a", "INSERT INTO t VALUES (1, 'a")):
            with self.subTest(source):
                self.assertEqual(expected, SQLServerConvertor.national_strings(source))
        # 完整生成：首列、重排的字符串列，多行 VALUES 中的中文、emoji，已有的 n 前缀
        sql = HEADER + """
CREATE TABLE `demo_national`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT 'a, b' COMMENT '名称',
  `note` varchar(64) NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '国家字符表';

BEGIN;
INSERT INTO `demo_national` (`name`, `id`, `note`) VALUES ('中文', 1, 'TRUE, ''FALSE'''), (n'二', 2, '😀');
COMMIT;
"""
        out, err = convert(SQLServerConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        self.assertIn("INSERT INTO demo_national (name, id, note) VALUES (N'中文', 1, N'TRUE, ''FALSE'''),"
                      " (N'二', 2, N'😀')\nGO", out)
        # 默认值中的字符串同样加 N 前缀：不带 N 时按排序规则的代码页保存，例如 1252 下中文变为 ??
        self.assertIn("DEFAULT N'a, b'", out)


class OracleConvertorTest(unittest.TestCase):

    TIME_TABLE = """
DROP TABLE IF EXISTS `demo_time`;
CREATE TABLE `demo_time`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `remark` varchar(64) NULL DEFAULT NULL COMMENT '备注',
  `plain_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '无精度',
  `second_time` datetime(0) NULL DEFAULT NULL COMMENT '精度 0',
  `milli_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '毫秒',
  `micro_time` datetime(6) NOT NULL DEFAULT '2026-01-01 00:00:00.123456' COMMENT '微秒',
  `stamp_time` timestamp NULL DEFAULT NULL COMMENT '无精度',
  `stamp0_time` timestamp(0) NULL COMMENT '精度 0',
  `stamp6_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '微秒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '时间表';

BEGIN;
INSERT INTO `demo_time` (`id`, `remark`, `plain_time`, `second_time`, `milli_time`, `micro_time`, `stamp_time`, `stamp0_time`, `stamp6_time`) VALUES (1, '2026-01-01 00:00:00.123', '2026-01-01 08:00:00', NULL, '2026-01-01 08:00:00.001', '2026-01-01 08:00:00.123456', '2026-01-01 08:00:00', NULL, '2026-01-01 08:00:00.5');
INSERT INTO `demo_time` (`id`, `remark`, `milli_time`) VALUES (2, 'a, b''c (x)', '2026-01-01 08:00:00.002'), (3, CONCAT('x', '2026-01-01 08:00:00.003'), '2026-01-01 08:00:00.004');
INSERT INTO `demo_time` (`id`, `remark`, `milli_time`) VALUES (4, NULL, STR_TO_DATE('2026-01-01 08:00:00.005', '%Y-%m-%d %H:%i:%s.%f'));
INSERT INTO `demo_time` VALUES (5, 'r', '2026-01-01 08:00:00', NULL, '2026-01-01 08:00:00.006', '2026-01-01 08:00:00.007', NULL, NULL, NULL);
INSERT INTO `demo_time` VALUES (6, 'r', '2026-01-01 08:00:00.008');
INSERT INTO `demo_time` (`id`, `unknown`) VALUES (7, '2026-01-01 08:00:00.009');
INSERT INTO `demo_time` (`id`, `milli_time`) VALUES (8, "2026-01-01 08:00:00.010");
COMMIT;
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def assert_line(self, line, out):
        # 列定义、INSERT 均为独立的一行，列定义除最后一列外以逗号结尾
        self.assertRegex(out, rf"(?m)^{re.escape(line)},?$")

    def test_translate_type_datetime(self):
        convertor = OracleConvertor(self.write_sql(HEADER))
        # datetime 缺省、精度 0 没有小数秒，保持原有的 date；显式的小数秒精度保留为 timestamp(n)
        self.assertEqual("date", convertor.translate_type("datetime", None))
        self.assertEqual("date", convertor.translate_type("datetime", 0))
        self.assertEqual("timestamp(3)", convertor.translate_type("datetime", 3))
        self.assertEqual("timestamp(6)", convertor.translate_type("datetime", 6))
        # MySQL timestamp 缺省精度为 0，不能生成 timestamp(None)，也不能使用 Oracle 缺省的精度 6
        self.assertEqual("timestamp(0)", convertor.translate_type("timestamp", None))
        self.assertEqual("timestamp(0)", convertor.translate_type("timestamp", 0))
        self.assertEqual("timestamp(6)", convertor.translate_type("timestamp", 6))

    def test_datetime_precision(self):
        out, err = convert(OracleConvertor(self.write_sql(HEADER + self.TIME_TABLE)))
        self.assertNotIn("无法正常解析", err)
        # 1. 列类型与默认值：CURRENT_TIMESTAMP(n) 保持原样，timestamp 列的日期时间字符串默认值转为 TIMESTAMP 字面量
        self.assert_line("    remark varchar2(64 char) DEFAULT NULL NULL", out)
        self.assert_line("    plain_time date DEFAULT CURRENT_TIMESTAMP NOT NULL", out)
        self.assert_line("    second_time date DEFAULT NULL NULL", out)
        self.assert_line("    milli_time timestamp(3) DEFAULT CURRENT_TIMESTAMP(3) NULL", out)
        self.assert_line("    micro_time timestamp(6) DEFAULT TIMESTAMP '2026-01-01 00:00:00.123456' NOT NULL", out)
        self.assert_line("    stamp_time timestamp(0) DEFAULT NULL NULL", out)
        self.assert_line("    stamp0_time timestamp(0)  NULL", out)
        self.assert_line("    stamp6_time timestamp(6) DEFAULT CURRENT_TIMESTAMP(6) NULL", out)
        self.assertNotIn("None", out)

        # 2. timestamp 列的值（含整秒）转为 TIMESTAMP 字面量，date 列为 to_date；文本列中相同格式的值、NULL 保持原样
        to_date = "to_date('2026-01-01 08:00:00', 'SYYYY-MM-DD HH24:MI:SS')"
        self.assert_line("INSERT INTO demo_time (id, remark, plain_time, second_time, milli_time, micro_time, stamp_time,"
                         " stamp0_time, stamp6_time) VALUES (1, '2026-01-01 00:00:00.123', " + to_date + ", NULL,"
                         " TIMESTAMP '2026-01-01 08:00:00.001', TIMESTAMP '2026-01-01 08:00:00.123456',"
                         " TIMESTAMP '2026-01-01 08:00:00', NULL, TIMESTAMP '2026-01-01 08:00:00.5');", out)
        # 多行 VALUES；文本中的逗号、双写引号、括号不影响按列对应，函数参数中的值不转换
        self.assert_line("INSERT INTO demo_time (id, remark, milli_time) VALUES (2, 'a, b''c (x)',"
                         " TIMESTAMP '2026-01-01 08:00:00.002'), (3, CONCAT('x', '2026-01-01 08:00:00.003'),"
                         " TIMESTAMP '2026-01-01 08:00:00.004');", out)
        self.assert_line("INSERT INTO demo_time (id, remark, milli_time) VALUES (4, NULL,"
                         " STR_TO_DATE('2026-01-01 08:00:00.005', '%Y-%m-%d %H:%i:%s.%f'));", out)
        # 没有列清单时按建表的列顺序对应
        self.assert_line("INSERT INTO demo_time VALUES (5, 'r', " + to_date + ", NULL,"
                         " TIMESTAMP '2026-01-01 08:00:00.006', TIMESTAMP '2026-01-01 08:00:00.007', NULL, NULL, NULL);", out)

        # 3. 列数不一致、列名未知、双引号值：无法安全识别，整条语句保持原样
        self.assert_line("INSERT INTO demo_time VALUES (6, 'r', '2026-01-01 08:00:00.008');", out)
        self.assert_line("INSERT INTO demo_time (id, unknown) VALUES (7, '2026-01-01 08:00:00.009');", out)
        self.assert_line("INSERT INTO demo_time (id, milli_time) VALUES (8, \"2026-01-01 08:00:00.010\");", out)
        self.assertEqual(7, out.count("TIMESTAMP '2026-01-01 08:00:00."))

    def test_reserved_column_with_timestamp(self):
        sql = HEADER + """
CREATE TABLE `demo_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `level` int NOT NULL COMMENT '级别',
  `sync_time` datetime(3) NULL DEFAULT NULL COMMENT '同步时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '级别表';

BEGIN;
INSERT INTO `demo_level` (`id`, `level`, `sync_time`) VALUES (1, 2, '2026-01-01 08:00:00.001');
COMMIT;
"""
        out, err = convert(OracleConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        # 保留字列名仍按原有规则转义，转义前按原列名对应列类型
        self.assert_line('    "level" number  NOT NULL', out)
        self.assert_line("INSERT INTO demo_level (id, \"level\", sync_time) VALUES (1, 2,"
                         " TIMESTAMP '2026-01-01 08:00:00.001');", out)

    def test_comment_in_values(self):
        table = """
CREATE TABLE `demo_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `remark` varchar(64) NULL DEFAULT NULL COMMENT '备注',
  `ts1` datetime(3) NULL DEFAULT NULL COMMENT '时间 1',
  `remark2` varchar(64) NULL DEFAULT NULL COMMENT '备注 2',
  `ts2` datetime(3) NULL DEFAULT NULL COMMENT '时间 2',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '注释表';
"""
        inserts = """
BEGIN;
INSERT INTO `demo_comment` VALUES (1 /*,*/, '2026-01-01 08:00:00.123', NULL /*(*/, 'x' /*)*/, '2026-01-01 08:00:00.456');
INSERT INTO `demo_comment` (`id`, `remark`, `ts1`, `remark2`, `ts2`) VALUES (2, '/*,*/ -- x # y', '2026-01-01 08:00:00.123', 'a /* ( */', '2026-01-01 08:00:00.456');
INSERT INTO `demo_comment` (`id`, `remark`, `ts1`, `remark2`, `ts2`) VALUES (3, 'r', /* 毫秒 */ '2026-01-01 08:00:00.123', NULL, '2026-01-01 08:00:00.456');
COMMIT;
"""
        # id 为表达式 5--1（MySQL 中即 5 - -1）：词法单元按列对应，但 id 不是整数字面量，无法确定 Oracle 序列的起始值
        expression = ("INSERT INTO `demo_comment` (`id`, `remark`, `ts1`, `remark2`, `ts2`) VALUES (5--1, 'r',"
                      " '2026-01-01 08:00:00.123', NULL, '2026-01-01 08:00:00.456');\n")
        out, err = convert(OracleConvertor(self.write_sql(HEADER + table + inserts)))
        self.assertNotIn("无法正常解析", err)
        # 1. 字符串外的注释：其中的逗号、括号会干扰按列对应，整条语句保持原样，文本列不能被改为 TIMESTAMP；
        # 这些语句的 id 不计入序列的起始值，在 stderr 提示
        self.assert_line("INSERT INTO demo_comment VALUES (1 /*,*/, '2026-01-01 08:00:00.123', NULL /*(*/, 'x' /*)*/,"
                         " '2026-01-01 08:00:00.456');", out)
        self.assert_line("INSERT INTO demo_comment (id, remark, ts1, remark2, ts2) VALUES (3, 'r', /* 毫秒 */"
                         " '2026-01-01 08:00:00.123', NULL, '2026-01-01 08:00:00.456');", out)
        self.assertIn("序列起始值未计入", err)
        # 2. 字符串内注释样式的文本是正常数据，timestamp 列仍然转换
        self.assert_line("INSERT INTO demo_comment (id, remark, ts1, remark2, ts2) VALUES (2, '/*,*/ -- x # y',"
                         " TIMESTAMP '2026-01-01 08:00:00.123', 'a /* ( */', TIMESTAMP '2026-01-01 08:00:00.456');", out)
        self.assertEqual(2, out.count("TIMESTAMP '2026-01-01 08:00:00."))
        # 3. MySQL 中 -- 后面没有空白时不是注释，仍然按列对应并转换（直接调用 convert_insert_values）
        convertor = OracleConvertor(self.write_sql(HEADER + table))
        convertor.gen_create(DDLParser(convertor.table_script_list[0].replace("`", "")).run()[0])
        self.assertEqual("INSERT INTO demo_comment (id, remark, ts1, remark2, ts2) VALUES (5--1, 'r',"
                         " TIMESTAMP '2026-01-01 08:00:00.123', NULL, TIMESTAMP '2026-01-01 08:00:00.456');",
                         convertor.convert_insert_values(
                             "demo_comment", "INSERT INTO demo_comment (id, remark, ts1, remark2, ts2) VALUES (5--1, 'r',"
                                             " '2026-01-01 08:00:00.123', NULL, '2026-01-01 08:00:00.456');"))
        # 4. 完整生成：id 为表达式时明确失败，不输出部分转换的脚本，见 InsertSequenceStartTest
        out = io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(ValueError) as context:
                OracleConvertor(self.write_sql(HEADER + table + inserts + expression)).print()
        self.assertIn("id 不是整数字面量: 5--1", str(context.exception))
        self.assertEqual("", out.getvalue())

    def test_other_convertors_unchanged(self):
        path = self.write_sql(HEADER + self.TIME_TABLE)
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                # TIMESTAMP 字面量只用于 Oracle
                self.assertNotIn("TIMESTAMP '", out)

    def test_ruoyi_vue_pro(self):
        convertor = OracleConvertor(os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql"))
        source_datetimes = []
        for table_sql in convertor.original_table_script_list:
            source_datetimes += re.findall(r"^\s*`\w+`\s+(datetime|timestamp)\b(\(\d+\))?", table_sql, flags=re.MULTILINE)
        self.assertTrue(source_datetimes)
        # 官方脚本只有不带小数秒精度的 datetime，Oracle 输出保持原有的 date，不出现 timestamp
        self.assertEqual([("datetime", "")], sorted(set(source_datetimes)))

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        self.assertEqual(len(source_datetimes), len(re.findall(r"^    \S+ date\b", out, flags=re.MULTILINE)))
        # DEFAULT CURRENT_TIMESTAMP 保持原样，但不出现 timestamp 列与 TIMESTAMP 字面量
        self.assertFalse(re.findall(r"^    \S+ timestamp\b", out, flags=re.MULTILINE))
        self.assertNotIn("TIMESTAMP '", out)


class TemporalConvertorTest(unittest.TestCase):
    # 有效的 MySQL 建表：当前时间默认值的精度与列的小数秒精度一致
    CLOCK_TABLE = """
DROP TABLE IF EXISTS `demo_clock`;
CREATE TABLE `demo_clock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '无精度',
  `dt0` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '精度 0，空括号',
  `dt1` datetime(1) NULL DEFAULT NULL COMMENT '精度 1',
  `dt3` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '毫秒',
  `dt3_now` datetime(3) NULL DEFAULT NOW(3) COMMENT 'NOW',
  `dt3_localtime` datetime(3) NULL DEFAULT LOCALTIME(3) COMMENT 'LOCALTIME',
  `dt6_localtimestamp` datetime(6) NULL DEFAULT LOCALTIMESTAMP(6) COMMENT 'LOCALTIMESTAMP',
  `dt_now` datetime NULL DEFAULT now() COMMENT '小写 now',
  `ts` timestamp NULL DEFAULT NULL COMMENT '无精度',
  `ts0` timestamp(0) NULL DEFAULT LOCALTIMESTAMP COMMENT '精度 0',
  `ts6` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '微秒',
  `note` varchar(64) NULL DEFAULT 'CURRENT_TIMESTAMP(3)' COMMENT '函数名文本',
  `fmt` varchar(64) NULL DEFAULT '2026-01-01 00:00:00.123' COMMENT '日期格式文本',
  `num` int NOT NULL DEFAULT 0 COMMENT '数字',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '时钟表';
"""

    # 各目标数据库期望的 (列名, 类型, 默认值)；NOT NULL 只有 dt
    PG_COLUMNS = [
        ("dt", "timestamp", "CURRENT_TIMESTAMP"),
        ("dt0", "timestamp(0)", "CURRENT_TIMESTAMP"),
        ("dt1", "timestamp(1)", "NULL"),
        ("dt3", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt3_now", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt3_localtime", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt6_localtimestamp", "timestamp(6)", "CURRENT_TIMESTAMP(6)"),
        ("dt_now", "timestamp", "CURRENT_TIMESTAMP"),
        ("ts", "timestamp", "NULL"),
        ("ts0", "timestamp(0)", "CURRENT_TIMESTAMP"),
        ("ts6", "timestamp(6)", "CURRENT_TIMESTAMP(6)"),
    ]
    ORACLE_COLUMNS = [
        ("dt", "date", "CURRENT_TIMESTAMP"),
        ("dt0", "date", "CURRENT_TIMESTAMP"),
        ("dt1", "timestamp(1)", "NULL"),
        ("dt3", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt3_now", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt3_localtime", "timestamp(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt6_localtimestamp", "timestamp(6)", "CURRENT_TIMESTAMP(6)"),
        ("dt_now", "date", "CURRENT_TIMESTAMP"),
        ("ts", "timestamp(0)", "NULL"),
        ("ts0", "timestamp(0)", "CURRENT_TIMESTAMP"),
        ("ts6", "timestamp(6)", "CURRENT_TIMESTAMP(6)"),
    ]
    SQLSERVER_COLUMNS = [
        ("dt", "datetime2", "CURRENT_TIMESTAMP"),
        ("dt0", "datetime2(0)", "CURRENT_TIMESTAMP"),
        ("dt1", "datetime2(1)", "NULL"),
        ("dt3", "datetime2(3)", "SYSDATETIME()"),
        ("dt3_now", "datetime2(3)", "SYSDATETIME()"),
        ("dt3_localtime", "datetime2(3)", "SYSDATETIME()"),
        ("dt6_localtimestamp", "datetime2(6)", "SYSDATETIME()"),
        ("dt_now", "datetime2", "CURRENT_TIMESTAMP"),
        ("ts", "datetime2", "NULL"),
        ("ts0", "datetime2(0)", "CURRENT_TIMESTAMP"),
        ("ts6", "datetime2(6)", "SYSDATETIME()"),
    ]
    # 达梦原生支持这些当前时间默认值的写法，只修正列精度
    DM_COLUMNS = [
        ("dt", "datetime", "CURRENT_TIMESTAMP"),
        ("dt0", "datetime(0)", "CURRENT_TIMESTAMP()"),
        ("dt1", "datetime(1)", "NULL"),
        ("dt3", "datetime(3)", "CURRENT_TIMESTAMP(3)"),
        ("dt3_now", "datetime(3)", "NOW(3)"),
        ("dt3_localtime", "datetime(3)", "LOCALTIME(3)"),
        ("dt6_localtimestamp", "datetime(6)", "LOCALTIMESTAMP(6)"),
        ("dt_now", "datetime", "now()"),
        ("ts", "timestamp(0)", "NULL"),
        ("ts0", "timestamp(0)", "LOCALTIMESTAMP"),
        ("ts6", "timestamp(6)", "CURRENT_TIMESTAMP(6)"),
    ]

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def assert_line(self, line, out):
        # 列定义为独立的一行，除最后一列外以逗号结尾
        self.assertRegex(out, rf"(?m)^\s+{re.escape(line)},?$")

    def column_line(self, name, out):
        return re.search(rf"(?m)^\s+{name} .*$", out).group()

    def test_translate_type_precision(self):
        path = self.write_sql(HEADER)
        # 显式精度（含 0）保留；缺省时 PostgreSQL 系、SQL Server、达梦的 datetime 保持原有输出
        expected = {
            PostgreSQLConvertor: ("timestamp", "timestamp({0})", "timestamp", "timestamp({0})"),
            KingbaseConvertor: ("timestamp", "timestamp({0})", "timestamp", "timestamp({0})"),
            OpengaussConvertor: ("timestamp", "timestamp({0})", "timestamp", "timestamp({0})"),
            HighGoConvertor: ("timestamp", "timestamp({0})", "timestamp", "timestamp({0})"),
            SQLServerConvertor: ("datetime2", "datetime2({0})", "datetime2", "datetime2({0})"),
            # 达梦缺省的 timestamp 按 MySQL 缺省精度 0 输出，不能生成 timestamp(None)
            DM8Convertor: ("datetime", "datetime({0})", "timestamp(0)", "timestamp({0})"),
        }
        for convertor_class, (datetime_none, datetime_n, timestamp_none, timestamp_n) in expected.items():
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                self.assertEqual(datetime_none, convertor.translate_type("datetime", None))
                self.assertEqual(timestamp_none, convertor.translate_type("timestamp", None))
                for precision in range(7):
                    self.assertEqual(datetime_n.format(precision), convertor.translate_type("datetime", precision))
                    self.assertEqual(timestamp_n.format(precision), convertor.translate_type("timestamp", precision))

    def test_current_time_default(self):
        cases = [("B", "datetime", "CURRENT_TIMESTAMP()"), ("B", "datetime", "LOCALTIME"),
                 ("B", "timestamp", "LOCALTIMESTAMP"), ("B", "datetime", "now()"), ("B", "datetime", "NOW( )"),
                 ("P(0)", "datetime", "CURRENT_TIMESTAMP(0)"), ("P(3)", "datetime", "NOW( 3 )"),
                 ("P(3)", "datetime", "localtime(3)"), ("P(6)", "timestamp", "LOCALTIMESTAMP(6)"),
                 ("P(6)", "timestamp null", "CURRENT_TIMESTAMP(6)")]
        for expected, type, default in cases:
            self.assertEqual(expected, convert_current_time_default(type, default, "B", "P({0})"), default)
        # 不带括号的 CURRENT_TIMESTAMP 各目标数据库均可用，保持原样
        for default in ("CURRENT_TIMESTAMP", "current_timestamp"):
            self.assertEqual(default, convert_current_time_default("datetime", default, "B", "P({0})"))
        # 非日期时间列、字符串、NULL、数字、未知或无效的表达式：保持原样，不猜测
        unchanged = [("varchar", "CURRENT_TIMESTAMP(3)"), ("date", "CURRENT_TIMESTAMP"), ("time", "NOW()"),
                     ("datetime", "'CURRENT_TIMESTAMP(3)'"), ("datetime", "'2026-01-01 00:00:00.123'"),
                     ("datetime", "NULL"), ("datetime", None), ("datetime", 0), ("datetime", "NOW"),
                     ("datetime", "CURRENT_TIMESTAMP(7)"), ("datetime", "NOW(3) + 1"), ("datetime", "CURRENT_DATE"),
                     ("datetime", "UTC_TIMESTAMP(3)"), ("datetime", "SYSDATE()")]
        for type, default in unchanged:
            self.assertEqual(default, convert_current_time_default(type, default, "B", "P({0})"), (type, default))

    def test_datetime_precision_and_default(self):
        path = self.write_sql(HEADER + self.CLOCK_TABLE)
        targets = [(PostgreSQLConvertor, self.PG_COLUMNS), (KingbaseConvertor, self.PG_COLUMNS),
                   (OpengaussConvertor, self.PG_COLUMNS), (HighGoConvertor, self.PG_COLUMNS),
                   (OracleConvertor, self.ORACLE_COLUMNS), (SQLServerConvertor, self.SQLSERVER_COLUMNS),
                   (DM8Convertor, self.DM_COLUMNS)]
        for convertor_class, columns in targets:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                # PostgreSQL 系的 NULL 在 DEFAULT 之前，其它数据库的 DEFAULT 在 NULL 之前
                pg_style = convertor_class not in (OracleConvertor, SQLServerConvertor, DM8Convertor)
                for name, full_type, default in columns:
                    nullable = "NOT NULL" if name == "dt" else "NULL"
                    line = (f"{name} {full_type} {nullable} DEFAULT {default}" if pg_style
                            else f"{name} {full_type} DEFAULT {default} {nullable}")
                    self.assert_line(line, out)
                # 文本列中函数名、日期格式的默认值，数字默认值保持原样；SQL Server 的字符串默认值带 N 前缀
                n = "N" if convertor_class is SQLServerConvertor else ""
                note = self.column_line("note", out)
                self.assertIn(f"DEFAULT {n}'CURRENT_TIMESTAMP", note)
                self.assertNotIn("SYSDATETIME", note)
                self.assertIn(f"DEFAULT {n}'2026-01-01 00:00:00.123'", self.column_line("fmt", out))
                self.assertIn("DEFAULT 0", self.column_line("num", out))

    def test_ruoyi_vue_pro(self):
        # 官方脚本只有不带精度的 datetime 与不带精度的 CURRENT_TIMESTAMP，各目标数据库不出现精度与 SYSDATETIME()
        path = os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql")
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertFalse(re.findall(r"^\s+\S+ (?:timestamp|datetime2?)\(", out, flags=re.MULTILINE))
                self.assertNotIn("SYSDATETIME", out)
                self.assertIn("DEFAULT CURRENT_TIMESTAMP", out)


class IntegerConvertorTest(unittest.TestCase):
    # MySQL 整数取值范围：https://dev.mysql.com/doc/refman/8.4/en/integer-types.html
    MYSQL_RANGES = {
        ("tinyint", False): (-2 ** 7, 2 ** 7 - 1),
        ("tinyint", True): (0, 2 ** 8 - 1),
        ("smallint", False): (-2 ** 15, 2 ** 15 - 1),
        ("smallint", True): (0, 2 ** 16 - 1),
        ("mediumint", False): (-2 ** 23, 2 ** 23 - 1),
        ("mediumint", True): (0, 2 ** 24 - 1),
        ("int", False): (-2 ** 31, 2 ** 31 - 1),
        ("int", True): (0, 2 ** 32 - 1),
        ("bigint", False): (-2 ** 63, 2 ** 63 - 1),
        ("bigint", True): (0, 2 ** 64 - 1),
    }
    # 目标数据库的整数取值范围，numeric(20,0)、decimal(20,0) 为 20 位精确数值，Oracle 的 smallint、number 为 38 位精确数值
    DECIMAL_20 = (-(10 ** 20 - 1), 10 ** 20 - 1)
    NUMBER_38 = (-(10 ** 38 - 1), 10 ** 38 - 1)
    TARGET_RANGES = {
        PostgreSQLConvertor: {"int2": (-2 ** 15, 2 ** 15 - 1), "int4": (-2 ** 31, 2 ** 31 - 1),
                              "int8": (-2 ** 63, 2 ** 63 - 1), "numeric(20,0)": DECIMAL_20},
        SQLServerConvertor: {"tinyint": (0, 255), "smallint": (-2 ** 15, 2 ** 15 - 1), "int": (-2 ** 31, 2 ** 31 - 1),
                             "bigint": (-2 ** 63, 2 ** 63 - 1), "numeric(20,0)": DECIMAL_20},
        DM8Convertor: {"smallint": (-2 ** 15, 2 ** 15 - 1), "int": (-2 ** 31, 2 ** 31 - 1),
                       "bigint": (-2 ** 63, 2 ** 63 - 1), "decimal(20,0)": DECIMAL_20},
        OracleConvertor: {"smallint": NUMBER_38, "number": NUMBER_38},
    }
    # 人大金仓、openGauss、瀚高继承 PostgreSQL 的类型转换
    CONVERTOR_RANGES = {
        PostgreSQLConvertor: TARGET_RANGES[PostgreSQLConvertor],
        KingbaseConvertor: TARGET_RANGES[PostgreSQLConvertor],
        OpengaussConvertor: TARGET_RANGES[PostgreSQLConvertor],
        HighGoConvertor: TARGET_RANGES[PostgreSQLConvertor],
        SQLServerConvertor: TARGET_RANGES[SQLServerConvertor],
        DM8Convertor: TARGET_RANGES[DM8Convertor],
        OracleConvertor: TARGET_RANGES[OracleConvertor],
    }
    # simple-ddl-parser 输出的类型写法 -> MySQL 整数类型：显示宽度在 size 中，zerofill 隐含 unsigned，integer 为 int 的别名
    SOURCE_TYPES = [
        ("tinyint", None, ("tinyint", False)),
        ("tinyint signed", 1, ("tinyint", False)),
        ("tinyint unsigned", 4, ("tinyint", True)),
        ("tinyint zerofill", 4, ("tinyint", True)),
        ("smallint", None, ("smallint", False)),
        ("SMALLINT UNSIGNED", None, ("smallint", True)),
        ("smallint zerofill", 4, ("smallint", True)),
        ("mediumint", None, ("mediumint", False)),
        ("MEDIUMINT UNSIGNED", 8, ("mediumint", True)),
        ("mediumint zerofill", 4, ("mediumint", True)),
        ("int", 11, ("int", False)),
        ("INT SIGNED", None, ("int", False)),
        ("int unsigned", 4, ("int", True)),
        ("int zerofill", 4, ("int", True)),
        ("int unsigned zerofill", None, ("int", True)),
        ("INTEGER", None, ("int", False)),
        ("integer unsigned", None, ("int", True)),
        ("bigint", 20, ("bigint", False)),
        ("bigint  UNSIGNED", None, ("bigint", True)),
        ("bigint unsigned zerofill", 4, ("bigint", True)),
    ]

    NUMBER_TABLE = """
DROP TABLE IF EXISTS `demo_number`;
CREATE TABLE `demo_number`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '编号',
  `t` tinyint NOT NULL DEFAULT 0 COMMENT 't',
  `flag` tinyint(1) NOT NULL DEFAULT 1 COMMENT '显示宽度 1，仍为整数',
  `tu` tinyint UNSIGNED NULL DEFAULT NULL COMMENT 'tu',
  `s` smallint NULL DEFAULT NULL COMMENT 's',
  `su` smallint(5) UNSIGNED NULL DEFAULT 65535 COMMENT 'su',
  `m` mediumint NULL DEFAULT NULL COMMENT 'm',
  `mu` mediumint UNSIGNED NULL DEFAULT 16777215 COMMENT 'mu',
  `i` int NULL DEFAULT NULL COMMENT 'i',
  `iu` int(10) UNSIGNED NULL DEFAULT 4294967295 COMMENT 'iu',
  `iz` int(4) ZEROFILL NULL DEFAULT NULL COMMENT '隐含 unsigned',
  `isg` integer NULL DEFAULT NULL COMMENT 'integer',
  `b` bigint NULL DEFAULT NULL COMMENT 'b',
  `bu` bigint UNSIGNED NULL DEFAULT 18446744073709551615 COMMENT 'bu',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '数字表';

BEGIN;
INSERT INTO `demo_number` (`id`, `t`, `flag`, `tu`, `s`, `su`, `m`, `mu`, `i`, `iu`, `iz`, `isg`, `b`, `bu`) VALUES (1, -128, 1, 255, -32768, 65535, -8388608, 16777215, -2147483648, 4294967295, 4294967295, 2147483647, -9223372036854775808, 18446744073709551615);
INSERT INTO `demo_number` (`id`, `t`, `flag`, `tu`, `s`, `su`, `m`, `mu`, `i`, `iu`, `iz`, `isg`, `b`, `bu`) VALUES (2, 127, 0, 0, 32767, 0, 8388607, 0, 2147483647, 0, 0, -2147483648, 9223372036854775807, 0);
COMMIT;
"""
    # 普通业务列对应的 MySQL 整数类型
    NUMBER_COLUMNS = {
        "t": ("tinyint", False), "flag": ("tinyint", False), "tu": ("tinyint", True),
        "s": ("smallint", False), "su": ("smallint", True), "m": ("mediumint", False), "mu": ("mediumint", True),
        "i": ("int", False), "iu": ("int", True), "iz": ("int", True), "isg": ("int", False),
        "b": ("bigint", False), "bu": ("bigint", True),
    }

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def assert_contains_range(self, target_range, mysql_range, message):
        self.assertLessEqual(target_range[0], mysql_range[0], message)
        self.assertGreaterEqual(target_range[1], mysql_range[1], message)

    def test_integer_type(self):
        for source_type, _, expected in self.SOURCE_TYPES:
            with self.subTest(source_type):
                self.assertEqual(expected, integer_type(source_type))
        # 其它类型、矛盾或未知的修饰：不按整数类型处理
        for source_type in ("", "decimal", "float unsigned", "double", "bit", "integerx", "point",
                            "int unsigned signed", "tinyint signed zerofill", "bigint auto_increment", "int(11)"):
            with self.subTest(source_type):
                self.assertIsNone(integer_type(source_type))

    def test_translate_type_range(self):
        path = self.write_sql(HEADER)
        for convertor_class, target_ranges in self.CONVERTOR_RANGES.items():
            convertor = convertor_class(path)
            for source_type, size, mysql_type in self.SOURCE_TYPES:
                with self.subTest(f"{convertor_class.__name__} {source_type}({size})"):
                    target_type = convertor.translate_type(source_type, size)
                    # 目标类型需要容纳源类型的全部取值，显示宽度不影响取值范围
                    self.assertIn(target_type, target_ranges)
                    self.assert_contains_range(target_ranges[target_type], self.MYSQL_RANGES[mysql_type], target_type)
                    self.assertEqual(target_type, convertor.translate_type(source_type, None))

    def test_integer_columns(self):
        path = self.write_sql(HEADER + self.NUMBER_TABLE)
        for convertor_class, target_ranges in self.CONVERTOR_RANGES.items():
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                # 1. 普通业务列：按生成的列类型，容纳 MySQL 源类型的全部取值；默认值保持原样
                for name, mysql_type in self.NUMBER_COLUMNS.items():
                    target_type = re.search(rf"(?m)^\s+{name} (\S+) ", out).group(1)
                    self.assert_contains_range(target_ranges[target_type], self.MYSQL_RANGES[mysql_type], name)
                self.assertRegex(out, r"(?m)^\s+bu \S+ .*DEFAULT 18446744073709551615")
                # 2. INSERT 中的最小值、最大值原样保留，不丢失精度
                inserts = [line for line in out.split("\n") if line.startswith("INSERT INTO demo_number")]
                self.assertEqual(2, len(inserts))
                for value in ("-128", "-9223372036854775808", "18446744073709551615", "4294967295", "16777215"):
                    self.assertIn(value, inserts[0])
                self.assertIn("9223372036854775807", inserts[1])

    def test_unsigned_id(self):
        # 主键 id 与普通列使用同一类型转换；SQL Server、达梦保持原有的 bigint IDENTITY，不在本次调整主键生成策略
        path = self.write_sql(HEADER + self.NUMBER_TABLE)
        expected = {
            PostgreSQLConvertor: "    id numeric(20,0) NOT NULL,",
            KingbaseConvertor: "    id numeric(20,0) NOT NULL,",
            OpengaussConvertor: "    id numeric(20,0) NOT NULL,",
            HighGoConvertor: "    id numeric(20,0) NOT NULL,",
            OracleConvertor: "    id number  NOT NULL,",
            SQLServerConvertor: "    id bigint NOT NULL PRIMARY KEY IDENTITY,",
            DM8Convertor: "    id bigint NOT NULL PRIMARY KEY IDENTITY,",
        }
        for convertor_class, line in expected.items():
            with self.subTest(convertor_class.__name__):
                out, _ = convert(convertor_class(path))
                self.assertIn(line + "\n", out)

    def test_ruoyi_vue_pro(self):
        # 官方脚本的无符号整数只有 system_social_user、system_social_user_bind 的 bigint UNSIGNED 主键
        path = os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql")
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertEqual(2, out.count("numeric(20,0)"))
                self.assertEqual(2, len(re.findall(r"(?m)^\s+id numeric\(20,0\) NOT NULL,$", out)))
        for convertor_class in (OracleConvertor, SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("(20,0)", out)
                # 类型映射缺失时输出裸 None；按单词匹配，菜单组件名 EamInspectionNonePoint 等标识符中的子串不算
                self.assertNotRegex(out, r"\bNone\b")


class StringLobConvertorTest(unittest.TestCase):
    # 各目标数据库的字符、文本、二进制类型；{0} 为源长度，{1} 为源长度的 2 倍
    TARGET_TYPES = {
        PostgreSQLConvertor: {"char": "varchar({0})", "varchar": "varchar({0})", "text": "text", "blob": "bytea"},
        OracleConvertor: {"char": "varchar2({0} char)", "varchar": "varchar2({0} char)", "text": "clob", "blob": "blob"},
        # SQL Server 的 nvarchar(n) 按 UTF-16 码元计数，char、varchar 需要 2 倍长度；长 varchar 见 StringCapacityConvertorTest
        SQLServerConvertor: {"char": "nvarchar({1})", "varchar": "nvarchar({1})", "text": "nvarchar(max)",
                             "blob": "varbinary(max)"},
        DM8Convertor: {"char": "varchar({0} char)", "varchar": "varchar({0} char)", "text": "text", "blob": "blob"},
    }
    # 人大金仓、openGauss、瀚高继承 PostgreSQL 的类型转换
    CONVERTOR_TYPES = {
        PostgreSQLConvertor: TARGET_TYPES[PostgreSQLConvertor],
        KingbaseConvertor: TARGET_TYPES[PostgreSQLConvertor],
        OpengaussConvertor: TARGET_TYPES[PostgreSQLConvertor],
        HighGoConvertor: TARGET_TYPES[PostgreSQLConvertor],
        OracleConvertor: TARGET_TYPES[OracleConvertor],
        SQLServerConvertor: TARGET_TYPES[SQLServerConvertor],
        DM8Convertor: TARGET_TYPES[DM8Convertor],
    }
    TEXT_TYPES = ("tinytext", "text", "mediumtext", "longtext")
    BLOB_TYPES = ("tinyblob", "blob", "mediumblob", "longblob")

    TEXT_TABLE = """
DROP TABLE IF EXISTS `demo_text`;
CREATE TABLE `demo_text`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `c` char NOT NULL DEFAULT 'N' COMMENT '缺省长度',
  `c1` char(1) NULL DEFAULT NULL COMMENT '长度 1',
  `c32` char(32) NOT NULL DEFAULT '' COMMENT '长度 32，空串默认值',
  `v` varchar(32) NULL DEFAULT NULL COMMENT '对照',
  `tt` tinytext NOT NULL COMMENT 'tinytext',
  `t` text NULL COMMENT 'text',
  `mt` mediumtext NOT NULL COMMENT 'mediumtext',
  `lt` longtext NULL COMMENT 'longtext',
  `tb` tinyblob NULL COMMENT 'tinyblob',
  `b` blob NULL COMMENT 'blob',
  `mb` mediumblob NOT NULL COMMENT 'mediumblob',
  `lb` longblob NULL COMMENT 'longblob',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '文本表';

BEGIN;
INSERT INTO `demo_text` (`id`, `c`, `c1`, `c32`, `v`, `tt`, `t`, `mt`, `lt`, `tb`, `b`, `mb`, `lb`) VALUES (1, 'N', '', '中文😀', 'it''s', '', '2026-01-01 08:00:00', 'a, b (c)', NULL, NULL, NULL, 'x', NULL);
COMMIT;
"""
    # 列名 -> (类型族, 长度)
    TEXT_COLUMNS = {
        "c": ("char", 1), "c1": ("char", 1), "c32": ("char", 32), "v": ("varchar", 32),
        "tt": ("text", None), "t": ("text", None), "mt": ("text", None), "lt": ("text", None),
        "tb": ("blob", None), "b": ("blob", None), "mb": ("blob", None), "lb": ("blob", None),
    }
    # 生成后仍为 NOT NULL 的列
    SOURCE_NOT_NULL = {"c", "c32", "tt", "mt", "mb"}
    NOT_NULL_COLUMNS = {
        PostgreSQLConvertor: SOURCE_NOT_NULL,
        HighGoConvertor: SOURCE_NOT_NULL,
        # 人大金仓、openGauss：text 列，以及 NOT NULL DEFAULT '' 的列允许 NULL
        KingbaseConvertor: {"c", "mb"},
        OpengaussConvertor: {"c", "mb"},
        # Oracle 中空串即 NULL：字符、文本列允许 NULL，二进制列保持 NOT NULL
        OracleConvertor: {"mb"},
        SQLServerConvertor: SOURCE_NOT_NULL,
        # 达梦：沿用原有策略，DEFAULT '' NOT NULL 的列允许 NULL
        DM8Convertor: {"c", "tt", "mt", "mb"},
    }

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    def test_translate_type(self):
        path = self.write_sql(HEADER)
        for convertor_class, target_types in self.CONVERTOR_TYPES.items():
            convertor = convertor_class(path)
            with self.subTest(convertor_class.__name__):
                # MySQL 中未指定长度的 char 等同于 char(1)，最长 255 个字符；解析器对 CHAR(32) 输出大写类型名
                for type, size, length in (("char", None, 1), ("char", 1, 1), ("char", 32, 32), ("CHAR", 32, 32),
                                           ("char", 255, 255)):
                    self.assertEqual(target_types["char"].format(length, length * 2), convertor.translate_type(type, size))
                for type in self.TEXT_TYPES:
                    self.assertEqual(target_types["text"], convertor.translate_type(type, None), type)
                for type in self.BLOB_TYPES:
                    self.assertEqual(target_types["blob"], convertor.translate_type(type, None), type)
                # varchar(32)：SQL Server 为 2 倍长度，其它保持原有映射
                self.assertEqual(target_types["varchar"].format(32, 64), convertor.translate_type("varchar", 32))

    def test_sqlserver_char_capacity(self):
        # MySQL char(n) 最多 n 个字符，SQL Server nvarchar(N) 最多 N 个 UTF-16 码元；全部为增补字符（例如 🚀）时码元数最多
        # 只保证保留全部取值，不复制长度约束：ASCII 等字符在目标中可以写入更多个
        convertor = SQLServerConvertor(self.write_sql(HEADER))
        for size, length in ((None, 1), (1, 1), (32, 32), (255, 255)):
            with self.subTest(size=size):
                value = "🚀" * length
                self.assertEqual(length, len(value))
                code_units = len(value.encode("utf-16-le")) // 2
                capacity = int(re.fullmatch(r"nvarchar\((\d+)\)", convertor.translate_type("char", size)).group(1))
                self.assertGreaterEqual(capacity, code_units)
                # 不超过 nvarchar 的定长上限 4000
                self.assertLessEqual(capacity, 4000)
        # varchar 同样需要 2 倍长度，长 varchar 见 StringCapacityConvertorTest
        self.assertEqual("nvarchar(64)", convertor.translate_type("varchar", 32))

    def test_string_lob_columns(self):
        path = self.write_sql(HEADER + self.TEXT_TABLE)
        for convertor_class, target_types in self.CONVERTOR_TYPES.items():
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                for name, (family, length) in self.TEXT_COLUMNS.items():
                    line = self.column_line(name, out)
                    lengths = (length, length * 2) if length else ()
                    self.assertTrue(line.startswith(target_types[family].format(*lengths) + " "), f"{name}: {line}")
                    self.assertEqual(name in self.NOT_NULL_COLUMNS[convertor_class], "NOT NULL" in line,
                                     f"{name}: {line}")
                # 默认值保持原样，SQL Server 带 N 前缀
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assertIn(f"DEFAULT {n}'N'", self.column_line("c", out))
                self.assertIn(f"DEFAULT {n}''", self.column_line("c32", out))

    def test_insert_strings(self):
        # 字符串值只解码一次：SQL Server 加 N 前缀；Oracle 文本列中日期时间格式的值保持为字符串，不转为 to_date
        path = self.write_sql(HEADER + self.TEXT_TABLE)
        columns = "(id, c, c1, c32, v, tt, t, mt, lt, tb, b, mb, lb)"
        plain = "(1, 'N', '', '中文😀', 'it''s', '', '2026-01-01 08:00:00', 'a, b (c)', NULL, NULL, NULL, 'x', NULL)"
        expected = {
            PostgreSQLConvertor: plain + ";",
            KingbaseConvertor: plain + ";",
            OpengaussConvertor: plain + ";",
            HighGoConvertor: plain + ";",
            DM8Convertor: plain + ";",
            OracleConvertor: plain + ";",
            SQLServerConvertor: "(1, N'N', N'', N'中文😀', N'it''s', N'', N'2026-01-01 08:00:00', N'a, b (c)',"
                                " NULL, NULL, NULL, N'x', NULL)\nGO",
        }
        for convertor_class, values in expected.items():
            with self.subTest(convertor_class.__name__):
                out, _ = convert(convertor_class(path))
                self.assertIn(f"INSERT INTO demo_text {columns} VALUES {values}", out)

    def test_ruoyi_vue_pro(self):
        # 官方脚本没有本次补充的 char、tinytext、mediumtext、tinyblob、longblob，各目标数据库的输出不受影响
        convertor = OracleConvertor(os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql"))
        for table_sql in convertor.original_table_script_list:
            self.assertFalse(re.findall(r"(?im)^\s*`\w+`\s+(?:char|tinytext|mediumtext|tinyblob|longblob)\b", table_sql))


class NumericConvertorTest(unittest.TestCase):
    # 目标数据库的定点数类型名与最大精度：PostgreSQL numeric 最大 1000 位，Oracle number、SQL Server numeric、达梦 decimal 最大 38 位
    TARGET_DECIMALS = {
        PostgreSQLConvertor: ("numeric", 1000),
        OracleConvertor: ("number", 38),
        SQLServerConvertor: ("numeric", 38),
        DM8Convertor: ("decimal", 38),
    }
    # 目标数据库浮点数类型的尾数位数：IEEE 754 单精度 24 位，双精度 53 位
    TARGET_FLOATS = {
        PostgreSQLConvertor: {"real": 24, "double precision": 53},
        OracleConvertor: {"binary_float": 24, "binary_double": 53},
        SQLServerConvertor: {"real": 24, "float": 53},
        DM8Convertor: {"real": 24, "double": 53},
    }
    # 人大金仓、openGauss、瀚高继承 PostgreSQL 的类型转换
    BASE_CONVERTORS = {
        PostgreSQLConvertor: PostgreSQLConvertor,
        KingbaseConvertor: PostgreSQLConvertor,
        OpengaussConvertor: PostgreSQLConvertor,
        HighGoConvertor: PostgreSQLConvertor,
        OracleConvertor: OracleConvertor,
        SQLServerConvertor: SQLServerConvertor,
        DM8Convertor: DM8Convertor,
    }
    # (源类型, 解析得到的长度, MySQL 的 (M, D) 或浮点尾数位数)：https://dev.mysql.com/doc/refman/8.0/en/numeric-type-syntax.html
    SOURCE_DECIMALS = [
        ("decimal", None, (10, 0)),
        ("dec", None, (10, 0)),
        ("numeric", None, (10, 0)),
        ("fixed", None, (10, 0)),
        ("decimal", 12, (12, 0)),
        ("numeric", [12], (12, 0)),
        ("dec", [12, 4], (12, 4)),
        ("FIXED", (12, 4), (12, 4)),
        ("decimal", [38, 10], (38, 10)),
        ("decimal", [30, 30], (30, 30)),
        ("decimal", [1, 0], (1, 0)),
        ("decimal unsigned", [10, 2], (10, 2)),
        ("DECIMAL UNSIGNED ZEROFILL", [12, 4], (12, 4)),
        ("numeric zerofill", None, (10, 0)),
        # M 与 D 均为 0 时 MySQL 按 (10,0) 处理
        ("decimal", 0, (10, 0)),
        ("numeric", [0], (10, 0)),
        ("decimal", [0, 0], (10, 0)),
        # 显式有符号
        ("decimal signed", None, (10, 0)),
        ("DEC SIGNED", [12, 4], (12, 4)),
    ]
    SOURCE_FLOATS = [
        ("float", None, 24),
        ("float", 0, 24),
        ("float", [24], 24),
        ("float", 25, 53),
        ("float", 53, 53),
        ("double", None, 53),
        ("double precision", None, 53),
        ("DOUBLE PRECISION UNSIGNED", None, 53),
        # MySQL 默认的 SQL 模式下 real 为 double；REAL_AS_FLOAT 模式不支持
        ("real", None, 53),
        ("float unsigned zerofill", None, 24),
        # 显式有符号
        ("float signed", None, 24),
        ("float signed", 25, 53),
        ("double signed", None, 53),
        ("DOUBLE PRECISION SIGNED", None, 53),
        ("real signed", None, 53),
    ]

    NUMBER_TABLE = """
DROP TABLE IF EXISTS `demo_number`;
CREATE TABLE `demo_number`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `d` decimal NOT NULL DEFAULT 0 COMMENT '缺省 (10,0)',
  `d12` decimal(12) NULL DEFAULT NULL COMMENT '只带 M',
  `d12_4` dec(12,4) NULL DEFAULT NULL COMMENT 'dec 别名',
  `n` numeric NULL DEFAULT NULL COMMENT 'numeric 别名',
  `fx` fixed(12,4) NULL DEFAULT NULL COMMENT 'fixed 别名',
  `du` decimal(10,2) unsigned NULL DEFAULT NULL COMMENT '无符号',
  `d38` decimal(38,10) NULL DEFAULT NULL COMMENT '38 位',
  `d30` decimal(30,30) NULL DEFAULT NULL COMMENT '全部小数',
  `f` float NULL DEFAULT NULL COMMENT '单精度',
  `f0` float(0) NULL DEFAULT NULL COMMENT 'float(0)',
  `f24` float(24) NULL DEFAULT NULL COMMENT 'float(24)',
  `f25` float(25) NULL DEFAULT NULL COMMENT 'float(25)',
  `f53` float(53) NULL DEFAULT NULL COMMENT 'float(53)',
  `db` double NULL DEFAULT NULL COMMENT '双精度',
  `dp` double precision NULL DEFAULT NULL COMMENT 'double precision',
  `d0` decimal(0) NULL DEFAULT NULL COMMENT 'M 为 0，按 10 处理',
  `ds` decimal(12,2) signed NULL DEFAULT NULL COMMENT '显式有符号',
  `fs` float signed NULL DEFAULT NULL COMMENT '显式有符号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '数值表';

BEGIN;
INSERT INTO `demo_number` (`id`, `d`, `d12`, `du`, `d38`, `d30`, `f`, `f25`, `dp`) VALUES (1, -9999999999, 999999999999, 99999999.99, -1234567890123456789012345678.1234567890, 0.123456789012345678901234567890, 3.4028235e38, -1.7976931348623157e308, 2.2250738585072014e-308);
COMMIT;
"""
    # 列名 -> MySQL 的 (M, D) 或浮点尾数位数
    NUMBER_COLUMNS = {
        "d": (10, 0), "d12": (12, 0), "d12_4": (12, 4), "n": (10, 0), "fx": (12, 4), "du": (10, 2),
        "d38": (38, 10), "d30": (30, 30),
        "f": 24, "f0": 24, "f24": 24, "f25": 53, "f53": 53, "db": 53, "dp": 53,
        "d0": (10, 0), "ds": (12, 2), "fs": 24,
    }

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    def assert_holds(self, convertor_class, target_type, source, message):
        """目标类型需要精确容纳源类型：定点数精度不小于 M、小数位数等于 D，浮点数尾数位数不小于源类型"""
        base = self.BASE_CONVERTORS[convertor_class]
        if isinstance(source, tuple):
            name, max_precision = self.TARGET_DECIMALS[base]
            match = re.fullmatch(rf"{name}\((\d+),(\d+)\)", target_type)
            self.assertIsNotNone(match, message)
            precision, scale = int(match.group(1)), int(match.group(2))
            self.assertGreaterEqual(precision, source[0], message)
            self.assertLessEqual(precision, max_precision, message)
            self.assertEqual(source[1], scale, message)
        else:
            self.assertIn(target_type, self.TARGET_FLOATS[base], message)
            self.assertGreaterEqual(self.TARGET_FLOATS[base][target_type], source, message)

    def test_decimal_type(self):
        for source_type, size, expected in self.SOURCE_DECIMALS:
            with self.subTest(f"{source_type} {size}"):
                self.assertEqual(expected, decimal_type(source_type, size))
        # 其它类型：不按定点数处理
        for source_type in ("", "int", "float", "double", "decimals", "varchar"):
            with self.subTest(source_type):
                self.assertIsNone(decimal_type(source_type, None))
        # 定点数类型的精度超出 MySQL 范围（M 为 1 ~ 65，D 为 0 ~ 30 且不大于 M），或修饰不合法：明确失败；
        # signed 与 unsigned、zerofill 同时出现属于矛盾修饰，即使个别 MySQL 版本容忍也不支持
        for source_type, size in (("decimal", 66), ("decimal", [10, 31]), ("decimal", [5, 6]), ("decimal", [0, 31]),
                                  # M 为 0 而 D 不为 0：D 大于 M，MySQL 拒绝，不按 (10,D) 处理
                                  ("decimal", [0, 2]), ("decimal", [0, 30]),
                                  ("decimal", [1, 2, 3]), ("decimal unsigned signed", None),
                                  ("decimal signed zerofill", None), ("numeric auto_increment", None)):
            with self.subTest(f"{source_type} {size}"):
                with self.assertRaises(ValueError):
                    decimal_type(source_type, size)

    def test_float_type(self):
        for source_type, size, bits in self.SOURCE_FLOATS:
            with self.subTest(f"{source_type} {size}"):
                self.assertEqual("float" if bits == 24 else "double", float_type(source_type, size))
        # 其它类型：不按浮点数处理
        for source_type in ("", "int", "decimal", "floaty", "doubles"):
            with self.subTest(source_type):
                self.assertIsNone(float_type(source_type, None))
        # float(p) 的 p 超出 0 ~ 53；已废弃的 float(M,D)、double(M,D)、real(M,D) 会按 D 位小数舍入，不能当作 p；
        # double、real 不能只带一个参数；修饰不合法
        for source_type, size in (("float", 54), ("float", -1), ("float", [10, 2]), ("double", [10, 2]),
                                  ("double precision", [10, 2]), ("real", [10, 2]), ("double", 10), ("real", 10),
                                  ("float signed unsigned", None), ("double signed zerofill", None),
                                  ("double precision extra", None)):
            with self.subTest(f"{source_type} {size}"):
                with self.assertRaises(ValueError):
                    float_type(source_type, size)

    def test_translate_type_range(self):
        path = self.write_sql(HEADER)
        for convertor_class in self.BASE_CONVERTORS:
            convertor = convertor_class(path)
            for source_type, size, source in self.SOURCE_DECIMALS + self.SOURCE_FLOATS:
                with self.subTest(f"{convertor_class.__name__} {source_type} {size}"):
                    target_type = convertor.translate_type(source_type, size)
                    self.assert_holds(convertor_class, target_type, source, target_type)

    def test_decimal_precision_limit(self):
        path = self.write_sql(HEADER)
        for convertor_class, base in self.BASE_CONVERTORS.items():
            convertor = convertor_class(path)
            with self.subTest(convertor_class.__name__):
                # 38 位各目标数据库均精确保存
                self.assert_holds(convertor_class, convertor.translate_type("decimal", [38, 0]), (38, 0), "38")
                if self.TARGET_DECIMALS[base][1] >= 65:
                    self.assertEqual("numeric(65,30)", convertor.translate_type("numeric", [65, 30]))
                    self.assertEqual("numeric(39,0)", convertor.translate_type("decimal", 39))
                else:
                    # 超出目标数据库的最大精度：明确失败，不截断，也不改用浮点数
                    with self.assertRaises(ValueError):
                        convertor.translate_type("decimal", 39)
                    with self.assertRaises(ValueError):
                        convertor.translate_type("numeric", [65, 30])

    def test_numeric_columns(self):
        path = self.write_sql(HEADER + self.NUMBER_TABLE)
        columns = "(id, d, d12, du, d38, d30, f, f25, dp)"
        values = ("(1, -9999999999, 999999999999, 99999999.99, -1234567890123456789012345678.1234567890,"
                  " 0.123456789012345678901234567890, 3.4028235e38, -1.7976931348623157e308, 2.2250738585072014e-308)")
        for convertor_class in self.BASE_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                for name, source in self.NUMBER_COLUMNS.items():
                    line = self.column_line(name, out)
                    # 类型可能含空格（例如 double precision），取 NULL、NOT NULL、DEFAULT 之前的部分
                    target_type = re.match(r"(.+?)\s+(?:NOT NULL|NULL|DEFAULT)\b", line).group(1)
                    self.assert_holds(convertor_class, target_type, source, f"{name}: {line}")
                # 默认值、INSERT 中的数值保持原样；Oracle 的 binary_float、binary_double 列的数值带 F、D 后缀，见 OracleFloatLiteralTest
                self.assertIn("DEFAULT 0", self.column_line("d", out))
                expected = values
                if convertor_class is OracleConvertor:
                    expected = (values.replace(" 3.4028235e38,", " 3.4028235e38F,")
                                .replace(" -1.7976931348623157e308,", " -1.7976931348623157e308D,")
                                .replace(" 2.2250738585072014e-308)", " 2.2250738585072014e-308D)"))
                self.assertIn(f"INSERT INTO demo_number {columns} VALUES {expected}", out)

    def test_invalid_numeric_column(self):
        # 已废弃的 float(M,D) 不能保持按 D 位小数舍入的语义，明确失败，不生成 None 类型
        sql = HEADER + """
CREATE TABLE `demo_float`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `price` float(10,2) NULL DEFAULT NULL COMMENT '价格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '浮点表';
"""
        path = self.write_sql(sql)
        for convertor_class in self.BASE_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                with self.assertRaises(ValueError):
                    convert(convertor_class(path))

    def test_ruoyi_vue_pro(self):
        # 官方脚本没有定点数、浮点数列，各目标数据库的输出不受影响；quartz 的 decimal 列属于不转换的 qrtz 表
        pattern = r"(?im)^\s*`\w+`\s+(?:decimal|dec|numeric|fixed|float|double|real)\b"
        convertor = OracleConvertor(os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql"))
        for table_sql in convertor.original_table_script_list:
            self.assertFalse(re.findall(pattern, table_sql))
        convertor = OracleConvertor(os.path.join(MYSQL_DIR, "quartz.sql"))
        for table_sql in convertor.original_table_script_list:
            if re.findall(pattern, table_sql):
                self.assertTrue(table_names([table_sql])[0].startswith("QRTZ_"))


class OracleFloatLiteralTest(unittest.TestCase):
    # 默认值覆盖 1.5、1e300、带引号的字符串、整数、NULL；以小数点开头的默认值（例如 .5）见 LeadingDotDefaultTest
    FLOAT_TABLE = """
DROP TABLE IF EXISTS `demo_float`;
CREATE TABLE `demo_float`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `f` float NULL DEFAULT 1.5 COMMENT '单精度',
  `d` double NOT NULL DEFAULT 1e300 COMMENT '双精度',
  `f25` float(25) NULL DEFAULT '-1.5E+300' COMMENT 'float(25)',
  `dp` double precision NULL DEFAULT 0 COMMENT 'double precision',
  `r` real NULL DEFAULT NULL COMMENT 'real',
  `num` decimal(38,10) NULL DEFAULT 1.5 COMMENT '定点数',
  `cnt` int NULL DEFAULT 0 COMMENT '整数',
  `note` varchar(64) NULL DEFAULT '1e300' COMMENT '文本',
  `ts` datetime(3) NULL DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '浮点表';

BEGIN;
INSERT INTO `demo_float` (`id`, `f`, `d`, `f25`, `dp`, `r`, `num`, `cnt`, `note`, `ts`) VALUES (1, 1.5, 1e300, -1.5E-300, .5, 1., 1.5, 15, '1.5', '2026-01-01 08:00:00.123');
INSERT INTO `demo_float` (`note`, `d`, `id`, `f`) VALUES ('2.5', '2.5', 2, NULL);
INSERT INTO `demo_float` (`id`, `d`, `cnt`) VALUES (3, -1e300, -1), (4, '+2E5', 2);
INSERT INTO `demo_float` VALUES (5, 0, 0, 0, 0, 0, 0, 0, '0', NULL);
INSERT INTO `demo_float` (`id`, `d`, `f`) VALUES (6, ABS(-1e300), 1.5);
INSERT INTO `demo_float` (`id`, `d`) VALUES (7, /* 注释 */ 1e300);
INSERT INTO `demo_float` (`id`, `unknown`) VALUES (8, 1e300);
INSERT INTO `demo_float` (`id`, `d`) VALUES (9, 1e300, 2);
INSERT INTO `demo_float` (`id`, `d`, `f`, `dp`) VALUES (10, 1.5D, 'abc', 0x10);
COMMIT;
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def assert_line(self, line, out):
        # 列定义、INSERT 均为独立的一行，列定义除最后一列外以逗号结尾
        self.assertRegex(out, rf"(?m)^{re.escape(line)},?$")

    def test_float_literal(self):
        # 简单数值按原文追加后缀，不经过 Python 的 float：超出 IEEE 范围、超长的有效数字均保持原文
        for full_type, value, expected in (
                ("binary_double", "1e300", "1e300D"), ("binary_double", "-1.5E-300", "-1.5E-300D"),
                ("binary_double", "+2e+5", "+2e+5D"), ("binary_double", ".5", ".5D"), ("binary_double", "1.", "1.D"),
                ("binary_double", " 15 ", "15D"), ("binary_double", 0, "0D"), ("binary_double", -7, "-7D"),
                ("binary_double", "'1.5'", "1.5D"), ("binary_double", "1e400", "1e400D"),
                ("binary_double", "0.1000000000000000055511151231257827", "0.1000000000000000055511151231257827D"),
                ("binary_float", "3.4028235e38", "3.4028235e38F")):
            with self.subTest(f"{full_type} {value!r}"):
                self.assertEqual(expected, OracleConvertor.float_literal(full_type, value))
        # NULL、表达式、非数值字符串、已带后缀、十六进制、不完整的数值：不转换
        for value in (None, "NULL", "'abc'", "''", "'1''5'", "' 1.5'", "1.5D", "2F", "0x10", "1e", "e5", ".", "- 1",
                      "ABS(1)", "1+1", True, 1.5):
            with self.subTest(repr(value)):
                self.assertIsNone(OracleConvertor.float_literal("binary_double", value))
        # 其它列类型：不转换
        for full_type in (None, "number", "number(38,10)", "varchar2(64 char)", "clob", "timestamp(3)"):
            with self.subTest(full_type):
                self.assertIsNone(OracleConvertor.float_literal(full_type, "1e300"))

    def test_float_default_and_insert(self):
        out, err = convert(OracleConvertor(self.write_sql(HEADER + self.FLOAT_TABLE)))
        self.assertNotIn("无法正常解析", err)
        # 1. binary_float、binary_double 列的数值默认值带 F、D 后缀，NULL 保持；定点数、整数、文本列的默认值保持原样
        self.assert_line("    f binary_float DEFAULT 1.5F NULL", out)
        self.assert_line("    d binary_double DEFAULT 1e300D NOT NULL", out)
        self.assert_line("    f25 binary_double DEFAULT -1.5E+300D NULL", out)
        self.assert_line("    dp binary_double DEFAULT 0D NULL", out)
        self.assert_line("    r binary_double DEFAULT NULL NULL", out)
        self.assert_line("    num number(38,10) DEFAULT 1.5 NULL", out)
        self.assert_line("    cnt number DEFAULT 0 NULL", out)
        self.assert_line("    note varchar2(64 char) DEFAULT '1e300' NULL", out)

        # 2. INSERT 按列对应：浮点数列的简单数值带后缀，引号括起的纯数值去掉引号；同一数值在定点数、整数、文本列保持原样；
        # timestamp 列的小数秒同时转换
        self.assert_line("INSERT INTO demo_float (id, f, d, f25, dp, r, num, cnt, note, ts) VALUES (1, 1.5F, 1e300D,"
                         " -1.5E-300D, .5D, 1.D, 1.5, 15, '1.5', TIMESTAMP '2026-01-01 08:00:00.123');", out)
        # 重排、省略的列清单，NULL 保持
        self.assert_line("INSERT INTO demo_float (note, d, id, f) VALUES ('2.5', 2.5D, 2, NULL);", out)
        # 多行 VALUES
        self.assert_line("INSERT INTO demo_float (id, d, cnt) VALUES (3, -1e300D, -1), (4, +2E5D, 2);", out)
        # 没有列清单时按建表的列顺序对应
        self.assert_line("INSERT INTO demo_float VALUES (5, 0F, 0D, 0D, 0D, 0D, 0, 0, '0', NULL);", out)
        # 函数参数中的数值不转换，同一行的其它浮点数列仍然转换
        self.assert_line("INSERT INTO demo_float (id, d, f) VALUES (6, ABS(-1e300), 1.5F);", out)

        # 3. 字符串外的注释、未知列、列数不一致：无法安全对应，整条语句保持原样；已带后缀、非数值字符串、十六进制不转换
        self.assert_line("INSERT INTO demo_float (id, d) VALUES (7, /* 注释 */ 1e300);", out)
        self.assert_line("INSERT INTO demo_float (id, unknown) VALUES (8, 1e300);", out)
        self.assert_line("INSERT INTO demo_float (id, d) VALUES (9, 1e300, 2);", out)
        self.assert_line("INSERT INTO demo_float (id, d, f, dp) VALUES (10, 1.5D, 'abc', 0x10);", out)

    def test_other_convertors_unchanged(self):
        # F、D 后缀只用于 Oracle，其它目标数据库的默认值、INSERT 保持原样
        path = self.write_sql(HEADER + self.FLOAT_TABLE)
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertIn("DEFAULT 1e300", out)
                self.assertIn("(3, -1e300, -1), (4, ", out)
                for literal in ("1e300D", "1.5F", "-1.5E+300D", "0D"):
                    self.assertNotIn(literal, out)


class LeadingDotDefaultTest(unittest.TestCase):
    # MySQL 允许以小数点开头的数值默认值，simple-ddl-parser 无法直接解析
    DOT_TABLE = """
DROP TABLE IF EXISTS `demo_dot`;
CREATE TABLE `demo_dot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `f` double NULL DEFAULT .5 COMMENT '双精度',
  `fn` double NULL DEFAULT -.5 COMMENT '负数',
  `fp` float NULL DEFAULT +.5 COMMENT '正号',
  `fe` double NULL DEFAULT .5e-3 COMMENT '科学计数',
  `num` decimal(10,2) NULL DEFAULT .5 COMMENT '定点数',
  `note` varchar(64) NULL DEFAULT 'DEFAULT .5' COMMENT 'DEFAULT .5',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '小数点表';

BEGIN;
INSERT INTO `demo_dot` (`id`, `f`, `num`, `note`) VALUES (1, .5, .5, 'DEFAULT .5');
COMMIT;
"""
    # 列名 -> 默认值：小数点前补 0，Oracle 的 binary_float、binary_double 列另带 F、D 后缀
    DEFAULTS = {"f": "0.5", "fn": "-0.5", "fp": "+0.5", "fe": "0.5e-3", "num": "0.5"}
    ORACLE_DEFAULTS = {"f": "0.5D", "fn": "-0.5D", "fp": "+0.5F", "fe": "0.5e-3D", "num": "0.5"}

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_protect_default_numbers(self):
        # 字符串、注释、反引号标识符之外 DEFAULT 后的完整简单数值：替换为占位标识，小数点前补 0，按文本处理
        for value, expected in ((".5", "0.5"), ("-.5", "-0.5"), ("+.5", "+0.5"), (".5e-3", "0.5e-3"),
                                (".25E+300", "0.25E+300"), (".1000000000000000055511151231257827",
                                                            "0.1000000000000000055511151231257827")):
            with self.subTest(value):
                sql, numbers = protect_default_numbers(f"`a` double NULL default {value},")
                self.assertEqual([expected], list(numbers.values()))
                self.assertEqual(f"`a` double NULL default {list(numbers)[0]},", sql)
        # 字符串（含 JSON 文本）、注释、标识符、表达式、已有整数部分、不完整的数值：保持原样
        for sql in ("`a` varchar(9) DEFAULT 'DEFAULT .5'", "`a` json DEFAULT '{\"v\": \"DEFAULT .5\"}'",
                    "`a` double /* DEFAULT .5 */ NULL", "`a` double -- DEFAULT .5\n NULL", "`a` double # DEFAULT .5\n",
                    "`DEFAULT .5` double NULL", "`a` double DEFAULT .5 + 1", "`a` double DEFAULT .5*2",
                    "`a` double DEFAULT 0.5", "`a` double DEFAULT .5x", "`a` double DEFAULT .5.1",
                    "`a` double DEFAULT ..5", "`a` double XDEFAULT .5", "`a` double DEFAULT .",
                    "`yudao_default_number_0` double DEFAULT .5"):
            with self.subTest(sql):
                self.assertEqual((sql, {}), protect_default_numbers(sql))

    def test_leading_dot_default(self):
        path = self.write_sql(HEADER + self.DOT_TABLE)
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                OracleConvertor, SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertIn("CREATE TABLE demo_dot (", out)
                self.assertNotIn("yudao_default_number", out)
                defaults = self.ORACLE_DEFAULTS if convertor_class is OracleConvertor else self.DEFAULTS
                for name, value in defaults.items():
                    line = re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)
                    self.assertRegex(line, rf"DEFAULT {re.escape(value)}(?:\s|$)", name)
                # 字符串默认值保持原样，SQL Server 带 N 前缀
                self.assertIn("DEFAULT N'DEFAULT .5'" if convertor_class is SQLServerConvertor
                              else "DEFAULT 'DEFAULT .5'", out)
                # INSERT 原文不变；只有 Oracle 的 binary_double 列带 D 后缀，SQL Server 原有逻辑为字符串加 N 前缀
                insert = {
                    OracleConvertor: "(1, .5D, .5, 'DEFAULT .5')",
                    SQLServerConvertor: "(1, .5, .5, N'DEFAULT .5')",
                }.get(convertor_class, "(1, .5, .5, 'DEFAULT .5')")
                self.assertIn(f"VALUES {insert}", out)

    def test_ruoyi_vue_pro(self):
        # 官方脚本没有以小数点开头的数值默认值，解析前的建表语句保持原样，各目标数据库的输出不受影响
        for sql_file in ("ruoyi-vue-pro.sql", "quartz.sql"):
            convertor = PostgreSQLConvertor(os.path.join(MYSQL_DIR, sql_file))
            for table_sql in convertor.table_script_list:
                with self.subTest(f"{sql_file} {table_names([table_sql])[0]}"):
                    self.assertEqual({}, protect_default_numbers(table_sql)[1])


class TypeAliasConvertorTest(unittest.TestCase):
    # MySQL 数值类型的别名 -> 标准写法：https://dev.mysql.com/doc/refman/8.0/en/other-vendor-data-types.html
    INTEGER_ALIASES = [("int1", "tinyint"), ("INT2", "smallint"), ("int3", "mediumint"), ("MiddleInt", "mediumint"),
                       ("int4", "int"), ("INT8", "bigint"), ("bool", "tinyint"), ("BOOLEAN", "tinyint")]
    FLOAT_ALIASES = [("float4", "float"), ("FLOAT8", "double")]
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)

    # {0} ~ {7} 依次为各列的类型；别名、标准写法使用相同的列名、默认值、注释与数据，只有类型不同
    ALIAS_TABLE = """
DROP TABLE IF EXISTS `demo_alias`;
CREATE TABLE `demo_alias`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `b` {0} NOT NULL DEFAULT 2 COMMENT 'BOOL 可以保存 2',
  `bu` {1} NULL DEFAULT 127 COMMENT 'BOOLEAN',
  `i1` {2} NULL DEFAULT NULL COMMENT 'INT1 FLOAT4',
  `i3` {3} NULL DEFAULT 16777215 COMMENT 'MIDDLEINT',
  `i8` {4} NULL DEFAULT NULL COMMENT 'INT8',
  `f4` {5} NULL DEFAULT 1.5 COMMENT 'FLOAT4',
  `f4d` {6} NULL DEFAULT NULL COMMENT 'FLOAT4(25)',
  `f8` {7} NULL DEFAULT 1e300 COMMENT 'FLOAT8',
  `note` varchar(64) NULL DEFAULT 'INT1 FLOAT4 BOOL' COMMENT '文本',
  `ts` datetime(3) NULL DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '别名表';

BEGIN;
INSERT INTO `demo_alias` (`id`, `b`, `bu`, `i1`, `i3`, `i8`, `f4`, `f4d`, `f8`, `note`, `ts`) VALUES (1, 2, 127, -128, 16777215, 18446744073709551615, 1.5, -1e300, 1e300, 'FLOAT8', '2026-01-01 08:00:00.123');
INSERT INTO `demo_alias` (`id`, `b`, `bu`, `i1`, `note`) VALUES (2, -128, 0, 127, 'INT8'), (3, 127, 1, 0, 'BOOLEAN');
COMMIT;
"""
    # MySQL 只接受不带属性的 bool、boolean；其它整数别名可带显示宽度与 signed、unsigned、zerofill
    ALIAS_TYPES = ("bool", "boolean", "int1(4) signed", "middleint(8) unsigned", "int8 unsigned zerofill",
                   "float4", "float4(25)", "float8")
    CANONICAL_TYPES = ("tinyint(1)", "tinyint(1)", "tinyint(4) signed", "mediumint(8) unsigned",
                       "bigint unsigned zerofill", "float", "float(25)", "double")

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, name, content):
        path = os.path.join(self.directory, name)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    @staticmethod
    def convert_body(convertor):
        # 去掉文件头中随生成时间变化的 Date 行
        out, err = convert(convertor)
        return re.sub(r"(?m)^ Date: .*$", "", out), err

    def test_integer_type_alias(self):
        # 别名与标准写法的识别结果相同，修饰的规则不变；bool、boolean 只有不带属性的写法合法
        for alias, canonical in self.INTEGER_ALIASES:
            bare = alias.lower() in ("bool", "boolean")
            for options in ("", " signed", " unsigned", " zerofill", " UNSIGNED ZEROFILL"):
                with self.subTest(alias + options):
                    if bare and options:
                        # MySQL 中 bool signed、boolean unsigned 等为语法错误，不按整数类型处理
                        self.assertIsNone(integer_type(alias + options))
                        continue
                    self.assertEqual(integer_type(canonical + options), integer_type(alias + options))
                    self.assertIsNotNone(integer_type(alias + options))
        self.assertEqual(("tinyint", False), integer_type("BOOL"))
        self.assertEqual(("tinyint", False), integer_type("boolean"))
        # 矛盾或未知的修饰、不是别名的单词：不按整数类型处理
        for source_type in ("int1 signed unsigned", "bool signed zerofill", "boolean auto_increment", "int9", "int16",
                            "booleans", "float4", "middle int"):
            with self.subTest(source_type):
                self.assertIsNone(integer_type(source_type))

    def test_float_type_alias(self):
        # float4 与 float 的 p 分界相同，float8 与 double 相同
        for alias, canonical in self.FLOAT_ALIASES:
            for source_type, size in ((alias, None), (alias + " unsigned", None), (alias + " SIGNED", None),
                                      (alias + " unsigned zerofill", None)):
                with self.subTest(f"{source_type} {size}"):
                    self.assertEqual(float_type(canonical + source_type[len(alias):], size), float_type(source_type, size))
        # float8 precision 与 double precision 相同，可带 signed、unsigned
        for options in ("", " SIGNED", " unsigned"):
            with self.subTest("float8 precision" + options):
                self.assertEqual("double", float_type("FLOAT8 PRECISION" + options, None))
                self.assertEqual(float_type("double precision" + options, None),
                                 float_type("float8 precision" + options, None))
        for size, expected in ((0, "float"), (24, "float"), ([24], "float"), (25, "double"), (53, "double")):
            with self.subTest(f"float4 {size}"):
                self.assertEqual(expected, float_type("float4", size))
                self.assertEqual(float_type("float", size), float_type("FLOAT4", size))
        # 与标准写法一样明确拒绝：p 超出范围、已废弃的 (M,D)、float8 带参数（float8(53) 与 double(53) 一样为语法错误）、
        # 矛盾或未知的修饰
        for source_type, size in (("float4", 54), ("float4", [10, 2]), ("float8", [10, 2]), ("float8", 10),
                                  ("float8", 53), ("float8 precision", 53), ("float4 signed unsigned", None),
                                  ("float8 signed zerofill", None), ("float8 precision zerofill signed", None),
                                  ("float4 extra", None)):
            with self.subTest(f"{source_type} {size}"):
                with self.assertRaises(ValueError):
                    float_type(source_type, size)
        # 不是别名的单词：不按浮点数处理
        for source_type in ("float2", "float16", "float44"):
            with self.subTest(source_type):
                self.assertIsNone(float_type(source_type, None))

    def test_translate_type_alias(self):
        path = self.write_sql("dump.sql", HEADER)
        for convertor_class in self.CONVERTORS:
            convertor = convertor_class(path)
            for alias, canonical in self.INTEGER_ALIASES + self.FLOAT_ALIASES:
                for size in (None, 1, 4):
                    # double 不能带参数；bool(1)、boolean(1) 在 MySQL 中为语法错误，不作为合法输入
                    if size is not None and (canonical == "double" or alias.lower() in ("bool", "boolean")):
                        continue
                    with self.subTest(f"{convertor_class.__name__} {alias}({size})"):
                        target_type = convertor.translate_type(alias, size)
                        self.assertIsNotNone(target_type)
                        self.assertEqual(convertor.translate_type(canonical, size), target_type)
            # bool、boolean 按 tinyint 的取值范围转换，不转为目标数据库的布尔类型
            with self.subTest(f"{convertor_class.__name__} boolean"):
                self.assertNotIn(convertor.translate_type("boolean", None), ("bool", "boolean", "bit", "number(1,0)"))

    def test_alias_columns(self):
        alias_path = self.write_sql("alias.sql", HEADER + self.ALIAS_TABLE.format(*self.ALIAS_TYPES))
        canonical_path = self.write_sql("canonical.sql", HEADER + self.ALIAS_TABLE.format(*self.CANONICAL_TYPES))
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = self.convert_body(convertor_class(alias_path))
                canonical_out, canonical_err = self.convert_body(convertor_class(canonical_path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                # 别名与标准写法的建表、默认值、INSERT 逐字相同
                self.assertEqual(canonical_out, out)
                self.assertEqual(canonical_err, err)
                # 字符串、注释中的类型名，以及 BOOL 列中的非布尔值保持原样
                self.assertIn("'INT1 FLOAT4 BOOL'", out)
                self.assertIn("'BOOL 可以保存 2'", out)
                self.assertRegex(out, r"(2, -128, 0, 127, N?'INT8'), (3, 127, 1, 0, N?'BOOLEAN')".replace("(", r"\(")
                                 .replace(")", r"\)"))
        # Oracle：float4、float8 为 binary_float、binary_double，默认值与 INSERT 的数值带 F、D 后缀，小数秒的时间列仍转换
        out, _ = convert(OracleConvertor(alias_path))
        self.assertRegex(out, r"(?m)^    f4 binary_float DEFAULT 1\.5F NULL,$")
        self.assertRegex(out, r"(?m)^    f8 binary_double DEFAULT 1e300D NULL,$")
        self.assertIn("1.5F, -1e300D, 1e300D, 'FLOAT8', TIMESTAMP '2026-01-01 08:00:00.123')", out)

    def test_ruoyi_vue_pro(self):
        # 官方脚本没有数值类型的别名，各目标数据库的输出不受影响
        pattern = r"(?im)^\s*`\w+`\s+(?:int[1-48]|middleint|bool|boolean|float[48])\b"
        for sql_file in ("ruoyi-vue-pro.sql", "quartz.sql"):
            convertor = OracleConvertor(os.path.join(MYSQL_DIR, sql_file))
            for table_sql in convertor.original_table_script_list:
                self.assertFalse(re.findall(pattern, table_sql), sql_file)


class BooleanLiteralConvertorTest(unittest.TestCase):
    # MySQL 中不带引号的 TRUE、FALSE 即 1、0：数值列转为 1、0；单个位的 bit 列，目标类型为 bool 的 PostgreSQL 系为 TRUE、FALSE，
    # 其它为 1、0；bool、boolean 为 tinyint 数值类型，不是布尔类型
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    PG_FAMILY = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor)
    # 单个位的 bit 列的目标类型
    FLAG_TYPES = {PostgreSQLConvertor: "bool", KingbaseConvertor: "bool", OpengaussConvertor: "bool",
                  HighGoConvertor: "bool", OracleConvertor: "number(1,0)", SQLServerConvertor: "bit", DM8Convertor: "bit"}

    BOOL_TABLE = """
DROP TABLE IF EXISTS `demo_bool`;
CREATE TABLE `demo_bool`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `b` bool NULL DEFAULT TRUE COMMENT 'BOOL',
  `bn` boolean NOT NULL DEFAULT fAlSe COMMENT 'BOOLEAN',
  `t` tinyint NULL DEFAULT 2 COMMENT '普通整数',
  `i` int NULL DEFAULT NULL COMMENT '整数',
  `d` decimal(10,2) NULL DEFAULT TRUE COMMENT '定点数',
  `f4` float4 NULL DEFAULT TRUE COMMENT 'float4',
  `f8` float8 NULL DEFAULT false COMMENT 'float8',
  `flag` bit(1) NULL DEFAULT TRUE COMMENT '单个位',
  `note` varchar(64) NULL DEFAULT 'TRUE' COMMENT '文本',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '布尔表';

BEGIN;
INSERT INTO `demo_bool` (`id`, `b`, `bn`, `t`, `i`, `d`, `f4`, `f8`, `flag`, `note`, `deleted`) VALUES (1, TrUe, FALSE, 2, -128, true, TRUE, false, TRUE, 'TRUE, FALSE', FALSE);
INSERT INTO `demo_bool` (`note`, `deleted`, `id`, `flag`) VALUES ('{"truth":true,"false":false}', TRUE, 2, false);
INSERT INTO `demo_bool` (`id`, `b`, `t`) VALUES (3, 127, TRUE), (4,  false , NULL);
INSERT INTO `demo_bool` VALUES (5, FALSE, TRUE, 127, NULL, 1.5, FALSE, TRUE, FALSE, 'FALSE', TRUE);
INSERT INTO `demo_bool` (`id`, `b`, `i`, `t`) VALUES (6, TRUE + 1, ABS(TRUE), TRUE);
COMMIT;
"""

    TIME_TABLE = """
DROP TABLE IF EXISTS `demo_bool_time`;
CREATE TABLE `demo_bool_time`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `f4` float4 NULL DEFAULT FALSE COMMENT '单精度',
  `f8` double NULL DEFAULT 1e300 COMMENT '双精度',
  `ts` datetime(3) NULL DEFAULT NULL COMMENT '毫秒',
  `dt` datetime NULL DEFAULT NULL COMMENT '秒',
  `n` int NULL DEFAULT TRUE COMMENT '整数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '浮点时间表';

BEGIN;
INSERT INTO `demo_bool_time` (`id`, `f4`, `f8`, `ts`, `dt`, `n`) VALUES (1, TRUE, 1e300, '2026-01-01 08:00:00.123', '2026-01-01 08:00:00', FALSE);
INSERT INTO `demo_bool_time` VALUES (2, 1.5, FALSE, '2026-01-01 08:00:00', NULL, TRUE);
COMMIT;
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    def expected_defaults(self, convertor_class):
        pg = convertor_class in self.PG_FAMILY
        oracle = convertor_class is OracleConvertor
        return {"b": "1", "bn": "0", "t": "2", "d": "1", "f4": "1F" if oracle else "1", "f8": "0D" if oracle else "0",
                "flag": "TRUE" if pg else "1", "note": ("N" if convertor_class is SQLServerConvertor else "") + "'TRUE'"}

    def expected_inserts(self, convertor_class):
        # SQL Server 原有逻辑为字符串加 N 前缀；Oracle 的 binary_float、binary_double 列带 F、D 后缀
        bit = ("TRUE", "FALSE") if convertor_class in self.PG_FAMILY else ("1", "0")
        oracle = convertor_class is OracleConvertor
        f4 = ("1F", "0F") if oracle else ("1", "0")
        f8 = ("1D", "0D") if oracle else ("1", "0")
        n = "N" if convertor_class is SQLServerConvertor else ""
        return [
            # deleted 在 PostgreSQL 系为 int2，即使源类型为 bit(1) 也转为 1、0
            f"(1, 1, 0, 2, -128, 1, {f4[0]}, {f8[1]}, {bit[0]}, {n}'TRUE, FALSE', 0)",
            f"({n}'{{\"truth\":true,\"false\":false}}', 1, 2, {bit[1]})",
            "(3, 127, 1), (4,  0 , NULL)",
            f"(5, 0, 1, 127, NULL, 1.5, {f4[1]}, {f8[0]}, {bit[1]}, {n}'FALSE', 1)",
            "(6, TRUE + 1, ABS(TRUE), 1)",
        ]

    def test_boolean_value(self):
        # 只识别完整的、不带引号的 TRUE、FALSE，不区分大小写，可带前后空白；解析器返回的 Python bool 同样识别
        for value, expected in ((True, True), (False, False), ("TRUE", True), ("fAlSe", False), (" true ", True),
                                ("\tFALSE\n", False)):
            with self.subTest(repr(value)):
                self.assertIs(expected, boolean_value(value))
        for value in (None, 0, 1, 2, "", "1", "0", "NULL", "'TRUE'", '"TRUE"', "TRUE+1", "TRUE + 1", "NOT TRUE",
                      "true_value", "TRUEX", "ABS(TRUE)"):
            with self.subTest(repr(value)):
                self.assertIsNone(boolean_value(value))

    def test_literal_kind_and_convert_literal(self):
        path = self.write_sql(HEADER)
        for convertor_class in self.CONVERTORS:
            convertor = convertor_class(path)
            pg = convertor_class in self.PG_FAMILY
            oracle = convertor_class is OracleConvertor
            with self.subTest(convertor_class.__name__):
                # 单个位的 bit；多个位的位串不是布尔值
                self.assertEqual("bit", convertor.literal_kind("bit", None))
                self.assertEqual("bit", convertor.literal_kind("bit", 1))
                self.assertIsNone(convertor.literal_kind("bit", 8))
                # bool、boolean 及其它整数、定点数为数值
                for type, size in (("bool", None), ("BOOLEAN", None), ("tinyint", 1), ("int8", None),
                                   ("decimal", [10, 2]), ("numeric", None)):
                    self.assertEqual("number", convertor.literal_kind(type, size), type)
                # 浮点数：Oracle 为 binary_float、binary_double，其它为数值
                for type, oracle_kind in (("float4", "binary_float"), ("float8", "binary_double"),
                                          ("double precision", "binary_double")):
                    self.assertEqual(oracle_kind if oracle else "number", convertor.literal_kind(type, None), type)
                # 文本：不带引号的 TRUE、FALSE 与 MySQL 一致保存为文本 '1'、'0'；日期时间：不转换 TRUE、FALSE
                n = "N" if convertor_class is SQLServerConvertor else ""
                for type, size in (("varchar", 64), ("text", None)):
                    kind = convertor.literal_kind(type, size)
                    self.assertEqual(f"{n}'1'", convertor.convert_literal(kind, "TRUE"), type)
                    self.assertEqual(f"{n}'0'", convertor.convert_literal(kind, False), type)
                self.assertIsNone(convertor.convert_literal(convertor.literal_kind("datetime", None), "TRUE"))
                self.assertEqual("timestamp" if oracle else None, convertor.literal_kind("datetime", 3))

                # 数值列：TRUE、FALSE（含 Python bool）转为 1、0；False 转换后为 "0"，不能当作未转换
                for value, expected in ((True, "1"), (False, "0"), ("TRUE", "1"), (" false ", "0")):
                    self.assertEqual(expected, convertor.convert_literal("number", value), repr(value))
                for value in ("'TRUE'", "TRUE + 1", 2, "NULL", None):
                    self.assertIsNone(convertor.convert_literal("number", value), repr(value))
                # 单个位的 bit 列
                self.assertEqual("TRUE" if pg else "1", convertor.convert_literal("bit", True))
                self.assertEqual("FALSE" if pg else "0", convertor.convert_literal("bit", "False"))
                # 其它列不转换
                self.assertIsNone(convertor.convert_literal(None, "TRUE"))
        # Oracle 浮点数列：TRUE、FALSE 先转为 1、0，再带 F、D 后缀；timestamp 列只转换带小数秒的值
        oracle = OracleConvertor(path)
        self.assertEqual("1F", oracle.convert_literal("binary_float", True))
        self.assertEqual("0D", oracle.convert_literal("binary_double", "FALSE"))
        self.assertEqual("1e300D", oracle.convert_literal("binary_double", "1e300"))
        self.assertEqual("TIMESTAMP '2026-01-01 08:00:00.123'",
                         oracle.convert_literal("timestamp", "'2026-01-01 08:00:00.123'"))
        # timestamp 列的整秒值同样为 TIMESTAMP 字面量；date 列为 to_date
        self.assertEqual("TIMESTAMP '2026-01-01 08:00:00'", oracle.convert_literal("timestamp", "'2026-01-01 08:00:00'"))
        self.assertEqual("to_date('2026-01-01 08:00:00', 'SYYYY-MM-DD HH24:MI:SS')",
                         oracle.convert_literal("date", "'2026-01-01 08:00:00'"))
        self.assertIsNone(oracle.convert_literal("timestamp", "TRUE"))

    def test_bool_columns(self):
        path = self.write_sql(HEADER + self.BOOL_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                # 1. 默认值：TRUE、FALSE 按列类型转换；普通整数、字符串 'TRUE' 保持原样
                for name, value in self.expected_defaults(convertor_class).items():
                    self.assertRegex(self.column_line(name, out), rf"DEFAULT {re.escape(value)}(?:\s|$)", name)
                # 2. bool、boolean 为数值类型，不转为目标数据库的布尔类型；bit(1) 为各自的单个位类型
                for name in ("b", "bn"):
                    self.assertNotIn(self.column_line(name, out).split()[0], ("bool", "boolean", "bit", "number(1,0)"))
                self.assertTrue(self.column_line("flag", out).startswith(self.FLAG_TYPES[convertor_class] + " "))
                # 3. deleted 在 PostgreSQL 系保持原有的 int2 DEFAULT 0
                if convertor_class in self.PG_FAMILY:
                    self.assertEqual("int2 NOT NULL DEFAULT 0", self.column_line("deleted", out))
                # 4. INSERT：显式、重排、部分、省略的列清单，多行 VALUES；字符串、JSON、表达式、函数参数保持原样
                for values in self.expected_inserts(convertor_class):
                    self.assertIn(f"VALUES {values}", out)
                self.assertNotIn("TrUe", out)
                self.assertNotIn("fAlSe", out)

    def test_convert_default(self):
        path = self.write_sql(HEADER + self.BOOL_TABLE)
        for convertor_class in self.CONVERTORS:
            convertor = convertor_class(path)
            convert(convertor)
            with self.subTest(convertor_class.__name__):
                # False 转换后为 "0"，不能因为是假值而保留原值；表名、列名不区分大小写
                self.assertEqual("0", convertor.convert_default("demo_bool", "bn", False))
                self.assertEqual("1", convertor.convert_default("DEMO_BOOL", "B", "TRUE"))
                self.assertEqual("1", convertor.convert_default("demo_bool", "deleted", True))
                # 文本列的 FALSE 与 MySQL 一致为文本 '0'；普通数值、未知的表与列保持原样
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assertEqual(f"{n}'0'", convertor.convert_default("demo_bool", "note", False))
                self.assertEqual(2, convertor.convert_default("demo_bool", "t", 2))
                self.assertEqual("TRUE", convertor.convert_default("demo_bool", "unknown", "TRUE"))
                self.assertEqual("TRUE", convertor.convert_default("unknown", "b", "TRUE"))

    def test_insert_fallback(self):
        path = self.write_sql(HEADER + self.BOOL_TABLE)
        # 未知列、列数不一致、字符串外的注释、未闭合的引号、双引号、反引号、括号不平衡、其它语法：整条语句保持原样
        unchanged = (
            "INSERT INTO demo_bool (id, nope) VALUES (1, TRUE);",
            "INSERT INTO demo_bool (id, b) VALUES (1, TRUE, 2);",
            "INSERT INTO demo_bool (id, b) VALUES (1, TRUE), (2);",
            "INSERT INTO demo_bool (id, b) VALUES (1, /* x */ TRUE);",
            "INSERT INTO demo_bool (id, b) VALUES (1, TRUE); -- TRUE",
            "INSERT INTO demo_bool (id, b) VALUES (1, TRUE) # TRUE;",
            "INSERT INTO demo_bool (id, note, b) VALUES (1, 'abc, TRUE);",
            'INSERT INTO demo_bool (id, note, b) VALUES (1, "x", TRUE);',
            "INSERT INTO demo_bool (id, note, b) VALUES (1, `x`, TRUE);",
            "INSERT INTO demo_bool (id, b) VALUES (1, (TRUE);",
            "INSERT INTO demo_bool (id, b) VALUES (1, TRUE) junk;",
            "INSERT demo_bool VALUES (1, TRUE);",
            # 没有需要转换的值：字符串 'TRUE'、表达式、函数参数
            "INSERT INTO demo_bool (id, b, i, t) VALUES (1, 'TRUE', ABS(TRUE), TRUE + 1);",
        )
        for convertor_class in self.CONVERTORS:
            convertor = convertor_class(path)
            convert(convertor)
            n = "N" if convertor_class is SQLServerConvertor else ""
            with self.subTest(convertor_class.__name__):
                # 整条语句只转换完整的字符串（SQL Server 带 N 前缀），其它保持原样
                for script in unchanged:
                    self.assertEqual(script.replace("'TRUE'", f"{n}'TRUE'"),
                                     convertor.convert_insert_values("demo_bool", script), script)
                # 未知的表
                script = "INSERT INTO unknown_table (id, b) VALUES (1, TRUE);"
                self.assertEqual(script, convertor.convert_insert_values("unknown_table", script))
                # 按列转换：数值列的 TRUE、FALSE 为 1、0，文本列的 TRUE 为文本 '1'；字符串内注释样式的文本保持原样
                self.assertEqual(
                    f"INSERT INTO demo_bool (id, note, b) VALUES (1, {n}'/* TRUE */ -- TRUE # TRUE', 1);",
                    convertor.convert_insert_values(
                        "demo_bool", "INSERT INTO demo_bool (id, note, b) VALUES (1, '/* TRUE */ -- TRUE # TRUE', TRUE);"))
                self.assertEqual(
                    f"INSERT INTO demo_bool (id, note, b) VALUES (2, {n}'1', 0);",
                    convertor.convert_insert_values("demo_bool", "INSERT INTO demo_bool (id, note, b) VALUES (2, TRUE, FALSE);"))

    def test_oracle_float_and_timestamp(self):
        path = self.write_sql(HEADER + self.TIME_TABLE)
        out, err = convert(OracleConvertor(path))
        self.assertNotIn("无法正常解析", err)
        # 浮点数列：TRUE、FALSE 先转为 1、0，再带 F、D 后缀；1e300 保持 D 后缀；整数列为 1、0
        self.assertEqual("binary_float DEFAULT 0F NULL", self.column_line("f4", out))
        self.assertEqual("binary_double DEFAULT 1e300D NULL", self.column_line("f8", out))
        self.assertEqual("number DEFAULT 1 NULL", self.column_line("n", out))
        # timestamp 列的值（含整秒）转为 TIMESTAMP 字面量，date 列为 to_date，不重复处理
        to_date = "to_date('2026-01-01 08:00:00', 'SYYYY-MM-DD HH24:MI:SS')"
        self.assertIn(f"VALUES (1, 1F, 1e300D, TIMESTAMP '2026-01-01 08:00:00.123', {to_date}, 0);", out)
        self.assertIn("VALUES (2, 1.5F, 0D, TIMESTAMP '2026-01-01 08:00:00', NULL, 1);", out)
        for wrong in ("TIMESTAMP to_date", "to_date(to_date", "to_date(TIMESTAMP", "1e300DD", "0DD", "1FF", "0FF"):
            self.assertNotIn(wrong, out)
        # 其它目标数据库：不带 F、D 后缀，TRUE、FALSE 为 1、0
        for convertor_class in self.CONVERTORS:
            if convertor_class is OracleConvertor:
                continue
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("1e300D", out)
                self.assertIn("VALUES (1, 1, 1e300, ", out)
                self.assertIn("VALUES (2, 1.5, 0, ", out)
                self.assertRegex(self.column_line("f4", out), r"DEFAULT 0(?:\s|$)")
                self.assertRegex(self.column_line("n", out), r"DEFAULT 1(?:\s|$)")

    def test_direct_gen_create_and_insert(self):
        # 不经过 print()，直接调用原有的 gen_create、gen_insert 时，同样按列类型转换默认值、INSERT 中的字面量；重复生成结果相同
        sql = HEADER + """
CREATE TABLE `demo_direct`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `f8` double NULL DEFAULT 1e300,
  `ts` datetime(3) NULL DEFAULT NULL,
  `b` bool NULL DEFAULT TRUE,
  `flag` bit(1) NULL DEFAULT FALSE,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB;

INSERT INTO `demo_direct` (`id`, `f8`, `ts`, `b`, `flag`) VALUES (1, 1e300, '2026-01-01 08:00:00.123', TRUE, TRUE);
"""
        path = self.write_sql(sql)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                ddl = DDLParser(convertor.table_script_list[0].replace("`", "")).run()[0]
                create = convertor.gen_create(ddl)
                insert = convertor.gen_insert(ddl["table_name"])
                self.assertEqual(create, convertor.gen_create(ddl))
                self.assertEqual(insert, convertor.gen_insert(ddl["table_name"]))
                pg = convertor_class in self.PG_FAMILY
                oracle = convertor_class is OracleConvertor
                # 默认值：Oracle 的 binary_double 带 D 后缀；bool 为数值 1；单个位的 bit 在 PostgreSQL 系为 FALSE，其它为 0
                f8 = self.column_line("f8", create)
                self.assertIn("DEFAULT 1e300D" if oracle else "DEFAULT 1e300", f8)
                if not oracle:
                    self.assertNotIn("1e300D", f8)
                self.assertRegex(self.column_line("b", create), r"DEFAULT 1(?:\s|$)")
                self.assertRegex(self.column_line("flag", create), r"DEFAULT FALSE(?:\s|$)" if pg else r"DEFAULT 0(?:\s|$)")
                # INSERT：Oracle 的小数秒转为 TIMESTAMP 字面量，SQL Server 字符串加 N 前缀
                ts = "'2026-01-01 08:00:00.123'"
                if oracle:
                    ts = "TIMESTAMP " + ts
                elif convertor_class is SQLServerConvertor:
                    ts = "N" + ts
                values = f"(1, {'1e300D' if oracle else '1e300'}, {ts}, 1, {'TRUE' if pg else '1'})"
                self.assertIn(f"VALUES {values}", insert)


class StringCapacityConvertorTest(unittest.TestCase):
    # MySQL varchar(n) 最多 n 个字符，按全部为 4 字节的增补字符（例如 😀）保留全部取值，不截断长度：
    # Oracle（MAX_STRING_SIZE = STANDARD、AL32UTF8）varchar2 最长 4000 字节，即 1000 个字符，更长的无索引列改用 clob；
    # SQL Server nvarchar(n) 按 UTF-16 码元计数、最长 4000，即 2000 个字符，更长的无索引列改用 nvarchar(max)；
    # 达梦按 1000 个字符保守处理，更长的无索引列改用 text；PostgreSQL 系 varchar(n) 按字符计数，保持原样
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    PG_FAMILY = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor)
    SIZES = (1, 32, 255, 1000, 1001, 2000, 2001, 2040, 2047, 2048, 4000, 4001, 8000, 10240, 16380)
    # 有长度上限的目标数据库 -> (最多的字符数, 未超出时的类型, 超出时的类型)；{0} 为源长度，{1} 为源长度的 2 倍
    LIMITS = {
        OracleConvertor: (1000, "varchar2({0} char)", "clob"),
        SQLServerConvertor: (2000, "nvarchar({1})", "nvarchar(max)"),
        DM8Convertor: (1000, "varchar({0} char)", "text"),
    }

    # 每个长度一张表，避免行长度预算影响列类型；{0} 为长度。utf8mb4 的 varchar(16380) 仍在 MySQL 65535 字节的行长度内
    CAPACITY_TABLE = """
DROP TABLE IF EXISTS `demo_v{0}`;
CREATE TABLE `demo_v{0}`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `v{0}` varchar({0}) NULL DEFAULT NULL COMMENT '长度 {0}',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '容量表';
"""
    # 有普通索引、唯一索引、主键的列；源长度超过 MySQL 索引长度上限，使用前缀索引；{0} 为长度
    INDEX_TABLES = {
        "demo_idx": """
CREATE TABLE `demo_idx`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `code` varchar({0}) NULL DEFAULT NULL COMMENT '编码',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_code`(`code`(191) ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '普通索引表';
""",
        "demo_uk": """
CREATE TABLE `demo_uk`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `code` varchar({0}) NULL DEFAULT NULL COMMENT '编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`(191) ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '唯一索引表';
""",
        "demo_pk": """
CREATE TABLE `demo_pk`  (
  `code` varchar({0}) NOT NULL COMMENT '编码',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`code`(191)) USING BTREE
) ENGINE = InnoDB COMMENT = '主键表';
""",
    }
    # 三个 varchar(1000)：全部写满 4 字节字符时约 12000 字节，达梦 16K 页实测无法全部按行内保存；{0}、{1} 为索引定义
    WIDE_VALUE = "😀" * 1000
    WIDE_TABLE = """
CREATE TABLE `demo_wide`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `wide_a` varchar(1000) NULL DEFAULT NULL COMMENT 'a',
  `wide_b` varchar(1000) NULL DEFAULT NULL COMMENT 'b',
  `wide_c` varchar(1000) NULL DEFAULT NULL COMMENT 'c',
  PRIMARY KEY (`id`) USING BTREE{0}
) ENGINE = InnoDB COMMENT = '宽表';

INSERT INTO `demo_wide` (`id`, `wide_a`, `wide_b`, `wide_c`) VALUES (1, '{1}', '{1}', '{1}');
"""
    WIDE_INDEXES = """,
  INDEX `idx_a`(`wide_a`(191) ASC) USING BTREE,
  INDEX `idx_b`(`wide_b`(191) ASC) USING BTREE,
  INDEX `idx_c`(`wide_c`(191) ASC) USING BTREE"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    def expected_type(self, convertor_class, size):
        if convertor_class not in self.LIMITS:
            return f"varchar({size})"
        limit, inline_type, long_type = self.LIMITS[convertor_class]
        return inline_type.format(size, size * 2) if size <= limit else long_type

    def test_varchar_capacity(self):
        path = self.write_sql(HEADER + "".join(self.CAPACITY_TABLE.format(size) for size in self.SIZES))
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("None", out)
                for size in self.SIZES:
                    line = self.column_line(f"v{size}", out)
                    # 达梦的 text 不区分大小写
                    self.assertTrue(line.lower().startswith(self.expected_type(convertor_class, size) + " "),
                                    f"varchar({size}): {line}")

    def test_indexed_long_varchar(self):
        # 超出上限的列有普通索引、唯一索引、主键时，改用大对象类型会丢失约束：明确失败，提示目标数据库、表、列
        for convertor_class, (limit, _, _) in self.LIMITS.items():
            for table_name, table_sql in self.INDEX_TABLES.items():
                with self.subTest(f"{convertor_class.__name__} {table_name}"):
                    convertor = convertor_class(self.write_sql(HEADER + table_sql.format(limit + 1)))
                    with self.assertRaises(ValueError) as context:
                        convert(convertor)
                    message = str(context.exception)
                    for text in (convertor.db_type, table_name, "code"):
                        self.assertIn(text, message)
        # PostgreSQL 系没有长度上限：有索引的长 varchar 保持 varchar(n)
        for convertor_class in self.PG_FAMILY:
            for table_name in ("demo_idx", "demo_uk"):
                with self.subTest(f"{convertor_class.__name__} {table_name}"):
                    path = self.write_sql(HEADER + self.INDEX_TABLES[table_name].format(16380))
                    out, err = convert(convertor_class(path))
                    self.assertNotIn("无法正常解析", err)
                    self.assertTrue(self.column_line("code", out).startswith("varchar(16380) "))

    def test_dm_row_budget(self):
        # 达梦：三个 varchar(1000) 不能全部按行内的 varchar(1000 char) 输出，需改用 text 或明确失败
        path = self.write_sql(HEADER + self.WIDE_TABLE.format("", self.WIDE_VALUE))
        names = ("wide_a", "wide_b", "wide_c")
        try:
            out, err = convert(DM8Convertor(path))
        except ValueError as e:
            self.assertIn("demo_wide", str(e))
        else:
            self.assertNotIn("无法正常解析", err)
            types = [self.column_line(name, out).lower() for name in names]
            self.assertTrue([t for t in types if t.startswith("text ")], types)
            self.assertFalse([t for t in types if not t.startswith(("text ", "varchar(1000 char) "))], types)
            # 数据不截断
            self.assertEqual(3, out.count(f"'{self.WIDE_VALUE}'"))
        # 三个列均有索引：没有可改用 text 的列，不能删除索引，明确失败
        path = self.write_sql(HEADER + self.WIDE_TABLE.format(self.WIDE_INDEXES, "a"))
        with self.assertRaises(ValueError) as context:
            convert(DM8Convertor(path))
        self.assertIn("demo_wide", str(context.exception))
        # 其它目标数据库：每列未超出上限，保持行内类型
        path = self.write_sql(HEADER + self.WIDE_TABLE.format("", self.WIDE_VALUE))
        for convertor_class in self.CONVERTORS:
            if convertor_class is DM8Convertor:
                continue
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                for name in names:
                    self.assertTrue(self.column_line(name, out).startswith(
                        self.expected_type(convertor_class, 1000) + " "), name)


class StringLiteralConvertorTest(unittest.TestCase):
    # MySQL 默认 SQL 模式下的字符串，DEFAULT、INSERT 中只解码一次，再按目标数据库生成标准 SQL 字符串（单引号双写，反斜杠是普通字符）；
    # SQL Server 带 N 前缀
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    # \\ \' \" \n \r \t \b \Z 为转义；\% \_ 保留反斜杠；未知的 \q 即 q；'' 为一个单引号
    ESCAPE_SOURCE = r"'a\\b\'c\"d\ne\rf\tg\bh\Zi\%j\_k\ql''m'"
    ESCAPE_VALUE = "a\\b'c\"d\ne\rf\tg\bh\x1ai\\%j\\_kql'm"
    # 首列、重排、部分、省略的列清单，单行中的多行 VALUES，已有的 N、n 前缀；_utf8mb4''、_utf8'' 引导符与普通字符串相同，
    # 不支持的 x'' 等见 InsertLiteralInputTest
    ESCAPE_TABLE = r"""
DROP TABLE IF EXISTS `demo_escape`;
CREATE TABLE `demo_escape`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `a` varchar(64) NULL DEFAULT @S COMMENT '转义',
  `b` varchar(64) NULL DEFAULT NULL COMMENT '备注',
  `num` int NULL DEFAULT NULL COMMENT '数字',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '转义表';

BEGIN;
INSERT INTO `demo_escape` (`a`, `id`, `b`, `num`) VALUES (@S, 1, 'x', 1);
INSERT INTO `demo_escape` (`id`, `b`) VALUES (2, @S);
INSERT INTO `demo_escape` VALUES (3, @S, NULL, 3);
INSERT INTO `demo_escape` (`id`, `a`) VALUES (4, @S), (5, N'中文\'x'), (6, n'y');
INSERT INTO `demo_escape` (`id`, `b`, `a`) VALUES (7, _utf8mb4'k', _utf8 'z');
COMMIT;
"""

    MODE_TABLE = r"""
CREATE TABLE `demo_mode`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `a` varchar(64) NULL DEFAULT NULL COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '模式表';

INSERT INTO `demo_mode` (`id`, `a`) VALUES (1, 'a\nb');
INSERT INTO `demo_mode` (`id`, `a`) VALUES (2, 'SET SQL_MODE = NO_BACKSLASH_ESCAPES');
"""

    # 中文、emoji、JSON、真实的反斜杠、双写的引号、SQL 样式的内容
    UNICODE_TABLE = r"""
CREATE TABLE `demo_unicode`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT '中文😀' COMMENT '名称',
  `doc` varchar(255) NULL DEFAULT '{"k": "a\\\\b", "q": "it\'s"}' COMMENT 'JSON',
  `path` varchar(64) NULL DEFAULT 'C:\\dir\\' COMMENT '路径',
  `stmt` varchar(255) NULL DEFAULT 'SELECT ''a''; -- x /* y */' COMMENT 'SQL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '字符表';

INSERT INTO `demo_unicode` (`id`, `name`, `doc`, `path`, `stmt`) VALUES (1, '中文😀', '{"k": "a\\\\b", "q": "it\'s"}', 'C:\\dir\\', 'SELECT ''a''; -- x /* y */');
"""
    UNICODE_VALUES = {"name": "中文😀", "doc": '{"k": "a\\\\b", "q": "it\'s"}', "path": "C:\\dir\\",
                      "stmt": "SELECT 'a'; -- x /* y */"}

    # 文本列中不带引号的 TRUE、FALSE，与 MySQL 一致保存为文本 '1'、'0'；数值列为 1、0
    FLAG_TABLE = """
CREATE TABLE `demo_flag_text`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `flag_yes` varchar(8) NULL DEFAULT TRUE COMMENT '真',
  `flag_no` char(8) NULL DEFAULT FALSE COMMENT '假',
  `body` text NULL COMMENT '正文',
  `num` int NULL DEFAULT TRUE COMMENT '数字',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '文本布尔表';

INSERT INTO `demo_flag_text` (`id`, `flag_yes`, `flag_no`, `body`, `num`) VALUES (1, FALSE, TRUE, TRUE, FALSE), (2, 'TRUE', 'FALSE', 'true', 1);
"""

    # 字符串中的连续空行、单独一行的 GO、注释、分号是数据
    FORMAT_TABLE = r"""
CREATE TABLE `demo_format`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `content` text NULL COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '格式表';

INSERT INTO `demo_format` (`id`, `content`) VALUES (1, 'a\n\n\n\nb\nGO\n-- c;\n/* d */;\n\n\n'), (2, 'x');
"""
    FORMAT_VALUE = "a\n\n\n\nb\nGO\n-- c;\n/* d */;\n\n\n"

    # 默认值原始字符串 -> 期望的值：(列名, 源 SQL 中的字符串, 解码后的值)
    RAW_DEFAULTS = (
        # 实际的中文
        ("zh", "'中文'", "中文"),
        # 单个反斜杠：MySQL 未知转义 \u 即 u，得到普通文本 u4e2d
        ("esc_u", "'\\u4e2d'", "u4e2d"),
        # 两个反斜杠：真实的反斜杠，得到反斜杠、u、4、e、2、d 六个字符，不能变为“中”
        ("raw_u", r"'\\u4e2d'", "\\u4e2d"),
        # 等号、双写的引号
        ("eq", "'k = ''v'''", "k = 'v'"),
        # 与解析器内部占位样式相同的用户数据
        ("marker", "'pars_m_t pars_m_dq pars_m_n'", "pars_m_t pars_m_dq pars_m_n"),
    )

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    @staticmethod
    def prefix(convertor_class):
        return "N" if convertor_class is SQLServerConvertor else ""

    def test_escape_default_and_insert(self):
        path = self.write_sql(HEADER + self.ESCAPE_TABLE.replace("@S", self.ESCAPE_SOURCE))
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                value = sql_string(self.ESCAPE_VALUE, n)
                x, chinese, y = sql_string("x", n), sql_string("中文'x", n), sql_string("y", n)
                # 默认值、INSERT 共用同一次解码：a\\b 为 a\b，不能再次解码为退格
                self.assertIn(f"DEFAULT {value}", out)
                self.assertIn(f"VALUES ({value}, 1, {x}, 1)", out)
                self.assertIn(f"VALUES (2, {value})", out)
                self.assertIn(f"VALUES (3, {value}, NULL, 3)", out)
                # 已有的 N、n 前缀不重复
                self.assertIn(f"VALUES (4, {value}), (5, {chinese}), (6, {y})", out)
                for wrong in ("NN'", "Nn'", "nN'", "N'N'"):
                    self.assertNotIn(wrong, out)
                # utf8 字符集引导符只声明字符集，与普通字符串相同，不保留 MySQL 的引导符
                self.assertIn(f"VALUES (7, {sql_string('k', n)}, {sql_string('z', n)})", out)
                self.assertNotIn("_utf8", out)

    def test_nul(self):
        # NUL 无法在目标数据库的脚本中保持，明确失败，不丢弃数据
        table = r"""
CREATE TABLE `demo_nul`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `a` varchar(8) NULL DEFAULT {0} COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = 'NUL 表';

INSERT INTO `demo_nul` (`id`, `a`) VALUES (1, {1});
"""
        for default, value in (("NULL", r"'a\0b'"), (r"'a\0b'", "'ab'")):
            path = self.write_sql(HEADER + table.format(default, value))
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {default} {value}"):
                    with self.assertRaises(ValueError):
                        convert(convertor_class(path))

    def test_no_backslash_escapes(self):
        # 启用 NO_BACKSLASH_ESCAPES 后反斜杠不是转义符：明确失败，或按原文保留反斜杠
        for statement in ("SET SQL_MODE = 'NO_BACKSLASH_ESCAPES';",
                          "SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_BACKSLASH_ESCAPES';",
                          "/*!40101 SET SQL_MODE='NO_BACKSLASH_ESCAPES' */;",
                          "SET @@sql_mode = CONCAT(@@sql_mode, ',NO_BACKSLASH_ESCAPES');"):
            path = self.write_sql(HEADER + statement + "\n" + self.MODE_TABLE)
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {statement}"):
                    try:
                        out, err = convert(convertor_class(path))
                    except ValueError as e:
                        self.assertIn("NO_BACKSLASH_ESCAPES", str(e))
                    else:
                        self.assertNotIn("无法正常解析", err)
                        self.assertIn(sql_string("a\\nb", self.prefix(convertor_class)), out)
        # 注释、字符串中的同名文本，以及不含 NO_BACKSLASH_ESCAPES 的 SET 语句：按默认模式解码
        statements = """SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';
SET @note = 'NO_BACKSLASH_ESCAPES';
-- SET SQL_MODE = 'NO_BACKSLASH_ESCAPES';
# SET sql_mode = 'NO_BACKSLASH_ESCAPES';
/* SET sql_mode = 'NO_BACKSLASH_ESCAPES'; */
"""
        path = self.write_sql(HEADER + statements + self.MODE_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                self.assertIn(f"VALUES (1, {sql_string('a' + chr(10) + 'b', n)})", out)
                self.assertIn(f"VALUES (2, {sql_string('SET SQL_MODE = NO_BACKSLASH_ESCAPES', n)})", out)

    def test_sql_mode_assignments(self):
        # 1. 真正启用 NO_BACKSLASH_ESCAPES：SET 与变量之间的注释、多个赋值中的任一位置、大小写、可执行注释均不能绕过，明确失败
        for statement in ("SET/**/SQL_MODE='NO_BACKSLASH_ESCAPES';",
                          "SET /* a */ sql_mode /* b */ = /* c */ 'NO_BACKSLASH_ESCAPES';",
                          "SET -- x\nSQL_MODE = 'NO_BACKSLASH_ESCAPES';",
                          "SET# x\nsql_mode='NO_BACKSLASH_ESCAPES';",
                          "set @note = 'x', Sql_Mode = 'no_backslash_escapes';",
                          "SET @@SESSION.sql_mode = 'ANSI_QUOTES,NO_BACKSLASH_ESCAPES';",
                          "/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_BACKSLASH_ESCAPES' */;",
                          "/*!50003 set sql_mode = 'no_backslash_escapes' */;",
                          # 反引号引用的 sql_mode，作用域与变量名之间的空白、注释（已在 MySQL 中验证为合法并启用）
                          "SET `sql_mode`='NO_BACKSLASH_ESCAPES';",
                          "SET @@SESSION.`sql_mode`='NO_BACKSLASH_ESCAPES';",
                          "SET SESSION `sql_mode`='NO_BACKSLASH_ESCAPES';",
                          "SET @@SESSION . sql_mode='NO_BACKSLASH_ESCAPES';",
                          "SET @@SESSION/**/.sql_mode='NO_BACKSLASH_ESCAPES';",
                          "set @@session /* x */ . `SQL_MODE` = 'no_backslash_escapes';",
                          "SET session /* x */ `Sql_Mode` = 'NO_BACKSLASH_ESCAPES';",
                          "SET @note = 'x', `sql_mode` = 'NO_BACKSLASH_ESCAPES';",
                          "/*!40101 SET @@SESSION . `sql_mode` = 'NO_BACKSLASH_ESCAPES' */;"):
            path = self.write_sql(HEADER + statement + "\n" + self.MODE_TABLE)
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {statement!r}"):
                    with self.assertRaises(ValueError) as context:
                        convert(convertor_class(path))
                    self.assertIn("NO_BACKSLASH_ESCAPES", str(context.exception))
        # 2. 有效的 SET 语句没有启用 NO_BACKSLASH_ESCAPES：清空 sql_mode 的同时给用户变量赋同名文本（任意顺序）、
        # 从 sql_mode 中移除该模式；按默认模式解码，不能误报
        for statement in ("SET sql_mode='', @note='NO_BACKSLASH_ESCAPES';",
                          "SET @note = 'NO_BACKSLASH_ESCAPES', sql_mode = '';",
                          "set SQL_MODE = '' ,  @NOTE = 'no_backslash_escapes';",
                          "SET sql_mode=REPLACE(@@sql_mode,'NO_BACKSLASH_ESCAPES','');",
                          "SET SESSION sql_mode = REPLACE(@@SESSION.sql_mode, 'NO_BACKSLASH_ESCAPES', '');",
                          "set sql_mode = replace( @@sql_mode , 'no_backslash_escapes' , '' );",
                          # 反引号引用、带空白的 sql_mode 清空，其它变量为同名文本；从引用的 sql_mode 中移除该模式
                          "SET `sql_mode` = '', @note = 'NO_BACKSLASH_ESCAPES';",
                          "SET @@SESSION . `sql_mode` = '' , @`note` = 'NO_BACKSLASH_ESCAPES';",
                          "SET sql_mode = REPLACE(@@SESSION . `sql_mode`, 'NO_BACKSLASH_ESCAPES', '');",
                          # 用户变量 @sql_mode、@`sql_mode` 不是系统变量
                          "SET @sql_mode = 'NO_BACKSLASH_ESCAPES';",
                          "SET @`sql_mode` = 'NO_BACKSLASH_ESCAPES';"):
            path = self.write_sql(HEADER + statement + "\n" + self.MODE_TABLE)
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {statement!r}"):
                    out, err = convert(convertor_class(path))
                    self.assertNotIn("无法正常解析", err)
                    n = self.prefix(convertor_class)
                    self.assertIn(f"VALUES (1, {sql_string('a' + chr(10) + 'b', n)})", out)
                    self.assertIn(f"VALUES (2, {sql_string('SET SQL_MODE = NO_BACKSLASH_ESCAPES', n)})", out)

    def test_unicode_default_and_insert(self):
        # SQL Server：默认值、INSERT 的字符串均带 N 前缀，不带 N 时按排序规则的代码页保存，例如 1252 下中文变为 ??
        path = self.write_sql(HEADER + self.UNICODE_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                for name, value in self.UNICODE_VALUES.items():
                    self.assertIn(f"DEFAULT {sql_string(value, n)}", self.column_line(name, out), name)
                values = ", ".join(sql_string(value, n) for value in self.UNICODE_VALUES.values())
                self.assertIn(f"VALUES (1, {values})", out)
                if convertor_class is SQLServerConvertor:
                    self.assertNotIn("DEFAULT '", out)

    def test_default_raw_token(self):
        # 默认值按源 SQL 中的原始字符串只解码一次：实际中文、真实的反斜杠 u 文本、未知转义、解析器占位样式的正文互不混淆
        columns = ",\n".join(f"  `{name}` varchar(32) NULL DEFAULT {source} COMMENT '{name}'"
                             for name, source, _ in self.RAW_DEFAULTS)
        names = ", ".join(f"`{name}`" for name, _, _ in self.RAW_DEFAULTS)
        sources = ", ".join(source for _, source, _ in self.RAW_DEFAULTS)
        sql = HEADER + f"""
CREATE TABLE `demo_raw`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
{columns},
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '原始默认值表';

INSERT INTO `demo_raw` (`id`, {names}) VALUES (1, {sources});
"""
        self.assertEqual(6, len("\\u4e2d"))
        path = self.write_sql(sql)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                for name, _, value in self.RAW_DEFAULTS:
                    line = self.column_line(name, out)
                    self.assertIn(f"DEFAULT {sql_string(value, n)}", line, name)
                    if name in ("esc_u", "raw_u"):
                        self.assertNotIn("中", line, name)
                values = ", ".join(sql_string(value, n) for _, _, value in self.RAW_DEFAULTS)
                self.assertIn(f"VALUES (1, {values})", out)

    def test_text_boolean(self):
        path = self.write_sql(HEADER + self.FLAG_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                one, zero = sql_string("1", n), sql_string("0", n)
                self.assertRegex(self.column_line("flag_yes", out), rf"DEFAULT {re.escape(one)}(?:\s|$)")
                self.assertRegex(self.column_line("flag_no", out), rf"DEFAULT {re.escape(zero)}(?:\s|$)")
                self.assertRegex(self.column_line("num", out), r"DEFAULT 1(?:\s|$)")
                strings = ", ".join(sql_string(value, n) for value in ("TRUE", "FALSE", "true"))
                self.assertIn(f"VALUES (1, {zero}, {one}, {one}, 0), (2, {strings}, 1)", out)

    def test_output_format(self):
        # 整理空行时跳过字符串：字符串中的连续换行不压缩，GO、注释、分号保持为数据
        path = self.write_sql(HEADER + self.FORMAT_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = self.prefix(convertor_class)
                self.assertIn(f"INSERT INTO demo_format (id, content) VALUES (1, {sql_string(self.FORMAT_VALUE, n)}),"
                              f" (2, {sql_string('x', n)})", out)


class OracleTextDateTest(unittest.TestCase):
    # Oracle：只有 date 列的日期时间字符串转为 to_date，timestamp 列（含整秒）转为 TIMESTAMP 字面量；
    # 文本列、无法按列对应的整条语句中，日期时间格式的字符串保持为字符串
    DATE_TABLE = """
DROP TABLE IF EXISTS `demo_odate`;
CREATE TABLE `demo_odate`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `remark` varchar(64) NULL DEFAULT '2026-01-01 08:00:00' COMMENT '文本',
  `content` text NULL COMMENT '长文本',
  `dt` datetime NULL DEFAULT '2026-01-01 08:00:00' COMMENT '日期',
  `ts` timestamp NULL DEFAULT '2026-01-01 08:00:00' COMMENT '整秒',
  `ts3` datetime(3) NULL DEFAULT '2026-01-01 08:00:00.123' COMMENT '毫秒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '日期文本表';

BEGIN;
INSERT INTO `demo_odate` (`id`, `remark`, `content`, `dt`, `ts`, `ts3`) VALUES (1, '2026-01-01 08:00:00', '2026-01-01 08:00:00', '2026-01-01 08:00:00', '2026-01-01 08:00:00', '2026-01-01 08:00:00');
INSERT INTO `demo_odate` VALUES (2, '2026-01-01 08:00:00', NULL, '2026-01-01 08:00:00', NULL, '2026-01-01 08:00:00.5');
INSERT INTO `demo_odate` (`id`, `remark`) VALUES (3, CONCAT('2026-01-01 08:00:00', ''));
INSERT INTO `demo_odate` (`id`, `remark`) VALUES (4, /* 注释 */ '2026-01-01 08:00:00');
INSERT INTO `demo_odate` (`id`, `remark`, `content`) VALUES (5, "r", '2026-01-01 08:00:00');
INSERT INTO `demo_odate` (`id`, `dt`) VALUES (6, '2026-01-01 08:00:00.5'), (7);
COMMIT;
"""
    TEXT = "'2026-01-01 08:00:00'"
    TO_DATE = "to_date('2026-01-01 08:00:00', 'SYYYY-MM-DD HH24:MI:SS')"
    TIMESTAMP = "TIMESTAMP '2026-01-01 08:00:00'"

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def assert_line(self, line, out):
        self.assertRegex(out, rf"(?m)^{re.escape(line)},?$")

    def test_text_date(self):
        out, err = convert(OracleConvertor(self.write_sql(HEADER + self.DATE_TABLE)))
        self.assertNotIn("无法正常解析", err)
        # 1. 默认值
        self.assert_line(f"    remark varchar2(64 char) DEFAULT {self.TEXT} NULL", out)
        self.assert_line(f"    dt date DEFAULT {self.TO_DATE} NULL", out)
        self.assert_line(f"    ts timestamp(0) DEFAULT {self.TIMESTAMP} NULL", out)
        self.assert_line("    ts3 timestamp(3) DEFAULT TIMESTAMP '2026-01-01 08:00:00.123' NULL", out)
        # 2. 按列对应：文本列保持字符串，date 列 to_date，timestamp 列整秒同样为 TIMESTAMP 字面量
        self.assert_line("INSERT INTO demo_odate (id, remark, content, dt, ts, ts3) VALUES "
                         f"(1, {self.TEXT}, {self.TEXT}, {self.TO_DATE}, {self.TIMESTAMP}, {self.TIMESTAMP});", out)
        self.assert_line(f"INSERT INTO demo_odate VALUES (2, {self.TEXT}, NULL, {self.TO_DATE}, NULL,"
                         " TIMESTAMP '2026-01-01 08:00:00.5');", out)
        # 3. 函数参数、字符串外的注释、双引号：不能整句改为 to_date
        self.assert_line(f"INSERT INTO demo_odate (id, remark) VALUES (3, CONCAT({self.TEXT}, ''));", out)
        self.assert_line(f"INSERT INTO demo_odate (id, remark) VALUES (4, /* 注释 */ {self.TEXT});", out)
        self.assert_line(f"INSERT INTO demo_odate (id, remark, content) VALUES (5, \"r\", {self.TEXT});", out)
        # 4. 多行 VALUES 的后一行列数不一致：先完整验证结构，整句只按字符串处理；前一行 date 列的小数秒不能提前触发类型转换的异常
        self.assert_line("INSERT INTO demo_odate (id, dt) VALUES (6, '2026-01-01 08:00:00.5'), (7);", out)
        self.assertEqual(3, out.count("to_date("))

    def test_direct_gen_create_and_insert(self):
        # 不经过 print()，直接调用 gen_create、gen_insert；同一个表重新 gen_create 时按新的列类型刷新
        convertor = OracleConvertor(self.write_sql(HEADER + self.DATE_TABLE))
        ddl = DDLParser(convertor.table_script_list[0].replace("`", "")).run()[0]
        row = (f"VALUES (1, {self.TEXT}, {self.TEXT}, {self.TO_DATE}, {self.TIMESTAMP}, {self.TIMESTAMP});")
        convertor.gen_create(ddl)
        insert = convertor.gen_insert(ddl["table_name"])
        self.assertIn(row, insert)
        # remark 改为 datetime：按 date 列转为 to_date
        refreshed = copy.deepcopy(ddl)
        remark = next(column for column in refreshed["columns"] if column["name"].lower() == "remark")
        remark["type"], remark["size"], remark["default"] = "datetime", None, None
        convertor.gen_create(refreshed)
        self.assertIn(f"VALUES (1, {self.TO_DATE}, {self.TEXT}, {self.TO_DATE}, {self.TIMESTAMP}, {self.TIMESTAMP});",
                      convertor.gen_insert(ddl["table_name"]))
        # 恢复为文本列
        convertor.gen_create(ddl)
        self.assertEqual(insert, convertor.gen_insert(ddl["table_name"]))

    def test_other_convertors(self):
        # to_date、TIMESTAMP 字面量只用于 Oracle
        path = self.write_sql(HEADER + self.DATE_TABLE)
        for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("to_date(", out)
                self.assertNotIn("TIMESTAMP '", out)


    def test_empty_value_fallback(self):
        # 多行 VALUES 先完整验证全部行：任一行的首、中、末字段为空或只有空白时，整条语句按坏结构只处理字符串；
        # 前一行 date 列的小数秒不能提前触发类型转换的异常。只验证回退，不表示这些语句在 MySQL 中合法
        table = """
CREATE TABLE `demo_oempty`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `remark` varchar(64) NULL DEFAULT NULL COMMENT '文本',
  `dt` datetime NULL DEFAULT NULL COMMENT '日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '空字段表';

"""
        # (列清单, VALUES)：后一行末尾、中间、首个字段为空或只有空白
        values = (
            ("id, dt", "(1, '2026-01-01 08:00:00.5'), (2,)"),
            ("id, dt", "(1, '2026-01-01 08:00:00.5'), (2, )"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (2, 'r',)"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (2, 'r', )"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (2, , NULL)"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (, 'r', NULL)"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (  , 'r', NULL)"),
            ("id, remark, dt", "(1, 'r', '2026-01-01 08:00:00.5'), (2, 'r', NULL), (3,  , NULL)"),
        )
        for columns, value in values:
            source_columns = ", ".join(f"`{name}`" for name in columns.split(", "))
            path = self.write_sql(HEADER + table
                                  + f"INSERT INTO `demo_oempty` ({source_columns}) VALUES {value};\n")
            with self.subTest(value):
                out, err = convert(OracleConvertor(path))
                self.assertNotIn("无法正常解析", err)
                self.assert_line(f"INSERT INTO demo_oempty ({columns}) VALUES {value};", out)
                self.assertNotIn("to_date(", out)
                self.assertNotIn("TIMESTAMP '2026", out)
            # 其它目标数据库同样整句回退，不抛异常；SQL Server 的字符串带 N 前缀
            for convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                                    SQLServerConvertor, DM8Convertor):
                with self.subTest(f"{convertor_class.__name__} {value}"):
                    out, err = convert(convertor_class(path))
                    self.assertNotIn("无法正常解析", err)
                    n = "N" if convertor_class is SQLServerConvertor else ""
                    self.assertIn(value.rsplit("), ", 1)[1].replace("'r'", f"{n}'r'"), out)
        # 对照：结构有效时，date 列的值仍正常转换，带小数秒仍明确失败
        valid = HEADER + table + ("INSERT INTO `demo_oempty` (`id`, `remark`, `dt`) VALUES "
                                  "(1, 'r', '2026-01-01 08:00:00'), (2, '', NULL);\n")
        out, err = convert(OracleConvertor(self.write_sql(valid)))
        self.assertNotIn("无法正常解析", err)
        self.assert_line(f"INSERT INTO demo_oempty (id, remark, dt) VALUES (1, 'r', {self.TO_DATE}), (2, '', NULL);", out)
        for value in ("(1, 'r', '2026-01-01 08:00:00.5'), (2, 'r', NULL)",
                      "(1, 'r', NULL), (2, 'r', '2026-01-01 08:00:00.5')"):
            path = self.write_sql(HEADER + table
                                  + f"INSERT INTO `demo_oempty` (`id`, `remark`, `dt`) VALUES {value};\n")
            with self.subTest(f"valid {value}"):
                with self.assertRaises(ValueError):
                    convert(OracleConvertor(path))

    def test_national_date(self):
        # N、n 前缀的日期时间字符串：只解码一次，date 列为 to_date，timestamp 列（整秒、小数秒）为 TIMESTAMP 字面量，
        # 文本列保持字符串；Oracle 不保留 N 前缀
        sql = HEADER + """
CREATE TABLE `demo_onational`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `remark` varchar(64) NULL DEFAULT N'2026-01-01 08:00:00' COMMENT '文本',
  `dt` datetime NULL DEFAULT n'2026-01-01 08:00:00' COMMENT '日期',
  `ts` timestamp NULL DEFAULT N'2026-01-01 08:00:00' COMMENT '整秒',
  `ts3` datetime(3) NULL DEFAULT n'2026-01-01 08:00:00.123' COMMENT '毫秒',
  `note` varchar(64) NULL DEFAULT N'x' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '国家字符日期表';

BEGIN;
INSERT INTO `demo_onational` (`ts3`, `id`, `dt`, `remark`, `ts`, `note`) VALUES (N'2026-01-01 08:00:00.5', 1, n'2026-01-01 08:00:00', N'2026-01-01 08:00:00', '2026-01-01 08:00:00', n'it''s'), (n'2026-01-01 08:00:00.25', 2, '2026-01-01 08:00:00', n'2026-01-01 08:00:00', N'2026-01-01 08:00:00', 'y');
INSERT INTO `demo_onational` VALUES (3, N'2026-01-01 08:00:00', N'2026-01-01 08:00:00', n'2026-01-01 08:00:00', N'2026-01-01 08:00:00.123', N'z');
COMMIT;
"""
        out, err = convert(OracleConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        # 1. 默认值
        self.assert_line(f"    remark varchar2(64 char) DEFAULT {self.TEXT} NULL", out)
        self.assert_line(f"    dt date DEFAULT {self.TO_DATE} NULL", out)
        self.assert_line(f"    ts timestamp(0) DEFAULT {self.TIMESTAMP} NULL", out)
        self.assert_line("    ts3 timestamp(3) DEFAULT TIMESTAMP '2026-01-01 08:00:00.123' NULL", out)
        self.assert_line("    note varchar2(64 char) DEFAULT 'x' NULL", out)
        # 2. INSERT：重排的列清单、多行 VALUES、省略列清单，与不带前缀的普通字符串混合
        self.assert_line("INSERT INTO demo_onational (ts3, id, dt, remark, ts, note) VALUES "
                         f"(TIMESTAMP '2026-01-01 08:00:00.5', 1, {self.TO_DATE}, {self.TEXT}, {self.TIMESTAMP}, 'it''s'), "
                         f"(TIMESTAMP '2026-01-01 08:00:00.25', 2, {self.TO_DATE}, {self.TEXT}, {self.TIMESTAMP}, 'y');",
                         out)
        self.assert_line(f"INSERT INTO demo_onational VALUES (3, {self.TEXT}, {self.TO_DATE}, {self.TIMESTAMP},"
                         " TIMESTAMP '2026-01-01 08:00:00.123', 'z');", out)
        for wrong in ("N'2026", "n'2026", "TIMESTAMP N", "TIMESTAMP n", "to_date(N", "to_date(n", "TIMESTAMP TIMESTAMP",
                      "to_date(to_date"):
            self.assertNotIn(wrong, out)
        # 3. date 列带小数秒：默认值、INSERT 均按既有精度策略明确失败
        table = """
CREATE TABLE `demo_onational_frac`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `dt` datetime NULL DEFAULT {0} COMMENT '日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '小数秒表';

INSERT INTO `demo_onational_frac` (`id`, `dt`) VALUES (1, {1});
"""
        for default, value in (("N'2026-01-01 08:00:00.5'", "NULL"), ("NULL", "n'2026-01-01 08:00:00.5'"),
                               ("NULL", "N'2026-01-01 08:00:00.5'")):
            with self.subTest(f"{default} {value}"):
                with self.assertRaises(ValueError):
                    convert(OracleConvertor(self.write_sql(HEADER + table.format(default, value))))


class OracleLongLiteralTest(unittest.TestCase):
    # Oracle 字符串字面量最长 4000 字节；clob 列的更长的值按 UTF-8 字符边界切成小块，每块 TO_CLOB 后拼接，
    # 不能先按 varchar2 拼接（中间结果超过 4000 字节）
    MAX_LITERAL_BYTES = 4000
    STRING = r"'(?:''|[^'])*'"
    CLOB_CONCAT_PATTERN = re.compile(
        rf"\(?\s*TO_CLOB\({STRING}\)(?:\s*\|\|\s*TO_CLOB\({STRING}\))*\s*\)?", flags=re.IGNORECASE)
    # id -> 写入 longtext 列的值：4000、4001、8000、40000 字节，emoji 跨块，连续的引号，末尾的反斜杠
    CONTENT_VALUES = {
        1: "a" * 4000,
        2: "a" * 4001,
        3: "e" * 8000,
        4: "c" * 40000,
        5: "a" + "😀" * 2000,
        6: "'" * 4500,
        7: "d" * 4100 + "\\",
        8: "中" * 1400 + "'😀\\",
    }
    # id -> 写入 varchar(2000) 列（Oracle 为 clob）的值
    BODY_VALUES = {101: "😀" * 2000, 102: "中" * 1400 + "'😀\\"}
    # 超过 4000 字节的默认值：1302 个字符，4202 字节
    MEMO_DEFAULT = "中" * 1000 + "😀" * 300 + "'\\"

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    @staticmethod
    def mysql_string(value):
        # 生成源 SQL 中的 MySQL 字符串
        return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"

    def clob_value(self, literal):
        """Oracle 字面量 -> 字符串的值：单个标准字符串，或 TO_CLOB 拼接的标准字符串；每块不超过 4000 字节"""
        literal = literal.strip()
        if not re.fullmatch(self.STRING, literal):
            self.assertRegex(literal[:200], r"(?i)^\(?\s*TO_CLOB\(")
            self.assertTrue(self.CLOB_CONCAT_PATTERN.fullmatch(literal), literal[:200])
        chunks = [chunk[1:-1].replace("''", "'") for chunk in re.findall(self.STRING, literal)]
        for chunk in chunks:
            self.assertLessEqual(len(chunk.encode("utf-8")), self.MAX_LITERAL_BYTES)
        return "".join(chunks)

    def test_long_insert(self):
        inserts = [f"INSERT INTO `demo_clob` (`id`, `content`) VALUES ({id}, {self.mysql_string(value)});"
                   for id, value in self.CONTENT_VALUES.items()]
        inserts += [f"INSERT INTO `demo_clob` (`id`, `body`) VALUES ({id}, {self.mysql_string(value)});"
                    for id, value in self.BODY_VALUES.items()]
        sql = HEADER + """
CREATE TABLE `demo_clob`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `content` longtext NULL COMMENT '内容',
  `body` varchar(2000) NULL DEFAULT NULL COMMENT '长 varchar',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '大文本表';

BEGIN;
""" + "\n".join(inserts) + "\nCOMMIT;\n"
        out, err = convert(OracleConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        self.assertRegex(out, r"(?m)^    content clob ")
        self.assertRegex(out, r"(?m)^    body clob ")
        for column, values in (("content", self.CONTENT_VALUES), ("body", self.BODY_VALUES)):
            for id, value in values.items():
                with self.subTest(f"{column} {id}"):
                    match = re.search(rf"(?m)^INSERT INTO demo_clob \(id, {column}\) VALUES \({id}, (.*?)\);$", out)
                    self.assertTrue(match)
                    self.assertEqual(value, self.clob_value(match.group(1)))

    def test_long_default(self):
        sql = HEADER + f"""
CREATE TABLE `demo_clob_default`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `memo` varchar(2000) NULL DEFAULT {self.mysql_string(self.MEMO_DEFAULT)} COMMENT '长默认值',
  `short_memo` varchar(2000) NULL DEFAULT '中文' COMMENT '短默认值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '大文本默认值表';
"""
        out, err = convert(OracleConvertor(self.write_sql(sql)))
        self.assertNotIn("无法正常解析", err)
        match = re.search(r"(?m)^    memo clob DEFAULT (.*) NULL,?$", out)
        self.assertTrue(match)
        self.assertEqual(self.MEMO_DEFAULT, self.clob_value(match.group(1)))
        self.assertRegex(out, r"(?m)^    short_memo clob DEFAULT '中文' NULL,?$")


class InsertLiteralInputTest(unittest.TestCase):
    # 正常的 MySQL 源 SQL：INSERT 按完整语句（而不是物理行）识别，字符串中的换行、分号、SQL 样式的文本都是数据；
    # utf8 字符集引导符与普通字符串相同；二进制、其它字符集、NUL 明确失败，不输出部分转换的脚本
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    INPUT_TABLE = """
CREATE TABLE `demo_input`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(255) NULL DEFAULT NULL COMMENT '名称',
  `flag` bit(1) NULL DEFAULT NULL COMMENT '标记',
  `num` int NULL DEFAULT NULL COMMENT '数值',
  `at` datetime NULL DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '输入表';
"""
    # 1. 开头的空白、注释，小写关键字，INSERT、INTO、表名、列清单、VALUES、各行之间换行，重排的列清单，多行 VALUES
    # 2. 省略列清单、VALUES 后没有空格，字符串中的真实换行、双写的引号、反斜杠转义，分号后的行尾注释
    # 3. 字符串中的真实换行后是同一张表的 INSERT 文本，以及 clean 会改写的 ENGINE、COLLATE、KEY、前缀长度、b'0' 文本；
    #    文件末尾的语句缺少分号（mysql 客户端同样执行）
    # 注释中的 INSERT 不是语句
    MULTILINE_INSERTS = r"""  /* 前导注释; INSERT INTO `demo_input` VALUES (8, 'ghost'); */ insert
into `demo_input`
(`name`, `id`,
 `flag`, `num`, `at`)
values
(_utf8mb4'多行
第二行', 1, b'1', TRUE, '2026-01-01 08:00:00'),
  (N'x;y', 2, FALSE, FALSE, NULL);
-- 行注释; INSERT INTO `demo_input` VALUES (8, 'ghost');
INSERT INTO `demo_input` VALUES(3, 'it''s
\'q\' C:\\', b'0', 3, NULL); # 行尾注释
INSERT INTO `demo_input` (`id`, `name`) VALUES (4, 'l1
INSERT INTO `demo_input` (`id`, `name`) VALUES (9, ''ghost'');
x) ENGINE=InnoDB; COLLATE utf8mb4_bin KEY `k`(10) b''0''')
"""
    MULTILINE_VALUES = ("多行\n第二行", "x;y", "it's\n'q' C:\\",
                        "l1\nINSERT INTO `demo_input` (`id`, `name`) VALUES (9, 'ghost');\n"
                        "x) ENGINE=InnoDB; COLLATE utf8mb4_bin KEY `k`(10) b'0'")
    TO_DATE = "to_date('2026-01-01 08:00:00', 'SYYYY-MM-DD HH24:MI:SS')"

    # 默认值、INSERT 中的 utf8 字符集引导符（不区分大小写，与字符串之间可有空白），括号中的字符串表达式默认值
    INTRODUCER_TABLE = r"""
CREATE TABLE `demo_intro`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL DEFAULT _utf8mb4'默认' COMMENT '名称',
  `code` varchar(64) NULL DEFAULT _UTF8 'c' COMMENT '编码',
  `body` text NULL DEFAULT (_utf8mb4'正文\'') COMMENT '正文',
  `memo` varchar(64) NULL DEFAULT ('备注') COMMENT '备注',
  `at` datetime NULL DEFAULT _utf8mb4'2026-01-01 08:00:00' COMMENT '时间',
  `ts` datetime(3) NULL DEFAULT NULL COMMENT '毫秒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '引导符表';

INSERT INTO `demo_intro` (`id`, `name`, `code`, `body`, `at`, `ts`) VALUES (1, _utf8mb4'中文😀', _utf8'it''s', _utf8mb3 'x\'y', _utf8mb4'2026-01-01 08:00:00', _UTF8MB4'2026-01-01 08:00:00.123'), (2, CONCAT(_utf8mb4'a', 'b'), N'n', NULL, NULL, NULL);
"""

    # 无法保持含义的字符串：(INSERT 中的值, 异常信息)；值写入 varchar 列
    UNSUPPORTED_VALUES = (
        ("_binary'x'", "_binary"),
        ("_BINARY 'x'", "_BINARY"),
        ("_latin1'x'", "_latin1"),
        ("_gbk 'x'", "_gbk"),
        ("x'41'", "十六进制"),
        ("X'41'", "十六进制"),
        ("b'101'", "位值"),
        ("_utf8mb3'😀'", "增补字符"),
        ("_utf8 '😀'", "增补字符"),
        ("N'😀'", "增补字符"),
        (r"'a\0b'", "NUL"),
        ("'a\x00b'", "NUL"),
    )
    # 无法保持含义的字符串默认值：(默认值, 异常信息)
    UNSUPPORTED_DEFAULTS = (
        ("_binary'x'", "_binary"),
        ("(_latin1'x')", "_latin1"),
        ("x'41'", "十六进制"),
        ("N'😀'", "增补字符"),
    )

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        # newline=""：按原样写入 \r\n、\r
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    @staticmethod
    def prefix(convertor_class):
        return "N" if convertor_class is SQLServerConvertor else ""

    @staticmethod
    def terminator(convertor_class):
        return "\nGO" if convertor_class is SQLServerConvertor else ";"

    def assert_rejected(self, convertor_class, path, message):
        # 明确失败，且不输出部分转换的脚本（前面已转换的表同样不输出）
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            with self.assertRaises(ValueError) as context:
                convertor_class(path).print()
        self.assertIn(message, str(context.exception))
        self.assertEqual("", out.getvalue())

    def test_multiline_statements(self):
        path = self.write_sql(HEADER + self.INPUT_TABLE + self.MULTILINE_INSERTS)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n, end = self.prefix(convertor_class), self.terminator(convertor_class)
                first, second, third, fourth = (sql_string(value, n) for value in self.MULTILINE_VALUES)
                one, zero = sql_string("1", n), sql_string("0", n)
                at = self.TO_DATE if convertor_class is OracleConvertor else sql_string("2026-01-01 08:00:00", n)
                false = convertor_class.boolean_bit_literals[1]
                # 字符串只解码一次：真实换行、引号、反斜杠、分号、SQL 样式的文本均保持；列清单的换行统一为空格，行之间的换行保持
                self.assertIn(f"INSERT INTO demo_input (name, id, flag, num, at) VALUES ({first}, 1, {one}, 1, {at}),\n"
                              f"  ({second}, 2, {false}, 0, NULL){end}\n", out)
                # 省略的列清单：SQL Server、达梦按建表的列顺序补充（见 InsertImplicitColumnTest），其它目标数据库保持省略
                columns = " (id, name, flag, num, at)" if convertor_class in (SQLServerConvertor, DM8Convertor) else ""
                self.assertIn(f"INSERT INTO demo_input{columns} VALUES (3, {third}, {zero}, 3, NULL){end}\n", out)
                self.assertIn(f"INSERT INTO demo_input (id, name) VALUES (4, {fourth}){end}\n", out)
                # 只有 3 条语句、4 行数据：字符串中的 INSERT 文本、注释中的 INSERT 不是语句
                self.assertEqual(3, len(re.findall(r"(?m)^INSERT INTO demo_input ", out)))
                self.assertEqual(1, out.count("(9, "))
                self.assertNotIn("(8, ", out)
                # 注释不输出；引导符、b'1' 不保留 MySQL 写法（第 4 行数据中的 b''0'' 是文本）
                for wrong in ("注释", "_utf8mb4", "b'1'"):
                    self.assertNotIn(wrong, out)
                if convertor_class in (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor):
                    self.assertIn("CREATE SEQUENCE demo_input_seq\n    START 5;", out)
                if convertor_class is OracleConvertor:
                    self.assertIn("CREATE SEQUENCE demo_input_seq\n    START WITH 5;", out)

    def test_crlf_and_carriage_return(self):
        # 与 mysql 客户端一致：行尾的 \r\n 为换行（字符串中的 \r\n 保存为 \n），单独的 \r 是数据
        sql = (HEADER + self.INPUT_TABLE
               + "INSERT INTO `demo_input` (`id`, `name`) VALUES (1, 'a\nb'),\n(2, 'c{CR}d');\n"
               + "INSERT INTO `demo_input` (`id`, `name`) VALUES (3, 'e{CR}\nf');\n")
        path = self.write_sql(sql.replace("\n", "\r\n").replace("{CR}", "\r"))
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n, end = self.prefix(convertor_class), self.terminator(convertor_class)
                first, second, third = sql_string("a\nb", n), sql_string("c\rd", n), sql_string("e\r\nf", n)
                self.assertIn(f"INSERT INTO demo_input (id, name) VALUES (1, {first}),\n(2, {second}){end}\n", out)
                # 字符串中的 \r\r\n：mysql 客户端只去掉行尾的一个 \r
                self.assertIn(f"INSERT INTO demo_input (id, name) VALUES (3, {third}){end}\n", out)
                self.assertNotIn("Table structure for demo_input\r", out)

    def test_utf8_introducers(self):
        path = self.write_sql(HEADER + self.INTRODUCER_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n, end = self.prefix(convertor_class), self.terminator(convertor_class)
                oracle = convertor_class is OracleConvertor
                at = self.TO_DATE if oracle else sql_string("2026-01-01 08:00:00", n)
                ts = "TIMESTAMP '2026-01-01 08:00:00.123'" if oracle else sql_string("2026-01-01 08:00:00.123", n)
                # 1. 默认值：引导符、括号中的字符串表达式默认值，与普通字符串默认值相同
                for name, value in (("name", "默认"), ("code", "c"), ("body", "正文'"), ("memo", "备注")):
                    self.assertIn(f"DEFAULT {sql_string(value, n)}", self.column_line(name, out), name)
                self.assertIn(f"DEFAULT {at}", self.column_line("at", out))
                # 2. INSERT：引导符的字符串与普通字符串相同，日期时间按列类型转换；表达式中的字符串同样转换
                values = ", ".join(sql_string(value, n) for value in ("中文😀", "it's", "x'y"))
                self.assertIn(f"INSERT INTO demo_intro (id, name, code, body, at, ts) VALUES (1, {values}, {at}, {ts}),"
                              f" (2, CONCAT({sql_string('a', n)}, {sql_string('b', n)}), {sql_string('n', n)},"
                              f" NULL, NULL, NULL){end}\n", out)
                for wrong in ("_utf8", "_UTF8", "DEFAULT DEFAULT", "\\u"):
                    self.assertNotIn(wrong, out)

    def test_unsupported_literals(self):
        # 前面能转换的表也不输出；同一条 INSERT 中前一行能转换时同样整体失败
        table = """
CREATE TABLE `demo_bad`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT {0} COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '不支持表';

INSERT INTO `demo_bad` (`id`, `name`) VALUES {1};
"""
        cases = [("NULL", f"(1, {value})", message) for value, message in self.UNSUPPORTED_VALUES]
        cases += [("NULL", f"(1, 'ok'), (2, {value})", message) for value, message in self.UNSUPPORTED_VALUES[:1]]
        cases += [(default, "(1, 'ok')", message) for default, message in self.UNSUPPORTED_DEFAULTS]
        for default, values, message in cases:
            path = self.write_sql(HEADER + self.INPUT_TABLE + table.format(default, values))
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {default} {values!r}"):
                    self.assert_rejected(convertor_class, path, message)

    def test_unsupported_insert_form(self):
        # 不是 INSERT INTO `表名` (列清单) VALUES 的语句明确失败，不默默丢弃数据
        for statement in ("INSERT INTO `demo_input` SET `id` = 1, `name` = 'x';",
                          "INSERT INTO `demo_input` (`id`, `name`) SELECT 1, 'x';"):
            path = self.write_sql(HEADER + self.INPUT_TABLE + statement + "\n")
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {statement}"):
                    self.assert_rejected(convertor_class, path, "不支持表 demo_input 的 INSERT 语句")


class InsertSequenceStartTest(unittest.TestCase):
    # PostgreSQL 系、Oracle 的序列起始值：全部 INSERT 中全部行的 id 的最大值加 1，按列清单对应 id，与行、语句的顺序无关；
    # 原来只取最后一条 INSERT 第一个值，多行 VALUES、乱序、重排的列清单时偏小，导致后续新增的 id 与已有数据冲突
    SEQUENCE_CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor, OracleConvertor)
    SEQ_TABLE = """
CREATE TABLE `demo_seq`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  `num` int NULL DEFAULT NULL COMMENT '数值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '序列表';

"""
    # (说明, INSERT 语句, 期望的起始值)；注释中为原来的起始值
    CASES = (
        # 原来为 3
        ("最后一条为多行", "INSERT INTO `demo_seq` (`id`, `name`) VALUES (1, 'a');\n"
                     "INSERT INTO `demo_seq` (`id`, `name`) VALUES (2, 'b'), (7, 'c');", 8),
        # 原来为 6
        ("行的 id 乱序", "INSERT INTO `demo_seq` (`id`, `name`) VALUES (5, 'a'), (9, 'b'), (2, 'c');", 10),
        # 原来为 4
        ("前面的语句 id 更大", "INSERT INTO `demo_seq` (`id`, `name`) VALUES (10, 'a');\n"
                       "INSERT INTO `demo_seq` (`id`, `name`) VALUES (3, 'b');", 11),
        # 原来为 1（首个值为字符串）
        ("重排的列清单", "INSERT INTO `demo_seq` (`name`, `id`) VALUES ('a', 12), ('b', 4);", 13),
        # 原来为 501（首个值为其它整数列）
        ("其它整数列在前", "INSERT INTO `demo_seq` (`num`, `id`, `name`) VALUES (500, 6, 'a');", 7),
        # 省略列清单：按建表的列顺序对应
        ("省略列清单", "INSERT INTO `demo_seq` VALUES (20, 'a', 300), (15, 'b', 400);", 21),
        # 字符串中 id 样式的文本、跨行的多行 VALUES 不影响
        ("字符串中的假 id", "INSERT INTO `demo_seq` (`id`, `name`) VALUES (3, '), (99, x'),\n"
                      "(4, 'VALUES (100, ''y'''), (2, '(999, ');", 5),
        # 没有数据
        ("没有 INSERT", "", 1),
    )
    # id 不是整数字面量，或列清单中没有 id 列：MySQL 由 AUTO_INCREMENT 生成或按表达式计算，无法确定起始值、
    # 目标数据库无法保持相同的 id，明确失败
    UNSUPPORTED_IDS = (
        ("INSERT INTO `demo_seq` (`id`, `name`) VALUES (1, 'a'), (NULL, 'b');", "id 不是整数字面量"),
        ("INSERT INTO `demo_seq` VALUES (NULL, 'a', 1);", "id 不是整数字面量"),
        ("INSERT INTO `demo_seq` (`id`, `name`) VALUES (DEFAULT, 'a');", "id 不是整数字面量"),
        ("INSERT INTO `demo_seq` (`id`, `name`) VALUES (1 + 1, 'a');", "id 不是整数字面量"),
        ("INSERT INTO `demo_seq` (`id`, `name`) VALUES ('5', 'a');", "id 不是整数字面量"),
        ("INSERT INTO `demo_seq` (`name`, `num`) VALUES ('a', 1);", "没有 id 列"),
    )

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    @staticmethod
    def sequence(convertor_class, start):
        if convertor_class is OracleConvertor:
            return f"CREATE SEQUENCE demo_seq_seq\n    START WITH {start};"
        return f"CREATE SEQUENCE demo_seq_seq\n    START {start};"

    def test_sequence_start(self):
        for name, inserts, start in self.CASES:
            path = self.write_sql(HEADER + self.SEQ_TABLE + inserts + "\n")
            for convertor_class in self.SEQUENCE_CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {name}"):
                    out, err = convert(convertor_class(path))
                    self.assertNotIn("无法正常解析", err)
                    self.assertNotIn("序列起始值", err)
                    self.assertIn(self.sequence(convertor_class, start), out)

    def test_unsupported_id(self):
        for inserts, message in self.UNSUPPORTED_IDS:
            path = self.write_sql(HEADER + self.SEQ_TABLE + inserts + "\n")
            for convertor_class in self.SEQUENCE_CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {inserts}"):
                    out, err = io.StringIO(), io.StringIO()
                    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
                        with self.assertRaises(ValueError) as context:
                            convertor_class(path).print()
                    self.assertIn(message, str(context.exception))
                    self.assertIn("demo_seq", str(context.exception))
                    # 不输出部分转换的脚本
                    self.assertEqual("", out.getvalue())
            # SQL Server、达梦不生成序列，但同样以 IDENTITY_INSERT 保持原 id 导入：原来 INSERT 原样输出，
            # NULL、DEFAULT 等导入失败，缺少 id 时由目标数据库重新编号，改为同样明确失败，不输出部分转换的脚本
            for convertor_class in (SQLServerConvertor, DM8Convertor):
                with self.subTest(f"{convertor_class.__name__} {inserts}"):
                    out, err = io.StringIO(), io.StringIO()
                    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
                        with self.assertRaises(ValueError) as context:
                            convertor_class(path).print()
                    self.assertIn(message, str(context.exception))
                    self.assertIn("demo_seq", str(context.exception))
                    self.assertEqual("", out.getvalue())

    def test_fallback_statement(self):
        # 无法按列对应的语句（值中有注释）整句回退，其中的 id 不计入起始值，在 stderr 提示；其它语句照常计入
        inserts = ("INSERT INTO `demo_seq` (`id`, `name`) VALUES (50 /* x */, 'a');\n"
                   "INSERT INTO `demo_seq` (`id`, `name`) VALUES (2, 'b');\n")
        path = self.write_sql(HEADER + self.SEQ_TABLE + inserts)
        for convertor_class in self.SEQUENCE_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertIn("表 demo_seq 的 INSERT 语句无法按列对应，序列起始值未计入", err)
                self.assertIn("VALUES (50 /* x */, ", out)
                self.assertIn(self.sequence(convertor_class, 3), out)

    def test_string_id(self):
        # id 不是数值列（例如字符串编号）：序列不对应 id，起始值为 1，不校验 id 的值；SQL Server、达梦同样不校验
        sql = HEADER + """
CREATE TABLE `demo_seq`  (
  `id` varchar(32) NOT NULL COMMENT '编号',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '序列表';

INSERT INTO `demo_seq` (`id`, `name`) VALUES ('a1', 'a'), ('100', 'b');
"""
        path = self.write_sql(sql)
        for convertor_class in self.SEQUENCE_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("序列起始值", err)
                self.assertIn(self.sequence(convertor_class, 1), out)
        # 只验证不套用整数 id 的校验；SQL Server、达梦建表时 id 列固定为 bigint IDENTITY，是另外的已有问题
        for convertor_class in (SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("未校验", err)
                self.assertIn("INSERT INTO demo_seq (id, name) VALUES (", out)

    def test_identity_fallback_statement(self):
        # SQL Server、达梦：无法按列对应的语句（值中有注释）整句回退，其中的 id 不校验，在 stderr 提示；其它语句照常校验
        inserts = ("INSERT INTO `demo_seq` (`id`, `name`) VALUES (50 /* x */, 'a');\n"
                   "INSERT INTO `demo_seq` (`id`, `name`) VALUES (2, 'b');\n")
        path = self.write_sql(HEADER + self.SEQ_TABLE + inserts)
        for convertor_class in (SQLServerConvertor, DM8Convertor):
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertIn("表 demo_seq 的 INSERT 语句无法按列对应，未校验其中的 id", err)
                self.assertIn("VALUES (50 /* x */, ", out)
                self.assertIn("VALUES (2, ", out)


class InsertImplicitColumnTest(unittest.TestCase):
    # 省略列清单的 INSERT 在 MySQL 中按建表的列顺序对应。SQL Server、达梦只有指定列清单（且 IDENTITY_INSERT 为 ON）时
    # 才能为自增列 id 赋值，原来原样输出 INSERT INTO 表 VALUES，导入时失败：按建表的列顺序补充列清单，值保持原样；
    # 其它目标数据库可以省略列清单，输出保持不变
    IDENTITY_CONVERTORS = (SQLServerConvertor, DM8Convertor)
    OTHER_CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor, OracleConvertor)
    # 建表的列顺序中 id 不在首位
    TABLE = """
CREATE TABLE `demo_implicit`  (
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `num` int NULL DEFAULT NULL COMMENT '数值',
  `note` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '省略列清单表';

"""
    # 1. 省略列清单的多行 VALUES，id 乱序；字符串中的双写引号、反斜杠转义、中文与增补字符，以及列清单样式的文本
    # 2. 省略列清单，INSERT、表名、VALUES、各行之间的换行与空白，字符串中的真实换行
    # 3. 显式的列清单保持原样
    INSERTS = r"""INSERT INTO `demo_implicit` VALUES ('a', 1, 1, 'it''s 中文😀'), ('b\'c', 9, 2, '(name, id) VALUES (x');
INSERT   INTO `demo_implicit`
VALUES
  ('c',
 4, 3, 'l1
l2'),
  ('d', 2, NULL, NULL);
INSERT INTO `demo_implicit` (`id`, `name`) VALUES (5, 'e');
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_identity_insert(self):
        path = self.write_sql(HEADER + self.TABLE + self.INSERTS)
        for convertor_class in self.IDENTITY_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                sqlserver = convertor_class is SQLServerConvertor
                n, end = ("N", "\nGO") if sqlserver else ("", ";")
                # 1. 列清单与 CREATE 的列名、列顺序一致
                create = re.search(r"CREATE TABLE demo_implicit \(\n(.*?)\n\)", out, flags=re.DOTALL).group(1)
                self.assertEqual(["name", "id", "num", "note"], [line.split()[0] for line in create.split("\n")])
                columns = "(name, id, num, note)"
                # 2. 值只按原有的规则转换（字符串只解码一次，SQL Server 带 N 前缀），行之间、值中的换行与空白保持
                first = (f"INSERT INTO demo_implicit {columns} VALUES ({sql_string('a', n)}, 1, 1,"
                         f" {sql_string('it' + chr(39) + 's 中文😀', n)}), ({sql_string('b' + chr(39) + 'c', n)}, 9, 2,"
                         f" {sql_string('(name, id) VALUES (x', n)}){end}")
                second = (f"INSERT INTO demo_implicit {columns} VALUES ({sql_string('c', n)},\n 4, 3,"
                          f" {sql_string('l1' + chr(10) + 'l2', n)}),\n  ({sql_string('d', n)}, 2, NULL, NULL){end}")
                # 3. 显式的列清单保持原样
                third = f"INSERT INTO demo_implicit (id, name) VALUES (5, {sql_string('e', n)}){end}"
                identity_on = "SET IDENTITY_INSERT demo_implicit ON\nGO" if sqlserver else "SET IDENTITY_INSERT demo_implicit ON;"
                self.assertIn(f"{identity_on}\n{first}\n{second}\n{third}\n", out)
                self.assertNotIn("INSERT INTO demo_implicit VALUES", out)
                self.assertEqual(3, len(re.findall(r"(?m)^INSERT INTO demo_implicit ", out)))

    def test_column_escaping(self):
        # 列名与 CREATE 使用同一套转义，见 escape_column_name；SQL Server、达梦没有需要转义的列名，与 CREATE 相同为小写列名
        path = self.write_sql(HEADER + self.TABLE)
        for convertor_class in self.IDENTITY_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                convertor.gen_create(DDLParser(convertor.table_script_list[0].replace("`", "")).run()[0])
                script = "INSERT INTO demo_implicit VALUES ('a', 1, 1, NULL);"
                self.assertEqual("INSERT INTO demo_implicit (name, id, num, note) VALUES ('a', 1, 1, NULL);",
                                 convertor.add_insert_columns("demo_implicit", script))
                # 需要转义的列名按 escape_column_name 转义，值保持原样
                convertor.reserved_column_names = {"name"}
                self.assertEqual("INSERT INTO demo_implicit (\"name\", id, num, note) VALUES ('a', 1, 1, NULL);",
                                 convertor.add_insert_columns("demo_implicit", script))
                # 已有列清单、表未记录列时保持原样
                for table_name, unchanged in (("demo_implicit", "INSERT INTO demo_implicit (id, name) VALUES (5, 'e');"),
                                              ("unknown_table", "INSERT INTO unknown_table VALUES (1);")):
                    self.assertEqual(unchanged, convertor.add_insert_columns(table_name, unchanged))

    def test_other_convertors_unchanged(self):
        # 其它目标数据库保持省略列清单；PostgreSQL 系、Oracle 的序列起始值按建表的列顺序对应 id，见 next_sequence_value
        path = self.write_sql(HEADER + self.TABLE + self.INSERTS)
        for convertor_class in self.OTHER_CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertNotIn("序列起始值", err)
                self.assertIn("INSERT INTO demo_implicit VALUES ('a', 1, 1, 'it''s 中文😀'),"
                              " ('b''c', 9, 2, '(name, id) VALUES (x');\n", out)
                self.assertIn("INSERT INTO demo_implicit VALUES ('c',\n 4, 3, 'l1\nl2'),\n  ('d', 2, NULL, NULL);\n", out)
                self.assertIn("INSERT INTO demo_implicit (id, name) VALUES (5, 'e');\n", out)
                self.assertNotIn("(name, id, num, note)", out)
                start = "START WITH 10;" if convertor_class is OracleConvertor else "START 10;"
                self.assertIn(f"CREATE SEQUENCE demo_implicit_seq\n    {start}", out)


class InsertCommentBoundaryTest(unittest.TestCase):
    # INSERT 头部（第一行值的左括号之前）、尾部（最后的值之后）的普通注释：原来空白统一为空格后，列清单中的行注释吞掉后面的列名，
    # 尾部的行注释吞掉结尾的分号，导入失败；尾部有注释的语句整句回退，其中的 id 不计入序列的起始值，后续新增的 id 冲突。
    # 头部、尾部的注释去掉，列名、值、id 正常对应；值中的注释仍整句回退；字符串中注释样式的文本是数据
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    SEQUENCE_CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor, OracleConvertor)
    TABLE = """
CREATE TABLE `demo_cmt`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  `num` int NULL DEFAULT NULL COMMENT '数值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '注释表';

"""
    # 1. 头部：表名之后、列清单中（行注释、井号注释，其中有括号、逗号、VALUES、反引号）、列清单之后、VALUES 之后的块注释；
    #    重排的列清单，多行 VALUES
    # 2. 尾部：行注释、井号注释之后换行再分号，块注释之后分号；文件末尾的语句缺少分号且省略列清单，为最大的 id
    # 3. 字符串中注释样式的文本
    INSERTS = """INSERT INTO `demo_cmt` /* 表名后 ) VALUES (99, */ (`name`, -- 列注释 ), VALUES (98,
`id`, # 井号注释 (`x`)
`num`) /* 列清单后 */ VALUES /* VALUES 后 ( */ ('-- a', 3, 1), ('# b', 1, 2);
INSERT INTO `demo_cmt` (`id`, `name`) VALUES (7, '/* c */ VALUES (100, ') -- 尾部注释 (200,
;
INSERT INTO `demo_cmt` (`id`, `name`) VALUES (5, 'd') # 尾部注释 (300,
;
INSERT INTO `demo_cmt` (`id`, `name`) VALUES (6, 'e') /* 尾部 (400, */ ;
INSERT INTO `demo_cmt` VALUES (8, 'f', NULL) -- 文件末尾的语句缺少分号"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_head_and_tail_comments(self):
        path = self.write_sql(HEADER + self.TABLE + self.INSERTS)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                # 每条语句都按列对应：id 计入序列的起始值，不提示
                self.assertNotIn("无法按列对应", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                end = "\nGO" if convertor_class is SQLServerConvertor else ";"
                implicit = " (id, name, num)" if convertor_class in (SQLServerConvertor, DM8Convertor) else ""
                for line in (f"INSERT INTO demo_cmt (name, id, num) VALUES ({sql_string('-- a', n)}, 3, 1),"
                             f" ({sql_string('# b', n)}, 1, 2){end}",
                             f"INSERT INTO demo_cmt (id, name) VALUES (7, {sql_string('/* c */ VALUES (100, ', n)}){end}",
                             f"INSERT INTO demo_cmt (id, name) VALUES (5, {sql_string('d', n)}){end}",
                             f"INSERT INTO demo_cmt (id, name) VALUES (6, {sql_string('e', n)}){end}",
                             f"INSERT INTO demo_cmt{implicit} VALUES (8, {sql_string('f', n)}, NULL){end}"):
                    self.assertIn(line + "\n", out)
                self.assertEqual(5, len(re.findall(r"(?m)^INSERT INTO demo_cmt ", out)))
                for comment in ("表名后", "列注释", "井号注释", "列清单后", "VALUES 后", "尾部", "文件末尾", "(98", "(200"):
                    self.assertNotIn(comment, out)
                # 尾部有注释的语句中的 id（最大为 8）计入序列的起始值，导入后新增的 id 不冲突
                if convertor_class is OracleConvertor:
                    self.assertIn("CREATE SEQUENCE demo_cmt_seq\n    START WITH 9;", out)
                elif convertor_class in self.SEQUENCE_CONVERTORS:
                    self.assertIn("CREATE SEQUENCE demo_cmt_seq\n    START 9;", out)

    def test_values_comment_fallback(self):
        # 值中（各行之间）的注释：仍整句回退，保持原样，其中的 id 不计入、不校验，在 stderr 提示；尾部的注释照常去掉
        path = self.write_sql(HEADER + self.TABLE
                              + "INSERT INTO `demo_cmt` (`id`, `name`) VALUES (1, 'a'), -- 行之间\n(2, 'b') -- 尾部\n;\n")
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                self.assertIn("表 demo_cmt 的 INSERT 语句无法按列对应", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assertIn(f"VALUES (1, {n}'a'), -- 行之间\n(2, {n}'b')", out)
                self.assertNotIn("尾部", out)

    def test_executable_comment(self):
        # 头部、尾部的 MySQL 可执行注释（/*! ... */）中的内容会执行，不能当作注释去掉：明确失败，不输出部分转换的脚本
        for statement in ("INSERT INTO `demo_cmt` (`id`, `name`) /*!99999 , `num` */ VALUES (1, 'a');",
                          "INSERT INTO `demo_cmt` (`id`, `name`) VALUES (1, 'a') /*!40000 , (2, 'b') */;"):
            path = self.write_sql(HEADER + self.TABLE + statement + "\n")
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {statement}"):
                    out = io.StringIO()
                    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(io.StringIO()):
                        with self.assertRaises(ValueError) as context:
                            convertor_class(path).print()
                    self.assertIn("可执行注释", str(context.exception))
                    self.assertEqual("", out.getvalue())


class DelimiterCommandTest(unittest.TestCase):
    # mysql 客户端的 DELIMITER 命令（存储过程、触发器等）：之后的语句不按分号结束，原来按分号拆分，把创建存储过程时并不执行的
    # INSERT 当作数据导入（MySQL 中只有第 1 行）。不解析存储过程，整个文件明确失败；字符串、注释中的 DELIMITER 文本不识别
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    TABLE = """
CREATE TABLE `demo_proc`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '存储过程表';

INSERT INTO `demo_proc` (`id`, `name`) VALUES (1, 'real');
"""
    ROUTINES = (
        # 存储过程中的 INSERT
        "DELIMITER $$\nCREATE PROCEDURE `demo_p`() BEGIN\n DO 1;\n INSERT INTO `demo_proc` VALUES (9, 'not-run');\nEND$$\n"
        "DELIMITER ;\n",
        # 前面有注释，小写，mysqldump 的 ;; 与可执行注释中的触发器
        "-- 触发器\ndelimiter ;;\n/*!50003 CREATE TRIGGER `demo_t` BEFORE INSERT ON `demo_proc` FOR EACH ROW BEGIN\n"
        " INSERT INTO `demo_proc` VALUES (9, 'not-run');\nEND */;;\nDELIMITER ;\n",
        # \d 简写，文件末尾缺少换行
        "\\d //\nCREATE PROCEDURE `demo_p`() BEGIN\n INSERT INTO `demo_proc` VALUES (9, 'not-run');\nEND//\n\\d ;",
    )
    # 字符串、行注释、井号注释、块注释中的 DELIMITER 文本
    PSEUDO = """INSERT INTO `demo_proc` (`id`, `name`) VALUES (2, 'x;
DELIMITER $$
y');
-- DELIMITER $$
# DELIMITER $$
/*
DELIMITER $$
*/
INSERT INTO `demo_proc` (`id`, `name`) VALUES (3, 'DELIMITER ;');
"""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def test_delimiter_rejected(self):
        for routine in self.ROUTINES:
            path = self.write_sql(HEADER + self.TABLE + routine)
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {routine[:16]!r}"):
                    out = io.StringIO()
                    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(io.StringIO()):
                        with self.assertRaises(ValueError) as context:
                            convertor_class(path).print()
                    self.assertIn("DELIMITER", str(context.exception))
                    # 前面能转换的表、数据同样不输出
                    self.assertEqual("", out.getvalue())

    def test_pseudo_delimiter(self):
        path = self.write_sql(HEADER + self.TABLE + self.PSEUDO)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                out, err = convert(convertor_class(path))
                self.assertNotIn("无法正常解析", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                end = "\nGO" if convertor_class is SQLServerConvertor else ";"
                self.assertIn(f"INSERT INTO demo_proc (id, name) VALUES (1, {sql_string('real', n)}){end}\n", out)
                second = sql_string("x;\nDELIMITER $$\ny", n)
                self.assertIn(f"INSERT INTO demo_proc (id, name) VALUES (2, {second}){end}\n", out)
                self.assertIn(f"INSERT INTO demo_proc (id, name) VALUES (3, {sql_string('DELIMITER ;', n)}){end}\n", out)
                self.assertEqual(3, len(re.findall(r"(?m)^INSERT INTO demo_proc ", out)))


class DdlCommentConvertorTest(unittest.TestCase):
    # 表、列注释：原来按物理行、全文的正则提取，双写的引号、反斜杠转义的引号被截断，转义未解码，原样拼入单引号后目标 SQL 语法错误；
    # 交给 DDLParser 前用正则去掉 COMMENT 子句，双写的引号同样破坏解析输入。现在按词法单元从原始建表语句提取并去掉 COMMENT 子句，
    # 按 MySQL 默认 SQL 模式只解码一次，再按目标数据库生成字符串；DEFAULT、INSERT、标识符、注释中 COMMENT 样式的文本不受影响
    CONVERTORS = (PostgreSQLConvertor, KingbaseConvertor, OpengaussConvertor, HighGoConvertor,
                  OracleConvertor, SQLServerConvertor, DM8Convertor)
    # 1. 双写的引号、反斜杠转义的引号，反斜杠与控制字符的转义，中文 emoji，真实的连续换行，空注释，没有注释的列
    # 2. 一行多列、注释中的逗号括号分号等 SQL 样式文本，COMMENT 关键字与字符串换行分隔，COMMENT 前后的注释
    # 3. DEFAULT、INSERT 中 COMMENT 样式的文本，索引的注释，保留字列名；表注释的关键字小写、等号两侧多个空白，后面还有表选项
    COMMENT_TABLE = r"""
DROP TABLE IF EXISTS `demo_comment`;
CREATE TABLE `demo_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'it''s',
  `q1` varchar(64) NULL DEFAULT NULL COMMENT 'he said \'hi\'',
  `esc` varchar(64) NULL DEFAULT NULL COMMENT 'C:\\dir\\ \n\r\t\b\Z\%\_\q\"',
  `uni` varchar(64) NULL DEFAULT NULL COMMENT '中文😀',
  `multi` text NULL COMMENT 'l1


l2',
  `blank` int NULL DEFAULT NULL COMMENT '',
  `plain` int NULL DEFAULT NULL,
  `g` int NULL COMMENT 'g, (x)', `h` int NULL COMMENT 'h; SELECT ''a'' -- x',
  `pseudo` varchar(64) NULL DEFAULT 'x COMMENT ''y''' COMMENT '真实',
  `j` int NULL /* COMMENT 'ghost1' */ COMMENT /* x */ 'j' -- COMMENT 'ghost2'
  ,
  `k` int NULL
    COMMENT
    'k',
  `level` int NULL DEFAULT NULL COMMENT '等级',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_q1`(`q1` ASC) USING BTREE COMMENT 'idx''s'
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci comment   =   'owner''s 表\n' ROW_FORMAT = Dynamic;

INSERT INTO `demo_comment` (`id`, `q1`) VALUES (1, 'COMMENT ''ghost3''');
"""
    COMMENT_VALUES = {
        "id": "it's",
        "q1": "he said 'hi'",
        "esc": "C:\\dir\\ \n\r\t\b\x1a\\%\\_q\"",
        "uni": "中文😀",
        "multi": "l1\n\n\nl2",
        "blank": "",
        "plain": "",
        "g": "g, (x)",
        "h": "h; SELECT 'a' -- x",
        "pseudo": "真实",
        "j": "j",
        "k": "k",
        "level": "等级",
    }
    TABLE_COMMENT_VALUE = "owner's 表\n"
    # 双引号的注释（MySQL 默认 SQL 模式下为字符串）：双写的双引号为一个双引号，其中的两个单引号是两个字符；单引号注释中的双引号原样保留；
    # 双引号中的分号、括号、逗号、SQL 样式文本、反斜杠转义、真实换行；DEFAULT、INSERT 中双引号样式的文本；同一列最后为空的注释
    DOUBLE_QUOTE_TABLE = r"""
CREATE TABLE `demo_dq`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT "it's",
  `a` varchar(64) NULL DEFAULT NULL COMMENT "say ""hi"" ok",
  `b` varchar(64) NULL DEFAULT NULL COMMENT "two '' quotes",
  `c` varchar(64) NULL DEFAULT NULL COMMENT "esc \" \' \\ \n\t \q",
  `d` varchar(64) NULL DEFAULT NULL COMMENT "a; (b), SELECT 'x' -- y /* z */ # w",
  `e` varchar(64) NULL DEFAULT 'x COMMENT "y"' COMMENT 'single "double" and x""y',
  `f` text NULL COMMENT "l1
l2",
  `g` int NULL COMMENT "",
  `h` int NULL COMMENT 'first' COMMENT "",
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = "owner's ""表""; (x)";

INSERT INTO `demo_dq` (`id`, `e`) VALUES (1, 'it''s "q"; (z)');
"""
    DOUBLE_QUOTE_VALUES = {
        "id": "it's",
        "a": 'say "hi" ok',
        "b": "two '' quotes",
        "c": "esc \" ' \\ \n\t q",
        "d": "a; (b), SELECT 'x' -- y /* z */ # w",
        "e": 'single "double" and x""y',
        "f": "l1\nl2",
        "g": "",
        "h": "",
    }
    DOUBLE_QUOTE_TABLE_VALUE = "owner's \"表\"; (x)"

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.directory = directory.name

    def write_sql(self, content):
        # newline=""：按原样写入 \r\n、\r
        path = os.path.join(self.directory, "dump.sql")
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(content)
        return path

    def column_line(self, name, out):
        # 列定义的内容：去掉列名与行尾逗号
        return re.search(rf"(?m)^ +{name} (.*?),?$", out).group(1)

    def assert_comments(self, convertor, out, table_name, column_values, table_value):
        # 每个列注释、表注释各生成一次：PostgreSQL 系、Oracle、达梦为 COMMENT ON，SQL Server 为带 N 前缀的扩展属性
        if isinstance(convertor, SQLServerConvertor):
            for name, value in column_values.items():
                self.assertIn(f"    'MS_Description', {sql_string(value, 'N')},\n    'SCHEMA', N'dbo',\n"
                              f"    'TABLE', N'{table_name}',\n    'COLUMN', N'{name}'\nGO\n", out, name)
            self.assertIn(f"    'MS_Description', {sql_string(table_value, 'N')},\n    'SCHEMA', N'dbo',\n"
                          f"    'TABLE', N'{table_name}'\nGO\n", out)
            self.assertEqual(len(column_values) + 1, out.count(f"'TABLE', N'{table_name}'"))
            return
        for name, value in column_values.items():
            column = convertor.escape_column_name(name)
            self.assertIn(f"COMMENT ON COLUMN {table_name}.{column} IS {sql_string(value)};\n", out, name)
        self.assertIn(f"COMMENT ON TABLE {table_name} IS {sql_string(table_value)};\n", out)
        self.assertEqual(len(column_values), out.count(f"COMMENT ON COLUMN {table_name}."))
        self.assertEqual(1, out.count(f"COMMENT ON TABLE {table_name} "))

    def assert_rejected(self, convertor_class, path, message):
        # 明确失败，且不输出部分转换的脚本
        out = io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(ValueError) as context:
                convertor_class(path).print()
        self.assertIn(message, str(context.exception))
        self.assertEqual("", out.getvalue())

    def test_ddl_comments(self):
        # 反引号标识符、DEFAULT 字符串中 COMMENT 样式的文本不识别；不带反引号的列名、小写的关键字；索引的注释去掉、不记录；
        # 同一列、表有多个 COMMENT 时取最后一个；表注释的关键字与等号、字符串之间换行；
        # SQL 注释替换为空格，COMMENT 与字符串之间的注释随 COMMENT 子句一起去掉
        sql = r"""CREATE TABLE `t` (
  `my COMMENT 'x'` int /* c */ COMMENT -- d
 'a', # COMMENT 'z'
  `comment` varchar(8) DEFAULT 'COMMENT ''y''' COMMENT 'b' COMMENT 'b''2',
  comment2 int comment 'c\'',
  KEY `k` (`comment`) COMMENT 'k',
  CONSTRAINT `ck` CHECK (`comment2` > 0)
) COMMENT 'first' Comment
=
'last';"""
        stripped, table_comment, column_comments = ddl_comments(sql)
        self.assertEqual("'last'", table_comment)
        self.assertEqual({"my COMMENT 'x'": "'a'", "comment": "'b''2'", "comment2": r"'c\''"}, column_comments)
        self.assertEqual(r"""CREATE TABLE `t` (
  `my COMMENT 'x'` int   ,  """ + r"""
  `comment` varchar(8) DEFAULT 'COMMENT ''y'''  ,
  comment2 int ,
  KEY `k` (`comment`) ,
  CONSTRAINT `ck` CHECK (`comment2` > 0)
) ;""", stripped)

    def test_column_and_table_comments(self):
        path = self.write_sql(HEADER + self.COMMENT_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                # 交给 DDLParser 的建表语句不含 COMMENT 子句
                self.assertNotRegex(convertor.table_script_list[0], r"(?i)COMMENT\s*=?\s*'(?!')")
                out, err = convert(convertor)
                self.assertNotIn("无法正常解析", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assert_comments(convertor, out, "demo_comment", self.COMMENT_VALUES, self.TABLE_COMMENT_VALUE)
                # 注释中的注释、索引的注释不是列注释；DEFAULT、INSERT 中 COMMENT 样式的文本保持为数据
                for ghost in ("ghost1", "ghost2", "idx"):
                    self.assertNotIn(ghost, out.replace("idx_", ""))
                self.assertIn(f"DEFAULT {sql_string('x COMMENT ' + chr(39) + 'y' + chr(39), n)}",
                              self.column_line("pseudo", out))
                self.assertIn(f"VALUES (1, {sql_string('COMMENT ' + chr(39) + 'ghost3' + chr(39), n)})", out)
                # 去掉索引的注释后索引照常生成
                self.assertRegex(out, r"CREATE INDEX \S+ ON demo_comment \(q1\)")

    def test_crlf_comments(self):
        # 与普通 mysql 文件一致：行尾的 \r\n 为换行（注释中的 \r\n 保存为 \n），单独的 \r 是数据
        sql = (HEADER + "CREATE TABLE `demo_crlf`  (\n  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'a\nb',\n"
               "  `name` varchar(8) NULL DEFAULT NULL COMMENT 'c{CR}d',\n  PRIMARY KEY (`id`) USING BTREE\n"
               ") ENGINE = InnoDB COMMENT = 'e\nf';\n")
        path = self.write_sql(sql.replace("\n", "\r\n").replace("{CR}", "\r"))
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                out, err = convert(convertor)
                self.assertNotIn("无法正常解析", err)
                self.assert_comments(convertor, out, "demo_crlf", {"id": "a\nb", "name": "c\rd"}, "e\nf")

    def test_double_quoted_lexer(self):
        # decode_mysql_string 按外层引号处理双写的引号，单引号字符串的结果不变
        for token, value in (("'a''b\"\"c\\'d'", "a'b\"\"c'd"), ('"a""b\'\'c\\"d"', "a\"b''c\"d"),
                             ('"x\\\\y\\nz"', "x\\y\nz"), ('""', ""), ("''", "")):
            self.assertEqual(value, decode_mysql_string(token), token)
        # 拆分语句、提取注释：双引号中的分号、括号、双写的双引号与单引号，单引号中的双引号，均不拆分、不截断
        sql = ("CREATE TABLE `t` (`a` int COMMENT \"x;\"\"y''(\", `b` int COMMENT 'p\"\"q;') COMMENT = \"t;\";\n"
               "SELECT 1;")
        statements = split_statements(sql)
        self.assertEqual(2, len(statements))
        stripped, table_comment, column_comments = ddl_comments(statements[0])
        self.assertEqual('"t;"', table_comment)
        self.assertEqual({"a": "\"x;\"\"y''(\"", "b": "'p\"\"q;'"}, column_comments)
        self.assertEqual("CREATE TABLE `t` (`a` int , `b` int ) ;", stripped)
        self.assertEqual("x;\"y''(", decode_mysql_string(column_comments["a"]))
        self.assertEqual("p\"\"q;", decode_mysql_string(column_comments["b"]))

    def test_double_quoted_comments(self):
        path = self.write_sql(HEADER + self.DOUBLE_QUOTE_TABLE)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                # 双引号中的分号不拆分语句；交给 DDLParser 的建表语句不含注释内容
                self.assertEqual(["demo_dq"], table_names(convertor.original_table_script_list))
                for text in ("say", "quotes", "owner", "first"):
                    self.assertNotIn(text, convertor.table_script_list[0])
                out, err = convert(convertor)
                self.assertNotIn("无法正常解析", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assert_comments(convertor, out, "demo_dq", self.DOUBLE_QUOTE_VALUES, self.DOUBLE_QUOTE_TABLE_VALUE)
                # DEFAULT、INSERT 中双引号样式的文本保持为数据
                default, value = sql_string('x COMMENT "y"', n), sql_string("it's \"q\"; (z)", n)
                self.assertIn(f"DEFAULT {default}", self.column_line("e", out))
                self.assertIn(f"VALUES (1, {value})", out)
                self.assertEqual(1, len(re.findall(r"(?m)^INSERT INTO demo_dq ", out)))

    def test_unsupported_comments(self):
        # 注释中的 NUL（\0 转义或实际字符）与字符串一致明确失败，单引号、双引号的注释相同
        table = """
CREATE TABLE `demo_bad_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT {0},
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = {1};
"""
        for column, table_comment, message in ((r"'a\0b'", "'t'", "NUL"), ("'a'", r"'t\0'", "NUL"),
                                               (r'"a\0b"', '"t"', "NUL"), ('"a"', r'"t\0"', "NUL"),
                                               ('"a\x00b"', "'t'", "NUL"), ("'a'", '"t\x00"', "NUL")):
            path = self.write_sql(HEADER + DEMO_TABLE + table.format(column, table_comment))
            for convertor_class in self.CONVERTORS:
                with self.subTest(f"{convertor_class.__name__} {column} {table_comment}"):
                    self.assert_rejected(convertor_class, path, message)

    def test_table_options(self):
        # 括号之后连续的表选项整体去掉：COMMENT 在开头、中间、最后，有无 ENGINE，逗号、换行分隔，空注释、重复的注释取最后一个，
        # 表选项之间的注释；列、索引、函数参数中的逗号与 DEFAULT 中表选项样式的文本不受影响
        head = ("CREATE TABLE `t` (\n  `id` bigint NOT NULL COMMENT 'id, x',\n"
                "  `price` decimal(10, 2) DEFAULT 'ENGINE=x, ROW_FORMAT=y; COMMENT=''z''',\n"
                "  KEY `k` (`id`, `price`),\n  CONSTRAINT `ck` CHECK (`id` IN (1, 2))\n)")
        cases = (
            # 原来去掉 COMMENT 后残留逗号，DDLParser 无法解析
            (" COMMENT='plain',\n ROW_FORMAT=DYNAMIC;", "'plain'"),
            (" ENGINE=InnoDB, COMMENT 'mid', DEFAULT CHARSET=utf8mb4;", "'mid'"),
            (" ROW_FORMAT=DYNAMIC\nAUTO_INCREMENT = 10 ,COLLATE utf8mb4_bin, comment = \"last\";", '"last"'),
            ("COMMENT='',ENGINE=InnoDB;", "''"),
            (" COMMENT='a' COMMENT='', COMMENT 'c';", "'c'"),
            (" /* x, ENGINE=y */ ENGINE=InnoDB -- a, COMMENT='z'\n, CHARACTER SET = utf8mb4 # b, c\n COMMENT='t';", "'t'"),
            # 其它表选项的值：字符串（含分号、逗号）、反引号标识符、括号中的列表；关键字小写
            (" engine=MyISAM union=(`a`, `b`) data directory='/d,1' tablespace `ts` storage disk"
             " connection=\"c;x\" checksum=1 stats_persistent=DEFAULT;", None),
        )
        for tail, comment in cases:
            with self.subTest(tail):
                stripped, table_comment, column_comments = ddl_comments(head + tail)
                self.assertEqual(head.replace(" COMMENT 'id, x'", " ") + " ;", stripped)
                self.assertEqual(comment, table_comment)
                self.assertEqual({"id": "'id, x'"}, column_comments)
                # 表选项结束于最后一个值，即结尾的分号之前
                self.assertEqual(len(head + tail) - 1, table_options_end(head + tail, len(head)))
        # 没有表选项时不变
        self.assertEqual(head.replace(" COMMENT 'id, x'", " ") + ";", ddl_comments(head + ";")[0])
        self.assertEqual(len(head), table_options_end(head + ";", len(head)))
        self.assertEqual(len(head), table_options_end(head + " , ROW_FORMAT=DYNAMIC;", len(head)))

    def test_unrecognized_table_options(self):
        # 不认识的内容（分区定义、可执行注释、AS SELECT、多余的逗号）之前的表选项去掉，之后保持原样，不扩大支持范围
        head = "CREATE TABLE `t` (`id` bigint NOT NULL)"
        cases = (
            (" ENGINE=InnoDB PARTITION BY HASH (`id`) PARTITIONS 4;", "  PARTITION BY HASH (`id`) PARTITIONS 4;", None),
            (" ENGINE=InnoDB\n/*!50100 PARTITION BY HASH (id) */;", " \n/*!50100 PARTITION BY HASH (id) */;", None),
            (" COMMENT='a' AS SELECT 1;", "  AS SELECT 1;", "'a'"),
            (" , ROW_FORMAT=DYNAMIC;", " , ROW_FORMAT=DYNAMIC;", None),
            (" ENGINE=InnoDB,, ROW_FORMAT=DYNAMIC;", " ,, ROW_FORMAT=DYNAMIC;", None),
        )
        for tail, expected, comment in cases:
            with self.subTest(tail):
                stripped, table_comment, _ = ddl_comments(head + tail)
                self.assertEqual(head + expected, stripped)
                self.assertEqual(comment, table_comment)

    def test_convert_table_options(self):
        # 无 ENGINE、逗号分隔的表选项，COMMENT 在开头、中间、最后，均能解析并生成表、列注释；DEFAULT、INSERT 中表选项样式的文本保持为数据
        sql = HEADER + """
CREATE TABLE `demo_opt1` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NULL DEFAULT 'ENGINE;x, ROW_FORMAT=DYNAMIC' COMMENT '名称, (a)',
  `price` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`, `price`)
) COMMENT='plain',
 ROW_FORMAT=DYNAMIC;

INSERT INTO `demo_opt1` (`id`, `name`) VALUES (1, 'ENGINE=InnoDB, COMMENT=''x'';');

CREATE TABLE `demo_opt2` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  PRIMARY KEY (`id`)
) ROW_FORMAT=DYNAMIC, /* c */ COMMENT "mid", AUTO_INCREMENT=5 -- tail
, DEFAULT CHARSET=utf8mb4;

CREATE TABLE `demo_opt3` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT='' COMMENT = 'last'
ROW_FORMAT = Dynamic;
"""
        path = self.write_sql(sql)
        for convertor_class in self.CONVERTORS:
            with self.subTest(convertor_class.__name__):
                convertor = convertor_class(path)
                # 交给 DDLParser 的建表语句只去掉真正的表选项：最后的右括号之后只剩分号；DEFAULT 中表选项样式的数据保持原样
                for table_sql in convertor.table_script_list:
                    self.assertRegex(table_sql, r"\)\s*;\Z")
                self.assertIn("DEFAULT 'ENGINE;x, ROW_FORMAT=DYNAMIC'", convertor.table_script_list[0])
                out, err = convert(convertor)
                self.assertNotIn("无法正常解析", err)
                n = "N" if convertor_class is SQLServerConvertor else ""
                self.assert_comments(convertor, out, "demo_opt1", {"id": "编号", "name": "名称, (a)", "price": ""}, "plain")
                self.assert_comments(convertor, out, "demo_opt2", {"id": "id"}, "mid")
                self.assert_comments(convertor, out, "demo_opt3", {"id": "id"}, "last")
                self.assertIn(f"DEFAULT {sql_string('ENGINE;x, ROW_FORMAT=DYNAMIC', n)}", self.column_line("name", out))
                self.assertIn(f"VALUES (1, {sql_string(chr(39).join(('ENGINE=InnoDB, COMMENT=', 'x', ';')), n)})", out)
                self.assertRegex(out, r"CREATE INDEX \S+ ON demo_opt1 \(name, price\)")


class ConvertorOfficialDumpTest(unittest.TestCase):

    def test_ruoyi_vue_pro(self):
        convertor = PostgreSQLConvertor(os.path.join(MYSQL_DIR, "ruoyi-vue-pro.sql"))
        names = table_names(convertor.table_script_list)
        self.assertEqual(48, len(names))
        self.assertEqual(len(names), len(set(names)))

        out, err = convert(convertor)
        self.assertNotIn("无法正常解析", err)
        # 48 张表，加上 dual 表
        self.assertEqual(49, out.count("-- Table structure for "))

    def test_quartz(self):
        convertor = PostgreSQLConvertor(os.path.join(MYSQL_DIR, "quartz.sql"))
        names = table_names(convertor.table_script_list)
        self.assertEqual(11, len(names))
        self.assertTrue(all(name.startswith("QRTZ_") for name in names))

        # quartz 的表不转换
        out, _ = convert(convertor)
        self.assertNotIn("qrtz", out.lower())


if __name__ == "__main__":
    unittest.main()
