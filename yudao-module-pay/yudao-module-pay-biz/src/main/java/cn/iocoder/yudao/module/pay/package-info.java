/**
 * pay 模块，我们放支付业务，提供业务的支付能力。
 * 例如说：商户、应用、支付、退款等等
 *
 * 1. Controller URL：以 /member/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 member_ 开头，方便在数据库中区分
 *
 * 注意，由于 Pay 模块和 Trade 模块，容易重名，所以类名都加载 Pay 的前缀~
 */
package cn.iocoder.yudao.module.pay;
