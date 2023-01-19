<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton preIcon="ep:edit" :title="t('action.edit')" @click="handleUpdate(row.id)" />
        <XTextButton preIcon="ep:delete" :title="t('action.del')" @click="deleteData(row.id)" />
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
    >
      <template #menuIds>
        <el-card class="w-120">
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
<script setup lang="ts" name="TenantPackage">
import { handleTree, defaultProps } from '@/utils/tree'
import type { FormExpose } from '@/components/Form'
import type { ElTree } from 'element-plus'
// 业务相关的 import
import { rules, allSchemas } from './tenantPackage.data'
import * as TenantPackageApi from '@/api/system/tenantPackage'
import { listSimpleMenusApi } from '@/api/system/menu'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const menuOptions = ref<any[]>([]) // 树形结构
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeNodeAll = ref(false)
const formRef = ref<FormExpose>() // 表单 Ref
const loading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题

// 全选/全不选
const handleCheckedTreeNodeAll = () => {
  treeRef.value!.setCheckedNodes(treeNodeAll.value ? menuOptions.value : [])
}
const getTree = async () => {
  const res = await listSimpleMenusApi()
  menuOptions.value = handleTree(res)
}

const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: TenantPackageApi.getTenantPackageTypePageApi,
  deleteApi: TenantPackageApi.deleteTenantPackageTypeApi
})

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
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await TenantPackageApi.getTenantPackageApi(rowId)
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
        const data = unref(formRef)?.formModel as TenantPackageApi.TenantPackageVO
        data.menuIds = [
          ...(treeRef.value!.getCheckedKeys(false) as unknown as Array<number>),
          ...(treeRef.value!.getHalfCheckedKeys() as unknown as Array<number>)
        ]
        if (actionType.value === 'create') {
          await TenantPackageApi.createTenantPackageTypeApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await TenantPackageApi.updateTenantPackageTypeApi(data)
          message.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
      } finally {
        loading.value = false
        // 刷新列表
        await reload()
      }
    }
  })
}

// ========== 初始化 ==========
onMounted(async () => {
  await getTree()
})
// getList()
</script>
