<script lang="ts" setup>
import { Crontab } from '@/components/Crontab'
import { ref, unref } from 'vue'
import DictTag from '@/components/DictTag/src/DictTag.vue'
import * as JobApi from '@/api/infra/job'
import { JobVO } from '@/api/infra/job/types'
import Icon from '@/components/Icon/src/Icon.vue'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import { rules, allSchemas } from './job.data'
import { useRouter } from 'vue-router'
import { useMessage } from '@/hooks/web/useMessage'
import { InfraJobStatusEnum } from '@/utils/constants'
const message = useMessage()
const { t } = useI18n() // 国际化
const { push } = useRouter()
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<JobVO>({
  getListApi: JobApi.getJobPageApi,
  delListApi: JobApi.deleteJobApi,
  exportListApi: JobApi.exportJobApi
})
const { getList, setSearchParams, delList, exportList } = methods

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const cronExpression = ref('')
const shortcuts = ref([
  {
    text: '每天8点和12点 (自定义追加)',
    value: '0 0 8,12 * * ?'
  }
])
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  cronExpression.value = ''
  setDialogTile('create')
  // 重置表单
  unref(formRef)?.getElFormRef()?.resetFields()
}

// 修改操作
const handleUpdate = async (row: JobVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await JobApi.getJobApi(row.id)
  cronExpression.value = res.cronExpression
  unref(formRef)?.setValues(res)
}
const handleChangeStatus = async (row: JobVO) => {
  const text = row.status === InfraJobStatusEnum.STOP ? '开启' : '关闭'
  const status =
    row.status === InfraJobStatusEnum.STOP ? InfraJobStatusEnum.NORMAL : InfraJobStatusEnum.STOP
  message
    .confirm('确认要' + text + '定时任务编号为"' + row.id + '"的数据项?', t('common.reminder'))
    .then(async () => {
      row.status =
        row.status === InfraJobStatusEnum.NORMAL
          ? InfraJobStatusEnum.NORMAL
          : InfraJobStatusEnum.STOP
      await JobApi.updateJobStatusApi(row.id, status)
      message.success(text + '成功')
      await getList()
    })
    .catch(() => {
      row.status =
        row.status === InfraJobStatusEnum.NORMAL
          ? InfraJobStatusEnum.STOP
          : InfraJobStatusEnum.NORMAL
    })
}
// 执行日志
const handleJobLog = (row: JobVO) => {
  if (row.id) {
    push('/job/job-log?id=' + row.id)
  } else {
    push('/job/job-log')
  }
}
// 执行一次
const handleRun = (row: JobVO) => {
  message.confirm('确认要立即执行一次' + row.name + '?', t('common.reminder')).then(async () => {
    await JobApi.runJobApi(row.id)
    message.success('执行成功')
    getList()
  })
}
// 提交按钮
const submitForm = async () => {
  actionLoading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as JobVO
    data.cronExpression = cronExpression.value
    if (actionType.value === 'create') {
      await JobApi.createJobApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await JobApi.updateJobApi(data)
      message.success(t('common.updateSuccess'))
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
    await getList()
  } finally {
    actionLoading.value = false
  }
}

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: JobVO) => {
  // 设置数据
  const res = JobApi.getJobApi(row.id)
  detailRef.value = res
  setDialogTile('detail')
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
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" v-hasPermi="['infra:job:create']" @click="handleCreate">
        <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
      </el-button>
      <el-button
        type="warning"
        v-hasPermi="['infra:job:export']"
        :loading="tableObject.exportLoading"
        @click="exportList('定时任务.xls')"
      >
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
      </el-button>
      <el-button type="info" v-hasPermi="['infra:job:query']" @click="handleJobLog">
        <Icon icon="ep:zoom-in" class="mr-5px" /> 执行日志
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
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_JOB_STATUS" :value="row.status" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['infra:job:update']" @click="handleUpdate(row)">
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:job:update']"
          @click="handleChangeStatus(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" />
          {{ row.status === InfraJobStatusEnum.STOP ? '开启' : '暂停' }}
        </el-button>
        <el-button link type="primary" v-hasPermi="['infra:job:query']" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:job:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
        <el-button link type="primary" v-hasPermi="['infra:job:trigger']" @click="handleRun(row)">
          <Icon icon="ep:view" class="mr-1px" /> 执行一次
        </el-button>
        <el-button link type="primary" v-hasPermi="['infra:job:query']" @click="handleJobLog(row)">
          <Icon icon="ep:view" class="mr-1px" /> 调度日志
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    >
      <template #cronExpression>
        <Crontab v-model="cronExpression" :shortcuts="shortcuts" />
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailRef"
    >
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_JOB_STATUS" :value="row.status" />
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
      <el-button
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :loading="actionLoading"
        @click="submitForm"
      >
        {{ t('action.save') }}
      </el-button>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
