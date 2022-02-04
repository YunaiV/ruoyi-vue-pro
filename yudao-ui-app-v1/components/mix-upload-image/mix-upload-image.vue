<template>
	<view class="upload-content">
		<view 
			class="upload-item"
			v-for="(item, index) in imageList" 
			:key="index"
		>
			<image class="upload-img" :src="item.filePath" mode="aspectFill" @click="previewImage(index)"></image>
			<!-- 删除按钮 -->
			<image class="upload-del-btn" 
				@click.stop.prevent="showDelModal(index)" 
				src="data:image/jpg;base64,/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAAA8AAD/4QMraHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzYgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOkZGNjhCRTZFMzgwRTExRUFCNkUwOEM2Q0Y3RjUzQTE2IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOkZGNjhCRTZGMzgwRTExRUFCNkUwOEM2Q0Y3RjUzQTE2Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6RkY2OEJFNkMzODBFMTFFQUI2RTA4QzZDRjdGNTNBMTYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6RkY2OEJFNkQzODBFMTFFQUI2RTA4QzZDRjdGNTNBMTYiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7/7gAOQWRvYmUAZMAAAAAB/9sAhAAGBAQEBQQGBQUGCQYFBgkLCAYGCAsMCgoLCgoMEAwMDAwMDBAMDg8QDw4MExMUFBMTHBsbGxwfHx8fHx8fHx8fAQcHBw0MDRgQEBgaFREVGh8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx//wAARCAAoACgDAREAAhEBAxEB/8QAawABAQEBAQAAAAAAAAAAAAAABggEAQIBAQEAAAAAAAAAAAAAAAAAAAABEAAABQMCAwgBBAMAAAAAAAABEQIDBBMFBgASIRQVMVFhcSIyBwhi8EFCM1M0FhEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMRAD8AovNs2sWHWJy8XhwUspHYyyghdedEBFLbaREDES8gDiOglbNfsBn2RSHEwpi7JbREaUWEoUObfzfAnFD3kQeGqgH/ANBf6/MdSlV+yrXc39p+7cegeYV9gM+x2Q2mbMXe7aAhVizVCtzb+D4m4ke4zDw0FU4VmtjzGxt3izuCplQih5lYE404HubcTxIQPyENRUrfYDNZGRZ9MhpcEbbZFqhRWj9NRAk+vzU4BH3AGqjH8fYFj2Z2S4W+LPXFzllYvW2I8KUxpUdKAFTaRI6pmPb2FwLcIAP6DeutdE5J7q9bl+R2DVqmWzb36Bp8hfHuPYVj1vhzbguTnUlaXp0FlSVRo0ZSRJCxLdU3bSHdx48CIRDR9f8ANZGO59DhqcELbe1phSmj9NRYkwvzS4JH3COgB5BX69cuY/v5p+qR++ord2+Og82Vu7uXeGizA8N1F5HJBHOrVP0bNvEz0FZpVS2NuLtCvm1VoEEqECMv2H+FXZ+qeoqTr91rrU3rdbq9ZfPcwdWqfq3n++qjuPcx1+2ct/sc2xR7ffUTt7OPboHf2AwqRjufzJiWxC23tapsV0vTUWJvo80uCZdwhoM3x58h2DCrBcZkO3LkZzIWLMCe8CFRo0ZSANSQPdU3GYFx4cSMBAU5e7u5dxvK5jw3UXuYGbvGrVPdv39pnoGGffIVmzOwW6TPtymc3irBmbc2QQmPJipQIApxIDuq7i/iQAfEiSAbvr/hUjIs/hzFNiNtsi0zZTpemogTYR5qcAy7gHQVTmuFWPMbG5Z7w2KmVCC2XkCTjTge1xtXEhA/IQ1FStmv1/z/AB2Q4qHDXe7aAjSlQkitzb+bAG4ke8jDx1UAwx6/i/y4W2VzH+Gg5v7S9u09A8wr6/5/kUhtUyGuyW0RCrKmpFDm38GBJxQ9xkHjoKpwrCrHh1jbs9nbFLKRFbzyxNx1wfc44rgYiXkAaiv/2Q=="
				mode="scaleToFill">
			</image>
			<!-- 进度 -->
			<view class="upload-progress" v-if="item.progress < 100">{{item.progress}}%</view>
		</view>
		<!-- 新增按钮 -->
		<view class="upload-add-btn" v-if="rduLength > 0" @click="chooseImage"></view>
		
		<mix-modal ref="mixModal" title="提示" text="确定要放弃这张图片么" @onConfirm="delImage"></mix-modal>
	</view>
</template>

<script>
export default {
	data() {
		return {
			imageList: []
		};
	},
	props: {
		count: {
			type: Number,
			default: 5 //单次可选择的图片数量
		},
		length: {
			type: Number,
			default: 1 //可上传总数量
		},
		index: {
			type: Number,
			default: 0
		}
	},
	computed: {
		rduLength(){
			return this.length - this.imageList.length;
		}
	},
	methods: {
		//选择图片
		chooseImage(){
			uni.chooseImage({
				count: this.rduLength < this.count ? this.rduLength : this.count, //最多可以选择的图片张数，默认9
				sizeType: ['original', 'compressed'], //original 原图，compressed 压缩图，默认二者都有
				sourceType: ['album', 'camera'], //album 从相册选图，camera 使用相机，默认二者都有
				success: res=> {
					this.uploadFiles(res.tempFilePaths);
				}
			});
		},
		//上传图片
		async uploadFiles(files){
			const item = {
				filePath: files[0],
				progress: 0
			}
			this.imageList.push(item);
			const result = await uniCloud.uploadFile({
				filePath: item.filePath,
				cloudPath: + new Date() + ('000000' + Math.floor(Math.random() * 999999)).slice(-6) + '.jpg',
				onUploadProgress: e=> {
					item.progress = Math.round(
						(e.loaded * 100) / e.total
					)
				}
			});
			if(!result.fileID){
				this.$util.msg('图片上传失败，上传任务已终止');
				this.imageList.pop();
				return;
			}
			const tempFiles = await uniCloud.getTempFileURL({
				fileList: [result.fileID]
			})
			const tempFile = tempFiles.fileList[0];
			if(tempFile.download_url || tempFile.fileID){
				item.url = tempFile.download_url || tempFile.fileID;
				this.$emit('onChange', {
					list: this.imageList,
					index: this.index
				})
			}else{
				this.$util.msg('图片上传失败，上传任务已终止');
				this.imageList.pop();
			}
			files.shift();
			if(files.length > 0){
				this.uploadFiles(files);
			}
		},
		//删除图片
		showDelModal(index){
			this.curIndex = index;
			this.$refs.mixModal.open();
		},
		delImage(index){
			this.imageList.splice(this.curIndex, 1);
			this.$emit('onChange', {
				list: this.imageList,
				index: this.index
			})
		},
		//预览图片
		previewImage(index){
			const urls = this.imageList.map(item=> item.filePath);
			uni.previewImage({
				current: index,
				urls: urls
			})
		}
	}
}
</script>

<style lang="scss">
	.upload-content{
		display: flex;
		flex-wrap: wrap;
		background-color: #fff;
	}
	.upload-item{
		position: relative;
		width:148rpx;
		height:148rpx;
		margin-right:28rpx;
		margin-bottom:24rpx;
		
		&:nth-child(4n){
			margin-right:0;
		}
		.upload-img{
			width:100%;
			height:100%;
			border-radius:8rpx;
		}
		.upload-del-btn{
			position: absolute;
			right:-16rpx;
			top:-14rpx;
			z-index: 5;
			width:36rpx;
			height:36rpx;
			border: 4rpx solid #fff;
			border-radius: 100px;
		}
		.upload-progress{
			position: absolute;
			left:0;
			top:0;
			display:flex;
			align-items:center;
			justify-content: center;
			width:100%;
			height:100%;
			background-color: rgba(0,0,0,.4);
			color:#fff;
			font-size:24rpx;
			border-radius:8rpx;
		}
	}
	.upload-add-btn {
		position: relative;
		float:left;
		width: 148rpx;
		height: 148rpx;
		margin-bottom: 24rpx;
		z-index: 85;
		border-radius:8rpx;
		background:#f7f7f7;
		&:before,
		&:after {
			content: " ";
			position: absolute;
			top: 50%;
			left: 50%;
			-webkit-transform: translate(-50%, -50%);
			transform: translate(-50%, -50%);
			width: 4rpx;
			height: 60rpx;
			background-color: #d6d6d6;
		}
		&:after {
			width: 60rpx;
			height: 4rpx;
		}
	}

</style>
