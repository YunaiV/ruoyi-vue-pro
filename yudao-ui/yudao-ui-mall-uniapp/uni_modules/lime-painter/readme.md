# Painter 画板 测试版

> uniapp 海报画板，更优雅的海报生成方案  
> [查看更多](https://limeui.qcoon.cn/#/painter)  

## 平台兼容

| H5  | 微信小程序 | 支付宝小程序 | 百度小程序 | 头条小程序 | QQ 小程序 | App |
| --- | ---------- | ------------ | ---------- | ---------- | --------- | --- |
| √   | √          | √            | 未测       | √          | √         | √   |

## 安装
在市场导入**[海报画板](https://ext.dcloud.net.cn/plugin?id=2389)uni_modules**版本的即可，无需`import`

## 代码演示

### 插件demo
- lime-painter 为 demo
- 位于 uni_modules/lime-painter/components/lime-painter
- 导入插件后直接使用可查看demo
```vue
<lime-painter />
```


### 基本用法

- 插件提供 JSON 及 Template 的方式绘制海报
- 参考 css 块状流布局模拟 css schema。
- 另外flex布局还不是成完善，请谨慎使用，普通的流布局我觉得已经够用了。

#### 方式一 Template

- 提供`l-painter-view`、`l-painter-text`、`l-painter-image`、`l-painter-qrcode`四种类型组件
- 通过 `css` 属性绘制样式，与 style 使用方式保持一致。
```html
<l-painter>
	//如果使用Template出现顺序错乱，可使用`template` 等所有变量完成再显示
	<template v-if="show">
		<l-painter-view
			css="background: #07c160; height: 120rpx; width: 120rpx; display: inline-block"
		></l-painter-view>
		<l-painter-view
			css="background: #1989fa; height: 120rpx; width: 120rpx; border-top-right-radius: 60rpx; border-bottom-left-radius: 60rpx; display: inline-block; margin: 0 30rpx;"
		></l-painter-view>
		<l-painter-view
			css="background: #ff9d00; height: 120rpx; width: 120rpx; border-radius: 50%; display: inline-block"
		></l-painter-view>
	<template>
</l-painter>
```

#### 方式二 JSON

- 在 json 里四种类型组件的`type`为`view`、`text`、`image`、`qrcode`
- 通过 `board` 设置海报所需的 JSON 数据进行绘制或`ref`获取组件实例调用组件内的`render(json)`
- 所有类型的 schema 都具有`css`字段，css 的 key 值使用**驼峰**如：`lineHeight`

```html
<l-painter :board="poster"/>
```

```js
data() {
	return {
		poster: {
			css: {
				// 根节点若无尺寸，自动获取父级节点
				width: '750rpx'
			},
			views: [
				{
					css: {
						background: "#07c160",
						height: "120rpx",
						width: "120rpx",
						display: "inline-block"
					},
					type: "view"
				},
				{
					css: {
						background: "#1989fa",
						height: "120rpx",
						width: "120rpx",
						borderTopRightRadius: "60rpx",
						borderBottomLeftRadius: "60rpx",
						display: "inline-block",
						margin: "0 30rpx"
					},
					views: [],
					type: "view"
				},
				{
					css: {
						background: "#ff9d00",
						height: "120rpx",
						width: "120rpx",
						borderRadius: "50%",
						display: "inline-block"
					},
					views: [],
					type: "view"
				},
			]
		}
	}
}
```

### View 容器

- 类似于 `div` 可以嵌套承载更多的 view、text、image，qrcode 共同构建一颗完整的节点树
- 在 JSON 里具有 `views` 的数组字段，用于嵌套承载节点。

#### 方式一 Template

```html
<l-painter>
  <l-painter-view css="background: #f0f0f0; padding-top: 100rpx;">
    <l-painter-view
      css="background: #d9d9d9; width: 33.33%; height: 100rpx; display: inline-block"
    ></l-painter-view>
    <l-painter-view
      css="background: #bfbfbf; width: 66.66%; height: 100rpx; display: inline-block"
    ></l-painter-view>
  </l-painter-view>
</l-painter>
```

#### 方式二 JSON

```js
{
	css: {},
	views: [
		{
			type: 'view',
			css: {
				background: '#f0f0f0',
				paddingTop: '100rpx'
			},
			views: [
				{
					type: 'view',
					css: {
						background: '#d9d9d9',
						width: '33.33%',
						height: '100rpx',
						display: 'inline-block'
					}
				},
				{
					type: 'view',
					css: {
						background: '#bfbfbf',
						width: '66.66%',
						height: '100rpx',
						display: 'inline-block'
					}
				}
			],

		}
	]
}
```

### Text 文本

- 通过 `text` 属性填写文本内容。
- 支持`\n`换行符
- 支持省略号，使用 css 的`line-clamp`设置行数，当文字内容超过会显示省略号。
- 支持`text-decoration`

#### 方式一 Template

```html
<l-painter>
  <l-painter-view css="background: #e0e2db; padding: 30rpx; color: #222a29">
    <l-painter-text
      text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼"
    />
    <l-painter-text
      text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼"
      css="text-align:center; padding-top: 20rpx; text-decoration: line-through "
    />
    <l-painter-text
      text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼"
      css="text-align:right; padding-top: 20rpx"
    />
    <l-painter-text
      text="水调歌头\n明月几时有？把酒问青天。不知天上宫阙，今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。"
      css="line-clamp: 3; padding-top: 20rpx; background: linear-gradient(,#ff971b 0%, #ff5000 100%); background-clip: text"
    />
  </l-painter-view>
</l-painter>
```

#### 方式二 JSON

```js
// 基础用法
{
	type: 'text',
	text: '登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼',
},
{
	type: 'text',
	text: '登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼',
	css: {
		// 设置居中对齐
		textAlign: 'center',
		// 设置中划线
		textDecoration: 'line-through'
	}
},
{
	type: 'text',
	text: '登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼',
	css: {
		// 设置右对齐
		textAlign: 'right',
	}
},
{
	type: 'text',
	text: '登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼',
	css: {
		// 设置行数，超出显示省略号
		lineClamp: 3,
		// 渐变文字
		background: 'linear-gradient(,#ff971b 0%, #1989fa 100%)',
		backgroundClip: 'text'
	}
}
```

### Image 图片

- 通过 `src` 属性填写图片路径。
- 图片路径支持：网络图片，本地 static 里的图片路径，缓存路径，**字节的static目录是写相对路径**
- 通过 `css` 的 `object-fit`属性可以设置图片的填充方式，可选值见下方 CSS 表格。
- 通过 `css` 的 `object-position`配合 `object-fit` 可以设置图片的对齐方式，类似于`background-position`，详情见下方 CSS 表格。
- 使用网络图片时：小程序需要去公众平台配置 [downloadFile](https://mp.weixin.qq.com/) 域名
- 使用网络图片时：**H5 和 Nvue 需要决跨域问题**

#### 方式一 Template

```html
<l-painter>
  <!-- 基础用法 -->
  <l-painter-image
    src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
    css="width: 200rpx; height: 200rpx"
  />
  <!-- 填充方式 -->
  <!-- css object-fit 设置 填充方式 见下方表格-->
  <l-painter-image
    src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
    css="width: 200rpx; height: 200rpx; object-fit: contain; background: #eee"
  />
  <!-- css object-position 设置 图片的对齐方式-->
  <l-painter-image
    src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
    css="width: 200rpx; height: 200rpx; object-fit: contain; object-position: 50% 50%; background: #eee"
  />
</l-painter>
```

#### 方式二 JSON

```js
// 基础用法
{
	type: 'image',
	src: 'https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg',
	css: {
		width: '200rpx',
		height: '200rpx'
	}
},
// 填充方式
// css objectFit 设置 填充方式 见下方表格
{
	type: 'image',
	src: 'https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg',
	css: {
		width: '200rpx',
		height: '200rpx',
		objectFit: 'contain'
	}
},
// css objectPosition 设置 图片的对齐方式
{
	type: 'image',
	src: 'https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg',
	css: {
		width: '200rpx',
		height: '200rpx',
		objectFit: 'contain',
		objectPosition: '50% 50%'
	}
}
```

### Qrcode 二维码

- 通过`text`属性填写需要生成二维码的文本。
- 通过 `css` 里的 `color` 可设置生成码点的颜色。
- 通过 `css` 里的 `background`可设置背景色。
- 通过 `css `里的 `width`、`height`设置尺寸。

#### 方式一 Template

```html
<l-painter>
  <l-painter-qrcode
    text="limeui.qcoon.cn"
    css="width: 200rpx; height: 200rpx"
  />
</l-painter>
```

#### 方式二 JSON

```js
{
	type: 'qrcode',
	text: 'limeui.qcoon.cn',
	css: {
		width: '200rpx',
		height: '200rpx',
	}
}
```

### 富文本
- 这是一个有限支持的测试能力，只能通过JSON方式，不要抱太大希望!
- 首先需要把富文本转成JSON,这需要引入`parser`这个包，如果你不使用是不会进入主包

```html
<l-painter ref="painter"/>
```
```js
import parseHtml from '@/uni_modules/lime-painter/parser'
const json = parseHtml(`<p><span>测试测试</span><img src="/static/logo.png"/></p>`)
this.$refs.painter.render(json)
```

### 生成图片

- 方式1、通过设置`isCanvasToTempFilePath`自动生成图片并在 `@success` 事件里接收海报临时路径
- 方式2、通过调用内部方法生成图片：

```html
<l-painter ref="painter">...code</l-painter>
```

```js
this.$refs.painter.canvasToTempFilePathSync({
  fileType: "jpg",
  // 如果返回的是base64是无法使用 saveImageToPhotosAlbum，需要设置 pathType为url
  pathType: 'url',
  quality: 1,
  success: (res) => {
    console.log(res.tempFilePath);
	// 非H5 保存到相册
	// H5 提示用户长按图另存
	uni.saveImageToPhotosAlbum({
		filePath: res.tempFilePath,
		success: function () {
			console.log('save success');
		}
	});
  },
});
```

### 主动调用方式

- 通过获取组件实例内部的`render`函数 传递`JSON`即可

```html
<l-painter ref="painter" />
```

```js
// 渲染
this.$refs.painter.render(jsonSchema);
// 生成图片
this.$refs.painter.canvasToTempFilePathSync({
  fileType: "jpg",
  // 如果返回的是base64是无法使用 saveImageToPhotosAlbum，需要设置 pathType为url
  pathType: 'url',
  quality: 1,
  success: (res) => {
    console.log(res.tempFilePath);
	// 非H5 保存到相册
	uni.saveImageToPhotosAlbum({
		filePath: res.tempFilePath,
		success: function () {
			console.log('save success');
		}
	});
  },
});
```


### H5跨域
- 一般是需要后端或管理OSS资源的大佬处理
- 一般OSS的处理方式:

1、设置来源
```cmd
*
```

2、允许Methods
```html
GET
```

3、允许Headers
```html
access-control-allow-origin:*
```

4、最后如果还是不行,可试下给插件设置`useCORS`
```html
<l-painter useCORS>
```



### 海报示例

- 提供一份示例，只把插件当成生成图片的工具，非必要不要在弹窗里使用。
- 通过设置`isCanvasToTempFilePath`主动生成图片，再由 `@success` 事件接收海报临时路径
- 设置`hidden`隐藏画板。
请注意，示例用到了图片，海报的渲染是包括下载图片的时间，也许在某天图片会失效或访问超级慢，请更换为你的图片再查看，另外如果你是小程序请在使用示例时把**不校验合法域名**勾上！！！！！不然不显示还以为是插件的锅，求求了大佬们！
#### 方式一 Template

```html
<image :src="path" mode="widthFix"></image>
<l-painter
  isCanvasToTempFilePath
  @success="path = $event"
  hidden
  css="width: 750rpx; padding-bottom: 40rpx; background: linear-gradient(,#ff971b 0%, #ff5000 100%)"
>
  <l-painter-image
    src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
    css="margin-left: 40rpx; margin-top: 40rpx; width: 84rpx;  height: 84rpx; border-radius: 50%;"
  />
  <l-painter-view
    css="margin-top: 40rpx; padding-left: 20rpx; display: inline-block"
  >
    <l-painter-text
      text="隔壁老王"
      css="display: block; padding-bottom: 10rpx; color: #fff; font-size: 32rpx; fontWeight: bold"
    />
    <l-painter-text
      text="为您挑选了一个好物"
      css="color: rgba(255,255,255,.7); font-size: 24rpx"
    />
  </l-painter-view>
  <l-painter-view
    css="margin-left: 40rpx; margin-top: 30rpx; padding: 32rpx; box-sizing: border-box; background: #fff; border-radius: 16rpx; width: 670rpx; box-shadow: 0 20rpx 58rpx rgba(0,0,0,.15)"
  >
    <l-painter-image
      src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
      css="object-fit: cover; object-position: 50% 50%; width: 606rpx; height: 606rpx; border-radius: 12rpx;"
    />
    <l-painter-view
      css="margin-top: 32rpx; color: #FF0000; font-weight: bold; font-size: 28rpx; line-height: 1em;"
    >
      <l-painter-text text="￥" css="vertical-align: bottom" />
      <l-painter-text
        text="39"
        css="vertical-align: bottom; font-size: 58rpx"
      />
      <l-painter-text text=".39" css="vertical-align: bottom" />
      <l-painter-text
        text="￥59.99"
        css="vertical-align: bottom; padding-left: 10rpx; font-weight: normal; text-decoration: line-through; color: #999999"
      />
    </l-painter-view>
    <l-painter-view css="margin-top: 32rpx; font-size: 26rpx; color: #8c5400">
      <l-painter-text text="自营" css="color: #212121; background: #ffb400;" />
      <l-painter-text
        text="30天最低价"
        css="margin-left: 16rpx; background: #fff4d9; text-decoration: line-through;"
      />
      <l-painter-text
        text="满减优惠"
        css="margin-left: 16rpx; background: #fff4d9"
      />
      <l-painter-text
        text="超高好评"
        css="margin-left: 16rpx; background: #fff4d9"
      />
    </l-painter-view>
    <l-painter-view css="margin-top: 30rpx">
      <l-painter-text
        css="line-clamp: 2; color: #333333; line-height: 1.8em; font-size: 36rpx; width: 478rpx; padding-right:32rpx; box-sizing: border-box"
        text="360儿童电话手表9X 智能语音问答定位支付手表 4G全网通20米游泳级防水视频通话拍照手表男女孩星空蓝"
      ></l-painter-text>
      <l-painter-qrcode
        css="width: 128rpx; height: 128rpx;"
        text="limeui.qcoon.cn"
      ></l-painter-qrcode>
    </l-painter-view>
  </l-painter-view>
</l-painter>
```

```js
data() {
	return {
		path: ''
	}
}
```

#### 方式二 JSON

```html
<image :src="path" mode="widthFix"></image>
<l-painter
  :board="poster"
  isCanvasToTempFilePath
  @success="path = $event"
  hidden
/>
```

```js
data() {
	return {
		path: '',
		poster: {
		    css: {
		        width: "750rpx",
		        paddingBottom: "40rpx",
		        background: "linear-gradient(,#000 0%, #ff5000 100%)"
		    },
		    views: [
		        {
		            src: "https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg",
		            type: "image",
		            css: {
		                background: "#fff",
		                objectFit: "cover",
		                marginLeft: "40rpx",
		                marginTop: "40rpx",
		                width: "84rpx",
		                border: "2rpx solid #fff",
		                boxSizing: "border-box",
		                height: "84rpx",
		                borderRadius: "50%"
		            }
		        },
		        {
		            type: "view",
		            css: {
		                marginTop: "40rpx",
		                paddingLeft: "20rpx",
		                display: "inline-block"
		            },
		            views: [
		                {
		                    text: "隔壁老王",
		                    type: "text",
		                    css: {
		                        display: "block",
		                        paddingBottom: "10rpx",
		                        color: "#fff",
		                        fontSize: "32rpx",
		                        fontWeight: "bold"
		                    }
		                },
		                {
		                    text: "为您挑选了一个好物",
		                    type: "text",
		                    css: {
		                        color: "rgba(255,255,255,.7)",
		                        fontSize: "24rpx"
		                    },
		                }
		            ],
		        },
		        {
		            css: {
		                marginLeft: "40rpx",
		                marginTop: "30rpx",
		                padding: "32rpx",
		                boxSizing: "border-box",
		                background: "#fff",
		                borderRadius: "16rpx",
		                width: "670rpx",
		                boxShadow: "0 20rpx 58rpx rgba(0,0,0,.15)"
		            },
		            views: [
		                {
							src: "https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg",
							type: "image",
		                    css: {
		                        objectFit: "cover",
		                        objectPosition: "50% 50%",
		                        width: "606rpx",
		                        height: "606rpx"
		                    },
		                }, {
		                    css: {
		                        marginTop: "32rpx",
		                        color: "#FF0000",
		                        fontWeight: "bold",
		                        fontSize: "28rpx",
		                        lineHeight: "1em"
		                    },
		                    views: [{
								text: "￥",
								type: "text",
		                        css: {
		                            verticalAlign: "bottom"
		                        },
		                    }, {
								text: "39",
								type: "text",
		                        css: {
		                            verticalAlign: "bottom",
		                            fontSize: "58rpx"
		                        },
		                    }, {
								text: ".39",
								type: "text",
		                        css: {
		                            verticalAlign: "bottom"
		                        },
		                    }, {
								text: "￥59.99",
								type: "text",
		                        css: {
		                            verticalAlign: "bottom",
		                            paddingLeft: "10rpx",
		                            fontWeight: "normal",
		                            textDecoration: "line-through",
		                            color: "#999999"
		                        }
		                    }],

		                    type: "view"
		                }, {
		                    css: {
		                        marginTop: "32rpx",
		                        fontSize: "26rpx",
		                        color: "#8c5400"
		                    },
		                    views: [{
								text: "自营",
								type: "text",
		                        css: {
		                            color: "#212121",
		                            background: "#ffb400"
		                        },
		                    }, {
								text: "30天最低价",
								type: "text",
		                        css: {
		                            marginLeft: "16rpx",
		                            background: "#fff4d9",
		                            textDecoration: "line-through"
		                        },
		                    }, {
								text: "满减优惠",
								type: "text",
		                        css: {
		                            marginLeft: "16rpx",
		                            background: "#fff4d9"
		                        },
		                    }, {
								text: "超高好评",
								type: "text",
		                        css: {
		                            marginLeft: "16rpx",
		                            background: "#fff4d9"
		                        },

		                    }],

		                    type: "view"
		                }, {
		                    css: {
		                        marginTop: "30rpx"
		                    },
		                    views: [
								{
									text: "360儿童电话手表9X 智能语音问答定位支付手表 4G全网通20米游泳级防水视频通话拍照手表男女孩星空蓝",
									type: "text",
									css: {
										paddingRight: "32rpx",
										boxSizing: "border-box",
										lineClamp: 2,
										color: "#333333",
										lineHeight: "1.8em",
										fontSize: "36rpx",
										width: "478rpx"
		                        },
		                    }, {
								text: "limeui.qcoon.cn",
								type: "qrcode",
		                        css: {
		                            width: "128rpx",
		                            height: "128rpx",
		                        },

		                    }],
		                    type: "view"
		                }],
		            type: "view"
		        }
		    ]
		}
	}
}
```


### 自定义字体
- 需要平台的支持，已知微信小程序支持，其它的没试过，如果可行请告之

```
// 需要在app.vue中下载字体
uni.loadFontFace({
	global:true,
	scopes: ['native'],
	family: '自定义字体名称',
	source: 'url("https://sungd.github.io/Pacifico.ttf")',
  
	success() {
	  console.log('success')
  }
})


// 然后就可以在插件的css中写font-family: '自定义字体名称'
```


### Nvue
- 必须为HBX 3.4.11及以上


### 原生小程序

- 插件里的`painter.js`支持在原生小程序中使用
- new Painter 之后在`source`里传入 JSON
- 再调用`render`绘制海报
- 如需生成图片，请查看微信小程序 cavnas 的[canvasToTempFilePath](https://developers.weixin.qq.com/miniprogram/dev/api/canvas/wx.canvasToTempFilePath.html)

```html
<canvas type="2d" id="painter" style="width: 100%"></canvas>
```

```js
import { Painter } from "./painter";
page({
  data: {
    poster: {
      css: {
        width: "750rpx",
      },
      views: [
        {
          type: "view",
          css: {
            background: "#d2d4c8",
            paddingTop: "100rpx",
          },
          views: [
            {
              type: "view",
              css: {
                background: "#5f7470",
                width: "33.33%",
                height: "100rpx",
                display: "inline-block",
              },
            },
            {
              type: "view",
              css: {
                background: "#889696",
                width: "33.33%",
                height: "100rpx",
                display: "inline-block",
              },
            },
            {
              type: "view",
              css: {
                background: "#b8bdb5",
                width: "33.33%",
                height: "100rpx",
                display: "inline-block",
              },
            },
          ],
        },
      ],
    },
  },
  async onLoad() {
    const res = await this.getCentext();
    const painter = new Painter(res);
    // 返回计算布局后的整个内容尺寸
    const { width, height } = await painter.source(this.data.poster);
    // 得到计算后的尺寸后 可给canvas尺寸赋值，达到动态响应效果
    // 渲染
    await painter.render();
  },
  // 获取canvas 2d
  // 非2d 需要传一个 createImage 方法用于获取图片信息 即把 getImageInfo 的 success 通过 promise resolve 返回
  getCentext() {
    return new Promise((resolve) => {
      wx.createSelectorQuery()
        .select(`#painter`)
        .node()
        .exec((res) => {
          let { node: canvas } = res[0];
          resolve({
            canvas,
            context: canvas.getContext("2d"),
            width: canvas.width,
            height: canvas.height,
			// createImage: getImageInfo()
            pixelRatio: 2,
          });
        });
    });
  },
});
```

### 旧版(1.6.x)更新

- 由于 1.8.x 版放弃了以定位的方式，所以 1.6.x 版更新之后要每个样式都加上`position: absolute`
- 旧版的 `image` mode 模式被放弃，使用`object-fit`
- 旧版的 `isRenderImage` 改成 `is-canvas-to-temp-file-path`
- 旧版的 `maxLines` 改成 `line-clamp`

## API

### Props

| 参数                       | 说明                                                         | 类型             | 默认值       |
| -------------------------- | ------------------------------------------------------------ | ---------------- | ------------ |
| board                      | JSON 方式的海报元素对象集                                    | <em>object</em>  | -            |
| css                        | 海报内容最外层的样式，可以理解为`body`                           | <em>object</em>  | 参数请向下看 |
| custom-style               | canvas 元素的样式                                            | <em>string</em>  |              |
| hidden               		 | 隐藏画板                                                    | <em>boolean</em>  |   `false`    |
| is-canvas-to-temp-file-path | 是否生成图片，在`@success`事件接收图片地址                   | <em>boolean</em> | `false`      |
| after-delay                | 生成图片错乱，可延时生成图片                                 | <em>number</em>  | `100`        |
| type                       | canvas 类型，对微信头条支付宝小程序可有效,可选值：`2d`，`''` | <em>string</em>  | `2d`         |
| file-type                  | 生成图片的后缀类型, 可选值：`png`、`jpg`                     | <em>string</em>  | `png`        |
| path-type                  | 生成图片路径类型，可选值`url`、`base64`                      | <em>string</em>  | `-`          |
| pixel-ratio                | 生成图片的像素密度，默认为对应手机的像素密度，`nvue`无效     | <em>number</em>  | `-`          |
| hidpi                | H5和APP是否使用高清处理     | <em>boolean</em>  | `true`          |
| width                      | **废弃** 画板的宽度，一般只用于通过内部方法时加上            | <em>number</em>  | ``           |
| height                     | **废弃** 画板的高度 ，同上                                   | <em>number</em>  | ``           |

### css
| 属性名                                                                              | 支持的值或类型                                                                                                                                                                       | 默认值   |
| ----------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------- |
| (min\max)width                                                                      | 支持`%`、`rpx`、`px`                                                                                                                                                                 | -        |
| height                                                                              | 同上                                                                                                                                                                                 | -        |
| color                                                                               | `string`                                                                                                                                                                             | -        |
| position                                                                            | 定位，可选值：`absolute`、`fixed`                                                                                                                                                    | -        |
| ↳ left、top、right、bottom                                                          | 配合`position`才生效，支持`%`、`rpx`、`px`                                                                                                                                           | -        |
| margin                                                                              | 可简写或各方向分别写，如：`margin-top`，支持`auto`、`rpx`、`px`                                                                                                                      | -        |
| padding                                                                             | 可简写或各方向分别写，支持`rpx`、`px`                                                                                                                                                | -        |
| border                                                                              | 可简写或各个值分开写：`border-width`、`border-style` 、`border-color`，简写请按顺序写                                                                                                | -        |
| line-clamp                                                                          | `number`，超过行数显示省略号                                                                                                                                                         | -        |
| vertical-align                                                                      | 文字垂直对齐，可选值：`bottom`、`top`、`middle`                                                                                                                                      | `middle` |
| line-height                                                                         | 文字行高，支持`rpx`、`px`、`em`                                                                                                                                                      | `1.4em`  |
| font-weight                                                                         | 文字粗细，可选值：`normal`、`bold`                                                                                                                                                   | `normal` |
| font-size                                                                           | 文字大小，`string`，支持`rpx`、`px`                                                                                                                                                  | `14px`   |
| text-decoration                                                                     | 文本修饰，可选值：`underline` 、`line-through`、`overline`                                                                                                                           | -        |
| text-stroke                                                                         | 文字描边，可简写或各个值分开写，如：`text-stroke-color`, `text-stroke-width`                                                                                                              | -        |
| text-align                                                                          | 文本水平对齐，可选值：`right` 、`center`                                                                                                                                             | `left`   |
| display                                                                             | 框类型，可选值：`block`、`inline-block`、`flex`、`none`，当为`none`时是不渲染该段, `flex`功能简陋。                                                                                                            | -        |
| flex                                                                                | 配合 display: flex; 属性定义了在分配多余空间,目前只用为数值如： flex: 1                                                                                                           | -        |
| align-self                                                                          | 配合 display: flex; 单个项目垂直轴对齐方式: `flex-start` `flex-end` `center`                                                                                                         | `flex-start`        |
| justify-content                                                                     | 配合 display: flex; 水平轴对齐方式: `flex-start` `flex-end` `center`                                                                                                         | `flex-start`        |
| align-items                                                                         | 配合 display: flex; 垂直轴对齐方式: `flex-start` `flex-end` `center`                                                                                                  | `flex-start`        |
| border-radius                                                                       | 圆角边框，支持`%`、`rpx`、`px`                                                                                                                                                       | -        |
| box-sizing                                                                          | 可选值：`border-box`                                                                                                                                                                 | -        |
| box-shadow                                                                          | 投影                                                                                                                                                                                 | -        |
| background(color)                                                                   | 支持渐变，但必须写百分比！如:`linear-gradient(,#ff971b 0%, #ff5000 100%)`、`radial-gradient(#0ff 15%, #f0f 60%)`,目前 radial-gradient 渐变的圆心为元素中点，半径为最长边，不支持设置 | -        |
| background-clip                                                                	  | 文字渐变，配合`background`背景渐变，设置`background-clip: text` 达到文字渐变效果 | -        |
| background-image                                                                    | view 元素背景：`url(src)`,若只是设置背景图，请不要设置`background-repeat`                                                                                                                                                           | -        |
| background-repeat                                                                   | 设置是否及如何重复背景纹理，可选值：`repeat`、`repeat-x`、`repeat-y`、`no-repeat`                                                                                                    | `repeat` |
| [object-fit](https://developer.mozilla.org/zh-CN/docs/Web/CSS/object-fit/)          | 图片元素适应容器方式,类似于`mode`,可选值：`cover`、 `contain`、 `fill`、 `none`                                                                                                      | -        |
| [object-position](https://developer.mozilla.org/zh-CN/docs/Web/CSS/object-position) | 图片的对齐方式，配合`object-fit`使用                                                                                                                                                 | -        |

### 图片填充模式 object-fit

| 名称    | 含义                                                   |
| ------- | ------------------------------------------------------ |
| contain | 保持宽高缩放图片，使图片的长边能完全显示出来           |
| cover   | 保持宽高缩放图片，使图片的短边能完全显示出来，裁剪长边 |
| fill    | 拉伸图片，使图片填满元素                               |
| none    | 保持图片原有尺寸                                       |

### 事件 Events

| 事件名   | 说明                                                             | 返回值 |
| -------- | ---------------------------------------------------------------- | ------ |
| success  | 生成图片成功,若使用`is-canvas-to-temp-filePath` 可以接收图片地址 | path   |
| fail     | 生成图片失败                                                     | error  |
| done     | 绘制成功                                                         |        |
| progress | 绘制进度                                                         | number |

### 暴露函数 Expose
| 事件名   | 说明                                                             | 返回值 |
| -------- | ---------------------------------------------------------------- | ------ |
| render(object)   |  渲染器，传入JSON 绘制海报 | promise   |
| [canvasToTempFilePath](https://uniapp.dcloud.io/api/canvas/canvasToTempFilePath.html#canvastotempfilepath)(object)   | 把当前画布指定区域的内容导出生成指定大小的图片，并返回文件临时路径。    |   |
| canvasToTempFilePathSync(object)    | 同步接口，同上                                                         |        |


## 常见问题

- 1、H5 端使用网络图片需要解决跨域问题。
- 2、小程序使用网络图片需要去公众平台增加下载白名单！二级域名也需要配！
- 3、H5 端生成图片是 base64，有时显示只有一半可以使用原生标签`<IMG/>`
- 4、发生保存图片倾斜变形或提示 native buffer exceed size limit 时，使用 pixel-ratio="2"参数，降分辨率。
- 5、h5 保存图片不需要调接口，提示用户长按图片保存。
- 6、画板不能隐藏，包括`v-if`，`v-show`、`display:none`、`opacity:0`，另外也不要把画板放在弹窗里。如果需要隐藏画板请设置 `custom-style="position: fixed; left: 200%"`
- 7、微信小程序真机调试请使用 **真机调试2.0**，不支持1.0。
- 8、微信小程序打开调试时可以生但并闭无法生成时，这种情况一般是没有在公众号配置download域名
- 9、HBX 3.4.5之前的版本不支持vue3
- 10、在微信开发工具上 canvas 层级最高无法zindex，并不影响真机
- 11、请不要导入非uni_modules插件
- 12、关于QQ小程序 报 Propertyor method"toJSON"is not defined 请把基础库调到 1.50.3
- 13、支付宝小程序 IDE 不支持 生成图片 请以真机调试结果为准
- 14、返回值为字符串 `data:,` 大概是尺寸超过限制，设置 pixel-ratio="2"
- 华为手机 APP 上无法生成图片，请使用 HBX2.9.11++（已过时，忽略这条）
- IOS APP 请勿使用 HBX2.9.3.20201014 的版本！这个版本无法生成图片。（已过时，忽略这条）
- 苹果微信 7.0.20 存在闪退和图片无法 onload 为微信 bug（已过时，忽略这条）
- 微信小程序 IOS 旧接口 如父级设置圆角，子级也设会导致子级的失效，为旧接口BUG。
- 微信小程序 安卓 旧接口 如使用图片必须加背景色，为旧接口BUG。
- 微信小程序 安卓端 [图片可能在首次可以加载成功，再次加载会不触发任何事件](https://developers.weixin.qq.com/community/develop/doc/000ee2b8dacf4009337f51f4556800?highLine=canvas%25202d%2520createImage)，临时解决方法是给图片加个时间戳
## 打赏

如果你觉得本插件，解决了你的问题，赠人玫瑰，手留余香。

![](https://testingcf.jsdelivr.net/gh/liangei/image@1.9/alipay.png)
![](https://testingcf.jsdelivr.net/gh/liangei/image@1.9/wpay.png)