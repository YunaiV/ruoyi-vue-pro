## 1.0.2（2022-07-04）
- 修复 在uni-forms下样式不生效的bug
## 1.0.1（2021-11-23）
- 修复 参数为对象的情况下，url在某些情况显示错误的bug
## 1.0.0（2021-11-19）
- 优化 组件UI，并提供设计资源，详见:[https://uniapp.dcloud.io/component/uniui/resource](https://uniapp.dcloud.io/component/uniui/resource)
- 文档迁移，详见:[https://uniapp.dcloud.io/component/uniui/uni-file-picker](https://uniapp.dcloud.io/component/uniui/uni-file-picker)
## 0.2.16（2021-11-08）
- 修复 传入空对象 ，显示错误的Bug
## 0.2.15（2021-08-30）
- 修复 return-type="object" 时且存在v-model时，无法删除文件的Bug
## 0.2.14（2021-08-23）
- 新增 参数中返回 fileID 字段
## 0.2.13（2021-08-23）
- 修复 腾讯云传入fileID 不能回显的bug
- 修复 选择图片后，不能放大的问题
## 0.2.12（2021-08-17）
- 修复 由于 0.2.11 版本引起的不能回显图片的Bug
## 0.2.11（2021-08-16）
- 新增 clearFiles(index) 方法，可以手动删除指定文件
- 修复 v-model 值设为 null 报错的Bug
## 0.2.10（2021-08-13）
- 修复 return-type="object" 时，无法删除文件的Bug
## 0.2.9（2021-08-03）
- 修复 auto-upload 属性失效的Bug
## 0.2.8（2021-07-31）
- 修复 fileExtname属性不指定值报错的Bug
## 0.2.7（2021-07-31）
- 修复 在某种场景下图片不回显的Bug
## 0.2.6（2021-07-30）
- 修复 return-type为object下，返回值不正确的Bug
## 0.2.5（2021-07-30）
- 修复（重要） H5 平台下如果和uni-forms组件一同使用导致页面卡死的问题
## 0.2.3（2021-07-28）
- 优化 调整示例代码
## 0.2.2（2021-07-27）
- 修复 vue3 下赋值错误的Bug
- 优化 h5平台下上传文件导致页面卡死的问题
## 0.2.0（2021-07-13）
- 组件兼容 vue3，如何创建vue3项目，详见 [uni-app 项目支持 vue3 介绍](https://ask.dcloud.net.cn/article/37834)
## 0.1.1（2021-07-02）
- 修复 sourceType 缺少默认值导致 ios 无法选择文件
## 0.1.0（2021-06-30）
- 优化 解耦与uniCloud的强绑定关系 ，如不绑定服务空间，默认autoUpload为false且不可更改
## 0.0.11（2021-06-30）
- 修复 由 0.0.10 版本引发的 returnType 属性失效的问题
## 0.0.10（2021-06-29）
- 优化 文件上传后进度条消失时机
## 0.0.9（2021-06-29）
- 修复 在uni-forms 中，删除文件 ，获取的值不对的Bug
## 0.0.8（2021-06-15）
- 修复 删除文件时无法触发 v-model 的Bug
## 0.0.7（2021-05-12）
- 新增 组件示例地址
## 0.0.6（2021-04-09）
- 修复 选择的文件非 file-extname 字段指定的扩展名报错的Bug
## 0.0.5（2021-04-09）
- 优化 更新组件示例
## 0.0.4（2021-04-09）
- 优化 file-extname 字段支持字符串写法，多个扩展名需要用逗号分隔
## 0.0.3（2021-02-05）
- 调整为uni_modules目录规范
- 修复 微信小程序不指定 fileExtname 属性选择失败的Bug
