<template>
  <div v-loading="loading" :style="'height:'+ height">
    <iframe :src="src" frameborder="no" style="width: 100%;height: 100%" scrolling="auto" />
  </div>
</template>
<script>
import {exportHtml} from "@/api/infra/dbDoc";

export default {
  name: "DBDoc",
  data() {
    return {
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true,
      src: undefined,
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
  },
  created() {
    exportHtml().then(response => {
      // var blob = new Blob(['<a id="a"><b id="b">hey!</b></a>'], {type : 'text/html'});
      this.src = window.URL.createObjectURL(response);
    })
  },
};
</script>
