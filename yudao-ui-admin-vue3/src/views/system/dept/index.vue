<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入部门名称" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择部门状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <!-- 操作：搜索 -->
        <XButton
          type="primary"
          preIcon="ep:search"
          :title="t('common.query')"
          @click="handleQuery()"
        />
        <!-- 操作：重置 -->
        <XButton preIcon="ep:refresh-right" :title="t('common.reset')" @click="resetQuery()" />
      </el-form-item>
    </el-form>
    <vxe-toolbar>
      <template #buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:dept:create']"
          @click="handleCreate()"
        />
        <XButton title="展开所有" @click="xTable?.setAllTreeExpand(true)" />
        <XButton title="关闭所有" @click="xTable?.clearTreeExpand()" />
      </template>
    </vxe-toolbar>
    <!-- 列表 -->
    <vxe-table
      show-overflow
      keep-source
      ref="xTable"
      :loading="tableLoading"
      :row-config="{ keyField: 'id' }"
      :column-config="{ resizable: true }"
      :tree-config="{ transform: true, rowField: 'id', parentField: 'parentId' }"
      :print-config="{}"
      :export-config="{}"
      :data="tableData"
      class="xtable"
    >
      <vxe-column title="部门名称" field="name" width="200" tree-node />
      <vxe-column title="负责人" field="leaderUserId" :formatter="userNicknameFormat" />
      <vxe-column title="排序" field="sort" />
      <vxe-column title="状态" field="status">
        <template #default="{ row }">
          <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </vxe-column>
      <vxe-column title="创建时间" field="createTime" formatter="formatDate" />
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <!-- 操作：修改 -->
          <XTextButton
            preIcon="ep:edit"
            :title="t('action.edit')"
            v-hasPermi="['system:dept:update']"
            @click="handleUpdate(row.id)"
          />
          <!-- 操作：删除 -->
          <XTextButton
            preIcon="ep:delete"
            :title="t('action.del')"
            v-hasPermi="['system:dept:delete']"
            @click="handleDelete(row.id)"
          />
        </template>
      </vxe-column>
    </vxe-table>
  </ContentWrap>
  <!-- 添加或修改菜单对话框 -->
  <XModal id="deptModel" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <!-- 操作工具栏 -->
    <Form ref="formRef" :schema="modelSchema" :rules="rules">
      <template #parentId>
        <el-tree-select
          node-key="id"
          v-model="deptParentId"
          :props="defaultProps"
          :data="deptOptions"
          :default-expanded-keys="[100]"
          check-strictly
        />
      </template>
      <template #leaderUserId>
        <el-select v-model="leaderUserId">
          <el-option
            v-for="item in userOption"
            :key="parseInt(item.id)"
            :label="item.nickname"
            :value="parseInt(item.id)"
          />
        </el-select>
      </template>
    </Form>
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :loading="actionLoading"
        @click="submitForm()"
        :title="t('action.save')"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" @click="dialogVisible = false" :title="t('dialog.close')" />
    </template>
  </XModal>
</template>
<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { ElForm, ElFormItem, ElInput, ElSelect, ElTreeSelect, ElOption } from 'element-plus'
import { VxeColumn, VxeTable, VxeTableInstance, VxeToolbar } from 'vxe-table'
import { modelSchema } from './dept.data'
import * as DeptApi from '@/api/system/dept'
import { getListSimpleUsersApi } from '@/api/system/user'
import { required } from '@/utils/formRules.js'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { handleTree } from '@/utils/tree'
import { FormExpose } from '@/components/Form'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const xTable = ref<VxeTableInstance>()
const tableLoading = ref(false)
const tableData = ref()
// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 遮罩层
const deptParentId = ref(0) // 上级ID
const leaderUserId = ref()
const formRef = ref<FormExpose>() // 表单 Ref
const deptOptions = ref() // 树形结构
const userOption = ref()
// 新增和修改的表单校验
const rules = reactive({
  name: [required],
  sort: [required],
  path: [required],
  status: [required]
})

// 下拉框[上级]的配置项目
const defaultProps = {
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id'
}
// 获取下拉框[上级]的数据
const getTree = async () => {
  const res = await DeptApi.listSimpleDeptApi()
  deptOptions.value = handleTree(res)
  console.info(deptOptions.value)
}
const getUserList = async () => {
  const res = await getListSimpleUsersApi()
  userOption.value = res
}
// ========== 查询 ==========
const queryParams = reactive<DeptApi.DeptPageReqVO>({
  name: undefined,
  status: undefined
})
// 执行查询
const getList = async () => {
  tableLoading.value = true
  const res = await DeptApi.getDeptPageApi(queryParams)
  tableData.value = res
  tableLoading.value = false
}

// 查询操作
const handleQuery = async () => {
  await getList()
}

// 重置操作
const resetQuery = async () => {
  queryParams.name = undefined
  queryParams.status = undefined
  await getList()
}

// ========== 新增/修改 ==========

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = async () => {
  deptParentId.value = 0
  leaderUserId.value = null
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await DeptApi.getDeptApi(rowId)
  console.info(res)
  deptParentId.value = res.deptParentId
  leaderUserId.value = res.leaderUserId
  await nextTick()
  unref(formRef)?.setValues(res)
}

// 提交新增/修改的表单
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as DeptApi.DeptVO
        data.parentId = deptParentId.value
        data.leaderUserId = leaderUserId.value
        if (dialogTitle.value.startsWith('新增')) {
          await DeptApi.createDeptApi(data)
        } else if (dialogTitle.value.startsWith('修改')) {
          await DeptApi.updateDeptApi(data)
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
      }
    }
  })
}

// 删除操作
const handleDelete = async (rowId: number) => {
  message.delConfirm().then(async () => {
    await DeptApi.deleteDeptApi(rowId)
    message.success(t('common.delSuccess'))
    await getList()
  })
}

const userNicknameFormat = (row) => {
  if (!row && !row.row && !row.row.leaderUserId) {
    return '未设置'
  }
  for (const user of userOption.value) {
    if (row.row.leaderUserId === user.id) {
      return user.nickname
    }
  }
  return '未知【' + row.row.leaderUserId + '】'
}

// ========== 初始化 ==========
onMounted(async () => {
  await getTree()
  await getUserList()
  await getList()
})
</script>
