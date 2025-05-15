/**
 * report 模块，主要实现数据可视化报表等功能：
 * 1. 基于「积木报表」实现，打印设计、报表设计、图形设计、大屏设计等。URL 前缀是 /jmreport，表名前缀是 jimu_
 *
 * 由于「积木报表」的大屏设计器需要收费，后续会自研，对应的是：
 * 1. Controller URL：以 /report/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 report_ 开头，方便在数据库中区分
 */
package cn.iocoder.yudao.module.report;
