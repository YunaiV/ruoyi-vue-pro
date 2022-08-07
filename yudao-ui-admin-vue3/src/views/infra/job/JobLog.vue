<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import DictTag from '@/components/DictTag/src/DictTag.vue'
import * as JobLogApi from '@/api/infra/jobLog'
import { JobLogVO } from '@/api/infra/jobLog/types'
import Icon from '@/components/Icon/src/Icon.vue'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { useRoute } from 'vue-router'
import { allSchemas } from './jobLog.data'
const { t } = useI18n() // 国际化
const { query } = useRoute()
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<JobLogVO>({
  getListApi: JobLogApi.getJobLogPageApi,
  exportListApi: JobLogApi.exportJobLogApi
})
const { getList, setSearchParams, exportList } = methods
const getTableList = async () => {
  const id = (query.id as unknown as number) && (query.jobId as unknown as number)
  tableObject.params = {
    jobId: id
  }
  await getList()
}

// ========== CRUD 相关 ==========
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: JobLogVO) => {
  // 设置数据
  const res = JobLogApi.getJobLogApi(row.id)
  detailRef.value = res
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}

// ========== 初始化 ==========
onMounted(() => {
  getTableList()
})
</script>
<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button
        type="warning"
        v-hasPermi="['infra:job:export']"
        :loading="tableObject.exportLoading"
        @click="exportList('定时任务日志.xls')"
      >
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
      </el-button>
    </div>
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
      <template #beginTime="{ row }">
        <span>{{
          dayjs(row.beginTime).format('YYYY-MM-DD HH:mm:ss') +
          ' ~ ' +
          dayjs(row.endTime).format('YYYY-MM-DD HH:mm:ss')
        }}</span>
      </template>
      <template #duration="{ row }">
        <span>{{ row.duration + ' 毫秒' }}</span>
      </template>
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_JOB_LOG_STATUS" :value="row.status" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['infra:job:query']" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_JOB_LOG_STATUS" :value="row.status" />
      </template>
      <template #retryInterval="{ row }">
        <span>{{ row.retryInterval + '毫秒' }} </span>
      </template>
      <template #monitorTimeout="{ row }">
        <span>{{ row.monitorTimeout > 0 ? row.monitorTimeout + ' 毫秒' : '未开启' }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
