# WHENEVER SQLERROR / OSERROR：遇到 SQL 错误或脚本文件无法读取时立即退出，避免导入失败被忽略；SET DEFINE OFF：避免数据里的 & 被当作变量替换。相关案例：https://t.zsxq.com/tbf3q
# -L：登录失败时直接退出，不再交互式重试
sqlplus -L -s ROOT/123456@//localhost/XEPDB1 <<'EOF'
WHENEVER OSERROR EXIT FAILURE
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF
@/tmp/schema.sql
EXIT
EOF
