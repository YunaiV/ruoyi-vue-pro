<script setup lang="ts">
import { ref, unref } from 'vue'
import dayjs from 'dayjs'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import type { FileConfigVO } from '@/api/infra/fileConfig/types'
import { rules, allSchemas } from './fileConfig.data'
import * as FileConfigApi from '@/api/infra/fileConfig'
import { useMessage } from '@/hooks/web/useMessage'
const message = useMessage()
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<FileConfigVO>({
  getListApi: FileConfigApi.getFileConfigPageApi,
  delListApi: FileConfigApi.deleteFileConfigApi
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
const handleUpdate = async (row: FileConfigVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await FileConfigApi.getFileConfigApi(row.id)
  unref(formRef)?.setValues(res)
}

// 主配置操作
const handleMaster = (row: FileConfigVO) => {
  message
    .confirm('是否确认修改配置【 ' + row.name + ' 】为主配置?', t('common.reminder'))
    .then(async () => {
      await FileConfigApi.updateFileConfigMasterApi(row.id)
      await getList()
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
        const data = unref(formRef)?.formModel as FileConfigVO
        if (actionType.value === 'create') {
          await FileConfigApi.createFileConfigApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await FileConfigApi.updateFileConfigApi(data)
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

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: FileConfigVO) => {
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
      <el-button type="primary" v-hasPermi="['infra:file-config:create']" @click="handleCreate">
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
      <template #storage="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_FILE_STORAGE" :value="row.storage" />
      </template>
      <template #primary="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="row.master" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file-config:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file-config:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file-config:update']"
          @click="handleMaster(row)"
        >
          <Icon icon="ep:flag" class="mr-1px" /> 主配置
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file-config:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:share" class="mr-1px" /> {{ t('action.test') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file-config:delete']"
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
      <template #storage="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_FILE_STORAGE" :value="row.storage" />
      </template>
      <template #primary="{ row }">
        <DictTag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="row.master" />
      </template>
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
