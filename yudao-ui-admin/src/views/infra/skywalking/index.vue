<template>
  <div>
    <doc-alert title="服务监控" url="https://doc.iocoder.cn/server-monitor/" />
    <i-frame v-if="!loading" :src="url" />
  </div>
</template>
<script>
import iFrame from "@/components/iFrame/index";
import { getConfigKey } from "@/api/infra/config";
export default {
  name: "InfraSkyWalking",
  components: { iFrame },
  data() {
    return {
      url: "http://skywalking.shop.iocoder.cn",
      loading: true
    };
  },
  created() {
    getConfigKey("url.skywalking").then(response => {
      if (!response.data || response.data.length === 0) {
        return
      }
      this.url = response.data;
    }).finally(() => {
      this.loading = false;
    })
  }
};
</script>
