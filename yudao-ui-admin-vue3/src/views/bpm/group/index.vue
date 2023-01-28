<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['bpm:user-group:create']"
          @click="handleCreate()"
        />
      </template>
      <template #memberUserIds_default="{ row }">
        <span v-for="userId in row.memberUserIds" :key="userId">
          {{ getUserNickname(userId) }} &nbsp;
        </span>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['bpm:user-group:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['bpm:user-group:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['bpm:user-group:delete']"
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
    >
      <template #memberUserIds="form">
        <el-select v-model="form.memberUserIds" multiple>
          <el-option v-for="item in users" :key="item.id" :label="item.nickname" :value="item.id" />
        </el-select>
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    >
      <template #memberUserIds="{ row }">
        <span v-for="userId in row.memberUserIds" :key="userId">
          {{ getUserNickname(userId) }} &nbsp;
        </span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>

<script setup lang="ts">
// 业务相关的 import
import * as UserGroupApi from '@/api/bpm/userGroup'
import { getListSimpleUsersApi, UserVO } from '@/api/system/user'
import { allSchemas, rules } from './group.data'
import { FormExpose } from '@/components/Form'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: UserGroupApi.getUserGroupPageApi,
  deleteApi: UserGroupApi.deleteUserGroupApi
})
// 用户列表
const users = ref<UserVO[]>([])

const getUserNickname = (userId) => {
  for (const user of users.value) {
    if (user.id === userId) {
      return user.nickname
    }
  }
  return '未知(' + userId + ')'
}

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
  const res = await UserGroupApi.getUserGroupApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  detailData.value = await UserGroupApi.getUserGroupApi(rowId)
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
        const data = unref(formRef)?.formModel as UserGroupApi.UserGroupVO
        if (actionType.value === 'create') {
          await UserGroupApi.createUserGroupApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await UserGroupApi.updateUserGroupApi(data)
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

// ========== 初始化 ==========
onMounted(() => {
  getListSimpleUsersApi().then((data) => {
    users.value = data
  })
})
</script>
