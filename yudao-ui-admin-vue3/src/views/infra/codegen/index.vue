<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <!-- 操作：导入 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.import')"
          v-hasPermi="['infra:codegen:create']"
          @click="openImportTable()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：预览 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.preview')"
          v-hasPermi="['infra:codegen:query']"
          @click="handlePreview(row)"
        />
        <!-- 操作：编辑 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['infra:codegen:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['infra:codegen:delete']"
          @click="handleDelete(row.id)"
        />
        <!-- 操作：同步 -->
        <XTextButton
          preIcon="ep:refresh"
          :title="t('action.sync')"
          v-hasPermi="['infra:codegen:update']"
          @click="handleSynchDb(row)"
        />
        <!-- 操作：生成 -->
        <XTextButton
          preIcon="ep:download"
          :title="t('action.generate')"
          v-hasPermi="['infra:codegen:download']"
          @click="handleGenTable(row)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <!-- 弹窗：导入表 -->
  <ImportTable ref="importRef" @ok="handleQuery()" />
  <!-- 弹窗：预览代码 -->
  <Preview ref="previewRef" />
</template>
<script setup lang="ts" name="Codegen">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { VxeGridInstance } from 'vxe-table'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import download from '@/utils/download'
import * as CodegenApi from '@/api/infra/codegen'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { allSchemas } from './codegen.data'
import { ImportTable, Preview } from './components'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push } = useRouter() // 路由跳转
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData } = useVxeGrid<CodegenTableVO>({
  allSchemas: allSchemas,
  getListApi: CodegenApi.getCodegenTablePageApi,
  deleteApi: CodegenApi.deleteCodegenTableApi
})

// 导入操作
const importRef = ref()
const openImportTable = () => {
  importRef.value.show()
}
// 预览操作
const previewRef = ref()
const handlePreview = (row: CodegenTableVO) => {
  previewRef.value.show(row)
}
// 编辑操作
const handleUpdate = (rowId: number) => {
  push('/codegen/edit?id=' + rowId)
}
// 同步操作
const handleSynchDb = (row: CodegenTableVO) => {
  // 基于 DB 同步
  const tableName = row.tableName
  message
    .confirm('确认要强制同步' + tableName + '表结构吗?', t('common.reminder'))
    .then(async () => {
      await CodegenApi.syncCodegenFromDBApi(row.id)
      message.success('同步成功')
    })
}
// 生成代码操作
const handleGenTable = async (row: CodegenTableVO) => {
  const res = await CodegenApi.downloadCodegenApi(row.id)
  download.zip(res, 'codegen-' + row.className + '.zip')
}
// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
}
// 查询操作
const handleQuery = async () => {
  await getList(xGrid)
}
</script>
