## 2.0.34（2022-09-25）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. `u-input`、`u-textarea`增加`ignoreCompositionEvent`属性
2. 修复`route`方法调用可能报错的问题
3. 修复`u-no-network`组件`z-index`无效的问题
4. 修复`textarea`组件在h5上confirmType=""报错的问题
5. `u-rate`适配`nvue`
6. 优化验证手机号码的正则表达式(根据工信部发布的《电信网编号计划（2017年版）》进行修改。)
7. `form-item`添加`labelPosition`属性
8. `u-calendar`修复`maxDate`设置为当前日期，并且当前时间大于08：00时无法显示日期列表的问题 (#724)
9. `u-radio`增加一个默认插槽用于自定义修改label内容 (#680)
10. 修复`timeFormat`函数在safari重的兼容性问题 (#664)
## 2.0.33（2022-06-17）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复`loadmore`组件`lineColor`类型错误问题
2. 修复`u-parse`组件`imgtap`、`linktap`不生效问题
## 2.0.32（2022-06-16）
# uView2.0重磅发布，利剑出鞘，一统江湖
1. `u-loadmore`新增自定义颜色、虚/实线
2. 修复`u-swiper-action`组件部分平台不能上下滑动的问题
3. 修复`u-list`回弹问题
4. 修复`notice-bar`组件动画在低端安卓机可能会抖动的问题
5. `u-loading-page`添加控制图标大小的属性`iconSize`
6. 修复`u-tooltip`组件`color`参数不生效的问题
7. 修复`u--input`组件使用`blur`事件输出为`undefined`的bug
8. `u-code-input`组件新增键盘弹起时，是否自动上推页面参数`adjustPosition`
9. 修复`image`组件`load`事件无回调对象问题
10. 修复`button`组件`loadingSize`设置无效问题
10. 其他修复
## 2.0.31（2022-04-19）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复`upload`在`vue`页面上传成功后没有成功标志的问题
2. 解决演示项目中微信小程序模拟上传图片一直出于上传中问题
3. 修复`u-code-input`组件在`nvue`页面编译到`app`平台上光标异常问题（`app`去除此功能）
4. 修复`actionSheet`组件标题关闭按钮点击事件名称错误的问题
5. 其他修复
## 2.0.30（2022-04-04）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. `u-rate`增加`readonly`属性
2. `tabs`滑块支持设置背景图片
3. 修复`u-subsection` `mode`为`subsection`时，滑块样式不正确的问题
4. `u-code-input`添加光标效果动画
5. 修复`popup`的`open`事件不触发
6. 修复`u-flex-column`无效的问题
7. 修复`u-datetime-picker`索引在特定场合异常问题
8. 修复`u-datetime-picker`最小时间字符串模板错误问题
9. `u-swiper`添加`m3u8`验证
10. `u-swiper`修改判断image和video逻辑
11. 修复`swiper`无法使用本地图片问题，增加`type`参数
12. 修复`u-row-notice`格式错误问题
13. 修复`u-switch`组件当`unit`为`rpx`时,`nodeStyle`消失的问题
14. 修复`datetime-picker`组件`showToolbar`与`visibleItemCount`属性无效的问题
15. 修复`upload`组件条件编译位置判断错误，导致`previewImage`属性设置为`false`时，整个组件都会被隐藏的问题
16. 修复`u-checkbox-group`设置`shape`属性无效的问题
17. 修复`u-upload`的`capture`传入字符串的时候不生效的问题
18. 修复`u-action-sheet`组件，关闭事件逻辑错误的问题
19. 修复`u-list`触顶事件的触发错误的问题
20. 修复`u-text`只有手机号可拨打的问题
21. 修复`u-textarea`不能换行的问题
22. 其他修复
## 2.0.29（2022-03-13）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复`u--text`组件设置`decoration`属性未生效的问题
2. 修复`u-datetime-picker`使用`formatter`后返回值不正确
3. 修复`u-datetime-picker` `intercept` 可能为undefined
4. 修复已设置单位 uni..config.unit = 'rpx'时，线型指示器 `transform` 的位置翻倍，导致指示器超出宽度
5. 修复mixin中bem方法生成的类名在支付宝和字节小程序中失效
6. 修复默认值传值为空的时候，打开`u-datetime-picker`报错，不能选中第一列时间的bug
7. 修复`u-datetime-picker`使用`formatter`后返回值不正确
8. 修复`u-image`组件`loading`无效果的问题
9. 修复`config.unit`属性设为`rpx`时，导航栏占用高度不足导致塌陷的问题
10. 修复`u-datetime-picker`组件`itemHeight`无效问题
11. 其他修复
## 2.0.28（2022-02-22）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. search组件新增searchIconSize属性
2. 兼容Safari/Webkit中传入时间格式如2022-02-17 12:00:56
3. 修复text value.js 判断日期出format错误问题
4. priceFormat格式化金额出现精度错误
5. priceFormat在部分情况下出现精度损失问题
6. 优化表单rules提示
7. 修复avatar组件src为空时，展示状态不对
8. 其他修复
## 2.0.27（2022-01-28）
# uView2.0重磅发布，利剑出鞘，一统江湖

1.样式修复
## 2.0.26（2022-01-28）
# uView2.0重磅发布，利剑出鞘，一统江湖

1.样式修复
## 2.0.25（2022-01-27）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复text组件mode=price时，可能会导致精度错误的问题
2. 添加$u.setConfig()方法，可设置uView内置的config, props, zIndex, color属性，详见：[修改uView内置配置方案](https://uviewui.com/components/setting.html#%E9%BB%98%E8%AE%A4%E5%8D%95%E4%BD%8D%E9%85%8D%E7%BD%AE)
3. 优化form组件在errorType=toast时，如果输入错误页面会有抖动的问题
4. 修复$u.addUnit()对配置默认单位可能无效的问题
## 2.0.24（2022-01-25）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复swiper在current指定非0时缩放有误
2. 修复u-icon添加stop属性的时候报错
3. 优化遗留的通过正则判断rpx单位的问题
4. 优化Layout布局 vue使用gutter时，会超出固定区域
5. 优化search组件高度单位问题（rpx -> px）
6. 修复u-image slot 加载和错误的图片失去了高度
7. 修复u-index-list中footer插槽与header插槽存在性判断错误
8. 修复部分机型下u-popup关闭时会闪烁
9. 修复u-image在nvue-app下失去宽高
10. 修复u-popup运行报错
11. 修复u-tooltip报错
12. 修复box-sizing在app下的警告
13. 修复u-navbar在小程序中报运行时错误
14. 其他修复
## 2.0.23（2022-01-24）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复image组件在hx3.3.9的nvue下可能会显示异常的问题
2. 修复col组件gutter参数带rpx单位处理不正确的问题
3. 修复text组件单行时无法显示省略号的问题
4. navbar添加titleStyle参数
5. 升级到hx3.3.9可消除nvue下控制台样式警告的问题
## 2.0.22（2022-01-19）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. $u.page()方法优化，避免在特殊场景可能报错的问题
2. picker组件添加immediateChange参数
3. 新增$u.pages()方法
## 2.0.21（2022-01-19）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 优化：form组件在用户设置rules的时候提示用户model必传
2. 优化遗留的通过正则判断rpx单位的问题
3. 修复微信小程序环境中tabbar组件开启safeAreaInsetBottom属性后，placeholder高度填充不正确
4. 修复swiper在current指定非0时缩放有误
5. 修复u-icon添加stop属性的时候报错
6. 修复upload组件在accept=all的时候没有作用
7. 修复在text组件mode为phone时call属性无效的问题
8. 处理u-form clearValidate方法
9. 其他修复
## 2.0.20（2022-01-14）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复calendar默认会选择一个日期，如果直接点确定的话，无法取到值的问题
2. 修复Slider缺少disabled props 还有注释
3. 修复u-notice-bar点击事件无法拿到index索引值的问题
4. 修复u-collapse-item在vue文件下，app端自定义插槽不生效的问题
5. 优化头像为空时显示默认头像 
6. 修复图片地址赋值后判断加载状态为完成问题
7. 修复日历滚动到默认日期月份区域
8. search组件暴露点击左边icon事件
9. 修复u-form clearValidate方法不生效
10. upload h5端增加返回文件参数（文件的name参数）
11. 处理upload选择文件后url为blob类型无法预览的问题
12. u-code-input 修复输入框没有往左移出一半屏幕
13. 修复Upload上传 disabled为true时，控制台报hoverClass类型错误
14. 临时处理ios app下grid点击坍塌问题
15. 其他修复
## 2.0.19（2021-12-29）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 优化微信小程序包体积可在微信中预览，请升级HbuilderX3.3.4，同时在“运行->运行到小程序模拟器”中勾选“运行时是否压缩代码”
2. 优化微信小程序setData性能，处理某些方法如$u.route()无法在模板中使用的问题
3. navbar添加autoBack参数
4. 允许avatar组件的事件冒泡
5. 修复cell组件报错问题
6. 其他修复
## 2.0.18（2021-12-28）
# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复app端编译报错问题
2. 重新处理微信小程序端setData过大的性能问题
3. 修复边框问题
4. 修复最大最小月份不大于0则没有数据出现的问题
5. 修复SwipeAction微信小程序端无法上下滑动问题
6. 修复input的placeholder在小程序端默认显示为true问题
7. 修复divider组件click事件无效问题
8. 修复u-code-input maxlength 属性值为 String 类型时显示异常
9. 修复当 grid只有 1到2时 在小程序端algin设置无效的问题
10. 处理form-item的label为top时，取消错误提示的左边距
11. 其他修复
## 2.0.17（2021-12-26）
## uView正在参与开源中国的“年度最佳项目”评选，之前投过票的现在也可以投票，恳请同学们投一票，[点此帮助uView](https://www.oschina.net/project/top_cn_2021/?id=583)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 解决HBuilderX3.3.3.20211225版本导致的样式问题
2. calendar日历添加monthNum参数
3. navbar添加center slot
## 2.0.16（2021-12-25）
## uView正在参与开源中国的“年度最佳项目”评选，之前投过票的现在也可以投票，恳请同学们投一票，[点此帮助uView](https://www.oschina.net/project/top_cn_2021/?id=583)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 解决微信小程序setData性能问题
2. 修复count-down组件change事件不触发问题
## 2.0.15（2021-12-21）
## uView正在参与开源中国的“年度最佳项目”评选，之前投过票的现在也可以投票，恳请同学们投一票，[点此帮助uView](https://www.oschina.net/project/top_cn_2021/?id=583)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复Cell单元格titleWidth无效
2. 修复cheakbox组件ischecked不更新
3. 修复keyboard是否显示"."按键默认值问题
4. 修复number-keyboard是否显示键盘的"."符号问题
5. 修复Input输入框 readonly无效
6. 修复u-avatar 导致打包app、H5时候报错问题
7. 修复Upload上传deletable无效
8. 修复upload当设置maxSize时无效的问题
9. 修复tabs lineWidth传入带单位的字符串的时候偏移量计算错误问题
10. 修复rate组件在有padding的view内，显示的星星位置和可触摸区域不匹配，无法正常选中星星
## 2.0.13（2021-12-14）
## [点击加群交流反馈：364463526](https://jq.qq.com/?_chanwv=1027&k=mCxS3TGY)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复配置默认单位为rpx可能会导致自定义导航栏高度异常的问题
## 2.0.12（2021-12-14）
## [点击加群交流反馈：364463526](https://jq.qq.com/?_chanwv=1027&k=mCxS3TGY)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复tabs组件在vue环境下划线消失的问题
2. 修复upload组件在安卓小程序无法选择视频的问题
3. 添加uni.$u.config.unit配置，用于配置参数默认单位，详见：[默认单位配置](https://www.uviewui.com/components/setting.html#%E9%BB%98%E8%AE%A4%E5%8D%95%E4%BD%8D%E9%85%8D%E7%BD%AE)
4. 修复textarea组件在没绑定v-model时，字符统计不生效问题
5. 修复nvue下控制是否出现滚动条失效问题
## 2.0.11（2021-12-13）
## [点击加群交流反馈：364463526](https://jq.qq.com/?_chanwv=1027&k=mCxS3TGY)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. text组件align参数无效的问题
2. subsection组件添加keyName参数
3. upload组件无法判断[Object file]类型的问题
4. 处理notify层级过低问题
5. codeInput组件添加disabledDot参数
6. 处理actionSheet组件round参数无效的问题
7. calendar组件添加round参数用于控制圆角值
8. 处理swipeAction组件在vue环境下默认被打开的问题
9. button组件的throttleTime节流参数无效的问题
10. 解决u-notify手动关闭方法close()无效的问题
11. input组件readonly不生效问题
12. tag组件type参数为info不生效问题
## 2.0.10（2021-12-08）
## [点击加群交流反馈：364463526](https://jq.qq.com/?_chanwv=1027&k=mCxS3TGY)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复button sendMessagePath属性不生效
2. 修复DatetimePicker选择器title无效
3. 修复u-toast设置loading=true不生效
4. 修复u-text金额模式传0报错
5. 修复u-toast组件的icon属性配置不生效
6. button的icon在特殊场景下的颜色优化
7. IndexList优化，增加#
## 2.0.9（2021-12-01）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 优化swiper的height支持100%值(仅vue有效)，修复嵌入视频时click事件无法触发的问题
2. 优化tabs组件对list值为空的判断，或者动态变化list时重新计算相关尺寸的问题
3. 优化datetime-picker组件逻辑，让其后续打开的默认值为上一次的选中值，需要通过v-model绑定值才有效
4. 修复upload内嵌在其他组件中，选择图片可能不会换行的问题
## 2.0.8（2021-12-01）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复toast的position参数无效问题
2. 处理input在ios nvue上无法获得焦点的问题
3. avatar-group组件添加extraValue参数，让剩余展示数量可手动控制
4. tabs组件添加keyName参数用于配置从对象中读取的键名
5. 处理text组件名字脱敏默认配置无效的问题
6. 处理picker组件item文本太长换行问题
## 2.0.7（2021-11-30）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 修复radio和checkbox动态改变v-model无效的问题。
2. 优化form规则validator在微信小程序用法
3. 修复backtop组件mode参数在微信小程序无效的问题
4. 处理Album的previewFullImage属性无效的问题
5. 处理u-datetime-picker组件mode='time'在选择改变时间时，控制台报错的问题
## 2.0.6（2021-11-27）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. 处理tag组件在vue下边框无效的问题。
2. 处理popup组件圆角参数可能无效的问题。
3. 处理tabs组件lineColor参数可能无效的问题。
4. propgress组件在值很小时，显示异常的问题。
## 2.0.5（2021-11-25）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. calendar在vue下显示异常问题。 
2. form组件labelPosition和errorType参数无效的问题
3. input组件inputAlign无效的问题
4. 其他一些修复
## 2.0.4（2021-11-23）
## [点击加群交流反馈：232041042](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

0. input组件缺失@confirm事件，以及subfix和prefix无效问题
1. component.scss文件样式在vue下干扰全局布局问题
2. 修复subsection在vue环境下表现异常的问题
3. tag组件的bgColor等参数无效的问题
4. upload组件不换行的问题
5. 其他的一些修复处理
## 2.0.3（2021-11-16）
## [点击加群交流反馈：1129077272](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. uView2.0已实现全面兼容nvue
2. uView2.0对1.x进行了架构重构，细节和性能都有极大提升
3. 目前uView2.0为公测阶段，相关细节可能会有变动
4. 我们写了一份与1.x的对比指南，详见[对比1.x](https://www.uviewui.com/components/diff1.x.html)
5. 处理modal的confirm回调事件拼写错误问题
6. 处理input组件@input事件参数错误问题
7. 其他一些修复
## 2.0.2（2021-11-16）
## [点击加群交流反馈：1129077272](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. uView2.0已实现全面兼容nvue
2. uView2.0对1.x进行了架构重构，细节和性能都有极大提升
3. 目前uView2.0为公测阶段，相关细节可能会有变动
4. 我们写了一份与1.x的对比指南，详见[对比1.x](https://www.uviewui.com/components/diff1.x.html)
5. 修复input组件formatter参数缺失问题
6. 优化loading-icon组件的scss写法问题，防止不兼容新版本scss
## 2.0.0(2020-11-15)
## [点击加群交流反馈：1129077272](https://jq.qq.com/?_wv=1027&k=KnbeceDU)

# uView2.0重磅发布，利剑出鞘，一统江湖

1. uView2.0已实现全面兼容nvue
2. uView2.0对1.x进行了架构重构，细节和性能都有极大提升
3. 目前uView2.0为公测阶段，相关细节可能会有变动
4. 我们写了一份与1.x的对比指南，详见[对比1.x](https://www.uviewui.com/components/diff1.x.html)
5. 修复input组件formatter参数缺失问题


