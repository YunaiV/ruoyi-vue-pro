/**
 * bpm 包下，业务流程管理（Business Process Management），我们放工作流的功能，基于 Flowable 6 版本实现。
 * 例如说：流程定义、表单配置、审核中心（我的申请、我的待办、我的已办）等等
 *
 * bpm 解释：https://baike.baidu.com/item/BPM/1933
 *
 * 1. Controller URL：以 /bpm/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 bpm_ 开头，方便在数据库中区分
 *
 * 注意，由于 Bpm 模块下，容易和其它模块重名，所以类名都加载 Bpm 的前缀~
 */
package cn.iocoder.yudao.module.bpm;
