<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <!-- 操作：导出 -->
      <template #toolbar_buttons>
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          @click="handleExport()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
      </template>
    </vxe-grid>
  </ContentWrap>

  <XModal id="smsLog" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailRef"
    />
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="SmsLog">
// 全局相关的 import
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { allSchemas } from './sms.log.data'
import * as SmsLoglApi from '@/api/system/sms/smsLog'
const { t } = useI18n() // 国际化

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, exportList } = useVxeGrid<SmsLoglApi.SmsLogVO>({
  allSchemas: allSchemas,
  getListApi: SmsLoglApi.getSmsLogPageApi,
  exportListApi: SmsLoglApi.exportSmsLogApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const handleDetail = (row: SmsLoglApi.SmsLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}

// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '短信日志.xls')
}
</script>
