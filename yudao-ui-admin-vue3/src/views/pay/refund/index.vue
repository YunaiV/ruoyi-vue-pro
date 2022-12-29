<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['pay:refund:export']"
          @click="handleExport()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['pay:refund:query']"
          @click="handleDetail(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="t('action.detail')">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData" />
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </XModal>
</template>
<script setup lang="ts" name="Refund">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { allSchemas } from './refund.data'
import * as RefundApi from '@/api/pay/refund'

const { t } = useI18n() // 国际化

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, exportList } = useVxeGrid<RefundApi.RefundVO>({
  allSchemas: allSchemas,
  getListApi: RefundApi.getRefundPageApi,
  exportListApi: RefundApi.exportRefundApi
})

// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '退款订单.xls')
}

// ========== CRUD 相关 ==========
const dialogVisible = ref(false) // 是否显示弹出层
const detailData = ref() // 详情 Ref

// 详情操作
const handleDetail = async (rowId: number) => {
  // 设置数据
  detailData.value = RefundApi.getRefundApi(rowId)
  dialogVisible.value = true
}
</script>
