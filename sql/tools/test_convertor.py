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
