<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <XButton
        type="primary"
        preIcon="ep:zoom-in"
        :title="t('action.add')"
        @click="handleCreate()"
      />
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
        <XTextButton preIcon="ep:edit" :title="t('action.edit')" @click="handleUpdate(row)" />
        <XTextButton preIcon="ep:delete" :title="t('action.del')" @click="delList(row.id, false)" />
      </template>
    </Table>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="dialogTitle" maxHeight="500px" width="50%">
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
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="loading"
        @click="submitForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="loading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
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
const menuOptions = ref<any[]>([]) // 树形结构
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
  //重置菜单树
  unref(treeRef)?.setCheckedKeys([])
  menuExpand.value = false
  menuNodeAll.value = false
  setDialogTile('create')
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
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      loading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as TenantPackageVO
        data.menuIds = treeRef.value!.getCheckedKeys(false) as string[]
        if (actionType.value === 'create') {
          await TenantPackageApi.createTenantPackageTypeApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else {
          await TenantPackageApi.updateTenantPackageTypeApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
        await getList()
      } finally {
        loading.value = false
      }
    }
  })
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  await getTree()
})
// getList()
</script>
