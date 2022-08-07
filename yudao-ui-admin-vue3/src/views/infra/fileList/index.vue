<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElUpload, UploadInstance, UploadRawFile, ElImage } from 'element-plus'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import type { FileVO } from '@/api/infra/fileList/types'
import { allSchemas } from './fileList.data'
import * as FileApi from '@/api/infra/fileList'
import { getAccessToken, getTenantId } from '@/utils/auth'

const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<FileVO>({
  getListApi: FileApi.getFilePageApi,
  delListApi: FileApi.deleteFileApi
})
const { getList, setSearchParams, delList } = methods
// ========== 上传相关 ==========
const uploadDialogVisible = ref(false)
const uploadDialogTitle = ref('上传')
const updateSupport = ref(0)
const uploadDisabled = ref(false)
const uploadRef = ref<UploadInstance>()
let updateUrl = import.meta.env.VITE_UPLOAD_URL
const uploadHeaders = ref()
// 文件上传之前判断
const beforeUpload = (file: UploadRawFile) => {
  const isImg = file.type === 'image/jpeg' || 'image/gif' || 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImg) ElMessage.error('上传文件只能是 jpeg / gif / png 格式!')
  if (!isLt5M) ElMessage.error('上传文件大小不能超过 5MB!')
  return isImg && isLt5M
}
// 处理上传的文件发生变化
// const handleFileChange = (uploadFile: UploadFile): void => {
//   uploadRef.value.data.path = uploadFile.name
// }
// 文件上传
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
    ElMessage.error(response.msg)
    return
  }
  ElMessage.success('上传成功')
  getList()
  uploadDialogVisible.value = false
  uploadDisabled.value = false
}
// 文件数超出提示
const handleExceed = (): void => {
  ElMessage.error('最多只能上传一个文件！')
}
// 上传错误提示
const excelUploadError = (): void => {
  ElMessage.error('导入数据失败，请您重新上传！')
}
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题
// 详情操作
const handleDetail = (row: FileVO) => {
  // 设置数据
  detailRef.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}
// ========== 初始化 ==========
getList()
</script>

<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <el-button type="primary" @click="uploadDialogVisible = true">
      <Icon icon="ep:upload" class="mr-5px" /> 上传文件
    </el-button>
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
      <template #url="{ row }">
        <el-image
          v-if="row.type === 'jpg' || 'png' || 'gif'"
          style="width: 80px; height: 50px"
          :src="row.url"
          :key="row.url"
          fit="contain"
          lazy
        />
        <span v-else>{{ row.url }}</span>
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['infra:file:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #url="{ row }">
        <el-image
          v-if="row.type === 'jpg' || 'png' || 'gif'"
          style="width: 100px; height: 100px"
          :src="row.url"
          :key="row.url"
          lazy
        />
        <span>{{ row.url }}</span>
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
  <Dialog v-model="uploadDialogVisible" :title="uploadDialogTitle" :destroy-on-close="true">
    <el-upload
      ref="uploadRef"
      :action="updateUrl + '?updateSupport=' + updateSupport"
      :headers="uploadHeaders"
      :drag="true"
      :limit="1"
      :multiple="true"
      :show-file-list="true"
      :disabled="uploadDisabled"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :on-success="handleFileSuccess"
      :on-error="excelUploadError"
      :auto-upload="false"
      accept=".jpg, .png, .gif"
    >
      <Icon icon="ep:upload-filled" />
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip">请上传 .jpg, .png, .gif 标准格式文件</div>
      </template>
    </el-upload>
    <template #footer>
      <el-button type="primary" @click="submitFileForm">
        <Icon icon="ep:upload-filled" />
        {{ t('action.save') }}
      </el-button>
      <el-button @click="uploadDialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
