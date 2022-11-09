<script setup lang="ts">
import { ref, unref } from 'vue'
import dayjs from 'dayjs'
import { ElTableColumn, ElTag, ElSwitch } from 'element-plus'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import type { ModelVO } from '@/api/bpm/model/types'
import { rules, allSchemas } from './model.data'
import * as ModelApi from '@/api/bpm/model'
import { useMessage } from '@/hooks/web/useMessage'
const message = useMessage()
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<ModelVO>({
  getListApi: ModelApi.getModelPageApi,
  delListApi: ModelApi.deleteModelApi
})
const { getList, setSearchParams, delList } = methods

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  // 重置表单
  unref(formRef)?.getElFormRef()?.resetFields()
}

// 修改操作
const handleUpdate = async (row: ModelVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await ModelApi.getModelApi(row.id)
  unref(formRef)?.setValues(res)
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
        const data = unref(formRef)?.formModel as ModelVO
        if (actionType.value === 'create') {
          await ModelApi.createModelApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await ModelApi.updateModelApi(data)
          message.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
        await getList()
      } finally {
        actionLoading.value = false
      }
    }
  })
}

/** 流程表单的详情按钮操作 */
const handleChangeState = async (row: ModelVO) => {
  const state = row.processDefinition.suspensionState
  const statusState = state === 1 ? '激活' : '挂起'
  message
    .confirm(
      '是否确认' + statusState + '流程名字为"' + row.name + '"的数据项?',
      t('common.reminder')
    )
    .then(async () => {
      ModelApi.updateModelStateApi(row.id, state).then(() => {
        message.success(t('common.updateSuccess'))
        getList()
      })
    })
    .catch(() => {})
}
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: ModelVO) => {
  // 设置数据
  detailRef.value = row
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
      <el-button type="primary" v-hasPermi="['bpm:model:create']" @click="handleCreate">
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
      <template #category="{ row }">
        <DictTag :type="DICT_TYPE.BPM_MODEL_CATEGORY" :value="row.category" />
      </template>
      <template #formId="{ row }">
        <span>{{ row.formName }}</span>
      </template>
      <template #processDefinition>
        <el-table-column label="流程版本" prop="processDefinition.version">
          <template #default="{ row }">
            <el-tag v-if="row.processDefinition">
              {{ 'v' + row.processDefinition.version }}
            </el-tag>
            <el-tag type="warning" v-else>未部署</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="激活状态" prop="processDefinition.suspensionState">
          <template #default="{ row }">
            <el-switch
              v-if="row.processDefinition"
              v-model="row.processDefinition.suspensionState"
              :active-value="1"
              :inactive-value="2"
              @change="handleChangeState(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="部署时间" prop="processDefinition.deploymentTime">
          <template #default="{ row }">
            <span>
              {{ dayjs(row.processDefinition.deploymentTime).format('YYYY-MM-DD HH:mm:ss') }}
            </span>
          </template>
        </el-table-column>
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['bpm:model:update']" @click="handleUpdate(row)">
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button link type="primary" v-hasPermi="['bpm:model:update']" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:model:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
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
    />
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailRef"
    >
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
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
