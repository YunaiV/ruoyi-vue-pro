<script setup lang="ts" name="ProcessInstance">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import type { ProcessInstanceVO } from '@/api/bpm/processInstance/types'
import { allSchemas } from './process.data'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { ElMessage, ElMessageBox } from 'element-plus'
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<ProcessInstanceVO>({
  getListApi: ProcessInstanceApi.getMyProcessInstancePageApi
})
const { getList, setSearchParams } = methods

// ========== CRUD 相关 ==========
const dialogVisible = ref(false) // 是否显示弹出层
// 发起流程
const handleAdd = () => {
  console.info('add')
}
// 取消操作
const handleCancel = (row: ProcessInstanceVO) => {
  ElMessageBox.prompt('请输入取消原因？', '取消流程', {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    type: 'warning',
    inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/, // 判断非空，且非空格
    inputErrorMessage: '取消原因不能为空'
  }).then(async ({ value }) => {
    await ProcessInstanceApi.cancelProcessInstanceApi(row.id, value)
    ElMessage.success('取消成功')
    getList()
  })
}

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: ProcessInstanceVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
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
      <el-button type="primary" v-hasPermi="['bpm:process-instance:query']" @click="handleAdd">
        <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
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
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:process-instance:query']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:process-instance:cancel']"
          @click="handleCancel(row)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="t('action.detail')">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef" />
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </XModal>
</template>
