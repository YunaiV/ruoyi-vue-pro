// bounce: iOS橡皮筋,Android半月弧,h5浏览器下拉背景等效果, 适用于h5和renderjs (下拉刷新时禁止)
const bounce = {
	// false: 禁止bounce; true:允许bounce
	setBounce: function(isBounce){
		window.$isMescrollBounce = isBounce
	}
}

// 引入即自动初始化 (仅初始化一次)
if(window && window.$isMescrollBounce == null){
	// 是否允许bounce, 默认允许
	window.$isMescrollBounce = true
	// 每次点击时重置bounce
	window.addEventListener('touchstart', function(){
		window.$isMescrollBounce = true
	}, {passive: true})
	// 滑动中标记是否禁止bounce (如:下拉刷新时禁止)
	window.addEventListener('touchmove', function(e){
		!window.$isMescrollBounce && e.preventDefault() // 禁止bounce
	}, {passive: false})
}

export default bounce;