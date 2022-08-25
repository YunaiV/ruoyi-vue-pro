<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { handleTree } from '@/utils/tree'
import dayjs from 'dayjs'
import { IconSelect } from '@/components/Icon'
import { Tooltip } from '@/components/Tooltip'
import * as MenuApi from '@/api/system/menu'
import { useI18n } from '@/hooks/web/useI18n'
import {
  ElRow,
  ElCol,
  ElTable,
  ElTableColumn,
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
import { MenuVO } from '@/api/system/menu/types'
import { SystemMenuTypeEnum, CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useMessage } from '@/hooks/web/useMessage'
import { required } from '@/utils/formRules.js'
const message = useMessage()
const { t } = useI18n() // 国际化
// ========== 创建菜单树结构 ==========
const loading = ref(true)
const menuData = ref<any[]>([]) // 树形结构
const getList = async () => {
  const res = await MenuApi.getMenuListApi(queryParams)
  menuData.value = handleTree(res)
  loading.value = false
}
const menuProps = {
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id'
}
const menuOptions = ref() // 树形结构
const getTree = async () => {
  const res = await MenuApi.listSimpleMenusApi()
  const menu = { id: 0, name: '主类目', children: [] as any[] }
  menu.children = handleTree(res)
  console.info(menu)
  menuOptions.value = menu
}
// ========== 查询 ==========
const queryParams = reactive({
  name: undefined,
  status: undefined
})
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
// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
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
// 表单校验
const rules = reactive({
  name: [required],
  sort: [required],
  path: [required],
  status: [required]
})
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}
// 新建操作
const handleCreate = () => {
  // 重置表单
  setDialogTile('create')
}
// 修改操作
const handleUpdate = async (row: MenuVO) => {
  // 设置数据
  const res = await MenuApi.getMenuApi(row.id)
  menuForm.value = res
  setDialogTile('update')
}
// 删除操作
const handleDelete = async (row: MenuVO) => {
  message
    .confirm(t('common.delDataMessage'), t('common.confirmTitle'))
    .then(async () => {
      await MenuApi.deleteMenuApi(row.id)
      message.success(t('common.delSuccess'))
    })
    .catch(() => {})
  await getList()
}
// 保存操作
function isExternal(path: string) {
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
// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  getTree()
})
</script>
<template>
  <!-- 搜索工作区 -->
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
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh-right" class="mr-5px" />
          {{ t('common.reset') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <!-- 列表 -->
  <ContentWrap>
    <div class="mb-10px">
      <el-button type="primary" v-hasPermi="['system:notice:create']" @click="handleCreate">
        <Icon icon="ep:zoom-in" class="mr-1px" /> {{ t('action.add') }}
      </el-button>
    </div>
    <el-table
      v-loading="loading"
      table-layout="auto"
      row-key="id"
      :data="menuData"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column label="菜单名称" prop="name" width="240px">
        <template #default="scope">
          <Icon :icon="scope.row.icon" />
          <span class="ml-3">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="菜单类型" prop="type">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.SYSTEM_MENU_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="路由地址" prop="path" />
      <el-table-column label="组件路径" prop="component" />
      <el-table-column label="权限标识" prop="permission" />
      <el-table-column label="排序" prop="sort" />
      <el-table-column label="状态" prop="status">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime">
        <template #default="scope">
          <span>{{ dayjs(scope.row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button
            link
            type="primary"
            v-hasPermi="['system:menu:update']"
            @click="handleUpdate(scope.row)"
          >
            <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:menu:delete']"
            @click="handleDelete(scope.row)"
          >
            <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
  <!-- 添加或修改菜单对话框 -->
  <Dialog v-model="dialogVisible" :title="dialogTitle" maxHeight="400px" width="40%">
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
              :data="menuData"
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
            <el-input-number v-model="menuForm.sort" controls-position="right" :min="0" clearable />
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
<style lang="less" scoped>
:deep(.el-button.is-text) {
  margin-left: 0;
  padding: 8px 10px;
}
</style>
