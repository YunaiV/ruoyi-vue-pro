<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          title="新建流程"
          v-hasPermi="['bpm:process-instance:query']"
          @click="handleCreate"
        />
      </template>
      <!-- 当前审批任务 -->
      <template #tasks_default="{ row }">
        <el-button
          v-for="task in row.tasks"
          :key="task.id"
          type="text"
          @click="handleFormDetail(task.id)"
        >
          <span>{{ task.name }}</span>
        </el-button>
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }">
        <XButton
          type="primary"
          title="取消"
          v-if="row.result === 1"
          preIcon="ep:delete"
          @click="handleCancel(row)"
        />
        <XButton type="primary" title="详情" preIcon="ep:edit-pen" @click="handleDetail(row)" />
      </template>
    </XTable>
  </ContentWrap>
</template>
<script setup lang="ts">
// 全局相关的 import
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useXTable } from '@/hooks/web/useXTable'
import { useRouter } from 'vue-router'

// 业务相关的 import
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
// import { decodeFields } from '@/utils/formGenerator' // TODO 芋艿：后续根据情况清理
import { allSchemas } from './process.data'

const router = useRouter() // 路由

// ========== 列表相关 ==========
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  getListApi: ProcessInstanceApi.getMyProcessInstancePageApi
})
// 流程表单的弹出框
const detailOpen = ref(false)
const detailForm = ref()
/** 发起流程操作 **/
const handleCreate = () => {
  router.push({
    name: 'BpmProcessInstanceCreate'
  })
}
/** 流程表单的详情按钮操作 */
const handleFormDetail = (row) => {
  // 情况一：使用流程表单
  if (row.formId) {
    // 设置值 TODO 芋艿：动态表单做完后，需要测试下
    detailForm.value = {
      ...JSON.parse(row.formConf),
      fields: decodeFields([], row.formFields)
    }
    // 弹窗打开
    detailOpen.value = true
    // 情况二：使用业务表单
  } else if (row.formCustomCreatePath) {
    router.push({ path: row.formCustomCreatePath })
  }
}

// 列表操作
const handleDetail = (row) => {
  console.log(row, 'row')
  router.push({ path: '/process-instance/detail', query: { id: row.id } })
}
/** 取消按钮操作 */
const handleCancel = (row) => {
  const id = row.id
  ElMessageBox.prompt('请输入取消原因？', '取消流程', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[\s\S]*.*\S[\s\S]*$/, // 判断非空，且非空格
    inputErrorMessage: '取消原因不能为空'
  })
    .then(({ value }) => {
      return ProcessInstanceApi.cancelProcessInstanceApi(id, value)
    })
    .then(() => {
      ElMessage({
        message: '取消成功',
        type: 'success'
      })
    })
}
</script>
