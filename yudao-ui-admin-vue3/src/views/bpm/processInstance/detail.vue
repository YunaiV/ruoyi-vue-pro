<template>
  <ContentWrap>
    <!-- TODO 审批信息 -->

    <!-- 申请信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <template #header>
        <span class="el-icon-document">申请信息【{{ processInstance.name }}】</span>
      </template>
      <!-- 情况一：流程表单 -->
      <el-col v-if="processInstance?.processDefinition?.formType === 10" :span="16" :offset="6">
        <form-create
          :rule="detailForm.rule"
          v-model:api="fApi"
          :option="detailForm.option"
          v-model="detailForm.value"
        />
      </el-col>
      <!-- 情况二：流程表单 -->
      <div v-if="processInstance?.processDefinition?.formType === 20">
        <router-link
          :to="
            processInstance.processDefinition.formCustomViewPath +
            '?id=' +
            processInstance.businessKey
          "
        >
          <XButton type="primary" preIcon="ep:view" title="点击查看" />
        </router-link>
      </div>
    </el-card>

    <!-- 审批记录 -->
    <el-card class="box-card" v-loading="tasksLoad">
      <template #header>
        <span class="el-icon-picture-outline">审批记录</span>
      </template>
      <el-col :span="16" :offset="4">
        <div class="block">
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in tasks"
              :key="index"
              :icon="getTimelineItemIcon(item)"
              :type="getTimelineItemType(item)"
            >
              <p style="font-weight: 700">任务：{{ item.name }}</p>
              <el-card :body-style="{ padding: '10px' }">
                <label v-if="item.assigneeUser" style="font-weight: normal; margin-right: 30px">
                  审批人：{{ item.assigneeUser.nickname }}
                  <el-tag type="info" size="mini">{{ item.assigneeUser.deptName }}</el-tag>
                </label>
                <label style="font-weight: normal" v-if="item.createTime">创建时间：</label>
                <label style="color: #8a909c; font-weight: normal">
                  {{ dayjs(item?.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </label>
                <label v-if="item.endTime" style="margin-left: 30px; font-weight: normal">
                  审批时间：
                </label>
                <label v-if="item.endTime" style="color: #8a909c; font-weight: normal">
                  {{ dayjs(item?.endTime).format('YYYY-MM-DD HH:mm:ss') }}
                </label>
                <label v-if="item.durationInMillis" style="margin-left: 30px; font-weight: normal">
                  耗时：
                </label>
                <label v-if="item.durationInMillis" style="color: #8a909c; font-weight: normal">
                  {{ formatPast2(item?.durationInMillis) }}
                </label>
                <p v-if="item.reason">
                  <el-tag :type="getTimelineItemType(item)">{{ item.reason }}</el-tag>
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-card>
  </ContentWrap>
</template>
<script setup lang="ts">
import dayjs from 'dayjs'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import * as TaskApi from '@/api/bpm/task'
import { formatPast2 } from '@/utils/formatTime'

const { query } = useRoute() // 查询参数
const message = useMessage() // 消息弹窗

// ========== 审批信息 ==========
const id = query.id as unknown as number
const processInstanceLoading = ref(false) // 流程实例的加载中
const processInstance = ref({}) // 流程实例
const runningTasks = ref([]) // 运行中的任务
const auditForms = ref([]) // 审批任务的表单
// const auditRule = reactive({
//   reason: [{ required: true, message: '审批建议不能为空', trigger: 'blur' }]
// })

// ========== 申请信息 ==========
import { setConfAndFields2 } from '@/utils/formCreate'
import { ApiAttrs } from '@form-create/element-ui/types/config'
const fApi = ref<ApiAttrs>()
// 流程表单详情
const detailForm = ref({
  rule: [],
  option: {},
  value: {}
})

// ========== 审批记录 ==========
const tasksLoad = ref(true)
const tasks = ref([])

const getTimelineItemIcon = (item) => {
  if (item.result === 1) {
    return 'el-icon-time'
  }
  if (item.result === 2) {
    return 'el-icon-check'
  }
  if (item.result === 3) {
    return 'el-icon-close'
  }
  if (item.result === 4) {
    return 'el-icon-remove-outline'
  }
  return ''
}
const getTimelineItemType = (item) => {
  if (item.result === 1) {
    return 'primary'
  }
  if (item.result === 2) {
    return 'success'
  }
  if (item.result === 3) {
    return 'danger'
  }
  if (item.result === 4) {
    return 'info'
  }
  return ''
}

// ========== 初始化 ==========
onMounted(() => {
  // 1. 获得流程实例相关
  processInstanceLoading.value = true
  ProcessInstanceApi.getProcessInstanceApi(id)
    .then((data) => {
      if (!data) {
        message.error('查询不到流程信息！')
        return
      }
      processInstance.value = data

      // 设置表单信息
      const processDefinition = data.processDefinition
      if (processDefinition.formType === 10) {
        setConfAndFields2(
          detailForm,
          processDefinition.formConf,
          processDefinition.formFields,
          data.formVariables
        )
        nextTick().then(() => {
          fApi.value.btn.show(false)
          fApi.value.resetBtn.show(false)
        })
      }

      // TODO 加载流程图

      // TODO 加载活动列表
    })
    .finally(() => {
      processInstanceLoading.value = false
    })

  // 2. 获得流程任务列表（审批记录）
  tasksLoad.value = true
  runningTasks.value = []
  auditForms.value = []
  TaskApi.getTaskListByProcessInstanceId(id)
    .then((data) => {
      // 审批记录
      tasks.value = []
      // 移除已取消的审批
      data.forEach((task) => {
        if (task.result !== 4) {
          tasks.value.push(task)
        }
      })
      // 排序，将未完成的排在前面，已完成的排在后面；
      tasks.value.sort((a, b) => {
        // 有已完成的情况，按照完成时间倒序
        if (a.endTime && b.endTime) {
          return b.endTime - a.endTime
        } else if (a.endTime) {
          return 1
        } else if (b.endTime) {
          return -1
          // 都是未完成，按照创建时间倒序
        } else {
          return b.createTime - a.createTime
        }
      })

      // 需要审核的记录
      // const userId = store.getters.userId
      // this.tasks.forEach(task => {
      //   if (task.result !== 1) { // 只有待处理才需要
      //     return
      //   }
      //   if (!task.assigneeUser || task.assigneeUser.id !== userId) { // 自己不是处理人
      //     return
      //   }
      //   this.runningTasks.push({ ...task })
      //   this.auditForms.push({
      //     reason: ''
      //   })
      // })
    })
    .finally(() => {
      tasksLoad.value = false
    })
})
</script>

<style lang="scss">
.my-process-designer {
  height: calc(100vh - 200px);
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}
</style>
