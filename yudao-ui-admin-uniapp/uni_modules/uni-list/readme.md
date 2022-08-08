## List 列表
> **组件名：uni-list**
> 代码块： `uList`、`uListItem`
> 关联组件：`uni-list-item`、`uni-badge`、`uni-icons`、`uni-list-chat`、`uni-list-ad`


List 列表组件，包含基本列表样式、可扩展插槽机制、长列表性能优化、多端兼容。

在vue页面里，它默认使用页面级滚动。在app-nvue页面里，它默认使用原生list组件滚动。这样的长列表，在滚动出屏幕外后，系统会回收不可见区域的渲染内存资源，不会造成滚动越长手机越卡的问题。

uni-list组件是父容器，里面的核心是uni-list-item子组件，它代表列表中的一个可重复行，子组件可以无限循环。

uni-list-item有很多风格，uni-list-item组件通过内置的属性，满足一些常用的场景。当内置属性不满足需求时，可以通过扩展插槽来自定义列表内容。

内置属性可以覆盖的场景包括：导航列表、设置列表、小图标列表、通信录列表、聊天记录列表。

涉及很多大图或丰富内容的列表，比如类今日头条的新闻列表、类淘宝的电商列表，需要通过扩展插槽实现。

下文均有样例给出。

uni-list不包含下拉刷新和上拉翻页。上拉翻页另见组件：[uni-load-more](https://ext.dcloud.net.cn/plugin?id=29)


### 安装方式

本组件符合[easycom](https://uniapp.dcloud.io/collocation/pages?id=easycom)规范，`HBuilderX 2.5.5`起，只需将本组件导入项目，在页面`template`中即可直接使用，无需在页面中`import`和注册`components`。

如需通过`npm`方式使用`uni-ui`组件，另见文档：[https://ext.dcloud.net.cn/plugin?id=55](https://ext.dcloud.net.cn/plugin?id=55)

> **注意事项**
> 为了避免错误使用，给大家带来不好的开发体验，请在使用组件前仔细阅读下面的注意事项，可以帮你避免一些错误。
> - 组件需要依赖 `sass` 插件 ，请自行手动安装
> - 组件内部依赖 `'uni-icons'` 、`uni-badge` 组件
> - `uni-list` 和 `uni-list-item` 需要配套使用，暂不支持单独使用 `uni-list-item`
> - 只有开启点击反馈后，会有点击选中效果
> - 使用插槽时，可以完全自定义内容
> - note 、rightText 属性暂时没做限制，不支持文字溢出隐藏，使用时应该控制长度显示或通过默认插槽自行扩展
> - 支付宝小程序平台需要在支付宝小程序开发者工具里开启 component2 编译模式，开启方式： 详情 --> 项目配置 --> 启用 component2 编译
> - 如果需要修改 `switch`、`badge` 样式，请使用插槽自定义
> - 在 `HBuilderX` 低版本中，可能会出现组件显示 `undefined` 的问题，请升级最新的 `HBuilderX` 或者 `cli`
> - 如使用过程中有任何问题，或者您对uni-ui有一些好的建议，欢迎加入 uni-ui 交流群：871950839
 

### 基本用法 

- 设置 `title` 属性，可以显示列表标题
- 设置 `disabled` 属性，可以禁用当前项

```html
<uni-list>
	<uni-list-item  title="列表文字" ></uni-list-item>
	<uni-list-item :disabled="true" title="列表禁用状态" ></uni-list-item>
</uni-list>
			 
```

### 多行内容显示

- 设置 `note` 属性 ，可以在第二行显示描述文本信息

```html
<uni-list>
	<uni-list-item title="列表文字" note="列表描述信息"></uni-list-item>
	<uni-list-item :disabled="true" title="列表文字" note="列表禁用状态"></uni-list-item>
</uni-list>

```

### 右侧显示角标、switch

- 设置 `show-badge` 属性 ，可以显示角标内容
- 设置 `show-switch` 属性，可以显示 switch 开关

```html
<uni-list>
	<uni-list-item  title="列表右侧显示角标" :show-badge="true" badge-text="12" ></uni-list-item>
	<uni-list-item title="列表右侧显示 switch"  :show-switch="true"  @switchChange="switchChange" ></uni-list-item>
</uni-list>

```

### 左侧显示略缩图、图标  

- 设置 `thumb` 属性 ，可以在列表左侧显示略缩图
- 设置 `show-extra-icon` 属性，并指定 `extra-icon` 可以在左侧显示图标

```html
 <uni-list>
 	<uni-list-item title="列表左侧带略缩图" note="列表描述信息" thumb="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png"
 	 thumb-size="lg" rightText="右侧文字"></uni-list-item>
 	<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon1" title="列表左侧带扩展图标" ></uni-list-item>
</uni-list>
```

### 开启点击反馈和右侧箭头
- 设置 `clickable` 为 `true` ，则表示这是一个可点击的列表，会默认给一个点击效果，并可以监听 `click` 事件
- 设置 `link` 属性，会自动开启点击反馈，并给列表右侧添加一个箭头
- 设置 `to` 属性，可以跳转页面，`link` 的值表示跳转方式，如果不指定，默认为 `navigateTo`

```html

<uni-list>
	<uni-list-item title="开启点击反馈" clickable  @click="onClick" ></uni-list-item>
	<uni-list-item title="默认 navigateTo 方式跳转页面" link to="/pages/vue/index/index" @click="onClick($event,1)" ></uni-list-item>
	<uni-list-item title="reLaunch 方式跳转页面" link="reLaunch" to="/pages/vue/index/index" @click="onClick($event,1)" ></uni-list-item>
</uni-list>

```


### 聊天列表示例
- 设置 `clickable` 为 `true` ，则表示这是一个可点击的列表，会默认给一个点击效果，并可以监听 `click` 事件
- 设置 `link` 属性，会自动开启点击反馈，`link` 的值表示跳转方式，如果不指定，默认为 `navigateTo`
- 设置 `to` 属性，可以跳转页面
- `time` 属性，通常会设置成时间显示，但是这个属性不仅仅可以设置时间，你可以传入任何文本，注意文本长度可能会影响显示
- `avatar` 和 `avatarList` 属性同时只会有一个生效，同时设置的话，`avatarList` 属性的长度大于1 ，`avatar` 属性将失效
- 可以通过默认插槽自定义列表右侧内容

```html

<uni-list>
	<uni-list :border="true">
		<!-- 显示圆形头像 -->
		<uni-list-chat :avatar-circle="true" title="uni-app" avatar="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png" note="您收到一条新的消息" time="2020-02-02 20:20" ></uni-list-chat>
		<!-- 右侧带角标 -->
		<uni-list-chat title="uni-app" avatar="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png" note="您收到一条新的消息" time="2020-02-02 20:20" badge-text="12" :badge-style="{backgroundColor:'#FF80AB'}"></uni-list-chat>
		<!-- 头像显示圆点 -->
		<uni-list-chat title="uni-app" avatar="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="dot"></uni-list-chat>
		<!-- 头像显示角标 -->
		<uni-list-chat title="uni-app" avatar="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="99"></uni-list-chat>
		<!-- 显示多头像 -->
		<uni-list-chat title="uni-app" :avatar-list="avatarList" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="dot"></uni-list-chat>
		<!-- 自定义右侧内容 -->
		<uni-list-chat title="uni-app" :avatar-list="avatarList" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="dot">
			<view class="chat-custom-right">
				<text class="chat-custom-text">刚刚</text>
				<!-- 需要使用 uni-icons 请自行引入 -->
				<uni-icons type="star-filled" color="#999" size="18"></uni-icons>
			</view>
		</uni-list-chat>
	</uni-list>
</uni-list>

```

```javascript

export default {
	components: {},
	data() {
		return {
			avatarList: [{
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}, {
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}, {
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}]
		}
	}
}

```


```css

.chat-custom-right {
	flex: 1;
	/* #ifndef APP-NVUE */
	display: flex;
	/* #endif */
	flex-direction: column;
	justify-content: space-between;
	align-items: flex-end;
}

.chat-custom-text {
	font-size: 12px;
	color: #999;
}

```

## API

### List Props

属性名			|类型		|默认值		|	说明																									
:-:				|:-:		|:-:		|	:-:	
border			|Boolean	|true		|	是否显示边框


### ListItem Props

属性名			|类型		|默认值		|	说明																					
:-:				|:-:		|:-:		|	:-:	
title			|String		|-			|	标题
note			|String		|-			|	描述
ellipsis		|Number		|0			|	title 是否溢出隐藏，可选值，0:默认;  1:显示一行;	2:显示两行;【nvue 暂不支持】
thumb			|String		|-			|	左侧缩略图，若thumb有值，则不会显示扩展图标
thumbSize		|String 	|medium 	|	略缩图尺寸，可选值，lg:大图;  medium:一般;	sm:小图;
showBadge		|Boolean	|false		|	是否显示数字角标	
badgeText		|String		|-			|	数字角标内容
badgeType		|String		|-			|	数字角标类型，参考[uni-icons](https://ext.dcloud.net.cn/plugin?id=21)	
badgeStyle  |Object   |-      | 数字角标样式，使用uni-badge的custom-style参数
rightText		|String		|-			|	右侧文字内容
disabled		|Boolean	|false		|	是否禁用	
showArrow 		|Boolean	|true		|	是否显示箭头图标			
link			|String 	|navigateTo	|	新页面跳转方式，可选值见下表
to				|String		|-			|	新页面跳转地址，如填写此属性，click 会返回页面是否跳转成功			
clickable		|Boolean	|false		|	是否开启点击反馈
showSwitch	    |Boolean	|false		|	是否显示Switch																			
switchChecked	|Boolean	|false		|	Switch是否被选中																			
showExtraIcon   |Boolean	|false		|	左侧是否显示扩展图标																		
extraIcon		|Object		|-			|	扩展图标参数，格式为 ``{color: '#4cd964',size: '22',type: 'spinner'}``，参考 [uni-icons](https://ext.dcloud.net.cn/plugin?id=28)	
direction		| String	|row		|	排版方向，可选值，row:水平排列;  column:垂直排列; 3个插槽是水平排还是垂直排，也受此属性控制


#### Link Options

属性名				|	说明
:-:					|	:-:
navigateTo 	| 	同 uni.navigateTo()
redirectTo 	|	同 uni.reLaunch()
reLaunch		|	同 uni.reLaunch()
switchTab  	|	同 uni.switchTab()

### ListItem Events

事件称名			|说明									|返回参数			
:-:				|:-:									|:-:				
click			|点击 uniListItem 触发事件，需开启点击反馈	|-					
switchChange	|点击切换 Switch 时触发，需显示 switch		|e={value:checked}	



### ListItem Slots

名称	 	|	说明					
:-:		|	:-:						
header	|	左/上内容插槽，可完全自定义默认显示
body	|	中间内容插槽，可完全自定义中间内容				
footer	|	右/下内容插槽，可完全自定义右侧内容		


> **通过插槽扩展**
> 需要注意的是当使用插槽时，内置样式将会失效，只保留排版样式，此时的样式需要开发者自己实现
> 如果	`uni-list-item` 组件内置属性样式无法满足需求，可以使用插槽来自定义uni-list-item里的内容。
> uni-list-item提供了3个可扩展的插槽：`header`、`body`、`footer`
> - 当 `direction` 属性为 `row` 时表示水平排列，此时 `header` 表示列表的左边部分，`body` 表示列表的中间部分，`footer` 表示列表的右边部分
> - 当 `direction` 属性为 `column` 时表示垂直排列，此时 `header` 表示列表的上边部分，`body` 表示列表的中间部分，`footer` 表示列表的下边部分
> 开发者可以只用1个插槽，也可以3个一起使用。在插槽中可自主编写view标签，实现自己所需的效果。

	
**示例**

```html
<uni-list>
	<uni-list-item title="自定义右侧插槽" note="列表描述信息" link>
		<template slot="header">
			<image class="slot-image" src="/static/logo.png" mode="widthFix"></image>
		</template>
	</uni-list-item>
	<uni-list-item>
		<!-- 自定义 header -->
		<view slot="header" class="slot-box"><image class="slot-image" src="/static/logo.png" mode="widthFix"></image></view>
		<!-- 自定义 body -->
		<text slot="body" class="slot-box slot-text">自定义插槽</text>
		<!-- 自定义 footer-->
		<template slot="footer">
			<image class="slot-image" src="/static/logo.png" mode="widthFix"></image>
		</template>
	</uni-list-item>
</uni-list>
```





### ListItemChat Props

属性名			|类型		|默认值		|	说明																		
:-:				|:-:		|:-:		|	:-:	
title 			|String		|-			|	标题
note 			|String		|-			|	描述
clickable		|Boolean	|false		|	是否开启点击反馈
badgeText		|String		|-			|	数字角标内容，设置为 `dot` 将显示圆点
badgePositon 	|String		|right		|	角标位置
link			|String 	|navigateTo	|	是否展示右侧箭头并开启点击反馈，可选值见下表
clickable		|Boolean	|false		|	是否开启点击反馈
to				|String		|-			|	跳转页面地址，如填写此属性，click 会返回页面是否跳转成功	
time			|String 	|-			|	右侧时间显示
avatarCircle 	|Boolean 	|false		|	是否显示圆形头像
avatar			|String 	|-			|	头像地址，avatarCircle 不填时生效
avatarList 		|Array	 	|-			|	头像组，格式为 [{url:''}]

#### Link Options

属性名		|	说明
:-:			|	:-:
navigateTo 	| 	同 uni.navigateTo()
redirectTo 	|	同 uni.reLaunch()
reLaunch	|	同 uni.reLaunch()
switchTab  	|	同 uni.switchTab()

### ListItemChat Slots

名称	 	|	说明					
:-		|	:-						
default	|	自定义列表右侧内容（包括时间和角标显示）

### ListItemChat Events
事件称名			|	说明						|	返回参数			
:-:				|	:-:						|	:-:	
@click			|	点击 uniListChat 触发事件	|	{data:{}}	，如有 to 属性，会返回页面跳转信息	






## 基于uni-list扩展的页面模板

通过扩展插槽，可实现多种常见样式的列表

**新闻列表类**

1. 云端一体混合布局：[https://ext.dcloud.net.cn/plugin?id=2546](https://ext.dcloud.net.cn/plugin?id=2546)
2. 云端一体垂直布局，大图模式：[https://ext.dcloud.net.cn/plugin?id=2583](https://ext.dcloud.net.cn/plugin?id=2583)
3. 云端一体垂直布局，多行图文混排：[https://ext.dcloud.net.cn/plugin?id=2584](https://ext.dcloud.net.cn/plugin?id=2584)
4. 云端一体垂直布局，多图模式：[https://ext.dcloud.net.cn/plugin?id=2585](https://ext.dcloud.net.cn/plugin?id=2585)
5. 云端一体水平布局，左图右文：[https://ext.dcloud.net.cn/plugin?id=2586](https://ext.dcloud.net.cn/plugin?id=2586)
6. 云端一体水平布局，左文右图：[https://ext.dcloud.net.cn/plugin?id=2587](https://ext.dcloud.net.cn/plugin?id=2587)
7. 云端一体垂直布局，无图模式，主标题+副标题：[https://ext.dcloud.net.cn/plugin?id=2588](https://ext.dcloud.net.cn/plugin?id=2588)

**商品列表类**

1. 云端一体列表/宫格视图互切：[https://ext.dcloud.net.cn/plugin?id=2651](https://ext.dcloud.net.cn/plugin?id=2651)
2. 云端一体列表（宫格模式）：[https://ext.dcloud.net.cn/plugin?id=2671](https://ext.dcloud.net.cn/plugin?id=2671)
3. 云端一体列表（列表模式）：[https://ext.dcloud.net.cn/plugin?id=2672](https://ext.dcloud.net.cn/plugin?id=2672)

## 组件示例

点击查看：[https://hellouniapp.dcloud.net.cn/pages/extUI/list/list](https://hellouniapp.dcloud.net.cn/pages/extUI/list/list)