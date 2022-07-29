<script setup lang="ts">
import { onMounted, ref, unref, watch } from 'vue'
import dayjs from 'dayjs'
import {
  ElInput,
  ElCard,
  ElTree,
  ElTreeSelect,
  ElSelect,
  ElOption,
  ElForm,
  ElFormItem,
  ElUpload,
  ElSwitch,
  ElCheckbox,
  UploadInstance,
  UploadRawFile
} from 'element-plus'
import { handleTree } from '@/utils/tree'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { useTable } from '@/hooks/web/useTable'
import { FormExpose } from '@/components/Form'
import type { UserVO } from '@/api/system/user/types'
import type { PostVO } from '@/api/system/post/types'
import { listSimpleDeptApi } from '@/api/system/dept'
import { listSimplePostsApi } from '@/api/system/post'
import { rules, allSchemas } from './user.data'
import * as UserApi from '@/api/system/user'
import download from '@/utils/download'
import { CommonStatusEnum } from '@/utils/constants'
import { getAccessToken, getTenantId } from '@/utils/auth'
import { useMessage } from '@/hooks/web/useMessage'
const message = useMessage()
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
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const tableTitle = ref('用户列表')
const { register, tableObject, methods } = useTable<UserVO>({
  getListApi: UserApi.getUserPageApi,
  delListApi: UserApi.deleteUserApi,
  exportListApi: UserApi.exportUserApi
})
const { getList, setSearchParams, delList, exportList } = methods

// ========== 创建部门树结构 ==========
const filterText = ref('')
const deptOptions = ref([]) // 树形结构
const searchForm = ref<FormExpose>()
const treeRef = ref<InstanceType<typeof ElTree>>()
const getTree = async () => {
  const res = await listSimpleDeptApi()
  deptOptions.value.push(...handleTree(res))
}
const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.includes(value)
}
const handleDeptNodeClick = (data: { [key: string]: any }) => {
  tableObject.params = {
    deptId: data.id
  }
  tableTitle.value = data.name
  methods.getList()
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
const deptId = ref(0) // 部门ID
const postIds = ref<string[]>([]) // 岗位ID
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
const handleAdd = () => {
  setDialogTile('create')
  // 重置表单
  deptId.value = 0
  unref(formRef)?.getElFormRef()?.resetFields()
}

// 修改操作
const handleUpdate = async (row: UserVO) => {
  await setDialogTile('update')
  // 设置数据
  const res = await UserApi.getUserApi(row.id)
  deptId.value = res.deptId
  postIds.value = res.postIds
  unref(formRef)?.setValues(res)
}

// 提交按钮
const submitForm = async () => {
  loading.value = true
  // 提交请求
  try {
    const data = unref(formRef)?.formModel as UserVO
    data.deptId = deptId.value
    data.postIds = postIds.value
    if (actionType.value === 'create') {
      await UserApi.createUserApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await UserApi.updateUserApi(data)
      message.success(t('common.updateSuccess'))
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
    await getList()
  } finally {
    loading.value = false
  }
}
// 改变用户状态操作
const handleStatusChange = async (row: UserVO) => {
  const text = row.status === CommonStatusEnum.ENABLE ? '启用' : '停用'
  message
    .confirm('确认要"' + text + '""' + row.username + '"用户吗?', t('common.reminder'))
    .then(async () => {
      row.status =
        row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.ENABLE : CommonStatusEnum.DISABLE
      await UserApi.updateUserStatusApi(row.id, row.status)
      message.success(text + '成功')
      await getList()
    })
    .catch(() => {
      row.status =
        row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.DISABLE : CommonStatusEnum.ENABLE
    })
}
// 重置密码
const handleResetPwd = (row: UserVO) => {
  message.prompt('请输入"' + row.username + '"的新密码', t('common.reminder')).then(({ value }) => {
    UserApi.resetUserPwdApi(row.id, value).then(() => {
      message.success('修改成功，新密码是：' + value)
    })
  })
}

// ========== 详情相关 ==========
const detailRef = ref()

// 详情操作
const handleDetail = async (row: UserVO) => {
  // 设置数据
  detailRef.value = row
  await setDialogTile('detail')
}
// ========== 导入相关 ==========
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
const handleFileSuccess = (response: any): void => {
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
  await getTree()
  await getPostOptions()
})
getList()
</script>

<template>
  <div class="flex">
    <el-card class="w-1/5 user" :gutter="12" shadow="always">
      <template #header>
        <div class="card-header">
          <span>部门列表</span>
        </div>
      </template>
      <el-input v-model="filterText" placeholder="搜索部门" />
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
    </el-card>
    <!-- 搜索工作区 -->
    <el-card class="w-4/5 user" style="margin-left: 10px" :gutter="12" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ tableTitle }}</span>
        </div>
      </template>
      <Search
        :schema="allSchemas.searchSchema"
        @search="setSearchParams"
        @reset="setSearchParams"
        ref="searchForm"
      />
      <!-- 操作工具栏 -->
      <div class="mb-10px">
        <el-button type="primary" v-hasPermi="['system:user:create']" @click="handleAdd">
          <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
        <el-button
          type="info"
          v-hasPermi="['system:user:import']"
          @click="importDialogVisible = true"
        >
          <Icon icon="ep:upload" class="mr-5px" /> {{ t('action.import') }}
        </el-button>
        <el-button
          type="warning"
          v-hasPermi="['system:user:export']"
          @click="exportList('用户数据.xls')"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
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
        <template #sex="{ row }">
          <DictTag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="row.sex" />
        </template>
        <template #status="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(row)"
          />
        </template>
        <template #loginDate="{ row }">
          <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </template>
        <template #action="{ row }">
          <el-button
            link
            type="primary"
            v-hasPermi="['system:user:update']"
            @click="handleUpdate(row)"
          >
            <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:user:update']"
            @click="handleDetail(row)"
          >
            <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:user:update-password']"
            @click="handleResetPwd(row)"
          >
            <Icon icon="ep:key" class="mr-1px" /> 重置密码
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:user:delete']"
            @click="delList(row.id, false)"
          >
            <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
          </el-button>
        </template>
      </Table>
    </el-card>
  </div>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :rules="rules"
      :schema="allSchemas.formSchema"
      ref="formRef"
    >
      <template #deptId>
        <el-tree-select
          node-key="id"
          v-model="deptId"
          :props="defaultProps"
          :data="deptOptions"
          check-strictly
        />
      </template>
      <template #postIds>
        <el-select v-model="postIds" multiple :placeholder="t('common.selectText')">
          <el-option
            v-for="item in postOptions"
            :key="item.id"
            :label="item.name"
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
      <template #deptId="{ row }">
        <span>{{ row.dept.name }}</span>
      </template>
      <template #postIds="{ row }">
        <span>{{ row.dept.name }}</span>
      </template>
      <template #sex="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="row.sex" />
      </template>
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #loginDate="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
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
  <!-- 导入 -->
  <Dialog
    v-model="importDialogVisible"
    :title="importDialogTitle"
    :destroy-on-close="true"
    maxHeight="350px"
  >
    <el-form class="drawer-multiColumn-form" label-width="150px">
      <el-form-item label="模板下载 :">
        <el-button type="primary" @click="handleImportTemp">
          <Icon icon="ep:download" />
          点击下载
        </el-button>
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
      <el-button type="primary" @click="submitFileForm">
        <Icon icon="ep:upload-filled" />
        {{ t('action.save') }}
      </el-button>
      <el-button @click="importDialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>

<style scoped>
.user {
  height: 900px;
  max-height: 960px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
