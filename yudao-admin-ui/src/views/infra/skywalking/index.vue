<template>
  <div v-loading="loading" :style="'height:'+ height">
    <iframe :src="src" frameborder="no" style="width: 100%;height: 100%" scrolling="auto" />
  </div>
</template>
<script>
export default {
  name: "SkyWalking",
  data() {
    return {
      src: "http://skywalking.shop.iocoder.cn/trace", // TODO 芋艿，后续改成配置读取
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true
    };
  },
  mounted: function() {
    setTimeout(() => {
      this.loading = false;
    }, 230);
    const that = this;
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 94.5 + "px;";
    };
  }
};
</script>
