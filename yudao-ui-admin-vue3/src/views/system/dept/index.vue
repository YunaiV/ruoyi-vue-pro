<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable ref="xGrid" @register="registerTable" show-overflow>
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:dept:create']"
          @click="handleCreate()"
        />
        <XButton title="展开所有" @click="xGrid?.Ref.setAllTreeExpand(true)" />
        <XButton title="关闭所有" @click="xGrid?.Ref.clearTreeExpand()" />
      </template>
      <template #leaderUserId_default="{ row }">
        <span>{{ userNicknameFormat(row) }}</span>
      </template>
      <template #actionbtns_default="{ row }">
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
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <!-- 添加或修改菜单对话框 -->
  <XModal id="deptModel" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form ref="formRef" :schema="allSchemas.formSchema" :rules="rules">
      <template #parentId="form">
        <el-tree-select
          node-key="id"
          v-model="form['parentId']"
          :props="defaultProps"
          :data="deptOptions"
          :default-expanded-keys="[100]"
          check-strictly
        />
      </template>
      <template #leaderUserId="form">
        <el-select v-model="form['leaderUserId']">
          <el-option
            v-for="item in userOption"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
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
<script setup lang="ts" name="Dept">
import { handleTree, defaultProps } from '@/utils/tree'
import type { FormExpose } from '@/components/Form'
import { allSchemas, rules } from './dept.data'
import * as DeptApi from '@/api/system/dept'
import { getListSimpleUsersApi, UserVO } from '@/api/system/user'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const xGrid = ref<any>() // 列表 Grid Ref
const treeConfig = {
  transform: true,
  rowField: 'id',
  parentField: 'parentId',
  expandAll: true
}

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 遮罩层
const formRef = ref<FormExpose>() // 表单 Ref
const deptOptions = ref() // 树形结构
const userOption = ref<UserVO[]>([])

const getUserList = async () => {
  const res = await getListSimpleUsersApi()
  userOption.value = res
}
// 获取下拉框[上级]的数据
const getTree = async () => {
  deptOptions.value = []
  const res = await DeptApi.listSimpleDeptApi()
  let dept: Tree = { id: 0, name: '顶级部门', children: [] }
  dept.children = handleTree(res)
  deptOptions.value.push(dept)
}
const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  treeConfig: treeConfig,
  getListApi: DeptApi.getDeptPageApi,
  deleteApi: DeptApi.deleteDeptApi
})
// ========== 新增/修改 ==========

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = async () => {
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await DeptApi.getDeptApi(rowId)
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
        if (actionType.value === 'create') {
          await DeptApi.createDeptApi(data)
          message.success(t('common.createSuccess'))
        } else if (actionType.value === 'update') {
          await DeptApi.updateDeptApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        await getTree()
        await reload()
      }
    }
  })
}

const userNicknameFormat = (row) => {
  if (!row || !row.leaderUserId) {
    return '未设置'
  }
  for (const user of userOption.value) {
    if (row.leaderUserId === user.id) {
      return user.nickname
    }
  }
  return '未知【' + row.leaderUserId + '】'
}

// ========== 初始化 ==========
onMounted(async () => {
  await getUserList()
  await getTree()
})
</script>
