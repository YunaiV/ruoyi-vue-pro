<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['pay:refund:export']"
          @click="exportList('退款订单.xls')"
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
    </XTable>
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
import { allSchemas } from './refund.data'
import * as RefundApi from '@/api/pay/refund'

const { t } = useI18n() // 国际化

// 列表相关的变量
const [registerTable, { exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: RefundApi.getRefundPageApi,
  exportListApi: RefundApi.exportRefundApi
})

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
