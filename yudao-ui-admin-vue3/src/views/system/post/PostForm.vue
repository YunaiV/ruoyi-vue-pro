<template>
  <!-- 弹窗 -->
  <XModal id="PostForm" :loading="modelLoading" v-model="modelVisible" :title="modelTitle">
    <!-- 表单：添加/修改 -->
    <Form
      ref="formRef"
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
    />
    <!-- 表单：详情 -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    />
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="modelVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts">
import type { FormExpose } from '@/components/Form'
import * as PostApi from '@/api/system/post'
import { rules, allSchemas } from './post.data'
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const emit = defineEmits(['success'])

// 弹窗相关的变量
const modelVisible = ref(false) // 是否显示弹出层
const modelTitle = ref('edit') // 弹出层标题
const modelLoading = ref(false) // 弹出层loading
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref

const openModal = async (type: string, rowId?: number) => {
  modelLoading.value = true
  modelTitle.value = t('action.' + type)
  actionType.value = type
  modelVisible.value = true
  // 设置数据
  if (rowId) {
    const res = await PostApi.getPostApi(rowId)
    if (type === 'update') {
      unref(formRef)?.setValues(res)
    } else if (type === 'detail') {
      detailData.value = res
    }
  }
  modelLoading.value = false
}

// 提交新增/修改的表单
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as PostApi.PostVO
        if (actionType.value === 'create') {
          await PostApi.createPostApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await PostApi.updatePostApi(data)
          message.success(t('common.updateSuccess'))
        }
        modelVisible.value = false
        emit('success')
      } finally {
        actionLoading.value = false
      }
    }
  })
}

defineExpose({ openModal: openModal })
</script>
