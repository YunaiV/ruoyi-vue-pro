<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <!-- 操作：新增 -->
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:oauth2-client:create']"
          @click="handleCreate()"
        />
      </template>
      <template #authorizedGrantTypes="{ row }">
        <el-tag
          :disable-transitions="true"
          :key="index"
          v-for="(authorizedGrantType, index) in row.authorizedGrantTypes"
          :index="index"
        >
          {{ authorizedGrantType }}
        </el-tag>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:oauth2-client:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:oauth2-client:update']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:oauth2-client:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <!-- 弹窗 -->
  <Dialog id="postModel" v-model="dialogVisible" :title="dialogTitle">
    <!-- 表单：添加/修改 -->
    <Form
      ref="formRef"
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
    />
    <!-- 表单：详情 -->
    <!-- TODO @星语：展示详情时，有点小丑，可额能得看看 -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailRef"
    />
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </Dialog>
</template>
<script setup lang="ts">
// 全局相关的 import
import { ref, unref } from 'vue'
import { ElTag } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
// 业务相关的 import
import * as ClientApi from '@/api/system/oauth2/client'
import { rules, allSchemas } from './client.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions } = useVxeGrid<ClientApi.OAuth2ClientVO>({
  allSchemas: allSchemas,
  getListApi: ClientApi.getOAuth2ClientPageApi
})
// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref
const detailRef = ref() // 详情 Ref

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
  const res = await ClientApi.getOAuth2ClientApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await ClientApi.getOAuth2ClientApi(rowId)
  detailRef.value = res
}

// 删除操作
const handleDelete = async (rowId: number) => {
  message
    .delConfirm()
    .then(async () => {
      await ClientApi.deleteOAuth2ClientApi(rowId)
      message.success(t('common.delSuccess'))
    })
    .finally(() => {
      // 刷新列表
      xGrid.value?.commitProxy('query')
    })
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
        const data = unref(formRef)?.formModel as ClientApi.OAuth2ClientVO
        if (actionType.value === 'create') {
          await ClientApi.createOAuth2ClientApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await ClientApi.updateOAuth2ClientApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        // 刷新列表
        xGrid.value?.commitProxy('query')
      }
    }
  })
}
</script>
