<template>
	<view class="app">
		<view class="cell">
			<text class="tit fill">头像</text>
			<view class="avatar-wrap" @click="chooseImage">
				<image class="avatar" :src="tempAvatar || userInfo.avatar || '/static/icon/default-avatar.png'" mode="aspectFill"></image>
				<!-- 进度遮盖 -->
				<view class="progress center"
					:class="{
						'no-transtion': uploadProgress === 0,
						show: uploadProgress != 100
					}"
					:style="{
						width: uploadProgress + '%',
						height: uploadProgress + '%',
					}"></view>
			</view>
		</view>
		<view class="cell b-b">
			<text class="tit fill">昵称</text>
			<input class="input" v-model="userInfo.nickname" type="text" maxlength="8" placeholder="请输入昵称" placeholder-class="placeholder">
		</view>
		<u-cell-group>
			<u-cell title="昵称" :value="userInfo.nickname" isLink @click="nicknameClick()"></u-cell>
		</u-cell-group>
		
		<u-modal :show="nicknameOpen" title="修改昵称" showCancelButton @confirm="nicknameSubmit" @cancel="nicknameCancel">
			<view class="slot-content">
				<u--form labelPosition="left" :model="nicknameForm" :rules="nicknameRules" ref="nicknameForm" errorType="toast">
					<u-form-item prop="nickname">
						<u--input v-model="nicknameForm.nickname" placeholder="请输入昵称" border="none"></u--input>
					</u-form-item>
				</u--form>
			</view>
		</u-modal>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				uploadProgress: 100, //头像上传进度
				tempAvatar: '', 
				userInfo: {},
				nicknameOpen: false,
				nicknameForm: {
					nickname: ''
				},
				nicknameRules: {
					nickname: [{
						required: true,
						message: '请输入昵称'
					}]
				}
			}
		},
		computed: {
			curUserInfo(){
				return this.$store.state.userInfo
			}
		},
		watch: {
			curUserInfo(curUserInfo){
				const {avatar, nickname, gender} = curUserInfo;
				this.userInfo = {avatar, nickname, gender,};
			}
		},
		onLoad() {
			const {avatar, nickname, gender, anonymous} = this.curUserInfo;
			this.userInfo = {avatar, nickname, gender};
		},
		methods: {
			nicknameClick() {
				this.nicknameOpen = true;
				this.nicknameForm.nickname = this.userInfo.nickname;
			},
			nicknameCancel() {
				this.nicknameOpen = false;
			},
			nicknameSubmit() {
				this.$refs.nicknameForm.validate().then(() => {
					this.loading = true;
					// 执行登陆
					const { mobile, code, password} = this.form;
					const loginPromise = this.loginType == 'password' ? login(mobile, password) :
						smsLogin(mobile, code);
					loginPromise.then(data => {
						// 登陆成功
						this.loginSuccessCallBack(data);
					}).catch(errors => {
					}).finally(() => {
						this.loading = false;
					})
				}).catch(errors => {
				});
			},
			// 提交修改
			async confirm() {
				// 校验信息是否变化
				const {uploadProgress, userInfo, curUserInfo} = this;
				let isUpdate = false;
				for (let key in userInfo) {
					if(userInfo[key] !== curUserInfo[key]){
						isUpdate = true;
						break;
					}
				}
				if (isUpdate === false) {
					this.$util.msg('信息未修改');
					this.$refs.confirmBtn.stop();
					return;
				}
				if (!userInfo.avatar) {
					this.$util.msg('请上传头像');
					this.$refs.confirmBtn.stop();
					return;
				}
				if (uploadProgress !== 100) {
					this.$util.msg('请等待头像上传完毕');
					this.$refs.confirmBtn.stop();
					return;
				}
				if (!userInfo.nickname) {
					this.$util.msg('请输入您的昵称');
					this.$refs.confirmBtn.stop();
					return;
				}
				const res = await this.$request('user', 'update', userInfo);
				this.$refs.confirmBtn.stop();
				this.$util.msg(res.msg);
				if(res.status === 1){
					this.$store.dispatch('getUserInfo'); //刷新用户信息
					setTimeout(()=>{
						uni.navigateBack();
					}, 1000)
				}
			},
			// 选择头像
			chooseImage(){
				uni.chooseImage({
					count: 1,
					success: res=> {
						uni.navigateTo({
							url: `./cutImage/cut?src=${res.tempFilePaths[0]}` 
						});
					}
				});
			}, 
			// 裁剪回调
			async setAvatar(filePath){
				this.tempAvatar = filePath;
				this.uploadProgress = 0;
				const result = await uniCloud.uploadFile({
					filePath: filePath,
					cloudPath: + new Date() + ('000000' + Math.floor(Math.random() * 999999)).slice(-6) + '.jpg',
					onUploadProgress: e=> {
						this.uploadProgress = Math.round(
							(e.loaded * 100) / e.total
						);
					}
				});
				if(!result.fileID){
					this.$util.msg('头像上传失败');
					return;
				}
				if(typeof uniCloud.getTempFileURL === 'undefined'){
					this.userInfo.avatar = result.fileID;
				}else{
					const tempFiles = await uniCloud.getTempFileURL({
						fileList: [result.fileID]
					})
					const tempFile = tempFiles.fileList[0];
					if(tempFile.download_url || tempFile.fileID){
						this.userInfo.avatar = tempFile.download_url || tempFile.fileID;
					}else{
						this.$util.msg('头像上传失败');
					}
				}
			}
		}
	}
</script>

<style scoped lang="scss">
	.app{
		padding-top: 16rpx;
	}
	.cell{
		display: flex;
		align-items: center;
		min-height: 110rpx;
		padding: 0 40rpx;
		
		&:first-child{
			margin-bottom: 10rpx;
		}
		&:after{
			left: 40rpx;
			right: 40rpx;
			border-color: #d8d8d8;
		}
		.tit{
			font-size: 30rpx;
			color: #333;
		}
		.avatar-wrap{
			width: 120rpx;
			height: 120rpx;
			position: relative;
			border-radius: 100rpx;
			overflow: hidden;
			
			.avatar{
				width: 100%;
				height: 100%;
				border-radius: 100rpx;
			}
			.progress{
				position: absolute;
				left: 50%;
				top: 50%;
				transform: translate(-50%, -50%);
				width: 100rpx;
				height: 100rpx;
				box-shadow: rgba(0,0,0,.6) 0px 0px 0px 2005px;
				border-radius: 100rpx;
				transition: .5s;
				opacity: 0;
				
				&.no-transtion{
					transition: 0s;
				}
				&.show{
					opacity: 1;
				}
			}
		}
		.input{
			flex: 1;
			text-align: right;
			font-size: 28rpx;
			color: #333;
		}
		switch{
			margin: 0;
			transform: scale(0.8) translateX(10rpx);
			transform-origin: center right;
		}
		.tip{
			margin-left: 20rpx;
			font-size: 28rpx;
			color: #999;
		}
		.checkbox{
			padding: 12rpx 0 12rpx 40rpx;
			font-size: 28rpx;
			color: #333;
			
			.mix-icon{
				margin-right: 12rpx;
				font-size: 36rpx;
				color: #ccc;
			}
			.icon-xuanzhong{
				color: $base-color;
			}
		}
	}
</style>
