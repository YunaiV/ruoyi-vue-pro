
## breadcrumb 面包屑导航
> **组件名：uni-breadcrumb**
> 代码块： `ubreadcrumb`

显示当前页面的路径，快速返回之前的任意页面。

### 安装方式

本组件符合[easycom](https://uniapp.dcloud.io/collocation/pages?id=easycom)规范，`HBuilderX 2.5.5`起，只需将本组件导入项目，在页面`template`中即可直接使用，无需在页面中`import`和注册`components`。

如需通过`npm`方式使用`uni-ui`组件，另见文档：[https://ext.dcloud.net.cn/plugin?id=55](https://ext.dcloud.net.cn/plugin?id=55)

### 基本用法

在 ``template`` 中使用组件

```html
<uni-breadcrumb separator="/">
	<uni-breadcrumb-item v-for="(route,index) in routes" :key="index" :to="route.to">{{route.name}}</uni-breadcrumb-item>
</uni-breadcrumb>
```

```js
export default {
		name: "uni-stat-breadcrumb",
		data() {
			return {
				routes: [{
					to: '/A',
					name: 'A页面'
				}, {
					to: '/B',
					name: 'B页面'
				}, {
					to: '/C',
					name: 'C页面'
				}]
			};
		}
	}
```


## API

### Breadcrumb Props

|属性名			|类型	|默认值	|说明				|
|:-:			|:-:	|:-:	|:-:				|
|separator		|String	|斜杠'/' |分隔符				|
|separatorClass	|String	|		|图标分隔符 class	    |

### Breadcrumb Item Props

|属性名	|类型			|默认值	|说明																			|
|:-:	|:-:			|:-:	|:-:																			|
|to		|String     	|		|路由跳转页面路径           														|
|replace|Boolean		|		|在使用 to 进行路由跳转时，启用 replace 将不会向 history 添加新记录(仅 h5 支持）         |




## 组件示例

点击查看：[https://hellouniapp.dcloud.net.cn/pages/extUI/breadcrumb/breadcrumb](https://hellouniapp.dcloud.net.cn/pages/extUI/breadcrumb/breadcrumb)