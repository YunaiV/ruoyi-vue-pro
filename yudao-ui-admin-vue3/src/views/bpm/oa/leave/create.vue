<template>
  <ContentWrap>
    <!-- 对话框(添加 / 修改) -->
    <Form :schema="allSchemas.formSchema" :rules="rules" ref="formRef" />
    <!-- 按钮：保存 -->
    <XButton
      type="primary"
      :title="t('action.save')"
      :loading="actionLoading"
      @click="submitForm"
    />
  </ContentWrap>
</template>
<script setup lang="ts">
import { FormExpose } from '@/components/Form'
// import XEUtils from 'xe-utils'

// 业务相关的 import
import * as LeaveApi from '@/api/bpm/leave'
import { rules, allSchemas } from './leave.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter() // 路由

// 表单参数
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref

// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (!valid) {
      return
    }
    try {
      // 设置提交中
      actionLoading.value = true
      const data = unref(formRef)?.formModel as LeaveApi.LeaveVO
      // data.startTime = XEUtils.toDateString(data.startTime, 'yyyy-MM-dd HH:mm:ss')
      // data.endTime = XEUtils.toDateString(data.endTime, 'yyyy-MM-dd HH:mm:ss')
      data.startTime = Date.parse(new Date(data.startTime).toString())
      data.endTime = Date.parse(new Date(data.endTime).toString())
      // 添加的提交
      await LeaveApi.createLeaveApi(data)
      message.success(t('common.createSuccess'))
      // 关闭窗口
      router.push({
        path: '/bpm/oa/leave'
      })
    } finally {
      actionLoading.value = false
    }
  })
}
</script>
