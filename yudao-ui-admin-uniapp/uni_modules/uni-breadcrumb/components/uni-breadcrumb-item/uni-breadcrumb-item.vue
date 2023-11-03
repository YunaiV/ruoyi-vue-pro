<template>
	<view class="uni-breadcrumb-item">
		<view :class="{
			'uni-breadcrumb-item--slot': true,
			'uni-breadcrumb-item--slot-link': to && currentPage !== to
			}" @click="navTo">
			<slot />
		</view>
		<i v-if="separatorClass" class="uni-breadcrumb-item--separator" :class="separatorClass" />
		<text v-else class="uni-breadcrumb-item--separator">{{ separator }}</text>
	</view>
</template>
<script>
	/**
	 * BreadcrumbItem 面包屑导航子组件
	 * @property {String/Object} to 路由跳转页面路径/对象
	 * @property {Boolean} replace 在使用 to 进行路由跳转时，启用 replace 将不会向 history 添加新记录(仅 h5 支持）
	 */
	export default {
		data() {
			return {
				currentPage: ""
			}
		},
		options: {
			virtualHost: true
		},
		props: {
			to: {
				type: String,
				default: ''
			},
			replace:{
				type: Boolean,
				default: false
			}
		},
		inject: {
			uniBreadcrumb: {
				from: "uniBreadcrumb",
				default: null
			}
		},
		created(){
			const pages = getCurrentPages()
			const page = pages[pages.length-1]

			if(page){
				this.currentPage = `/${page.route}`
			}
		},
		computed: {
			separator() {
				return this.uniBreadcrumb.separator
			},
			separatorClass() {
				return this.uniBreadcrumb.separatorClass
			}
		},
		methods: {
			navTo() {
				const { to } = this

				if (!to || this.currentPage === to){
					return
				}

				if(this.replace){
					uni.redirectTo({
						url:to
					})
				}else{
					uni.navigateTo({
						url:to
					})
				}
			}
		}
	}
</script>
<style lang="scss">
	$uni-primary: #2979ff !default;
	$uni-base-color: #6a6a6a !default;
	$uni-main-color: #3a3a3a !default;
	.uni-breadcrumb-item {
		display: flex;
		align-items: center;
		white-space: nowrap;
		font-size: 14px;

		&--slot {
			color: $uni-base-color;
			padding: 0 10px;

			&-link {
				color: $uni-main-color;
				font-weight: bold;
				/* #ifndef APP-NVUE */
				cursor: pointer;
				/* #endif */

				&:hover {
					color: $uni-primary;
				}
			}
		}

		&--separator {
			font-size: 12px;
			color: $uni-base-color;
		}

		&:first-child &--slot {
			padding-left: 0;
		}
		
		&:last-child &--separator {
			display: none;
		}
	}
</style>
