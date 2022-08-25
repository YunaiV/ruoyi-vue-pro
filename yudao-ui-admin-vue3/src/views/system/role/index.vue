<script setup lang="ts">
import { onMounted, reactive, ref, unref } from 'vue'
import dayjs from 'dayjs'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElMessage,
  ElTree,
  ElCard,
  ElSwitch
} from 'element-plus'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import { rules, allSchemas } from './role.data'
import type { RoleVO } from '@/api/system/role/types'
import type {
  PermissionAssignRoleDataScopeReqVO,
  PermissionAssignRoleMenuReqVO
} from '@/api/system/permission/types'
import * as RoleApi from '@/api/system/role'
import * as PermissionApi from '@/api/system/permission'
import { listSimpleMenusApi } from '@/api/system/menu'
import { listSimpleDeptApi } from '@/api/system/dept'
import { handleTree } from '@/utils/tree'
import { SystemDataScopeEnum } from '@/utils/constants'
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<RoleVO>({
  getListApi: RoleApi.getRolePageApi,
  delListApi: RoleApi.deleteRoleApi
})
const { getList, setSearchParams, delList } = methods

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
const handleUpdate = async (row: RoleVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await RoleApi.getRoleApi(row.id)
  unref(formRef)?.setValues(res)
}

// 提交按钮
const submitForm = async () => {
  loading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as RoleVO
    if (actionType.value === 'create') {
      await RoleApi.createRoleApi(data)
      ElMessage.success(t('common.createSuccess'))
    } else {
      await RoleApi.updateRoleApi(data)
      ElMessage.success(t('common.updateSuccess'))
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
    await getList()
  } finally {
    loading.value = false
  }
}

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: RoleVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}

// ========== 数据权限 ==========
const dataScopeForm = reactive({
  id: 0,
  name: '',
  code: '',
  dataScope: 0,
  checkList: []
})
const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}
const treeOptions = ref([]) // 菜单树形结构
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
const handleScope = async (type: string, row: RoleVO) => {
  dataScopeForm.id = row.id
  dataScopeForm.name = row.name
  dataScopeForm.code = row.code
  if (type === 'menu') {
    const menuRes = await listSimpleMenusApi()
    treeOptions.value = handleTree(menuRes)
    const role = await PermissionApi.listRoleMenusApi(row.id)
    console.info(role)
    if (role) {
      // treeRef.value!.setCheckedKeys(role as unknown as Array<number>)
      defaultCheckedKeys.value = role
    }
  } else if (type === 'data') {
    const deptRes = await listSimpleDeptApi()
    treeOptions.value = handleTree(deptRes)
    const role = await RoleApi.getRoleApi(row.id)
    console.info(role)
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
  const keys = treeRef.value!.getCheckedKeys(false) as unknown as Array<number>
  console.info(keys)
  if ('data' === actionScopeType.value) {
    const data = ref<PermissionAssignRoleDataScopeReqVO>({
      roleId: dataScopeForm.id,
      dataScope: dataScopeForm.dataScope,
      dataScopeDeptIds: dataScopeForm.dataScope !== SystemDataScopeEnum.DEPT_CUSTOM ? [] : keys
    })
    await PermissionApi.assignRoleDataScopeApi(data.value)
  } else if ('menu' === actionScopeType.value) {
    const data = ref<PermissionAssignRoleMenuReqVO>({
      roleId: dataScopeForm.id,
      menuIds: keys
    })
    await PermissionApi.assignRoleMenuApi(data.value)
  }
  ElMessage.success(t('common.updateSuccess'))
  dialogScopeVisible.value = false
}
const init = () => {
  dataScopeDictDatas.value = getDictOptions(DICT_TYPE.SYSTEM_DATA_SCOPE)
}
// ========== 初始化 ==========
onMounted(() => {
  getList()
  init()
})
</script>

<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" v-hasPermi="['system:role:create']" @click="handleCreate">
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
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_ROLE_TYPE" :value="row.type" />
      </template>
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
          v-hasPermi="['system:role:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:role:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:permission:assign-role-menu']"
          @click="handleScope('menu', row)"
        >
          <Icon icon="ep:basketball" class="mr-1px" /> 菜单权限
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:permission:assign-role-data-scope']"
          @click="handleScope('data', row)"
        >
          <Icon icon="ep:coin" class="mr-1px" /> 数据权限
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:role:delete']"
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
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_ROLE_TYPE" :value="row.type" />
      </template>
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
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
        :loading="loading"
        @click="submitForm"
      >
        {{ t('action.save') }}
      </el-button>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
  <Dialog v-model="dialogScopeVisible" :title="dialogScopeTitle" max-height="600px">
    <el-form :model="dataScopeForm">
      <el-form-item label="角色名称">
        <el-input v-model="dataScopeForm.name" :disabled="true" />
      </el-form-item>
      <el-form-item label="角色标识">
        <el-input v-model="dataScopeForm.code" :disabled="true" />
      </el-form-item>
      <!-- 分配角色的数据权限对话框 -->
      <el-form-item label="权限范围" v-if="actionScopeType === 'data'">
        <el-select v-model="dataScopeForm.dataScope">
          <el-option
            v-for="item in dataScopeDictDatas"
            :key="parseInt(item.value)"
            :label="item.label"
            :value="parseInt(item.value)"
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
        <el-card class="box-card">
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
      <el-button type="primary" :loading="loading" @click="submitScope">
        {{ t('action.save') }}
      </el-button>
      <el-button @click="dialogScopeVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
