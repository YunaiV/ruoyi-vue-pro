

> `重要通知：组件升级更新 2.0.0 后，支持日期+时间范围选择，组件 ui 将使用日历选择日期，ui 变化较大，同时支持 PC 和 移动端。此版本不向后兼容，不再支持单独的时间选择（type=time）及相关的 hide-second 属性（时间选可使用内置组件 picker）。若仍需使用旧版本，可在插件市场下载*非uni_modules版本*，旧版本将不再维护`

## DatetimePicker 时间选择器

> **组件名：uni-datetime-picker**
> 代码块： `uDatetimePicker`


该组件的优势是，支持**时间戳**输入和输出（起始时间、终止时间也支持时间戳），可**同时选择**日期和时间。

若只是需要单独选择日期和时间，不需要时间戳输入和输出，可使用原生的 picker 组件。

**_点击 picker 默认值规则：_**

- 若设置初始值 value, 会显示在 picker 显示框中
- 若无初始值 value，则初始值 value 为当前本地时间 Date.now()， 但不会显示在 picker 显示框中

### [查看文档](https://uniapp.dcloud.io/component/uniui/uni-datetime-picker)
#### 如使用过程中有任何问题，或者您对uni-ui有一些好的建议，欢迎加入 uni-ui 交流群：871950839 