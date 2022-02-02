// 使用renderjs直接操作window对象,实现动态控制app和h5的bounce
// bounce: iOS橡皮筋,Android半月弧,h5浏览器下拉背景等效果 (下拉刷新时禁止)
// https://uniapp.dcloud.io/frame?id=renderjs

// 与wxs的me实例一致
var me = {}

// 初始化window对象的touch事件 (仅初始化一次)
if(window && !window.$mescrollRenderInit){
	window.$mescrollRenderInit = true
	
	
	window.addEventListener('touchstart', function(e){
		if (me.disabled()) return;
		me.startPoint = me.getPoint(e); // 记录起点
	}, {passive: true})
	
	
	window.addEventListener('touchmove', function(e){
		if (me.disabled()) return;
		if (me.getScrollTop() > 0) return; // 需在顶部下拉,才禁止bounce
		
		var curPoint = me.getPoint(e); // 当前点
		var moveY = curPoint.y - me.startPoint.y; // 和起点比,移动的距离,大于0向下拉,小于0向上拉
		// 向下拉
		if (moveY > 0) {
			// 可下拉的条件
			if (!me.isDownScrolling && !me.optDown.isLock && (!me.isUpScrolling || (me.isUpScrolling && me.isUpBoth))) {
				
				// 只有touch在mescroll的view上面,才禁止bounce
				var el = e.target;
				var isMescrollTouch = false;
				while (el && el.tagName && el.tagName !== 'UNI-PAGE-BODY' && el.tagName != "BODY") {
					var cls = el.classList;
					if (cls && cls.contains('mescroll-render-touch')) {
						isMescrollTouch = true
						break;
					}
					el = el.parentNode; // 继续检查其父元素
				}
				// 禁止bounce (不会对swiper和iOS侧滑返回造成影响)
				if (isMescrollTouch && e.cancelable && !e.defaultPrevented) e.preventDefault();
			}
		}
	}, {passive: false})
}

/* 获取滚动条的位置 */
me.getScrollTop = function() {
	return me.scrollTop || 0
}

/* 是否禁用下拉刷新 */
me.disabled = function(){
	return !me.optDown || !me.optDown.use || me.optDown.native
}

/* 根据点击滑动事件获取第一个手指的坐标 */
me.getPoint = function(e) {
	if (!e) {
		return {x: 0,y: 0}
	}
	if (e.touches && e.touches[0]) {
		return {x: e.touches[0].pageX,y: e.touches[0].pageY}
	} else if (e.changedTouches && e.changedTouches[0]) {
		return {x: e.changedTouches[0].pageX,y: e.changedTouches[0].pageY}
	} else {
		return {x: e.clientX,y: e.clientY}
	}
}

/**
 * 监听逻辑层数据的变化 (实时更新数据)
 */
function propObserver(wxsProp) {
	me.optDown = wxsProp.optDown
	me.scrollTop = wxsProp.scrollTop
	me.isDownScrolling = wxsProp.isDownScrolling
	me.isUpScrolling = wxsProp.isUpScrolling
	me.isUpBoth = wxsProp.isUpBoth
}

/* 导出模块 */
const renderBiz = {
	data() {
		return {
			propObserver: propObserver,
		}
	}
}

export default renderBiz;