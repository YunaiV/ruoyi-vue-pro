<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
  /* 引入uView基础样式 */
  @import "@/uni_modules/uview-ui/index.scss";
	
	/*每个页面公共css */
  .container {
    background-color: $uni-bg-color;
    box-sizing: border-box;
    height: 100%;
  }
</style>
