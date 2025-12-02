#!/usr/bin/env python3
"""
MySQL to PostgreSQL INSERT Statement Converter

将 MySQL INSERT 语句转换为 PostgreSQL 兼容格式

主要转换规则:
1. 移除反引号 (`)
2. b'0' -> false, b'1' -> true
3. 保持 NULL 不变
4. 处理转义字符
5. 移除 MySQL 特有语法

使用方法:
    python mysql_to_pgsql_converter.py <input_sql_file> [output_sql_file]

示例:
    # 转换所有表
    python mysql_to_pgsql_converter.py ../mysql/data.sql data-pgsql.sql

    # 仅转换指定表
    python mysql_to_pgsql_converter.py ../mysql/data.sql data-pgsql.sql -t pay_app pay_channel

    # 显示统计信息
    python mysql_to_pgsql_converter.py ../mysql/data.sql data-pgsql.sql -v

问题记录 (2025-12-02):
1. 单行 INSERT 语句解析: 需要在检测到 INSERT INTO 时立即检查行尾分号
2. b'0'/b'1' 正则匹配: 使用前向断言 (?=[,)\\s;]|$) 替代 \\b
3. 表字段类型不匹配: 需要先运行 upgrade 脚本修复 smallint -> boolean
"""

import re
import sys
import argparse
from pathlib import Path
from typing import List, Optional


class MySQLToPostgreSQLConverter:
    """MySQL INSERT 语句到 PostgreSQL 的转换器"""

    def __init__(self, tables: Optional[List[str]] = None):
        """
        初始化转换器

        Args:
            tables: 要转换的表名列表，None 表示转换所有表
        """
        self.tables = tables  # None 表示转换所有表
        self.stats = {
            'total_lines': 0,
            'insert_statements': 0,
            'converted': 0,
            'skipped': 0,
            'errors': 0,
        }
        self.converted_tables = set()

    def convert_file(self, input_path: str, output_path: Optional[str] = None) -> str:
        """
        转换整个 SQL 文件

        Args:
            input_path: 输入 MySQL SQL 文件路径
            output_path: 输出 PostgreSQL SQL 文件路径，None 则返回字符串

        Returns:
            转换后的 SQL 内容
        """
        input_file = Path(input_path)
        if not input_file.exists():
            raise FileNotFoundError(f"Input file not found: {input_path}")

        with open(input_file, 'r', encoding='utf-8') as f:
            content = f.read()

        converted = self._convert_content(content, input_file.name)

        if output_path:
            output_file = Path(output_path)
            output_file.parent.mkdir(parents=True, exist_ok=True)
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(converted)
            print(f"Converted SQL written to: {output_path}")

        return converted

    def _convert_content(self, content: str, source_name: str = 'unknown') -> str:
        """转换 SQL 内容"""
        lines = content.split('\n')
        self.stats['total_lines'] = len(lines)

        converted_lines = []
        header = self._generate_header(source_name)
        converted_lines.append(header)

        current_insert = []
        in_insert = False

        for line in lines:
            stripped = line.strip()

            # 检测 INSERT 语句开始
            if stripped.startswith('INSERT INTO'):
                in_insert = True
                current_insert = [line]
                self.stats['insert_statements'] += 1
                # 检查单行 INSERT 语句 (以分号结尾)
                if stripped.endswith(';'):
                    full_insert = '\n'.join(current_insert)
                    converted = self._convert_insert(full_insert)
                    if converted:
                        converted_lines.append(converted)
                    in_insert = False
                    current_insert = []
            elif in_insert:
                current_insert.append(line)
                # 检测多行 INSERT 语句结束 (以分号结尾)
                if stripped.endswith(';'):
                    full_insert = '\n'.join(current_insert)
                    converted = self._convert_insert(full_insert)
                    if converted:
                        converted_lines.append(converted)
                    in_insert = False
                    current_insert = []
            # 保留 BEGIN/COMMIT 事务语句
            elif stripped in ('BEGIN;', 'COMMIT;'):
                converted_lines.append(line)

        # 添加统计信息
        converted_lines.append(self._generate_footer())

        return '\n'.join(converted_lines)

    def _convert_insert(self, insert_sql: str) -> Optional[str]:
        """
        转换单条 INSERT 语句

        Args:
            insert_sql: MySQL INSERT 语句

        Returns:
            PostgreSQL INSERT 语句，None 表示跳过
        """
        # 提取表名
        table_match = re.search(r'INSERT INTO\s+`?(\w+)`?', insert_sql)
        if not table_match:
            self.stats['errors'] += 1
            return None

        table_name = table_match.group(1)

        # 检查是否在转换列表中 (None 表示转换所有表)
        if self.tables is not None and table_name not in self.tables:
            self.stats['skipped'] += 1
            return None

        try:
            converted = self._apply_conversions(insert_sql)
            self.stats['converted'] += 1
            self.converted_tables.add(table_name)
            return converted
        except Exception as e:
            print(f"Error converting INSERT for table {table_name}: {e}")
            self.stats['errors'] += 1
            return None

    def _apply_conversions(self, sql: str) -> str:
        """应用所有转换规则"""
        result = sql

        # 1. 移除反引号
        result = result.replace('`', '')

        # 2. 转换 MySQL bit 类型到 PostgreSQL boolean
        # b'0' -> false, b'1' -> true
        # 注意：需要先运行 upgrade 脚本将 smallint 列改为 boolean
        # 使用更宽松的匹配：b'0' 或 b'1' 后面跟着逗号、括号、空格或行尾
        result = re.sub(r"b'0'(?=[,)\s;]|$)", 'false', result)
        result = re.sub(r"b'1'(?=[,)\s;]|$)", 'true', result)

        # 3. 处理 CHARACTER SET 和 COLLATE 子句 (在 INSERT 中不常见，但保险起见)
        result = re.sub(r'\s+CHARACTER SET\s+\w+', '', result, flags=re.IGNORECASE)
        result = re.sub(r'\s+COLLATE\s+\w+', '', result, flags=re.IGNORECASE)

        # 4. 处理 MySQL 特有的转义 (如 \')
        # PostgreSQL 使用 '' 来转义单引号
        # 注意：这需要小心处理，避免破坏正常的转义
        # 将 \' 转换为 '' (PostgreSQL 标准单引号转义)
        result = result.replace("\\'", "''")

        # 5. 处理 ON DUPLICATE KEY UPDATE (PostgreSQL 使用 ON CONFLICT)
        result = re.sub(
            r'\s+ON DUPLICATE KEY UPDATE.*?;',
            ';',
            result,
            flags=re.IGNORECASE | re.DOTALL
        )

        return result

    def _generate_header(self, source_name: str) -> str:
        """生成输出文件头部"""
        tables_str = '\n'.join(f'   - {t}' for t in sorted(self.converted_tables)) if self.converted_tables else '   (all tables)'
        return f"""/*
 MySQL to PostgreSQL Data Migration Script

 Generated by: mysql_to_pgsql_converter.py
 Source: {source_name} (MySQL)
 Target: PostgreSQL

 Tables included:
{tables_str}

 Usage:
   psql -U postgres -d ruoyi-vue-pro -f <this_file.sql>

 Or with password:
   PGPASSWORD=Pass1234 psql -h 127.0.0.1 -U postgres -d ruoyi-vue-pro -f <this_file.sql>

 IMPORTANT:
   Before running this script, you may need to run the corresponding
   upgrade script to fix column types (smallint -> boolean).
*/

-- Disable triggers during import for better performance
SET session_replication_role = replica;

"""

    def _generate_footer(self) -> str:
        """生成输出文件尾部"""
        tables_list = sorted(self.converted_tables)
        seq_patterns = '|'.join(f'{t}' for t in tables_list) if tables_list else '.*'

        return f"""
-- Re-enable triggers
SET session_replication_role = DEFAULT;

-- Update sequences to max id + 1
DO $$
DECLARE
    seq_name TEXT;
    table_name TEXT;
    max_id BIGINT;
BEGIN
    FOR seq_name, table_name IN
        SELECT s.relname, regexp_replace(s.relname, '_seq$', '')
        FROM pg_class s
        JOIN pg_namespace n ON s.relnamespace = n.oid
        WHERE s.relkind = 'S'
        AND n.nspname = 'public'
        AND s.relname ~ '^({seq_patterns})_seq$'
    LOOP
        BEGIN
            EXECUTE format('SELECT COALESCE(MAX(id), 0) + 1 FROM %I', table_name) INTO max_id;
            IF max_id IS NOT NULL THEN
                EXECUTE format('ALTER SEQUENCE %I RESTART WITH %s', seq_name, max_id);
                RAISE NOTICE 'Updated sequence % to %', seq_name, max_id;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            -- Table might not exist, skip
            NULL;
        END;
    END LOOP;
END $$;

-- Statistics
/*
Conversion Statistics:
{chr(10).join(f'  {k}: {v}' for k, v in self.stats.items())}

Tables converted:
{chr(10).join(f'  - {t}' for t in sorted(self.converted_tables))}
*/
"""

    def print_stats(self):
        """打印转换统计信息"""
        print("\n=== Conversion Statistics ===")
        for key, value in self.stats.items():
            print(f"  {key}: {value}")
        print("\nTables converted:")
        for table in sorted(self.converted_tables):
            print(f"  - {table}")


def main():
    parser = argparse.ArgumentParser(
        description='Convert MySQL INSERT statements to PostgreSQL format'
    )
    parser.add_argument(
        'input_file',
        help='Input MySQL SQL file path'
    )
    parser.add_argument(
        'output_file',
        nargs='?',
        default=None,
        help='Output PostgreSQL SQL file path (default: stdout)'
    )
    parser.add_argument(
        '-t', '--tables',
        nargs='+',
        default=None,
        help='Specific tables to convert (default: all tables)'
    )
    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Print conversion statistics'
    )

    args = parser.parse_args()

    converter = MySQLToPostgreSQLConverter(tables=args.tables)

    try:
        result = converter.convert_file(args.input_file, args.output_file)

        if not args.output_file:
            print(result)

        if args.verbose:
            converter.print_stats()

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
