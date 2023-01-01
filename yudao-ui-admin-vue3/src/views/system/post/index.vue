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
          v-hasPermi="['system:post:create']"
          @click="handleCreate()"
        />
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:post:export']"
          @click="handleExport()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:post:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:post:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:post:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="postModel" :loading="modelLoading" v-model="modelVisible" :title="modelTitle">
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
<script setup lang="ts" name="Post">
// 全局相关的 import
import { ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
// 业务相关的 import
import * as PostApi from '@/api/system/post'
import { rules, allSchemas } from './post.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData, exportList } = useVxeGrid<PostApi.PostVO>({
  allSchemas: allSchemas,
  getListApi: PostApi.getPostPageApi,
  deleteApi: PostApi.deletePostApi,
  exportListApi: PostApi.exportPostApi
})
// 弹窗相关的变量
const modelVisible = ref(false) // 是否显示弹出层
const modelTitle = ref('edit') // 弹出层标题
const modelLoading = ref(false) // 弹出层loading
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  modelLoading.value = true
  modelTitle.value = t('action.' + type)
  actionType.value = type
  modelVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  modelLoading.value = false
}

// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '岗位列表.xls')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await PostApi.getPostApi(rowId)
  unref(formRef)?.setValues(res)
  modelLoading.value = false
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await PostApi.getPostApi(rowId)
  detailData.value = res
  modelLoading.value = false
}

// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
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
      } finally {
        actionLoading.value = false
        // 刷新列表
        await getList(xGrid)
      }
    }
  })
}
</script>
