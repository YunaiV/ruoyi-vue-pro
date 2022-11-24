<template>
  <ContentWrap>
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:notice:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:notice:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:notice:update']"
          @click="handleDetail(row)"
        />
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:notice:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal id="noticeModel" v-model="dialogVisible" :title="dialogTitle">
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
import * as NoticeApi from '@/api/system/notice'
import { NoticeVO } from '@/api/system/notice/types'
import { rules, allSchemas } from './notice.data'
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

const { gridOptions } = useVxeGrid<NoticeVO>({
  allSchemas: allSchemas,
  getListApi: NoticeApi.getNoticePageApi
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
  const res = await NoticeApi.getNoticeApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = (row: NoticeVO) => {
  setDialogTile('detail')
  detailRef.value = row
}

// 删除操作
const handleDelete = async (rowId: number) => {
  message
    .delConfirm()
    .then(async () => {
      await NoticeApi.deleteNoticeApi(rowId)
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
        const data = unref(formRef)?.formModel as NoticeVO
        if (actionType.value === 'create') {
          await NoticeApi.createNoticeApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await NoticeApi.updateNoticeApi(data)
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
