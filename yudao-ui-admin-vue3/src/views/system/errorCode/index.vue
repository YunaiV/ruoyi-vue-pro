<template>
  <ContentWrap>
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:error-code:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:error-code:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:error-code:update']"
          @click="handleDetail(row.id)"
        />
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:error-code:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal id="errorCodeModel" v-model="dialogVisible" :title="dialogTitle">
    <template #default>
      <!-- 对话框(添加 / 修改) -->
      <Form
        v-if="['create', 'update'].includes(actionType)"
        :schema="allSchemas.formSchema"
        :rules="rules"
        ref="formRef"
      />
      <!-- 对话框(详情) -->
      <Descriptions
        v-if="actionType === 'detail'"
        :schema="allSchemas.detailSchema"
        :data="detailRef"
      />
    </template>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm"
      />
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>

<script setup lang="ts">
import { ref, unref } from 'vue'
import { rules, allSchemas } from './errorCode.data'
import * as ErrorCodeApi from '@/api/system/errorCode'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮Loading
const xGrid = ref<VxeGridInstance>() // grid Ref
const formRef = ref<FormExpose>() // 表单 Ref
const detailRef = ref() // 详情 Ref

const { gridOptions } = useVxeGrid<ErrorCodeApi.ErrorCodeVO>({
  allSchemas: allSchemas,
  getListApi: ErrorCodeApi.getErrorCodePageApi
})
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  // 重置表单
  unref(formRef)?.getElFormRef()?.resetFields()
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await ErrorCodeApi.getErrorCodeApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  // 设置数据
  const res = await ErrorCodeApi.getErrorCodeApi(rowId)
  detailRef.value = res
}

// 删除操作
const handleDelete = async (rowId: number) => {
  message
    .delConfirm()
    .then(async () => {
      await ErrorCodeApi.deleteErrorCodeApi(rowId)
      message.success(t('common.delSuccess'))
    })
    .finally(() => {
      xGrid.value?.commitProxy('query')
    })
}

// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as ErrorCodeApi.ErrorCodeVO
        if (actionType.value === 'create') {
          await ErrorCodeApi.createErrorCodeApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await ErrorCodeApi.updateErrorCodeApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        xGrid.value?.commitProxy('query')
      }
    }
  })
}
</script>
