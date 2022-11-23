<template>
  <!-- 导入表 -->
  <XModal title="导入表" v-model="visible">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="数据源" prop="dataSourceConfigId">
        <el-select v-model="queryParams.dataSourceConfigId" placeholder="请选择数据源" clearable>
          <el-option
            v-for="config in dataSourceConfigs"
            :key="config.id"
            :label="config.name"
            :value="config.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="表名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入表名称" clearable />
      </el-form-item>
      <el-form-item label="表描述" prop="comment">
        <el-input v-model="queryParams.comment" placeholder="请输入表描述" clearable />
      </el-form-item>
      <el-form-item>
        <XButton
          type="primary"
          preIcon="ep:search"
          :title="t('common.query')"
          @click="handleQuery()"
        />
        <XButton preIcon="ep:refresh-right" :title="t('common.reset')" @click="resetQuery()" />
      </el-form-item>
    </el-form>
    <vxe-table
      ref="xTable"
      :data="dbTableList"
      v-loading="dbLoading"
      :checkbox-config="{ highlight: true, range: true }"
      height="260px"
      class="xtable-scrollbar"
    >
      <vxe-column type="checkbox" width="60" />
      <vxe-column field="name" title="表名称" />
      <vxe-column field="comment" title="表描述" />
    </vxe-table>
    <template #footer>
      <div class="dialog-footer">
        <XButton type="primary" :title="t('action.import')" @click="handleImportTable()" />
        <XButton :title="t('dialog.close')" @click="handleClose()" />
      </div>
    </template>
  </XModal>
</template>
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { VxeTableInstance } from 'vxe-table'
import { ElForm, ElFormItem, ElInput, ElSelect, ElOption } from 'element-plus'
import type { DatabaseTableVO } from '@/api/infra/codegen/types'
import { getSchemaTableListApi, createCodegenListApi } from '@/api/infra/codegen'
import { getDataSourceConfigListApi, DataSourceConfigVO } from '@/api/infra/dataSourceConfig'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const emit = defineEmits(['ok'])
// ======== 显示页面 ========
const visible = ref(false)
const dbLoading = ref(true)
const queryParams = reactive({
  name: undefined,
  comment: undefined,
  dataSourceConfigId: 0
})
const dataSourceConfigs = ref<DataSourceConfigVO[]>([])
const show = async () => {
  const res = await getDataSourceConfigListApi()
  dataSourceConfigs.value = res
  queryParams.dataSourceConfigId = dataSourceConfigs.value[0].id
  visible.value = true
  await getList()
}
/** 查询表数据 */
const dbTableList = ref<DatabaseTableVO[]>([])

/** 查询表数据 */
const getList = async () => {
  dbLoading.value = true
  const res = await getSchemaTableListApi(queryParams)
  dbTableList.value = res
  dbLoading.value = false
}
// 查询操作
const handleQuery = async () => {
  await getList()
}
// 重置操作
const resetQuery = async () => {
  queryParams.name = undefined
  queryParams.comment = undefined
  queryParams.dataSourceConfigId = 0
  await getList()
}
const xTable = ref<VxeTableInstance>()
/** 多选框选中数据 */
const tables = ref<string[]>([])

/** 导入按钮操作 */
const handleImportTable = async () => {
  if (xTable.value?.getCheckboxRecords().length === 0) {
    message.error('请选择要导入的表')
    return
  }
  xTable.value?.getCheckboxRecords().forEach((item) => {
    tables.value.push(item.name)
  })
  await createCodegenListApi({
    dataSourceConfigId: queryParams.dataSourceConfigId,
    tableNames: tables.value
  })
  message.success('导入成功')
  emit('ok')
  handleClose()
}
const handleClose = () => {
  visible.value = false
  tables.value = []
}
defineExpose({
  show
})
</script>
