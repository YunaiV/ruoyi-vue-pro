<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:operate-log:export']"
          @click="handleExport()"
        />
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败' }}</span>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
      </template>
    </vxe-grid>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="postModel" v-model="dialogVisible" :title="t('action.detail')">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败' }}</span>
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
    </Descriptions>
    <template #footer>
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="OperateLog">
// 全局相关的 import
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
// 业务相关的 import
import * as OperateLogApi from '@/api/system/operatelog'
import { allSchemas } from './operatelog.data'

const { t } = useI18n() // 国际化
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, exportList } = useVxeGrid<OperateLogApi.OperateLogVO>({
  allSchemas: allSchemas,
  getListApi: OperateLogApi.getOperateLogPageApi,
  exportListApi: OperateLogApi.exportOperateLogApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const actionLoading = ref(false) // 按钮 Loading
const detailRef = ref() // 详情 Ref
// 详情
const handleDetail = (row: OperateLogApi.OperateLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}

// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '岗位列表.xls')
}
</script>
