<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['infra:file-config:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['infra:file-config:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['infra:file-config:query']"
          @click="handleDetail(row.id)"
        />
        <XTextButton
          preIcon="ep:flag"
          title="主配置"
          v-hasPermi="['infra:file-config:update']"
          @click="handleMaster(row)"
        />
        <XTextButton
          preIcon="ep:share"
          :title="t('action.test')"
          v-hasPermi="['infra:file-config:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['infra:file-config:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
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
<script setup lang="ts" name="FileConfig">
// 全局相关的 import
import { ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
// 业务相关的 import
import * as FileConfigApi from '@/api/infra/fileConfig'
import { rules, allSchemas } from './fileConfig.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData } = useVxeGrid<FileConfigApi.FileConfigVO>({
  allSchemas: allSchemas,
  getListApi: FileConfigApi.getFileConfigPageApi,
  deleteApi: FileConfigApi.deleteFileConfigApi
})

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
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
  const res = await FileConfigApi.getFileConfigApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  // 设置数据
  const res = await FileConfigApi.getFileConfigApi(rowId)
  detailData.value = res
}

// 主配置操作
const handleMaster = (row: FileConfigApi.FileConfigVO) => {
  message
    .confirm('是否确认修改配置【 ' + row.name + ' 】为主配置?', t('common.reminder'))
    .then(async () => {
      await FileConfigApi.updateFileConfigMasterApi(row.id)
      await getList(xGrid)
    })
}

// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
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
        const data = unref(formRef)?.formModel as FileConfigApi.FileConfigVO
        if (actionType.value === 'create') {
          await FileConfigApi.createFileConfigApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await FileConfigApi.updateFileConfigApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        await getList(xGrid)
      }
    }
  })
}
</script>
