<template>
	<view class="page">
		<!-- <demo-block title="json用法">
			<image :src="picture" v-if="picture" mode="widthFix"></image>
			<l-painter :board="poster" @success="picture = $event" isCanvasToTempFilePath ref="json" style="position: fixed;left: 1500rpx;width: 750rpx;" path-type="url"></l-painter>
		</demo-block> -->
		<!-- <demo-block title="基础用法">
			<l-painter ref="painter">
				<l-painter-view css="background: #07c160; height: 120rpx; width: 120rpx; display: inline-block">
				</l-painter-view>
				<l-painter-view
					css="background: #1989fa; height: 120rpx; width: 120rpx; border-top-right-radius: 60rpx; border-bottom-left-radius: 60rpx; display: inline-block; margin: 0 30rpx;">
				</l-painter-view>
				<l-painter-view
					css="background: #ff9d00; height: 120rpx; width: 120rpx; border-radius: 50%; display: inline-block">
				</l-painter-view>
			</l-painter>
		</demo-block> -->
		<!-- <demo-block title="View 容器">
			<l-painter>
				<l-painter-view css="background: #f0f0f0; padding-top: 100rpx;">
					<l-painter-view css="background: #d9d9d9; width: 33.33%; height: 100rpx; display: inline-block">
					</l-painter-view>
					<l-painter-view css="background: #bfbfbf; width: 66.66%; height: 100rpx; display: inline-block">
					</l-painter-view>
				</l-painter-view>
			</l-painter>
		</demo-block> -->
		<!-- <demo-block title="Text 文本">
			<l-painter>
				<l-painter-view css="background: #f5f5f5; padding: 30rpx; color: #222a29">
					<l-painter-text text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼" />
					<l-painter-text text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼"
						css="text-align:center; padding-top: 20rpx; text-decoration: line-through " />
					<l-painter-text text="登鹳雀楼\n白日依山尽，黄河入海流\n欲穷千里目，更上一层楼" css="text-align:right; padding-top: 20rpx" />
					<l-painter-text text="水调歌头\n明月几时有？把酒问青天。不知天上宫阙，今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。"
						css="line-clamp: 3; padding-top: 20rpx; background: linear-gradient(,#ff971b 0%, #1989fa 100%); background-clip: text" />
				</l-painter-view>
			</l-painter>
		</demo-block> -->
		<!-- <demo-block title="Image 图片">
			<l-painter>
				<l-painter-view>
					<l-painter-text text="基础用法"
						css="margin-top: 30rpx; display: block; padding-bottom:20rpx; color: #999" />
					<l-painter-view>
						<l-painter-image
							src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
							css="width: 200rpx; height: 200rpx" />
					</l-painter-view>
				</l-painter-view>
				<l-painter-view>
					<l-painter-text text="填充方式"
						css=" margin-top: 30rpx; display: block; padding-bottom:20rpx; color: #999" />
					<l-painter-view css="display: inline-block; padding-right: 12rpx">
						<l-painter-image
							src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
							css="width: 200rpx; height: 200rpx; object-fit: contain; background: #f5f5f5" />
						<l-painter-text text="contain" css="margin-top: 10rpx; display: block; text-align:center;" />
					</l-painter-view>
					<l-painter-view css="display: inline-block; padding: 0 12rpx">
						<l-painter-image
							src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
							css="width: 200rpx; height: 200rpx; object-fit: cover; background: #f5f5f5" />
						<l-painter-text text="cover" css="margin-top: 10rpx; display: block; text-align:center;" />
					</l-painter-view>
					<l-painter-view css="display: inline-block; padding: 0 12rpx">
						<l-painter-image
							src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
							css="width: 200rpx; height: 200rpx; object-fit: fill; background: #f5f5f5" />
						<l-painter-text text="fill" css="margin-top: 10rpx; display: block; text-align:center;" />
					</l-painter-view>
					<l-painter-view css="display: inline-block; margin-top: 30rpx">
						<l-painter-image
							src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"
							css="width: 200rpx; height: 200rpx; object-fit: none; background: #f5f5f5" />
						<l-painter-text text="none" css="margin-top: 10rpx; display: block; text-align:center;" />
					</l-painter-view>
				</l-painter-view>
			</l-painter>
		</demo-block> -->
		<!-- <demo-block title="QRcode 二维码">
			<l-painter>
				<l-painter-qrcode text="limeui.qcoon.cn" css="width: 200rpx; height: 200rpx" />
			</l-painter>
		</demo-block> -->
		<demo-block title="栗子海报">
		<image v-if="picture2" :src="picture2" mode="widthFix"></image>
		<l-painter css="width: 750rpx; padding-bottom: 100rpx; background: linear-gradient(180deg,#ff971b 0%, #ff5000 100%)" 
				@fail="fail"
				@done="done"
				pathType="url"
				ref="poster"
				performance
				>
			<l-painter-image src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"  css="background: #ffffff; object-fit: cover; margin-left: 40rpx; margin-top: 40rpx; width: 84rpx; border: 2rpx solid #ffffff; box-sizing: border-box; height: 84rpx; border-radius: 50%;"/>
			<l-painter-view css="margin-top: 40rpx; padding-left: 20rpx; display: inline-block">
				<l-painter-text text="隔壁老王" css="display: block; padding-bottom: 10rpx; color: #ffffff; font-size: 32rpx; fontWeight: bold"/>
				<l-painter-text text="为您挑选了一个好物?" css="color: rgba(255,255,255,.7); font-size: 24rpx"/>
			</l-painter-view>
			<l-painter-view css="margin-left: 40rpx; margin-top: 30rpx; padding: 32rpx; box-sizing: border-box; background: #ffffff; border-radius: 16rpx; width: 670rpx; box-shadow: 0 20rpx 58rpx rgba(0,0,0,.15)">
				<l-painter-image src="https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg"  css="object-fit: cover; object-position: 50% 50%; width: 606rpx; height: 606rpx;"/>
				<l-painter-view css="margin-top: 32rpx; color: #FF0000; font-weight: bold; font-size: 28rpx; line-height: 1em;">
					<l-painter-text text="￥" css="vertical-align: bottom"/>
					<l-painter-text text="39" css="vertical-align: bottom; font-size: 58rpx"/>
					<l-painter-text text=".39" css="vertical-align: bottom"/>
					<l-painter-text text="￥59.99" css="vertical-align: bottom; padding-left: 10rpx; font-weight: normal; text-decoration: line-through; color: #999999"/>
				</l-painter-view>
				<l-painter-view css="margin-top: 32rpx; font-size: 26rpx; color: #8c5400">
					<l-painter-text text="自营" css="color: #212121; background: #ffb400;"/>
					<l-painter-text text="30天最低价" css="margin-left: 16rpx; background: #fff4d9; text-decoration: line-through;"/>
					<l-painter-text text="满减优惠" css="margin-left: 16rpx; background: #fff4d9"/>
					<l-painter-text text="超高好评" css="margin-left: 16rpx; background: #fff4d9"/>
				</l-painter-view>
				<l-painter-view css="margin-top: 30rpx; display: flex">
					<l-painter-text uid="tag" css="position:absolute; top: 6rpx; padding: 5rpx 10rpx; color: #212121; background: #ffb400; font-size: 24rpx" text="京东自营"></l-painter-text>
					<l-painter-text css="text-indent: calc(tag.width + 10rpx); flex: 1; padding-right: 20rpx; box-sizing: border-box; line-clamp: 2; color: #333333; line-height: 1.8em; font-size: 36rpx;" 
						text="360儿童电话手表9X 智能语音问答定位支付手表 4G全网通20米游泳级防水视频通话拍照手表男女孩星空蓝"></l-painter-text>
					<l-painter-qrcode css="width: 128rpx; height: 128rpx;" text="limeui.qcoon.cn"></l-painter-qrcode>
				</l-painter-view>
			</l-painter-view>
		</l-painter>
		<button type="default" @click="save">save</button>
	</demo-block>
	</view>
</template>

<script>
	export default {
		data: () => ({
			picture: '',
			picture2: '',
			show: false,
			"poster": {
					"css": {
						"width": "750rpx",
						"height": "1333rpx"
					},
					"views": [{
						"type": "image",
						"src": "https://m.360buyimg.com/babel/jfs/t1/196317/32/13733/288158/60f4ea39E6fb378ed/d69205b1a8ed3c97.jpg",
						"css": {
							"width": "750rpx",
							"height": "1333rpx",
							"background": "white",
							"position": "fixed",
							"top": "0",
							"left": "0",
							"zIndex": -1
						}
					}, {
						"css": {
							"width": "750rpx",
							"position": "fixed",
							"top": "610rpx",
							"left": "303rpx"
						},
						"views": [{
							"type": "view",
							"css": {
								"width": "150rpx",
								"height": "150rpx",
								"background": "#FFF"
							},
							"views": [{
								"type": "image",
								"src": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHgAAAB4CAMAAAAOusbgAAAC91BMVEX/GAD/////AAD/CAD3rq3/FAD//Pz/DwD2pKP7zcr/wrz/5+T/YlH/MBv52db/jID/Oyb/GAD/8/L/STb/Lhj/JA3/gXP/fnD/cGL/////AAD/GAD/EwDjHRv/EAD/wrz/DAD/FgD/CQD/AwH/29ffAAD/Y1L+/Pz/HAP/BQD/5+T/GwH/STb/MBv/FQD/+/r/p57/Lhj/DgD/cGL/fnD/zsn/PCj/YE//Szn/jID//f3/JA3/NyL/8/Pra2v/ta7/1tL/rKTpTEvpX1//3tn/vbzqWVf/a1v/mY//yML/gXP/VUP/5ePkGBb/KBL/Njb/Mx7/joL/9/b/3tz/ubL/dmf/Gxv/4d3/vLX/Z1fhAQD/n57/U1H/IQvhCQf/y8j/wbz/emz/bl7/LBX/HwjaAAD/2Nf/k4f/kIT/Pyv/JhD/rqX/qJ//oZj/lYr/gYD/RjXkIR7/+fn/4+D/09H/v7j/UT//JiX/6ujW09T/l43/iH7/WUf/TjvkHhz/8O7/6ebn5eb/npT/nJHxh4b/fG7iExHiDw3/8vH/sqr/sKf/hnn/XUvoPj3/RTL/LhrlAAD/2tf/0s7/hHf/c3HkJSPW/f7/7er/pp3zmJjnNDP/Qy//KBz/GBj/HRHe3N33xcb/qKPufn7/dGXtXVz/OTjq6Or/pJrvdnbtZWX/ZGLqVFT/WlL/UE//Dg3kBwXoAADk/f3y8PLwpqj/pp7/n5f/cHDlLi3lEA7t6+3Y19r61NX2vL3zt7eurK3/kI/yjo1sb3HtcHD/IRn/KRjpDAv6///z///49vf54uPY4uPl3+HGxMW1trj/rq3flpaEh4j/a2H/XFvkR0jpRkX/RkP28/XQ8/Xb7/HdsbLLm5yAfX/fbGzoWVjjUVD/MyjG/f3G2ty71dfbysvntbbNp6mjl5njeXjcWVjrNzbcNTTnEhDo7u/QsbLfqqvap6fOoaJ1oaLhi4t3eHm7c3O0Tk6lMC8CLC3gHBrRGxr/FRUAExX7ExI4D1MFAAAAGXRSTlPy8vLy/vLx8v358vLy8vny8uXy8vLy8vLyQ/UAIwAAE0tJREFUaN6tm3dcFEcUx4H03svOrcfuncfdckU4WjhAkIgEEBQkIigo1YDGiD2IaBRFVBRR7BpbjCVRE2NJ77333nvvPX/kTVlm95b18PPx90ncMrPve7M75b2ZIeyiMFOdEnZOeHj4hRHkIuKCcKpTI06LuBCOF192Cr5/WsTZ4XqdDRkuPT08/PTzIkxNA9ZiLpsrWhCEREQuULJAlY58KBWOaSkKvi+iSEGvSCTaYwcIgtNrt5grDJN9Yi/iYJIBFRrBOMGKxpmDTU0DF8BuBRll14DdNrgx2wimT44yB5uYZmCfMinSqImiVQXLytLIyKyFwzzDCvVgUZofOS5rrjnY1DQBi2iwYNQUq6SCfeQzxiOElunBfmWWADIHm5qmYLBrVH8OFtEgAragOA24KEWx2hIEo8bBA9UUbGK6b+AaKOnhIHCiAGowARfDA/4pfQZPGhOlasxSLTh99F2jR+jB1nLIleQSOXjBmKgxE9n5PHhgSYCAjaZXGcAexBXLwUx6sFtEINnNwTPgulTQC8BG09UGcBSyqEJXhQCrGTk4GxLjjWCjac+JgwuZ7UgECi7xHLiuPMng8UfiiZZLFqk+Pn71whuSiZbW+Dk4/YbkG+JXk3xHrjlJYDeiUty0B1suMDXY9bX6OkRVcBLAfJDgiXnMblpwO05GzPbJBLMO3e62oCQVZIcbORrw9dCpS7jinlywf3lcXJzHb5ESryF6cHzV3pf3lkCGCQ/SO82SxT88Lm5v4ckFo/lwPgiqsh8xCUzVvBKgqpNSq42OwDVItlhkIpt7HgN4kEwFmaJOHDwHcQ0PArvx51yIwazEYElxqQAvXEtubNZQYqNprwG89JarVN2yQw+WxVS4uY6CxQbIsVGWJfcSGIsXw804eLJaJGBlTiRRVg7tq42m40KPTtrxuFgAUTBqhuNQCYqINLU6jlV9K6KSTUenEwO3a8B5ONHvBzdPlOxqO65in1EWsfo8HmcIRi3Wggexm4ehMHPwyAdN2q7tq0vhWnJDie12O9Ru7oGYmGY+V0x/o0aJfj14Vl3/usKR3pHL6lbWjdq0yRtrkSl4KtzIHun1pvgsvhqv11vt6wGbmiZgeGlWo6C26MGVyIqyA85AO/Kj4YGAc67aZW6EG/MDTmcygubkdDqnc5/L1DQDWyWj/MHga5FExuNRyAaNAqSC6yEBV/nxAMbOQAIHm5oG8Ck2UyFpKGmmCophjsC1cJyu+lzYyxxMmqmMrofjKvhFSVpwCjK3DeGPy1RSTTQukN1in8DA2WlpaYOwlwnHaFziSDhZzsDJdpc9Ca6LVfBwxdx2WNg50aYaGu3ElWpo9NAiCpZdKSkpNbJFdqeALHBSDkeXTMFpkHHB/vKUcplWLmHeUHPb54SFC30UgN1WRVFwq5EVkIzjOjiKbgymWoJwBgI+vsL7Dq5EtD7KtF5yiVY/VC6qGCTjDKjhJIJ3QFXxQ2lFqBqShuuWbAo6zMFuH1TLmhMDj5+ZOHMpOy/58MMP67Wh0dSxCQnxiqx4EhISJlitKtenzE9IGFsFT46nYKUerrsCapc2M1HVTDy+jWXXF+rAVQj1eDYZCDRV0AscARK4zZV6yiyiOmwfMldRMJohaDQcceGEUew8QgfOJT471WC/TbEEB4ML2CCR4fdzcCR36AnYI2g0GfmIVC8hElnxuSVMB07WgOdy74ZrYl/AcwSNViAqmxt6AbjuIm6EG8DnJiXdnoG/7+1JSalWCs64Pen2qG3btq1LGnPbmME0kageMoQCWxvAYpWTgadvG0S0zGaxxkJCNlxPIoF5BEJknPewnxXHAk2fAFLYWL8cUdndocHEGZCmCHqNp74viawH9OLsAZjF9N6AxmdPQionBBi+Bb5p7EBykYmzRwqluNSAECE/j1II2GXXeIJdkJcJoQRSqxXiDbYjkKsX8HgKJjW7iNRqFdwOsdhVEgWXQAi2qrIyvtJl7QFLM5ILrwPdMAH/5i1bCpgatuwoLCycs6VgS17h0tsqwUqVJBNwIHn1wiOD8Ws4QkI+LCkWwr7K2bOTL2Bgqus0tXoAj0QJGLpjE60tKSnBBZwyNWNTIu5p2Og0AH/TdNykqLePRZxjkfRcKpjH3twjA3FwofDQa0OYdg7sTe8fWpsdNOvDqw+X3esEsGmJpyCmHAa+TuiodRxXta8OjmJtwkp7raCpCHdv4GIIvWawbzz0waYHC19+OQ5E4jIGHuLop1UFsMoqKh4fuf/xv8gNx5BZeAKmbm/c3rhJTdc0NUjU9Py9cURz/LIOzH+WomnHosBlBLcscji6d7Yeun/r1s9++/3vgy0EnCmA+PsS3Xq/ehTqDRyFZFnTjtGKgDm4cZGjbd+htTTt4X//fOnIgSBwiWjD5vTgiQbwCBYLaMHD9V09QvEU3FhW0VhW9sj5cPfXB4SnnnrjuQeev+ON/EV6sJOVGEyr4t2alYF9tuT09PSrbDqwraA9/YX0IoFq9ut5rw8i4Ja2jq8dR8964oknnM53H3DeeffdkPzE041tFDz0hXSsF26d8XpeXo1ITL8wlYPFlLy810sDFEyjUL9bB3b7DOMxAbflP7tr9/lP3gnaJQh377oJ68n7DuRT8K2ISqa1mppewMFs6GJgi0wGTB0YXAufYok2gB0733x+zR3PAm3XU+++JLzx7M3P3vSUMBBSCHgQEn0gOg/itWPTotYhQx4OZvMqVjcdPpI0boIiGMBlZ775xU9v3Yy1656HH37gxpvuvunjB/KPMfAEJJP5RpfApl+xCxqDL1hHnqgBy74Vqamp5RByFaSmzlxFHCO4AeepkdOmTSvCveC0jGmzMLji6++e++SDH9fceM8999y4684vHn743Zde+vmPg40c7KuBJ5d3TZvWNRnM1PgoeB5YaJqZOrMZjisDuuZEekW9ilxIQWPhpBn5UTIGdx5a88nH3x77dM09N2Ldveu555//6P7/oE4zMHubJRKyk/jGgyi4EiwsI1/RhhrU5kRslwJ4vBCkYHD+wbc/eu7VxzuPvbhmzTMEffMdT97/eEWjFnwVLiAG48jrKgZeiPzkK+bowHQcsKFVwWCNI4CacIm//ear/IP9Gg90vvrDM2vefOaZt9bctL0TuCp4IlLQRjxiKQjZ60isqZBxrRLMN/MugoGLIdRKgpArGXd2i9MW488aSFucFq0B01fdeOzgIsC09Ovs3PDli++8886X3Z34Rk+JIcLzgLUMi98q9sclhhsTcSgJ5qPY0LViblraxQDGoVf5/gUQcqXhHn1/yv487N5u2YLjsh6wEksbDVNL/oGDLZ9//t5B3FlycBFYad9fvj91KgRmA3DRWci3GI5zKVi2QKR3GWlOkqKgJT1Do4ISqUOP4zIOrsFgru4DnW/D9y3L14GxEpAdlQsmAjAJ+U4JOwNiMAi0esCTkJ/Uy2ibguMUDi7QgRcd/eCXO96641Av4OlIQgVmYEgEs+qMgA9a+TqYvQgQr0xB9YFAAEpss8kmYFDnwDU3v/39Z529gRVUUxRwEtc6wMxysGKTCPgsiMHqFYuvwesd2YTj8LEJY28dueLReojRIsttZuD8Yy9+9eIH33z6uBG8GCykr9i0aRMemZaNXDGSvUw1MaFd9LMQZgZePrTbUS5LnYYQ/UoNihm4reO9zquPdr/HuUZHAAcoqXCcL+iVZpUYeDkSsdBsXgPIxM6AGiTZuwhYRCkc3NJW1q/fgX7H8vPLKlp6BY+wwhtdyXsunUoYmMeS8RxMHQE/+915cLRScGOFo8zRnVkG/lZ+haNjAxzLHGX9WvTg/qTEJuB5PeCELDrnWsLBosvjucXTnjUuq3nY5GGTsmBCVrgag9v2PNS6+7601u2tD23Yvl4oWn8qaH0L80CmQ+Y43MuPy8oal2YAA2rYZE+9LBOwQdwRwBcFNKoD7cPgij1bn551L/7/zO6Os4Q9mZm7d+8e2E/jCHi5ISO4mM52m4PxRLximaedRKU+l2OPMDBz55CdmVufdtTee19txyO1tbWORvaqlwDHYwLmpo8HdotqiTf2gPcR8HbhkZ1DXhu4r04A0H3bi4TW1od2tvVjr5pORZiDVxrAC17JfiWdg6WGw9cfXlD6SnZ2ip+C09fuweBFG65e33qvsH797sxFHXtaW0c4H1rfOoSC3xeaFdlfkE1UWhodBM4Ba3l+OQjs4auDvFbbifNCwZX9Sa3udmxfX/uI4Ki4r7Gi7NR9tWc6a1871ZFPwFcLssJn6Q21uomHMRxMPJBkDiZeN9txQcGFJQRccVTYU7teWOSYu7V2u9Bdu1Vw5AutDgauViyqJGv/IHAMceiNYB6JpqshDCJeoA7sOPP+2syhrRUVG+7t3rq7duesRyocA7va8ilYQUwWdyjwBcnJN5RQsDQDzovxWHxkIY6eZy+thAXSAkkHzu9oWdTR4YCO5OiGDW35r21wtED/2U3AQ4TZ8ABWnCyGAkew6DmKzTbkqY5ecK0eP29fLY4P8X9U9Iqr9qjQI58tBNg4+WJYhWHgVWsPvXp1b9qXmZn5PhyHbNjOu2Of1JcSF2vAuWYlnrNp2mCDYKjFoy37l2tlb7W6HXFFhJ0blTQmg4Ntc2KIRjfddtttSQtGx8TE2ij41luyrw3WjuEFj21JnjBhQvOW6n+SteNAFDytbccJo2NGzx4T1aNzNbWaTQJrwy6lpx2bqBwcCoRlU9AOYzIHV5HqwxWuB4NkKrIlo6hcweG1KZjFZWwlNf544Fw4RpmB3TY+S0J7rgbYn4RAxSZcp5dlkNwkADJqBgBHsXkdPfhstj8JwKLXQ5Uoyrbq6ZGRxeWS7Kv3kPE4WFmRdN1SFhPhiQLRokTBTVxb5maN49kSFVnJhWOpwsFp47Iizya1ehQFo0nqaM3GY7xarPQnHohR9qkETAOgONxV8blwLpHtpZDcHJxBavVpeA8aA1+nDiN47KJbzazKNOpzBUuRoxk4h9QdspJK4rJxyMqzybivJ0cOXinZxNPCIuy0xHmIe5mjoDxMiEyflOICwZXEJgZ9kKAMBbALqZOoNHeeGizYNSIxP0mkmkt8rktjH9ucg1/W5tjNTWqJNz8Wy/TYo8UjRozIs1vkFLhKkWnNd8XGPuqdB+CNmx/bvAQyVG0muTfHUbDsjtWoRoanWWIRZJ5O/OrTBwwYEMCuLDlSBQZolIjoisJsOG9ChIxScYLAngLnGM1Xc6tzmUUaC1EInmaJ4wxrEqZKRZIkWmjUfj0DJ2ozbEQ2tI5f0uk5fslifh7CSFLfwMMBDEJLcURHz42O5PXGPXtcURrwWGQjJs4IO915HNESW7FImNFEz8nCtVMH5vEZB9MbzLmhBgPTkZ+YOCPsPK+pHh0+C49wdf2x6tbi+li3kpzDUj0kBoGbYB1/hwbsXA4BWw4F+1KoyZEzwALW+dCcTMRajFF0c4KayMG5iLQYDnbxvsktMZP1apcZcgusUXQ7hs1FZm/ZxM461h2X4saogquR2jfRfRo+spuHgUNtge1dUyVwT9I04G245gXvEbBoS0yXtTg41BZYqlzY/norHJcM8xBB2OWzbPRQlYsWcTgcvXAsoEMMAQdysnrGH6UKTOYqsljOHrok1BZY7u0vJIuPTPCaLIgJ94gKLZDbTwdVY3OaT2foe7z9iFBbbqiWIZlvq8KiYz8/18kC4EAQuJDM0IuyzJ44LSSYxzcczDaEshEDBctt0fcwMHSROesJJJE+HRFqCyxVJLhqXRxsqwZ38HCNRH69LTdGr1ybbK1JiuLyWi3WRDC7Y/RdONHWsC4m5syQW2C5OBglMr8IpC47cI2gQS6XxCpBM03kc5nGrXMhwCH30Ft6E4riC7UnCl6KQC4KXoyY0g0l5mAXcX1dHJxOpnH6BJ60Ond1lzq2wGJpqSQT8JTxcAEiId+E1bk9KpV4hZeqIEe9xMFkoTY+Pvfy0GB30BRZFxIBHKQZ+lqtSkRTqSfIwHyhNiI0mE++6P9qgIuumPcibpqD+74v06UtMd/KrlcUXyLmDR3xeJDW6hMDL4Rd6jks7MqhYNyOR08sUqeuYOv6tdALJFp7LLB2zONBtR2PLu4rmCub/7mCW9Quw8XC+V1wXKCx4NW/Dt5zxZ44eCGS+VZ2WebLcJORSByBQm7BtK9Gk0NtgTXfQ7+Njk48MRUhUvuuo9+ZDEDiFIGrmY1tC+CYatgVsWqYR9WwuOOBI+k0KCSyITcJJk4LYZYVW0iFMbkcLDTT8ZiGd1VsNC+GjEnwwKDJkz2XmIxO5mDugUQzJ4P4c2z4aqcLXiCSSDVR4MphC4/hJwjmPhcFVyM/AWcjkQTmg7Tgajb/vk0H9qNYCjbfAmsOhi03dJtzOXOrliE7SZxPPyOP2hU7Ygs87Ek2aRgeYgusOVhy5fRfmVHPXOdK8KmTsVMG18s0YF81XmOp42ZjFB8Fh9gCaw7GT9nsYwM0WIADP3KwiKbD9TKkM0vB5ltgQ4L9kgIfyVwAVtdLuVlrD9hippBgugxnphBbboRQfz7IdUVEWMSV7PysM87AGc445axwc8GfD9K/LTw3Qmc24rzTIfF/OQkX7MfEGOgAAAAASUVORK5CYII=",
								"css": {
									"width": "138rpx",
									"height": "138rpx",
									"marginTop": "6rpx",
									"marginLeft": "6rpx"
								}
							}]
						}]
					}]
				},
		}),
		mounted() {
			this.$refs.poster.canvasToTempFilePathSync({
				fileType: 'jpg',
				quality: 1,
				success: (res) => {
					console.log(`mounted`, res.tempFilePath)
					this.picture2 = res.tempFilePath
					this.saveImage()
				}
			})
		},
		methods: {
			close() {
				this.show = false
			},
			fail(v) {
				console.log(v)
			},
			done(v) {
				console.log('绘制完成:')
			},
			save() {
				this.$refs.poster.canvasToTempFilePathSync({
					fileType: 'jpg',
					quality: 1,
					success: (res) => {
						console.log(res.tempFilePath)
						this.picture2 = res.tempFilePath
						this.saveImage()
					},
					fail(e) {
						console.log('???????????',e)
					}
				})
			},
			// 保存图征
			saveImage() {
				// #ifndef H5
				uni.saveImageToPhotosAlbum({
					filePath: this.picture,
					success(res) {
						uni.showToast({
							title: '已保存到相册',
							icon: 'success',
							duration: 2000
						});
					},
				});
				// #endif
			}
		}
	};
</script>

<style lang="stylus" scoped>

</style>
