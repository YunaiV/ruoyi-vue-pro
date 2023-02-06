<template>
  <ContentWrap>
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：发起请假 -->
        <XButton type="primary" preIcon="ep:plus" title="发起请假" @click="handleCreate()" />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作: 取消请假 -->
        <XTextButton
          preIcon="ep:delete"
          title="取消请假"
          v-hasPermi="['bpm:oa-leave:create']"
          v-if="row.result === 1"
          @click="cancelLeave(row)"
        />
        <!-- 操作: 详情 -->
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
        <!-- 操作: 审批进度 -->
        <XTextButton preIcon="ep:edit-pen" title="审批进度" @click="handleProcessDetail(row)" />
      </template>
    </XTable>
  </ContentWrap>
</template>

<script setup lang="ts">
// 全局相关的 import
import { ElMessageBox } from 'element-plus'
// 业务相关的 import
import { allSchemas } from './leave.data'
import * as LeaveApi from '@/api/bpm/leave'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter() // 路由

const [registerTable, { reload }] = useXTable({
  allSchemas: allSchemas,
  getListApi: LeaveApi.getLeavePageApi
})

// 发起请假
const handleCreate = () => {
  router.push({
    name: 'OALeaveCreate'
  })
}

// 取消请假
const cancelLeave = (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消流程', {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    inputPattern: /^[\s\S]*.*\S[\s\S]*$/, // 判断非空，且非空格
    inputErrorMessage: '取消原因不能为空'
  }).then(async ({ value }) => {
    await ProcessInstanceApi.cancelProcessInstanceApi(row.id, value)
    message.success('取消成功')
    reload()
  })
}

// 详情
const handleDetail = (row) => {
  router.push({
    name: 'OALeaveDetail',
    query: {
      id: row.id
    }
  })
}

// 审批进度
const handleProcessDetail = (row) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId
    }
  })
}
</script>
