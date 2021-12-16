function scroll(event, ownerInstance) {
	// detail中含有scroll-view的信息，比如scroll-view的实际宽度，当前时间点scroll-view的移动距离等
	var detail = event.detail
	var scrollWidth = detail.scrollWidth
	var scrollLeft = detail.scrollLeft
	// 获取当前组件的dataset，说白了就是祸国殃民的腾xun搞出来的垃ji
	var dataset = event.currentTarget.dataset
	// 此为scroll-view外部包裹元素的宽度
	// 某些HX版本(3.1.18)，发现view元素中大写的data-scrollWidth，在wxs中，变成了全部小写，所以这里需要特别处理
	var scrollComponentWidth = dataset.scrollWidth || dataset.scrollwidth || 0
	// 指示器和滑块的宽度
	var indicatorWidth = dataset.indicatorWidth || dataset.indicatorwidth || 0
	var barWidth = dataset.barWidth || dataset.barwidth || 0
	// 此处的计算理由为：scroll-view的滚动距离与目标滚动距离(scroll-view的实际宽度减去包裹元素的宽度)之比，等于滑块当前移动距离与总需
	// 滑动距离(指示器的总宽度减去滑块宽度)的比值
	var x = scrollLeft / (scrollWidth - scrollComponentWidth) * (indicatorWidth - barWidth)
	setBarStyle(ownerInstance, x)
}

// 由于webview的无能，无法保证scroll-view在滑动过程中，一直触发scroll事件，会导致
// 无法监听到某些滚动值，当在首尾临界值无法监听到时，这是致命的，因为错失这些值会导致滑块无法回到起点和终点
// 所以这里需要对临界值做监听并处理
function scrolltolower(event, ownerInstance) {
	ownerInstance.callMethod('scrollEvent', 'right')
	// 获取当前组件的dataset
	var dataset = event.currentTarget.dataset
	// 指示器和滑块的宽度
	var indicatorWidth = dataset.indicatorWidth || dataset.indicatorwidth || 0
	var barWidth = dataset.barWidth || dataset.barwidth || 0
	// scroll-view滚动到右边终点时，将滑块也设置为到右边的终点，它所需移动的距离为：指示器宽度 - 滑块宽度
	setBarStyle(ownerInstance, indicatorWidth - barWidth)
}

function scrolltoupper(event, ownerInstance) {
	ownerInstance.callMethod('scrollEvent', 'left')
	// 滚动到左边时，将滑块设置为0的偏移距离，回到起点
	setBarStyle(ownerInstance, 0)
}

function setBarStyle(ownerInstance, x) {
	ownerInstance.selectComponent('.u-scroll-list__indicator__line__bar') && ownerInstance.selectComponent('.u-scroll-list__indicator__line__bar').setStyle({
		transform: 'translateX(' + x + 'px)'
	})
}

module.exports = {
	scroll: scroll,
	scrolltolower: scrolltolower,
	scrolltoupper: scrolltoupper
}
