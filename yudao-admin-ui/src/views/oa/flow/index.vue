<template>
  <div class="app-container">
    <el-row>
      <el-button type="primary" v-on:click="leave">请假申请</el-button>
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
    leave() {
      getStartForm('leave-formkey').then(response => {
        const route = response.data;
        this.$router.replace(route);
      });
    }
  }
};
</script>
