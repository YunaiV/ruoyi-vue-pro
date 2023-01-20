<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <!-- 流程名称 -->
      <template #name_default="{ row }">
        <XTextButton :title="row.name" @click="handleBpmnDetail(row.id)" />
      </template>
      <!-- 表单信息 -->
      <template #formId_default="{ row }">
        <XTextButton :title="row.formName" @click="handleFormDetail(row.formId)" />
      </template>
      <!-- 流程版本 -->
      <template #version_default="{ row }">
        <el-tag>v{{ row.version }}</el-tag>
      </template>
      <!-- 激活状态 -->
      <template #suspensionState_default="{ row }">
        <el-tag type="success" v-if="row.suspensionState === 1">激活</el-tag>
        <el-tag type="warning" v-if="row.suspensionState === 2">挂起</el-tag>
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:user"
          title="分配规则"
          v-hasPermi="['bpm:task-assign-rule:query']"
          @click="handleAssignRule(row)"
        />
      </template>
    </XTable>

    <!-- 表单详情的弹窗 -->
    <XModal v-model="formDetailVisible" width="800" title="表单详情" :show-footer="false">
      <ViewForm
        :rule="formDetailPreview.rule"
        :option="formDetailPreview.option"
        v-if="formDetailVisible"
      />
    </XModal>
  </ContentWrap>
</template>

<script setup lang="ts">
// 全局相关的 import
import { ref } from 'vue'
// import { DICT_TYPE, getDictOptions } from '@/utils/dict'

// 业务相关的 import
import * as DefinitionApi from '@/api/bpm/definition'
// import * as ModelApi from '@/api/bpm/model'
import { allSchemas } from './definition.data'

// const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter() // 路由

import viewForm from '@form-create/element-ui'
const ViewForm = viewForm.$form()
import { setConfAndFields2 } from '@/utils/formCreate'

// ========== 列表相关 ==========
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  getListApi: DefinitionApi.getProcessDefinitionPageApi
})

// 流程表单的详情按钮操作
const formDetailVisible = ref(false)
const formDetailPreview = ref({
  rule: [],
  option: {}
})
const handleFormDetail = async (rowId: number) => {
  // 设置表单
  const data = await FormApi.getFormApi(rowId)
  setConfAndFields2(formDetailPreview, data.conf, data.fields)
  // 弹窗打开
  formDetailVisible.value = true
}

// 流程图的详情按钮操作
const handleBpmnDetail = (row) => {
  // TODO 芋艿：流程组件开发中
  console.log(row)
  message.success('流程组件开发中，预计 2 月底完成')
}

// 点击任务分配按钮
const handleAssignRule = (row) => {
  router.push({
    name: 'BpmTaskAssignRuleList',
    query: {
      modelId: row.id
    }
  })
}
</script>
