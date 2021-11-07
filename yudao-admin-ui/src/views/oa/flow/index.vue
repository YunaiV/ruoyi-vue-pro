<template>
  <div class="app-container">
    <el-row>
      <div>TODO @芋艿, 待办请假和 已办请假，感觉可以做成通用，不同的业务表单对应的待办任务和已办任务可以通用，或许可以想个更通用的名字 </div>
    </el-row>
  </div>
</template>

<script>
import { getStartForm } from "@/api/oa/flow";
export default {
  name: "Flow",
  components: {
  },
  data() {
    return {
      // 表单参数
      form: {},

      rules: {
      }
    };
  },
  created() {

  },
  methods: {
    // leave() {
    //   getStartForm('leave-formkey').then(response => {
    //     const route = response.data;
    //     this.$router.replace(route);
    //   });
    // }
  }
};
</script>
