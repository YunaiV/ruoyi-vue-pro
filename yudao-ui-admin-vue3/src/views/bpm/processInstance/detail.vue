<template>
  <ContentWrap>
    <!-- TODO 审批信息 -->

    <!-- 申请信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <template #header>
        <span class="el-icon-document">申请信息【{{ processInstance.name }}】</span>
      </template>
      <!-- 情况一：流程表单 -->
      <el-col v-if="processInstance?.processDefinition?.formType === 10" :span="16" :offset="6">
        <form-create
          :rule="detailForm.rule"
          v-model:api="fApi"
          :option="detailForm.option"
          v-model="detailForm.value"
        />
      </el-col>
      <!-- 情况二：流程表单 -->
      <div v-if="processInstance?.processDefinition?.formType === 20">
        <router-link
          :to="
            processInstance.processDefinition.formCustomViewPath +
            '?id=' +
            processInstance.businessKey
          "
        >
          <el-button type="primary">点击查看</el-button>
        </router-link>
      </div>
    </el-card>
  </ContentWrap>
</template>
<script setup lang="ts">
import * as ProcessInstanceApi from '@/api/bpm/processInstance'

const { query } = useRoute() // 查询参数
const message = useMessage() // 消息弹窗

// ========== 审批信息 ==========
const id = query.id as unknown as number
const processInstanceLoading = ref(false) // 流程实例的加载中
const processInstance = ref({}) // 流程实例

// ========== 申请信息 ==========
import { setConfAndFields2 } from '@/utils/formCreate'
import { ApiAttrs } from '@form-create/element-ui/types/config'
const fApi = ref<ApiAttrs>()
// 流程表单详情
const detailForm = ref({
  rule: [],
  option: {},
  value: {}
})

// ========== 初始化 ==========
onMounted(() => {
  // 1. 获得流程实例相关
  processInstanceLoading.value = true
  ProcessInstanceApi.getProcessInstanceApi(id)
    .then((data) => {
      if (!data) {
        message.error('查询不到流程信息！')
        return
      }
      processInstance.value = data

      // 设置表单信息
      const processDefinition = data.processDefinition
      if (processDefinition.formType === 10) {
        setConfAndFields2(
          detailForm,
          processDefinition.formConf,
          processDefinition.formFields,
          data.formVariables
        )
        nextTick().then(() => {
          fApi.value.btn.show(false)
          fApi.value.resetBtn.show(false)
        })
      }

      // TODO 加载流程图

      // TODO 加载活动列表
    })
    .finally(() => {
      processInstanceLoading.value = false
    })

  // 2. TODO
})
</script>

<style lang="scss">
.my-process-designer {
  height: calc(100vh - 200px);
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}
</style>
