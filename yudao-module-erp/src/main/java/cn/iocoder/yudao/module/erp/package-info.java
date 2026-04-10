/**
 * erp 包下，企业资源管理（Enterprise Resource Planning）。
 * 例如说：采购、销售、库存、财务、产品等等
 *
 * 1. Controller URL：以 /erp/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 erp_ 开头，方便在数据库中区分
 *
 * 注意，由于 Erp 模块下，容易和其它模块重名，所以类名都加载 Erp 的前缀~
 */
package cn.iocoder.yudao.module.erp;
