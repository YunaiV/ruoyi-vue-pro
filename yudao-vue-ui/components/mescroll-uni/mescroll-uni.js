/* mescroll
 * version 1.3.0
 * 2020-07-10 wenju
 * http://www.mescroll.com
 */

export default function MeScroll(options, isScrollBody) {
	let me = this;
	me.version = '1.3.0'; // mescroll版本号
	me.options = options || {}; // 配置
	me.isScrollBody = isScrollBody || false; // 滚动区域是否为原生页面滚动; 默认为scroll-view

	me.isDownScrolling = false; // 是否在执行下拉刷新的回调
	me.isUpScrolling = false; // 是否在执行上拉加载的回调
	let hasDownCallback = me.options.down && me.options.down.callback; // 是否配置了down的callback

	// 初始化下拉刷新
	me.initDownScroll();
	// 初始化上拉加载,则初始化
	me.initUpScroll();

	// 自动加载
	setTimeout(function() { // 待主线程执行完毕再执行,避免new MeScroll未初始化,在回调获取不到mescroll的实例
		// 自动触发下拉刷新 (只有配置了down的callback才自动触发下拉刷新)
		if ((me.optDown.use || me.optDown.native) && me.optDown.auto && hasDownCallback) {
			if (me.optDown.autoShowLoading) {
				me.triggerDownScroll(); // 显示下拉进度,执行下拉回调
			} else {
				me.optDown.callback && me.optDown.callback(me); // 不显示下拉进度,直接执行下拉回调
			}
		}
		// 自动触发上拉加载
		if(!me.isUpAutoLoad){ // 部分小程序(头条小程序)emit是异步, 会导致isUpAutoLoad判断有误, 先延时确保先执行down的callback,再执行up的callback
			setTimeout(function(){
				me.optUp.use && me.optUp.auto && !me.isUpAutoLoad && me.triggerUpScroll();
			},100)
		}
	}, 30); // 需让me.optDown.inited和me.optUp.inited先执行
}

/* 配置参数:下拉刷新 */
MeScroll.prototype.extendDownScroll = function(optDown) {
	// 下拉刷新的配置
	MeScroll.extend(optDown, {
		use: true, // 是否启用下拉刷新; 默认true
		auto: true, // 是否在初始化完毕之后自动执行下拉刷新的回调; 默认true
		native: false, // 是否使用系统自带的下拉刷新; 默认false; 仅mescroll-body生效 (值为true时,还需在pages配置enablePullDownRefresh:true;详请参考mescroll-native的案例)
		autoShowLoading: false, // 如果设置auto=true(在初始化完毕之后自动执行下拉刷新的回调),那么是否显示下拉刷新的进度; 默认false
		isLock: false, // 是否锁定下拉刷新,默认false;
		offset: 80, // 在列表顶部,下拉大于80px,松手即可触发下拉刷新的回调
		startTop: 100, // scroll-view快速滚动到顶部时,此时的scroll-top可能大于0, 此值用于控制最大的误差
		inOffsetRate: 1, // 在列表顶部,下拉的距离小于offset时,改变下拉区域高度比例;值小于1且越接近0,高度变化越小,表现为越往下越难拉
		outOffsetRate: 0.2, // 在列表顶部,下拉的距离大于offset时,改变下拉区域高度比例;值小于1且越接近0,高度变化越小,表现为越往下越难拉
		bottomOffset: 20, // 当手指touchmove位置在距离body底部20px范围内的时候结束上拉刷新,避免Webview嵌套导致touchend事件不执行
		minAngle: 45, // 向下滑动最少偏移的角度,取值区间  [0,90];默认45度,即向下滑动的角度大于45度则触发下拉;而小于45度,将不触发下拉,避免与左右滑动的轮播等组件冲突;
		textInOffset: '下拉刷新', // 下拉的距离在offset范围内的提示文本
		textOutOffset: '释放更新', // 下拉的距离大于offset范围的提示文本
		textLoading: '加载中 ...', // 加载中的提示文本
		bgColor: "transparent", // 背景颜色 (建议在pages.json中再设置一下backgroundColorTop)
		textColor: "gray", // 文本颜色 (当bgColor配置了颜色,而textColor未配置时,则textColor会默认为白色)
		inited: null, // 下拉刷新初始化完毕的回调
		inOffset: null, // 下拉的距离进入offset范围内那一刻的回调
		outOffset: null, // 下拉的距离大于offset那一刻的回调
		onMoving: null, // 下拉过程中的回调,滑动过程一直在执行; rate下拉区域当前高度与指定距离的比值(inOffset: rate<1; outOffset: rate>=1); downHight当前下拉区域的高度
		beforeLoading: null, // 准备触发下拉刷新的回调: 如果return true,将不触发showLoading和callback回调; 常用来完全自定义下拉刷新, 参考案例【淘宝 v6.8.0】
		showLoading: null, // 显示下拉刷新进度的回调
		afterLoading: null, // 显示下拉刷新进度的回调之后,马上要执行的代码 (如: 在wxs中使用)
		beforeEndDownScroll: null, // 准备结束下拉的回调. 返回结束下拉的延时执行时间,默认0ms; 常用于结束下拉之前再显示另外一小段动画,才去隐藏下拉刷新的场景, 参考案例【dotJump】
		endDownScroll: null, // 结束下拉刷新的回调
		afterEndDownScroll: null, // 结束下拉刷新的回调,马上要执行的代码 (如: 在wxs中使用)
		callback: function(mescroll) {
			// 下拉刷新的回调;默认重置上拉加载列表为第一页
			mescroll.resetUpScroll();
		}
	})
}

/* 配置参数:上拉加载 */
MeScroll.prototype.extendUpScroll = function(optUp) {
	// 上拉加载的配置
	MeScroll.extend(optUp, {
		use: true, // 是否启用上拉加载; 默认true
		auto: true, // 是否在初始化完毕之后自动执行上拉加载的回调; 默认true
		isLock: false, // 是否锁定上拉加载,默认false;
		isBoth: true, // 上拉加载时,如果滑动到列表顶部是否可以同时触发下拉刷新;默认true,两者可同时触发;
		callback: null, // 上拉加载的回调;function(page,mescroll){ }
		page: {
			num: 0, // 当前页码,默认0,回调之前会加1,即callback(page)会从1开始
			size: 10, // 每页数据的数量
			time: null // 加载第一页数据服务器返回的时间; 防止用户翻页时,后台新增了数据从而导致下一页数据重复;
		},
		noMoreSize: 5, // 如果列表已无数据,可设置列表的总数量要大于等于5条才显示无更多数据;避免列表数据过少(比如只有一条数据),显示无更多数据会不好看
		offset: 80, // 距底部多远时,触发upCallback
		textLoading: '加载中 ...', // 加载中的提示文本
		textNoMore: '-- 我也是有底线的 --', // 没有更多数据的提示文本
		bgColor: "transparent", // 背景颜色 (建议在pages.json中再设置一下backgroundColorBottom)
		textColor: "gray", // 文本颜色 (当bgColor配置了颜色,而textColor未配置时,则textColor会默认为白色)
		inited: null, // 初始化完毕的回调
		showLoading: null, // 显示加载中的回调
		showNoMore: null, // 显示无更多数据的回调
		hideUpScroll: null, // 隐藏上拉加载的回调
		errDistance: 60, // endErr的时候需往上滑动一段距离,使其往下滑动时再次触发onReachBottom,仅mescroll-body生效
		toTop: {
			// 回到顶部按钮,需配置src才显示
			src: null, // 图片路径,默认null (绝对路径或网络图)
			offset: 1000, // 列表滚动多少距离才显示回到顶部按钮,默认1000
			duration: 300, // 回到顶部的动画时长,默认300ms (当值为0或300则使用系统自带回到顶部,更流畅; 其他值则通过step模拟,部分机型可能不够流畅,所以非特殊情况不建议修改此项)
			btnClick: null, // 点击按钮的回调
			onShow: null, // 是否显示的回调
			zIndex: 9990, // fixed定位z-index值
			left: null, // 到左边的距离, 默认null. 此项有值时,right不生效. (支持20, "20rpx", "20px", "20%"格式的值, 其中纯数字则默认单位rpx)
			right: 20, // 到右边的距离, 默认20 (支持20, "20rpx", "20px", "20%"格式的值, 其中纯数字则默认单位rpx)
			bottom: 120, // 到底部的距离, 默认120 (支持20, "20rpx", "20px", "20%"格式的值, 其中纯数字则默认单位rpx)
			safearea: false, // bottom的偏移量是否加上底部安全区的距离, 默认false, 需要适配iPhoneX时使用 (具体的界面如果不配置此项,则取本vue的safearea值)
			width: 72, // 回到顶部图标的宽度, 默认72 (支持20, "20rpx", "20px", "20%"格式的值, 其中纯数字则默认单位rpx)
			radius: "50%" // 圆角, 默认"50%" (支持20, "20rpx", "20px", "20%"格式的值, 其中纯数字则默认单位rpx)
		},
		empty: {
			use: true, // 是否显示空布局
			icon: null, // 图标路径
			tip: '~ 暂无相关数据 ~', // 提示
			btnText: '', // 按钮
			btnClick: null, // 点击按钮的回调
			onShow: null, // 是否显示的回调
			fixed: false, // 是否使用fixed定位,默认false; 配置fixed为true,以下的top和zIndex才生效 (transform会使fixed失效,最终会降级为absolute)
			top: "100rpx", // fixed定位的top值 (完整的单位值,如 "10%"; "100rpx")
			zIndex: 99 // fixed定位z-index值
		},
		onScroll: false // 是否监听滚动事件
	})
}

/* 配置参数 */
MeScroll.extend = function(userOption, defaultOption) {
	if (!userOption) return defaultOption;
	for (let key in defaultOption) {
		if (userOption[key] == null) {
			let def = defaultOption[key];
			if (def != null && typeof def === 'object') {
				userOption[key] = MeScroll.extend({}, def); // 深度匹配
			} else {
				userOption[key] = def;
			}
		} else if (typeof userOption[key] === 'object') {
			MeScroll.extend(userOption[key], defaultOption[key]); // 深度匹配
		}
	}
	return userOption;
}

/* 简单判断是否配置了颜色 (非透明,非白色) */
MeScroll.prototype.hasColor = function(color) {
	if(!color) return false;
	let c = color.toLowerCase();
	return c != "#fff" && c != "#ffffff" && c != "transparent" && c != "white"
}

/* -------初始化下拉刷新------- */
MeScroll.prototype.initDownScroll = function() {
	let me = this;
	// 配置参数
	me.optDown = me.options.down || {};
	if(!me.optDown.textColor && me.hasColor(me.optDown.bgColor)) me.optDown.textColor = "#fff"; // 当bgColor有值且textColor未设置,则textColor默认白色
	me.extendDownScroll(me.optDown);
	
	// 如果是mescroll-body且配置了native,则禁止自定义的下拉刷新
	if(me.isScrollBody && me.optDown.native){
		me.optDown.use = false
	}else{
		me.optDown.native = false // 仅mescroll-body支持,mescroll-uni不支持
	}
	
	me.downHight = 0; // 下拉区域的高度

	// 在页面中加入下拉布局
	if (me.optDown.use && me.optDown.inited) {
		// 初始化完毕的回调
		setTimeout(function() { // 待主线程执行完毕再执行,避免new MeScroll未初始化,在回调获取不到mescroll的实例
			me.optDown.inited(me);
		}, 0)
	}
}

/* 列表touchstart事件 */
MeScroll.prototype.touchstartEvent = function(e) {
	if (!this.optDown.use) return;

	this.startPoint = this.getPoint(e); // 记录起点
	this.startTop = this.getScrollTop(); // 记录此时的滚动条位置
	this.startAngle = 0; // 初始角度
	this.lastPoint = this.startPoint; // 重置上次move的点
	this.maxTouchmoveY = this.getBodyHeight() - this.optDown.bottomOffset; // 手指触摸的最大范围(写在touchstart避免body获取高度为0的情况)
	this.inTouchend = false; // 标记不是touchend
}

/* 列表touchmove事件 */
MeScroll.prototype.touchmoveEvent = function(e) {
	if (!this.optDown.use) return;
	let me = this;

	let scrollTop = me.getScrollTop(); // 当前滚动条的距离
	let curPoint = me.getPoint(e); // 当前点

	let moveY = curPoint.y - me.startPoint.y; // 和起点比,移动的距离,大于0向下拉,小于0向上拉

	// 向下拉 && 在顶部
	// mescroll-body,直接判定在顶部即可
	// scroll-view在滚动时不会触发touchmove,当触顶/底/左/右时,才会触发touchmove
	// scroll-view滚动到顶部时,scrollTop不一定为0,也有可能大于0; 在iOS的APP中scrollTop可能为负数,不一定和startTop相等
	if (moveY > 0 && (
			(me.isScrollBody && scrollTop <= 0)
			||
			(!me.isScrollBody && (scrollTop <= 0 || (scrollTop <= me.optDown.startTop && scrollTop === me.startTop)) )
		)) {
		// 可下拉的条件
		if (!me.inTouchend && !me.isDownScrolling && !me.optDown.isLock && (!me.isUpScrolling || (me.isUpScrolling &&
				me.optUp.isBoth))) {

			// 下拉的初始角度是否在配置的范围内
			if(!me.startAngle) me.startAngle = me.getAngle(me.lastPoint, curPoint); // 两点之间的角度,区间 [0,90]
			if (me.startAngle < me.optDown.minAngle) return; // 如果小于配置的角度,则不往下执行下拉刷新

			// 如果手指的位置超过配置的距离,则提前结束下拉,避免Webview嵌套导致touchend无法触发
			if (me.maxTouchmoveY > 0 && curPoint.y >= me.maxTouchmoveY) {
				me.inTouchend = true; // 标记执行touchend
				me.touchendEvent(); // 提前触发touchend
				return;
			}
			
			me.preventDefault(e); // 阻止默认事件

			let diff = curPoint.y - me.lastPoint.y; // 和上次比,移动的距离 (大于0向下,小于0向上)

			// 下拉距离  < 指定距离
			if (me.downHight < me.optDown.offset) {
				if (me.movetype !== 1) {
					me.movetype = 1; // 加入标记,保证只执行一次
					me.optDown.inOffset && me.optDown.inOffset(me); // 进入指定距离范围内那一刻的回调,只执行一次
					me.isMoveDown = true; // 标记下拉区域高度改变,在touchend重置回来
				}
				me.downHight += diff * me.optDown.inOffsetRate; // 越往下,高度变化越小

				// 指定距离  <= 下拉距离
			} else {
				if (me.movetype !== 2) {
					me.movetype = 2; // 加入标记,保证只执行一次
					me.optDown.outOffset && me.optDown.outOffset(me); // 下拉超过指定距离那一刻的回调,只执行一次
					me.isMoveDown = true; // 标记下拉区域高度改变,在touchend重置回来
				}
				if (diff > 0) { // 向下拉
					me.downHight += diff * me.optDown.outOffsetRate; // 越往下,高度变化越小
				} else { // 向上收
					me.downHight += diff; // 向上收回高度,则向上滑多少收多少高度
				}
			}
			
			me.downHight = Math.round(me.downHight) // 取整
			let rate = me.downHight / me.optDown.offset; // 下拉区域当前高度与指定距离的比值
			me.optDown.onMoving && me.optDown.onMoving(me, rate, me.downHight); // 下拉过程中的回调,一直在执行
		}
	}

	me.lastPoint = curPoint; // 记录本次移动的点
}

/* 列表touchend事件 */
MeScroll.prototype.touchendEvent = function(e) {
	if (!this.optDown.use) return;
	// 如果下拉区域高度已改变,则需重置回来
	if (this.isMoveDown) {
		if (this.downHight >= this.optDown.offset) {
			// 符合触发刷新的条件
			this.triggerDownScroll();
		} else {
			// 不符合的话 则重置
			this.downHight = 0;
			this.endDownScrollCall(this);
		}
		this.movetype = 0;
		this.isMoveDown = false;
	} else if (!this.isScrollBody && this.getScrollTop() === this.startTop) { // scroll-view到顶/左/右/底的滑动事件
		let isScrollUp = this.getPoint(e).y - this.startPoint.y < 0; // 和起点比,移动的距离,大于0向下拉,小于0向上拉
		// 上滑
		if (isScrollUp) {
			// 需检查滑动的角度
			let angle = this.getAngle(this.getPoint(e), this.startPoint); // 两点之间的角度,区间 [0,90]
			if (angle > 80) {
				// 检查并触发上拉
				this.triggerUpScroll(true);
			}
		}
	}
}

/* 根据点击滑动事件获取第一个手指的坐标 */
MeScroll.prototype.getPoint = function(e) {
	if (!e) {
		return {
			x: 0,
			y: 0
		}
	}
	if (e.touches && e.touches[0]) {
		return {
			x: e.touches[0].pageX,
			y: e.touches[0].pageY
		}
	} else if (e.changedTouches && e.changedTouches[0]) {
		return {
			x: e.changedTouches[0].pageX,
			y: e.changedTouches[0].pageY
		}
	} else {
		return {
			x: e.clientX,
			y: e.clientY
		}
	}
}

/* 计算两点之间的角度: 区间 [0,90]*/
MeScroll.prototype.getAngle = function(p1, p2) {
	let x = Math.abs(p1.x - p2.x);
	let y = Math.abs(p1.y - p2.y);
	let z = Math.sqrt(x * x + y * y);
	let angle = 0;
	if (z !== 0) {
		angle = Math.asin(y / z) / Math.PI * 180;
	}
	return angle
}

/* 触发下拉刷新 */
MeScroll.prototype.triggerDownScroll = function() {
	if (this.optDown.beforeLoading && this.optDown.beforeLoading(this)) {
		//return true则处于完全自定义状态
	} else {
		this.showDownScroll(); // 下拉刷新中...
		!this.optDown.native && this.optDown.callback && this.optDown.callback(this); // 执行回调,联网加载数据
	}
}

/* 显示下拉进度布局 */
MeScroll.prototype.showDownScroll = function() {
	this.isDownScrolling = true; // 标记下拉中
	if (this.optDown.native) {
		uni.startPullDownRefresh(); // 系统自带的下拉刷新
		this.showDownLoadingCall(0); // 仍触发showLoading,因为上拉加载用到
	} else{
		this.downHight = this.optDown.offset; // 更新下拉区域高度
		this.showDownLoadingCall(this.downHight); // 下拉刷新中...
	}
}

MeScroll.prototype.showDownLoadingCall = function(downHight) {
	this.optDown.showLoading && this.optDown.showLoading(this, downHight); // 下拉刷新中...
	this.optDown.afterLoading && this.optDown.afterLoading(this, downHight); // 下拉刷新中...触发之后马上要执行的代码
}

/* 显示系统自带的下拉刷新时需要处理的业务 */
MeScroll.prototype.onPullDownRefresh = function() {
	this.isDownScrolling = true; // 标记下拉中
	this.showDownLoadingCall(0); // 仍触发showLoading,因为上拉加载用到
	this.optDown.callback && this.optDown.callback(this); // 执行回调,联网加载数据
}

/* 结束下拉刷新 */
MeScroll.prototype.endDownScroll = function() {
	if (this.optDown.native) { // 结束原生下拉刷新
		this.isDownScrolling = false;
		this.endDownScrollCall(this);
		uni.stopPullDownRefresh();
		return
	}
	let me = this;
	// 结束下拉刷新的方法
	let endScroll = function() {
		me.downHight = 0;
		me.isDownScrolling = false;
		me.endDownScrollCall(me);
		if(!me.isScrollBody){
			me.setScrollHeight(0) // scroll-view重置滚动区域,使数据不满屏时仍可检查触发翻页
			me.scrollTo(0,0) // scroll-view需重置滚动条到顶部,避免startTop大于0时,对下拉刷新的影响
		}
	}
	// 结束下拉刷新时的回调
	let delay = 0;
	if (me.optDown.beforeEndDownScroll) delay = me.optDown.beforeEndDownScroll(me); // 结束下拉刷新的延时,单位ms
	if (typeof delay === 'number' && delay > 0) {
		setTimeout(endScroll, delay);
	} else {
		endScroll();
	}
}

MeScroll.prototype.endDownScrollCall = function() {
	this.optDown.endDownScroll && this.optDown.endDownScroll(this);
	this.optDown.afterEndDownScroll && this.optDown.afterEndDownScroll(this);
}

/* 锁定下拉刷新:isLock=ture,null锁定;isLock=false解锁 */
MeScroll.prototype.lockDownScroll = function(isLock) {
	if (isLock == null) isLock = true;
	this.optDown.isLock = isLock;
}

/* 锁定上拉加载:isLock=ture,null锁定;isLock=false解锁 */
MeScroll.prototype.lockUpScroll = function(isLock) {
	if (isLock == null) isLock = true;
	this.optUp.isLock = isLock;
}

/* -------初始化上拉加载------- */
MeScroll.prototype.initUpScroll = function() {
	let me = this;
	// 配置参数
	me.optUp = me.options.up || {use: false}
	if(!me.optUp.textColor && me.hasColor(me.optUp.bgColor)) me.optUp.textColor = "#fff"; // 当bgColor有值且textColor未设置,则textColor默认白色
	me.extendUpScroll(me.optUp);

	if (me.optUp.use === false) return; // 配置不使用上拉加载时,则不初始化上拉布局
	me.optUp.hasNext = true; // 如果使用上拉,则默认有下一页
	me.startNum = me.optUp.page.num + 1; // 记录page开始的页码

	// 初始化完毕的回调
	if (me.optUp.inited) {
		setTimeout(function() { // 待主线程执行完毕再执行,避免new MeScroll未初始化,在回调获取不到mescroll的实例
			me.optUp.inited(me);
		}, 0)
	}
}

/*滚动到底部的事件 (仅mescroll-body生效)*/
MeScroll.prototype.onReachBottom = function() {
	if (this.isScrollBody && !this.isUpScrolling) { // 只能支持下拉刷新的时候同时可以触发上拉加载,否则滚动到底部就需要上滑一点才能触发onReachBottom
		if (!this.optUp.isLock && this.optUp.hasNext) {
			this.triggerUpScroll();
		}
	}
}

/*列表滚动事件 (仅mescroll-body生效)*/
MeScroll.prototype.onPageScroll = function(e) {
	if (!this.isScrollBody) return;
	
	// 更新滚动条的位置 (主要用于判断下拉刷新时,滚动条是否在顶部)
	this.setScrollTop(e.scrollTop);

	// 顶部按钮的显示隐藏
	if (e.scrollTop >= this.optUp.toTop.offset) {
		this.showTopBtn();
	} else {
		this.hideTopBtn();
	}
}

/*列表滚动事件*/
MeScroll.prototype.scroll = function(e, onScroll) {
	// 更新滚动条的位置
	this.setScrollTop(e.scrollTop);
	// 更新滚动内容高度
	this.setScrollHeight(e.scrollHeight);

	// 向上滑还是向下滑动
	if (this.preScrollY == null) this.preScrollY = 0;
	this.isScrollUp = e.scrollTop - this.preScrollY > 0;
	this.preScrollY = e.scrollTop;

	// 上滑 && 检查并触发上拉
	this.isScrollUp && this.triggerUpScroll(true);

	// 顶部按钮的显示隐藏
	if (e.scrollTop >= this.optUp.toTop.offset) {
		this.showTopBtn();
	} else {
		this.hideTopBtn();
	}

	// 滑动监听
	this.optUp.onScroll && onScroll && onScroll()
}

/* 触发上拉加载 */
MeScroll.prototype.triggerUpScroll = function(isCheck) {
	if (!this.isUpScrolling && this.optUp.use && this.optUp.callback) {
		// 是否校验在底部; 默认不校验
		if (isCheck === true) {
			let canUp = false;
			// 还有下一页 && 没有锁定 && 不在下拉中
			if (this.optUp.hasNext && !this.optUp.isLock && !this.isDownScrolling) {
				if (this.getScrollBottom() <= this.optUp.offset) { // 到底部
					canUp = true; // 标记可上拉
				}
			}
			if (canUp === false) return;
		}
		this.showUpScroll(); // 上拉加载中...
		this.optUp.page.num++; // 预先加一页,如果失败则减回
		this.isUpAutoLoad = true; // 标记上拉已经自动执行过,避免初始化时多次触发上拉回调
		this.num = this.optUp.page.num; // 把最新的页数赋值在mescroll上,避免对page的影响
		this.size = this.optUp.page.size; // 把最新的页码赋值在mescroll上,避免对page的影响
		this.time = this.optUp.page.time; // 把最新的页码赋值在mescroll上,避免对page的影响
		this.optUp.callback(this); // 执行回调,联网加载数据
	}
}

/* 显示上拉加载中 */
MeScroll.prototype.showUpScroll = function() {
	this.isUpScrolling = true; // 标记上拉加载中
	this.optUp.showLoading && this.optUp.showLoading(this); // 回调
}

/* 显示上拉无更多数据 */
MeScroll.prototype.showNoMore = function() {
	this.optUp.hasNext = false; // 标记无更多数据
	this.optUp.showNoMore && this.optUp.showNoMore(this); // 回调
}

/* 隐藏上拉区域**/
MeScroll.prototype.hideUpScroll = function() {
	this.optUp.hideUpScroll && this.optUp.hideUpScroll(this); // 回调
}

/* 结束上拉加载 */
MeScroll.prototype.endUpScroll = function(isShowNoMore) {
	if (isShowNoMore != null) { // isShowNoMore=null,不处理下拉状态,下拉刷新的时候调用
		if (isShowNoMore) {
			this.showNoMore(); // isShowNoMore=true,显示无更多数据
		} else {
			this.hideUpScroll(); // isShowNoMore=false,隐藏上拉加载
		}
	}
	this.isUpScrolling = false; // 标记结束上拉加载
}

/* 重置上拉加载列表为第一页
 *isShowLoading 是否显示进度布局;
 * 1.默认null,不传参,则显示上拉加载的进度布局
 * 2.传参true, 则显示下拉刷新的进度布局
 * 3.传参false,则不显示上拉和下拉的进度 (常用于静默更新列表数据)
 */
MeScroll.prototype.resetUpScroll = function(isShowLoading) {
	if (this.optUp && this.optUp.use) {
		let page = this.optUp.page;
		this.prePageNum = page.num; // 缓存重置前的页码,加载失败可退回
		this.prePageTime = page.time; // 缓存重置前的时间,加载失败可退回
		page.num = this.startNum; // 重置为第一页
		page.time = null; // 重置时间为空
		if (!this.isDownScrolling && isShowLoading !== false) { // 如果不是下拉刷新触发的resetUpScroll并且不配置列表静默更新,则显示进度;
			if (isShowLoading == null) {
				this.removeEmpty(); // 移除空布局
				this.showUpScroll(); // 不传参,默认显示上拉加载的进度布局
			} else {
				this.showDownScroll(); // 传true,显示下拉刷新的进度布局,不清空列表
			}
		}
		this.isUpAutoLoad = true; // 标记上拉已经自动执行过,避免初始化时多次触发上拉回调
		this.num = page.num; // 把最新的页数赋值在mescroll上,避免对page的影响
		this.size = page.size; // 把最新的页码赋值在mescroll上,避免对page的影响
		this.time = page.time; // 把最新的页码赋值在mescroll上,避免对page的影响
		this.optUp.callback && this.optUp.callback(this); // 执行上拉回调
	}
}

/* 设置page.num的值 */
MeScroll.prototype.setPageNum = function(num) {
	this.optUp.page.num = num - 1;
}

/* 设置page.size的值 */
MeScroll.prototype.setPageSize = function(size) {
	this.optUp.page.size = size;
}

/* 联网回调成功,结束下拉刷新和上拉加载
 * dataSize: 当前页的数据量(必传)
 * totalPage: 总页数(必传)
 * systime: 服务器时间 (可空)
 */
MeScroll.prototype.endByPage = function(dataSize, totalPage, systime) {
	let hasNext;
	if (this.optUp.use && totalPage != null) hasNext = this.optUp.page.num < totalPage; // 是否还有下一页
	this.endSuccess(dataSize, hasNext, systime);
}

/* 联网回调成功,结束下拉刷新和上拉加载
 * dataSize: 当前页的数据量(必传)
 * totalSize: 列表所有数据总数量(必传)
 * systime: 服务器时间 (可空)
 */
MeScroll.prototype.endBySize = function(dataSize, totalSize, systime) {
	let hasNext;
	if (this.optUp.use && totalSize != null) {
		let loadSize = (this.optUp.page.num - 1) * this.optUp.page.size + dataSize; // 已加载的数据总数
		hasNext = loadSize < totalSize; // 是否还有下一页
	}
	this.endSuccess(dataSize, hasNext, systime);
}

/* 联网回调成功,结束下拉刷新和上拉加载
 * dataSize: 当前页的数据个数(不是所有页的数据总和),用于上拉加载判断是否还有下一页.如果不传,则会判断还有下一页
 * hasNext: 是否还有下一页,布尔类型;用来解决这个小问题:比如列表共有20条数据,每页加载10条,共2页.如果只根据dataSize判断,则需翻到第三页才会知道无更多数据,如果传了hasNext,则翻到第二页即可显示无更多数据.
 * systime: 服务器时间(可空);用来解决这个小问题:当准备翻下一页时,数据库新增了几条记录,此时翻下一页,前面的几条数据会和上一页的重复;这里传入了systime,那么upCallback的page.time就会有值,把page.time传给服务器,让后台过滤新加入的那几条记录
 */
MeScroll.prototype.endSuccess = function(dataSize, hasNext, systime) {
	let me = this;
	// 结束下拉刷新
	if (me.isDownScrolling) me.endDownScroll();

	// 结束上拉加载
	if (me.optUp.use) {
		let isShowNoMore; // 是否已无更多数据
		if (dataSize != null) {
			let pageNum = me.optUp.page.num; // 当前页码
			let pageSize = me.optUp.page.size; // 每页长度
			// 如果是第一页
			if (pageNum === 1) {
				if (systime) me.optUp.page.time = systime; // 设置加载列表数据第一页的时间
			}
			if (dataSize < pageSize || hasNext === false) {
				// 返回的数据不满一页时,则说明已无更多数据
				me.optUp.hasNext = false;
				if (dataSize === 0 && pageNum === 1) {
					// 如果第一页无任何数据且配置了空布局
					isShowNoMore = false;
					me.showEmpty();
				} else {
					// 总列表数少于配置的数量,则不显示无更多数据
					let allDataSize = (pageNum - 1) * pageSize + dataSize;
					if (allDataSize < me.optUp.noMoreSize) {
						isShowNoMore = false;
					} else {
						isShowNoMore = true;
					}
					me.removeEmpty(); // 移除空布局
				}
			} else {
				// 还有下一页
				isShowNoMore = false;
				me.optUp.hasNext = true;
				me.removeEmpty(); // 移除空布局
			}
		}

		// 隐藏上拉
		me.endUpScroll(isShowNoMore);
	}
}

/* 回调失败,结束下拉刷新和上拉加载 */
MeScroll.prototype.endErr = function(errDistance) {
	// 结束下拉,回调失败重置回原来的页码和时间
	if (this.isDownScrolling) {
		let page = this.optUp.page;
		if (page && this.prePageNum) {
			page.num = this.prePageNum;
			page.time = this.prePageTime;
		}
		this.endDownScroll();
	}
	// 结束上拉,回调失败重置回原来的页码
	if (this.isUpScrolling) {
		this.optUp.page.num--;
		this.endUpScroll(false);
		// 如果是mescroll-body,则需往回滚一定距离
		if(this.isScrollBody && errDistance !== 0){ // 不处理0
			if(!errDistance) errDistance = this.optUp.errDistance; // 不传,则取默认
			this.scrollTo(this.getScrollTop() - errDistance, 0) // 往上回滚的距离
		}
	}
}

/* 显示空布局 */
MeScroll.prototype.showEmpty = function() {
	this.optUp.empty.use && this.optUp.empty.onShow && this.optUp.empty.onShow(true)
}

/* 移除空布局 */
MeScroll.prototype.removeEmpty = function() {
	this.optUp.empty.use && this.optUp.empty.onShow && this.optUp.empty.onShow(false)
}

/* 显示回到顶部的按钮 */
MeScroll.prototype.showTopBtn = function() {
	if (!this.topBtnShow) {
		this.topBtnShow = true;
		this.optUp.toTop.onShow && this.optUp.toTop.onShow(true);
	}
}

/* 隐藏回到顶部的按钮 */
MeScroll.prototype.hideTopBtn = function() {
	if (this.topBtnShow) {
		this.topBtnShow = false;
		this.optUp.toTop.onShow && this.optUp.toTop.onShow(false);
	}
}

/* 获取滚动条的位置 */
MeScroll.prototype.getScrollTop = function() {
	return this.scrollTop || 0
}

/* 记录滚动条的位置 */
MeScroll.prototype.setScrollTop = function(y) {
	this.scrollTop = y;
}

/* 滚动到指定位置 */
MeScroll.prototype.scrollTo = function(y, t) {
	this.myScrollTo && this.myScrollTo(y, t) // scrollview需自定义回到顶部方法
}

/* 自定义scrollTo */
MeScroll.prototype.resetScrollTo = function(myScrollTo) {
	this.myScrollTo = myScrollTo
}

/* 滚动条到底部的距离 */
MeScroll.prototype.getScrollBottom = function() {
	return this.getScrollHeight() - this.getClientHeight() - this.getScrollTop()
}

/* 计步器
 star: 开始值
 end: 结束值
 callback(step,timer): 回调step值,计步器timer,可自行通过window.clearInterval(timer)结束计步器;
 t: 计步时长,传0则直接回调end值;不传则默认300ms
 rate: 周期;不传则默认30ms计步一次
 * */
MeScroll.prototype.getStep = function(star, end, callback, t, rate) {
	let diff = end - star; // 差值
	if (t === 0 || diff === 0) {
		callback && callback(end);
		return;
	}
	t = t || 300; // 时长 300ms
	rate = rate || 30; // 周期 30ms
	let count = t / rate; // 次数
	let step = diff / count; // 步长
	let i = 0; // 计数
	let timer = setInterval(function() {
		if (i < count - 1) {
			star += step;
			callback && callback(star, timer);
			i++;
		} else {
			callback && callback(end, timer); // 最后一次直接设置end,避免计算误差
			clearInterval(timer);
		}
	}, rate);
}

/* 滚动容器的高度 */
MeScroll.prototype.getClientHeight = function(isReal) {
	let h = this.clientHeight || 0
	if (h === 0 && isReal !== true) { // 未获取到容器的高度,可临时取body的高度 (可能会有误差)
		h = this.getBodyHeight()
	}
	return h
}
MeScroll.prototype.setClientHeight = function(h) {
	this.clientHeight = h;
}

/* 滚动内容的高度 */
MeScroll.prototype.getScrollHeight = function() {
	return this.scrollHeight || 0;
}
MeScroll.prototype.setScrollHeight = function(h) {
	this.scrollHeight = h;
}

/* body的高度 */
MeScroll.prototype.getBodyHeight = function() {
	return this.bodyHeight || 0;
}
MeScroll.prototype.setBodyHeight = function(h) {
	this.bodyHeight = h;
}

/* 阻止浏览器默认滚动事件 */
MeScroll.prototype.preventDefault = function(e) {
	// 小程序不支持e.preventDefault, 已在wxs中禁止
	// app的bounce只能通过配置pages.json的style.app-plus.bounce为"none"来禁止, 或使用renderjs禁止
	// cancelable:是否可以被禁用; defaultPrevented:是否已经被禁用
	if (e && e.cancelable && !e.defaultPrevented) e.preventDefault()
}