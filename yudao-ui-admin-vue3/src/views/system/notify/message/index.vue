<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:notify-message:query']"
          @click="handleDetail(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="messageModel" :loading="modelLoading" v-model="modelVisible" :title="modelTitle">
    <!-- 表单：详情 -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    />
    <template #footer>
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="modelVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="NotifyMessage">
// 业务相关的 import
import { allSchemas } from './message.data'
import * as NotifyMessageApi from '@/api/system/notify/message'

const { t } = useI18n() // 国际化

// 列表相关的变量
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  getListApi: NotifyMessageApi.getNotifyMessagePageApi
})

// 弹窗相关的变量
const modelVisible = ref(false) // 是否显示弹出层
const modelTitle = ref('edit') // 弹出层标题
const modelLoading = ref(false) // 弹出层loading
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  modelLoading.value = true
  modelTitle.value = t('action.' + type)
  actionType.value = type
  modelVisible.value = true
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await NotifyMessageApi.getNotifyMessageApi(rowId)
  detailData.value = res
  modelLoading.value = false
}
</script>
