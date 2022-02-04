<template>
  <div style="margin-top: 16px">
<!--    <el-form-item label="处理用户">-->
<!--      <el-select v-model="userTaskForm.assignee" @change="updateElementTask('assignee')">-->
<!--        <el-option v-for="ak in mockData" :key="'ass-' + ak" :label="`用户${ak}`" :value="`user${ak}`" />-->
<!--      </el-select>-->
<!--    </el-form-item>-->
<!--    <el-form-item label="候选用户">-->
<!--      <el-select v-model="userTaskForm.candidateUsers" multiple collapse-tags @change="updateElementTask('candidateUsers')">-->
<!--        <el-option v-for="uk in mockData" :key="'user-' + uk" :label="`用户${uk}`" :value="`user${uk}`" />-->
<!--      </el-select>-->
<!--    </el-form-item>-->
<!--    <el-form-item label="候选分组">-->
<!--      <el-select v-model="userTaskForm.candidateGroups" multiple collapse-tags @change="updateElementTask('candidateGroups')">-->
<!--        <el-option v-for="gk in mockData" :key="'ass-' + gk" :label="`分组${gk}`" :value="`group${gk}`" />-->
<!--      </el-select>-->
<!--    </el-form-item>-->
    <el-form-item label="到期时间">
      <el-input v-model="userTaskForm.dueDate" clearable @change="updateElementTask('dueDate')" />
    </el-form-item>
    <el-form-item label="跟踪时间">
      <el-input v-model="userTaskForm.followUpDate" clearable @change="updateElementTask('followUpDate')" />
    </el-form-item>
    <el-form-item label="优先级">
      <el-input v-model="userTaskForm.priority" clearable @change="updateElementTask('priority')" />
    </el-form-item>
    友情提示：任务的分配规则，使用
    <router-link target="_blank" :to="{path:'/bpm/manager/model'}"><el-link type="danger">流程模型</el-link> </router-link>
    下的【分配规则】替代，提供指定角色、部门负责人、部门成员、岗位、工作组、自定义脚本等 7 种维护的任务分配维度，更加灵活！
  </div>
</template>

<script>
export default {
  name: "UserTask",
  props: {
    id: String,
    type: String
  },
  data() {
    return {
      defaultTaskForm: {
        assignee: "",
        candidateUsers: [],
        candidateGroups: [],
        dueDate: "",
        followUpDate: "",
        priority: ""
      },
      userTaskForm: {},
      mockData: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    };
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.bpmnElement = window.bpmnInstances.bpmnElement;
        this.$nextTick(() => this.resetTaskForm());
      }
    }
  },
  methods: {
    resetTaskForm() {
      for (let key in this.defaultTaskForm) {
        let value;
        if (key === "candidateUsers" || key === "candidateGroups") {
          value = this.bpmnElement?.businessObject[key] ? this.bpmnElement.businessObject[key].split(",") : [];
        } else {
          value = this.bpmnElement?.businessObject[key] || this.defaultTaskForm[key];
        }
        this.$set(this.userTaskForm, key, value);
      }
    },
    updateElementTask(key) {
      const taskAttr = Object.create(null);
      if (key === "candidateUsers" || key === "candidateGroups") {
        taskAttr[key] = this.userTaskForm[key] && this.userTaskForm[key].length ? this.userTaskForm[key].join() : null;
      } else {
        taskAttr[key] = this.userTaskForm[key] || null;
      }
      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    }
  },
  beforeDestroy() {
    this.bpmnElement = null;
  }
};
</script>
