<template>
  <ContentWrap>
    <XTable @register="registerTable">
      <template #suspensionState_default="{ row }">
        <el-tag type="success" v-if="row.suspensionState === 1">激活</el-tag>
        <el-tag type="warning" v-if="row.suspensionState === 2">挂起</el-tag>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作: 审批进度 -->
        <XTextButton preIcon="ep:view" title="详情" @click="handleAudit(row)" />
      </template>
    </XTable>
  </ContentWrap>
</template>

<script setup lang="ts">
// 业务相关的 import
import { allSchemas } from './done.data'
import * as TaskApi from '@/api/bpm/task'

const router = useRouter() // 路由

const [registerTable] = useXTable({
  allSchemas: allSchemas,
  getListApi: TaskApi.getDoneTaskPage
})

// 处理审批按钮
const handleAudit = (row) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstance.id
    }
  })
}
</script>
