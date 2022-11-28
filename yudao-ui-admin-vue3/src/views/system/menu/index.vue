<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入菜单名称" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择菜单状态">
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
          v-hasPermi="['system:menu:create']"
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
    >
      <vxe-column title="菜单名称" field="name" width="200" tree-node>
        <template #default="{ row }">
          <Icon :icon="row.icon" />
          <span class="ml-3">{{ row.name }}</span>
        </template>
      </vxe-column>
      <vxe-column title="菜单类型" field="type">
        <template #default="{ row }">
          <DictTag :type="DICT_TYPE.SYSTEM_MENU_TYPE" :value="row.type" />
        </template>
      </vxe-column>
      <vxe-column title="路由地址" field="path" />
      <vxe-column title="组件路径" field="component" />
      <vxe-column title="权限标识" field="permission" />
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
            v-hasPermi="['system:menu:update']"
            @click="handleUpdate(row.id)"
          />
          <!-- 操作：删除 -->
          <XTextButton
            preIcon="ep:delete"
            :title="t('action.del')"
            v-hasPermi="['system:menu:delete']"
            @click="handleDelete(row.id)"
          />
        </template>
      </vxe-column>
    </vxe-table>
  </ContentWrap>
  <!-- 添加或修改菜单对话框 -->
  <XModal id="menuModel" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <el-form ref="formRef" :model="menuForm" :rules="rules" label-width="auto" label-position="top">
      <el-form-item label="上级菜单">
        <el-tree-select
          node-key="id"
          v-model="menuForm.parentId"
          :props="menuProps"
          :data="menuOptions"
          :default-expanded-keys="[0]"
          check-strictly
        />
      </el-form-item>
      <el-form-item label="菜单名称" prop="name" span="12">
        <el-input v-model="menuForm.name" placeholder="请输入菜单名称" clearable />
      </el-form-item>
      <el-form-item label="菜单类型" prop="type">
        <el-radio-group v-model="menuForm.type">
          <el-radio-button
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_MENU_TYPE)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <template v-if="menuForm.type !== 3">
        <el-form-item label="菜单图标">
          <IconSelect v-model="menuForm.icon" clearable />
        </el-form-item>
        <el-form-item label="路由地址" prop="path">
          <template #label>
            <Tooltip
              titel="路由地址"
              message="访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头"
            />
          </template>
          <el-input v-model="menuForm.path" placeholder="请输入路由地址" clearable />
        </el-form-item>
      </template>
      <template v-if="menuForm.type === 2">
        <el-form-item label="路由地址" prop="component">
          <el-input v-model="menuForm.component" placeholder="请输入组件地址" clearable />
        </el-form-item>
      </template>
      <template v-if="menuForm.type !== 1">
        <el-form-item label="权限标识" prop="permission">
          <template #label>
            <Tooltip
              titel="权限标识"
              message="Controller 方法上的权限字符，如：@PreAuthorize(`@ss.hasPermission('system:user:list')`)"
            />
          </template>
          <el-input v-model="menuForm.permission" placeholder="请输入权限标识" clearable />
        </el-form-item>
      </template>
      <el-form-item label="显示排序" prop="sort">
        <el-input-number v-model="menuForm.sort" controls-position="right" :min="0" clearable />
      </el-form-item>
      <el-form-item label="菜单状态" prop="status">
        <el-radio-group v-model="menuForm.status">
          <el-radio
            border
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="menuForm.type !== 3">
        <el-form-item label="显示状态" prop="status">
          <template #label>
            <Tooltip
              titel="显示状态"
              message="选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问"
            />
          </template>
          <el-radio-group v-model="menuForm.visible">
            <el-radio border key="true" :label="true">显示</el-radio>
            <el-radio border key="false" :label="false">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      <template v-if="menuForm.type === 2">
        <el-form-item label="缓存状态" prop="keepAlive">
          <template #label>
            <Tooltip
              titel="缓存状态"
              message="选择缓存时，则会被 `keep-alive` 缓存，需要匹配组件的 `name` 和路由地址保持一致"
            />
          </template>
          <el-radio-group v-model="menuForm.keepAlive">
            <el-radio border key="true" :label="true">缓存</el-radio>
            <el-radio border key="false" :label="false">不缓存</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
    </el-form>
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
<script setup lang="ts" name="Menu">
// 全局相关的 import
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElTreeSelect,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElRadioButton,
  FormInstance
} from 'element-plus'
import { Tooltip } from '@/components/Tooltip'
import { IconSelect } from '@/components/Icon'
import { VxeTableInstance } from 'vxe-table'
// 业务相关的 import
import * as MenuApi from '@/api/system/menu'
import { required } from '@/utils/formRules.js'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { SystemMenuTypeEnum, CommonStatusEnum } from '@/utils/constants'
import { handleTree } from '@/utils/tree'
import { deepCopy } from 'windicss/utils'

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
// 新增和修改的表单值
const formRef = ref<FormInstance>()
const menuFormNull = {
  id: 0,
  name: '',
  permission: '',
  type: SystemMenuTypeEnum.DIR,
  sort: 1,
  parentId: 0,
  path: '',
  icon: '',
  component: '',
  status: CommonStatusEnum.ENABLE,
  visible: true,
  keepAlive: true,
  createTime: ''
}
const menuForm = ref<MenuApi.MenuVO>(menuFormNull)
// 新增和修改的表单校验
const rules = reactive({
  name: [required],
  sort: [required],
  path: [required],
  status: [required]
})

// ========== 下拉框[上级菜单] ==========
// 下拉框[上级菜单]的配置项目
const menuProps = {
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id'
}
const menuOptions = ref<any[]>([]) // 树形结构
// 获取下拉框[上级菜单]的数据
const getTree = async () => {
  menuOptions.value = []
  const res = await MenuApi.listSimpleMenusApi()
  let menu: Tree = { id: 0, name: '主类目', children: [] }
  menu.children = handleTree(res)
  menuOptions.value.push(menu)
}

// ========== 查询 ==========
const queryParams = reactive<MenuApi.MenuPageReqVO>({
  name: undefined,
  status: undefined
})
// 执行查询
const getList = async () => {
  tableLoading.value = true
  const res = await MenuApi.getMenuListApi(queryParams)
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
const setDialogTile = async (type: string) => {
  await getTree()
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  // 重置表单
  formRef.value?.resetFields()
  menuForm.value = deepCopy(menuFormNull)
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  await setDialogTile('update')
  // 设置数据
  const res = await MenuApi.getMenuApi(rowId)
  menuForm.value = res
}

// 提交新增/修改的表单
const submitForm = async () => {
  actionLoading.value = true
  // 提交请求
  try {
    if (
      menuForm.value.type === SystemMenuTypeEnum.DIR ||
      menuForm.value.type === SystemMenuTypeEnum.MENU
    ) {
      if (!isExternal(menuForm.value.path)) {
        if (menuForm.value.parentId === 0 && menuForm.value.path.charAt(0) !== '/') {
          message.error('路径必须以 / 开头')
          return
        } else if (menuForm.value.parentId !== 0 && menuForm.value.path.charAt(0) === '/') {
          message.error('路径不能以 / 开头')
          return
        }
      }
    }
    if (actionType.value === 'create') {
      await MenuApi.createMenuApi(menuForm.value)
      message.success(t('common.createSuccess'))
    } else {
      await MenuApi.updateMenuApi(menuForm.value)
      message.success(t('common.updateSuccess'))
    }
  } finally {
    dialogVisible.value = false
    actionLoading.value = false
    // 操作成功，重新加载列表
    await getList()
  }
}

// 判断 path 是不是外部的 HTTP 等链接
const isExternal = (path: string) => {
  return /^(https?:|mailto:|tel:)/.test(path)
}

// ========== 删除 ==========
// 删除操作
const handleDelete = async (rowId: number) => {
  message.delConfirm().then(async () => {
    await MenuApi.deleteMenuApi(rowId)
    message.success(t('common.delSuccess'))
    await getList()
  })
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
})
</script>
