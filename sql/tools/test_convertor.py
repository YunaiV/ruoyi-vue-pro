# encoding=utf8
"""convertor.py 的单元测试

cd sql/tools
uv run --with simple-ddl-parser python -m unittest test_convertor -v
"""

import contextlib
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
    split_statements,
)

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
