<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
        <!-- 操作：登出 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.logout')"
          v-hasPermi="['system:oauth2-token:delete']"
          @click="handleForceLogout(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData" />
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="Token">
import { allSchemas } from './token.data'
import * as TokenApi from '@/api/system/oauth2/token'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const [registerTable, { reload }] = useXTable({
  allSchemas: allSchemas,
  topActionSlots: false,
  getListApi: TokenApi.getAccessTokenPageApi
})

// ========== 详情相关 ==========
const detailData = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref(t('action.detail')) // 弹出层标题
// 详情
const handleDetail = async (row: TokenApi.OAuth2TokenVO) => {
  // 设置数据
  detailData.value = row
  dialogVisible.value = true
}

// 强退操作
const handleForceLogout = (rowId: number) => {
  message
    .confirm('是否要强制退出用户')
    .then(async () => {
      await TokenApi.deleteAccessTokenApi(rowId)
      message.success(t('common.success'))
    })
    .finally(async () => {
      // 刷新列表
      await reload()
    })
}
</script>
