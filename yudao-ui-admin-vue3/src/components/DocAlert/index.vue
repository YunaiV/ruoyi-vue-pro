<template>
  <el-alert v-if="enable()" :title="'【' + title + '】文档地址：' + url" type="success" show-icon />
</template>

<script setup>
import {getDocEnable} from "@/utils/ruoyi";

const props = defineProps({
  title: String,
  url: String,
})

function enable() {
  return getDocEnable();
}

</script>
<style scoped>
.el-alert--success.is-light {
  margin-bottom: 10px;
}
</style>
