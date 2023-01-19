<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <!-- 操作：导出 -->
      <template #toolbar_buttons>
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          @click="exportList('错误数据.xls')"
        />
      </template>
      <template #duration_default="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode_default="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败(' + row.resultMsg + ')' }}</span>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['infra:api-access-log:query']"
          @click="handleDetail(row)"
        />
        <XTextButton
          preIcon="ep:cpu"
          title="已处理"
          v-if="row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT"
          v-hasPermi="['infra:api-error-log:update-status']"
          @click="handleProcessClick(row, InfraApiErrorLogProcessStatusEnum.DONE, '已处理')"
        />
        <XTextButton
          preIcon="ep:mute-notification"
          title="已忽略"
          v-if="row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT"
          v-hasPermi="['infra:api-error-log:update-status']"
          @click="handleProcessClick(row, InfraApiErrorLogProcessStatusEnum.IGNORE, '已忽略')"
        />
      </template>
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData" />
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="ApiErrorLog">
import { allSchemas } from './apiErrorLog.data'
import * as ApiErrorLogApi from '@/api/infra/apiErrorLog'
import { InfraApiErrorLogProcessStatusEnum } from '@/utils/constants'

const { t } = useI18n() // 国际化
const message = useMessage()

// ========== 列表相关 ==========
const [registerTable, { reload, exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: ApiErrorLogApi.getApiErrorLogPageApi,
  exportListApi: ApiErrorLogApi.exportApiErrorLogApi
})
// ========== 详情相关 ==========
const detailData = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// 详情操作
const handleDetail = (row: ApiErrorLogApi.ApiErrorLogVO) => {
  // 设置数据
  detailData.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}

// 异常处理操作
const handleProcessClick = (
  row: ApiErrorLogApi.ApiErrorLogVO,
  processSttatus: number,
  type: string
) => {
  message
    .confirm('确认标记为' + type + '?', t('common.reminder'))
    .then(async () => {
      await ApiErrorLogApi.updateApiErrorLogPageApi(row.id, processSttatus)
      message.success(t('common.updateSuccess'))
    })
    .finally(async () => {
      // 刷新列表
      await reload()
    })
    .catch(() => {})
}
</script>
