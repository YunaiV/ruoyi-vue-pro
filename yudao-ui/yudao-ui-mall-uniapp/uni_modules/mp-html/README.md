## 为减小组件包的大小，默认组件包中不包含编辑、latex 公式等扩展功能，需要使用扩展功能的请参考下方的 插件扩展 栏的说明

## 功能介绍
- 全端支持（含 `v3、NVUE`）
- 支持丰富的标签（包括 `table`、`video`、`svg` 等）
- 支持丰富的事件效果（自动预览图片、链接处理等）
- 支持设置占位图（加载中、出错时、预览时）
- 支持锚点跳转、长按复制等丰富功能
- 支持大部分 *html* 实体
- 丰富的插件（关键词搜索、内容编辑、`latex` 公式等）
- 效率高、容错性强且轻量化

查看 [功能介绍](https://jin-yufeng.gitee.io/mp-html/#/overview/feature) 了解更多

## 使用方法
- `uni_modules` 方式  
  1. 点击右上角的 `使用 HBuilder X 导入插件` 按钮直接导入项目或点击 `下载插件 ZIP` 按钮下载插件包并解压到项目的 `uni_modules/mp-html` 目录下  
  2. 在需要使用页面的 `(n)vue` 文件中添加  
     ```html
     <!-- 不需要引入，可直接使用 -->
     <mp-html :content="html" />
     ```
     ```javascript
     export default {
       data() {
         return {
           html: '<div>Hello World!</div>'
         }
       }
     }
     ```
  3. 需要更新版本时在 `HBuilder X` 中右键 `uni_modules/mp-html` 目录选择 `从插件市场更新` 即可  

- 源码方式  
  1. 从 [github](https://github.com/jin-yufeng/mp-html/tree/master/dist/uni-app) 或 [gitee](https://gitee.com/jin-yufeng/mp-html/tree/master/dist/uni-app) 下载源码  
     插件市场的 **非 uni_modules 版本** 无法更新，不建议从插件市场获取  
  2. 在需要使用页面的 `(n)vue` 文件中添加  
     ```html
     <mp-html :content="html" />
     ```
     ```javascript
     import mpHtml from '@/components/mp-html/mp-html'
     export default {
       // HBuilderX 2.5.5+ 可以通过 easycom 自动引入
       components: {
         mpHtml
       },
       data() {
         return {
           html: '<div>Hello World!</div>'
         }
       }
     }
     ```

- npm 方式  
  1. 在项目根目录下执行  
     ```bash
     npm install mp-html
     ```
  2. 在需要使用页面的 `(n)vue` 文件中添加  
     ```html
     <mp-html :content="html" />
     ```
     ```javascript
     import mpHtml from 'mp-html/dist/uni-app/components/mp-html/mp-html'
     export default {
       // 不可省略
       components: {
         mpHtml
       },
       data() {
         return {
           html: '<div>Hello World!</div>'
         }
       }
     }
     ```
  3. 需要更新版本时执行以下命令即可  
     ```bash
     npm update mp-html
     ```
  
  使用 *cli* 方式运行的项目，通过 *npm* 方式引入时，需要在 *vue.config.js* 中配置 *transpileDependencies*，详情可见 [#330](https://github.com/jin-yufeng/mp-html/issues/330#issuecomment-913617687)  
  如果在 **nvue** 中使用还要将 `dist/uni-app/static` 目录下的内容拷贝到项目的 `static` 目录下，否则无法运行  

查看 [快速开始](https://jin-yufeng.gitee.io/mp-html/#/overview/quickstart) 了解更多

## 组件属性

| 属性 | 类型 | 默认值 | 说明 |
|:---:|:---:|:---:|---|
| container-style | String |  | 容器的样式（[2.1.0+](https://jin-yufeng.gitee.io/mp-html/#/changelog/changelog#v210)） |
| content | String |  | 用于渲染的 html 字符串 |
| copy-link | Boolean | true | 是否允许外部链接被点击时自动复制 |
| domain | String |  | 主域名（用于链接拼接） |
| error-img | String |  | 图片出错时的占位图链接 |
| lazy-load | Boolean | false | 是否开启图片懒加载 |
| loading-img | String |  | 图片加载过程中的占位图链接 |
| pause-video | Boolean | true | 是否在播放一个视频时自动暂停其他视频 |
| preview-img | Boolean | true | 是否允许图片被点击时自动预览 |
| scroll-table | Boolean | false | 是否给每个表格添加一个滚动层使其能单独横向滚动 |
| selectable | Boolean | false | 是否开启文本长按复制 |
| set-title | Boolean | true | 是否将 title 标签的内容设置到页面标题 |
| show-img-menu | Boolean | true | 是否允许图片被长按时显示菜单 |
| tag-style | Object |  | 设置标签的默认样式 |
| use-anchor | Boolean | false | 是否使用锚点链接 |

查看 [属性](https://jin-yufeng.gitee.io/mp-html/#/basic/prop) 了解更多

## 组件事件

| 名称 | 触发时机 |
|:---:|---|
| load | dom 树加载完毕时 |
| ready | 图片加载完毕时 |
| error | 发生渲染错误时 |
| imgtap | 图片被点击时 |
| linktap | 链接被点击时 |
| play | 音视频播放时 |

查看 [事件](https://jin-yufeng.gitee.io/mp-html/#/basic/event) 了解更多

## api
组件实例上提供了一些 `api` 方法可供调用

| 名称 | 作用 |
|:---:|---|
| in | 将锚点跳转的范围限定在一个 scroll-view 内 |
| navigateTo | 锚点跳转 |
| getText | 获取文本内容 |
| getRect | 获取富文本内容的位置和大小 |
| setContent | 设置富文本内容 |
| imgList | 获取所有图片的数组 |
| pauseMedia | 暂停播放音视频（[2.2.2+](https://jin-yufeng.gitee.io/mp-html/#/changelog/changelog#v222)） |
| setPlaybackRate | 设置音视频播放速率（[2.4.0+](https://jin-yufeng.gitee.io/mp-html/#/changelog/changelog#v240)） |

查看 [api](https://jin-yufeng.gitee.io/mp-html/#/advanced/api) 了解更多

## 插件扩展  
除基本功能外，本组件还提供了丰富的扩展，可按照需要选用

| 名称 | 作用 |
|:---:|---|
| audio | 音乐播放器 |
| editable | 富文本 **编辑**（[示例项目](https://mp-html.oss-cn-hangzhou.aliyuncs.com/editable.zip)） |
| emoji | 解析 emoji |
| highlight | 代码块高亮显示 |
| markdown | 渲染 markdown |
| search | 关键词搜索 |
| style | 匹配 style 标签中的样式 |
| txv-video | 使用腾讯视频 |
| img-cache | 图片缓存 by [@PentaTea](https://github.com/PentaTea) |
| latex | 渲染 latex 公式 by [@Zeng-J](https://github.com/Zeng-J) |

从插件市场导入的包中 **不含有** 扩展插件，使用插件需通过微信小程序 `富文本插件` 获取或参考以下方法进行打包：  
1. 获取完整组件包  
   ```bash
   npm install mp-html
   ```
2. 编辑 `tools/config.js` 中的 `plugins` 项，选择需要的插件  
3. 生成新的组件包  
   在 `node_modules/mp-html` 目录下执行  
   ```bash
   npm install
   npm run build:uni-app
   ```
4. 拷贝 `dist/uni-app` 中的内容到项目根目录  

查看 [插件](https://jin-yufeng.gitee.io/mp-html/#/advanced/plugin) 了解更多

## 关于 nvue
`nvue` 使用原生渲染，不支持部分 `css` 样式，为实现和 `html` 相同的效果，组件内部通过 `web-view` 进行渲染，性能上差于原生，根据 `weex` 官方建议，`web` 标签仅应用在非常规的降级场景。因此，如果通过原生的方式（如 `richtext`）能够满足需要，则不建议使用本组件，如果有较多的富文本内容，则可以直接使用 `vue` 页面  
由于渲染方式与其他端不同，有以下限制：  
1. 不支持 `lazy-load` 属性
2. 视频不支持全屏播放
3. 如果在 `flex-direction: row` 的容器中使用，需要给组件设置宽度或设置 `flex: 1` 占满剩余宽度

纯 `nvue` 模式下，[此问题](https://ask.dcloud.net.cn/question/119678) 修复前，不支持通过 `uni_modules` 引入，需要本地引入（将 [dist/uni-app](https://github.com/jin-yufeng/mp-html/tree/master/dist/uni-app) 中的内容拷贝到项目根目录下）  

## 立即体验
![富文本插件](https://mp-html.oss-cn-hangzhou.aliyuncs.com/qrcode.jpg)

## 问题反馈
遇到问题时，请先查阅 [常见问题](https://jin-yufeng.gitee.io/mp-html/#/question/faq) 和 [issue](https://github.com/jin-yufeng/mp-html/issues) 中是否已有相同的问题  
可通过 [issue](https://github.com/jin-yufeng/mp-html/issues/new/choose) 、插件问答或发送邮件到 [mp_html@126.com](mailto:mp_html@126.com) 提问，不建议在评论区提问（不方便回复）  
提问请严格按照 [issue 模板](https://github.com/jin-yufeng/mp-html/issues/new/choose) ，描述清楚使用环境、`html` 内容或可复现的 `demo` 项目以及复现方式，对于 **描述不清**、**无法复现** 或重复的问题将不予回复  

欢迎加入 `QQ` 交流群：  
群1（已满）：`699734691`  
群2（已满）：`778239129`  
群3：`960265313`  

查看 [问题反馈](https://jin-yufeng.gitee.io/mp-html/#/question/feedback) 了解更多
