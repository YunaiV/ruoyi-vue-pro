/**
* BaseCloud APP更新检测组件
v1.0.4
*/
<template>
	<view class="base-cloud" style="display: inline-block;">
		<view class="father" style="display: flex;align-items: center;" v-if="showVersion" @click.stop.prevent="checkVersion">
			<text class="version-text">版本{{version}}</text>
			<view class="abs top right" v-if="updateData.updated" style="top: -3px;right: -7px;">
				<view class="w7 h7 rds redBg"></view>
			</view>
		</view>
		<view class="fixed z20 wp6 flex ct plr50 pb50 " v-if="show">
			<view class="w100p showIn" :animation="inData" style="max-width: 300px;">
				<view class="rds12" :style="{'background-color':color}">
					<view class="father">
						<view class="h120  father hidden">
							<view class="abs top left50p">
								<image src="./static/cloudRight.png" mode="widthFix" class="w500 h120  animated goAway infinite"></image>
							</view>
							<view class="abs top right50p">
								<image src="./static/cloudLeft.png" mode="widthFix" class="w500 h120  animated goAwayLeft infinite"></image>
							</view>
							<image class="abs top30 left30 w10 h10 animated infinite fadeOut slow" src="./static/star.png" mode="widthFix"></image>
							<image class="abs top60 left80 w10 h10  animated infinite fadeOut slowest delay-1s" src="./static/star.png" mode="widthFix"></image>
							<image class="abs top20 right20 w10 h10  animated infinite fadeOut slower delay-2s" src="./static/star.png" mode="widthFix"></image>
							<image class="abs top20 right50 w30 h30 animated fadeOutRight infinite slowest" src="./static/smallCloud.png"
							 mode="widthFix"></image>
							<image class="abs top30 left50 w30 h30 animated fadeOutRight infinite slow8 " src="./static/smallCloud.png" mode="widthFix"></image>
						</view>
						<view class="abs bottom animated bounceUp infinite">
							<view class="animated goUp delay-06s">
								<image src="./static/airship.png" mode="widthFix" class="w80 h75 center-block father z3"></image>
								<view class="father" style="top: -5px;">
									<image src="./static/shipAir.png" mode="widthFix" class="w40 h85 center-block  animated infinite splashOut"></image>
									<view class="abs">
										<image src="./static/shipAir.png" mode="widthFix" class="w40 h85 center-block animated infinite splashOut delay-03s"></image>
									</view>
									<view class="abs">
										<image src="./static/shipAir.png" mode="widthFix" class="w40 h85 center-block animated infinite splashOut delay-06s"></image>
									</view>
									<view class="abs bottom" style="bottom: -80upx;">
										<image src="./static/shipGas.png" mode="widthFix" class="w40 h85 center-block animated infinite splash"></image>
									</view>
								</view>
							</view>
						</view>
					</view>

					<view class=" whiteBg hidden plr20 father z3 rdsBr12 rdsBl12" :class="{'pb100':progress <= 0 || progress >= 100 || completed}">
						<view class="ptb10 fz16 bold">
							<block v-if="progress == 0">
								{{title}} {{ updateData.version ? 'v' + updateData.version : ''}}
							</block>
							<block v-else-if="progress <=100 && !completed">
								<view>
									<view class="fz30 normal avanti pt15 text-center">
										{{ progress }}
										<text class="fz12 ml2">
											%
										</text>
									</view>

									<view class="text-center pb40 op8 gray fz14 normal">
										版本更新中，不要离开...
									</view>
								</view>
							</block>
							<view class="text-center pt15" v-else-if="completed">
								版本升级成功
								<view class="pb40 op8 gray fz14 normal pt5">
									更新已完成，请重启应用体验新版
								</view>
							</view>
						</view>
						<scroll-view scroll-y="true" class="scroll-view h60 autoY pb20" v-if="progress == 0">
							<view class="column">
								<text v-if="updateData.description.length === 0">
									{{ defaultContent }}
								</text>
								<text  v-for="(item, index) in updateData.description" :key="index">
									{{ index + 1 }}.{{ item }}
								</text>
							</view>
						</scroll-view>
						<view class="pd50 pt25" v-else-if="progress < 100">
							<view class="grayBg bd rds23">
								<view class="grayBg rds23">
									<view class="ptb3 rds23" :style="{width:progress+'%','background-color':color}"></view>
								</view>
							</view>
						</view>
						<view class="father">
							<view class="abs top left50p  roundBox rds text-center" :style="{'background-color':color}">
								<view class="pt30" v-if="!completed">
									<button hover-class="op9" @tap.stop="download" class="btn bd2 whiteBg line rds23 inline plr50 ptb10 fz16">
										立即升级
									</button>
								</view>
								<view class="pt30" v-else>
									<button hover-class="op8" @tap.stop="restart" class="btn bd2 whiteBg line rds23 inline plr50 ptb10 fz16">
										立即重启
									</button>
								</view>
							</view>
						</view>
					</view>
				</view>
				<view class="op9 father" v-if="progress == 0">
					<view class="abs">
						<view class="flex ct ">
							<view class="h30 bl3 whiteBd"></view>
						</view>
						<view class="close-btn" @click="hide">
							<text class="mix-icon icon-close"></text>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<mix-loading v-if="isLoading"></mix-loading>
	</view>
</template>

<script>
	export default {
		name: "version-update",
		props: {
			title: {
				default: "发现新版本"
			},
			defaultContent: {
				default: "快来升级，体验最新的功能吧！"
			},
			showVersion: {
				default: true
			},
			autoShow: {
				default: false
			},
			isCache: {
				default: true
			},
			updateUrl: {
				default: "api/base-app-version"
			},
			color: {
				default: "#ff536f"
			}
		},
		data() {
			return {
				show: false,
				version: "1.0.0",
				updateData: {
					description: []
				},
				progress: 0,
				completed: false,
				inData: null
			};
		},
		created() {
			// #ifdef APP-PLUS
			plus.runtime.getProperty(plus.runtime.appid, (widgetInfo) => {
				this.version = widgetInfo.version;
			});
			// #endif
		},
		methods: {
			async checkVersion(e) {
				console.log(e);
				const res = await this.$request('version', 'check', {
					version: this.version
				}, {
					showLoading: true
				})
				if (res.status === 0) {
					this.$util.msg(res.msg);
				} else {
					res.data.description = res.data.description.split(';');
					this.updateData = res.data;
					this.show = true;
				}
			},

			download(e) {
				if(this.isDownloading){
					return;
				}
				this.isDownloading = true;
				const task = uni.downloadFile({
					url: this.updateData.downloadLink,
					success: (downloadResult) => {
						this.isDownloading =false;
						uni.hideLoading();
						if (downloadResult.statusCode === 200) {
							plus.runtime.install(downloadResult.tempFilePath, {
								force: false
							}, (e) => {
								this.downloadSuccess(e);
							}, (err) => {
								this.downloadError(err);
							});
						}
					},
					fail: err => {
						this.isDownloading =false;
						this.downloadError(err);
					}
				});

				task.onProgressUpdate((e) => {
					console.log(e.progress);
					this.progress = e.progress;
				});
			},
			downloadError(e) {
				this.show = false;
				this.progress = 0;
				uni.showModal({
					title: '提示',
					content: '更新失败，请稍后再试',
					showCancel: false,
					confirmColor: '#414cd9'
				});
			},
			downloadSuccess(e) {
				this.completed = true;
			},
			restart(e) {
				this.show = false;
				this.completed = false;
				this.progress = 0;
				// #ifdef APP-PLUS
				plus.runtime.restart();
				// #endif
			},
			hide(e) {
				var animation = uni.createAnimation({
					duration: 300,
					timingFunction: 'ease',
				});
				animation.scale(0).opacity(0).step();
				this.inData = animation.export();
				setTimeout((e) => {
					this.show = false;
					this.inData = null;
				}, 420);
			},

		},
	}
</script>

<style scoped lang="scss">
	.version-text{
		margin-right: 10rpx;
		font-size: 26rpx;
		color: #999;
		position: relative;
		z-index: -2rpx;
	}
	.close-btn{
		display: flex;
		justify-content: center;
		color: #fff;
		
		.mix-icon{
			margin-top: -20rpx;
			padding: 20rpx;
			font-size: 48rpx;
		}
	}
	.scroll-view{
		height: auto !important;
		min-height: 120rpx;
		max-height: 17vh;
		
		text{
			margin-bottom: 16rpx;
			line-height: 1.5;
			font-size: 28rpx;
		}
	}
	.roundBox {
		width: 5000upx;
		height: 5000upx;
		margin-left: -2500upx;
	}

	.animated {
		-webkit-animation-duration: 1s;
		animation-duration: 1s;
		-webkit-animation-fill-mode: both;
		animation-fill-mode: both;
		animation-timing-function: linear;
	}

	.animated.infinite {
		-webkit-animation-iteration-count: infinite;
		animation-iteration-count: infinite;
	}

	@keyframes goUp {
		from {
			-webkit-transform: translate3d(0, 70%, 0);
			transform: translate3d(0, 70%, 0);
		}

		to {
			-webkit-transform: translate3d(0, 0, 0);
			transform: translate3d(0, 0, 0);
		}
	}

	.goUp {
		-webkit-animation-name: goUp;
		animation-name: goUp;
		-webkit-animation-duration: 600ms;
		animation-duration: 600ms;
		animation-timing-function: ease-in;
	}

	@keyframes splash {

		0,
		100% {
			transform: scaleX(0.9);
		}

		5%,
		95% {
			transform: scaleX(1.02);
		}

		10%,
		80% {
			transform: scaleX(1.05);
		}

		25%,
		75% {
			transform: scaleX(1.08);
		}

		50% {
			transform: scaleX(1.1);
		}
	}

	.splash {
		-webkit-animation-name: splash;
		animation-name: splash;
		-webkit-animation-duration: 0.6s;
		animation-duration: 0.6s;
		animation-timing-function: linear;
	}

	@-webkit-keyframes splashOut {
		from {
			opacity: 1;
			transform: scaleX(0);
		}

		to {
			opacity: 0;
			transform: scaleX(2);
		}
	}

	.splashOut {
		-webkit-animation-name: splashOut;
		animation-name: splashOut;
		-webkit-animation-duration: 1s;
		animation-duration: 1s;
	}

	@keyframes bounceUp {
		0% {
			transform: translate3d(0, 0, 0);
		}

		50% {
			transform: translate3d(0, -30rpx, 0);
		}
	}

	.bounceUp {
		-webkit-animation-name: bounceUp;
		animation-name: bounceUp;
		-webkit-animation-duration: 1.6s;
		animation-duration: 1.6s;
		animation-timing-function: linear;
	}

	@keyframes fadeOut {

		0,
		100% {
			opacity: 1;
		}

		50% {
			opacity: 0;
		}
	}

	.fadeOut {
		-webkit-animation-name: fadeOut;
		animation-name: fadeOut;
	}

	@keyframes fadeOutRight {
		0% {
			opacity: 0;
			transform: translate3d(-200%, 0, 0);
		}

		50% {
			opacity: 1;
			transform: translate3d(0, 0, 0);
		}

		100% {
			opacity: 0;
			transform: translate3d(200%, 0, 0);
		}
	}

	.fadeOutRight {
		-webkit-animation-name: fadeOutRight;
		animation-name: fadeOutRight;
	}

	.animated.delay-03s {
		-webkit-animation-delay: 0.3s;
		animation-delay: 0.3s;
	}

	.animated.delay-06s {
		-webkit-animation-delay: 0.6s;
		animation-delay: 0.6s;
	}

	.animated.delay-1s {
		-webkit-animation-delay: 1s;
		animation-delay: 1s;
	}

	.animated.delay-2s {
		-webkit-animation-delay: 2s;
		animation-delay: 2s;
	}

	.animated.delay-3s {
		-webkit-animation-delay: 3s;
		animation-delay: 3s;
	}

	.animated.fast {
		-webkit-animation-duration: 800ms;
		animation-duration: 800ms;
	}

	.animated.faster {
		-webkit-animation-duration: 500ms;
		animation-duration: 500ms;
	}

	.animated.fastest {
		-webkit-animation-duration: 200ms;
		animation-duration: 200ms;
	}

	.animated.slow {
		-webkit-animation-duration: 2s;
		animation-duration: 2s;
	}

	.animated.slower {
		-webkit-animation-duration: 3s;
		animation-duration: 3s;
	}

	.animated.slowest {
		-webkit-animation-duration: 10s;
		animation-duration: 10s;
	}

	.animated.slow4 {
		-webkit-animation-duration: 5s;
		animation-duration: 5s;
	}

	.animated.slow5 {
		-webkit-animation-duration: 5s;
		animation-duration: 5s;
	}

	.animated.slow8 {
		-webkit-animation-duration: 8s;
		animation-duration: 8s;
	}

	.goAway {
		transform: translate3d(-50%, 10%, 0);
		-webkit-animation-name: goAway;
		animation-name: goAway;
		-webkit-animation-duration: 2s;
		animation-duration: 2s;
	}

	@keyframes goAway {
		from {
			transform: translate3d(-50%, 10%, 0);
		}

		to {
			transform: translate3d(-1.3%, -17.6%, 0);
		}
	}

	.goAwayLeft {
		transform: translate3d(50%, 10%, 0);
		-webkit-animation-name: goAwayLeft;
		animation-name: goAwayLeft;
		-webkit-animation-duration: 2s;
		animation-duration: 2s;
	}

	@keyframes goAwayLeft {
		from {
			transform: translate3d(50%, 10%, 0);
		}

		to {
			transform: translate3d(2%, -17%, 0);
		}
	}

	@keyframes showIn {

		0% {
			opacity: 0;
			transform: scale3d(0.5, 0.5, 0.5);
		}

		100% {
			opacity: 1;
			transform: scale3d(1, 1, 1);
		}
	}

	.showIn {
		animation-duration: 0.4s;
		animation-name: showIn;
	}
	
	div,a,img,span,page,view,navigator,image,text,input,textarea,button,form{
		box-sizing: border-box;
	}
	
	a{
		text-decoration: none;
		color: $main;
	}
	
	form{
		display: block;
		width: 100%;
	}
	
	image{will-change: transform}
	
	input,textarea,form{
		width: 100%;
		height: auto;
		min-height: 10px;
		display: block;
		font-size: inherit;
	}
	
	button{
		color: inherit;
		line-height: inherit;
		margin: 0;
		background-color: transparent;
		padding: 0;
		-webkit-border-radius: 0;
		-moz-border-radius: 0;
		border-radius: 0;
		&:after{
			display: none;
		}
	}
	
	switch .uni-switch-input{
		margin-right: 0;
	}
	.wx-switch-input,.uni-switch-input{width:42px !important;height:22px !important;}
		.wx-switch-input::before,.uni-switch-input::before{width:40px !important;height: 20px !important;}
		.wx-switch-input::after,.uni-switch-input::after{width: 20px !important;height: 20px !important;}
	
	
	/**背景颜色**/
	.bg{
		background-color: $main;
		color: $mainInverse;
	}
	.gradualBg{
		background-image: $mainGradual;
		color: $mainGradualInverse ;
	}
	.grayBg{
		background-color: #f7f7f7;
		color: #30302f;
	}
	.whiteBg{
		background-color: #fff;
		color: #000;
	}
	.blackBg{
		background-color: #000;
		color: #fff;
	}
	.orangeBg{
		background-color: $orange;
		color: #fff;
	}
	.redBg{
		background-color: $red;
		color: #fff;
	}
	.yellowBg{
		background-color: $yellow;
		color: #000;
	}
	.greenBg{
		background-color: $green;
		color: #fff;
	}
	.brownBg{
		background-color: $brown;
		color: #fff;
	}
	.blueBg{
		background-color: $blue;
		color: #fff;
	}
	.purpleBg{
		background-color: $purple;
		color: #fff;
	}
	
	/* 文字颜色 */
	.main{
		color: $main;
	}
	.green{
		color: $green;
	}
	.red{
		color: $red;
	}
	.yellow{
		color: $yellow;
	}
	.black{
		color: $black;
	}
	.white{
		color: $white;
	}
	.gray{
		color: $gray;
	}
	.grey{
		color: $grey;
	}
	.orange{
		color: $orange;
	}
	.brown{
		color: $brown;
	}
	.blue{
		color: $blue;
	}
	.purple{
		color: $purple;
	}
	
	.hoverMain{
		&:hover{
			color: $main;
		}
	}
	
	.hoverGreen{
		&:hover{
			color: $green;
		}
	}
	
	.hoverRed{
		&:hover{
			color: $red;
		}
	}
	
	.hoverBlue{
		&:hover{
			color: $blue;
		}
	}
	
	.hoverGray{
		&:hover{
			color: $gray;
		}
	}
	
	.hoverWhite{
		&:hover{
			color: $white;
		}
	}
	
	.hoverBlack{
		&:hover{
			color: $black;
		}
	}
	
	.hoverOrange{
		&:hover{
			color: $orange;
		}
	}
	
	.hoverYellow{
		&:hover{
			color: $yellow;
		}
	}
	
	.hoverBrown{
		&:hover{
			color: $brown;
		}
	}
	
	.hoverPurple{
		&:hover{
			color: $purple;
		}
	}
	
	/* 宽度 高度 */
	$w:0;
	@while $w <= 500 {
		@if $w <= 100 {
			.w#{$w}p{
				width: $w*1%;
			}
			.h#{$w}p{
				height: $w*1%;
			}
			@if $w == 100 {
				.100p{
					width: 100%;
					height: 100%;
				}
				.ww{
					width: 100vw;
				}
				.hh{
					height: 100vh;
				}
			}
		}
		.w#{$w}{
			width: $w*2upx;
		}
		.h#{$w}{
			height: $w*2upx;
		}
		@if $w < 10 {
			$w : $w + 1 ;
		} @else{
			$w : $w + 5 ;
		}
	}
	
	
	/* 字号 */
	@for $fz from 12 through 100 {
		.fz#{$fz}{
			font-size: $fz*2upx !important;
		}
	}
	
	/* 边距 - 覆盖顺序是小的尺寸覆盖大的尺寸 少的方向覆盖多的方向 */
	$s : 0 ;
	@while $s <= 500 {
		.pd#{$s}{
			padding: $s*2upx!important;
		}
		.m#{$s}{
			margin: $s*2upx!important;
		}
		@if $s == 15 {
			.pd{
				padding: 30upx!important;
			}
			.m{
				margin: 30upx!important;
			}
		}
		@if $s < 10 {
			$s : $s + 1 ;
		} @else if($s < 100){
			$s : $s + 5 ;
		}   @else if($s < 300){
			$s : $s + 10 ;
		} @else{
			$s : $s + 50 ;
		}
	}
	
	$s : 0 ;
	@while $s <= 500 {
		.ptb#{$s}{
			padding-top: $s*2upx!important;
			padding-bottom: $s*2upx!important;
		}
		.plr#{$s}{
			padding-left: $s*2upx!important;
			padding-right: $s*2upx!important;
		}
		.mtb#{$s}{
			margin-top: $s*2upx!important;
			margin-bottom: $s*2upx!important;
		}
		.mlr#{$s}{
			margin-left: $s*2upx!important;
			margin-right: $s*2upx!important;
		}
		@if $s == 15 {
			.ptb{
				padding-top: 30upx!important;
				padding-bottom: 30upx!important;
			}
			.plr{
				padding-left: 30upx!important;
				padding-right: 30upx!important;
			}
			
			.mlr{
				margin-left: 30upx!important;
				margin-right: 30upx!important;
			}
			.mtb{
				margin-top: 30upx!important;
				margin-bottom: 30upx!important;
			}
		}
		@if $s < 10 {
			$s : $s + 1 ;
		} @else if($s < 100){
			$s : $s + 5 ;
		}   @else if($s < 300){
			$s : $s + 10 ;
		} @else{
			$s : $s + 50 ;
		}
	}
	
	$s : 0 ;
	@while $s <= 500 {
		.pl#{$s}{
			padding-left: $s*2upx!important;
		}
		.pr#{$s}{
			padding-right: $s*2upx!important;
		}
		.pt#{$s}{
			padding-top: $s*2upx!important;
		}
		.pb#{$s}{
			padding-bottom: $s*2upx!important;
		}
		.ml#{$s}{
			margin-left: $s*2upx!important;
		}
		.mr#{$s}{
			margin-right: $s*2upx!important;
		}
		.mt#{$s}{
			margin-top: $s*2upx!important;
		}
		.mb#{$s}{
			margin-bottom: $s*2upx!important;
		}
		@if $s == 15 {
			.pt{
				padding-top: 30upx!important;
			}
			.pb{
				padding-bottom: 30upx!important;
			}
			.pl{
				padding-left: 30upx!important;
			}
			.pr{
				padding-right: 30upx!important;
			}
			.mt{
				margin-top: 30upx!important;
			}
			.mr{
				margin-right: 30upx!important;
			}
			.ml{
				margin-left: 30upx!important;
			}
			.mb{
				margin-bottom: 30upx!important;
			}
		}
		@if $s < 10 {
			$s : $s + 1 ;
		} @else if($s < 100){
			$s : $s + 5 ;
		} @else if($s < 300){
			$s : $s + 10 ;
		} @else{
			$s : $s + 50 ;
		}
	}
	
	
	
	/* 文字溢出隐藏 */
	.clip{
		width: 100%;
		display: -webkit-box;
		overflow: hidden;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
		@for $i from 2 through 10{
			&.c#{$i}{
				-webkit-line-clamp: $i;
			}
		}
	}
	
	.cut{
		display: block;
		width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	/* 价格 */
	.price{
		font-size: inherit ;
		&:before{
			content: "￥";
			font-size: 70%;
			color: inherit;
			font-weight: normal;
			font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif ;
		}
		.point{
			display: inline-block;
			font-size: 70%;
			font-weight: inherit;
			letter-spacing: 1px;
			color: inherit;
		}
		&.noPrefix{
			&:before{
				content: '';
			}
		}
	}
	
	/* 布局 */
	.grid,.gridNoPd,.gridSmPd,.gridNoMb{
		display: -webkit-box;
		display: -webkit-flex;
		display: -ms-flexbox;
		display: flex;
		-webkit-flex-wrap: wrap;
		-ms-flex-wrap: wrap;
		flex-wrap: wrap;
		width: 100%;
		padding: 20upx 20upx 0 20upx;
		>.item,>image,>view,>navigator,>text,>button{
			width: 50%;
			padding: 0 10upx;
			margin-bottom: 20upx;
		}
		@for $i from 1 through 50{
			&.g#{$i}{
				>.item,>image,>view,>navigator,>text,>button{
					width: 100%/$i;
				}
			}
		}
	}
	
	.gridNoMb{
		>.item,>image,>view,>navigator,>text,>button{
			margin-bottom: 0;
		}
	}
	
	.gridNoPd{
		padding: 0;
		>.item,>image,>view,>navigator,>text,>button{
			padding: 0;
			margin-bottom: 0;
		}
	}
	.gridSmPd{
		padding: 0;
		>.item,>image,>view,>navigator,>text,>button{
			padding-right: 0;
			&:first-child{
				padding-left: 0;
				padding-right: 10upx;
			}
		}
	}
	
	/* flex布局 */
	.flex{
		display: flex;
		align-items: center;
		justify-content: space-between;
		&.t{
			align-items: flex-start;
		}
		&.b{
			align-items: flex-end;
		}
		&.cv{ //垂直方向铺满
			align-items: stretch;
		}
		&.bk{ //水平方向铺满
			flex-direction: column;
		}
		&.lt{
			justify-content: flex-start;
		}
		&.ct{
			justify-content: center;
		}
		&.rt{
			justify-content: flex-end;
		}
		&.ar{
			justify-content: space-around;
		}
		&.av{
			>.item,view,button,navigator,image,text{
				flex:1;
			}
		}
	}
	
	/* 定位布局 */
	.father{
		position: relative;
	}
	.abs,.fixed{
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1;
		&image{
			width: 100%;
			height: 100%;
		}
		&.top{
			bottom: auto;
		}
		&.bottom{
			top: auto;
		}
		&.left{
			right: auto;
		}
		&.right{
			left: auto;
		}
	}
	@for $i from 0 through 20 {
		.z#{$i}{
			z-index: $i !important;
		}
	}
	
	@for $i from 1 through 200 {
		.top-#{$i}{
			bottom: auto;
			top: $i * -2upx;
		}
		.left-#{$i}{
			right: auto;
			left: $i *  -2upx;
		}
		.bottom-#{$i}{
			top: auto;
			bottom: $i *  -2upx;
		}
		.right-#{$i}{
			left: auto;
			right: $i *  -2upx;
		}
		.top#{$i}{
			bottom: auto;
			top: $i * 2upx;
		}
		.left#{$i}{
			right: auto;
			left: $i *  2upx;
		}
		.bottom#{$i}{
			top: auto;
			bottom: $i *  2upx;
		}
		.right#{$i}{
			left: auto;
			right: $i *  2upx;
		}
		.top-#{$i}p{
			bottom: auto;
			top: $i * -1%;
		}
		.left-#{$i}p{
			right: auto;
			left: $i * -1%;
		}
		.bottom-#{$i}p{
			top: auto;
			bottom: $i * -1%;
		}
		.right-#{$i}p{
			left: auto;
			right: $i * -1%;
		}
		.top#{$i}p{
			bottom: auto;
			top: $i * 1%;
		}
		.left#{$i}p{
			right: auto;
			left: $i * 1%;
		}
		.bottom#{$i}p{
			top: auto;
			bottom: $i * 1%;
		}
		.right#{$i}p{
			left: auto;
			right: $i * 1%;
		}
	}
	
	.fixed{
		position: fixed;
	}
	
	/* fix-auto布局 */
	.fixAuto,.fixAutoNoPd,.fixAutoSmPd{
		display: table;
		width: 100%;
		padding: 20upx 10upx;
		>.item,>view,>image,>navigator,>text,>button{
			vertical-align: top;
			padding: 0 10upx;
			display: table-cell ;
		}
		&.middle{
			>.item,>view,>image,>navigator,>text{
				vertical-align: middle;
			}
		}
		&.bottom{
			>.item,>view,>image,>navigator,>text{
				vertical-align: bottom;
			}
		}
	}
	.fixAutoSmPd{
		padding: 0;
		>.item,>view,>image,>navigator,>text{
			padding-right: 0;
			&:first-child{
				padding-left: 0;
				padding-right: 10upx;
			}
		}
	}
	.fixAutoNoPd{
		padding: 0;
		>.item,>view,>image,>navigator,>text{
			padding: 0;
		}
	}
	
	/* 浮动组件 */
	.clear{
		&:after{
			content: "";
			clear: both;
			height: 0;
			display: block;
			visibility: hidden;
		}
	}
	.fl{
		float: left;
	}
	.fr{
		float: right;
	}
	
	/* 按钮样式类 */
	.btn,.roundBtn{
		cursor: pointer;
		display: inline-block;
		text-align: center;
		padding: 8upx 24upx;
		background-color: $main;
		color: $mainInverse ;
		font-size: 28upx;
		border: 1px solid $main;
		-webkit-border-radius: 8upx;
		-moz-border-radius: 8upx;
		border-radius: 8upx;
		transition: 0.4s;
		white-space: nowrap;
		text-overflow: ellipsis;
		&.gradualBg{
			border-color: transparent;
			background-image: $mainGradual;
			color: $mainGradualInverse;
			
		}
		&.blackBg{
			background-color: $black;
			border-color: $black;
			color: #fff;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba($black , 0.4);
			}
		}
		&.greenBg{
			background-color: $green;
			border-color: $green;
			color: #fff;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba($green , 0.4);
			}
		}
		&.grayBg{
			border-color: rgba(#30302f,0.2);
			background-color: #f7f7f7;
			color: #30302f;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba( #30302f , 0.2);
			}
		}
		&.whiteBg{
			border-color: rgba(#fff,0.2);
			background-color: #fff;
			color: #000;
		}
		
		&.orangeBg{
			border-color: $orange;
			background-color: $orange;
			color: #fff;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba( $orange , 0.4);
			}
		}
		&.redBg{
			border-color: $red;
			background-color: $red;
			color: #fff;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba( $red , 0.4);
			}
		}
		&.yellowBg{
			border-color: $yellow;
			background-color: $yellow;
			color: #000;
			&.shadow{
				box-shadow: 0px 2px 9px -1px rgba( $yellow , 0.4);
			}
		}
		
		&.line{
			background-color: transparent;
			background-image: none;
			color: $main;
			&.blackBg{
				color: $black;
			}
			&.greenBg{
				color: $green;
			}
			&.yellowBg{
				color: $yellow;
			}
			&.grayBg{
				color: #30302f;
			}
			&.whiteBg{
				border-color:  rgba(#fff,0.7);
				color: #fff;
			}
			&.orangeBg{
				color: $orange;
			}
			&.redBg{
				color: $red;
			}
		}
		&+.btn,&+.roundBtn{
			margin-left: 20upx;
		}
		&.block{
		   margin: 0;
		   padding: 20upx 24upx;
		   display: block;
		   width: 100%;
		   &+.btn{
			   margin-left: 0;
		   }
		}
		&:hover{
			-webkit-transform: scale(0.99);
			-moz-transform: scale(0.99);
			-ms-transform: scale(0.99);
			-o-transform: scale(0.99);
			transform: scale(0.99);
			opacity: 0.8;
		}
		&.disabled{
			-webkit-transform: scale(1);
			-moz-transform: scale(1);
			-ms-transform: scale(1);
			-o-transform: scale(1);
			transform: scale(1);
			opacity: 0.4;
			cursor: not-allowed;
		}
	}
	
	[class^="tag"] , [class*=" tag"]{
		display: inline-block;
		font-size: 24upx;
		padding: 4upx 14upx;
		border-radius: 4upx;
		margin-right: 6upx;
		margin-left: 6upx;
	}
	.tag{
		background-color: rgba($main,0.2);
		color: $main;
	}
	.tagBlue{
		background-color: rgba($blue,0.2);
		color: $blue;
	}
	.tagGray{
		background-color: rgba($gray,0.2);
		color: $gray;
	}
	
	.tagGradual{
		background-image: linear-gradient(to top right,rgba($main,0.2),rgba($main,0.1));
		color: $main;
	}
	
	.tagBlack{
		background-color: rgba($black,0.2);
		color: $black;
	}
	.tagGreen{
		background-color: rgba($green,0.2);
		color: $green;
	}
	
	.tagWhite{
		background-color: rgba($white,0.2);
		color: $white;
	}
	
	.tagOrange{
		background-color: rgba($orange,0.2);
		color: $orange;
	}
	.tagRed{
		background-color: rgba($red,0.2);
		color: $red;
	}
	.tagYellow{
		background-color: rgba($yellow,0.2);
		color: $yellow;
	}
	
	/* 边线(实线、虚线) */
	.bdn{
		border: none;
	}
	.bd{
		border: 1px solid $borderColor;
		&.dashed{
			border-style: dashed;
		}
	}
	.bt{
		border-top:1px solid $borderColor;
		&.dashed{
			border-top-style: dashed;
		}
	}
	.bb{
		border-bottom:1px solid $borderColor;
		&.dashed{
			border-bottom-style: dashed;
		}
	}
	.bl{
		border-left:1px solid $borderColor;
		&.dashed{
			border-left-style: dashed;
		}
	}
	.br{
		border-right: 1px solid $borderColor;
		&.dashed{
			border-right-style: dashed;
		}
	}
	
	$b:2;
	@while $b <= 10 {
		.bd#{$b}{
			border: #{$b}px solid $borderColor;
			&.dashed{
				border-style: dashed;
			}
		}
		.bt#{$b}{
			border-top:#{$b}px solid $borderColor;
			&.dashed{
				border-top-style: dashed;
			}
		}
		.bb#{$b}{
			border-bottom:#{$b}px solid $borderColor;
			&.dashed{
				border-bottom-style: dashed;
			}
		}
		.bl#{$b}{
			border-left:#{$b}px solid $borderColor;
			&.dashed{
				border-left-style: dashed;
			}
		}
		.br#{$b}{
			border-right: #{$b}px solid $borderColor;
			&.dashed{
				border-right-style: dashed;
			}
		}
		$b : $b + 1 ;
	}
	
	/* 边线颜色 */
	.mainBd{
		border-color: $main;
	}
	.greenBd{
		border-color:  $green;
	}
	.redBd{
		border-color: $red;
	}
	.yellowBd{
		border-color:$yellow ;
	}
	.blackBd{
		border-color: $black;
	}
	.whiteBd{
		border-color:$white ;
	}
	.grayBd{
		border-color:$gray;
	}
	.greyBd{
		border-color:$grey;
	}
	.orangeBd{
		border-color:$orange;
	}
	
	/* 圆角 */
	.radius,.rds{
		-webkit-border-radius: 100%!important;
		-moz-border-radius: 100%!important;
		border-radius: 100%!important;
	}
	
	$r:0;
	@while $r <= 50{
		.rds#{$r},&.radius#{$r}{
			-webkit-border-radius:$r*2upx!important;
			-moz-border-radius:$r*2upx!important;
			border-radius:$r*2upx!important;
		}
		$r : $r + 1;
	}
	
	.rdsTl,.radiusTopLeft{
		border-top-left-radius:100%!important;
	}
	.rdsTr,.radiusTopRight{
		border-top-right-radius: 100%!important;
	}
	.rdsBl,.radiusBottomLeft{
		border-bottom-left-radius: 100%!important;
	}
	.rdsBr,.radiusBottomRight{
		border-bottom-right-radius: 100%!important;
	}
	
	$r:0;
	@while $r <= 50{
		.rdsTl#{$r},.radiusTopLeft#{$r}{
			border-top-left-radius: $r*2upx!important;
		}
		.rdsTr#{$r},.radiusTopRight#{$r}{
			border-top-right-radius: $r*2upx!important;
		}
		.rdsBl#{$r},.radiusBottomLeft#{$r}{
			border-bottom-left-radius: $r*2upx!important;
		}
		.rdsBr#{$r},.radiusBottomRight#{$r}{
			border-bottom-right-radius: $r*2upx!important;
		}
		$r : $r + 1;
	}
	
	/* 正方形&长方形 */
	[class^="square"] , [class*=" square"]{
		width: 100%;
		position: relative;
		height: auto;
		>.item,>image,>view,>navigator,>text,>button{
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
		}
	}
	
	$p : 200 ;
	@while $p > 0 {
		.square#{$p}{
			padding-top: $p*1%;
		}
		@if $p == 100 {
			.square{
				padding-top: 100%;
			}
		}
		$p : $p - 5 ;
	}
	
	
	
	/* 阴影 */
	.shadow{
		box-shadow: 0px 2px 9px -1px rgba(0, 0, 0, 0.1);
	}
	
	/* 遮罩层 */
	.wrapper-top,.wt{
		background-image: linear-gradient(rgba(0,0,0,0.3) , rgba(0,0,0,0.02));
	}
	.wrapper-bottom,.wb{
		background-image: linear-gradient( rgba(0,0,0,0.02) , rgba(0,0,0,0.3) );
	}
	
	[class^="wp"],[class*=" wp"] {
		z-index: 10;
	}
	
	/* 透明度 */
	@for $op from 0 through 10 {
		.op#{$op}{
			opacity: $op * 0.1!important;
		}
		.wp#{$op}{
			background-color: rgba(#000,$op*0.1);
		}
		@if $op == 5 {
			.wp{
				background-color: rgba(#000,0.5);
			}
		}
	}
	
	/* 分割线 */
	[class*=" split"],[class^="split"] {
		position: relative;
		&:before{
			content:"";
			display: block;
			position: absolute;
			left: 0;
			top: 50%;
			border-left: 1px solid $borderColor;
		}
	}
	
	$s:10;
	@while $s <= 100 {
		.split#{$s}{
			&:before{
				height: #{$s*2}upx;
				margin-top: -#{$s}upx;
			}
		}
		@if $s == 10 {
			.split{
				&:before{
					height: 20upx;
					margin-top: -10upx;
				}
			}
		}
		$s:$s+2;
	}
	
	.hover,[class^="hover"],[class*=" hover"]{
		transition: all 0.4s;
		cursor: pointer;
		&:hover{
			opacity: 0.8 !important;
		}
	}
	
	
	
	.statusBar{
		height: var(--status-bar-height);
	}
	
	.winBottom{
		height: var(--windown-bottom);
	}
	
	.safeBottom{
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
	}
	
	.disabled{
		opacity:0.8;
		cursor: not-allowed;
	}
	
	
	
	.grid,.gridNoMb,.gridNoPd{
		>.btn,>.roundBtn{
			&+.btn,&+.roundBtn{
				margin-left: 0 ;
			}
		}
	}
	
	.roundBtn{
		-webkit-border-radius: 100upx;
		-moz-border-radius: 100upx;
		border-radius: 100upx;
	} 
	 
	 
	 
	 /* 位置 */
	 .text-center,.tc{
	 	text-align: center!important;
	 }
	 .text-left,.tl{
	 	text-align: left!important;
	 }
	 .text-right,.tr{
	 	text-align: right!important;
	 }
	 .text-justify,.tj{
	 	text-align: justify!important;
	 }
	 .text-bold,.bold{
	 	font-weight: bold!important;
	 }
	 .text-normal,.normal{
	 	font-weight: normal!important;
	 }
	 .break{
	 	white-space: normal;
	 	word-break: break-all;
	 }
	 .noBreak{
	 	white-space: nowrap;
	 	word-break: keep-all;
	 }
	 .inline{
	 	display: inline-block;
	 }
	 .block{
	 	display: block;
	 	width: 100%;
	 }
	 .none{
	 	display: none;
	 }
	 .center-block{
	 	margin: 0 auto;
	 	display: block;
	 }
	 .hidden{
	 	overflow: hidden;
	 }
	 .hiddenX{
	 	overflow-x: hidden;
	 }
	 .hiddenY{
	 	overflow-y: hidden;
	 }
	 .auto{
	 	overflow: auto;
	 }
	 .autoX{
	 	overflow-x: auto;
	 }
	 .autoY{
	 	overflow-y: auto;
	 }
	 .showInMb{
		 display: block;
	 }
	 .showInPc{
		 display: none;
	 }
	 table{
	 	width: 100%;
	 	border-collapse: collapse;
	 	border-spacing: 0;
	 	border: 1px solid #e6e6e6;
	 	thead{
	 		tr{
	 			background-color: #f2f2f2;
	 			th{
	 				color: #8799a3;
	 				width: 1%;
	 			}
	 		}
	 	}
	 	tr{
	 		background-color: #fff;
	 		transition: all 0.4s;
	 		td,th{
	 			border: 1px solid #e6e6e6;
	 			overflow: hidden;
	 			-o-text-overflow: ellipsis;
	 			text-overflow: ellipsis;
	 			white-space: nowrap;
	 			word-wrap: break-word;
	 			padding: 5px 10px;
	 			height: 28px;
	 			line-height: 28px;
	 			&.autoWidth{
	 				width: auto;
	 			}
	 		}
	 		&:hover{
	 			background-color: #f2f2f2;
	 		}
	 	}
	 }
</style>
