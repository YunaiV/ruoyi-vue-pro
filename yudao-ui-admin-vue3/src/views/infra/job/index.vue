<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['infra:job:create']"
          @click="handleCreate()"
        />
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['infra:job:export']"
          @click="exportList('定时任务.xls')"
        />
        <XButton
          type="info"
          preIcon="ep:zoom-in"
          title="执行日志"
          v-hasPermi="['infra:job:query']"
          @click="handleJobLog()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['infra:job:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:edit"
          :title="row.status === InfraJobStatusEnum.STOP ? '开启' : '暂停'"
          v-hasPermi="['infra:job:update']"
          @click="handleChangeStatus(row)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['infra:job:delete']"
          @click="deleteData(row.id)"
        />
        <el-dropdown class="p-0.5" v-hasPermi="['infra:job:trigger', 'infra:job:query']">
          <XTextButton :title="t('action.more')" postIcon="ep:arrow-down" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>
                <!-- 操作：执行 -->
                <XTextButton
                  preIcon="ep:view"
                  title="执行一次"
                  v-hasPermi="['infra:job:trigger']"
                  @click="handleRun(row)"
                />
              </el-dropdown-item>
              <el-dropdown-item>
                <!-- 操作：详情 -->
                <XTextButton
                  preIcon="ep:view"
                  :title="t('action.detail')"
                  v-hasPermi="['infra:job:query']"
                  @click="handleDetail(row.id)"
                />
              </el-dropdown-item>
              <el-dropdown-item>
                <!-- 操作：日志 -->
                <XTextButton
                  preIcon="ep:view"
                  title="调度日志"
                  v-hasPermi="['infra:job:query']"
                  @click="handleJobLog(row.id)"
                />
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    >
      <template #cronExpression="form">
        <Crontab v-model="form['cronExpression']" :shortcuts="shortcuts" />
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    >
      <template #retryInterval="{ row }">
        <span>{{ row.retryInterval + '毫秒' }} </span>
      </template>
      <template #monitorTimeout="{ row }">
        <span>{{ row.monitorTimeout > 0 ? row.monitorTimeout + ' 毫秒' : '未开启' }}</span>
      </template>
      <template #nextTimes>
        <span>{{ Array.from(nextTimes, (x) => parseTime(x)).join('; ') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="Job">
import type { FormExpose } from '@/components/Form'
import * as JobApi from '@/api/infra/job'
import { rules, allSchemas } from './job.data'
import { InfraJobStatusEnum } from '@/utils/constants'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push } = useRouter()

// 列表相关的变量
const [registerTable, { reload, deleteData, exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: JobApi.getJobPageApi,
  deleteApi: JobApi.deleteJobApi,
  exportListApi: JobApi.exportJobApi
})

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref
const nextTimes = ref([])
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
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await JobApi.getJobApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  // 设置数据
  const res = await JobApi.getJobApi(rowId)
  detailData.value = res
  // 后续执行时长
  const jobNextTime = await JobApi.getJobNextTimesApi(rowId)
  nextTimes.value = jobNextTime
  setDialogTile('detail')
}

const parseTime = (time) => {
  if (!time) {
    return null
  }
  const format = '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time
        .replace(new RegExp(/-/gm), '/')
        .replace('T', ' ')
        .replace(new RegExp(/\.[\d]{3}/gm), '')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

const handleChangeStatus = async (row: JobApi.JobVO) => {
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
      await reload()
    })
    .catch(() => {
      row.status =
        row.status === InfraJobStatusEnum.NORMAL
          ? InfraJobStatusEnum.STOP
          : InfraJobStatusEnum.NORMAL
    })
}
// 执行日志
const handleJobLog = (rowId?: number) => {
  if (rowId) {
    push('/job/job-log?id=' + rowId)
  } else {
    push('/job/job-log')
  }
}
// 执行一次
const handleRun = (row: JobApi.JobVO) => {
  message.confirm('确认要立即执行一次' + row.name + '?', t('common.reminder')).then(async () => {
    await JobApi.runJobApi(row.id)
    message.success('执行成功')
    await reload()
  })
}
// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as JobApi.JobVO
        if (actionType.value === 'create') {
          await JobApi.createJobApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await JobApi.updateJobApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        await reload()
      }
    }
  })
}
</script>
