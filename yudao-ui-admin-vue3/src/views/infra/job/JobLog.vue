<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['infra:job:export']"
          @click="handleExport()"
        />
      </template>
      <template #beginTime_default="{ row }">
        <span>{{
          dayjs(row.beginTime).format('YYYY-MM-DD HH:mm:ss') +
          ' ~ ' +
          dayjs(row.endTime).format('YYYY-MM-DD HH:mm:ss')
        }}</span>
      </template>
      <template #duration_default="{ row }">
        <span>{{ row.duration + ' 毫秒' }}</span>
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['infra:job:query']"
          @click="handleDetail(row)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #retryInterval="{ row }">
        <span>{{ row.retryInterval + '毫秒' }} </span>
      </template>
      <template #monitorTimeout="{ row }">
        <span>{{ row.monitorTimeout > 0 ? row.monitorTimeout + ' 毫秒' : '未开启' }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="JobLog">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import * as JobLogApi from '@/api/infra/jobLog'
import { allSchemas } from './jobLog.data'

const { t } = useI18n() // 国际化
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, exportList } = useVxeGrid<JobLogApi.JobLogVO>({
  allSchemas: allSchemas,
  getListApi: JobLogApi.getJobLogPageApi,
  exportListApi: JobLogApi.exportJobLogApi
})
// ========== CRUD 相关 ==========
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: JobLogApi.JobLogVO) => {
  // 设置数据
  const res = JobLogApi.getJobLogApi(row.id)
  detailRef.value = res
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '定时任务详情.xls')
}
</script>
