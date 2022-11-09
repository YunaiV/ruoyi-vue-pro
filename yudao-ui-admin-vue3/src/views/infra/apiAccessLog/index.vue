<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import type { ApiAccessLogVO } from '@/api/infra/apiAccessLog/types'
import { allSchemas } from './apiAccessLog.data'
import * as ApiAccessLogApi from '@/api/infra/apiAccessLog'
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<ApiAccessLogVO>({
  getListApi: ApiAccessLogApi.getApiAccessLogPageApi
})
const { getList, setSearchParams } = methods

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// 详情操作
const handleDetail = (row: ApiAccessLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
// ========== 初始化 ==========
getList()
</script>

<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 列表 -->
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
      <template #beginTime="{ row }">
        <span>{{ dayjs(row.beginTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败(' + row.resultMsg + ')' }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:api-access-log:query']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
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
      <template #beginTime="{ row }">
        <span>{{ dayjs(row.beginTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败(' + row.resultMsg + ')' }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
