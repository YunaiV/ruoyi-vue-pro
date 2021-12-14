// 定义在wxs (含renderjs) 逻辑层的数据和方法, 与视图层相互通信
const WxsMixin = {
	data() {
		return {
			// 传入wxs视图层的数据 (响应式)
			wxsProp: {
				optDown:{}, // 下拉刷新的配置
				scrollTop:0, // 滚动条的距离
				bodyHeight:0, // body的高度
				isDownScrolling:false, // 是否正在下拉刷新中
				isUpScrolling:false, // 是否正在上拉加载中
				isScrollBody:true, // 是否为mescroll-body滚动
				isUpBoth:true, // 上拉加载时,是否同时可以下拉刷新
				t: 0 // 数据更新的标记 (只有数据更新了,才会触发wxs的Observer)
			},
			
			// 标记调用wxs视图层的方法
			callProp: {
				callType: '', // 方法名
				t: 0 // 数据更新的标记 (只有数据更新了,才会触发wxs的Observer)
			},
			
			// 不用wxs的平台使用此处的wxsBiz对象,抹平wxs的写法 (微信小程序和APP使用的wxsBiz对象是./wxs/wxs.wxs)
			// #ifndef MP-WEIXIN || APP-PLUS || H5
			wxsBiz: {
				//注册列表touchstart事件,用于下拉刷新
				touchstartEvent: e=> {
					this.mescroll.touchstartEvent(e);
				},
				//注册列表touchmove事件,用于下拉刷新
				touchmoveEvent: e=> {
					this.mescroll.touchmoveEvent(e);
				},
				//注册列表touchend事件,用于下拉刷新
				touchendEvent: e=> {
					this.mescroll.touchendEvent(e);
				},
				propObserver(){}, // 抹平wxs的写法
				callObserver(){} // 抹平wxs的写法
			},
			// #endif
			
			// 不用renderjs的平台使用此处的renderBiz对象,抹平renderjs的写法 (app 和 h5 使用的renderBiz对象是./wxs/renderjs.js)
			// #ifndef APP-PLUS || H5
			renderBiz: {
				propObserver(){} // 抹平renderjs的写法
			}
			// #endif
		}
	},
	methods: {
		// wxs视图层调用逻辑层的回调
		wxsCall(msg){
			if(msg.type === 'setWxsProp'){
				// 更新wxsProp数据 (值改变才触发更新)
				this.wxsProp = {
					optDown: this.mescroll.optDown,
					scrollTop: this.mescroll.getScrollTop(),
					bodyHeight: this.mescroll.getBodyHeight(),
					isDownScrolling: this.mescroll.isDownScrolling,
					isUpScrolling: this.mescroll.isUpScrolling,
					isUpBoth: this.mescroll.optUp.isBoth,
					isScrollBody:this.mescroll.isScrollBody,
					t: Date.now()
				}
			}else if(msg.type === 'setLoadType'){
				// 设置inOffset,outOffset的状态
				this.downLoadType = msg.downLoadType
			}else if(msg.type === 'triggerDownScroll'){
				// 主动触发下拉刷新
				this.mescroll.triggerDownScroll();
			}else if(msg.type === 'endDownScroll'){
				// 结束下拉刷新
				this.mescroll.endDownScroll();
			}else if(msg.type === 'triggerUpScroll'){
				// 主动触发上拉加载
				this.mescroll.triggerUpScroll(true);
			}
		}
	},
	mounted() {
		// #ifdef MP-WEIXIN || APP-PLUS || H5
		// 配置主动触发wxs显示加载进度的回调
		this.mescroll.optDown.afterLoading = ()=>{
			this.callProp = {callType: "showLoading", t: Date.now()} // 触发wxs的方法 (值改变才触发更新)
		}
		// 配置主动触发wxs隐藏加载进度的回调
		this.mescroll.optDown.afterEndDownScroll = ()=>{
			this.callProp = {callType: "endDownScroll", t: Date.now()} // 触发wxs的方法 (值改变才触发更新)
			setTimeout(()=>{
				if(this.downLoadType === 4 || this.downLoadType === 0){
					this.callProp = {callType: "clearTransform", t: Date.now()} // 触发wxs的方法 (值改变才触发更新)
				}
			},320)
		}
		// 初始化wxs的数据
		this.wxsCall({type: 'setWxsProp'})
		// #endif
	}
}

export default WxsMixin;
