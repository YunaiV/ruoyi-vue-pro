<template>
  <i-frame :src="url" />
</template>
<script>
import iFrame from "@/components/iFrame/index";
export default {
  name: "Druid",
  components: { iFrame },
  data() {
    return {
      url: "http://skywalking.shop.iocoder.cn/trace", // TODO 芋艿，后续改成配置读取
    };
  },
};
</script>
