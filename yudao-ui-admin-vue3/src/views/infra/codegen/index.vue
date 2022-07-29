<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import * as CodegenApi from '@/api/infra/codegen'
import { useTable } from '@/hooks/web/useTable'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { allSchemas } from './codegen.data'
import { useI18n } from '@/hooks/web/useI18n'
import { ImportTable, Preview } from './components'
import download from '@/utils/download'
import { useRouter } from 'vue-router'
import { useMessage } from '@/hooks/web/useMessage'
const message = useMessage()
const { t } = useI18n() // 国际化
const { push } = useRouter()
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<CodegenTableVO>({
  getListApi: CodegenApi.getCodegenTablePageApi,
  delListApi: CodegenApi.deleteCodegenTableApi
})
const { getList, setSearchParams, delList } = methods
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
const handleEditTable = (row: CodegenTableVO) => {
  push('/codegen/edit?id=' + row.id)
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
// 查询操作
const handleQuery = () => {
  getList()
}
// ========== 初始化 ==========
getList()
</script>
<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" v-hasPermi="['infra:codegen:create']" @click="openImportTable">
        <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.import') }}
      </el-button>
    </div>
    <!-- 列表 -->
    <Table
      :columns="allSchemas.tableColumns"
      :selection="false"
      :data="tableObject.tableList"
      :loading="tableObject.loading"
      :pagination="{
        total: tableObject.total
      }"
      v-model:pageSize="tableObject.pageSize"
      v-model:currentPage="tableObject.currentPage"
      @register="register"
    >
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #updateTime="{ row }">
        <span>{{ dayjs(row.updateTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:codegen:preview']"
          @click="handlePreview(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.preview') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:codegen:update']"
          @click="handleEditTable(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:codegen:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:codegen:update']"
          @click="handleSynchDb(row)"
        >
          <Icon icon="ep:refresh" class="mr-1px" /> {{ t('action.sync') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:codegen:download']"
          @click="handleGenTable(row)"
        >
          <Icon icon="ep:download" class="mr-1px" /> {{ t('action.generate') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <ImportTable ref="importRef" @ok="handleQuery" />
  <Preview ref="previewRef" />
</template>
