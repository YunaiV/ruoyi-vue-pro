<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:operate-log:export']"
          @click="exportList('操作日志.xls')"
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
    </XTable>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="postModel" v-model="dialogVisible" :title="t('action.detail')">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData">
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
// 业务相关的 import
import * as OperateLogApi from '@/api/system/operatelog'
import { allSchemas } from './operatelog.data'

const { t } = useI18n() // 国际化
// 列表相关的变量
const [registerTable, { exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: OperateLogApi.getOperateLogPageApi,
  exportListApi: OperateLogApi.exportOperateLogApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const actionLoading = ref(false) // 按钮 Loading
const detailData = ref() // 详情 Ref
// 详情
const handleDetail = (row: OperateLogApi.OperateLogVO) => {
  // 设置数据
  detailData.value = row
  dialogVisible.value = true
}
</script>
