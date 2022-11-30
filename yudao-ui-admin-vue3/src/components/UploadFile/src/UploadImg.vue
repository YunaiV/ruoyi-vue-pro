<template>
  <el-upload
    ref="uploadRef"
    :multiple="limit > 1"
    name="file"
    list-type="picture-card"
    v-model:file-list="fileList"
    :show-file-list="true"
    :action="updateUrl"
    :headers="uploadHeaders"
    :limit="limit"
    :before-upload="beforeUpload"
    :on-exceed="handleExceed"
    :on-success="handleFileSuccess"
    :on-error="excelUploadError"
    :on-remove="handleRemove"
    :on-preview="handlePictureCardPreview"
    :class="{ hide: fileList.length >= limit }"
  >
    <Icon icon="ep:upload-filled" />
  </el-upload>
  <!-- 文件列表 -->
  <Dialog v-model="dialogVisible" title="预览" width="800" append-to-body>
    <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
  </Dialog>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { propTypes } from '@/utils/propTypes'
import { getAccessToken, getTenantId } from '@/utils/auth'
import { ElUpload, UploadInstance, UploadProps, UploadRawFile, UploadUserFile } from 'element-plus'

const message = useMessage() // 消息弹窗
const emit = defineEmits(['input'])

const props = defineProps({
  imgs: propTypes.oneOfType([String, Object, Array]),
  title: propTypes.string.def('图片上传'),
  updateUrl: propTypes.string.def(import.meta.env.VITE_UPLOAD_URL),
  fileType: propTypes.array.def(['jpg', 'png', 'gif', 'jpeg']), // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileSize: propTypes.number.def(5), // 大小限制(MB)
  limit: propTypes.number.def(1), // 数量限制
  isShowTip: propTypes.bool.def(false) // 是否显示提示
})
// ========== 上传相关 ==========
const uploadRef = ref<UploadInstance>()
const uploadList = ref<UploadUserFile[]>([])
const fileList = ref<UploadUserFile[]>([])
const uploadNumber = ref<number>(0)
const dialogImageUrl = ref()
const dialogVisible = ref(false)
const uploadHeaders = ref({
  Authorization: 'Bearer ' + getAccessToken(),
  'tenant-id': getTenantId()
})
watch(
  () => props.imgs,
  (val) => {
    if (val) {
      // 首先将值转为数组, 当只穿了一个图片时，会报map方法错误
      const list = Array.isArray(props.imgs)
        ? props.imgs
        : Array.isArray(props.imgs?.split(','))
        ? props.imgs?.split(',')
        : Array.of(props.imgs)
      // 然后将数组转为对象数组
      fileList.value = list.map((item) => {
        if (typeof item === 'string') {
          // edit by 芋道源码
          item = { name: item, url: item }
        }
        return item
      })
    } else {
      fileList.value = []
      return []
    }
  },
  {
    deep: true,
    immediate: true
  }
)
// 文件上传之前判断
const beforeUpload: UploadProps['beforeUpload'] = (file: UploadRawFile) => {
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
  console.info(uploadList.value)
  console.info(fileList.value)
  uploadList.value.push({ name: res.data, url: res.data })
  if (uploadList.value.length == uploadNumber.value) {
    fileList.value = fileList.value.concat(uploadList.value)
    uploadList.value = []
    uploadNumber.value = 0
    emit('input', listToString(fileList.value))
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
    emit('input', listToString(fileList.value))
  }
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
// 预览
const handlePictureCardPreview: UploadProps['onPreview'] = (file) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}
</script>
<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
:deep(.hide .el-upload--picture-card) {
  display: none;
}
// 去掉动画效果
:deep(.el-list-enter-active, .el-list-leave-active) {
  transition: all 0s;
}

:deep(.el-list-enter, .el-list-leave-active) {
  opacity: 0;
  transform: translateY(0);
}
</style>
