## 1.4.6（2022-07-13）
- 修复 model 需要校验的值没有声明对应字段时，导致第一次不触发校验的bug
## 1.4.5（2022-07-05）
- 新增 更多表单示例
- 优化 子表单组件过期提示的问题
- 优化 子表单组件uni-datetime-picker、uni-data-select、uni-data-picker的显示样式
## 1.4.4（2022-07-04）
- 更新 删除组件日志
## 1.4.3（2022-07-04）
- 修复 由 1.4.0 引发的 label 插槽不生效的bug
## 1.4.2（2022-07-04）
- 修复 子组件找不到 setValue 报错的bug
## 1.4.1（2022-07-04）
- 修复 uni-data-picker 在 uni-forms-item 中报错的bug
- 修复 uni-data-picker 在 uni-forms-item 中宽度不正确的bug
## 1.4.0（2022-06-30）
- 【重要】组件逻辑重构，部分用法用旧版本不兼容，请注意兼容问题
- 【重要】组件使用 Provide/Inject 方式注入依赖，提供了自定义表单组件调用 uni-forms 校验表单的能力
- 新增 model 属性，等同于原 value/modelValue 属性，旧属性即将废弃
- 新增 validateTrigger 属性的 blur 值，仅 uni-easyinput 生效
- 新增 onFieldChange 方法，可以对子表单进行校验，可替代binddata方法
- 新增 子表单的 setRules 方法，配合自定义校验函数使用
- 新增 uni-forms-item 的 setRules 方法，配置动态表单使用可动态更新校验规则
- 优化 动态表单校验方式，废弃拼接name的方式
## 1.3.3（2022-06-22）
- 修复 表单校验顺序无序问题
## 1.3.2（2021-12-09）
-
## 1.3.1（2021-11-19）
- 修复 label 插槽不生效的bug
## 1.3.0（2021-11-19）
- 优化 组件UI，并提供设计资源，详见:[https://uniapp.dcloud.io/component/uniui/resource](https://uniapp.dcloud.io/component/uniui/resource)
- 文档迁移，详见:[https://uniapp.dcloud.io/component/uniui/uni-forms](https://uniapp.dcloud.io/component/uniui/uni-forms)
## 1.2.7（2021-08-13）
- 修复 没有添加校验规则的字段依然报错的Bug
## 1.2.6（2021-08-11）
- 修复 重置表单错误信息无法清除的问题
## 1.2.5（2021-08-11）
- 优化 组件文档
## 1.2.4（2021-08-11）
- 修复 表单验证只生效一次的问题
## 1.2.3（2021-07-30）
- 优化 vue3下事件警告的问题
## 1.2.2（2021-07-26）
- 修复 vue2 下条件编译导致destroyed生命周期失效的Bug
- 修复 1.2.1 引起的示例在小程序平台报错的Bug
## 1.2.1（2021-07-22）
- 修复 动态校验表单，默认值为空的情况下校验失效的Bug
- 修复 不指定name属性时，运行报错的Bug
- 优化 label默认宽度从65调整至70，使required为true且四字时不换行
- 优化 组件示例，新增动态校验示例代码
- 优化 组件文档，使用方式更清晰
## 1.2.0（2021-07-13）
- 组件兼容 vue3，如何创建vue3项目，详见 [uni-app 项目支持 vue3 介绍](https://ask.dcloud.net.cn/article/37834)
## 1.1.2（2021-06-25）
- 修复 pattern 属性在微信小程序平台无效的问题
## 1.1.1（2021-06-22）
- 修复 validate-trigger属性为submit且err-show-type属性为toast时不能弹出的Bug
## 1.1.0（2021-06-22）
- 修复 只写setRules方法而导致校验不生效的Bug
- 修复 由上个办法引发的错误提示文字错位的Bug
## 1.0.48（2021-06-21）
- 修复 不设置 label 属性 ，无法设置label插槽的问题
## 1.0.47（2021-06-21）
- 修复 不设置label属性，label-width属性不生效的bug
- 修复 setRules 方法与rules属性冲突的问题
## 1.0.46（2021-06-04）
- 修复 动态删减数据导致报错的问题
## 1.0.45（2021-06-04）
- 新增 modelValue 属性 ，value 即将废弃
## 1.0.44（2021-06-02）
- 新增 uni-forms-item 可以设置单独的 rules
- 新增 validate 事件增加 keepitem 参数，可以选择那些字段不过滤
- 优化 submit 事件重命名为 validate
## 1.0.43（2021-05-12）
- 新增 组件示例地址
## 1.0.42（2021-04-30）
- 修复 自定义检验器失效的问题
## 1.0.41（2021-03-05）
- 更新 校验器
- 修复 表单规则设置类型为 number 的情况下，值为0校验失败的Bug
## 1.0.40（2021-03-04）
- 修复 动态显示uni-forms-item的情况下，submit 方法获取值错误的Bug
## 1.0.39（2021-02-05）
- 调整为uni_modules目录规范
- 修复 校验器传入 int 等类型 ，返回String类型的Bug
