# 达梦 flowable 适配

参考文档: [《Flowable6.8(6.x 版本通用)整合集成达梦 8 数据库(DM8)详解，解决自动生成表时 dmn 相关表语法报错问题》](https://blog.csdn.net/TangBoBoa/article/details/130392495)

## 1. 覆盖 flowable，liquibase 相关代码

把`flowable-patch/src`下的文件按文件结果添加到`yudao-server`或者`yudao-module-bpm-biz`项目对应目录中，甚至你可以做个独立模块。

## 2. 修改相关 DAO

例如`cn.iocoder.yudao.module.bpm.dal.dataobject.oa.BpmOALeaveDO`

```diff
- @TableField("`type`")
private String type;
```

## 3. 关于`flowable.database-schema-update`配置

首次运行，修改`flowable.database-schema-update=true`，系统会自动建表，第二次运行需要修改为`false`。

## 4. TODO

配置`flowable.database-schema-update=true`第二次运行失败，报错

```text
Object [FLW_EV_DATABASECHANGELOG] already exists
```
