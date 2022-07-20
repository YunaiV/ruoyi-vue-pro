<script setup lang="ts">
import dayjs from 'dayjs'
import { useTable } from '@/hooks/web/useTable'
import { allSchemas } from './token.data'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import type { OAuth2TokenVo } from '@/api/system/oauth2/token.types'
import * as TokenApi from '@/api/system/oauth2/token'
import { ref } from 'vue'
const { t } = useI18n() // 国际化
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<OAuth2TokenVo>({
  getListApi: TokenApi.getAccessTokenPageApi,
  delListApi: TokenApi.deleteAccessTokenApi
})
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref(t('action.detail')) // 弹出层标题
const { getList, setSearchParams, delList } = methods
// 详情
const handleDetail = (row: OAuth2TokenVo) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}
// 强退操作
const handleForceLogout = (row: OAuth2TokenVo) => {
  delList(row.id, false)
}
getList()
</script>
<template>
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <Table
      :columns="allSchemas.tableColumns"
      :selection="false"
      :data="tableObject.tableList"
      :loading="tableObject.loading"
      :pagination="{
        total: tableObject.total
      }"
      v-model:pageSize="tableObject.pageSize"
      v-model:currentPage="tableObject.currentPage"
      @register="register"
    >
      <template #userType="{ row }">
        <DictTag :type="DICT_TYPE.USER_TYPE" :value="row.userType" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #expiresTime="{ row }">
        <span>{{ dayjs(row.expiresTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:oauth2-token:delete']"
          @click="handleForceLogout(row)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.logout') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #userType="{ row }">
        <DictTag :type="DICT_TYPE.USER_TYPE" :value="row.userType" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #expiresTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
