<template>
  <ContentWrap>
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
        <XButton
          type="primary"
          preIcon="ep:search"
          :title="t('common.query')"
          @click="handleQuery()"
        />
        <XButton preIcon="ep:refresh-right" :title="t('common.reset')" @click="resetQuery()" />
      </el-form-item>
    </el-form>
    <vxe-toolbar>
      <template #buttons>
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
          <XTextButton
            preIcon="ep:edit"
            :title="t('action.edit')"
            v-hasPermi="['system:menu:update']"
            @click="handleUpdate(row)"
          />
          <XTextButton
            preIcon="ep:delete"
            :title="t('action.del')"
            v-hasPermi="['system:menu:delete']"
            @click="handleDelete(row)"
          />
        </template>
      </vxe-column>
    </vxe-table>
  </ContentWrap>
  <XModal v-model="dialogVisible" id="menuModel" :title="dialogTitle">
    <template #default>
      <!-- 对话框(添加 / 修改) -->
      <el-form
        :model="menuForm"
        :rules="rules"
        :inline="true"
        label-width="120px"
        label-position="right"
      >
        <el-row :gutter="24">
          <el-col :span="24">
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
          </el-col>
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="name">
              <el-input v-model="menuForm.name" placeholder="请输入菜单名称" clearable />
            </el-form-item>
          </el-col>
          <template v-if="menuForm.type !== 3">
            <el-col :span="12">
              <el-form-item label="菜单图标">
                <IconSelect v-model="menuForm.icon" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="路由地址" prop="path">
                <template #label>
                  <Tooltip
                    titel="路由地址"
                    message="访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头"
                  />
                </template>
                <el-input v-model="menuForm.path" placeholder="请输入路由地址" clearable />
              </el-form-item>
            </el-col>
          </template>
          <template v-if="menuForm.type === 2">
            <el-col :span="12">
              <el-form-item label="路由地址" prop="component">
                <el-input v-model="menuForm.component" placeholder="请输入组件地址" clearable />
              </el-form-item>
            </el-col>
          </template>
          <template v-if="menuForm.type !== 1">
            <el-col :span="12">
              <el-form-item label="权限标识" prop="permission">
                <template #label>
                  <Tooltip
                    titel="权限标识"
                    message="Controller 方法上的权限字符，如：@PreAuthorize(`@ss.hasPermission('system:user:list')`)"
                  />
                </template>
                <el-input v-model="menuForm.permission" placeholder="请输入权限标识" clearable />
              </el-form-item>
            </el-col>
          </template>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sort">
              <el-input-number
                v-model="menuForm.sort"
                controls-position="right"
                :min="0"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态" prop="status">
              <el-radio-group v-model="menuForm.status">
                <el-radio-button
                  v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                  :key="dict.value"
                  :label="dict.value"
                >
                  {{ dict.label }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <template v-if="menuForm.type !== 3">
            <el-col :span="12">
              <el-form-item label="显示状态" prop="status">
                <template #label>
                  <Tooltip
                    titel="显示状态"
                    message="选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问"
                  />
                </template>
                <el-radio-group v-model="menuForm.visible">
                  <el-radio-button key="true" :label="true">显示</el-radio-button>
                  <el-radio-button key="false" :label="false">隐藏</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </template>
          <template v-if="menuForm.type === 2">
            <el-col :span="12">
              <el-form-item label="缓存状态" prop="keepAlive">
                <template #label>
                  <Tooltip
                    titel="缓存状态"
                    message="选择缓存时，则会被 `keep-alive` 缓存，需要匹配组件的 `name` 和路由地址保持一致"
                  />
                </template>
                <el-radio-group v-model="menuForm.keepAlive">
                  <el-radio-button key="true" :label="true">缓存</el-radio-button>
                  <el-radio-button key="false" :label="false">不缓存</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>
    </template>
    <template #footer>
      <!-- 操作按钮 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :loading="actionLoading"
        @click="submitForm"
        :title="t('action.save')"
      />
      <XButton :loading="actionLoading" @click="dialogVisible = false" :title="t('dialog.close')" />
    </template>
  </XModal>
</template>
<script setup lang="ts">
import * as MenuApi from '@/api/system/menu'
import { MenuVO } from '@/api/system/menu/types'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { IconSelect } from '@/components/Icon'
import { Tooltip } from '@/components/Tooltip'
import { required } from '@/utils/formRules.js'
import { onMounted, reactive, ref } from 'vue'
import { VxeTableInstance } from 'vxe-table'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { SystemMenuTypeEnum, CommonStatusEnum } from '@/utils/constants'
import {
  ElRow,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElTreeSelect,
  ElOption,
  ElRadioGroup,
  ElRadioButton
} from 'element-plus'
import { handleTree } from '@/utils/tree'
const { t } = useI18n() // 国际化
const message = useMessage()
const xTable = ref<VxeTableInstance>()
const tableLoading = ref(false)
const tableData = ref()
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const statusOption = ref() // 状态选项
const menuForm = ref<MenuVO>({
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
})
const menuProps = {
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id'
}
interface Tree {
  id: number
  name: string
  children?: Tree[] | any[]
}
const menuOptions = ref<any[]>([]) // 树形结构
const getTree = async () => {
  menuOptions.value = []
  const res = await MenuApi.listSimpleMenusApi()
  let menu: Tree = { id: 0, name: '主类目', children: [] }
  menu.children = handleTree(res)
  menuOptions.value.push(menu)
}
// ========== 查询 ==========
const queryParams = reactive({
  name: null,
  status: null
})
const getList = async () => {
  tableLoading.value = true
  statusOption.value = getIntDictOptions(DICT_TYPE.COMMON_STATUS)
  const res = await MenuApi.getMenuListApi(queryParams)
  tableData.value = res
  tableLoading.value = false
}
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}
// 新建操作
const handleCreate = () => {
  setDialogTile('create')
}
// 修改操作
const handleUpdate = async (row: MenuVO) => {
  // 设置数据
  const res = await MenuApi.getMenuApi(row.id)
  console.log(res)
  menuForm.value = res
  setDialogTile('update')
}
// 删除操作
const handleDelete = async (row: MenuVO) => {
  message.confirm(t('common.delDataMessage'), t('common.confirmTitle')).then(async () => {
    await MenuApi.deleteMenuApi(row.id)
    message.success(t('common.delSuccess'))
    await getList()
  })
}
// 表单校验
const rules = reactive({
  name: [required],
  sort: [required],
  path: [required],
  status: [required]
})
// 查询操作
const handleQuery = async () => {
  await getList()
}
// 重置操作
const resetQuery = async () => {
  queryParams.name = null
  queryParams.status = null
  await getList()
}
// 保存操作
const isExternal = (path: string) => {
  return /^(https?:|mailto:|tel:)/.test(path)
}
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
    // 操作成功，重新加载列表
    dialogVisible.value = false
    await getList()
  } finally {
    actionLoading.value = false
  }
}
onMounted(async () => {
  await getList()
  getTree()
})
</script>
