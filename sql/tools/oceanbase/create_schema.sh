#!/bin/bash
set -e

export MYSQL_PWD="${OB_TENANT_PASSWORD}"
client=(obclient -h127.0.0.1 -P2881 -u"root@${OB_TENANT_NAME}" --default-character-set=utf8mb4)

"${client[@]}" -e 'CREATE DATABASE IF NOT EXISTS `ruoyi-vue-pro` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
"${client[@]}" -D ruoyi-vue-pro < /tmp/schema.sql
"${client[@]}" -D ruoyi-vue-pro < /tmp/quartz.sql
