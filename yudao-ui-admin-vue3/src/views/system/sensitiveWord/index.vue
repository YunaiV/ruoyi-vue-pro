<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <XButton
        type="primary"
        preIcon="ep:zoom-in"
        :title="t('action.add')"
        v-hasPermi="['system:sensitive-word:create']"
        @click="handleCreate()"
      />
      <XButton
        type="warning"
        preIcon="ep:download"
        :title="t('action.export')"
        v-hasPermi="['system:sensitive-word:export']"
        @click="exportList('敏感词数据.xls')"
      />
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
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
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
          @click="handleDetail(row)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:sensitive-word:delete']"
          @click="delList(row.id, false)"
        />
      </template>
    </Table>
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
      :data="detailRef"
    />
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
<script setup lang="ts">
import { onMounted, ref, unref } from 'vue'
import dayjs from 'dayjs'
import { useMessage } from '@/hooks/web/useMessage'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import type { SensitiveWordVO } from '@/api/system/sensitiveWord/types'
import { rules, allSchemas } from './sensitiveWord.data'
import * as SensitiveWordApi from '@/api/system/sensitiveWord'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<SensitiveWordVO>({
  getListApi: SensitiveWordApi.getSensitiveWordPageApi,
  delListApi: SensitiveWordApi.deleteSensitiveWordApi,
  exportListApi: SensitiveWordApi.exportSensitiveWordApi
})
const { getList, setSearchParams, delList, exportList } = methods

// 获取标签
const tagsOptions = ref()
const getTags = async () => {
  const res = await SensitiveWordApi.getSensitiveWordTagsApi()
  tagsOptions.value = res
}
// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const tags = ref()
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

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await SensitiveWordApi.getSensitiveWordApi(rowId)
  tags.value = res.tags
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
        const data = unref(formRef)?.formModel as SensitiveWordVO
        data.tags = tags.value
        if (actionType.value === 'create') {
          await SensitiveWordApi.createSensitiveWordApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await SensitiveWordApi.updateSensitiveWordApi(data)
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
const handleDetail = async (row: SensitiveWordVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}

// ========== 初始化 ==========
onMounted(async () => {
  await getTags()
  await getList()
})
</script>
