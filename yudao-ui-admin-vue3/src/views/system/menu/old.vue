<script setup lang="ts">
import { onMounted, ref, unref } from 'vue'
import { handleTree } from '@/utils/tree'
import { useI18n } from '@/hooks/web/useI18n'
import { IconSelect } from '@/components/Icon'
import { ElCard, ElMessage, ElMessageBox, ElTree, ElTreeSelect } from 'element-plus'
import { columns, modelSchema } from './menu.data'
import { Form, FormExpose } from '@/components/Form'
import * as MenuApi from '@/api/system/menu'
import { MenuVO } from '@/api/system/menu/types'
const { t } = useI18n() // 国际化
interface Tree {
  id: number
  name: string
  children?: Tree[]
}
const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}
// ========== 创建菜单树结构 ==========
const menuOptions = ref([]) // 树形结构
const treeRef = ref<InstanceType<typeof ElTree>>()
const getTree = async () => {
  const res = await MenuApi.listSimpleMenusApi()
  menuOptions.value = handleTree(res)
}
const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.includes(value)
}
// ========== 菜单信息form表单 ==========
const loading = ref(false) // 遮罩层
const formRef = ref<FormExpose>()
const iconModel = ref('ep:user')
const menuParentId = ref()
const menuStatus = ref('add')
const menuTitle = ref('菜单信息')
const showEdit = ref(false)
// 提交按钮
const submitForm = async () => {
  loading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as MenuVO
    data.parentId = menuParentId.value
    // TODO: 表单提交待完善
    if (menuStatus.value === 'add') {
      await MenuApi.createMenuApi(data)
    } else if (menuStatus.value === 'edit') {
      await MenuApi.updateMenuApi(data)
    }
  } finally {
    loading.value = false
  }
}
// ========== 按钮列表相关 ==========
const tableData = ref([])
const tableLoading = ref(false)
const onDisabled = ref(true)
const tableTitle = ref('按钮信息')
// 树点击事件
const handleMenuNodeClick = async (data: { [key: string]: any }) => {
  showEdit.value = true
  const res = await MenuApi.getMenuApi(data.id)
  menuTitle.value = res.name + '-菜单信息'
  tableTitle.value = res.name + '-按钮列表'
  menuParentId.value = data.id
  tableData.value = await MenuApi.getMenuListApi({ name: res.name })
  unref(formRef)?.setValues(res)
  onDisabled.value = true
  changeDisabled()
}
const handleCreate = () => {
  // 重置表单
  unref(formRef)?.getElFormRef()?.resetFields()
  menuParentId.value = 0
  onDisabled.value = false
  changeDisabled()
}
const handleEdit = () => {
  onDisabled.value = false
  changeDisabled()
}
const changeDisabled = () => {
  unref(formRef)?.setProps({
    disabled: onDisabled
  })
}
// 修改操作
const handleUpdate = async (row: MenuVO) => {
  // 设置数据
  const res = await MenuApi.getMenuApi(row.id)
  unref(formRef)?.setValues(res)
}

// 删除操作
const handleDelete = (row: MenuVO) => {
  ElMessageBox.confirm(t('common.delDataMessage'), t('common.confirmTitle'), {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      await MenuApi.deleteMenuApi(row.id)
      ElMessage.success(t('common.delSuccess'))
    })
    .catch(() => {})
}
onMounted(async () => {
  await getTree()
})
</script>
<template>
  <div class="flex">
    <el-card class="w-1/4 menu" :gutter="12" shadow="always">
      <template #header>
        <div class="card-header">
          <span>菜单列表</span>
          <el-button type="primary" v-hasPermi="['system:menu:create']" @click="handleCreate">
            新增根节点
          </el-button>
        </div>
      </template>
      <!-- <p>菜单列表</p> -->
      <el-tree
        ref="treeRef"
        node-key="id"
        :accordion="true"
        :data="menuOptions"
        :props="defaultProps"
        :highlight-current="true"
        :filter-method="filterNode"
        @node-click="handleMenuNodeClick"
      />
    </el-card>
    <el-card class="w-1/2 menu" style="margin-left: 10px" :gutter="12" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ menuTitle }}</span>
        </div>
      </template>
      <div v-if="!showEdit">
        <span>请从左侧选择菜单</span>
      </div>
      <div v-if="showEdit">
        <Form :loading="loading" :schema="modelSchema" ref="formRef">
          <template #parentId>
            <el-tree-select
              node-key="id"
              v-model="menuParentId"
              :props="defaultProps"
              :data="menuOptions"
              check-strictly
            />
          </template>
          <template #icon>
            <IconSelect v-model="iconModel" />
          </template>
        </Form>
        <el-button
          v-if="!onDisabled"
          type="primary"
          v-hasPermi="['system:menu:update']"
          :loading="loading"
          @click="submitForm"
        >
          {{ t('action.save') }}
        </el-button>
        <el-button v-if="!onDisabled" :loading="loading" @click="showEdit = false">
          {{ t('common.cancel') }}
        </el-button>
        <el-button
          v-if="onDisabled"
          v-hasPermi="['system:menu:update']"
          type="primary"
          :loading="loading"
          @click="handleEdit"
        >
          {{ t('action.edit') }}
        </el-button>
      </div>
    </el-card>
    <el-card class="w-1/2 menu" style="margin-left: 10px" :gutter="12" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ tableTitle }}</span>
          <!-- <el-button type="primary">新增根节点</el-button> -->
        </div>
      </template>
      <!-- 列表 -->
      <Table :loading="tableLoading" :columns="columns" :data="tableData">
        <template #action="{ row }">
          <el-button
            link
            type="primary"
            v-hasPermi="['system:menu:update']"
            @click="handleUpdate(row)"
          >
            <Icon icon="ep:edit" class="mr-5px" /> {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:menu:delete']"
            @click="handleDelete(row)"
          >
            <Icon icon="ep:delete" class="mr-5px" /> {{ t('action.del') }}
          </el-button>
        </template>
      </Table>
    </el-card>
  </div>
</template>
<style scoped>
.menu {
  height: 1000px;
  max-height: 1800px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
