<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <!-- 操作：新增 -->
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:sms-channel:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:sms-channel:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:sms-channel:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:sms-channel:delete']"
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>

  <XModal id="smsChannel" v-model="dialogVisible" :title="dialogTitle">
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
      :data="detailData"
    />
    <!-- 操作按钮 -->
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
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="SmsChannel">
import type { FormExpose } from '@/components/Form'
// 业务相关的 import
import * as SmsChannelApi from '@/api/system/sms/smsChannel'
import { rules, allSchemas } from './sms.channel.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 列表相关的变量
const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: SmsChannelApi.getSmsChannelPageApi,
  deleteApi: SmsChannelApi.deleteSmsChannelApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await SmsChannelApi.getSmsChannelApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await SmsChannelApi.getSmsChannelApi(rowId)
  detailData.value = res
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
        const data = unref(formRef)?.formModel as SmsChannelApi.SmsChannelVO
        if (actionType.value === 'create') {
          await SmsChannelApi.createSmsChannelApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await SmsChannelApi.updateSmsChannelApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        // 刷新列表
        await reload()
      }
    }
  })
}
</script>
