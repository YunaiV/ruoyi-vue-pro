<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
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
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData">
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
import { allSchemas } from './apiAccessLog.data'
import * as ApiAccessLogApi from '@/api/infra/apiAccessLog'

const { t } = useI18n() // 国际化

// 列表相关的变量
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  topActionSlots: false,
  getListApi: ApiAccessLogApi.getApiAccessLogPageApi
})
// ========== 详情相关 ==========
const detailData = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// 详情操作
const handleDetail = (row: ApiAccessLogApi.ApiAccessLogVO) => {
  // 设置数据
  detailData.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
</script>
