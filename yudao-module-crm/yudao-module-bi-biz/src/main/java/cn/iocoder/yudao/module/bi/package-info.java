/**
 * crm 包下，商业智能（Business Intelligence）。
 * 例如说：报表、图表、数据分析等等
 * <p>
 * 1. Controller URL：以 /bi/ 开头，避免和其它 Module 冲突
 *
 * TODO @anhaohao：mall 当时独立拆分一个 statistics 模块的原因，是因为 mall 拆分了多个模块，没有模块适合承接统计的能力，所以独立了。
 * TODO crm 因为没有拆分，所以可以直接放在 crm 模块下面；这样，我们可以在 controller/admin 和 service 下，新建一个 bi 包，专门放置统计的代码。
 */
package cn.iocoder.yudao.module.bi;