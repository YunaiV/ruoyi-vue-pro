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
  name: "InfraAdminServer",
  components: { iFrame },
  data() {
    return {
      url: process.env.VUE_APP_BASE_API + "/admin/applications",
      loading: true
    };
  },
  created() {
    getConfigKey("url.spring-boot-admin").then(response => {
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
