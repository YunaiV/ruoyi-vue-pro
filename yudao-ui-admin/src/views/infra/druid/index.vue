<template>
  <div>
    <doc-alert title="数据库 MyBatis" url="https://doc.iocoder.cn/mybatis/" />
    <doc-alert title="多数据源（读写分离）" url="https://doc.iocoder.cn/dynamic-datasource/" />
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
      url: process.env.VUE_APP_BASE_API + "/druid/index.html"
    };
  },
};
</script>
