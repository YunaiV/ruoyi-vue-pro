import contextlib
import io
import os
import tempfile
import unittest

from convertor import PostgreSQLConvertor

SAMPLE = """\
DROP TABLE IF EXISTS `demo`;
CREATE TABLE `demo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL COMMENT '名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='demo';

INSERT INTO `demo` VALUES (1, 'CREATE TABLE `undo_log` (\\n  `id` bigint NOT NULL,\\n  PRIMARY KEY (`id`)\\n) ;');
"""


class ConvertorTableScanTest(unittest.TestCase):
    def test_create_table_inside_insert_data_is_ignored(self):
        with tempfile.TemporaryDirectory() as d:
            path = os.path.join(d, "dump.sql")
            with open(path, "w", encoding="utf-8") as f:
                f.write(SAMPLE)
            convertor = PostgreSQLConvertor(path)
            self.assertEqual(len(convertor.table_script_list), 1)
            self.assertTrue(convertor.table_script_list[0].startswith("CREATE TABLE `demo`"))
            err = io.StringIO()
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(err):
                convertor.print()
            self.assertNotIn("无法正常解析", err.getvalue())


if __name__ == "__main__":
    unittest.main()
