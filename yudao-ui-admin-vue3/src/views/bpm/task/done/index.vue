<script setup lang="ts">
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import type { TaskDoneVO } from '@/api/bpm/task/types'
import { allSchemas } from './done.data'
import * as TaskDoneApi from '@/api/bpm/task'
import { useRouter } from 'vue-router'
dayjs.extend(duration)
const { t } = useI18n() // 国际化
const { push } = useRouter()
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<TaskDoneVO>({
  getListApi: TaskDoneApi.getDoneTaskPage
})
const { getList, setSearchParams } = methods

// 审批操作
const handleAudit = async (row: TaskDoneVO) => {
  push('/bpm/process-instance/detail?id=' + row.processInstance.id)
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
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #endTime="{ row }">
        <span>{{ dayjs(row.endTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #durationInMillis="{ row }">
        <span>{{ dayjs.duration(row.durationInMillis).asMilliseconds() }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['bpm:task:query']" @click="handleAudit(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
</template>
