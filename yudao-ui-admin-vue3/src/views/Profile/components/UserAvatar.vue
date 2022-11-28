<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="props.img" title="点击上传头像" class="img-circle img-lg" alt="" />
  </div>
  <el-dialog
    v-model="dialogVisible"
    title="编辑头像"
    :mask-closable="false"
    width="800px"
    append-to-body
    @opened="cropperVisible = true"
  >
    <el-row>
      <el-col :xs="24" :md="12" :style="{ height: '350px' }">
        <VueCropper
          ref="cropper"
          v-if="cropperVisible"
          :img="options.img"
          :info="true"
          :infoTrue="options.infoTrue"
          :autoCrop="options.autoCrop"
          :autoCropWidth="options.autoCropWidth"
          :autoCropHeight="options.autoCropHeight"
          :fixedNumber="options.fixedNumber"
          :fixedBox="options.fixedBox"
          :centerBox="options.centerBox"
          @real-time="realTime"
        />
      </el-col>
      <el-col :xs="24" :md="12" :style="{ height: '350px' }">
        <div
          class="avatar-upload-preview"
          :style="{
            width: previews.w + 'px',
            height: previews.h + 'px',
            overflow: 'hidden',
            margin: '5px'
          }"
        >
          <div :style="previews.div">
            <img :src="previews.url" :style="previews.img" style="!max-width: 100%" alt="" />
          </div>
        </div>
      </el-col>
    </el-row>
    <template #footer>
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload
            action="#"
            :http-request="requestUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-button size="small">
              <Icon icon="ep:upload-filled" class="mr-5px" />
              选择
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button size="small" @click="changeScale(1)">
            <Icon icon="ep:zoom-in" class="mr-5px" />
          </el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button size="small" @click="changeScale(-1)">
            <Icon icon="ep:zoom-out" class="mr-5px" />
          </el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button size="small" @click="rotateLeft()">
            <Icon icon="ep:arrow-left-bold" class="mr-5px" />
          </el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button size="small" @click="rotateRight()">
            <Icon icon="ep:arrow-right-bold" class="mr-5px" />
          </el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button size="small" type="primary" @click="uploadImg()">提 交</el-button>
        </el-col>
      </el-row>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import { ref, reactive, watch, Ref, UnwrapNestedRefs } from 'vue'
import VueCropper from 'vue-cropper/lib/vue-cropper.vue'
import 'vue-cropper/dist/index.css'
import { ElRow, ElCol, ElUpload, ElMessage, ElDialog } from 'element-plus'
import { propTypes } from '@/utils/propTypes'
import { uploadAvatarApi } from '@/api/system/user/profile'

const cropper = ref()
const dialogVisible = ref(false)
const cropperVisible = ref(false)
const props = defineProps({
  img: propTypes.string.def('')
})
interface Options {
  img: string | ArrayBuffer | null // 裁剪图片的地址
  info: true // 裁剪框的大小信息
  outputSize: number // 裁剪生成图片的质量 [1至0.1]
  outputType: 'jpeg' // 裁剪生成图片的格式
  canScale: boolean // 图片是否允许滚轮缩放
  autoCrop: boolean // 是否默认生成截图框
  autoCropWidth: number // 默认生成截图框宽度
  autoCropHeight: number // 默认生成截图框高度
  fixedBox: boolean // 固定截图框大小 不允许改变
  fixed: boolean // 是否开启截图框宽高固定比例
  fixedNumber: Array<number> // 截图框的宽高比例  需要配合centerBox一起使用才能生效
  full: boolean // 是否输出原图比例的截图
  canMoveBox: boolean // 截图框能否拖动
  original: boolean // 上传图片按照原始比例渲染
  centerBox: boolean // 截图框是否被限制在图片里面
  infoTrue: boolean // true 为展示真实输出图片宽高 false 展示看到的截图框宽高
}
const options: UnwrapNestedRefs<Options> = reactive({
  img: '', // 需要剪裁的图片
  autoCrop: true, // 是否默认生成截图框
  autoCropWidth: 200, // 默认生成截图框的宽度
  autoCropHeight: 200, // 默认生成截图框的长度
  fixedBox: false, // 是否固定截图框的大小 不允许改变
  info: true, // 裁剪框的大小信息
  outputSize: 1, // 裁剪生成图片的质量 [1至0.1]
  outputType: 'jpeg', // 裁剪生成图片的格式
  canScale: false, // 图片是否允许滚轮缩放
  fixed: true, // 是否开启截图框宽高固定比例
  fixedNumber: [1, 1], // 截图框的宽高比例 需要配合centerBox一起使用才能生效
  full: true, // 是否输出原图比例的截图
  canMoveBox: false, // 截图框能否拖动
  original: false, // 上传图片按照原始比例渲染
  centerBox: true, // 截图框是否被限制在图片里面
  infoTrue: true // true 为展示真实输出图片宽高 false 展示看到的截图框宽高
})
const previews: Ref<any> = ref({})
/** 编辑头像 */
const editCropper = () => {
  dialogVisible.value = true
}
/** 向左旋转 */
const rotateLeft = () => {
  cropper.value.rotateLeft()
}
/** 向右旋转 */
const rotateRight = () => {
  cropper.value.rotateRight()
}
/** 图片缩放 */
const changeScale = (num: number) => {
  num = num || 1
  cropper.value.changeScale(num)
}
// 覆盖默认的上传行为
const requestUpload: any = () => {}
/** 上传预处理 */
const beforeUpload = (file: Blob) => {
  if (file.type.indexOf('image/') == -1) {
    ElMessage('文件格式错误，请上传图片类型,如:JPG,PNG后缀的文件。')
  } else {
    const reader = new FileReader()
    // 转化为base64
    reader.readAsDataURL(file)
    reader.onload = () => {
      if (reader.result) {
        // 获取到需要剪裁的图片 展示到剪裁框中
        options.img = reader.result as string
      }
      return false
    }
  }
}
/** 上传图片 */
const uploadImg = () => {
  cropper.value.getCropBlob((data: any) => {
    let formData = new FormData()
    formData.append('avatarFile', data)
    uploadAvatarApi(formData).then((res) => {
      options.img = res
      window.location.reload()
    })
    dialogVisible.value = false
    cropperVisible.value = false
  })
}
/** 实时预览 */
const realTime = (data: any) => {
  previews.value = data
}
watch(
  () => props.img,
  () => {
    if (props.img) {
      options.img = props.img
      previews.value.img = props.img
      previews.value.url = props.img
    }
  }
)
</script>

<style scoped>
.user-info-head {
  position: relative;
  display: inline-block;
}
.img-circle {
  border-radius: 50%;
}
.img-lg {
  width: 120px;
  height: 120px;
}
.avatar-upload-preview {
  position: absolute;
  top: 50%;
  -webkit-transform: translate(50%, -50%);
  transform: translate(50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  -webkit-box-shadow: 0 0 4px #ccc;
  box-shadow: 0 0 4px #ccc;
  overflow: hidden;
}
.user-info-head:hover:after {
  content: '+';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
