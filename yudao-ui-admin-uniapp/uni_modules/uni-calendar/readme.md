

## Calendar 日历
> **组件名：uni-calendar**
> 代码块： `uCalendar`


日历组件

> **注意事项**
> 为了避免错误使用，给大家带来不好的开发体验，请在使用组件前仔细阅读下面的注意事项，可以帮你避免一些错误。
> - 本组件农历转换使用的js是 [@1900-2100区间内的公历、农历互转](https://github.com/jjonline/calendar.js)  
> - 仅支持自定义组件模式
> - `date`属性传入的应该是一个 String ，如： 2019-06-27 ，而不是 new Date()
> - 通过 `insert` 属性来确定当前的事件是 @change 还是 @confirm 。理应合并为一个事件，但是为了区分模式，现使用两个事件，这里需要注意
> - 弹窗模式下无法阻止后面的元素滚动，如有需要阻止，请在弹窗弹出后，手动设置滚动元素为不可滚动


### 安装方式

本组件符合[easycom](https://uniapp.dcloud.io/collocation/pages?id=easycom)规范，`HBuilderX 2.5.5`起，只需将本组件导入项目，在页面`template`中即可直接使用，无需在页面中`import`和注册`components`。

如需通过`npm`方式使用`uni-ui`组件，另见文档：[https://ext.dcloud.net.cn/plugin?id=55](https://ext.dcloud.net.cn/plugin?id=55)

### 基本用法

在 ``template`` 中使用组件

```html
<view>
	<uni-calendar 
	:insert="true"
	:lunar="true" 
	:start-date="'2019-3-2'"
	:end-date="'2019-5-20'"
	@change="change"
	 />
</view>
```

### 通过方法打开日历

需要设置 `insert` 为 `false`

```html
<view>
	<uni-calendar 
	ref="calendar"
	:insert="false"
	@confirm="confirm"
	 />
	 <button @click="open">打开日历</button>
</view>
```

```javascript

export default {
	data() {
		return {};
	},
	methods: {
		open(){
			this.$refs.calendar.open();
		},
		confirm(e) {
			console.log(e);
		}
	}
};

```


## API

### Calendar Props

|  属性名	|    类型	| 默认值| 说明																													|
| 		| 																													|
| date		| String	|-		| 自定义当前时间，默认为今天																							|
| lunar		| Boolean	| false	| 显示农历																												|
| startDate	| String	|-		| 日期选择范围-开始日期																									|
| endDate	| String	|-		| 日期选择范围-结束日期																									|
| range		| Boolean	| false	| 范围选择																												|
| insert	| Boolean	| false	| 插入模式,可选值，ture：插入模式；false：弹窗模式；默认为插入模式														|
|clearDate	|Boolean	|true	|弹窗模式是否清空上次选择内容	|
| selected	| Array		|-		| 打点，期待格式[{date: '2019-06-27', info: '签到', data: { custom: '自定义信息', name: '自定义消息头',xxx:xxx... }}]	|
|showMonth	| Boolean	| true	| 是否显示月份为背景																									|

### Calendar Events

|  事件名		| 说明								|返回值|
| 								|		| 									|
| open	| 弹出日历组件，`insert :false` 时生效|- 	|





## 组件示例

点击查看：[https://hellouniapp.dcloud.net.cn/pages/extUI/calendar/calendar](https://hellouniapp.dcloud.net.cn/pages/extUI/calendar/calendar)