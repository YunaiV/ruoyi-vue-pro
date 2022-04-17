<template>
  <div>
    <doc-alert title="服务监控" url="https://doc.iocoder.cn/server-monitor/" />
    <i-frame :src="url" />
  </div>
</template>
<script>
import iFrame from "@/components/iFrame/index";
export default {
  name: "Druid",
  components: { iFrame },
  data() {
    return {
      url: "http://skywalking.shop.iocoder.cn", // TODO 芋艿，后续改成配置读取
    };
  },
};
</script>
