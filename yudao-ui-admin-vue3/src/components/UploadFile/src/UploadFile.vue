<template>
  <div class="upload-file">
    <el-upload
      ref="uploadRef"
      :multiple="props.limit > 1"
      name="file"
      v-model="valueRef"
      v-model:file-list="fileList"
      :show-file-list="true"
      :auto-upload="autoUpload"
      :action="updateUrl"
      :headers="uploadHeaders"
      :limit="props.limit"
      :drag="drag"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :on-success="handleFileSuccess"
      :on-error="excelUploadError"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      class="upload-file-uploader"
    >
      <el-button type="primary"><Icon icon="ep:upload-filled" />选取文件</el-button>
      <template v-if="isShowTip" #tip>
        <div style="font-size: 8px">
          大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
        </div>
        <div style="font-size: 8px">
          格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b> 的文件
        </div>
      </template>
    </el-upload>
  </div>
</template>
<script setup lang="ts" name="UploadFile">
import { PropType } from 'vue'

import { propTypes } from '@/utils/propTypes'
import { getAccessToken, getTenantId } from '@/utils/auth'
import type { UploadInstance, UploadUserFile, UploadProps, UploadRawFile } from 'element-plus'

const message = useMessage() // 消息弹窗
const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  modelValue: {
    type: Array as PropType<UploadUserFile[]>,
    required: true
  },
  title: propTypes.string.def('文件上传'),
  updateUrl: propTypes.string.def(import.meta.env.VITE_UPLOAD_URL),
  fileType: propTypes.array.def(['doc', 'xls', 'ppt', 'txt', 'pdf']), // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileSize: propTypes.number.def(5), // 大小限制(MB)
  limit: propTypes.number.def(5), // 数量限制
  autoUpload: propTypes.bool.def(true), // 自动上传
  drag: propTypes.bool.def(false), // 拖拽上传
  isShowTip: propTypes.bool.def(true) // 是否显示提示
})
// ========== 上传相关 ==========
const valueRef = ref(props.modelValue)
const uploadRef = ref<UploadInstance>()
const uploadList = ref<UploadUserFile[]>([])
const fileList = ref<UploadUserFile[]>(props.modelValue)
const uploadNumber = ref<number>(0)
const uploadHeaders = ref({
  Authorization: 'Bearer ' + getAccessToken(),
  'tenant-id': getTenantId()
})
// 文件上传之前判断
const beforeUpload: UploadProps['beforeUpload'] = (file: UploadRawFile) => {
  if (fileList.value.length >= props.limit) {
    message.error(`上传文件数量不能超过${props.limit}个!`)
    return false
  }
  let fileExtension = ''
  if (file.name.lastIndexOf('.') > -1) {
    fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1)
  }
  const isImg = props.fileType.some((type: string) => {
    if (file.type.indexOf(type) > -1) return true
    return !!(fileExtension && fileExtension.indexOf(type) > -1)
  })
  const isLimit = file.size < props.fileSize * 1024 * 1024
  if (!isImg) {
    message.error(`文件格式不正确, 请上传${props.fileType.join('/')}格式!`)
    return false
  }
  if (!isLimit) {
    message.error(`上传文件大小不能超过${props.fileSize}MB!`)
    return false
  }
  message.success('正在上传文件，请稍候...')
  uploadNumber.value++
}
// 处理上传的文件发生变化
// const handleFileChange = (uploadFile: UploadFile): void => {
//   uploadRef.value.data.path = uploadFile.name
// }
// 文件上传成功
const handleFileSuccess: UploadProps['onSuccess'] = (res: any): void => {
  message.success('上传成功')
  const fileListNew = fileList.value
  fileListNew.pop()
  fileList.value = fileListNew
  uploadList.value.push({ name: res.data, url: res.data })
  if (uploadList.value.length == uploadNumber.value) {
    fileList.value = fileList.value.concat(uploadList.value)
    uploadList.value = []
    uploadNumber.value = 0
    emit('update:modelValue', listToString(fileList.value))
  }
}
// 文件数超出提示
const handleExceed: UploadProps['onExceed'] = (): void => {
  message.error(`上传文件数量不能超过${props.limit}个!`)
}
// 上传错误提示
const excelUploadError: UploadProps['onError'] = (): void => {
  message.error('导入数据失败，请您重新上传！')
}
// 删除上传文件
const handleRemove = (file) => {
  const findex = fileList.value.map((f) => f.name).indexOf(file.name)
  if (findex > -1) {
    fileList.value.splice(findex, 1)
    emit('update:modelValue', listToString(fileList.value))
  }
}
const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  console.log(uploadFile)
}
// 对象转成指定字符串分隔
const listToString = (list: UploadUserFile[], separator?: string) => {
  let strs = ''
  separator = separator || ','
  for (let i in list) {
    strs += list[i].url + separator
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}
</script>
<style scoped lang="scss">
.upload-file-uploader {
  margin-bottom: 5px;
}
:deep(.upload-file-list .el-upload-list__item) {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
:deep(.el-upload-list__item-file-name) {
  max-width: 250px;
}
:deep(.upload-file-list .ele-upload-list__item-content) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
:deep(.ele-upload-list__item-content-action .el-link) {
  margin-right: 10px;
}
</style>
