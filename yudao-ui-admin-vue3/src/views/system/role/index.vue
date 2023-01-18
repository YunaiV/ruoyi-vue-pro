<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <!-- 操作：新增 -->
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:role:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：编辑 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:role:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:role:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：菜单权限 -->
        <XTextButton
          preIcon="ep:basketball"
          title="菜单权限"
          v-hasPermi="['system:permission:assign-role-menu']"
          @click="handleScope('menu', row)"
        />
        <!-- 操作：数据权限 -->
        <XTextButton
          preIcon="ep:coin"
          title="数据权限"
          v-hasPermi="['system:permission:assign-role-data-scope']"
          @click="handleScope('data', row)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:role:delete']"
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="dialogTitle">
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
      :data="detailData"
    />
    <!-- 操作按钮 -->
    <template #footer>
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm()"
      />
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>

  <XModal v-model="dialogScopeVisible" :title="dialogScopeTitle">
    <el-form :model="dataScopeForm" label-width="140px" :inline="true">
      <el-form-item label="角色名称">
        <el-tag>{{ dataScopeForm.name }}</el-tag>
      </el-form-item>
      <el-form-item label="角色标识">
        <el-tag>{{ dataScopeForm.code }}</el-tag>
      </el-form-item>
      <!-- 分配角色的数据权限对话框 -->
      <el-form-item label="权限范围" v-if="actionScopeType === 'data'">
        <el-select v-model="dataScopeForm.dataScope">
          <el-option
            v-for="item in dataScopeDictDatas"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <!-- 分配角色的菜单权限对话框 -->
      <el-form-item
        label="权限范围"
        v-if="
          actionScopeType === 'menu' || dataScopeForm.dataScope === SystemDataScopeEnum.DEPT_CUSTOM
        "
      >
        <el-card shadow="never">
          <template #header>
            父子联动(选中父节点，自动选择子节点):
            <el-switch v-model="checkStrictly" inline-prompt active-text="是" inactive-text="否" />
            全选/全不选:
            <el-switch
              v-model="treeNodeAll"
              inline-prompt
              active-text="是"
              inactive-text="否"
              @change="handleCheckedTreeNodeAll()"
            />
          </template>
          <el-tree
            ref="treeRef"
            node-key="id"
            show-checkbox
            :default-checked-keys="defaultCheckedKeys"
            :check-strictly="!checkStrictly"
            :props="defaultProps"
            :data="treeOptions"
            empty-text="加载中，请稍后"
          />
        </el-card>
      </el-form-item>
    </el-form>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitScope()"
      />
      <XButton
        :loading="actionLoading"
        :title="t('dialog.close')"
        @click="dialogScopeVisible = false"
      />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="Role">
import type { ElTree } from 'element-plus'
import type { FormExpose } from '@/components/Form'
import { handleTree, defaultProps } from '@/utils/tree'
import { SystemDataScopeEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { rules, allSchemas } from './role.data'
import * as RoleApi from '@/api/system/role'
import { listSimpleMenusApi } from '@/api/system/menu'
import { listSimpleDeptApi } from '@/api/system/dept'
import * as PermissionApi from '@/api/system/permission'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: RoleApi.getRolePageApi,
  deleteApi: RoleApi.deleteRoleApi
})

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await RoleApi.getRoleApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  // 设置数据
  const res = await RoleApi.getRoleApi(rowId)
  detailData.value = res
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
        const data = unref(formRef)?.formModel as RoleApi.RoleVO
        if (actionType.value === 'create') {
          await RoleApi.createRoleApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await RoleApi.updateRoleApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        // 刷新列表
        await reload()
      }
    }
  })
}

// ========== 数据权限 ==========
const dataScopeForm = reactive({
  id: 0,
  name: '',
  code: '',
  dataScope: 0,
  checkList: []
})
const treeOptions = ref<any[]>([]) // 菜单树形结构
const treeRef = ref<InstanceType<typeof ElTree>>()
const dialogScopeVisible = ref(false)
const dialogScopeTitle = ref('数据权限')
const actionScopeType = ref('')
const dataScopeDictDatas = ref()
const defaultCheckedKeys = ref()
// 选项
const checkStrictly = ref(true)
const treeNodeAll = ref(false)
// 全选/全不选
const handleCheckedTreeNodeAll = () => {
  treeRef.value!.setCheckedNodes(treeNodeAll.value ? treeOptions.value : [])
}
// 权限操作
const handleScope = async (type: string, row: RoleApi.RoleVO) => {
  dataScopeForm.id = row.id
  dataScopeForm.name = row.name
  dataScopeForm.code = row.code
  if (type === 'menu') {
    const menuRes = await listSimpleMenusApi()
    treeOptions.value = handleTree(menuRes)
    const role = await PermissionApi.listRoleMenusApi(row.id)
    if (role) {
      // treeRef.value!.setCheckedKeys(role as unknown as Array<number>)
      defaultCheckedKeys.value = role
    }
  } else if (type === 'data') {
    const deptRes = await listSimpleDeptApi()
    treeOptions.value = handleTree(deptRes)
    const role = await RoleApi.getRoleApi(row.id)
    dataScopeForm.dataScope = role.dataScope
    if (role.dataScopeDeptIds) {
      // treeRef.value!.setCheckedKeys(role.dataScopeDeptIds as unknown as Array<number>, false)
      defaultCheckedKeys.value = role.dataScopeDeptIds
    }
  }
  actionScopeType.value = type
  dialogScopeVisible.value = true
}
// 保存权限
const submitScope = async () => {
  if ('data' === actionScopeType.value) {
    const data = ref<PermissionApi.PermissionAssignRoleDataScopeReqVO>({
      roleId: dataScopeForm.id,
      dataScope: dataScopeForm.dataScope,
      dataScopeDeptIds:
        dataScopeForm.dataScope !== SystemDataScopeEnum.DEPT_CUSTOM
          ? []
          : (treeRef.value!.getCheckedKeys(false) as unknown as Array<number>)
    })
    await PermissionApi.assignRoleDataScopeApi(data.value)
  } else if ('menu' === actionScopeType.value) {
    const data = ref<PermissionApi.PermissionAssignRoleMenuReqVO>({
      roleId: dataScopeForm.id,
      menuIds: [
        ...(treeRef.value!.getCheckedKeys(false) as unknown as Array<number>),
        ...(treeRef.value!.getHalfCheckedKeys() as unknown as Array<number>)
      ]
    })
    await PermissionApi.assignRoleMenuApi(data.value)
  }
  message.success(t('common.updateSuccess'))
  dialogScopeVisible.value = false
}
const init = () => {
  dataScopeDictDatas.value = getIntDictOptions(DICT_TYPE.SYSTEM_DATA_SCOPE)
}
// ========== 初始化 ==========
onMounted(() => {
  init()
})
</script>
