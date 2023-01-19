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
          @click="exportList('短信日志.xls')"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
      </template>
    </XTable>
  </ContentWrap>

  <XModal id="smsLog" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    />
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="SmsLog">
import { allSchemas } from './sms.log.data'
import * as SmsLoglApi from '@/api/system/sms/smsLog'
const { t } = useI18n() // 国际化

// 列表相关的变量
const [registerTable, { exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: SmsLoglApi.getSmsLogPageApi,
  exportListApi: SmsLoglApi.exportSmsLogApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
// ========== 详情相关 ==========
const detailData = ref() // 详情 Ref
const handleDetail = (row: SmsLoglApi.SmsLogVO) => {
  // 设置数据
  detailData.value = row
  dialogVisible.value = true
}
</script>
