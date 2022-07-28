<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import type { ApiErrorLogVO } from '@/api/infra/apiErrorLog/types'
import { allSchemas } from './apiErrorLog.data'
import * as ApiErrorLogApi from '@/api/infra/apiErrorLog'
import { InfraApiErrorLogProcessStatusEnum } from '@/utils/constants'
import { useMessage } from '@/hooks/web/useMessage'
const message = useMessage()
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<ApiErrorLogVO>({
  getListApi: ApiErrorLogApi.getApiErrorLogPageApi,
  exportListApi: ApiErrorLogApi.exportApiErrorLogApi
})
const { getList, setSearchParams, exportList } = methods

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题

// 详情操作
const handleDetail = (row: ApiErrorLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
// 异常处理操作
const handleProcessClick = (row: ApiErrorLogVO, processSttatus: number, type: string) => {
  message
    .confirm('确认标记为' + type + '?', t('common.reminder'))
    .then(async () => {
      ApiErrorLogApi.updateApiErrorLogPageApi(row.id, processSttatus).then(() => {
        message.success(t('common.updateSuccess'))
        getList()
      })
    })
    .catch(() => {})
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
    <el-button v-hasPermi="['infra:api-error-log:export']" @click="exportList('错误数据.xls')">
      <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
    </el-button>
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
      <template #processStatus="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS" :value="row.processStatus" />
      </template>
      <template #exceptionTime="{ row }">
        <span>{{ dayjs(row.exceptionTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #processTime="{ row }">
        <span v-if="row.processTime">{{
          dayjs(row.processTime).format('YYYY-MM-DD HH:mm:ss')
        }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:api-error-log:export']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-if="row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT"
          v-hasPermi="['infra:api-error-log:update-status']"
          @click="handleProcessClick(row, InfraApiErrorLogProcessStatusEnum.DONE, '已处理')"
        >
          <Icon icon="ep:cpu" class="mr-1px" /> 已处理
        </el-button>
        <el-button
          link
          type="primary"
          v-if="row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT"
          v-hasPermi="['infra:api-error-log:update-status']"
          @click="handleProcessClick(row, InfraApiErrorLogProcessStatusEnum.IGNORE, '已忽略')"
        >
          <Icon icon="ep:mute-notification" class="mr-1px" /> 已忽略
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
      <template #processStatus="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS" :value="row.processStatus" />
      </template>
      <template #exceptionTime="{ row }">
        <span>{{ dayjs(row.exceptionTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
