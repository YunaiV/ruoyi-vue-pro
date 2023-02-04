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
        <el-button v-for="task in row.tasks" :key="task.id" link>
          <span>{{ task.name }}</span>
        </el-button>
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['bpm:process-instance:cancel']"
          @click="handleDetail(row)"
        />
        <XTextButton
          preIcon="ep:delete"
          title="取消"
          v-if="row.result === 1"
          v-hasPermi="['bpm:process-instance:query']"
          @click="handleCancel(row)"
        />
      </template>
    </XTable>
  </ContentWrap>
</template>
<script setup lang="ts">
// 全局相关的 import
import { ElMessageBox } from 'element-plus'

// 业务相关的 import
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { allSchemas } from './process.data'

const router = useRouter() // 路由
const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const [registerTable, { reload }] = useXTable({
  allSchemas: allSchemas,
  getListApi: ProcessInstanceApi.getMyProcessInstancePageApi
})

/** 发起流程操作 **/
const handleCreate = () => {
  router.push({
    name: 'BpmProcessInstanceCreate'
  })
}

// 列表操作
const handleDetail = (row) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.id
    }
  })
}

/** 取消按钮操作 */
const handleCancel = (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消流程', {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    inputPattern: /^[\s\S]*.*\S[\s\S]*$/, // 判断非空，且非空格
    inputErrorMessage: '取消原因不能为空'
  }).then(async ({ value }) => {
    await ProcessInstanceApi.cancelProcessInstanceApi(row.id, value)
    message.success('取消成功')
    reload()
  })
}
</script>
