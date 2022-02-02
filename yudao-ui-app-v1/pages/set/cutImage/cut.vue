<template>
	<view class="content">
		<view class="title-view" :style="{height: navigationBarHeight + 'px'}">
			<navigator open-type="navigateBack" class="back-btn mix-icon icon-xiangzuo"></navigator>
			<text class="title">裁剪</text>
			<text class="empty"></text>
		</view>

		<view class="cropper-wrapper">
			<canvas 
				class="cropper"
				disable-scroll="true"
				@touchstart="touchStart"
				@touchmove="touchMove"
				@touchend="touchEnd"
				:style="{ width: cropperOpt.width, height: cropperOpt.height }"
				canvas-id="cropper"
			 ></canvas>
		</view>
		<view class="cropper-buttons">
			<view class="btn upload" @tap="uploadTap">重选</view>
			<view class="btn getCropperImage" @tap="getCropperImage">确定</view>
		</view>
	</view>
</template>

<script>
	import weCropper from './cut.js';
	const device = uni.getSystemInfoSync();
	const width = device.windowWidth;
	const height = device.windowHeight;

	export default {
		data() {
			return {
				cropperOpt: {
					id: 'cropper',
					width: width,
					height: height,
					scale: 2.5,
					zoom: 8,
					cut: {
						x: (width - 200) / 2,
						y: (height - this.systemInfo.navigationBarHeight - this.systemInfo.statusBarHeight - 200) / 2,
						width: 200,
						height: 200
					}
				},
				weCropper: ''
			};
		},
		computed: {
			navigationBarHeight(){
				console.log(this.systemInfo.navigationBarHeight);
				return this.systemInfo.navigationBarHeight;
			}
		},
		onLoad(option) {
			// do something
			const cropperOpt = this.cropperOpt;
			const src = option.src;
			console.log(src);
			if (src) {
				Object.assign(cropperOpt, {
					src
				});

				this.weCropper = new weCropper(cropperOpt)
					.on('ready', function(ctx) {})
					.on('beforeImageLoad', ctx => {
						/* uni.showToast({
							title: '上传中',
							icon: 'loading',
							duration: 3000
						}); */
					})
					.on('imageLoad', ctx => {
						uni.hideToast();
					});
			}
		},
		methods: {
			touchStart(e) {
				this.weCropper.touchStart(e);
			},
			touchMove(e) {
				this.weCropper.touchMove(e);
			},
			touchEnd(e) {
				this.weCropper.touchEnd(e);
			},
			convertBase64UrlToBlob(dataURI, type) {
				var binary = atob(dataURI.split(',')[1]);
				var array = [];
				for (var i = 0; i < binary.length; i++) {
					array.push(binary.charCodeAt(i));
				}
				return new Blob([new Uint8Array(array)], {
					type: type
				}, {
					filename: '1111.jpg'
				});
			},
			blobToDataURL(blob) {
				var a = new FileReader();
				a.readAsDataURL(blob); //读取文件保存在result中
				a.onload = function(e) {
					var getRes = e.target.result; //读取的结果在result中
					console.log(getRes);
				};
			},
			getCropperImage() {
				let _this = this;
				this.weCropper.getCropperImage(avatar => {
					if (avatar) {
						this.$util.prePage().setAvatar(avatar);
						uni.navigateBack();

					} else {
						console.log('获取图片失败，请稍后重试');
					}
				});
			},
			uploadTap() {
				const self = this;

				uni.chooseImage({
					count: 1, // 默认9
					sizeType: ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
					sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
					success(res) {
						let src = res.tempFilePaths[0];
						//  获取裁剪图片资源后，给data添加src属性及其值
						self.weCropper.pushOrign(src);
					}
				});
			}
		},
	};
</script>

<style lang="scss">
	page, .content{
		width: 100%;
		height: 100%;
		overflow: hidden;
	}
	.content {
		display: flex;
		flex-direction: column;
		background-color: #000;
		padding-top: var(--status-bar-height);
	}

	.title-view{
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		background: transparent;
		
		.back-btn{
			display: flex;
			justify-content: center;
			align-items: center;
			width: 42px;
			height: 40px;
			font-size: 18px;
			color: #fff;
		}
		.title{
			font-size: 17px;
			color: #fff;
		}
		.empty{
			width: 42px;
		}
	}

	.cropper {
		width: 100%;
		flex: 1;
	}

	.cropper-wrapper {
		flex: 1;
		position: relative;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: center;
		width: 100%;
		background-color: #000;
	}

	.cropper-buttons {
		flex-shrink: 0;
		display: flex;
		justify-content: space-between;
		align-items: center;
		width: 100%;
		height: 50px;
		background-color: rgba(0, 0, 0, 0.4);
		
		.btn{
			width: 100px;
			height: 50px;
			line-height: 50px;
			font-size: 15px;
			color: #fff;
			
			&.upload{
				padding-left: 20px;
			}
			
			&.getCropperImage{
				padding-right: 20px;
				text-align: right;
			}
		}
	}
</style>
