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


# 单引号字符串：支持反斜杠转义、双写引号
SQL_STRING = r"'(?:\\[\s\S]|''|[^'\\])*'"
# 双引号字符串（MySQL 默认 SQL 模式）：支持反斜杠转义、双写引号
SQL_DOUBLE_QUOTED_STRING = r'"(?:\\[\s\S]|""|[^"\\])*"'
# 依次匹配：引号字符串（支持反斜杠转义、双写引号）、注释、语句分隔符；未闭合的引号吞掉剩余内容
SQL_TOKEN_PATTERN = re.compile(
    SQL_STRING
    + "|" + SQL_DOUBLE_QUOTED_STRING
    + r"|`(?:``|[^`])*`"
    r"|--(?=\s|\Z)[^\n]*|#[^\n]*|/\*[\s\S]*?(?:\*/|\Z)"
    r"|['\"`][\s\S]*"
    r"|;"
)
# 语句开头的空白、注释
SQL_LEADING_PATTERN = re.compile(r"(?:\s|--(?=\s|\Z)[^\n]*|#[^\n]*|/\*[\s\S]*?(?:\*/|\Z))*")
CREATE_TABLE_PATTERN = re.compile(r"CREATE\s+TABLE\s", flags=re.IGNORECASE)
# MySQL 日期时间字面量，分组 1 为小数秒，例如 '2026-01-01 00:00:00.123'
DATETIME_LITERAL_PATTERN = re.compile(r"'\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(\.\d{1,6})?'")
# 简单数值字面量：可带正负号、小数点、科学计数，例如 -1.5E+300、.5、1.；不含 Oracle 的 F、D 后缀
NUMBER_LITERAL_PATTERN = re.compile(r"[+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?")
# 建表语句中 DEFAULT 后以小数点开头的完整简单数值，例如 .5、-.5、+.5e-3；后面紧跟运算符的表达式不匹配；
# 分组 1 为 DEFAULT 及空白，2 为数值
LEADING_DOT_DEFAULT_PATTERN = re.compile(
    r"(?<![\w$])(DEFAULT\s+)([+-]?\.\d+(?:[eE][+-]?\d+)?)(?![\w.]|\s*[-+*/%^|&])", flags=re.IGNORECASE)
# 解析前替换以小数点开头的数值默认值的占位标识，{0} 为序号
DEFAULT_NUMBER_PLACEHOLDER = "yudao_default_number_{0}"
# 解析前替换字符串默认值的占位标识，{0} 为序号
DEFAULT_STRING_PLACEHOLDER = "yudao_default_string_{0}"
# 单引号字符串之前紧邻的前缀：N、n（国家字符集），x、X（十六进制），b、B（位值）；或 _ 开头的字符集引导符，
# 与字符串之间可有空白，例如 _utf8mb4'a'、_utf8mb4 'a'；含义与是否支持见 decode_mysql_literal
STRING_PREFIX = r"[nNxXbB]|_[A-Za-z0-9_]+\s*"
# 字符串之前的文本（不含其它词法单元）结尾的前缀，前面不是标识符；分组 1 为前缀
STRING_PREFIX_PATTERN = re.compile(r"(?<![\w$])(" + STRING_PREFIX + r")\Z")
# 字符串之前的 DEFAULT 关键字（前面不是标识符），表达式默认值可带左括号，例如 DEFAULT (_utf8mb4'x')；
# 分组 1 为左括号（没有时为 None），2 为字符串的 N、n、x、X 前缀或字符集引导符（没有时为 None）；
# b'0'、b'1' 由 clean 统一处理，这里不识别 b、B 前缀
DEFAULT_STRING_PREFIX_PATTERN = re.compile(
    r"(?<![\w$])DEFAULT(?:\s*(\()\s*|\s+)([nNxX]|_[A-Za-z0-9_]+\s*)?\Z", flags=re.IGNORECASE)
# 建表语句中不是列定义的开头关键字：主键、索引、约束，见 ddl_comments
TABLE_DEFINITION_KEYWORDS = {"PRIMARY", "KEY", "INDEX", "UNIQUE", "FULLTEXT", "SPATIAL", "CONSTRAINT", "FOREIGN", "CHECK"}
# 字符串之前的列定义的 COMMENT 关键字（前面不是标识符，注释已替换为空白），例如 COMMENT 'xxx'
COLUMN_COMMENT_PATTERN = re.compile(r"(?<![\w$])COMMENT\s*\Z", flags=re.IGNORECASE)
# 字符串之前的表选项的 COMMENT 关键字，可带等号，例如 COMMENT = 'xxx'、comment='xxx'
TABLE_COMMENT_PATTERN = re.compile(r"(?<![\w$])COMMENT\s*(?:=\s*)?\Z", flags=re.IGNORECASE)
# MySQL 的表选项名称，见 table_options_end；多个单词之间可有任意空白
TABLE_OPTION_NAMES = (
    "AUTOEXTEND_SIZE", "AUTO_INCREMENT", "AVG_ROW_LENGTH", "CHARACTER SET", "CHARSET", "CHECKSUM", "COLLATE", "COMMENT",
    "COMPRESSION", "CONNECTION", "DATA DIRECTORY", "INDEX DIRECTORY", "DELAY_KEY_WRITE", "ENCRYPTION", "ENGINE",
    "ENGINE_ATTRIBUTE", "INSERT_METHOD", "KEY_BLOCK_SIZE", "MAX_ROWS", "MIN_ROWS", "PACK_KEYS", "PASSWORD",
    "ROW_FORMAT", "SECONDARY_ENGINE_ATTRIBUTE", "STATS_AUTO_RECALC", "STATS_PERSISTENT", "STATS_SAMPLE_PAGES",
    "STORAGE", "TABLESPACE", "UNION",
)
# 遮蔽后的一个表选项：[DEFAULT] 名称 [=] 值，值为字符串、反引号标识符、括号中的列表（UNION）或单词、数值；
# 字符串、反引号标识符的内容已遮蔽为 x，注释已遮蔽为空白
TABLE_OPTION = (
    r"(?:DEFAULT\s+)?(?:" + "|".join(name.replace(" ", r"\s+") for name in TABLE_OPTION_NAMES) + r")(?![\w$])"
    r"\s*(?:=\s*)?(?:'x*'|\"x*\"|`x*`|\([^()]*\)|[\w$.+-]+(?![\w$.+-]))"
)
# 第一个表选项前面只能是空白；之后的表选项之间可以是空白或一个逗号
FIRST_TABLE_OPTION_PATTERN = re.compile(r"\s*" + TABLE_OPTION, flags=re.IGNORECASE)
NEXT_TABLE_OPTION_PATTERN = re.compile(r"\s*,?\s*" + TABLE_OPTION, flags=re.IGNORECASE)
# INSERT 语句：分组 1 为值之前的部分（含 VALUES），2 为列清单（没有时为 None），3 为值
INSERT_VALUES_PATTERN = re.compile(r"(INSERT INTO\s+\S+\s*(?:\(([^)]*)\)\s*)?VALUES\s+)([\s\S]*)", flags=re.IGNORECASE)
# 源 SQL 中的 INSERT 语句：关键字不区分大小写，各部分之间可有任意空白（含换行）；
# 分组 1 为反引号引用的表名，2 为列清单（含括号，没有时为 None），3 为 VALUES 之后的值（含结尾的分号）
INSERT_STATEMENT_PATTERN = re.compile(
    r"INSERT\s+INTO\s*`([^`]+)`\s*(\([^)]*\))?\s*VALUES(?![\w$])\s*([\s\S]*)", flags=re.IGNORECASE)
# INSERT 语句的表名：分组 1 为反引号引用的表名
INSERT_TABLE_PATTERN = re.compile(r"INSERT\s+INTO\s*`([^`]+)`", flags=re.IGNORECASE)
# INSERT 值的词法单元：单引号字符串、括号、逗号、分号，未闭合的单引号及双引号、反引号单独匹配，其它文本
INSERT_VALUE_TOKEN_PATTERN = re.compile(SQL_STRING + r"|[(),;'\"`]|[^(),;'\"`]+")
# MySQL 当前时间默认值：CURRENT_TIMESTAMP、LOCALTIME、LOCALTIMESTAMP 可省略括号，NOW 必须带括号；分组 1、2 为小数秒精度
CURRENT_TIME_DEFAULT_PATTERN = re.compile(
    r"(?:CURRENT_TIMESTAMP|LOCALTIME|LOCALTIMESTAMP)(?:\s*\(\s*([0-6]?)\s*\))?|NOW\s*\(\s*([0-6]?)\s*\)",
    flags=re.IGNORECASE,
)


def convert_current_time_default(type: str, default, bare: str, with_precision: str):
    """将日期时间列的 MySQL 当前时间默认值转为目标数据库的表达式，其它默认值保持原样

    只转换源类型为 datetime、timestamp 的列上完整匹配的 CURRENT_TIMESTAMP、NOW、LOCALTIME、LOCALTIMESTAMP；
    字符串、NULL、其它表达式不转换。不带括号的 CURRENT_TIMESTAMP 各目标数据库均可用，保持原样

    Args:
        type (str): 源列类型（小写）
        default: 解析得到的默认值
        bare (str): 未指定精度（含空括号）时的表达式
        with_precision (str): 指定精度（含 0）时的表达式模板，{0} 为精度

    Returns:
        转换后的默认值
    """
    if type not in ("datetime", "timestamp", "timestamp null") or not isinstance(default, str):
        return default
    match = CURRENT_TIME_DEFAULT_PATTERN.fullmatch(default.strip())
    if not match:
        return default
    precision = match.group(1) or match.group(2)
    if precision:
        return with_precision.format(int(precision))
    if re.fullmatch(r"CURRENT_TIMESTAMP", default.strip(), flags=re.IGNORECASE):
        return default
    return bare


# MySQL 整数类型的别名 -> 宽度：https://dev.mysql.com/doc/refman/8.0/en/other-vendor-data-types.html
# bool、boolean 即 tinyint(1)，可以保存 -128 ~ 127 的任意整数（例如 2），按 tinyint 的取值范围转换，不转为目标数据库的布尔类型；
# MySQL 只接受不带属性的 bool、boolean，bool signed / unsigned / zerofill、bool(1) 均为语法错误
INTEGER_ALIASES = {
    "integer": "int",
    "int1": "tinyint",
    "int2": "smallint",
    "int3": "mediumint",
    "middleint": "mediumint",
    "int4": "int",
    "int8": "bigint",
    "bool": "tinyint",
    "boolean": "tinyint",
}
# MySQL 浮点数类型的别名：float4 即 float，float8 即 double，参数与修饰的规则相同
FLOAT_ALIASES = {"float4": "float", "float8": "double"}


def integer_type(type: str) -> Optional[Tuple[str, bool]]:
    """识别 MySQL 整数类型，用于选择能容纳其取值范围的目标类型

    类型名为 tinyint、smallint、mediumint、int、bigint 或其别名（见 INTEGER_ALIASES），后面只能跟 signed、unsigned、zerofill；
    zerofill 隐含 unsigned。显示宽度（例如 int(11)、tinyint(1)）在 size 中，不影响取值范围。
    signed 与 unsigned、zerofill 同时出现，或出现其它单词时，不按整数类型处理；bool、boolean 带属性时同样不处理

    Args:
        type (str): 解析得到的列类型，例如 int unsigned zerofill

    Returns:
        (宽度, 是否无符号)，例如 ("int", True)；不是整数类型时返回 None
    """
    words = type.lower().split()
    if not words or (words[0] in ("bool", "boolean") and len(words) > 1):
        return None
    width = INTEGER_ALIASES.get(words[0], words[0])
    options = set(words[1:])
    if width not in ("tinyint", "smallint", "mediumint", "int", "bigint") \
            or not options <= {"signed", "unsigned", "zerofill"}:
        return None
    unsigned = bool(options & {"unsigned", "zerofill"})
    if unsigned and "signed" in options:
        return None
    return width, unsigned


def numeric_size(size) -> tuple:
    """将解析得到的长度统一为元组：None 为 ()，单个数字 M 为 (M,)，列表、元组保持各项"""
    if size is None:
        return ()
    if isinstance(size, int):
        return (size,)
    return tuple(size)


def decimal_type(type: str, size) -> Optional[Tuple[int, int]]:
    """识别 MySQL 定点数类型，返回补全后的精度与小数位数

    类型名为 decimal、dec、numeric、fixed，后面只能跟 signed、unsigned、zerofill（unsigned、zerofill 只禁止负数，不扩大取值范围）；
    signed 与 unsigned、zerofill 同时出现时不支持。
    不带参数时为 (10,0)，只带 M 时为 (M,0)；M 与 D 均为 0（例如 decimal(0)、decimal(0,0)）时与 MySQL 一致按 (10,0) 处理，
    M 为 0 而 D 不为 0（例如 decimal(0,2)）时 D 大于 M，MySQL 同样拒绝。M 取 1 ~ 65，D 取 0 ~ 30，且 D 不大于 M

    Args:
        type (str): 解析得到的列类型，例如 decimal unsigned
        size: 解析得到的长度，例如 None、12、(12, 4)

    Returns:
        (M, D)，例如 (10, 0)；不是定点数类型时返回 None

    Raises:
        ValueError: 定点数类型的修饰或精度不合法
    """
    words = type.lower().split()
    if not words or words[0] not in ("decimal", "dec", "numeric", "fixed"):
        return None
    options = set(words[1:])
    if not options <= {"signed", "unsigned", "zerofill"} or ("signed" in options and options & {"unsigned", "zerofill"}):
        raise ValueError(f"不支持的定点数类型: {type}")
    values = numeric_size(size)
    if len(values) > 2 or not all(isinstance(value, int) for value in values):
        raise ValueError(f"不支持的定点数类型: {type}{size}")
    precision, scale = values + (10, 0)[len(values):]
    if precision == 0 and scale == 0:
        precision = 10
    if not 1 <= precision <= 65 or not 0 <= scale <= 30 or scale > precision:
        raise ValueError(f"定点数精度超出 MySQL 范围: {type}({precision},{scale})")
    return precision, scale


def float_type(type: str, size) -> Optional[str]:
    """识别 MySQL 浮点数类型，返回 float（单精度）或 double（双精度）

    类型名为 float、double、double precision、real 或其别名（见 FLOAT_ALIASES），后面只能跟 signed、unsigned、zerofill（unsigned、zerofill 只禁止负数）；
    signed 与 unsigned、zerofill 同时出现时不支持。float(p) 的 p 取 0 ~ 24 为单精度，25 ~ 53 为双精度；real 按 MySQL 默认的 SQL 模式为双精度，不支持 REAL_AS_FLOAT 模式。
    已废弃的 float(M,D)、double(M,D) 会按 D 位小数舍入，目标数据库的浮点类型无法保持，与 double(p)、real(p) 一样不支持

    Args:
        type (str): 解析得到的列类型，例如 double precision
        size: 解析得到的长度，例如 None、24

    Returns:
        float 或 double；不是浮点数类型时返回 None

    Raises:
        ValueError: 浮点数类型的修饰或参数不合法
    """
    words = type.lower().split()
    if not words:
        return None
    # 先将别名规范为标准写法，float8 precision 与 double precision 相同
    words[0] = FLOAT_ALIASES.get(words[0], words[0])
    precision_words = words[0] == "double" and words[1:2] == ["precision"]
    if words[0] not in ("float", "double", "real"):
        return None
    options = set(words[2:] if precision_words else words[1:])
    if not options <= {"signed", "unsigned", "zerofill"} or ("signed" in options and options & {"unsigned", "zerofill"}):
        raise ValueError(f"不支持的浮点数类型: {type}")
    values = numeric_size(size)
    if not values:
        return "float" if words[0] == "float" else "double"
    if words[0] != "float" or len(values) != 1 or not isinstance(values[0], int) or not 0 <= values[0] <= 53:
        raise ValueError(f"不支持的浮点数类型: {type}{size}")
    return "float" if values[0] <= 24 else "double"


# 不带引号的 TRUE、FALSE（不区分大小写），MySQL 中即 1、0
BOOLEAN_LITERAL_PATTERN = re.compile(r"\s*(TRUE|FALSE)\s*", flags=re.IGNORECASE)


def boolean_value(value) -> Optional[bool]:
    """识别默认值或 INSERT 中完整的 TRUE、FALSE 字面量

    只识别不带引号的 TRUE、FALSE（含解析器返回的 Python bool）；'TRUE' 等字符串、TRUE + 1 等表达式不识别

    Args:
        value: 默认值或 INSERT 中的值

    Returns:
        True 或 False；不是 TRUE、FALSE 字面量时返回 None
    """
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        match = BOOLEAN_LITERAL_PATTERN.fullmatch(value)
        if match:
            return match.group(1).lower() == "true"
    return None


# MySQL 默认 SQL 模式下字符串中的转义：\0 为 NUL（不支持），\% \_ 保留反斜杠（LIKE 通配符），其它未列出的 \x 即 x
MYSQL_STRING_ESCAPES = {"'": "'", '"': '"', "b": "\b", "n": "\n", "r": "\r", "t": "\t", "Z": "\x1a",
                        "\\": "\\", "%": "\\%", "_": "\\_"}
# 字符串中的转义与双写的引号；只有与外层引号相同的双写引号为一个引号，见 decode_mysql_string
MYSQL_STRING_ESCAPE_PATTERN = re.compile(r"\\([\s\S])|''|\"\"")
# 默认值、INSERT 中完整的字符串：可带前缀（见 STRING_PREFIX），分组 1 为前缀（没有时为 None），2 为字符串
DEFAULT_STRING_PATTERN = re.compile(r"(?:(" + STRING_PREFIX + "))?(" + SQL_STRING + ")")
# 与源文件的 UTF-8 编码一致、解码后与普通字符串相同的字符集引导符（小写）；其中 utf8、utf8mb3 与 N、n 前缀的国家字符集
# 均为 utf8mb3，只能保存基本多文种平面的字符
UTF8_INTRODUCERS = {"_utf8mb4", "_utf8mb3", "_utf8"}
UTF8MB3_PREFIXES = {"n", "_utf8mb3", "_utf8"}
# 系统变量名 sql_mode，可用反引号引用，例如 `sql_mode`（不区分大小写）
SQL_MODE_NAME = r"(?:SQL_MODE|`SQL_MODE`)"
# 系统变量 @@sql_mode 的引用：作用域只能是不带引号的关键字，作用域、点、变量名之间可有空白（注释已替换为空白），
# 例如 @@sql_mode、@@SESSION.`sql_mode`、@@SESSION . sql_mode；{0} 为允许的作用域
SQL_MODE_REFERENCE = r"@@(?:(?:{0})\s*\.\s*)?" + SQL_MODE_NAME
# SET 语句中赋值给 sql_mode 的目标（字符串已替换为 \x00、注释已替换为空白、可执行注释 /*! */ 已展开），
# 例如 sql_mode、`sql_mode`、SESSION `sql_mode`、@@global.sql_mode；@sql_mode、@`sql_mode`、@old_sql_mode 等用户变量不是 SQL 模式
SQL_MODE_TARGET_PATTERN = re.compile(
    r"\s*(?:(?:(?:GLOBAL|SESSION|LOCAL|PERSIST|PERSIST_ONLY)\s+)?" + SQL_MODE_NAME
    + "|" + SQL_MODE_REFERENCE.format("GLOBAL|SESSION|LOCAL|PERSIST|PERSIST_ONLY") + r")\s*:?=",
    flags=re.IGNORECASE,
)
# 以空串替换当前 sql_mode 中的模式名：REPLACE(@@sql_mode, '...', '')，两个参数为字符串（已替换为 \x00）；
# 系统变量引用与 SQL_MODE_TARGET_PATTERN 一致，可带反引号、空白
SQL_MODE_REMOVE_PATTERN = re.compile(
    r"\s*REPLACE\s*\(\s*" + SQL_MODE_REFERENCE.format("GLOBAL|SESSION|LOCAL") + r"\s*,\s*\x00\s*,\s*\x00\s*\)\s*",
    flags=re.IGNORECASE)
# 生成的目标 SQL 中的标准字符串（反斜杠不是转义符）与行注释，整理空行时跳过字符串
OUTPUT_TOKEN_PATTERN = re.compile(r"'(?:''|[^'])*'|--[^\n]*")


def decode_mysql_string(token: str) -> str:
    """按 MySQL 默认 SQL 模式解码完整的单引号或双引号字符串，返回字符串的值；每个字符串只解码一次

    只有与外层引号相同的双写引号为一个引号：单引号字符串中的 "" 、双引号字符串中的 '' 是两个字符，保持原样

    Args:
        token (str): 完整的单引号或双引号字符串，例如 'it\\'s'、"a""b"

    Returns:
        字符串的值

    Raises:
        ValueError: 包含 NUL 字符，目标数据库的脚本无法保持
    """

    def replace(match) -> str:
        char = match.group(1)
        if char is None:
            return token[0] if match.group() == token[0] * 2 else match.group()
        if char == "0":
            raise ValueError(f"不支持包含 NUL（\\0）字符的字符串: {token[:64]}")
        return MYSQL_STRING_ESCAPES.get(char, char)

    value = MYSQL_STRING_ESCAPE_PATTERN.sub(replace, token[1:-1])
    if "\x00" in value:
        raise ValueError(f"不支持包含 NUL 字符的字符串: {token[:64]}")
    return value


def decode_mysql_literal(prefix: Optional[str], token: str) -> str:
    """按 MySQL 默认 SQL 模式解码带前缀的完整单引号字符串，返回字符串的值，见 decode_mysql_string

    1. 没有前缀，或 N、n 前缀，或 _utf8mb4、_utf8mb3、_utf8 引导符（不区分大小写，与字符串之间可有空白）：与源文件的 UTF-8 编码一致，
       引导符只声明字符集，解码后与普通字符串相同
    2. b'0'、b'1'（位值）：与 clean 中的 b'0'、b'1' 一致，为字符串 '0'、'1'

    Args:
        prefix (Optional[str]): 字符串前的前缀（见 STRING_PREFIX），没有时为 None
        token (str): 完整的单引号字符串，例如 'it\\'s'

    Returns:
        字符串的值

    Raises:
        ValueError: 其它字符集引导符（例如 _binary、_latin1）按其它字符集解释源文件的字节，十六进制、其它位值字符串为二进制数据，
            目标数据库的字符串无法保持；utf8mb3 的字符串包含增补字符（例如 emoji）；包含 NUL 字符
    """
    name = (prefix or "").strip().lower()
    if name == "b" and token in ("'0'", "'1'"):
        return token[1:-1]
    if name in ("x", "b"):
        raise ValueError(f"不支持十六进制、位值字符串: {prefix}{token[:64]}")
    if name.startswith("_") and name not in UTF8_INTRODUCERS:
        raise ValueError(f"不支持字符集引导符 {prefix.strip()} 的字符串: {prefix.strip()}{token[:64]}")
    value = decode_mysql_string(token)
    if name in UTF8MB3_PREFIXES and any(ord(char) > 0xFFFF for char in value):
        raise ValueError(f"utf8mb3 的字符串不支持增补字符（例如 emoji）: {prefix.strip()}{token[:64]}")
    return value


def quote_sql_string(value: str) -> str:
    """生成标准 SQL 字符串：单引号双写，反斜杠、换行等字符保持原样"""
    return "'" + value.replace("'", "''") + "'"


def rewrite_strings(sql: str, convert) -> str:
    """按词法单元改写语句中完整的单引号字符串，字符串外的内容、注释、双引号与反引号内容保持原样

    字符串前的前缀（见 STRING_PREFIX，前面不是标识符），例如 N''、_utf8mb4''、x''，与字符串一起由 convert 的结果替代，
    是否支持由 convert 判断；前面紧跟其它字母、数字的字符串保持原样

    Args:
        sql (str): SQL 语句
        convert: (前缀（没有时为 None）, 完整的单引号字符串) -> 目标字面量

    Returns:
        改写后的语句
    """
    parts, start, gap_start = [], 0, 0
    for match in SQL_TOKEN_PATTERN.finditer(sql):
        # gap：与上一个词法单元之间的文本，前缀只能在其中
        gap = sql[gap_start:match.start()]
        gap_start = match.end()
        if not re.fullmatch(SQL_STRING, match.group()):
            continue
        begin, prefix = match.start(), STRING_PREFIX_PATTERN.search(gap)
        if prefix:
            begin -= len(gap) - prefix.start()
        elif gap and re.match(r"[\w$]", gap[-1]):
            continue
        parts.append(sql[start:begin])
        parts.append(convert(prefix.group() if prefix else None, match.group()))
        start = match.end()
    parts.append(sql[start:])
    return "".join(parts)


def check_sql_mode(content: str) -> None:
    """源 SQL 中修改 sql_mode 启用 NO_BACKSLASH_ESCAPES 时明确失败：反斜杠不再是转义符，按默认模式解码字符串会改变数据

    按词法单元识别 SET 语句，字符串、注释中的同名文本不识别；注释按空白分隔前后的词（例如 SET/**/SQL_MODE），
    MySQL 可执行注释（/*! ... */）中的语句同样识别。只检查赋值给 sql_mode 的表达式，同一语句中其它变量的值不检查；
    sql_mode 目标与 @@sql_mode 引用可带反引号，作用域、点、变量名之间可有空白或注释，见 SQL_MODE_TARGET_PATTERN；
    REPLACE(@@sql_mode, 'NO_BACKSLASH_ESCAPES', '')（模式名不区分大小写）以空串替换，不会新增该模式，不视为启用；
    MySQL REPLACE 区分大小写，这里不判断是否真正移除了已有的模式。不解释其它表达式，例如通过用户变量间接赋值不识别

    Args:
        content (str): 源 SQL 内容

    Raises:
        ValueError: 启用了 NO_BACKSLASH_ESCAPES
    """
    # code：字符串替换为 \x00 的语句；strings：按顺序的原始字符串
    code, strings = [], []

    def check(assignment: str, values: List[str]):
        target = SQL_MODE_TARGET_PATTERN.match(assignment)
        if not target:
            return
        expression = assignment[target.end():]
        tokens = iter(values)
        if "NO_BACKSLASH_ESCAPES" not in re.sub("\x00", lambda _: next(tokens), expression).upper():
            return
        # 以空串替换当前 sql_mode 中的子串不会新增模式（当前模式按默认模式假设未启用 NO_BACKSLASH_ESCAPES）；
        # MySQL REPLACE 区分大小写，小写的模式名不一定能移除已有的大写模式，这里只判断不会因此启用，模式名不区分大小写
        if SQL_MODE_REMOVE_PATTERN.fullmatch(expression) and all(value[0] == value[-1] for value in values) \
                and [value[1:-1].upper() for value in values] == ["NO_BACKSLASH_ESCAPES", ""]:
            return
        raise ValueError("不支持启用 NO_BACKSLASH_ESCAPES 的 SQL：字符串中的反斜杠不是转义符")

    def flush():
        statement = "".join(code)
        keyword = re.match(r"\s*SET\b", statement, flags=re.IGNORECASE)
        if keyword:
            # 按括号外的逗号拆分各个赋值
            rest = statement[keyword.end():]
            depth, start, offset = 0, 0, 0
            for position, char in enumerate(rest):
                if char == "(":
                    depth += 1
                elif char == ")":
                    depth -= 1
                elif char == "," and depth <= 0:
                    count = rest.count("\x00", start, position)
                    check(rest[start:position], strings[offset:offset + count])
                    start, offset = position + 1, offset + count
            check(rest[start:], strings[offset:])
        code.clear()
        strings.clear()

    def visit(sql: str):
        start = 0
        for match in SQL_TOKEN_PATTERN.finditer(sql):
            code.append(sql[start:match.start()].replace("\x00", ""))
            start = match.end()
            token = match.group()
            if token == ";":
                flush()
            elif token.startswith("/*!"):
                code.append(" ")
                visit(re.sub(r"^/\*!\d*|\*/$", "", token))
                code.append(" ")
            elif token.startswith(("--", "#", "/*")):
                code.append(" ")
            elif token[0] in "'\"":
                code.append("\x00")
                strings.append(token)
            else:
                code.append(token.replace("\x00", ""))
        code.append(sql[start:].replace("\x00", ""))

    visit(content)
    flush()


def compact_blank_lines(script: str) -> str:
    """连续的空行压缩为一个，字符串内的换行保持原样"""
    parts, start = [], 0
    for match in OUTPUT_TOKEN_PATTERN.finditer(script):
        parts.append(re.sub("\n{3,}", "\n\n", script[start:match.start()]))
        parts.append(match.group())
        start = match.end()
    parts.append(re.sub("\n{3,}", "\n\n", script[start:]))
    return "".join(parts)


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


def check_delimiter(content: str) -> None:
    """源 SQL 中有 mysql 客户端的 DELIMITER 命令（存储过程、触发器等）时明确失败

    DELIMITER 之后的语句不按分号结束，按分号拆分会把存储过程中的 INSERT（创建时并不执行）误当作数据；
    不解析存储过程，整个文件不转换。只识别语句开头（见 split_statements，开头的空白、注释已去掉）的 DELIMITER、\\d 命令，
    字符串、注释、标识符中的同名文本不识别

    Args:
        content (str): 源 SQL 内容

    Raises:
        ValueError: 有 DELIMITER 命令
    """
    # 末尾缺少分号的命令同样识别，见 insert_statements
    for statement in split_statements(content + "\n;"):
        if re.match(r"(?:DELIMITER|\\d)(?=\s|;)", statement, flags=re.IGNORECASE):
            raise ValueError(f"不支持包含 DELIMITER 命令（存储过程、触发器等）的 SQL，整个文件不转换: {statement[:64]}")


def strip_insert_comments(statement: str) -> str:
    """去掉 INSERT 语句头部、尾部的普通注释（--、#、/* */），值保持原样

    头部为第一行值的左括号之前（含列清单、VALUES 前后），注释替换为空格，列清单可按列对应；
    尾部为最后的值之后、结尾的分号之前，注释去掉，避免行注释吞掉结尾的分号，id 照常计入序列的起始值。
    值中（各行之间、值之间）的注释保持原样，由 convert_insert_values 整句回退。
    按词法单元识别，字符串、标识符中注释样式的文本不受影响

    Args:
        statement (str): 完整的 INSERT 语句，以分号结尾，见 split_statements

    Returns:
        去掉头部、尾部注释的语句

    Raises:
        ValueError: 头部、尾部有 MySQL 可执行注释（/*! ... */），去掉会改变语句的含义
    """
    tokens = list(SQL_TOKEN_PATTERN.finditer(statement))
    # head：第一行值的左括号的位置（VALUES 关键字之后），没有 VALUES 时整条语句为头部；
    # tail：最后一个不是空白、注释、分号的内容的结束位置
    head, tail, values, start = len(statement), 0, False, 0
    for match in tokens + [None]:
        end = match.start() if match else len(statement)
        gap = statement[start:end]
        if head == len(statement):
            keyword = None if values else re.search(r"(?<![\w$])VALUES(?![\w$])", gap, flags=re.IGNORECASE)
            values = values or keyword is not None
            bracket = gap.find("(", keyword.end() if keyword else 0) if values else -1
            if bracket >= 0:
                head = start + bracket
        if gap.strip():
            tail = start + len(gap.rstrip())
        if match is None:
            break
        if not match.group().startswith(("--", "#", "/*")) and match.group() != ";":
            tail = match.end()
        start = match.end()

    parts, start = [], 0
    for match in tokens:
        token = match.group()
        if not token.startswith(("--", "#", "/*")) or head <= match.start() < tail:
            continue
        if token.startswith("/*!"):
            raise ValueError(f"不支持 INSERT 语句头部、尾部的 MySQL 可执行注释: {statement[:64]}")
        parts.append(statement[start:match.start()])
        # 头部的注释可能是列名、关键字之间唯一的分隔，替换为空格
        parts.append(" " if match.start() < head else "")
        start = match.end()
    parts.append(statement[start:])
    return "".join(parts)


def protect_default_numbers(table_sql: str) -> Tuple[str, Dict[str, str]]:
    """将建表语句中以小数点开头的数值默认值（例如 DEFAULT .5）替换为占位标识，解析后还原为补齐整数部分的数值

    simple-ddl-parser 无法解析以小数点开头的数值，整个建表语句会解析失败。
    只替换字符串、注释、反引号标识符之外的 DEFAULT 后的完整简单数值；还原时只在小数点前补 0（.5 为 0.5，-.5 为 -0.5），
    按文本处理，不经过 Python 的 float

    Args:
        table_sql (str): 建表语句

    Returns:
        (替换后的建表语句, {占位标识: 补齐整数部分的数值})；语句中已包含占位标识前缀时不替换
    """
    numbers = {}
    if DEFAULT_NUMBER_PLACEHOLDER.format("") in table_sql.lower():
        return table_sql, numbers

    def replace(match) -> str:
        placeholder = DEFAULT_NUMBER_PLACEHOLDER.format(len(numbers))
        numbers[placeholder] = re.sub(r"^([+-]?)\.", r"\g<1>0.", match.group(2))
        return match.group(1) + placeholder

    parts, start = [], 0
    for token in SQL_TOKEN_PATTERN.finditer(table_sql):
        parts.append(LEADING_DOT_DEFAULT_PATTERN.sub(replace, table_sql[start:token.start()]))
        parts.append(token.group())
        start = token.end()
    parts.append(LEADING_DOT_DEFAULT_PATTERN.sub(replace, table_sql[start:]))
    return "".join(parts), numbers


def protect_default_strings(table_sql: str) -> Tuple[str, Dict[str, str]]:
    """将原始建表语句中 DEFAULT 后的完整字符串（可带 N、n、x、X 前缀或字符集引导符）替换为占位标识，解析后还原为原始的字符串

    simple-ddl-parser 会改写字符串默认值（中文转为 \\uXXXX，\\n、\\' 等转为内部占位符，反斜杠翻倍），也无法解析引导符、
    括号中的表达式默认值，无法还原为 MySQL 原文；保留原文后由 decode_mysql_literal 判断前缀并只解码一次。
    括号中只有一个字符串的表达式默认值（例如 MySQL 8 text 列的 DEFAULT (_utf8mb4'x')）连同括号一起替换，还原为括号中的字符串。
    按词法单元处理：字符串、注释中的 DEFAULT 文本不识别；后面紧跟另一个字符串的拼接、括号中的其它表达式保持原样

    Args:
        table_sql (str): 原始建表语句

    Returns:
        (替换后的建表语句, {占位标识: 原始的字符串，含前缀})

    Raises:
        ValueError: 语句中已包含占位标识前缀，无法安全还原
    """
    strings = {}
    if DEFAULT_STRING_PLACEHOLDER.format("") in table_sql.lower():
        raise ValueError(f"建表语句中包含保留的占位标识 {DEFAULT_STRING_PLACEHOLDER.format('')}，无法转换")
    parts, start, gap_start = [], 0, 0
    for match in SQL_TOKEN_PATTERN.finditer(table_sql):
        gap = table_sql[gap_start:match.start()]
        gap_start = match.end()
        if not re.fullmatch(SQL_STRING, match.group()):
            continue
        default = DEFAULT_STRING_PREFIX_PATTERN.search(gap)
        if not default or re.match(r"\s*['\"]", table_sql[match.end():]):
            continue
        # literal：原始的字符串（含前缀）的开始位置；有左括号时连同括号替换到右括号为止
        offset = match.start() - len(gap)
        literal = offset + default.start(2) if default.group(2) else match.start()
        begin, end = literal, match.end()
        if default.group(1):
            close = re.match(r"\s*\)", table_sql[match.end():])
            if not close:
                continue
            begin, end = offset + default.start(1), match.end() + close.end()
        placeholder = DEFAULT_STRING_PLACEHOLDER.format(len(strings))
        strings[placeholder] = table_sql[literal:match.end()]
        parts.append(table_sql[start:begin])
        parts.append(placeholder)
        start = end
    parts.append(table_sql[start:])
    return "".join(parts), strings


def ddl_comments(table_sql: str) -> Tuple[str, Optional[str], Dict[str, str]]:
    """按词法单元提取原始建表语句中的表注释、列注释，并去掉全部 COMMENT 子句，交给 DDLParser 解析

    simple-ddl-parser 无法解析双写的引号、反斜杠转义、中文等注释内容，也会误读 SQL 注释（--、#、/* */）中的 COMMENT 文本；
    按物理行、全文的正则会被双写的引号截断，也会误识别 DEFAULT 字符串、标识符中 COMMENT 样式的文本。
    1. 第一个顶层括号中按括号外的逗号拆分定义：列定义（以反引号标识符或不是 TABLE_DEFINITION_KEYWORDS 的单词开头）中的
       COMMENT 'xxx' 为列注释；主键、索引、约束中的 COMMENT 子句同样去掉，不记录
    2. 括号之后的表选项中的 COMMENT [=] 'xxx' 为表注释，关键字不区分大小写，等号两侧可有空白
    3. COMMENT 与字符串之间可有空白、注释；字符串、反引号标识符、注释中的 COMMENT 文本不识别；同一列、表有多个 COMMENT 时取最后一个
    4. SQL 注释替换为空格；可执行注释（/*! ... */）保持原样
    5. 括号之后连续的表选项（含 COMMENT、其间的注释与分隔的逗号）整体替换为一个空格，见 table_options_end；
       DDLParser 不需要表选项，无 ENGINE、逗号分隔的表选项（例如 COMMENT='x', ROW_FORMAT=DYNAMIC）也不会残留
    MySQL 的注释只能是不带前缀的单引号或双引号字符串（默认 SQL 模式，不能带 N 前缀、字符集引导符）；
    分区定义、可执行注释中的 COMMENT 不识别

    Args:
        table_sql (str): 原始建表语句，见 table_scripts

    Returns:
        (去掉 COMMENT 子句、SQL 注释的建表语句, 表注释的原始字符串（没有时为 None）, {列名: 列注释的原始字符串})；
        原始字符串（含外层引号）由 decode_mysql_string 只解码一次
    """
    # removed：按位置顺序去掉的 (开始位置, 结束位置, 替换文本)
    removed, table_comment, column_comments = [], None, {}
    # depth：括号深度；definitions：第一个顶层括号之前为 None，其中（列、索引定义）为 True，之后（表选项）为 False；
    # head：当前定义还没有遇到第一个词；name：当前列定义的列名，主键、索引、约束为 None
    depth, definitions, head, name = 0, None, False, None
    # code：上一个不是注释的词法单元之后的文本，注释替换为等长的空白，用于识别字符串前的 COMMENT 关键字；code_start 为其开始位置
    code, code_start, gap_start = "", 0, 0
    # options_start：第一个顶层括号之后（表选项）的开始位置
    options_start = None
    for match in SQL_TOKEN_PATTERN.finditer(table_sql):
        gap_offset, gap = gap_start, table_sql[gap_start:match.start()]
        gap_start = match.end()
        for piece_match in re.finditer(r"[(),]|[^(),]+", gap):
            piece = piece_match.group()
            if piece == "(":
                depth += 1
                if depth == 1 and definitions is None:
                    definitions, head, name = True, True, None
            elif piece == ")":
                if depth == 1 and definitions:
                    definitions = False
                    options_start = gap_offset + piece_match.end()
                depth -= 1
            elif piece == "," and depth == 1 and definitions:
                head, name = True, None
            elif head and piece.strip():
                word = re.match(r"\s*([\w$]+)", piece)
                if word and word.group(1).upper() not in TABLE_DEFINITION_KEYWORDS:
                    name = word.group(1)
                head = False
        token = match.group()
        if token.startswith(("--", "#")) or (token.startswith("/*") and not token.startswith("/*!")):
            code += gap + " " * len(token)
            removed.append((match.start(), match.end(), " "))
            continue
        keyword = None
        if definitions and depth == 1:
            keyword = COLUMN_COMMENT_PATTERN.search(code + gap)
        elif definitions is False and depth == 0:
            keyword = TABLE_COMMENT_PATTERN.search(code + gap)
        if keyword and re.fullmatch(f"{SQL_STRING}|{SQL_DOUBLE_QUOTED_STRING}", token):
            if definitions is False:
                table_comment = token
            elif name:
                column_comments[name] = token
            # COMMENT 子句包含其中的 SQL 注释，整体去掉
            begin = code_start + keyword.start()
            while removed and removed[-1][0] >= begin:
                removed.pop()
            removed.append((begin, match.end(), ""))
        elif head and token.startswith("`"):
            # 与交给 DDLParser 的列名一致：去掉反引号
            name = token.replace("`", "")
        head = False
        code, code_start = "", match.end()
    if options_start is not None:
        # 连续的表选项整体替换为一个空格，其中已记录的 COMMENT 子句、注释不再单独替换
        options_end = table_options_end(table_sql, options_start)
        if options_end > options_start:
            removed = [span for span in removed if span[0] < options_start] + [(options_start, options_end, " ")] \
                + [span for span in removed if span[0] >= options_end]
    parts, start = [], 0
    for begin, end, replacement in removed:
        parts.append(table_sql[start:begin] + replacement)
        start = end
    parts.append(table_sql[start:])
    return "".join(parts), table_comment, column_comments


def table_options_end(table_sql: str, start: int) -> int:
    """获得建表语句中从 start 开始的连续表选项的结束位置

    按词法单元遮蔽后识别：注释遮蔽为等长的空白，字符串、反引号标识符的内容遮蔽为 x，位置与原文一致；
    依次识别 [DEFAULT] 名称 [=] 值（名称见 TABLE_OPTION_NAMES），第一个表选项前面只能是空白，之后可以用一个逗号分隔。
    遇到不认识的内容（分区定义、可执行注释、AS SELECT、多余的逗号等）即停止，其后的内容保持原样

    Args:
        table_sql (str): 原始建表语句
        start (int): 第一个顶层括号之后的位置，不在字符串、注释之中

    Returns:
        最后一个表选项的结束位置；没有表选项时为 start
    """
    parts, position = [], start
    for match in SQL_TOKEN_PATTERN.finditer(table_sql, start):
        parts.append(table_sql[position:match.start()])
        token = match.group()
        if token.startswith(("--", "#")) or (token.startswith("/*") and not token.startswith("/*!")):
            parts.append(" " * len(token))
        elif re.fullmatch(f"{SQL_STRING}|{SQL_DOUBLE_QUOTED_STRING}|`(?:``|[^`])*`", token):
            parts.append(token[0] + "x" * (len(token) - 2) + token[-1])
        else:
            parts.append(token)
        position = match.end()
    parts.append(table_sql[position:])
    masked, end = "".join(parts), 0
    option = FIRST_TABLE_OPTION_PATTERN.match(masked)
    while option:
        end = option.end()
        option = NEXT_TABLE_OPTION_PATTERN.match(masked, end)
    return start + end


def table_scripts(content: str) -> List[str]:
    """获得真实的 CREATE TABLE 语句，不包括 INSERT 数据、注释中的建表文本

    相关 issue：https://github.com/YunaiV/ruoyi-vue-pro/issues/1273
    """
    return [statement for statement in split_statements(content) if CREATE_TABLE_PATTERN.match(statement)]


def insert_statements(content: str) -> Dict[str, List[str]]:
    """按表名分组源 SQL 中完整的 INSERT 语句，见 split_statements

    按词法单元拆分：字符串中的换行、分号、INSERT 文本不拆分语句，语句开头的空白、注释已去掉；
    末尾缺少分号的语句与 mysql 客户端一致同样执行，补一个分号；头部、尾部的注释已去掉，见 strip_insert_comments

    Args:
        content (str): 原始的 sql 内容，不经过 clean

    Returns:
        {反引号中的表名: 按原文顺序的 INSERT 语句}
    """
    statements = {}
    for statement in split_statements(content + "\n;"):
        if re.match(r"INSERT(?![\w$])", statement, flags=re.IGNORECASE):
            statement = strip_insert_comments(statement)
        match = INSERT_TABLE_PATTERN.match(statement)
        if match:
            statements.setdefault(match.group(1), []).append(statement)
    return statements


def read_sql(sql_file: str) -> str:
    """读取源 SQL 文件

    utf-8-sig：只去掉文件开头的 BOM，避免首条语句无法识别；
    与 mysql 客户端（非 binary 模式）一致，只将行尾的 \\r\\n 视为换行，字符串中单独的 \\r 是数据，保持原样
    """
    with open(sql_file, encoding="utf-8-sig", newline="") as f:
        return f.read().replace("\r\n", "\n")


def load_and_clean(sql_file: str) -> str:
    """加载源 SQL 文件，并清理内容方便下一步 ddl 解析

    Args:
        sql_file (str): sql文件路径

    Returns:
        str: 清理后的sql文件内容
    """
    return clean(read_sql(sql_file))


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
    # ENGINE 等表选项已由 ddl_comments 按词法单元去掉，不再按全文正则删除（会截断字符串中 ENGINE 样式的数据）
    return content


class Convertor(ABC):
    # 不同数据库的关键字不完全一致；子类按需声明需要转义的列名。
    reserved_column_names = set()
    # 单个位的 bit 列中 TRUE、FALSE 对应的字面量；目标类型为 bool 的数据库使用 TRUE、FALSE
    boolean_bit_literals = ("1", "0")
    # varchar 按字符计算长度的标量类型的最大长度：超出时改用 long_string_type，None 为不限制；有索引的列超出时明确失败
    max_varchar_length = None
    long_string_type = None
    # 行长度的保守估算上限（字节）：超出时将最长的无索引 varchar 改用 long_string_type，None 为不限制
    max_row_bytes = None

    def __init__(self, src: str, db_type) -> None:
        self.src = src
        self.db_type = db_type
        self.content = load_and_clean(self.src)
        # original_content 保留原始 COMMENT 信息，用于注释提取
        self.original_content = read_sql(src)
        # 存储过程、触发器等 DELIMITER 命令之后的语句无法按分号拆分，明确失败，见 check_delimiter
        check_delimiter(self.original_content)
        # 字符串按 MySQL 默认 SQL 模式解码，启用 NO_BACKSLASH_ESCAPES 时明确失败
        check_sql_mode(self.original_content)
        # 表名 -> INSERT 语句：在原始内容上按词法单元拆分，clean 的全局替换不能改写字符串中的数据，见 insert_statements
        self.insert_scripts = insert_statements(self.original_content)
        # 在原始内容上扫描建表语句，避免全局清理破坏引号边界；每个建表语句再单独清理
        self.original_table_script_list = table_scripts(self.original_content)
        # 按词法单元剥离表、列、索引的 COMMENT 子句及 SQL 注释后再清理，交给 DDLParser 解析，见 ddl_comments
        self.table_script_list = [clean(ddl_comments(table_sql)[0]) for table_sql in self.original_table_script_list]
        # 表名 -> {列名: 需要按列类型转换字面量的类别（见 literal_kind），其它列为 None}，
        # 按建表的列顺序；gen_create 时记录，gen_insert 时使用
        self.table_columns = {}
        # 表名 -> 改用 long_string_type 的 varchar 列名，gen_create 时记录，见 register_string_columns
        self.long_string_columns = {}

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

    def translate_numeric_type(self, type: str, size) -> Optional[str]:
        """定点数、浮点数类型转换，使用子类声明的 max_decimal_precision、decimal_format、float_types

        定点数超出目标数据库的最大精度时明确失败，不截断，也不改用浮点数

        Args:
            type (str): 解析得到的列类型
            size: 解析得到的长度

        Returns:
            目标类型；不是定点数、浮点数类型时返回 None

        Raises:
            ValueError: 类型不合法，或超出目标数据库的最大精度
        """
        decimal = decimal_type(type, size)
        if decimal:
            precision, scale = decimal
            if precision > self.max_decimal_precision:
                raise ValueError(f"{self.db_type} 转换暂不支持 {type}({precision},{scale})，"
                                 f"最大精度为 {self.max_decimal_precision}")
            return self.decimal_format.format(precision, scale)
        floating = float_type(type, size)
        if floating:
            return self.float_types[floating]
        return None

    def literal_kind(self, type: str, size) -> Optional[str]:
        """列的字面量类别，用于按列类型转换默认值、INSERT 中的字面量

        Args:
            type (str): 解析得到的列类型
            size: 解析得到的长度

        Returns:
            bit：单个位的 bit（bit、bit(1)）；number：整数、定点数、浮点数；string：字符串、文本；其它列返回 None
        """
        type = type.lower()
        if type == "bit":
            # 多个位的位串不是布尔值，不转换
            return "bit" if size in (None, 1) else None
        if integer_type(type) or decimal_type(type, size) or float_type(type, size):
            return "number"
        if type in ("varchar", "char", "tinytext", "text", "mediumtext", "longtext"):
            return "string"
        return None

    def register_literal_columns(self, ddl: Dict) -> None:
        """记录表中改用 long_string_type 的 varchar 列（见 register_string_columns）与各列的字面量类别，
        供 column_type、convert_default、convert_insert_values 使用；各 gen_create 开始时调用，重复生成时刷新"""
        self.register_string_columns(ddl)
        self.table_columns[ddl["table_name"].lower()] = {
            column["name"].lower(): self.column_literal_kind(column, ddl["table_name"]) for column in ddl["columns"]}

    @staticmethod
    def indexed_columns(ddl: Dict) -> set:
        """表中有主键、唯一索引、普通索引的列名（小写）；有 id 列时 gen_pk 以 id 为主键"""
        names = {str(name).strip("`").lower() for name in ddl.get("primary_key") or []}
        for column in ddl["columns"]:
            if column["name"].lower() == "id" or column.get("unique"):
                names.add(column["name"].lower())
        keys = [key for index in ddl.get("index", []) for key in Convertor.index_columns(index.get("columns", []))]
        keys += [key for _, _, uk_columns in Convertor.unique_index(ddl) for key in uk_columns]
        names.update(key.split()[0] for key in keys)
        return names

    def register_string_columns(self, ddl: Dict) -> None:
        """记录表中改用 long_string_type 的 varchar 列，不截断长度、不删除约束

        1. 超过 max_varchar_length 个字符的 varchar 改用 long_string_type
        2. 设置了 max_row_bytes 时，按 row_bytes 保守估算行长度，超出时依次将最长的无索引 varchar 改用 long_string_type

        Raises:
            ValueError: 有主键、索引的列需要改用 long_string_type，或没有可改用的列而行长度仍超出
        """
        table_name = ddl["table_name"].lower()
        long_columns = set()
        self.long_string_columns[table_name] = long_columns
        if self.long_string_type is None:
            return
        indexed = self.indexed_columns(ddl)
        sizes = {column["name"].lower(): column["size"] or 0
                 for column in ddl["columns"] if column["type"].lower() == "varchar"}
        for name, size in sizes.items():
            if self.max_varchar_length is not None and size > self.max_varchar_length:
                if name in indexed:
                    raise ValueError(f"{self.db_type} 转换不支持表 {table_name} 的列 {name} varchar({size})：超过 "
                                     f"{self.max_varchar_length} 个字符需要改用 {self.long_string_type}，但该列有主键或索引")
                long_columns.add(name)
        if self.max_row_bytes is None:
            return
        while self.row_bytes(ddl, long_columns) > self.max_row_bytes:
            candidates = [name for name in sizes if name not in long_columns and name not in indexed]
            if not candidates:
                raise ValueError(f"{self.db_type} 转换不支持表 {table_name}：估算行长度超过 {self.max_row_bytes} 字节，"
                                 f"且没有可改用 {self.long_string_type} 的无索引 varchar 列")
            long_columns.add(max(candidates, key=lambda name: sizes[name]))

    def row_bytes(self, ddl: Dict, long_columns: set) -> int:
        """保守估算表的行长度（字节），设置了 max_row_bytes 的子类实现"""
        raise NotImplementedError

    def column_type(self, table_name: str, column: Dict) -> Optional[str]:
        """列的目标类型：改用 long_string_type 的 varchar 列（见 register_string_columns）为 long_string_type，其它见 translate_type"""
        if column["name"].lower() in self.long_string_columns.get(table_name.lower(), ()):
            return self.long_string_type
        return self.translate_type(column["type"].lower(), column["size"])

    def string_literal(self, value: str) -> str:
        """解码后的字符串值 -> 目标数据库的字符串字面量，默认为标准 SQL 字符串，子类按需覆盖"""
        return quote_sql_string(value)

    def convert_strings(self, sql: str) -> str:
        """语句中完整的 MySQL 单引号字符串（含前缀）只解码一次，转为目标数据库的字符串字面量，
        见 rewrite_strings、decode_mysql_literal、string_literal"""
        return rewrite_strings(sql, lambda prefix, token: self.string_literal(decode_mysql_literal(prefix, token)))

    def convert_string_default(self, default):
        """完整的 MySQL 字符串默认值（可带前缀，见 decode_mysql_literal）转为目标数据库的字符串字面量；其它默认值保持原样"""
        if isinstance(default, str):
            match = DEFAULT_STRING_PATTERN.fullmatch(default.strip())
            if match:
                return self.string_literal(decode_mysql_literal(match.group(1), match.group(2)))
        return default

    def column_literal_kind(self, column: Dict, table_name: Optional[str] = None) -> Optional[str]:
        """建表语句中列的字面量类别，见 literal_kind；deleted 列各目标数据库均为数值或 bit，按数值处理

        Args:
            column (Dict): 列 DDL
            table_name (Optional[str]): 表名，子类按改用 long_string_type 的列区分类别时使用
        """
        kind = self.literal_kind(column["type"], column["size"])
        if kind and column["name"].lower() == "deleted":
            return "number"
        return kind

    def convert_literal(self, kind: Optional[str], value) -> Optional[str]:
        """按列的字面量类别转换默认值、INSERT 中的单个字面量

        1. 字符串列中完整的 MySQL 字符串（可带前缀，见 decode_mysql_literal）只解码一次，转为 string_literal
        2. 不带引号的 TRUE、FALSE 在 MySQL 中即 1、0：数值列转为 1、0，单个位的 bit 列转为 boolean_bit_literals，
           字符串列与 MySQL 一致保存为文本 '1'、'0'
        3. 'TRUE' 等字符串、表达式、其它列保持原样

        Args:
            kind (Optional[str]): 列的字面量类别，见 literal_kind
            value: 默认值，或 INSERT 中去掉前后空白的值

        Returns:
            转换后的字面量；不需要转换时返回 None
        """
        if kind == "string" and isinstance(value, str):
            match = DEFAULT_STRING_PATTERN.fullmatch(value.strip())
            if match:
                return self.string_literal(decode_mysql_literal(match.group(1), match.group(2)))
        boolean = boolean_value(value)
        if boolean is None or kind not in ("number", "bit", "string"):
            return None
        if kind == "bit":
            return self.boolean_bit_literals[0 if boolean else 1]
        if kind == "string":
            return self.string_literal("1" if boolean else "0")
        return "1" if boolean else "0"

    def convert_insert_values(self, table_name: str, insert_script: str) -> str:
        """按列类型转换 INSERT 中的字面量，见 convert_literal

        只转换值恰好为单个字面量的值；函数参数、表达式等保持原样。
        没有列清单时按建表的列顺序对应；列名未知、列数不一致、字符串外的注释、无法识别的语法，整条语句保持原样
        """
        rows = self.insert_values(table_name, insert_script)
        if rows is None:
            return self.convert_strings(insert_script)
        match, tokens, values = rows
        columns = self.table_columns[table_name.lower()]
        replacements = {}
        for name, value in values:
            raw = "".join(tokens[i] for i in value)
            text = raw.strip()
            kind = columns[name]
            literal = self.convert_literal(kind, text) if kind and text else None
            if literal is not None:
                # 值可能带有前后空白，保留空白，只替换字面量本身（含字符串的 N、n 前缀）
                begin = raw.index(text)
                replacements[value[0]] = raw[:begin] + literal + raw[begin + len(text):]
                replacements.update((i, "") for i in value[1:])
        # 已按列类型转换的值原样输出；其它连续的文本中的字符串只解码一次，见 convert_strings
        parts, rest = [match.group(1)], []
        for i, token in enumerate(tokens):
            if i in replacements:
                parts.append(self.convert_strings("".join(rest)))
                parts.append(replacements[i])
                rest.clear()
            else:
                rest.append(token)
        parts.append(self.convert_strings("".join(rest)))
        return "".join(parts)

    def insert_values(self, table_name: str, insert_script: str) -> Optional[Tuple]:
        """将 INSERT 中每一行的每个值对应到列，供 convert_insert_values、next_sequence_value 使用

        没有列清单时按建表的列顺序对应；先确认整条语句每一行的结构、列数均有效，结构无效时不返回任何一行

        Args:
            table_name (str): 表名
            insert_script (str): INSERT 语句，见 Convertor.inserts

        Returns:
            (INSERT_VALUES_PATTERN 的匹配, 值的词法单元, 按行、列顺序的 [(列名, 值的词法单元下标)])；
            列名未知、列数不一致、空值、字符串外的注释、无法识别的语法时返回 None
        """
        columns = self.table_columns.get(table_name.lower())
        match = INSERT_VALUES_PATTERN.match(insert_script)
        if not columns or not match:
            return None
        if match.group(2) is None:
            names = list(columns)
        else:
            names = [name.strip().lower() for name in match.group(2).split(",")]
        if any(name not in columns for name in names):
            return None
        # 字符串外的注释：其中的逗号、括号会干扰按列对应；字符串内注释样式的文本不受影响
        if any(token.startswith(("--", "#", "/*")) for token in SQL_TOKEN_PATTERN.findall(match.group(3))):
            return None

        tokens = INSERT_VALUE_TOKEN_PATTERN.findall(match.group(3))
        # 记录 (列名, 值的词法单元下标)；结构无效时整条语句不按列处理，不对前面的行调用可能明确失败的 convert_literal
        values = []
        # state：括号外期望的下一个词法单元，row 为行，separator 为行之间的逗号或结尾的分号，end 为分号之后
        depth, index, value, state = 0, 0, [], "row"

        def finish_value() -> bool:
            # 当前值结束，返回列数是否仍匹配；空值、仅空白的值为无效结构
            nonlocal index
            if index >= len(names) or not "".join(tokens[i] for i in value).strip():
                return False
            values.append((names[index], list(value)))
            index += 1
            value.clear()
            return True

        for i, token in enumerate(tokens):
            # 未闭合的单引号，或值中出现双引号、反引号：无法安全识别
            if token in ("'", '"', "`"):
                return None
            if depth == 0:
                if token == "(" and state == "row":
                    depth, index = 1, 0
                elif token == "," and state == "separator":
                    state = "row"
                elif token == ";" and state == "separator":
                    state = "end"
                elif token.strip():
                    return None
            elif depth == 1 and token == ",":
                if not finish_value():
                    return None
            elif depth == 1 and token == ")":
                if not finish_value() or index != len(names):
                    return None
                depth, state = 0, "separator"
            else:
                if token == "(":
                    depth += 1
                elif token == ")":
                    depth -= 1
                value.append(i)
        # 没有行、以逗号结尾、括号未闭合：无效结构
        if depth != 0 or state == "row":
            return None
        return match, tokens, values

    def max_explicit_id(self, table_name: str, insert_scripts: List[str], unchecked: str) -> int:
        """全部 INSERT 中全部行的显式 id 的最大值（不小于 0），同时校验 id：各目标数据库均保持原 id 导入，只支持显式的整数 id

        id 按列清单（没有列清单时按建表的列顺序）对应，见 insert_values，与行、语句的顺序无关；
        字符串中 id 样式的文本、其它列的值不影响。id 列不是数值列（例如字符串编号）、没有 id 列时不校验，为 0。
        无法按列对应的语句（值中有注释等，见 convert_insert_values 的整句回退）不校验、不计入，在 stderr 提示

        Args:
            table_name (str): 表名
            insert_scripts (List[str]): 表的 INSERT 语句（转换前），见 Convertor.inserts
            unchecked (str): 无法按列对应时，stderr 提示中说明其中的 id 如何处理

        Returns:
            id 的最大值，没有数据时为 0

        Raises:
            ValueError: 能按列对应的语句中 id 不是整数字面量（NULL、DEFAULT、字符串、表达式等），或列清单中没有 id 列：
                MySQL 由 AUTO_INCREMENT 生成或按表达式计算，目标数据库无法保持相同的 id、无法确定序列的起始值；
                任一语句失败时整个文件不输出，见 print
        """
        if self.table_columns.get(table_name.lower(), {}).get("id") != "number":
            return 0
        last_id = 0
        for insert_script in insert_scripts:
            rows = self.insert_values(table_name, insert_script)
            if rows is None:
                print(f"!!! 表 {table_name} 的 INSERT 语句无法按列对应，{unchecked}: {insert_script[:64]}", file=sys.stderr)
                continue
            _, tokens, values = rows
            ids = ["".join(tokens[i] for i in value).strip() for name, value in values if name == "id"]
            if not ids:
                raise ValueError(f"不支持表 {table_name} 的 INSERT，需要显式的整数 id：列清单中没有 id 列: {insert_script[:64]}")
            for text in ids:
                if not re.fullmatch(r"[+-]?\d+", text):
                    raise ValueError(f"不支持表 {table_name} 的 INSERT，需要显式的整数 id：id 不是整数字面量: {text[:64]}")
                last_id = max(last_id, int(text))
        return last_id

    def next_sequence_value(self, table_name: str, insert_scripts: List[str]) -> int:
        """序列的起始值：全部 INSERT 中全部行的 id 的最大值加 1，见 max_explicit_id；没有数据时为 1。
        id 列不是数值列（例如字符串编号）时序列不对应 id，为 1

        Args:
            table_name (str): 表名
            insert_scripts (List[str]): 表的 INSERT 语句（转换前），见 Convertor.inserts

        Returns:
            序列的起始值

        Raises:
            ValueError: 见 max_explicit_id
        """
        return self.max_explicit_id(table_name, insert_scripts, "序列起始值未计入其中的 id") + 1

    def convert_default(self, table_name: str, column_name: str, default):
        """按列的字面量类别转换默认值，见 convert_literal；其它完整的字符串默认值见 convert_string_default"""
        kind = self.table_columns.get(table_name.lower(), {}).get(column_name.lower())
        literal = self.convert_literal(kind, default)
        return self.convert_string_default(default) if literal is None else literal

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

    def add_insert_columns(self, table_name: str, insert_script: str) -> str:
        """省略列清单的 INSERT 按建表的列顺序补充列清单，值保持原样；已有列清单、表未记录列时保持原样

        SQL Server、达梦只有指定列清单（且 SET IDENTITY_INSERT 为 ON）时才能为自增列 id 赋值。
        列顺序为 gen_create 记录的建表列顺序，见 register_literal_columns；列名与 CREATE 使用同一套转义，见 escape_column_name。
        与 escape_insert_columns 相同，在 convert_insert_values 之后调用，不影响按列对应的字面量转换

        Args:
            table_name (str): 表名
            insert_script (str): INSERT 语句（见 Convertor.inserts），或 convert_insert_values 转换后的语句
        """
        columns = self.table_columns.get(table_name.lower())
        match = INSERT_VALUES_PATTERN.match(insert_script)
        if not columns or not match or match.group(2) is not None:
            return insert_script
        names = ", ".join(self.escape_column_name(name) for name in columns)
        head = re.sub(r"\s*VALUES\s+\Z", "", match.group(1), flags=re.IGNORECASE)
        return f"{head} ({names}) VALUES {match.group(3)}"

    @staticmethod
    def inserts(table_name: str, insert_scripts: Dict[str, List[str]]) -> Generator:
        """收集 `table_name` 对应的 INSERT 语句，统一为 INSERT INTO 表名 (列清单) VALUES 值; 的形式

        Args:
            table_name (str): 表名
            insert_scripts (Dict[str, List[str]]): 表名 -> 完整的 INSERT 语句，见 insert_statements

        Raises:
            ValueError: 不是 INSERT INTO `表名` (列清单) VALUES ... 的语句，例如 INSERT INTO ... SET、INSERT INTO ... SELECT，
                不能默默丢弃数据
        """
        for statement in insert_scripts.get(table_name, []):
            match = INSERT_STATEMENT_PATTERN.fullmatch(statement)
            if not match:
                raise ValueError(f"不支持表 {table_name} 的 INSERT 语句，仅支持 INSERT INTO `表名` (列清单) VALUES：{statement[:64]}")
            # 列清单中的换行等空白统一为一个空格
            head = re.sub(r"\s+", " ", (match.group(2) or "").replace("`", "").lower())
            # 字符串中的转义由各 gen_insert 按词法单元只解码一次，见 convert_insert_values；结尾分号前的空白不是数据
            tail = re.sub(r"\s+;\Z", ";", match.group(3).strip())
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

    def print(self):
        """打印转换后的sql脚本到终端

        全部表转换完成后才输出：任一语句明确失败（例如不支持的字符串）时不输出部分转换的脚本
        """
        scripts = [
            PREAMBLE.format(
                db_type=self.db_type,
                date=time.strftime("%Y-%m-%d %H:%M:%S"),
            ) + "\n"
        ]

        dual = self.gen_dual()
        if dual:
            scripts.append(
                f"""-- ----------------------------
-- Table structure for dual
-- ----------------------------
{dual}
""" + "\n"
            )

        error_scripts = []
        for table_sql, orig_table_sql in zip(self.table_script_list, self.original_table_script_list):
            # 按词法单元剥离 COMMENT 子句（DDLParser 无法处理双写的引号、中文等注释内容），同时提取表、列注释，见 ddl_comments
            stripped_sql, table_comment, column_comments = ddl_comments(orig_table_sql)
            # 字符串默认值会被 DDLParser 改写（中文转为 \uXXXX、转义转为内部占位符），在清理前先替换为占位标识，
            # 解析后还原为原始的字符串，由 convert_default 只解码一次；其它清理与 table_script_list 一致
            protected_sql, default_strings = protect_default_strings(stripped_sql)
            # 以小数点开头的数值默认值（例如 DEFAULT .5）DDLParser 无法解析，先替换为占位标识，解析后还原
            table_sql_for_parse, default_numbers = protect_default_numbers(clean(protected_sql))
            ddl = DDLParser(table_sql_for_parse.replace("`", "")).run()
            if len(ddl) == 0:
                # 去掉反引号后，与关键字同名的标识符（例如索引名、列名 no）无法解析：保留反引号，
                # 由 normalize_names 只去掉标识符两侧的引号后重试
                ddl = DDLParser(table_sql_for_parse, normalize_names=True).run()

            # 如果parse失败, 需要跟进
            if len(ddl) == 0:
                error_scripts.append(table_sql)
                continue

            table_ddl = ddl[0]
            table_name = table_ddl["table_name"]

            # simple-ddl-parser 1.9+ 将字符串默认值中的等号解析为 ``\\03d``。
            # 还原为原始字符，避免导出的目标库脚本出现错误默认值。
            restored = set()
            for column in table_ddl["columns"]:
                # 还原字符串默认值的原始字符串（含 N、n 前缀），不使用解析器改写后的值
                if isinstance(column.get("default"), str) and column["default"].strip().lower() in default_strings:
                    restored.add(column["default"].strip().lower())
                    column["default"] = default_strings[column["default"].strip().lower()]
                    continue
                if isinstance(column.get("default"), str) \
                        and DEFAULT_STRING_PLACEHOLDER.format("") in column["default"].lower():
                    raise ValueError(f"无法还原表 {table_name} 的列 {column['name']} 的字符串默认值: {column['default']}")
                if isinstance(column.get("default"), str):
                    column["default"] = column["default"].replace(r"\03d", "=")
                # 还原以小数点开头的数值默认值，补齐整数部分
                if isinstance(column.get("default"), str) and column["default"].lower() in default_numbers:
                    column["default"] = default_numbers[column["default"].lower()]
            # 解析器没有返回的字符串默认值：明确失败，不默默丢弃默认值
            missing = sorted(set(default_strings) - restored)
            if missing:
                raise ValueError(f"无法还原表 {table_name} 的字符串默认值: {', '.join(default_strings[p] for p in missing)}")

            # 忽略 quartz 的内容
            if table_name.lower().startswith("qrtz"):
                continue

            # 解析注释
            # 从原始建表语句按词法单元提取的注释（见 ddl_comments）只解码一次，没有注释的列为空串；由各 gen_comment 生成目标字符串
            for column in table_ddl["columns"]:
                comment = column_comments.get(column["name"])
                column["comment"] = decode_mysql_string(comment) if comment else ""
            table_ddl["comment"] = decode_mysql_string(table_comment) if table_comment else ""

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
            script = compact_blank_lines(script).strip() + "\n"

            scripts.append(script)

        print("".join(scripts), end="")

        # 将parse失败的脚本打印出来
        if error_scripts:
            print("!!! 以下内容无法正常解析", file=sys.stderr)
            for script in error_scripts:
                # print to stderr
                print(script, file=sys.stderr)


class PostgreSQLConvertor(Convertor):
    # MySQL 整数类型 -> 能容纳其取值范围的类型：int2 / int4 / int8 为 16 / 32 / 64 位有符号整数，
    # 无符号的 smallint、int 需要扩大一级，bigint unsigned 的最大值 18446744073709551615 为 20 位，使用精确的 numeric(20,0)
    integer_types = {
        ("tinyint", False): "int2",
        ("tinyint", True): "int2",
        ("smallint", False): "int2",
        ("smallint", True): "int4",
        ("mediumint", False): "int4",
        ("mediumint", True): "int4",
        ("int", False): "int4",
        ("int", True): "int8",
        ("bigint", False): "int8",
        ("bigint", True): "numeric(20,0)",
    }
    # numeric 最大精度 1000，可容纳 MySQL decimal 的全部精度；real / double precision 为 IEEE 单精度 / 双精度
    max_decimal_precision = 1000
    decimal_format = "numeric({0},{1})"
    float_types = {"float": "real", "double": "double precision"}
    # bit(1) 转为 bool，TRUE、FALSE 保持布尔字面量
    boolean_bit_literals = ("TRUE", "FALSE")

    def __init__(self, src):
        super().__init__(src, "PostgreSQL")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        integer = integer_type(type)
        if integer:
            return self.integer_types[integer]
        numeric = self.translate_numeric_type(type, size)
        if numeric:
            return numeric
        if type in ("varchar", "char"):
            # MySQL 中未指定长度的 char 等同于 char(1)
            return f"varchar({size or 1})"
        if type == "datetime":
            # 显式的小数秒精度（含 0）保留为 timestamp(n)；缺省保持原有的 timestamp
            return f"timestamp({size})" if size is not None else "timestamp"
        if type == "timestamp null":
            return "timestamp"
        if type == "date":
            return "date"
        if type == "json":
            return "jsonb"
        if type == "time":
            return "time"
        if type == "timestamp":
            # 显式精度 0 不能当作缺省
            return f"timestamp({size})" if size is not None else "timestamp"
        if type == "bit":
            return "bool"
        if type in ("tinytext", "text", "mediumtext", "longtext"):
            return "text"
        if type in ("tinyblob", "blob", "mediumblob", "longblob"):
            return "bytea"

    @staticmethod
    def translate_default(type: str, default):
        """日期时间列的当前时间默认值转为 CURRENT_TIMESTAMP(n)：NOW(n) 不存在，LOCALTIME 为 time 类型，CURRENT_TIMESTAMP() 语法错误"""
        return convert_current_time_default(type, default, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP({0})")

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""
        # 记录各列的字面量类别，生成默认值、INSERT 时按列类型转换字面量
        self.register_literal_columns(ddl)

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
            default_value = self.translate_default(type, col["default"])
            # TRUE、FALSE 等按列类型转换，见 convert_literal
            default_value = self.convert_default(ddl["table_name"], name, default_value)
            default = f"DEFAULT {default_value}" if default_value is not None else ""
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
        """生成字段及表的注释：解码后的注释生成标准 SQL 字符串，见 quote_sql_string"""

        script = ""
        for column in table_ddl["columns"]:
            table_comment = quote_sql_string(column["comment"])
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{self.escape_column_name(column['name'])} IS {table_comment};"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS {quote_sql_string(table_comment)};\n"
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
        """生成 insert 语句，以及根据全部 insert 中最大的 id+1 生成 Sequence，见 next_sequence_value"""

        sources = list(Convertor.inserts(table_name, self.insert_scripts))
        inserts = [self.convert_insert_values(table_name, s) for s in sources]
        inserts = [self.escape_insert_columns(s) for s in inserts]
        ## 生成 insert 脚本
        script = ""
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

        # 生成 Sequence
        script += (
            "\n\n"
            + f"""DROP SEQUENCE IF EXISTS {table_name}_seq;
CREATE SEQUENCE {table_name}_seq
    START {self.next_sequence_value(table_name, sources)};"""
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
    # MySQL 整数类型 -> 能容纳其取值范围的类型：smallint 即 number(38)，number 为 38 位精确数值，保持原有映射
    integer_types = {
        ("tinyint", False): "smallint",
        ("tinyint", True): "smallint",
        ("smallint", False): "smallint",
        ("smallint", True): "smallint",
        ("mediumint", False): "number",
        ("mediumint", True): "number",
        ("int", False): "number",
        ("int", True): "number",
        ("bigint", False): "number",
        ("bigint", True): "number",
    }
    # number 最大精度 38；float(n) 为按十进制精度计算的 number，不是 IEEE 浮点数，使用 binary_float / binary_double
    max_decimal_precision = 38
    decimal_format = "number({0},{1})"
    float_types = {"float": "binary_float", "double": "binary_double"}
    # binary_float、binary_double 的字面量后缀：带后缀的数值直接按 IEEE 浮点数解析，不先按 number 解析
    float_literal_suffixes = {"binary_float": "F", "binary_double": "D"}
    # Oracle 12.2 以下（或 COMPATIBLE < 12.2）标识符最长 30 字节，超出会报 ORA-00972。相关案例：https://t.zsxq.com/tbf3q
    max_identifier_length = 30
    # AL32UTF8、MAX_STRING_SIZE = STANDARD 时 varchar2 最长 4000 字节，即 1000 个 4 字节字符；更长的无索引 varchar 改用 clob
    max_varchar_length = 1000
    long_string_type = "clob"
    # 字符串字面量最长 4000 字节；更长的值按不超过 clob_chunk_bytes 字节的小块用 TO_CLOB 拼接，见 string_literal
    max_string_literal_bytes = 4000
    clob_chunk_bytes = 2000

    def __init__(self, src):
        super().__init__(src, "Oracle")

    def string_literal(self, value: str) -> str:
        """解码后的字符串值 -> Oracle 字符串字面量

        不超过 max_string_literal_bytes 字节时为标准 SQL 字符串；更长的值（只能写入 clob）按 UTF-8 字节切成小块，
        每块单独加引号后用 TO_CLOB 拼接，避免按 varchar2 拼接溢出，切分只在字符之间
        """
        if len(value.encode("utf-8")) <= self.max_string_literal_bytes:
            return quote_sql_string(value)
        chunks, chunk, size = [], [], 0
        for char in value:
            char_bytes = len(char.encode("utf-8"))
            if chunk and size + char_bytes > self.clob_chunk_bytes:
                chunks.append("".join(chunk))
                chunk, size = [], 0
            chunk.append(char)
            size += char_bytes
        chunks.append("".join(chunk))
        return "(" + " || ".join(f"TO_CLOB({quote_sql_string(chunk)})" for chunk in chunks) + ")"

    @classmethod
    def float_literal(cls, full_type: Optional[str], value) -> Optional[str]:
        """binary_float、binary_double 列的数值，转为带 F、D 后缀的浮点数字面量

        不带后缀的数值字面量先按 number 解析，超出 number 的范围（约 1E126）会溢出，极小的值会下溢为 0；
        带后缀的字面量直接按 IEEE 浮点数解析，不依赖 NLS。单引号括起的纯数值字符串（MySQL 按数值处理）同样转换，
        避免隐式转换依赖 NLS_NUMERIC_CHARACTERS。只处理完整的简单数值，按原文追加后缀，不经过 Python 的 float

        Args:
            full_type (Optional[str]): 目标列类型
            value: 默认值或 INSERT 中的值，可能为解析器返回的 int、str

        Returns:
            带后缀的字面量；不是 binary_float、binary_double 列，或不是简单数值（NULL、表达式、函数、其它字符串等）时返回 None
        """
        suffix = cls.float_literal_suffixes.get(full_type)
        if suffix is None:
            return None
        if isinstance(value, int) and not isinstance(value, bool):
            return f"{value}{suffix}"
        if not isinstance(value, str):
            return None
        text = value.strip()
        if len(text) >= 2 and text[0] == "'" and text[-1] == "'":
            text = text[1:-1]
        if not NUMBER_LITERAL_PATTERN.fullmatch(text):
            return None
        return f"{text}{suffix}"

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

        integer = integer_type(type)
        if integer:
            return self.integer_types[integer]
        numeric = self.translate_numeric_type(type, size)
        if numeric:
            return numeric
        if type == "varchar":
            # MySQL varchar(n) 按字符计算长度，Oracle 默认按字节，所以显式使用 CHAR 语义。相关案例：https://t.zsxq.com/tbf3q
            # 超过 max_varchar_length 个字符时改用 clob，不截断长度；有索引的列明确失败，见 register_string_columns
            return f"varchar2({size} char)" if size <= self.max_varchar_length else self.long_string_type
        if type == "char":
            # MySQL 中未指定长度的 char 等同于 char(1)，最长 255 个字符；与 varchar 一致使用 CHAR 语义
            return f"varchar2({size or 1} char)"
        if type == "datetime":
            # MySQL datetime 缺省没有小数秒，与 Oracle date 一致；显式的小数秒精度保留为 timestamp(n)，date 只保存到秒
            return f"timestamp({size})" if size else "date"
        if type == "timestamp":
            # MySQL timestamp 缺省精度为 0，Oracle timestamp 缺省精度为 6，需显式输出
            return f"timestamp({size if size is not None else 0})"
        if type == "bit":
            return "number(1,0)"
        if type in ("tinytext", "text", "mediumtext", "longtext"):
            return "clob"
        if type in ("tinyblob", "blob", "mediumblob", "longblob"):
            return "blob"

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""
        # 记录各列的字面量类别，生成默认值、INSERT 时按列类型转换字面量
        self.register_literal_columns(ddl)

        def generate_column(col):
            name = col["name"].lower()
            if name == "deleted":
                return "deleted number(1,0) DEFAULT 0 NOT NULL"

            type = col["type"].lower()
            full_type = self.column_type(ddl["table_name"], col)
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            # Oracle的 INSERT '' 不能通过NOT NULL校验，因此对文字类型字段覆写为 NULL
            nullable = "NULL" if type in ("varchar", "char", "tinytext", "text", "mediumtext", "longtext") else nullable
            # 当前时间默认值转为 CURRENT_TIMESTAMP(n)：NOW、LOCALTIME 不能用于 DEFAULT，CURRENT_TIMESTAMP() 精度超出范围
            default = convert_current_time_default(type, col["default"], "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP({0})")
            # 字符串、TRUE、FALSE、date / timestamp 列的日期时间字符串等按列类型转换，见 convert_literal
            default = self.convert_default(ddl["table_name"], name, default)
            # binary_float、binary_double 列的数值默认值转为带 F、D 后缀的字面量，避免先按 number 解析而溢出
            float_default = self.float_literal(full_type, default)
            if float_default:
                default = float_default
            default = f"DEFAULT {default}" if default is not None else ""
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
        # 注释只能是字符串常量，不使用 string_literal 的 TO_CLOB 拼接
        script = ""
        for column in table_ddl["columns"]:
            table_comment = quote_sql_string(column["comment"])
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{self.escape_column_name(column['name'])} IS {table_comment};"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS {quote_sql_string(table_comment)};\n"
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

    def literal_kind(self, type: str, size) -> Optional[str]:
        """列的字面量类别：date、timestamp 列为 date、timestamp，binary_float、binary_double 列为目标类型，其它见基类"""
        full_type = self.translate_type(type, size)
        if full_type is not None and full_type.startswith("timestamp"):
            return "timestamp"
        if full_type == "date":
            return "date"
        if full_type in self.float_literal_suffixes:
            return full_type
        return super().literal_kind(type, size)

    def convert_literal(self, kind: Optional[str], value) -> Optional[str]:
        """按列的字面量类别转换单个字面量，不依赖 NLS

        1. timestamp 列中的日期时间字符串（含整秒、小数秒，可带前缀，见 decode_mysql_literal），转为 TIMESTAMP 字面量
        2. date 列中的日期时间字符串（可带前缀），转为 to_date；date 只保存到秒，带小数秒时明确失败
        3. binary_float、binary_double 列中的简单数值，转为带 F、D 后缀的字面量，见 float_literal；TRUE、FALSE 先转为 1、0
        4. 其它列见基类；其它列中的日期时间样式的字符串保持为字符串

        Raises:
            ValueError: date 列的日期时间字符串带小数秒；前缀不支持，见 decode_mysql_literal
        """
        if kind in ("timestamp", "date"):
            # 解码后的值为日期时间（只含数字、横线、冒号、空格、小数点）时，加引号后使用；N、n 前缀、字符集引导符见 decode_mysql_literal
            string = DEFAULT_STRING_PATTERN.fullmatch(value.strip()) if isinstance(value, str) else None
            if not string:
                return None
            text = decode_mysql_literal(string.group(1), string.group(2))
            literal = DATETIME_LITERAL_PATTERN.fullmatch(quote_sql_string(text))
            if not literal:
                return None
            if kind == "timestamp":
                return f"TIMESTAMP {literal.group()}"
            if literal.group(1):
                raise ValueError(f"Oracle date 列不支持带小数秒的值: {literal.group()}")
            return f"to_date({literal.group()}, 'SYYYY-MM-DD HH24:MI:SS')"
        if kind in self.float_literal_suffixes:
            boolean = boolean_value(value)
            if boolean is not None:
                value = "1" if boolean else "0"
            return self.float_literal(kind, value)
        return super().convert_literal(kind, value)

    def gen_insert(self, table_name: str) -> str:
        """拷贝 INSERT 语句，以及根据全部 INSERT 中最大的 id+1 生成序列，见 next_sequence_value"""
        sources = list(Convertor.inserts(table_name, self.insert_scripts))
        inserts = []
        for insert_script in sources:
            insert_script = self.convert_insert_values(table_name, insert_script)
            # date、timestamp 列的日期时间字符串已在上一步按列类型转换，其它列的同样式文本保持原值
            insert_script = self.escape_insert_columns(insert_script)
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n-- ----------------------------
-- Records of {table_name.lower()}
-- ----------------------------
-- @formatter:off
{inserts_lines}
COMMIT;
-- @formatter:on"""

        # 生成 Sequence
        seq_name = self.shorten_identifier(f"{table_name}_seq")
        if seq_name != f"{table_name}_seq":
            # 序列名会被 @KeySequence 引用，缩短后需要提示同步修改
            print(f"!!! Oracle 序列名 {table_name}_seq 超过 {self.max_identifier_length} 字节，已缩短为 {seq_name}，"
                  f"如有 @KeySequence 引用需同步修改", file=sys.stderr)
        script += f"""

CREATE SEQUENCE {seq_name}
    START WITH {self.next_sequence_value(table_name, sources)};"""

        return script


class SQLServerConvertor(Convertor):
    """_summary_

    Args:
        Convertor (_type_): _description_
    """

    # MySQL 整数类型 -> 能容纳其取值范围的类型：SQL Server 的 tinyint 为 0 ~ 255，smallint / int / bigint 为 16 / 32 / 64 位有符号整数；
    # 有符号 tinyint 需要 smallint，无符号的 smallint、int 需要扩大一级，bigint unsigned 为 20 位，使用精确的 numeric(20,0)
    integer_types = {
        ("tinyint", False): "smallint",
        ("tinyint", True): "tinyint",
        ("smallint", False): "smallint",
        ("smallint", True): "int",
        ("mediumint", False): "int",
        ("mediumint", True): "int",
        ("int", False): "int",
        ("int", True): "bigint",
        ("bigint", False): "bigint",
        ("bigint", True): "numeric(20,0)",
    }
    # numeric 最大精度 38；real 为 float(24) 单精度，float 为 float(53) 双精度
    max_decimal_precision = 38
    decimal_format = "numeric({0},{1})"
    float_types = {"float": "real", "double": "float"}
    # nvarchar(n) 最长 4000 个 UTF-16 码元，即 2000 个字符；更长的无索引 varchar 改用 nvarchar(max)
    max_varchar_length = 2000
    long_string_type = "nvarchar(max)"

    def __init__(self, src):
        super().__init__(src, "Microsoft SQL Server")

    def string_literal(self, value: str) -> str:
        """解码后的字符串值 -> Unicode 字符串常量 N'...'：不带 N 时按数据库排序规则的代码页保存，例如 1252 下中文变为 ??"""
        return "N" + quote_sql_string(value)

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        integer = integer_type(type)
        if integer:
            return self.integer_types[integer]
        numeric = self.translate_numeric_type(type, size)
        if numeric:
            return numeric
        if type == "varchar":
            # MySQL 按字符计数，nvarchar(n) 按 UTF-16 码元计数，增补字符（例如 emoji）占 2 个码元，长度需要 2 倍；
            # nvarchar(n) 最长 4000，超过 max_varchar_length 个字符时改用 nvarchar(max)，不截断长度；有索引的列明确失败，见 register_string_columns
            return f"nvarchar({size * 2})" if size <= self.max_varchar_length else self.long_string_type
        if type == "char":
            # MySQL 中未指定长度的 char 等同于 char(1)，最长 255 个字符；MySQL 按字符计数，nvarchar(n) 按 UTF-16 码元计数，
            # 增补字符（例如 emoji）占 2 个码元，长度需要 2 倍才能保留全部取值，最大 510
            return f"nvarchar({(size or 1) * 2})"
        if type in ("datetime", "timestamp"):
            # 显式的小数秒精度（含 0）保留为 datetime2(n)；缺省保持原有的 datetime2
            return f"datetime2({size})" if size is not None else "datetime2"
        if type == "bit":
            # MySQL bit(1) 与 SQL Server bit 均只有 0、1、NULL；多位的位串不是布尔值，暂不支持
            if size not in (None, 1):
                raise ValueError(f"SQL Server 转换暂不支持 bit({size})，仅支持 bit(1)")
            return "bit"
        if type in ("tinytext", "text", "mediumtext", "longtext"):
            return "nvarchar(max)"
        if type in ("tinyblob", "blob", "mediumblob", "longblob"):
            return "varbinary(max)"

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""
        # 记录各列的字面量类别，生成默认值、INSERT 时按列类型转换字面量
        self.register_literal_columns(ddl)

        def _generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"
            if name == "deleted":
                return "deleted bit DEFAULT 0 NOT NULL"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default_value = col["default"]
            # bit 的默认值使用数字常量：b'0'、b'1' 在清理后为 '0'、'1'
            if full_type == "bit" and default_value in ("'0'", "'1'"):
                default_value = default_value.strip("'")
            # 当前时间默认值：CURRENT_TIMESTAMP 不带精度参数，NOW、LOCALTIME、LOCALTIMESTAMP 不存在；
            # 指定精度时使用 SYSDATETIME()（datetime2(7)），由 datetime2(n) 列限定精度
            default_value = convert_current_time_default(type, default_value, "CURRENT_TIMESTAMP", "SYSDATETIME()")
            # TRUE、FALSE 等按列类型转换，见 convert_literal
            default_value = self.convert_default(ddl["table_name"], name, default_value)
            default = f"DEFAULT {default_value}" if default_value is not None else ""
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
        """生成字段及表的注释：注释、表名、列名均为 Unicode 字符串常量，见 string_literal"""

        script = ""
        table_name = self.string_literal(table_ddl["table_name"])

        for column in table_ddl["columns"]:
            column_comment = self.string_literal(column["comment"])
            field = self.string_literal(column["name"])

            script += f"""EXEC sp_addextendedproperty
    'MS_Description', {column_comment},
    'SCHEMA', N'dbo',
    'TABLE', {table_name},
    'COLUMN', {field}
GO

"""

        table_comment = table_ddl["comment"]
        if table_comment:
            script += f"""EXEC sp_addextendedproperty
    'MS_Description', {self.string_literal(table_comment)},
    'SCHEMA', N'dbo',
    'TABLE', {table_name}
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

    @staticmethod
    def national_strings(insert_script: str) -> str:
        """INSERT 中的字符串加 N 前缀，SQL Server 的 Unicode 字符串常量只能使用大写 N

        按词法单元处理，只处理完整的单引号字符串，每一行 VALUES 均处理；字符串内容（含 ''、逗号、括号）、注释、标识符保持原样。
        已有的 N、n 前缀统一为一个大写 N；前面紧跟其它字母、数字的字符串（例如 x'0A'、_utf8mb4'a' 等引导符）保持原样

        Args:
            insert_script (str): INSERT 语句

        Returns:
            字符串加 N 前缀后的 INSERT 语句
        """
        parts, start = [], 0
        for match in SQL_TOKEN_PATTERN.finditer(insert_script):
            if not re.fullmatch(SQL_STRING, match.group()):
                continue
            begin = match.start()
            # 已有的 N、n 前缀：前一个字符为 n、N，且它前面不是字母、数字（不是标识符的结尾）
            if begin and insert_script[begin - 1] in "nN" \
                    and (begin < 2 or not re.match(r"[\w$]", insert_script[begin - 2])):
                parts.append(insert_script[start:begin - 1] + "N")
            elif begin and re.match(r"[\w$]", insert_script[begin - 1]):
                continue
            else:
                parts.append(insert_script[start:begin] + "N")
            parts.append(match.group())
            start = match.end()
        parts.append(insert_script[start:])
        return "".join(parts)

    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句"""

        # 收集 `table_name` 对应的 insert 语句
        sources = list(Convertor.inserts(table_name, self.insert_scripts))
        # IDENTITY_INSERT 保持原 id 导入：只支持显式的整数 id，见 max_explicit_id
        self.max_explicit_id(table_name, sources, "未校验其中的 id")
        inserts = []
        for insert_script in sources:
            # 字符串只解码一次并加 N 前缀（见 string_literal），TRUE、FALSE 等按列类型转换，见 convert_insert_values；
            # 转换后的字符串为标准 SQL 字符串，不再经过按 MySQL 转义识别的 national_strings
            insert_script = self.convert_insert_values(table_name, insert_script)
            # 省略的列清单按建表的列顺序补充，IDENTITY_INSERT 为 ON 时才能为 id 赋值，见 add_insert_columns
            insert_script = self.add_insert_columns(table_name, insert_script)
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
    # MySQL 整数类型 -> 能容纳其取值范围的类型：smallint / int / bigint 为 16 / 32 / 64 位有符号整数，
    # 无符号的 smallint、int 需要扩大一级，bigint unsigned 的最大值 18446744073709551615 为 20 位，使用精确的 decimal(20,0)
    integer_types = {
        ("tinyint", False): "smallint",
        ("tinyint", True): "smallint",
        ("smallint", False): "smallint",
        ("smallint", True): "int",
        ("mediumint", False): "int",
        ("mediumint", True): "int",
        ("int", False): "int",
        ("int", True): "bigint",
        ("bigint", False): "bigint",
        ("bigint", True): "decimal(20,0)",
    }
    # decimal 最大精度 38；real 为单精度，double 为双精度
    max_decimal_precision = 38
    decimal_format = "decimal({0},{1})"
    float_types = {"float": "real", "double": "double"}
    # 按 UTF8 字符集、16K 数据页保守估算（其它页大小、字符集未验证）：varchar(n char) 每个字符最多 4 字节，
    # 超过 1000 个字符的无索引 varchar 改用 text；16K 页实测单列 6000 字节可写入、8000 字节失败，
    # 行长度按 6000 字节（含行头估算）保守控制，超出时依次将最长的无索引 varchar 改用 text，见 register_string_columns、row_bytes
    max_varchar_length = 1000
    long_string_type = "text"
    max_row_bytes = 6000
    # 行头、空值标记等的保守估算（字节）
    row_overhead_bytes = 64
    # 各目标类型在行内的保守估算长度（字节）；text、blob 只计行内的引用，按 lob_reference_bytes 估算
    fixed_type_bytes = {"smallint": 2, "int": 4, "bigint": 8, "real": 4, "double": 8, "bit": 1,
                        "date": 4, "time": 8, "datetime": 12, "timestamp": 12}
    decimal_bytes = 24
    lob_reference_bytes = 64

    def __init__(self, src):
        super().__init__(src, "DM8")

    def row_bytes(self, ddl: Dict, long_columns: set) -> int:
        """按 UTF8、16K 数据页保守估算表的行长度（字节）：varchar(n char) 按 4n + 2，改用 text 的列只计行内引用；
        无法识别的类型按 lob_reference_bytes 估算"""
        total = self.row_overhead_bytes
        for column in ddl["columns"]:
            if column["name"].lower() in long_columns:
                total += self.lob_reference_bytes
                continue
            full_type = self.translate_type(column["type"].lower(), column["size"]) or ""
            match = re.fullmatch(r"varchar\((\d+) char\)", full_type)
            if match:
                total += int(match.group(1)) * 4 + 2
            elif full_type.startswith("decimal"):
                total += self.decimal_bytes
            else:
                total += self.fixed_type_bytes.get(full_type.split("(")[0], self.lob_reference_bytes)
        return total

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""
        type = type.lower()

        integer = integer_type(type)
        if integer:
            return self.integer_types[integer]
        numeric = self.translate_numeric_type(type, size)
        if numeric:
            return numeric
        if type == "varchar":
            # MySQL varchar(n) 按字符计算长度，DM8 默认可能按字节，所以显式使用 CHAR 语义；
            # 超过 max_varchar_length 个字符时改用 text，不截断长度；有索引的列明确失败，见 register_string_columns
            return f"varchar({size} char)" if size <= self.max_varchar_length else self.long_string_type
        if type == "char":
            # MySQL 中未指定长度的 char 等同于 char(1)，最长 255 个字符；与 varchar 一致使用 CHAR 语义
            return f"varchar({size or 1} char)"
        if type == "datetime":
            # 显式的小数秒精度（含 0）保留为 datetime(n)；缺省保持原有的 datetime
            return f"datetime({size})" if size is not None else "datetime"
        if type == "timestamp":
            # MySQL timestamp 缺省精度为 0，不能生成 timestamp(None)
            return f"timestamp({size if size is not None else 0})"
        if type == "bit":
            return "bit"
        if type in ("tinytext", "text", "mediumtext", "longtext"):
            return "text"
        if type in ("tinyblob", "blob", "mediumblob", "longblob"):
            return "blob"

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""
        # 记录各列的字面量类别，生成默认值、INSERT 时按列类型转换字面量
        self.register_literal_columns(ddl)

        def generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"

            # 行长度超出时改用 text 的 varchar 列见 register_string_columns
            full_type = self.column_type(ddl["table_name"], col)
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            # 字符串、TRUE、FALSE 等按列类型转换，见 convert_literal
            default_value = self.convert_default(ddl["table_name"], name, col["default"])
            default = f"DEFAULT {default_value}" if default_value is not None else ""
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
            table_comment = quote_sql_string(column["comment"])
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{column['name']} IS {table_comment};"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS {quote_sql_string(table_comment)};\n"
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
        sources = list(Convertor.inserts(table_name, self.insert_scripts))
        # IDENTITY_INSERT 保持原 id 导入：只支持显式的整数 id，见 max_explicit_id
        self.max_explicit_id(table_name, sources, "未校验其中的 id")
        # 省略的列清单按建表的列顺序补充，IDENTITY_INSERT 为 ON 时才能为 id 赋值，见 add_insert_columns
        inserts = [self.add_insert_columns(table_name, self.convert_insert_values(table_name, s)) for s in sources]

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
        # 记录各列的字面量类别，生成默认值、INSERT 时按列类型转换字面量
        self.register_literal_columns(ddl)

        def _generate_column(col):
            name = col["name"].lower()
            if name == "deleted":
                return "deleted int2 NOT NULL DEFAULT 0"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            if full_type == "text":
                nullable = "NULL"
            default_value = self.translate_default(type, col["default"])
            # TRUE、FALSE 等按列类型转换，见 convert_literal
            default_value = self.convert_default(ddl["table_name"], name, default_value)
            default = f"DEFAULT {default_value}" if default_value is not None else ""
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
