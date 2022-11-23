<script setup lang="ts" name="Group">
import { ref, unref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElSelect, ElOption } from 'element-plus'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import type { UserGroupVO } from '@/api/bpm/userGroup/types'
import { rules, allSchemas } from './group.data'
import * as UserGroupApi from '@/api/bpm/userGroup'
import { getListSimpleUsersApi } from '@/api/system/user'
import { UserVO } from '@/api/system/user'

const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<UserGroupVO>({
  getListApi: UserGroupApi.getUserGroupPageApi,
  delListApi: UserGroupApi.deleteUserGroupApi
})
const { getList, setSearchParams, delList } = methods

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref

// ========== 用户选择  ==========
const userIds = ref<number[]>([])
const userOptions = ref<UserVO[]>([])
const getUserOptions = async () => {
  const res = await getListSimpleUsersApi()
  userOptions.value.push(...res)
}

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  userIds.value = []
}

// 修改操作
const handleUpdate = async (row: UserGroupVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await UserGroupApi.getUserGroupApi(row.id)
  userIds.value = res.memberUserIds
  unref(formRef)?.setValues(res)
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
        const data = unref(formRef)?.formModel as UserGroupVO
        data.memberUserIds = userIds.value
        if (actionType.value === 'create') {
          await UserGroupApi.createUserGroupApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else {
          await UserGroupApi.updateUserGroupApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
        await getList()
      } finally {
        actionLoading.value = false
      }
    }
  })
}

// 根据用户名获取用户真实名
const getUserNickName = (userId: number) => {
  for (const user of userOptions.value) {
    if (user.id === userId) return user.nickname
  }
  return '未知(' + userId + ')'
}
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: UserGroupVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  await getUserOptions()
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
      <el-button type="primary" v-hasPermi="['bpm:user-group:create']" @click="handleCreate()">
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
      <template #memberUserIds="{ row }">
        <span v-for="userId in row.memberUserIds" :key="userId">
          {{ getUserNickName(userId) + ' ' }}
        </span>
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:user-group:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:user-group:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['bpm:user-group:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>

  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    >
      <template #memberUserIds>
        <el-select v-model="userIds" multiple>
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
          />
        </el-select>
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailRef"
    >
      <template #memberUserIds="{ row }">
        <span v-for="userId in row.memberUserIds" :key="userId">
          {{ getUserNickName(userId) + ' ' }}
        </span>
      </template>
    </Descriptions>
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
