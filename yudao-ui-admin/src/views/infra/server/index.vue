<template>
  <div v-loading="loading" :style="'height:'+ height">
    <iframe ref="iframe" :src="src" frameborder="no" style="width: 100%;height: 100%" scrolling="auto" />
  </div>
</template>
<script>
export default {
  name: "Server",
  data() {
    return {
      src: process.env.VUE_APP_BASE_API + "/admin",
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true
    };
  },
  mounted: function() {
    const that = this;

    setTimeout(() => {
      // 模拟点击【应用】菜单
      // that.$refs["iframe"].contentWindow.document.getElementsByClassName('navbar-item')[2].click(); // TODO 暂时去掉，存在跨域问题
      // 取消加载中
      this.loading = false;
    }, 230);

    // 大小变化的监听
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 94.5 + "px;";
    };
  }
};
</script>
