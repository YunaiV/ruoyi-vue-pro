<script setup lang="ts" name="Todo">
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import type { TaskTodoVO } from '@/api/bpm/task/types'
import { allSchemas } from './done.data'
import * as TaskTodoApi from '@/api/bpm/task'
import { useRouter } from 'vue-router'
const { push } = useRouter()
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<TaskTodoVO>({
  getListApi: TaskTodoApi.getTodoTaskPage
})
const { getList, setSearchParams } = methods

// 审批操作
const handleAudit = async (row: TaskTodoVO) => {
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
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['bpm:task:update']" @click="handleAudit(row)">
          <Icon icon="ep:edit" class="mr-1px" /> 审批
        </el-button>
      </template>
    </Table>
  </ContentWrap>
</template>
