<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
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
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败(' + row.resultMsg + ')' }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="ApiAccessLog">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { allSchemas } from './apiAccessLog.data'
import * as ApiAccessLogApi from '@/api/infra/apiAccessLog'
const { t } = useI18n() // 国际化

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions } = useVxeGrid<ApiAccessLogApi.ApiAccessLogVO>({
  allSchemas: allSchemas,
  topActionSlots: false,
  getListApi: ApiAccessLogApi.getApiAccessLogPageApi
})
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// 详情操作
const handleDetail = (row: ApiAccessLogApi.ApiAccessLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
</script>
