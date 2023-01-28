<template>
  <ContentWrap>
    <!-- 详情 -->
    <Descriptions :schema="allSchemas.detailSchema" :data="formData" />
  </ContentWrap>
</template>

<script lang="ts" setup>
// 业务相关的 import
import * as LeaveApi from '@/api/bpm/leave'
import { allSchemas } from '@/views/bpm/oa/leave/leave.data'

const { query } = useRoute() // 查询参数
const message = useMessage() // 消息弹窗

const id = ref() // 请假编号
// 表单参数
const formData = ref({
  startTime: undefined,
  endTime: undefined,
  type: undefined,
  reason: undefined
})

onMounted(() => {
  id.value = query.id
  if (!id.value) {
    message.error('未传递 id 参数，无法查看 OA 请假信息')
    return
  }
  // 获得请假信息
  LeaveApi.getLeaveApi(id.value).then((data) => {
    formData.value = data
  })
})
</script>
