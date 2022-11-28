<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:sensitive-word:create']"
          @click="handleCreate()"
        />
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:sensitive-word:export']"
          @click="handleExport()"
        />
      </template>
      <template #tags_default="{ row }">
        <el-tag
          :disable-transitions="true"
          :key="index"
          v-for="(tag, index) in row.tags"
          :index="index"
        >
          {{ tag }}
        </el-tag>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:sensitive-word:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:sensitive-word:update']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:sensitive-word:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    >
      <template #tags>
        <el-select v-model="tags" multiple placeholder="请选择">
          <el-option v-for="item in tagsOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    >
      <template #tags="{ row }">
        <el-tag
          :disable-transitions="true"
          :key="index"
          v-for="(tag, index) in row.tags"
          :index="index"
        >
          {{ tag }}
        </el-tag>
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
<script setup lang="ts" name="SensitiveWord">
import { onMounted, ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
import { ElTag, ElSelect, ElOption } from 'element-plus'
import * as SensitiveWordApi from '@/api/system/sensitiveWord'
import { rules, allSchemas } from './sensitiveWord.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData, exportList } =
  useVxeGrid<SensitiveWordApi.SensitiveWordVO>({
    allSchemas: allSchemas,
    getListApi: SensitiveWordApi.getSensitiveWordPageApi,
    deleteApi: SensitiveWordApi.deleteSensitiveWordApi,
    exportListApi: SensitiveWordApi.exportSensitiveWordApi
  })
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref
const tags = ref()

// 获取标签
const tagsOptions = ref()
const getTags = async () => {
  const res = await SensitiveWordApi.getSensitiveWordTagsApi()
  tagsOptions.value = res
}

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  tags.value = null
  setDialogTile('create')
}

// 导出操作
const handleExport = async () => {
  await exportList(xGrid, '敏感词数据.xls')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await SensitiveWordApi.getSensitiveWordApi(rowId)
  tags.value = res.tags
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await SensitiveWordApi.getSensitiveWordApi(rowId)
  detailData.value = res
}

// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
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
        const data = unref(formRef)?.formModel as SensitiveWordApi.SensitiveWordVO
        data.tags = tags.value
        if (actionType.value === 'create') {
          await SensitiveWordApi.createSensitiveWordApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await SensitiveWordApi.updateSensitiveWordApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        // 刷新列表
        await getList(xGrid)
      }
    }
  })
}

// ========== 初始化 ==========
onMounted(async () => {
  await getTags()
})
</script>
