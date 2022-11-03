<script setup lang="ts">
import * as MenuApi from '@/api/system/menu'
import { MenuVO } from '@/api/system/menu/types'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { required } from '@/utils/formRules.js'
import { onMounted, reactive, ref } from 'vue'
import { VxeTableInstance } from 'vxe-table'
import { DICT_TYPE } from '@/utils/dict'
import { SystemMenuTypeEnum, CommonStatusEnum } from '@/utils/constants'

const { t } = useI18n() // 国际化
const message = useMessage()
const xTable = ref<VxeTableInstance>()
const tableLoading = ref(false)
const tableData = ref()
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
// ========== 查询 ==========
const queryParams = reactive({
  name: undefined,
  status: undefined
})
const getList = async () => {
  tableLoading.value = true
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
})
</script>
<template>
  <ContentWrap>
    <vxe-toolbar>
      <template #buttons>
        <vxe-button @click="xTable?.setAllTreeExpand(true)">展开所有</vxe-button>
        <vxe-button @click="xTable?.clearTreeExpand()">关闭所有</vxe-button>
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
          <vxe-button
            type="text"
            status="primary"
            v-hasPermi="['system:menu:update']"
            @click="handleUpdate(row)"
          >
            <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
          </vxe-button>
          <vxe-button
            type="text"
            status="primary"
            v-hasPermi="['system:menu:delete']"
            @click="handleDelete(row)"
          >
            <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
          </vxe-button>
        </template>
      </vxe-column>
    </vxe-table>
  </ContentWrap>
  <vxe-modal
    v-model="dialogVisible"
    id="menuModel"
    :title="dialogTitle"
    width="800"
    height="400"
    min-width="460"
    min-height="320"
    show-zoom
    resize
    remember
    storage
    transfer
    show-footer
  >
    <template #default>
      <!-- 对话框(添加 / 修改) -->
      <vxe-form
        ref="xForm"
        v-if="['create', 'update'].includes(actionType)"
        :data="menuForm"
        :rules="rules"
        @submit="submitForm"
      >
        <vxe-form-item title="菜单名称" field="name" :item-render="{}">
          <template #default="{ data }">
            <vxe-input v-model="data.name" placeholder="请输入菜单名称" clearable />
          </template>
        </vxe-form-item>
      </vxe-form>
    </template>
  </vxe-modal>
</template>
