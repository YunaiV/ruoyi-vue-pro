<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['infra:job:export']"
          @click="exportList('定时任务详情.xls')"
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
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData">
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
import dayjs from 'dayjs'

import * as JobLogApi from '@/api/infra/jobLog'
import { allSchemas } from './jobLog.data'

const { t } = useI18n() // 国际化
// 列表相关的变量
const [registerTable, { exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: JobLogApi.getJobLogPageApi,
  exportListApi: JobLogApi.exportJobLogApi
})
// ========== CRUD 相关 ==========
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// ========== 详情相关 ==========
const detailData = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: JobLogApi.JobLogVO) => {
  // 设置数据
  const res = JobLogApi.getJobLogApi(row.id)
  detailData.value = res
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
</script>
