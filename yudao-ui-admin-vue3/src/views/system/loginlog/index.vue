<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          @click="handleExport()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
      </template>
    </vxe-grid>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="postModel" v-model="dialogVisible" :title="dialogTitle">
    <template #default>
      <!-- 表单：详情 -->
      <Descriptions :schema="allSchemas.detailSchema" :data="detailRef" />
    </template>
    <template #footer>
      <!-- 按钮：关闭 -->
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts">
// 全局相关的 import
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
// 业务相关的 import
import { allSchemas } from './loginLog.data'
import { getLoginLogPageApi, exportLoginLogApi, LoginLogVO } from '@/api/system/loginLog'
import download from '@/utils/download'

const { t } = useI18n() // 国际化
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions } = useVxeGrid<LoginLogVO>({
  allSchemas: allSchemas,
  getListApi: getLoginLogPageApi
})

// 详情操作
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref(t('action.detail')) // 弹出层标题
// 详情
const handleDetail = async (row: LoginLogVO) => {
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
  const res = await exportLoginLogApi(queryParams)
  download.excel(res, '登录列表.xls')
}
</script>
