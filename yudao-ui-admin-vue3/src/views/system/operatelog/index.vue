<script setup lang="ts">
import dayjs from 'dayjs'
import { useTable } from '@/hooks/web/useTable'
import { allSchemas } from './operatelog.data'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import type { OperateLogVO } from '@/api/system/operatelog/types'
import * as OperateLogApi from '@/api/system/operatelog'
import { ref } from 'vue'
const { t } = useI18n() // 国际化
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<OperateLogVO>({
  getListApi: OperateLogApi.getOperateLogPageApi,
  exportListApi: OperateLogApi.exportOperateLogApi
})
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref(t('action.detail')) // 弹出层标题
const { getList, setSearchParams, exportList } = methods
// 详情
const handleDetail = (row: OperateLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}
getList()
</script>
<template>
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button
        type="warning"
        v-hasPermi="['system:operate-log:export']"
        :loading="tableObject.exportLoading"
        @click="exportList('操作日志.xls')"
      >
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
      </el-button>
    </div>
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
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="row.type" />
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败' }}</span>
      </template>
      <template #startTime="{ row }">
        <span>{{ dayjs(row.startTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #resultCode="{ row }">
        <span>{{ row.resultCode === 0 ? '成功' : '失败' }}</span>
      </template>
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="row.type" />
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + 'ms' }}</span>
      </template>
      <template #startTime="{ row }">
        <span>{{ dayjs(row.startTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
