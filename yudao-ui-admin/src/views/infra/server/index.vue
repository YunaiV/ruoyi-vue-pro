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
      url: process.env.VUE_APP_BASE_API + "/admin/applications"
    };
  },
};
</script>
