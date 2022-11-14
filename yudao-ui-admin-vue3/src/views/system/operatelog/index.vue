<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <!-- 操作：新增 -->
      <template #toolbar_buttons>
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
  <Dialog id="postModel" v-model="dialogVisible" :title="dialogTitle">
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
  </Dialog>
</template>
<script setup lang="ts">
// 全局相关的 import
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
// 业务相关的 import
import * as OperateLogApi from '@/api/system/operatelog'
import { allSchemas } from './operatelog.data'
import download from '@/utils/download'

const { t } = useI18n() // 国际化
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions } = useVxeGrid<OperateLogApi.OperateLogVO>({
  allSchemas: allSchemas,
  getListApi: OperateLogApi.getOperateLogPageApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionLoading = ref(false) // 按钮 Loading
const detailRef = ref() // 详情 Ref
// 详情
const handleDetail = (row: OperateLogApi.OperateLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}

// 导出操作
// TODO @星语：导出需要有二次确认哈
const handleExport = async () => {
  const queryParams = Object.assign(
    {},
    JSON.parse(JSON.stringify(xGrid.value?.getRefMaps().refForm.value.data)) // TODO @星语：这个有没办法，封装个 util 获取哈？
  )
  const res = await OperateLogApi.exportOperateLogApi(queryParams)
  download.excel(res, '岗位列表.xls')
}
</script>
