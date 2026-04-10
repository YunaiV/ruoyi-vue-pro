/**
 * mes 包下，制造执行系统（Manufacturing Execution System）。
 * 例如说：基础数据、排班日历、设备管理、工具管理、生产管理、质量管理、仓库管理等等
 *
 * 1. Controller URL：以 /mes/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 mes_ 开头，方便在数据库中区分
 *
 * 注意，由于 Mes 模块下，容易和其它模块重名，所以类名都加了 Mes 的前缀~
 */
package cn.iocoder.yudao.module.mes;
