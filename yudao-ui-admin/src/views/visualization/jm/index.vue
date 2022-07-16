<template>
  <div>
    <doc-alert title="接口文档" url="https://doc.iocoder.cn/api-doc/" />
    <i-frame :src="url" />
  </div>
</template>
<script>
import iFrame from "@/components/iFrame/index";
import {getAccessToken} from "@/utils/auth";
export default {
  name: "JimuReport",
  components: { iFrame },
  data() {
    return {
      url: process.env.VUE_APP_BASE_API + "/jmreport/list?token=" + getAccessToken(),
    };
  },
};
</script>
