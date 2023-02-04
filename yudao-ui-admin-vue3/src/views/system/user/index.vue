<template>
  <div class="flex">
    <el-card class="w-1/5 user" :gutter="12" shadow="always">
      <template #header>
        <div class="card-header">
          <span>部门列表</span>
          <XTextButton title="修改部门" @click="handleDeptEdit()" />
        </div>
      </template>
      <el-input v-model="filterText" placeholder="搜索部门" />
      <el-scrollbar height="650">
        <el-tree
          ref="treeRef"
          node-key="id"
          default-expand-all
          :data="deptOptions"
          :props="defaultProps"
          :highlight-current="true"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          @node-click="handleDeptNodeClick"
        />
      </el-scrollbar>
    </el-card>
    <el-card class="w-4/5 user" style="margin-left: 10px" :gutter="12" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ tableTitle }}</span>
        </div>
      </template>
      <!-- 列表 -->
      <XTable @register="registerTable">
        <template #toolbar_buttons>
          <!-- 操作：新增 -->
          <XButton
            type="primary"
            preIcon="ep:zoom-in"
            :title="t('action.add')"
            v-hasPermi="['system:user:create']"
            @click="handleCreate()"
          />
          <!-- 操作：导入用户 -->
          <XButton
            type="warning"
            preIcon="ep:upload"
            :title="t('action.import')"
            v-hasPermi="['system:user:import']"
            @click="importDialogVisible = true"
          />
          <!-- 操作：导出用户 -->
          <XButton
            type="warning"
            preIcon="ep:download"
            :title="t('action.export')"
            v-hasPermi="['system:user:export']"
            @click="exportList('用户数据.xls')"
          />
        </template>
        <template #status_default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(row)"
          />
        </template>
        <template #actionbtns_default="{ row }">
          <!-- 操作：编辑 -->
          <XTextButton
            preIcon="ep:edit"
            :title="t('action.edit')"
            v-hasPermi="['system:user:update']"
            @click="handleUpdate(row.id)"
          />
          <!-- 操作：详情 -->
          <XTextButton
            preIcon="ep:view"
            :title="t('action.detail')"
            v-hasPermi="['system:user:update']"
            @click="handleDetail(row.id)"
          />
          <el-dropdown
            class="p-0.5"
            v-hasPermi="[
              'system:user:update-password',
              'system:permission:assign-user-role',
              'system:user:delete'
            ]"
          >
            <XTextButton :title="t('action.more')" postIcon="ep:arrow-down" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <!-- 操作：重置密码 -->
                  <XTextButton
                    preIcon="ep:key"
                    title="重置密码"
                    v-hasPermi="['system:user:update-password']"
                    @click="handleResetPwd(row)"
                  />
                </el-dropdown-item>
                <el-dropdown-item>
                  <!-- 操作：分配角色 -->
                  <XTextButton
                    preIcon="ep:key"
                    title="分配角色"
                    v-hasPermi="['system:permission:assign-user-role']"
                    @click="handleRole(row)"
                  />
                </el-dropdown-item>
                <el-dropdown-item>
                  <!-- 操作：删除 -->
                  <XTextButton
                    preIcon="ep:delete"
                    :title="t('action.del')"
                    v-hasPermi="['system:user:delete']"
                    @click="deleteData(row.id)"
                  />
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </XTable>
    </el-card>
  </div>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :rules="rules"
      :schema="allSchemas.formSchema"
      ref="formRef"
    >
      <template #deptId="form">
        <el-tree-select
          node-key="id"
          v-model="form['deptId']"
          :props="defaultProps"
          :data="deptOptions"
          check-strictly
        />
      </template>
      <template #postIds="form">
        <el-select v-model="form['postIds']" multiple :placeholder="t('common.selectText')">
          <el-option
            v-for="item in postOptions"
            :key="item.id"
            :label="item.name"
            :value="(item.id as unknown as number)"
          />
        </el-select>
      </template>
    </Form>
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    >
      <template #deptId="{ row }">
        <span>{{ row.dept?.name }}</span>
      </template>
      <template #postIds="{ row }">
        <template v-if="row.postIds !== ''">
          <el-tag v-for="(post, index) in row.postIds" :key="index" index="">
            <template v-for="postObj in postOptions">
              {{ post === postObj.id ? postObj.name : '' }}
            </template>
          </el-tag>
        </template>
        <template v-else> </template>
      </template>
    </Descriptions>
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
  <!-- 分配用户角色 -->
  <XModal v-model="roleDialogVisible" title="分配角色">
    <el-form :model="userRole" label-width="140px" :inline="true">
      <el-form-item label="用户名称">
        <el-tag>{{ userRole.username }}</el-tag>
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-tag>{{ userRole.nickname }}</el-tag>
      </el-form-item>
      <el-form-item label="角色">
        <el-transfer
          v-model="userRole.roleIds"
          :titles="['角色列表', '已选择']"
          :props="{
            key: 'id',
            label: 'name'
          }"
          :data="roleOptions"
        />
      </el-form-item>
    </el-form>
    <!-- 操作按钮 -->
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton type="primary" :title="t('action.save')" :loading="loading" @click="submitRole()" />
      <!-- 按钮：关闭 -->
      <XButton :title="t('dialog.close')" @click="roleDialogVisible = false" />
    </template>
  </XModal>
  <!-- 导入 -->
  <XModal v-model="importDialogVisible" :title="importDialogTitle">
    <el-form class="drawer-multiColumn-form" label-width="150px">
      <el-form-item label="模板下载 :">
        <XButton type="primary" prefix="ep:download" title="点击下载" @click="handleImportTemp()" />
      </el-form-item>
      <el-form-item label="文件上传 :">
        <el-upload
          ref="uploadRef"
          :action="updateUrl + '?updateSupport=' + updateSupport"
          :headers="uploadHeaders"
          :drag="true"
          :limit="1"
          :multiple="true"
          :show-file-list="true"
          :disabled="uploadDisabled"
          :before-upload="beforeExcelUpload"
          :on-exceed="handleExceed"
          :on-success="handleFileSuccess"
          :on-error="excelUploadError"
          :auto-upload="false"
          accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        >
          <Icon icon="ep:upload-filled" />
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">请上传 .xls , .xlsx 标准格式文件</div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="是否更新已经存在的用户数据:">
        <el-checkbox v-model="updateSupport" />
      </el-form-item>
    </el-form>
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        type="warning"
        preIcon="ep:upload-filled"
        :title="t('action.save')"
        @click="submitFileForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :title="t('dialog.close')" @click="importDialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="User">
import type { ElTree, UploadRawFile, UploadInstance } from 'element-plus'
import { handleTree, defaultProps } from '@/utils/tree'
import download from '@/utils/download'
import { CommonStatusEnum } from '@/utils/constants'
import { getAccessToken, getTenantId } from '@/utils/auth'
import type { FormExpose } from '@/components/Form'
import { rules, allSchemas } from './user.data'
import * as UserApi from '@/api/system/user'
import { listSimpleDeptApi } from '@/api/system/dept'
import { listSimpleRolesApi } from '@/api/system/role'
import { listSimplePostsApi, PostVO } from '@/api/system/post'
import {
  aassignUserRoleApi,
  listUserRolesApi,
  PermissionAssignUserRoleReqVO
} from '@/api/system/permission'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const queryParams = reactive({
  deptId: null
})
// ========== 列表相关 ==========
const tableTitle = ref('用户列表')
// 列表相关的变量
const [registerTable, { reload, deleteData, exportList }] = useXTable({
  allSchemas: allSchemas,
  params: queryParams,
  getListApi: UserApi.getUserPageApi,
  deleteApi: UserApi.deleteUserApi,
  exportListApi: UserApi.exportUserApi
})
// ========== 创建部门树结构 ==========
const filterText = ref('')
const deptOptions = ref<Tree[]>([]) // 树形结构
const treeRef = ref<InstanceType<typeof ElTree>>()
const getTree = async () => {
  const res = await listSimpleDeptApi()
  deptOptions.value.push(...handleTree(res))
}
const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.includes(value)
}
const handleDeptNodeClick = async (row: { [key: string]: any }) => {
  queryParams.deptId = row.id
  await reload()
}
const { push } = useRouter()
const handleDeptEdit = () => {
  push('/system/dept')
}
watch(filterText, (val) => {
  treeRef.value!.filter(val)
})
// ========== CRUD 相关 ==========
const loading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref
const postOptions = ref<PostVO[]>([]) //岗位列表

// 获取岗位列表
const getPostOptions = async () => {
  const res = await listSimplePostsApi()
  postOptions.value.push(...res)
}
// 设置标题
const setDialogTile = async (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = async () => {
  setDialogTile('create')
  // 重置表单
  await nextTick()
  if (allSchemas.formSchema[0].field !== 'username') {
    unref(formRef)?.addSchema(
      {
        field: 'username',
        label: '用户账号',
        component: 'Input'
      },
      0
    )
    unref(formRef)?.addSchema(
      {
        field: 'password',
        label: '用户密码',
        component: 'InputPassword'
      },
      1
    )
  }
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  await nextTick()
  unref(formRef)?.delSchema('username')
  unref(formRef)?.delSchema('password')
  // 设置数据
  const res = await UserApi.getUserApi(rowId)
  unref(formRef)?.setValues(res)
}
const detailData = ref()

// 详情操作
const handleDetail = async (rowId: number) => {
  // 设置数据
  const res = await UserApi.getUserApi(rowId)
  detailData.value = res
  await setDialogTile('detail')
}

// 提交按钮
const submitForm = async () => {
  loading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as UserApi.UserVO
    if (actionType.value === 'create') {
      await UserApi.createUserApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await UserApi.updateUserApi(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
  } finally {
    // unref(formRef)?.setSchema(allSchemas.formSchema)
    // 刷新列表
    await reload()
    loading.value = false
  }
}
// 改变用户状态操作
const handleStatusChange = async (row: UserApi.UserVO) => {
  const text = row.status === CommonStatusEnum.ENABLE ? '启用' : '停用'
  message
    .confirm('确认要"' + text + '""' + row.username + '"用户吗?', t('common.reminder'))
    .then(async () => {
      row.status =
        row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.ENABLE : CommonStatusEnum.DISABLE
      await UserApi.updateUserStatusApi(row.id, row.status)
      message.success(text + '成功')
      // 刷新列表
      await reload()
    })
    .catch(() => {
      row.status =
        row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.DISABLE : CommonStatusEnum.ENABLE
    })
}
// 重置密码
const handleResetPwd = (row: UserApi.UserVO) => {
  message.prompt('请输入"' + row.username + '"的新密码', t('common.reminder')).then(({ value }) => {
    UserApi.resetUserPwdApi(row.id, value).then(() => {
      message.success('修改成功，新密码是：' + value)
    })
  })
}
// 分配角色
const roleDialogVisible = ref(false)
const roleOptions = ref()
const userRole = reactive({
  id: 0,
  username: '',
  nickname: '',
  roleIds: []
})
const handleRole = async (row: UserApi.UserVO) => {
  userRole.id = row.id
  userRole.username = row.username
  userRole.nickname = row.nickname
  // 获得角色拥有的权限集合
  const roles = await listUserRolesApi(row.id)
  userRole.roleIds = roles
  // 获取角色列表
  const roleOpt = await listSimpleRolesApi()
  roleOptions.value = roleOpt
  roleDialogVisible.value = true
}
// 提交
const submitRole = async () => {
  const data = ref<PermissionAssignUserRoleReqVO>({
    userId: userRole.id,
    roleIds: userRole.roleIds
  })
  await aassignUserRoleApi(data.value)
  message.success(t('common.updateSuccess'))
  roleDialogVisible.value = false
}
// ========== 导入相关 ==========
// TODO @星语：这个要不要把导入用户，封装成一个小组件？可选哈
const importDialogVisible = ref(false)
const uploadDisabled = ref(false)
const importDialogTitle = ref('用户导入')
const updateSupport = ref(0)
let updateUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/system/user/import'
const uploadHeaders = ref()
// 下载导入模版
const handleImportTemp = async () => {
  const res = await UserApi.importUserTemplateApi()
  download.excel(res, '用户导入模版.xls')
}
// 文件上传之前判断
const beforeExcelUpload = (file: UploadRawFile) => {
  const isExcel =
    file.type === 'application/vnd.ms-excel' ||
    file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isExcel) message.error('上传文件只能是 xls / xlsx 格式!')
  if (!isLt5M) message.error('上传文件大小不能超过 5MB!')
  return isExcel && isLt5M
}
// 文件上传
const uploadRef = ref<UploadInstance>()
const submitFileForm = () => {
  uploadHeaders.value = {
    Authorization: 'Bearer ' + getAccessToken(),
    'tenant-id': getTenantId()
  }
  uploadDisabled.value = true
  uploadRef.value!.submit()
}
// 文件上传成功
const handleFileSuccess = async (response: any): Promise<void> => {
  if (response.code !== 0) {
    message.error(response.msg)
    return
  }
  importDialogVisible.value = false
  uploadDisabled.value = false
  const data = response.data
  let text = '上传成功数量：' + data.createUsernames.length + ';'
  for (let username of data.createUsernames) {
    text += '< ' + username + ' >'
  }
  text += '更新成功数量：' + data.updateUsernames.length + ';'
  for (const username of data.updateUsernames) {
    text += '< ' + username + ' >'
  }
  text += '更新失败数量：' + Object.keys(data.failureUsernames).length + ';'
  for (const username in data.failureUsernames) {
    text += '< ' + username + ': ' + data.failureUsernames[username] + ' >'
  }
  message.alert(text)
  await reload()
}
// 文件数超出提示
const handleExceed = (): void => {
  message.error('最多只能上传一个文件！')
}
// 上传错误提示
const excelUploadError = (): void => {
  message.error('导入数据失败，请您重新上传！')
}
// ========== 初始化 ==========
onMounted(async () => {
  await getPostOptions()
  await getTree()
})
</script>

<style scoped>
.user {
  height: 780px;
  max-height: 800px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
