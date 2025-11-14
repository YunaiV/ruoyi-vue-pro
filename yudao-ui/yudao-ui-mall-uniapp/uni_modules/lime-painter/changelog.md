## 1.9.6.6（2024-09-25）
- fix: 修复background-position无效的问题
## 1.9.6.5（2024-04-14）
- fix: 修复`nvue`无法生图的问题
## 1.9.6.4（2024-03-10）
- fix: 修复代理ctx导致H5不能使用ctx.save
## 1.9.6.3（2024-03-08）
- fix: 修复支付宝真机无法使用的问题
## 1.9.6.2（2024-02-22）
- fix: 修复使用render函数报错的问题
## 1.9.6.1（2023-12-22）
- fix: 修复字节小程序非2d字体偏移
- fix: 修复`canvasToTempFilePathSync`会触发两次的问题
- fix: 修复`parser`图片没有宽度的问题
## 1.9.6（2023-12-06）
- fix: 修复背景图受padding影响
- fix: 修复因字节报错改了代理实现导致微信报错
- 1.9.5.8（2023-11-16）
- fix: 修复margin问题
- fix: 修复borderWidth问题
- fix: 修复textBox问题
- fix: 修复字节开发工具报`could not be cloned.`问题
## 1.9.5.7（2023-07-27）
- fix: 去掉多余的方法
- chore: 更新文档，增加自定义字体说明
## 1.9.5.6（2023-07-21）
- feat: 有限的支持富文本
- feat: H5和APP 增加 `hidpi` prop，主要用于大尺寸无法生成图片时用
- fix: 修复 钉钉小程序 缺少 `measureText` 方法
- chore: 由于微信小程序 pc 端的 canvas 2d 时不时抽风，故不使用canvas 2d
## 1.9.5.5（2023-06-27）
- fix: 修复把`emoji`表情字符拆分成多个字符的情况
## 1.9.5.4（2023-06-05）
- fix: 修复因`canvasToTempFilePathSync`监听导致重复调用
## 1.9.5.3（2023-05-23）
- fix: 因isPc错写成了isPC导致小程序PC不能生成图片
## 1.9.5.2（2023-05-22）
- feat: 删除多余文件
## 1.9.5.1（2023-05-22）
- fix: 修复 文字行数与`line-clamp`相同但不满一行时也加了省略号的问题
## 1.9.5（2023-05-14）
- feat: 增加 `text-indent` 和 `calc` 方法
- feat: 优化 布局时间
## 1.9.4.4（2023-04-15）
- fix: 修复无法匹配负值
- fix: 修复 Nvue IOS getImageInfo `useCORS` 为 undefined
## 1.9.4.3（2023-04-01）
- feat: 增加支持文字描边 `text-stroke: '5rpx #fff'`
## 1.9.4.2（2023-03-30）
- fix: 修复 支付宝小程序 isPC 在手机也为true的问题
- feat: 由 微信开发工具 3060 版 无法获取图片尺寸，现 微信开发工具 3220 版 修复该问题，故还原上一版的获取图片方式。
## 1.9.4.1（2023-03-28）
- fix: 修复固定高度不正确问题
## 1.9.4（2023-03-17）
- fix: nvue ios getImageInfo缺少this报错
- fix: pathType 非2d无效问题
- fix: 修复 小米9se 可能会存在多次init 导致画面多次放大
- fix: 修复 border 分开写 width style无效问题
- fix: 修复 支付宝小程序IOS 再次进入不渲染的问题
- fix: 修复 支付宝小程序安卓Zindex排序错乱问题
- fix: 修复 微信开发工具 3060 版 无法获取图片的问题
- feat: 把 for in 改为 forEach
- feat: 增加 hidden
- feat: 根节点 box-sizing 默认 `border-box`
- feat: 增加支持 `vw` `wh`
- chore: pathType 取消 默认值，因为字节开发工具不能显示
- chore: 支付宝小程序开发工具不支持 生成图片 请以真机调试为准
- bug: 企业微信 2.20.3无法使用
## 1.9.3.5（2022-06-29）
- feat: justifyContent 增加 `space-around`、`space-between`
- feat: canvas 2d 也使用`getImageInfo`
- fix: 修复 `text`的 `text-decoration`错位
## 1.9.3.4（2022-06-20）
- fix: 修复 因创建节点速度问题导致顺序出错。 
- fix: 修复 微信小程序 PC 无法显示本地图片 
- fix: 修复 flex-box 对齐问题 
- feat: 增加 `text-shadow`
- feat: 重写 `text` 对齐方式
- chore: 更新文档
## 1.9.3.3（2022-06-17）
- fix: 修复 支付宝小程序 canvas 2d 存在ctx.draw问题导致报错
- fix: 修复 支付宝小程序 toDataURL 存在权限问题改用 `toTempFilePath`
- fix: 修复 支付宝小程序 image size 问题导致 `objectFit` 无效
## 1.9.3.2（2022-06-14）
- fix: 修复 image 设置背景色不生效问题
- fix: 修复 nvue 环境判断缺少参数问题
## 1.9.3.1（2022-06-14）
- fix: 修复 bottom 定位不对问题
- fix: 修复 因小数导致计算出错换行问题
- feat: 增加 `useCORS` h5端图片跨域 在设置请求头无效果后试一下设置这个值
- chore: 更新文档
## 1.9.3（2022-06-13）
- feat: 增加 `zIndex`
- feat: 增加 `flex-box` 该功能处于原始阶段，非常简陋。
- tips: QQ小程序 vue3 不支持, 为 uni 官方BUG
## 1.9.2.9（2022-06-10）
- fix: 修复`text-align`及`margin`居中问题
## 1.9.2.8（2022-06-10）
- fix: 修复 Nvue `canvasToTempFilePathSync` 不生效问题
## 1.9.2.7（2022-06-10）
- fix: 修复 margin及padding的bug
- fix: 修复 Nvue `isCanvasToTempFilePath` 不生效问题
## 1.9.2.6（2022-06-09）
- fix: 修复 Nvue 不显示
- feat: 增加支持字体渐变
```html
<l-painter-text 
	text="水调歌头\n明月几时有？把酒问青天。不知天上宫阙，今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。"
	css="background: linear-gradient(,#ff971b 0%, #1989fa 100%); background-clip: text" />
```
## 1.9.2.5（2022-06-09）
- chore: 更变获取父级宽度的设定
- chore: `pathType` 在canvas 2d 默认为 `url`
## 1.9.2.4（2022-06-08）
- fix: 修复 `pathType` 不生效问题
## 1.9.2.3（2022-06-08）
- fix: 修复 `canvasToTempFilePath` 漏写 `success` 参数
## 1.9.2.2（2022-06-07）
- chore: 更新文档
## 1.9.2.1（2022-06-07）
- fix: 修复 vue3 赋值给this再传入导致image无法绘制
- fix: 修复 `canvasToTempFilePathSync` 时机问题
- feat: canvas 2d 更改图片生成方式 `toDataURL` 
## 1.9.2（2022-05-30）
- fix: 修复 `canvasToTempFilePathSync` 在 vue3 下只生成一次
## 1.9.1.7（2022-05-28）
- fix: 修复 `qrcode`显示不全问题
## 1.9.1.6（2022-05-28）
- fix: 修复 `canvasToTempFilePathSync` 会重复多次问题
- fix: 修复 `view` css `backgroundImage` 图片下载失败导致 子节点不渲染
## 1.9.1.5（2022-05-27）
- fix: 修正支付宝小程序 canvas 2d版本号 2.7.15
## 1.9.1.4（2022-05-22）
- fix: 修复字节小程序无法使用xml方式
- fix: 修复字节小程序无法使用base64(非2D情况下工具上无法显示)
- fix: 修复支付宝小程序 `canvasToTempFilePath` 报错
## 1.9.1.3（2022-04-29）
- fix: 修复vue3打包后uni对象为空后的报错
## 1.9.1.2（2022-04-25）
- fix: 删除多余文件
## 1.9.1.1（2022-04-25）
- fix: 修复图片不显示问题
## 1.9.1（2022-04-12）
- fix: 因四舍五入导致有些机型错位
- fix: 修复无views报错 
- chore: nvue下因ios无法读取插件内static文件，改由下载方式
## 1.9.0（2022-03-20）
- fix: 因无法固定尺寸导致生成图片不全
- fix: 特定情况下text判断无效
- chore: 本地化APP Nvue webview
## 1.8.9（2022-02-20）
- fix: 修复 小程序下载最多10次并发的问题
- fix: 修复 APP端无法获取本地图片
- fix: 修复 APP Nvue端不执行问题
- chore: 增加图片缓存机制
## 1.8.8.8（2022-01-27）
- fix: 修复 主动调用尺寸问题
## 1.8.8.6（2022-01-26）
- fix: 修复 nvue 下无宽度时获取父级宽度 
- fix: 修复 ios app 无法渲染问题
## 1.8.8（2022-01-23）
- fix: 修复 主动调用时无节点问题
- fix: 修复 `box-shadow` 颜色问题
- fix: 修复 `transform:rotate` 角度位置问题
- feat: 增加 `overflow:hidden`
## 1.8.7（2022-01-07）
- fix: 修复 image 方向为 `right` 时原始宽高问题
- feat: 支持 view 设置背景图 `background-image: url(xxx)`
- chore: 去掉可选链
## 1.8.6（2021-11-28）
- feat: 支持`view`对`inline-block`的子集使用`text-align`
## 1.8.5.5（2021-08-17）
- chore: 更新文档，删除 replace
- fix: 修复 text 值为 number时报错
## 1.8.5.4（2021-08-16）
- fix: 字节小程序兼容
## 1.8.5.3（2021-08-15）
- fix: 修复线性渐变与css现实效果不一致的问题
- chore: 更新文档
## 1.8.5.2（2021-08-13）
- chore: 增加`background-image`、`background-repeat` 能力，主要用于背景纹理的绘制，并不是代替`image`。例如：大面积的重复平铺的水印
- 注意：这个功能H5暂时无法使用，因为[官方的API有BUG](https://ask.dcloud.net.cn/question/128793)，待官方修复！！！
## 1.8.5.1（2021-08-10）
- fix: 修复因`margin`报错问题
## 1.8.5（2021-08-09）
- chore: 增加margin支持`auto`,以达到居中效果
## 1.8.4（2021-08-06）
- chore: 增加判断缓存文件条件
- fix: 修复css 多余空格报错问题
## 1.8.3（2021-08-04）
- tips: 1.6.x 以下的版本升级到1.8.x后要为每个元素都加上定位：position: 'absolute'
- fix: 修复只有一个view子元素时不计算高度的问题
## 1.8.2（2021-08-03）
- fix: 修复 path-type 为 `url` 无效问题
- fix: 修复 qrcode `text` 为空时报错问题
- fix: 修复 image `src` 动态设置时不生效问题
- feat: 增加 css 属性 `min-width` `max-width`
## 1.8.1（2021-08-02）
- fix: 修复无法加载本地图片
## 1.8.0（2021-08-02）
- chore 文档更新
- 使用旧版的同学不要升级！
## 1.8.0-beta（2021-07-30）
- ## 全新布局方式 不兼容旧版！
- chore: 布局方式变更
- tips: 微信canvas 2d 不支持真机调试
## 1.6.6（2021-07-09）
- chore: 统一命名规范，无须主动引入组件
## 1.6.5（2021-06-08）
- chore: 去掉console
## 1.6.4（2021-06-07）
- fix: 修复 数字 为纯字符串时不转换的BUG
## 1.6.3（2021-06-06）
- fix: 修复 PC 端放大的BUG
## 1.6.2（2021-05-31）
- fix: 修复 报`adaptor is not a function`错误
- fix: 修复 text 多行高度
- fix: 优化 默认文字的基准线
- feat: `@progress`事件，监听绘制进度
## 1.6.1（2021-02-28）
- 删除多余节点
## 1.6.0（2021-02-26）
- 调整为uni_modules目录规范
- 修复：transform的rotate不能为负数问题
- 新增：`pathType` 指定生成图片返回的路径类型,可选值有 `base64`、`url`
