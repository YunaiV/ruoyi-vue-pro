<template>
  <el-alert v-if="enable()" :title="'【' + title + '】文档地址：' + url" type="success" show-icon />
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
    }
  }
};
</script>
<style scoped>
.el-alert--success.is-light {
  margin-bottom: 10px;
}
</style>
