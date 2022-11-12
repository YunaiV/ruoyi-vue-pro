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
      <template #createTime_item="{ data }">
        <el-date-picker
          v-model="data.createTime"
          type="datetimerange"
          range-separator="-"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
        />
      </template>
      <template #type_default="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
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
          @click="handleDetail(row)"
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
      >
        <template #type="{ row }">
          <DictTag :type="DICT_TYPE.SYSTEM_ERROR_CODE_TYPE" :value="row.type" />
        </template>
        <template #createTime="{ row }">
          <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </template>
      </Descriptions>
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
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import type { ErrorCodeVO } from '@/api/system/errorCode/types'
import { rules, allSchemas } from './errorCode.data'
import * as ErrorCodeApi from '@/api/system/errorCode'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
import { ElDatePicker } from 'element-plus'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮Loading
const xGrid = ref<VxeGridInstance>() // grid Ref
const formRef = ref<FormExpose>() // 表单 Ref
const detailRef = ref() // 详情 Ref

const { gridOptions } = useVxeGrid<ErrorCodeVO>({
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

// 详情操作
const handleDetail = async (row: ErrorCodeVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await ErrorCodeApi.getErrorCodeApi(rowId)
  unref(formRef)?.setValues(res)
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
        const data = unref(formRef)?.formModel as ErrorCodeVO
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
