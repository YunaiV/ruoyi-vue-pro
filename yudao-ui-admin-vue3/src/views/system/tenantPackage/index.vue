<script setup lang="ts">
import { onMounted, ref, unref } from 'vue'
import dayjs from 'dayjs'
import { handleTree } from '@/utils/tree'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import { TenantPackageVO } from '@/api/system/tenantPackage/types'
import { ElMessage, ElCard, ElSwitch, ElTree } from 'element-plus'
import { rules, allSchemas } from './tenantPackage.data'
import * as TenantPackageApi from '@/api/system/tenantPackage'
import { listSimpleMenusApi } from '@/api/system/menu'
const { t } = useI18n() // 国际化

const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}
// ========== 创建菜单树结构 ==========
const menuOptions = ref([]) // 树形结构
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeNodeAll = ref(false)
// 全选/全不选
const handleCheckedTreeNodeAll = () => {
  treeRef.value!.setCheckedNodes(treeNodeAll.value ? menuOptions.value : [])
}
const getTree = async () => {
  const res = await listSimpleMenusApi()
  menuOptions.value = handleTree(res)
}
const menuExpand = ref(false)
const menuNodeAll = ref(false)

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<TenantPackageVO>({
  getListApi: TenantPackageApi.getTenantPackageTypePageApi,
  delListApi: TenantPackageApi.deleteTenantPackageTypeApi
})
const { getList, setSearchParams, delList } = methods

// ========== CRUD 相关 ==========
const loading = ref(false) // 遮罩层
const formRef = ref<FormExpose>() // 表单 Ref
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题

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
  //重置菜单树
  unref(treeRef)?.setCheckedKeys([])
  menuExpand.value = false
  menuNodeAll.value = false
}

// 修改操作
const handleUpdate = async (row: any) => {
  setDialogTile('update')
  // 设置数据
  const res = await TenantPackageApi.getTenantPackageApi(row.id)
  unref(formRef)?.setValues(res)
  // 设置选中
  unref(treeRef)?.setCheckedKeys(res.menuIds)
}
// 提交按钮
const submitForm = async () => {
  loading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as TenantPackageVO
    data.menuIds = treeRef.value!.getCheckedKeys(false) as string[]
    if (actionType.value === 'create') {
      await TenantPackageApi.createTenantPackageTypeApi(data)
      ElMessage.success(t('common.createSuccess'))
      console.log('new data')
    } else {
      await TenantPackageApi.updateTenantPackageTypeApi(data)
      ElMessage.success(t('common.updateSuccess'))
      console.log('edit data')
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
    await getList()
  } finally {
    loading.value = false
  }
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  await getTree()
})
// getList()
</script>

<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" @click="handleCreate">
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
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="handleUpdate(row)">
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button link type="primary" @click="delList(row.id, false)">
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="dialogTitle" maxHeight="500px" width="50%">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    >
      <template #menuIds>
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              全选/全不选:
              <el-switch
                v-model="treeNodeAll"
                inline-prompt
                active-text="是"
                inactive-text="否"
                @change="handleCheckedTreeNodeAll()"
              />
            </div>
          </template>
          <el-tree
            ref="treeRef"
            node-key="id"
            show-checkbox
            :props="defaultProps"
            :data="menuOptions"
            empty-text="加载中，请稍后"
          />
        </el-card>
      </template>
    </Form>
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
</template>
