# encoding=utf8
"""芋道系统数据库迁移工具

Author: dhb52 (https://gitee.com/dhb52)

pip install simple-ddl-parser

or with uv
uv run --with simple-ddl-parser convertor.py postgres ../mysql/ruoyi-vue-pro.sql > ../postgresql/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py sqlserver ../mysql/ruoyi-vue-pro.sql > ../sqlserver/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py kingbase ../mysql/ruoyi-vue-pro.sql > ../kingbase/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py opengauss ../mysql/ruoyi-vue-pro.sql > ../opengauss/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py highgo ../mysql/ruoyi-vue-pro.sql > ../highgo/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py oracle ../mysql/ruoyi-vue-pro.sql > ../oracle/ruoyi-vue-pro.sql
uv run --with simple-ddl-parser convertor.py dm8 ../mysql/ruoyi-vue-pro.sql > ../dm/ruoyi-vue-pro-dm8.sql
uv run --with simple-ddl-parser convertor.py oceanbase ../mysql/ruoyi-vue-pro.sql > ../oceanbase/ruoyi-vue-pro.sql
"""

import argparse
import hashlib
import pathlib
import re
import sys
import time
from abc import ABC, abstractmethod
from typing import Dict, Generator, List, Optional, Tuple, Union

from simple_ddl_parser import DDLParser

# 避免 Windows 系统使用默认的 GBK 编码
sys.stdout = open(sys.stdout.fileno(), mode='w', encoding='utf-8', buffering=1)

PREAMBLE = """/*
 Yudao Database Transfer Tool

 Source Server Type    : MySQL

 Target Server Type    : {db_type}

 Date: {date}
*/

"""


# 依次匹配：引号字符串（支持反斜杠转义、双写引号）、注释、语句分隔符；未闭合的引号吞掉剩余内容
SQL_TOKEN_PATTERN = re.compile(
    r"'(?:\\[\s\S]|''|[^'\\])*'"
    r'|"(?:\\[\s\S]|""|[^"\\])*"'
    r"|`(?:``|[^`])*`"
    r"|--(?=\s|\Z)[^\n]*|#[^\n]*|/\*[\s\S]*?(?:\*/|\Z)"
    r"|['\"`][\s\S]*"
    r"|;"
)
# 语句开头的空白、注释
SQL_LEADING_PATTERN = re.compile(r"(?:\s|--(?=\s|\Z)[^\n]*|#[^\n]*|/\*[\s\S]*?(?:\*/|\Z))*")
CREATE_TABLE_PATTERN = re.compile(r"CREATE\s+TABLE\s", flags=re.IGNORECASE)


def split_statements(content: str) -> List[str]:
    """按分号拆分 SQL 语句，忽略引号、注释内的分号

    仅用于定位顶层语句，不是完整的 SQL 解析器。返回的语句保留原文，去掉开头的空白、注释，以分号结尾

    Args:
        content (str): sql 内容

    Returns:
        List[str]: 语句列表
    """
    statements = []
    start = 0
    for match in SQL_TOKEN_PATTERN.finditer(content):
        if match.group() != ";":
            continue
        statement = content[start:match.end()]
        start = match.end()
        statement = statement[SQL_LEADING_PATTERN.match(statement).end():]
        if statement != ";":
            statements.append(statement)
    return statements


def table_scripts(content: str) -> List[str]:
    """获得真实的 CREATE TABLE 语句，不包括 INSERT 数据、注释中的建表文本

    相关 issue：https://github.com/YunaiV/ruoyi-vue-pro/issues/1273
    """
    return [statement for statement in split_statements(content) if CREATE_TABLE_PATTERN.match(statement)]


def load_and_clean(sql_file: str) -> str:
    """加载源 SQL 文件，并清理内容方便下一步 ddl 解析

    Args:
        sql_file (str): sql文件路径

    Returns:
        str: 清理后的sql文件内容
    """
    # utf-8-sig：只去掉文件开头的 BOM，避免首条语句无法识别
    return clean(open(sql_file, encoding="utf-8-sig").read())


def clean(content: str) -> str:
    """清理 SQL 内容方便下一步 ddl 解析

    Args:
        content (str): sql 内容

    Returns:
        str: 清理后的sql内容
    """
    REPLACE_PAIR_LIST = (
        (")\nVALUES ", ") VALUES "),
        (" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ", " "),
        (" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ", " "),
        (" KEY `", " INDEX `"),
        ("UNIQUE INDEX", "UNIQUE KEY"),
        ("b'0'", "'0'"),
        ("b'1'", "'1'"),
    )

    for replace_pair in REPLACE_PAIR_LIST:
        content = content.replace(*replace_pair)
    # 移除所有 CHARACTER SET / COLLATE 变体 (utf8mb3、utf8 等)
    content = re.sub(r" CHARACTER SET \w+ COLLATE \w+", "", content)
    content = re.sub(r" CHARACTER SET \w+", "", content)
    content = re.sub(r" COLLATE \w+", "", content)
    # 移除索引字段的前缀长度定义，例如: `name`(32) -> `name`
    # 移除索引定义上的 USING BTREE COMMENT 部分
    # 相关 issue：https://t.zsxq.com/96IFc 、https://t.zsxq.com/rC3A3
    content = re.sub(r'`([^`]+)`\(\d+\)', r'`\1`', content)
    content = re.sub(r'\s+USING\s+BTREE\s+COMMENT\s+\'[^\']+\'', '', content)
    content = re.sub(r"ENGINE.*COMMENT", "COMMENT", content)
    content = re.sub(r"ENGINE.*;", ";", content)
    return content


class Convertor(ABC):
    # 不同数据库的关键字不完全一致；子类按需声明需要转义的列名。
    reserved_column_names = set()

    def __init__(self, src: str, db_type) -> None:
        self.src = src
        self.db_type = db_type
        self.content = load_and_clean(self.src)
        # original_content 保留原始 COMMENT 信息，用于注释提取
        self.original_content = open(src, encoding="utf-8-sig").read()
        # 在原始内容上扫描建表语句，避免全局清理破坏引号边界；每个建表语句再单独清理
        self.original_table_script_list = table_scripts(self.original_content)
        # 剥离列级 COMMENT，交给 DDLParser 解析
        self.table_script_list = [
            re.sub(r" COMMENT '(?:[^'\\]|\\.)*'", "", clean(table_sql))
            for table_sql in self.original_table_script_list
        ]

    @abstractmethod
    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]) -> str:
        """字段类型转换

        Args:
            type (str): 字段类型
            size (Optional[Union[int, Tuple[int]]]): 字段长度描述, 如varchar(255), decimal(10,2)

        Returns:
            str: 类型定义
        """
        pass

    @abstractmethod
    def gen_create(self, table_ddl: Dict) -> str:
        """生成 create 脚本

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str:  生成脚本
        """
        pass

    @abstractmethod
    def gen_pk(self, table_name: str) -> str:
        """生成主键定义

        Args:
            table_name (str): 表名

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_index(self, ddl: Dict) -> str:
        """生成索引定义

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段/表注释

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_uk(self, table_ddl: Dict) -> str:
        """生成

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """

    @abstractmethod
    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句块

        Args:
            table_name (str): 表名

        Returns:
            str: 生成脚本
        """
        pass

    def gen_dual(self) -> str:
        """生成虚拟 dual 表

        Returns:
            str: 生成脚本, 默认返回空脚本, 表示当前数据库无需手工创建
        """
        return ""

    def escape_column_name(self, name: str) -> str:
        """转义目标库保留字列名，例如 Oracle / Kingbase 的 level。"""

        column_name = name.lower()
        if column_name in self.reserved_column_names:
            return f'"{column_name}"'
        return column_name

    def escape_insert_columns(self, insert_script: str) -> str:
        """INSERT 显式列清单需要和 CREATE / COMMENT 使用同一套列名转义。"""

        match = re.match(
            r"(INSERT INTO\s+\S+\s*\()([^)]+)(\)\s+VALUES\s+[\s\S]*)",
            insert_script,
            flags=re.IGNORECASE,
        )
        if not match:
            return insert_script

        columns = [
            self.escape_column_name(column.strip())
            for column in match.group(2).split(",")
        ]
        return f"{match.group(1)}{', '.join(columns)}{match.group(3)}"

    @staticmethod
    def inserts(table_name: str, script_content: str) -> Generator:
        PREFIX = f"INSERT INTO `{table_name}`"

        # 收集 `table_name` 对应的 insert 语句
        for line in script_content.split("\n"):
            if line.startswith(PREFIX):
                head, tail = line.replace(PREFIX, "").split(" VALUES ", maxsplit=1)
                head = head.strip().replace("`", "").lower()
                tail = tail.strip().replace(r"\"", '"')
                # tail = tail.replace("b'0'", "'0'").replace("b'1'", "'1'")
                col_part = f" {head}" if head else ""
                yield f"INSERT INTO {table_name.lower()}{col_part} VALUES {tail}"

    @staticmethod
    def index(ddl: Dict) -> Generator:
        """生成索引定义

        Args:
            ddl (Dict): 表DDL

        Yields:
            Generator[str]: create index 语句
        """

        for no, index in enumerate(ddl.get("index", []), 1):
            columns = ", ".join(Convertor.index_columns(index.get("columns", [])))
            if not columns:
                continue
            table_name = ddl["table_name"].lower()
            yield f"CREATE INDEX idx_{table_name}_{no:02d} ON {table_name} ({columns})"

    @staticmethod
    def index_columns(columns) -> list:
        """兼容 simple-ddl-parser 不同版本的索引列结构。"""

        keys = []

        def append(name, order="ASC"):
            if not name:
                return
            column_name = str(name).strip("`").lower()
            column_order = str(order or "ASC").upper()
            if column_order == "DESC":
                keys.append(f"{column_name} desc")
            else:
                keys.append(column_name)

        def visit(value):
            # 普通索引常见结构：[[{'name': 'user_id', 'order': 'ASC'}]]
            if isinstance(value, (list, tuple)):
                for item in value:
                    visit(item)
                return
            if isinstance(value, dict):
                name = value.get("name")
                if isinstance(name, (dict, list, tuple)):
                    visit(name)
                    return
                append(name, value.get("order", "ASC"))
                return
            # 唯一索引在部分版本中会被解析成 ['mobile', 'ASC', 'tenant_id', 'ASC']。
            if isinstance(value, str):
                token = value.strip("`")
                order = token.upper()
                if order in ("ASC", "DESC"):
                    if order == "DESC" and keys and not keys[-1].endswith(" desc"):
                        keys[-1] = f"{keys[-1]} desc"
                    return
                append(token)

        visit(columns)
        return keys

    @staticmethod
    def unique_index(ddl: Dict) -> Generator:
        if "constraints" in ddl and "uniques" in ddl["constraints"]:
            uk_list = ddl["constraints"]["uniques"]
            for uk in uk_list:
                table_name = ddl["table_name"]
                uk_name = uk["constraint_name"]
                uk_columns = Convertor.index_columns(uk["columns"])
                if not uk_columns:
                    continue
                yield table_name, uk_name, uk_columns

    @staticmethod
    def filed_comments(table_sql: str) -> Generator:
        for line in table_sql.split("\n"):
            match = re.match(r"^`([^`]+)`.* COMMENT '([^']+)'", line.strip())
            if match:
                field = match.group(1)
                comment_string = match.group(2).replace("\\n", "\n")
                yield field, comment_string

    def table_comment(self, table_sql: str) -> str:
        # 兼容 COMMENT='xxx' / COMMENT = 'xxx' 等等号两侧空格变体；允许 COMMENT 内含转义单引号
        match = re.search(r"COMMENT\s*=\s*'((?:[^'\\]|\\.)*)'", table_sql)
        return match.group(1) if match else None

    def print(self):
        """打印转换后的sql脚本到终端"""
        print(
            PREAMBLE.format(
                db_type=self.db_type,
                date=time.strftime("%Y-%m-%d %H:%M:%S"),
            )
        )

        dual = self.gen_dual()
        if dual:
            print(
                f"""-- ----------------------------
-- Table structure for dual
-- ----------------------------
{dual}
"""
            )

        error_scripts = []
        for table_sql, orig_table_sql in zip(self.table_script_list, self.original_table_script_list):
            # 剥离 COMMENT 子句避免 DDLParser 无法处理中文等特殊字符
            table_sql_for_parse = re.sub(r"\s+COMMENT\s+'[^']*'", "", table_sql)
            ddl = DDLParser(table_sql_for_parse, normalize_names=True).run()

            # 如果parse失败, 需要跟进
            if len(ddl) == 0:
                error_scripts.append(table_sql)
                continue

            table_ddl = ddl[0]
            table_name = table_ddl["table_name"]

            # simple-ddl-parser 1.9+ 将字符串默认值中的等号解析为 ``\\03d``。
            # 还原为原始字符，避免导出的目标库脚本出现错误默认值。
            for column in table_ddl["columns"]:
                if isinstance(column.get("default"), str):
                    column["default"] = column["default"].replace(r"\03d", "=")

            # 忽略 quartz 的内容
            if table_name.lower().startswith("qrtz"):
                continue

            # 解析注释
            # 从原始建表语句提取注释（支持中文等 DDLParser 不能处理的内容），避免 INSERT 数据中的同名建表文本干扰
            comments_dict = dict(Convertor.filed_comments(orig_table_sql))
            for column in table_ddl["columns"]:
                column["comment"] = comments_dict.get(column["name"], "")
            table_ddl["comment"] = self.table_comment(orig_table_sql) or ""

            # 为每个表生成个6个基本部分
            create = self.gen_create(table_ddl)
            has_id = any(col["name"].lower() == "id" for col in table_ddl["columns"])
            pk = self.gen_pk(table_name) if has_id else ""
            uk = self.gen_uk(table_ddl)
            index = self.gen_index(table_ddl)
            comment = self.gen_comment(table_ddl)
            inserts = self.gen_insert(table_name)

            # 组合当前表的DDL脚本
            script = f"""{create}

{pk}

{uk}

{index}

{comment}

{inserts}
"""

            # 清理
            script = re.sub("\n{3,}", "\n\n", script).strip() + "\n"

            print(script, end="")

        # 将parse失败的脚本打印出来
        if error_scripts:
            print("!!! 以下内容无法正常解析", file=sys.stderr)
            for script in error_scripts:
                # print to stderr
                print(script, file=sys.stderr)


class PostgreSQLConvertor(Convertor):
    def __init__(self, src):
        super().__init__(src, "PostgreSQL")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        if type in ("varchar", "char"):
            # MySQL 中未指定长度的 char 等同于 char(1)
            return f"varchar({size or 1})"
        if type in ("int", "int unsigned", "int unsigned zerofill"):
            return "int4"
        if type in ("bigint", "bigint unsigned"):
            return "int8"
        if type in ("tinyint", "smallint", "tinyint unsigned"):
            return "int2"
        if type in ("datetime", "timestamp null"):
            return "timestamp"
        if type == "date":
            return "date"
        if type == "json":
            return "jsonb"
        if type == "time":
            return "time"
        if type == "double":
            return "double precision"
        if type == "timestamp":
            return f"timestamp({size})" if size else "timestamp"
        if type == "bit":
            return "bool"
        if type in ("text", "longtext", "mediumtext"):
            return "text"
        if type in ("blob", "mediumblob", "longblob"):
            return "bytea"
        if type == "decimal":
            return (
                f"numeric({','.join(str(s) for s in size)})" if size and len(size) else "numeric"
            )

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""

        def _generate_column(col):
            name = col["name"].lower()
            if name == "deleted":
                return "deleted int2 NOT NULL DEFAULT 0"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            if full_type is None:
                raise NotImplementedError(
                    f"未支持的类型: '{col['type']}' (列: {name}, 表: {ddl['table_name']})"
                )
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            return f"{self.escape_column_name(name)} {full_type} {nullable} {default}"

        table_name = ddl["table_name"].lower()
        columns = [f"{_generate_column(col).strip()}" for col in ddl["columns"]]
        filed_def_list = ",\n  ".join(columns)
        script = f"""-- ----------------------------
-- Table structure for {table_name}
-- ----------------------------
DROP TABLE IF EXISTS {table_name};
CREATE TABLE {table_name} (
    {filed_def_list}
);"""

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段及表的注释"""

        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{self.escape_column_name(column['name'])} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name) -> str:
        """生成主键定义"""
        return f"ALTER TABLE {table_name} ADD CONSTRAINT pk_{table_name} PRIMARY KEY (id);\n"

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句，以及根据最后的 insert id+1 生成 Sequence"""

        inserts = list(Convertor.inserts(table_name, self.content))
        inserts = [self.escape_insert_columns(s) for s in inserts]
        # 转换 MySQL 字符串转义为 PostgreSQL 格式：\\ -> \，\' -> ''
        inserts = [re.sub(r"\\\\|\\'", lambda m: "\\" if m.group() == "\\\\" else "''", s) for s in inserts]
        ## 生成 insert 脚本
        script = ""
        last_id = 0
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n-- ----------------------------
-- Records of {table_name.lower()}
-- ----------------------------
-- @formatter:off
BEGIN;
{inserts_lines}
COMMIT;
-- @formatter:on"""
            match = re.search(r"VALUES \((\d+),", inserts[-1])
            if match:
                last_id = int(match.group(1))

        # 生成 Sequence
        script += (
            "\n\n"
            + f"""DROP SEQUENCE IF EXISTS {table_name}_seq;
CREATE SEQUENCE {table_name}_seq
    START {last_id + 1};"""
        )

        return script

    def gen_dual(self) -> str:
        return """DROP TABLE IF EXISTS dual;
CREATE TABLE dual
(
    id int2
);

COMMENT ON TABLE dual IS '数据库连接的表';

-- ----------------------------
-- Records of dual
-- ----------------------------
-- @formatter:off
INSERT INTO dual VALUES (1);
-- @formatter:on"""


class OracleConvertor(Convertor):
    reserved_column_names = {"level", "size"}
    # Oracle 12.2 以下（或 COMPATIBLE < 12.2）标识符最长 30 字节，超出会报 ORA-00972。相关案例：https://t.zsxq.com/tbf3q
    max_identifier_length = 30

    def __init__(self, src):
        super().__init__(src, "Oracle")

    def shorten_identifier(self, name: str) -> str:
        """超长的索引、约束、序列名：截断后追加完整名称的哈希，保证结果稳定且不重名"""
        if len(name.encode("utf-8")) <= self.max_identifier_length:
            return name
        digest = hashlib.md5(name.encode("utf-8")).hexdigest()[:6]
        # 按字节截断，并丢弃被截断的不完整字符
        prefix_bytes = name.encode("utf-8")[: self.max_identifier_length - len(digest) - 1]
        prefix = prefix_bytes.decode("utf-8", errors="ignore").rstrip("_")
        return f"{prefix}_{digest}"

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""
        type = type.lower()

        if type == "varchar":
            # MySQL varchar(n) 按字符计算长度，Oracle 默认按字节，所以显式使用 CHAR 语义。相关案例：https://t.zsxq.com/tbf3q
            return f"varchar2({size if size < 4000 else 4000} char)"
        if type in ("int", "int unsigned"):
            return "number"
        if type == "bigint" or type == "bigint unsigned":
            return "number"
        if type == "datetime":
            return "date"
        if type == "timestamp":
            return f"timestamp({size})"
        if type == "bit":
            return "number(1,0)"
        if type in ("tinyint", "smallint"):
            return "smallint"
        if type in ("text", "longtext"):
            return "clob"
        if type in ("blob", "mediumblob"):
            return "blob"
        if type == "decimal":
            return (
                f"number({','.join(str(s) for s in size)})" if len(size) else "number"
            )

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""

        def generate_column(col):
            name = col["name"].lower()
            if name == "deleted":
                return "deleted number(1,0) DEFAULT 0 NOT NULL"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            # Oracle的 INSERT '' 不能通过NOT NULL校验，因此对文字类型字段覆写为 NULL
            nullable = "NULL" if type in ("varchar", "text", "longtext") else nullable
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            # Oracle DEFAULT 定义在 NULLABLE 之前
            return f"{self.escape_column_name(name)} {full_type} {default} {nullable}"

        table_name = ddl["table_name"].lower()
        columns = [f"{generate_column(col).strip()}" for col in ddl["columns"]]
        field_def_list = ",\n    ".join(columns)
        script = f"""-- ----------------------------
-- Table structure for {table_name}
-- ----------------------------
CREATE TABLE {table_name} (
    {field_def_list}
);"""

        # oracle INSERT '' 不能通过 NOT NULL 校验
        script = script.replace("DEFAULT '' NOT NULL", "DEFAULT '' NULL")

        return script

    def gen_comment(self, table_ddl: Dict) -> str:
        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{self.escape_column_name(column['name'])} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        pk_name = self.shorten_identifier(f"pk_{table_name}")
        return f"ALTER TABLE {table_name} ADD CONSTRAINT {pk_name} PRIMARY KEY (id);\n"

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = self.shorten_identifier(f"uk_{table_name}_{idx:02d}")
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_index(self, ddl: Dict) -> str:
        scripts = []
        for script in self.index(ddl):
            # 格式为 CREATE INDEX {index_name} ON ...
            index_name = script.split()[2]
            scripts.append(script.replace(index_name, self.shorten_identifier(index_name), 1) + ";")
        return "\n".join(scripts)

    def gen_insert(self, table_name: str) -> str:
        """拷贝 INSERT 语句"""
        inserts = []
        for insert_script in Convertor.inserts(table_name, self.content):
            insert_script = self.escape_insert_columns(insert_script)
            # 对日期数据添加 TO_DATE 转换
            insert_script = re.sub(
                r"('\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}')",
                r"to_date(\g<1>, 'SYYYY-MM-DD HH24:MI:SS')",
                insert_script,
            )
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        last_id = 0
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n-- ----------------------------
-- Records of {table_name.lower()}
-- ----------------------------
-- @formatter:off
{inserts_lines}
COMMIT;
-- @formatter:on"""
            match = re.search(r"VALUES \((\d+),", inserts[-1])
            if match:
                last_id = int(match.group(1))

        # 生成 Sequence
        seq_name = self.shorten_identifier(f"{table_name}_seq")
        if seq_name != f"{table_name}_seq":
            # 序列名会被 @KeySequence 引用，缩短后需要提示同步修改
            print(f"!!! Oracle 序列名 {table_name}_seq 超过 {self.max_identifier_length} 字节，已缩短为 {seq_name}，"
                  f"如有 @KeySequence 引用需同步修改", file=sys.stderr)
        script += f"""

CREATE SEQUENCE {seq_name}
    START WITH {last_id + 1};"""

        return script


class SQLServerConvertor(Convertor):
    """_summary_

    Args:
        Convertor (_type_): _description_
    """

    def __init__(self, src):
        super().__init__(src, "Microsoft SQL Server")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        if type == "varchar":
            return f"nvarchar({size if size < 4000 else 4000})"
        if type in ("int", "int unsigned"):
            return "int"
        if type in ("bigint", "bigint unsigned"):
            return "bigint"
        if type in ("datetime", "timestamp"):
            return "datetime2"
        if type == "bit":
            return "varchar(1)"
        if type in ("tinyint", "smallint"):
            return "tinyint"
        if type in ("text", "longtext"):
            return "nvarchar(max)"
        if type in ("blob", "mediumblob"):
            return "varbinary(max)"
        if type == "decimal":
            return (
                f"numeric({','.join(str(s) for s in size)})" if len(size) else "numeric"
            )

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""

        def _generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"
            if name == "deleted":
                return "deleted bit DEFAULT 0 NOT NULL"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            return f"{name} {full_type} {default} {nullable}"

        table_name = ddl["table_name"].lower()
        columns = [f"{_generate_column(col).strip()}" for col in ddl["columns"]]
        filed_def_list = ",\n    ".join(columns)
        script = f"""-- ----------------------------
-- Table structure for {table_name}
-- ----------------------------
DROP TABLE IF EXISTS {table_name}
GO
CREATE TABLE {table_name} (
    {filed_def_list}
)
GO"""

        return script

    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段及表的注释"""

        script = ""
        table_name = table_ddl["table_name"]

        for column in table_ddl["columns"]:
            column_comment = column["comment"]
            field = column["name"]

            script += f"""EXEC sp_addextendedproperty
    'MS_Description', N'{column_comment}',
    'SCHEMA', N'dbo',
    'TABLE', N'{table_name}',
    'COLUMN', N'{field}'
GO

"""

        table_comment = table_ddl["comment"]
        if table_comment:
            script += f"""EXEC sp_addextendedproperty
    'MS_Description', N'{table_comment}',
    'SCHEMA', N'dbo',
    'TABLE', N'{table_name}'
GO

"""
        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        return ""

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)})\nGO"

        return script

    def gen_index(self, ddl: Dict) -> str:
        """生成 index"""
        return "\n".join(f"{script}\nGO" for script in self.index(ddl))

    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句"""

        # 收集 `table_name` 对应的 insert 语句
        inserts = []
        for insert_script in Convertor.inserts(table_name, self.content):
            # SQLServer: 字符串前加N，hack，是否存在替换字符串内容的风险
            insert_script = insert_script.replace(", '", ", N'").replace(
                "VALUES ('", "VALUES (N')"
            )
            # 删除 insert 的结尾分号
            insert_script = re.sub(";$", r"\nGO", insert_script)
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n-- ----------------------------
-- Records of {table_name.lower()}
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT {table_name.lower()} ON
GO
{inserts_lines}
SET IDENTITY_INSERT {table_name.lower()} OFF
GO
COMMIT
GO
-- @formatter:on"""

        return script

    def gen_dual(self) -> str:
        return """DROP TABLE IF EXISTS dual
GO
CREATE TABLE dual
(
  id int
)
GO

EXEC sp_addextendedproperty
    'MS_Description', N'数据库连接的表',
    'SCHEMA', N'dbo',
    'TABLE', N'dual'
GO

-- ----------------------------
-- Records of dual
-- ----------------------------
-- @formatter:off
INSERT INTO dual VALUES (1)
GO
-- @formatter:on"""


class DM8Convertor(Convertor):
    def __init__(self, src):
        super().__init__(src, "DM8")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""
        type = type.lower()

        if type == "varchar":
            # MySQL varchar(n) is character-oriented. DM8 may treat varchar(n)
            # as bytes, so use explicit CHAR semantics for generated scripts.
            return f"varchar({size} char)"
        if type in ("int", "int unsigned"):
            return "int"
        if type in ("bigint", "bigint unsigned"):
            return "bigint"
        if type == "datetime":
            return "datetime"
        if type == "timestamp":
            return f"timestamp({size})"
        if type == "bit":
            return "bit"
        if type in ("tinyint", "smallint"):
            return "smallint"
        if type in ("text", "longtext"):
            return "text"
        if type in ("blob", "mediumblob"):
            return "blob"
        if type == "decimal":
            return (
                f"decimal({','.join(str(s) for s in size)})" if len(size) else "decimal"
            )

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""

        def generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            return f"{name} {full_type} {default} {nullable}"

        table_name = ddl["table_name"].lower()
        columns = [f"{generate_column(col).strip()}" for col in ddl["columns"]]
        field_def_list = ",\n    ".join(columns)
        script = f"""-- ----------------------------
-- Table structure for {table_name}
-- ----------------------------
CREATE TABLE {table_name} (
    {field_def_list}
);"""

        # oracle INSERT '' 不能通过 NOT NULL 校验
        script = script.replace("DEFAULT '' NOT NULL", "DEFAULT '' NULL")

        return script

    def gen_comment(self, table_ddl: Dict) -> str:
        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{column['name']} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        return ""

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_insert(self, table_name: str) -> str:
        """拷贝 INSERT 语句"""
        inserts = list(Convertor.inserts(table_name, self.content))

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n-- ----------------------------
-- Records of {table_name.lower()}
-- ----------------------------
-- @formatter:off
SET IDENTITY_INSERT {table_name.lower()} ON;
{inserts_lines}
COMMIT;
SET IDENTITY_INSERT {table_name.lower()} OFF;
-- @formatter:on"""

        return script


class KingbaseConvertor(PostgreSQLConvertor):
    reserved_column_names = {"level"}

    def __init__(self, src):
        super().__init__(src)
        self.db_type = "Kingbase"

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""

        def _generate_column(col):
            name = col["name"].lower()
            if name == "deleted":
                return "deleted int2 NOT NULL DEFAULT 0"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            if full_type == "text":
                nullable = "NULL"
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            return f"{self.escape_column_name(name)} {full_type} {nullable} {default}"

        table_name = ddl["table_name"].lower()
        columns = [f"{_generate_column(col).strip()}" for col in ddl["columns"]]
        filed_def_list = ",\n  ".join(columns)
        script = f"""-- ----------------------------
-- Table structure for {table_name}
-- ----------------------------
DROP TABLE IF EXISTS {table_name};
CREATE TABLE {table_name} (
    {filed_def_list}
);"""

        # Kingbase INSERT '' 不能通过 NOT NULL 校验
        script = script.replace("NOT NULL DEFAULT ''", "NULL DEFAULT ''")

        return script


class OpengaussConvertor(KingbaseConvertor):
    reserved_column_names = set()

    def __init__(self, src):
        super().__init__(src)
        self.db_type = "OpenGauss"


class HighGoConvertor(PostgreSQLConvertor):
    def __init__(self, src):
        super().__init__(src)
        self.db_type = "HighGo"


def main():
    parser = argparse.ArgumentParser(description="芋道系统数据库转换工具")
    parser.add_argument(
        "type",
        type=str,
        help="目标数据库类型",
        choices=["postgres", "oracle", "sqlserver", "dm8", "kingbase", "opengauss", "highgo", "oceanbase"],
    )
    parser.add_argument(
        "path",
        type=str,
        help="源数据库脚本路径",
        nargs="?",
        default="../mysql/ruoyi-vue-pro.sql"
    )
    args = parser.parse_args()

    sql_file = pathlib.Path(args.path).resolve().as_posix()
    if args.type == "oceanbase":
        # OceanBase MySQL 模式直接使用原始 SQL，保留字段类型、索引和数据。
        print(pathlib.Path(sql_file).read_text(encoding="utf-8"), end="")
        return

    convertor = None
    if args.type == "postgres":
        convertor = PostgreSQLConvertor(sql_file)
    elif args.type == "oracle":
        convertor = OracleConvertor(sql_file)
    elif args.type == "sqlserver":
        convertor = SQLServerConvertor(sql_file)
    elif args.type == "dm8":
        convertor = DM8Convertor(sql_file)
    elif args.type == "kingbase":
        convertor = KingbaseConvertor(sql_file)
    elif args.type == "opengauss":
        convertor = OpengaussConvertor(sql_file)
    elif args.type == "highgo":
        convertor = HighGoConvertor(sql_file)
    else:
        raise NotImplementedError(f"不支持目标数据库类型: {args.type}")

    convertor.print()


if __name__ == "__main__":
    main()
