<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:tenant:create']"
          @click="handleCreate()"
        />
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:tenant:export']"
          @click="exportList('租户列表.xls')"
        />
      </template>
      <template #accountCount_default="{ row }">
        <el-tag> {{ row.accountCount }} </el-tag>
      </template>
      <template #packageId_default="{ row }">
        <el-tag v-if="row.packageId === 0" type="danger">系统租户</el-tag>
        <el-tag v-else type="success"> {{ getPackageName(row.packageId) }} </el-tag>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:tenant:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:tenant:update']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:tenant:delete']"
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
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
    >
      <template #packageId="{ row }">
        <el-tag v-if="row.packageId === 0" type="danger">系统租户</el-tag>
        <el-tag v-else type="success"> {{ getPackageName(row.packageId) }} </el-tag>
      </template>
    </Descriptions>
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
<script setup lang="ts" name="Tenant">
import type { FormExpose } from '@/components/Form'
import * as TenantApi from '@/api/system/tenant'
import { rules, allSchemas, tenantPackageOption } from './tenant.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const [registerTable, { reload, deleteData, exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: TenantApi.getTenantPageApi,
  deleteApi: TenantApi.deleteTenantApi,
  exportListApi: TenantApi.exportTenantApi
})

const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref
const getPackageName = (packageId: number) => {
  for (let item of tenantPackageOption) {
    if (item.value === packageId) {
      return item.label
    }
  }
  return '未知套餐'
}

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  // 重置表单
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await TenantApi.getTenantApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  // 设置数据
  const res = await TenantApi.getTenantApi(rowId)
  detailData.value = res
  setDialogTile('detail')
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
        const data = unref(formRef)?.formModel as TenantApi.TenantVO
        if (actionType.value === 'create') {
          await TenantApi.createTenantApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await TenantApi.updateTenantApi(data)
          message.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
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
