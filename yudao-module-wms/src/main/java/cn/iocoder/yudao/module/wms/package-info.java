/**
 * wms 包下，仓库管理系统（Warehouse Management System）
 * 例如说：仓库、物料、库存、入库、出库、移库、盘库等等
 *
 * 1. Controller URL：以 /wms/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 wms_ 开头，方便在数据库中区分
 *
 * 注意，由于 WMS 模块下，容易和其它模块重名，所以类名都加载 Wms 的前缀~
 */
package cn.iocoder.yudao.module.wms;
