## 1.2.2（2023-01-28）
- 修复 运行/打包 控制台警告问题
## 1.2.1（2022-09-05）
- 修复 当 text 超过 max-num 时，badge 的宽度计算是根据 text 的长度计算，更改为 css 计算实际展示宽度，详见:[https://ask.dcloud.net.cn/question/150473](https://ask.dcloud.net.cn/question/150473)
## 1.2.0（2021-11-19）
- 优化 组件UI，并提供设计资源，详见:[https://uniapp.dcloud.io/component/uniui/resource](https://uniapp.dcloud.io/component/uniui/resource)
- 文档迁移，详见:[https://uniapp.dcloud.io/component/uniui/uni-badge](https://uniapp.dcloud.io/component/uniui/uni-badge)
## 1.1.7（2021-11-08）
- 优化 升级ui
- 修改 size 属性默认值调整为 small
- 修改 type 属性，默认值调整为 error，info 替换 default
## 1.1.6（2021-09-22）
- 修复 在字节小程序上样式不生效的 bug
## 1.1.5（2021-07-30）
- 组件兼容 vue3，如何创建vue3项目，详见 [uni-app 项目支持 vue3 介绍](https://ask.dcloud.net.cn/article/37834)
## 1.1.4（2021-07-29）
- 修复 去掉 nvue 不支持css 的 align-self 属性，nvue 下不暂支持 absolute 属性
## 1.1.3（2021-06-24）
- 优化 示例项目
## 1.1.1（2021-05-12）
- 新增 组件示例地址
## 1.1.0（2021-05-12）
- 新增 uni-badge 的 absolute 属性，支持定位
- 新增 uni-badge 的 offset 属性，支持定位偏移
- 新增 uni-badge 的 is-dot 属性，支持仅显示有一个小点
- 新增 uni-badge 的 max-num 属性，支持自定义封顶的数字值，超过 99 显示99+
- 优化 uni-badge 属性 custom-style， 支持以对象形式自定义样式
## 1.0.7（2021-05-07）
- 修复 uni-badge 在 App 端，数字小于10时不是圆形的bug
- 修复 uni-badge 在父元素不是 flex 布局时，宽度缩小的bug
- 新增 uni-badge 属性 custom-style， 支持自定义样式
## 1.0.6（2021-02-04）
- 调整为uni_modules目录规范
