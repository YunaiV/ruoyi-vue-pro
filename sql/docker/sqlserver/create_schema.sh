#!/usr/bin/env bash

/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Yudao@24' -Q "CREATE DATABASE [ruoyi-vue-pro];
GO"
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Yudao@24' -d 'ruoyi-vue-pro' -i /tmp/schema.sql
