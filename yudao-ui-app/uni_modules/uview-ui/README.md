<p align="center">
    <img alt="logo" src="https://uviewui.com/common/logo.png" width="120" height="120" style="margin-bottom: 10px;">
</p>
<h3 align="center" style="margin: 30px 0 30px;font-weight: bold;font-size:40px;">uView</h3>
<h3 align="center">多平台快速开发的UI框架</h3>

## 说明

uView UI，是[uni-app](https://uniapp.dcloud.io/)生态优秀的UI框架，全面的组件和便捷的工具会让您信手拈来，如鱼得水

## 特性

- 兼容安卓，iOS，微信小程序，H5，QQ小程序，百度小程序，支付宝小程序，头条小程序
- 60+精选组件，功能丰富，多端兼容，让您快速集成，开箱即用
- 众多贴心的JS利器，让您飞镖在手，召之即来，百步穿杨
- 众多的常用页面和布局，让您专注逻辑，事半功倍
- 详尽的文档支持，现代化的演示效果
- 按需引入，精简打包体积


## 安装

```bash
# npm方式安装，插件市场导入无需执行此命令
npm i uview-ui
```

## 快速上手

1. `main.js`引入uView库
```js
// main.js
import uView from 'uview-ui';
Vue.use(uView);
```

2. `App.vue`引入基础样式(注意style标签需声明scss属性支持)
```css
/* App.vue */
<style lang="scss">
@import "uview-ui/index.scss";
</style>
```

3. `uni.scss`引入全局scss变量文件
```css
/* uni.scss */
@import "uview-ui/theme.scss";
```

4. `pages.json`配置easycom规则(按需引入)

```js
// pages.json
{
	"easycom": {
		// npm安装的方式不需要前面的"@/"，下载安装的方式需要"@/"
		// npm安装方式
		"^u-(.*)": "uview-ui/components/u-$1/u-$1.vue"
		// 下载安装方式
		// "^u-(.*)": "@/uview-ui/components/u-$1/u-$1.vue"
	},
	// 此为本身已有的内容
	"pages": [
		// ......
	]
}
```

请通过[快速上手](https://www.uviewui.com/components/quickstart.html)了解更详细的内容 

## 使用方法
配置easycom规则后，自动按需引入，无需`import`组件，直接引用即可。

```html
<template>
	<u-button text="按钮"></u-button>
</template>
```

请通过[快速上手](https://www.uviewui.com/components/quickstart.html)了解更详细的内容 

## 链接

- [官方文档](https://www.uviewui.com/)
- [更新日志](https://www.www.uviewui.com/components/changelog.html)
- [升级指南](https://www.uviewui.com/components/changelog.html)
- [关于我们](https://www.uviewui.com/cooperation/about.html)

## 预览

您可以通过**微信**扫码，查看最佳的演示效果。
<br>
<br>
<img src="https://uviewui.com/common/weixin_mini_qrcode.png" width="220" height="220" >

## 捐赠uView的研发

uView文档和源码全部开源免费，如果您认为uView帮到了您的开发工作，您可以捐赠uView的研发工作，捐赠无门槛，哪怕是一杯可乐也好(相信这比打赏主播更有意义)。

<img src="https://uviewui.com/common/alipay.png" width="220" ><img style="margin-left: 100px;" src="https://uviewui.com/common/wechat.png" width="220" >

## 版权信息
uView遵循[MIT](https://en.wikipedia.org/wiki/MIT_License)开源协议，意味着您无需支付任何费用，也无需授权，即可将uView应用到您的产品中。
