<script setup lang="ts">
import { onMounted, ref, unref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { FormExpose } from '@/components/Form'
import { rules, allSchemas } from './dataSourceConfig.data'
import type { DataSourceConfigVO } from '@/api/infra/dataSourceConfig/types'
import * as DataSourceConfiggApi from '@/api/infra/dataSourceConfig'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n() // 国际化
const tableData = ref()
const getList = async () => {
  const res = await DataSourceConfiggApi.getDataSourceConfigListApi()
  tableData.value = res
}
// ========== CRUD 相关 ==========
const loading = ref(false) // 遮罩层
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
const handleUpdate = async (row: DataSourceConfigVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await DataSourceConfiggApi.getDataSourceConfigApi(row.id)
  unref(formRef)?.setValues(res)
}

// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      loading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as DataSourceConfigVO
        if (actionType.value === 'create') {
          await DataSourceConfiggApi.createDataSourceConfigApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else {
          await DataSourceConfiggApi.updateDataSourceConfigApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
        await getList()
      } finally {
        loading.value = false
      }
    }
  })
}

// 删除操作
const handleDelete = async (row: DataSourceConfigVO) => {
  await DataSourceConfiggApi.deleteDataSourceConfigApi(row.id)
  ElMessage.success(t('common.delSuccess'))
}

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: DataSourceConfigVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}
onMounted(async () => {
  await getList()
})
</script>

<template>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button
        v-hasPermi="['infra:data-source-config:create']"
        type="primary"
        @click="handleCreate"
      >
        <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
      </el-button>
    </div>
    <Table :columns="allSchemas.tableColumns" :data="tableData">
      <template #createTime="{ row }">
        <span>{{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '' }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:data-source-config:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:data-source-config:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:data-source-config:delete']"
          @click="handleDelete(row)"
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
        <span>{{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '' }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :loading="loading"
        @click="submitForm"
      >
        {{ t('action.save') }}
      </el-button>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
