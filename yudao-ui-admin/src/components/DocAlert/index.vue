<template>
  <el-alert v-if="enable()" type="success" show-icon>
    <template slot="title">
      <div @click="goToUrl">{{ '【' + title + '】文档地址：' + url }}</div>
    </template>
  </el-alert>
</template>

<script>
import {getDocEnable} from "@/utils/ruoyi";

export default {
  name: "DocAlert",
  props: {
    title: String,
    url: String,
  },
  methods: {
    enable: function () {
      return getDocEnable();
    },
    goToUrl: function() {
      window.open(this.url);
    }
  }
};
</script>
<style scoped>
.el-alert--success.is-light {
  margin-bottom: 10px;
  cursor: pointer;
}
</style>
