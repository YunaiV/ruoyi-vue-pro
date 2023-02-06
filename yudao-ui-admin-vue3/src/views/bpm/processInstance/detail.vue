<template>
  <ContentWrap>
    <!-- 审批信息 -->
    <el-card
      class="box-card"
      v-loading="processInstanceLoading"
      v-for="(item, index) in runningTasks"
      :key="index"
    >
      <template #header>
        <span class="el-icon-picture-outline">审批任务【{{ item.name }}】</span>
      </template>
      <el-col :span="16" :offset="6">
        <el-form
          :ref="'form' + index"
          :model="auditForms[index]"
          :rules="auditRule"
          label-width="100px"
        >
          <el-form-item label="流程名" v-if="processInstance && processInstance.name">
            {{ processInstance.name }}
          </el-form-item>
          <el-form-item label="流程发起人" v-if="processInstance && processInstance.startUser">
            {{ processInstance.startUser.nickname }}
            <el-tag type="info" size="small">{{ processInstance.startUser.deptName }}</el-tag>
          </el-form-item>
          <el-form-item label="审批建议" prop="reason">
            <el-input
              type="textarea"
              v-model="auditForms[index].reason"
              placeholder="请输入审批建议"
            />
          </el-form-item>
        </el-form>
        <div style="margin-left: 10%; margin-bottom: 20px; font-size: 14px">
          <XButton
            pre-icon="ep:select"
            type="success"
            title="通过"
            @click="handleAudit(item, true)"
          />
          <XButton
            pre-icon="ep:close"
            type="danger"
            title="不通过"
            @click="handleAudit(item, false)"
          />
          <XButton
            pre-icon="ep:edit"
            type="primary"
            title="转办"
            @click="handleUpdateAssignee(item)"
          />
          <XButton
            pre-icon="ep:position"
            type="primary"
            title="委派"
            @click="handleDelegate(item)"
          />
          <XButton pre-icon="ep:back" type="warning" title="委派" @click="handleBack(item)" />
        </div>
      </el-col>
    </el-card>

    <!-- 申请信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <template #header>
        <span class="el-icon-document">申请信息【{{ processInstance.name }}】</span>
      </template>
      <!-- 情况一：流程表单 -->
      <el-col v-if="processInstance?.processDefinition?.formType === 10" :span="16" :offset="6">
        <form-create
          ref="fApi"
          :rule="detailForm.rule"
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
                  <el-tag type="info" size="small">{{ item.assigneeUser.deptName }}</el-tag>
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

    <!-- 高亮流程图 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <template #header>
        <span class="el-icon-picture-outline">流程图</span>
      </template>
      <my-process-viewer
        key="designer"
        v-model="bpmnXML"
        :value="bpmnXML"
        v-bind="bpmnControlForm"
        :prefix="bpmnControlForm.prefix"
        :activityData="activityList"
        :processInstanceData="processInstance"
        :taskData="tasks"
      />
    </el-card>

    <!-- 对话框(转派审批人) -->
    <XModal v-model="updateAssigneeVisible" title="转派审批人" width="500">
      <el-form
        ref="updateAssigneeFormRef"
        :model="updateAssigneeForm"
        :rules="updateAssigneeRules"
        label-width="110px"
      >
        <el-form-item label="新审批人" prop="assigneeUserId">
          <el-select v-model="updateAssigneeForm.assigneeUserId" clearable style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="parseInt(item.id)"
              :label="item.nickname"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <!-- 操作按钮 -->
      <template #footer>
        <!-- 按钮：保存 -->
        <XButton
          type="primary"
          :title="t('action.save')"
          :loading="updateAssigneeLoading"
          @click="submitUpdateAssigneeForm"
        />
        <!-- 按钮：关闭 -->
        <XButton
          :loading="updateAssigneeLoading"
          :title="t('dialog.close')"
          @click="updateAssigneeLoading = false"
        />
      </template>
    </XModal>
  </ContentWrap>
</template>
<script setup lang="ts">
import dayjs from 'dayjs'
import * as UserApi from '@/api/system/user'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import * as DefinitionApi from '@/api/bpm/definition'
import * as TaskApi from '@/api/bpm/task'
import * as ActivityApi from '@/api/bpm/activity'
import { formatPast2 } from '@/utils/formatTime'
import { setConfAndFields2 } from '@/utils/formCreate'
// import { OptionAttrs } from '@form-create/element-ui/types/config'
import { ApiAttrs } from '@form-create/element-ui/types/config'
import { useUserStore } from '@/store/modules/user'

const { query } = useRoute() // 查询参数
const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const { proxy } = getCurrentInstance()

// ========== 审批信息 ==========
const id = query.id as unknown as number
const processInstanceLoading = ref(false) // 流程实例的加载中
const processInstance = ref<any>({}) // 流程实例
const runningTasks = ref<any[]>([]) // 运行中的任务
const auditForms = ref<any[]>([]) // 审批任务的表单
const auditRule = reactive({
  reason: [{ required: true, message: '审批建议不能为空', trigger: 'blur' }]
})

// 处理审批通过和不通过的操作
const handleAudit = async (task, pass) => {
  // 1.1 获得对应表单
  const index = runningTasks.value.indexOf(task)
  const auditFormRef = proxy.$refs['form' + index][0]
  // alert(auditFormRef)

  // 1.2 校验表单
  const elForm = unref(auditFormRef)
  if (!elForm) return
  const valid = await elForm.validate()
  if (!valid) return

  // 2.1 提交审批
  const data = {
    id: task.id,
    reason: auditForms.value[index].reason
  }
  if (pass) {
    await TaskApi.approveTask(data)
    message.success('审批通过成功')
  } else {
    await TaskApi.rejectTask(data)
    message.success('审批不通过成功')
  }
  // 2.2 加载最新数据
  getDetail()
}

// ========== 申请信息 ==========
const fApi = ref<ApiAttrs>()
const userId = useUserStore().getUser.id // 当前登录的编号
// 流程表单详情
const detailForm = ref({
  rule: [],
  option: {},
  value: {}
})

// ========== 审批记录 ==========
const tasksLoad = ref(true)
const tasks = ref<any[]>([])

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

// ========== 审批记录 ==========
const updateAssigneeVisible = ref(false)
const updateAssigneeLoading = ref(false)
const updateAssigneeForm = ref({
  id: undefined,
  assigneeUserId: undefined
})
const updateAssigneeRules = ref({
  assigneeUserId: [{ required: true, message: '新审批人不能为空', trigger: 'change' }]
})
const updateAssigneeFormRef = ref()
const userOptions = ref<any[]>([])

// 处理转派审批人
const handleUpdateAssignee = (task) => {
  // 设置表单
  resetUpdateAssigneeForm()
  updateAssigneeForm.value.id = task.id
  // 设置为打开
  updateAssigneeVisible.value = true
}

// 提交转派审批人
const submitUpdateAssigneeForm = async () => {
  // 1. 校验表单
  const elForm = unref(updateAssigneeFormRef)
  if (!elForm) return
  const valid = await elForm.validate()
  if (!valid) return

  // 2.1 提交审批
  updateAssigneeLoading.value = true
  try {
    await TaskApi.updateTaskAssignee(updateAssigneeForm.value)
    // 2.2 设置为隐藏
    updateAssigneeVisible.value = false
    // 加载最新数据
    getDetail()
  } finally {
    updateAssigneeLoading.value = false
  }
}

// 重置转派审批人表单
const resetUpdateAssigneeForm = () => {
  updateAssigneeForm.value = {
    id: undefined,
    assigneeUserId: undefined
  }
  updateAssigneeFormRef.value?.resetFields()
}

/** 处理审批退回的操作 */
const handleDelegate = async (task) => {
  message.error('暂不支持【委派】功能，可以使用【转派】替代！')
  console.log(task)
}

/** 处理审批退回的操作 */
const handleBack = async (task) => {
  message.error('暂不支持【退回】功能！')
  // 可参考 http://blog.wya1.com/article/636697030/details/7296
  // const data = {
  //   id: task.id,
  //   assigneeUserId: 1
  // }
  // backTask(data).then(response => {
  //   this.$modal.msgSuccess("回退成功！");
  //   this.getDetail(); // 获得最新详情
  // });
  console.log(task)
}

// ========== 高亮流程图 ==========
const bpmnXML = ref(null)
const bpmnControlForm = ref({
  prefix: 'flowable'
})
const activityList = ref([])

// ========== 初始化 ==========
onMounted(() => {
  // 加载详情
  getDetail()
  // 加载用户的列表
  UserApi.getListSimpleUsersApi().then((data) => {
    userOptions.value.push(...data)
  })
})

const getDetail = () => {
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
          fApi.value?.fapi.btn.show(false)
          fApi.value?.fapi.resetBtn.show(false)
          fApi.value?.fapi.disabled(true)
        })
      }

      // 加载流程图
      DefinitionApi.getProcessDefinitionBpmnXMLApi(processDefinition.id).then((data) => {
        bpmnXML.value = data
      })

      // 加载活动列表
      ActivityApi.getActivityList({
        processInstanceId: data.id
      }).then((data) => {
        activityList.value = data
      })
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
      tasks.value.forEach((task) => {
        // 1.1 只有待处理才需要
        if (task.result !== 1) {
          return
        }
        // 1.2 自己不是处理人
        if (!task.assigneeUser || task.assigneeUser.id !== userId) {
          return
        }
        // 2. 添加到处理任务
        runningTasks.value.push({ ...task })
        auditForms.value.push({
          reason: ''
        })
      })
    })
    .finally(() => {
      tasksLoad.value = false
    })
}
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
