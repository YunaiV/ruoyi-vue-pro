<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:upload"
          title="上传文件"
          @click="uploadDialogVisible = true"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:copy-document"
          :title="t('common.copy')"
          @click="handleCopy(row.url)"
        />
        <XTextButton preIcon="ep:view" :title="t('action.detail')" @click="handleDetail(row)" />
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['infra:file:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailData">
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
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
  <XModal v-model="uploadDialogVisible" :title="uploadDialogTitle">
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
      <!-- 按钮：保存 -->
      <XButton
        type="primary"
        prefix="ep:upload-filled"
        :title="t('action.save')"
        @click="submitFileForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :title="t('dialog.close')" @click="uploadDialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="FileList">
import { ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { ElUpload, ElImage, UploadInstance, UploadRawFile } from 'element-plus'
// 业务相关的 import
import { allSchemas } from './fileList.data'
import * as FileApi from '@/api/infra/fileList'
import { getAccessToken, getTenantId } from '@/utils/auth'
import { useClipboard } from '@vueuse/core'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData } = useVxeGrid<FileApi.FileVO>({
  allSchemas: allSchemas,
  getListApi: FileApi.getFilePageApi,
  deleteApi: FileApi.deleteFileApi
})

const detailData = ref() // 详情 Ref
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('') // 弹出层标题
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
  if (!isImg) message.error('上传文件只能是 jpeg / gif / png 格式!')
  if (!isLt5M) message.error('上传文件大小不能超过 5MB!')
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
const handleFileSuccess = async (response: any): Promise<void> => {
  if (response.code !== 0) {
    message.error(response.msg)
    return
  }
  message.success('上传成功')
  uploadDialogVisible.value = false
  uploadDisabled.value = false
  await getList(xGrid)
}
// 文件数超出提示
const handleExceed = (): void => {
  message.error('最多只能上传一个文件！')
}
// 上传错误提示
const excelUploadError = (): void => {
  message.error('导入数据失败，请您重新上传！')
}

// 详情操作
const handleDetail = (row: FileApi.FileVO) => {
  // 设置数据
  detailData.value = row
  dialogTitle.value = t('action.detail')
  dialogVisible.value = true
}

// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
}

// ========== 复制相关 ==========
const handleCopy = async (text: string) => {
  const { copy, copied, isSupported } = useClipboard({ source: text })
  if (!isSupported) {
    message.error(t('common.copyError'))
  } else {
    await copy()
    if (unref(copied)) {
      message.success(t('common.copySuccess'))
    }
  }
}
</script>
