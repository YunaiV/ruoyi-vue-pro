## v2.5.0（2024-04-22）
1. `U` `play` 事件增加返回 `src` 等信息 [详细](https://github.com/jin-yufeng/mp-html/issues/526)
2. `U` `preview-img` 属性支持设置为 `all` 开启 `base64` 图片预览 [详细](https://github.com/jin-yufeng/mp-html/issues/536)
3. `U` `editable` 插件增加简易模式（点击文字直接编辑）
4. `U` `latex` 插件支持块级公式 [详细](https://github.com/jin-yufeng/mp-html/issues/582)
5. `F` 修复了表格部分情况下背景丢失的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/587)
6. `F` 修复了部分 `svg` 无法显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/591)
7. `F` 修复了 `h5` 和 `app` 端部分情况下样式无法识别的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/518)
8. `F` 修复了 `latex` 插件部分情况下显示不正确的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/580)
9. `F` 修复了 `editable` 插件表格无法删除的问题
10. `F` 修复了 `editable` 插件 `vue3` `h5` 端点击图片报错的问题
11. `F` 修复了 `editable` 插件点击表格没有菜单栏的问题
## v2.4.3（2024-01-21）
1. `A` 增加 [card](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#card) 插件 [详细](https://github.com/jin-yufeng/mp-html/pull/533) by [@whoooami](https://github.com/whoooami)
2. `F` 修复了 `svg` 中包含 `foreignobject` 可能不显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/523)
3. `F` 修复了合并单元格的表格部分情况下显示不正确的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/561)
4. `F` 修复了 `img` 标签设置 `object-fit` 无效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/567)
5. `F` 修复了 `latex` 插件公式会换行的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/540) 
6. `F` 修复了 `editable` 和 `audio` 插件共用时点击 `audio` 无法编辑的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/529) by [@whoooami](https://github.com/whoooami)
7. `F` 修复了微信小程序部分情况下图片会报错 `replace of undefined` 的问题
8. `F` 修复了快手小程序图片不显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/571)
## v2.4.2（2023-05-14）
1. `A` `editable` 插件支持修改文字颜色 [详细](https://github.com/jin-yufeng/mp-html/issues/254)
2. `F` 修复了 `svg` 中有 `style` 不生效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/505)
3. `F` 修复了使用旧版编译器可能报错 `Bad attr nodes` 的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/472)
4. `F` 修复了 `app` 端可能出现无法读取 `lazyLoad` 的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/513)
5. `F` 修复了 `editable` 插件在点击换图时未拼接 `domain` 的问题 [详细](https://github.com/jin-yufeng/mp-html/pull/497) by [@TwoKe945](https://github.com/TwoKe945)
6. `F` 修复了 `latex` 插件部分情况下不显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/515) 
7. `F` 修复了 `editable` 插件点击音视频时其他标签框不消失的问题
## v2.4.1（2022-12-25）
1. `F` 修复了没有图片时 `ready` 事件可能不触发的问题
2. `F` 修复了加载过程中可能出现 `Root label not found` 错误的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/470)
3. `F` 修复了 `audio` 插件退出页面可能会报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/457)
4. `F` 修复了 `vue3` 运行到 `app` 在 `HBuilder X 3.6.10` 以上报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/480)
5. `F` 修复了 `nvue` 端链接中包含 `%22` 时可能无法显示的问题
6. `F` 修复了 `vue3` 使用 `highlight` 插件可能报错的问题
## v2.4.0（2022-08-27）
1. `A` 增加了 [setPlaybackRate](https://jin-yufeng.gitee.io/mp-html/#/advanced/api#setPlaybackRate) 的 `api`，可以设置音视频的播放速率 [详细](https://github.com/jin-yufeng/mp-html/issues/452)
2. `A` 示例小程序代码开源 [详细](https://github.com/jin-yufeng/mp-html-demo)
3. `U` 优化 `ready` 事件触发时机，未设置懒加载的情况下基本可以准确触发 [详细](https://github.com/jin-yufeng/mp-html/issues/195)
4. `U` `highlight` 插件在编辑状态下不进行高亮处理，便于编辑
5. `F` 修复了 `flex` 布局下图片大小可能不正确的问题
6. `F` 修复了 `selectable` 属性没有设置 `force` 也可能出现渲染异常的问题
7. `F` 修复了表格中的图片大小可能不正确的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/448)
8. `F` 修复了含有合并单元格的表格可能无法设置竖直对齐的问题
9. `F` 修复了 `editable` 插件在 `scroll-view` 中使用时工具条位置可能不正确的问题
10. `F` 修复了 `vue3` 使用 [search](advanced/plugin#search) 插件可能导致错误换行的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/449)
## v2.3.2（2022-08-13）
1. `A` 增加 [latex](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#latex) 插件，可以渲染数学公式 [详细](https://github.com/jin-yufeng/mp-html/pull/447) by [@Zeng-J](https://github.com/Zeng-J)
2. `U` 优化根节点下有很多标签的长内容渲染速度
3. `U` `highlight` 插件适配 `lang-xxx` 格式
4. `F` 修复了 `table` 标签设置 `border` 属性后可能无法修改边框样式的问题 [详细](https://github.com/jin-yufeng/mp-html/pull/439) by [@zouxingjie](https://github.com/zouxingjie)
5. `F` 修复了 `editable` 插件输入连续空格无效的问题
6. `F` 修复了 `vue3` 图片设置 `inline` 会报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/438)
7. `F` 修复了 `vue3` 使用 `table` 可能报错的问题
## v2.3.1（2022-05-20）
1. `U` `app` 端支持使用本地图片
2. `U` 优化了微信小程序 `selectable` 属性在 `ios` 端的处理 [详细](https://jin-yufeng.gitee.io/mp-html/#/basic/prop#selectable)
3. `F` 修复了 `editable` 插件不在顶部时 `tooltip` 位置可能错误的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/430)
4. `F` 修复了 `vue3` 运行到微信小程序可能报错丢失内容的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/414)
5. `F` 修复了 `vue3` 部分标签可能被错误换行的问题
6. `F` 修复了 `editable` 插件 `app` 端插入视频无法预览的问题
## v2.3.0（2022-04-01）
1. `A` 增加了 `play` 事件，音视频播放时触发，可用于与页面其他音视频进行互斥播放 [详细](basic/event#play)
2. `U` `show-img-menu` 属性支持控制预览时是否长按弹出菜单
3. `U` 优化 `wxs` 处理，提高渲染性能 [详细](https://developers.weixin.qq.com/community/develop/article/doc/0006cc2b204740f601bd43fa25a413)  
4. `U` `video` 标签支持 `object-fit` 属性
5. `U` 增加支持一些常用实体编码 [详细](https://github.com/jin-yufeng/mp-html/issues/418)
6. `F` 修复了图片仅设置高度可能不显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/410)
7. `F` 修复了 `video` 标签高度设置为 `auto` 不显示的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/411)
8. `F` 修复了使用 `grid` 布局时可能样式错误的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/413)
9. `F` 修复了含有合并单元格的表格部分情况下显示异常的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/417)
10. `F` 修复了 `editable` 插件连续插入内容时顺序不正确的问题
11. `F` 修复了 `uni-app` 包 `vue3` 使用 `audio` 插件报错的问题
12. `F` 修复了 `uni-app` 包 `highlight` 插件使用自定义的 `prism.min.js` 报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/416)
## v2.2.2（2022-02-26）
1. `A` 增加了 [pauseMedia](https://jin-yufeng.gitee.io/mp-html/#/advanced/api#pauseMedia) 的 `api`，可用于暂停播放音视频 [详细](https://github.com/jin-yufeng/mp-html/issues/317)
2. `U` 优化了长内容的加载速度  
3. `U` 适配 `vue3` [#389](https://github.com/jin-yufeng/mp-html/issues/389)、[#398](https://github.com/jin-yufeng/mp-html/pull/398) by [@zhouhuafei](https://github.com/zhouhuafei)、[#400](https://github.com/jin-yufeng/mp-html/issues/400)
4. `F` 修复了小程序端图片高度设置为百分比时可能不显示的问题
5. `F` 修复了 `highlight` 插件部分情况下可能显示不完整的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/403)
## v2.2.1（2021-12-24）
1. `A` `editable` 插件增加上下移动标签功能
2. `U` `editable` 插件支持在文本中间光标处插入内容
3. `F` 修复了 `nvue` 端设置 `margin` 后可能导致高度不正确的问题
4. `F` 修复了 `highlight` 插件使用压缩版的 `prism.css` 可能导致背景失效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/367)
5. `F` 修复了编辑状态下使用 `emoji` 插件内容为空时可能报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/371)
6. `F` 修复了使用 `editable` 插件后将 `selectable` 属性设置为 `force` 不生效的问题
## v2.2.0（2021-10-12）
1. `A` 增加 `customElements` 配置项，便于添加自定义功能性标签 [详细](https://github.com/jin-yufeng/mp-html/issues/350)
2. `A` `editable` 插件增加切换音视频自动播放状态的功能 [详细](https://github.com/jin-yufeng/mp-html/pull/341) by [@leeseett](https://github.com/leeseett)
3. `A` `editable` 插件删除媒体标签时触发 `remove` 事件，便于删除已上传的文件
4. `U` `editable` 插件 `insertImg` 方法支持同时插入多张图片 [详细](https://github.com/jin-yufeng/mp-html/issues/342)
5. `U` `editable` 插入图片和音视频时支持拼接 `domian` 主域名
6. `F` 修复了内部链接参数中包含 `://` 时被认为是外部链接的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/356)
7. `F` 修复了部分 `svg` 标签名或属性名大小写不正确时不生效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/351)
8. `F` 修复了 `nvue` 页面运行到非 `app` 平台时可能样式错误的问题
## v2.1.5（2021-08-13）
1. `A` 增加支持标签的 `dir` 属性
2. `F` 修复了 `ruby` 标签文字与拼音没有居中对齐的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/325)
3. `F` 修复了音视频标签内有 `a` 标签时可能无法播放的问题
4. `F` 修复了 `externStyle` 中的 `class` 名包含下划线或数字时可能失效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/326)
5. `F` 修复了 `h5` 端引入 `externStyle` 可能不生效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/326)
## v2.1.4（2021-07-14）
1. `F` 修复了 `rt` 标签无法设置样式的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/318)
2. `F` 修复了表格中有单元格同时合并行和列时可能显示不正确的问题
3. `F` 修复了 `app` 端无法关闭图片长按菜单的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/322)
4. `F` 修复了 `editable` 插件只能添加图片链接不能修改的问题 [详细](https://github.com/jin-yufeng/mp-html/pull/312) by [@leeseett](https://github.com/leeseett)
## v2.1.3（2021-06-12）
1. `A` `editable` 插件增加 `insertTable` 方法
2. `U` `editable` 插件支持编辑表格中的空白单元格 [详细](https://github.com/jin-yufeng/mp-html/issues/310)
3. `F` 修复了 `externStyle` 中使用伪类可能失效的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/298)
4. `F` 修复了多个组件同时使用时 `tag-style` 属性时可能互相影响的问题 [详细](https://github.com/jin-yufeng/mp-html/pull/305) by [@woodguoyu](https://github.com/woodguoyu)
5. `F` 修复了包含 `linearGradient` 的 `svg` 可能无法显示的问题
6. `F` 修复了编译到头条小程序时可能报错的问题
7. `F` 修复了 `nvue` 端不触发 `click` 事件的问题
8. `F` 修复了 `editable` 插件尾部插入时无法撤销的问题
9. `F` 修复了 `editable` 插件的 `insertHtml` 方法只能在末尾插入的问题
10. `F` 修复了 `editable` 插件插入音频不显示的问题
## v2.1.2（2021-04-24）
1. `A` 增加了 [img-cache](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#img-cache) 插件，可以在 `app` 端缓存图片 [详细](https://github.com/jin-yufeng/mp-html/issues/292) by [@PentaTea](https://github.com/PentaTea)
2. `U` 支持通过 `container-style` 属性设置 `white-space` 来保留连续空格和换行符 [详细](https://jin-yufeng.gitee.io/mp-html/#/question/faq#space)
3. `U` 代码风格符合 [standard](https://standardjs.com) 标准
4. `U` `editable` 插件编辑状态下支持预览视频 [详细](https://github.com/jin-yufeng/mp-html/issues/286)
5. `F` 修复了 `svg` 标签内嵌 `svg` 时无法显示的问题
6. `F` 修复了编译到支付宝和头条小程序时部分区域不可复制的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/291)
## v2.1.1（2021-04-09）
1. 修复了对 `p` 标签设置 `tag-style` 可能不生效的问题
2. 修复了 `svg` 标签中的文本无法显示的问题
3. 修复了使用 `editable` 插件编辑表格时可能报错的问题
4. 修复了使用 `highlight` 插件运行到头条小程序时可能没有样式的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/280)
5. 修复了使用 `editable` 插件 `editable` 属性为 `false` 时会报错的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/284)
6. 修复了 `style` 插件连续子选择器失效的问题
7. 修复了 `editable` 插件无法修改图片和字体大小的问题
## v2.1.0.2（2021-03-21）
修复了 `nvue` 端使用可能报错的问题
## v2.1.0（2021-03-20）
1. `A` 增加了 [container-style](https://jin-yufeng.gitee.io/mp-html/#/basic/prop#container-style) 属性 [详细](https://gitee.com/jin-yufeng/mp-html/pulls/1)
2. `A` 增加支持 `strike` 标签
3. `A` `editable` 插件增加 `placeholder` 属性 [详细](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#editable)
4. `A` `editable` 插件增加 `insertHtml` 方法 [详细](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#editable)
5. `U` 外部样式支持标签名选择器 [详细](https://jin-yufeng.gitee.io/mp-html/#/overview/quickstart#setting)
6. `F` 修复了 `nvue` 端部分情况下可能不显示的问题
## v2.0.5（2021-03-12）
1. `U` [linktap](https://jin-yufeng.gitee.io/mp-html/#/basic/event#linktap) 事件增加返回内部文本内容 `innerText` [详细](https://github.com/jin-yufeng/mp-html/issues/271)
2. `U` [selectable](https://jin-yufeng.gitee.io/mp-html/#/basic/prop#selectable) 属性设置为 `force` 时能够在微信 `iOS` 端生效（文本块会变成 `inline-block`） [详细](https://github.com/jin-yufeng/mp-html/issues/267)
3. `F` 修复了部分情况下竖向无法滚动的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/182)
4. `F` 修复了多次修改富文本数据时部分内容可能不显示的问题
5. `F` 修复了 [腾讯视频](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#txv-video) 插件可能无法播放的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/265)
6. `F` 修复了 [highlight](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin#highlight) 插件没有设置高亮语言时没有应用默认样式的问题 [详细](https://github.com/jin-yufeng/mp-html/issues/276) by [@fuzui](https://github.com/fuzui)
