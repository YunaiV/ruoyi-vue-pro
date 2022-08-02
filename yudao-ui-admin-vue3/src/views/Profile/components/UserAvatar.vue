<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { ElRow, ElCol, ElUpload, ElMessage, ElDialog } from 'element-plus'
import { propTypes } from '@/utils/propTypes'
import { uploadAvatarApi } from '@/api/system/user/profile'
const cropper = ref()
const dialogVisible = ref(false)
const cropperVisible = ref(false)
const props = defineProps({
  img: propTypes.string.def('')
})
const options = reactive({
  dialogTitle: '编辑头像',
  options: {
    img: props.img, //裁剪图片的地址
    autoCrop: true, // 是否默认生成截图框
    autoCropWidth: 200, // 默认生成截图框宽度
    autoCropHeight: 200, // 默认生成截图框高度
    fixedBox: true // 固定截图框大小 不允许改变
  },
  previews: {
    img: '',
    url: ''
  }
})
/** 编辑头像 */
const editCropper = () => {
  dialogVisible.value = true
}
// 打开弹出层结束时的回调
const modalOpened = () => {
  cropperVisible.value = true
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
    reader.readAsDataURL(file)
    reader.onload = () => {
      if (reader.result) {
        options.options.img = reader.result as string
      }
    }
  }
}
/** 上传图片 */
const uploadImg = () => {
  cropper.value.getCropBlob((data: any) => {
    let formData = new FormData()
    formData.append('avatarfile', data)
    uploadAvatarApi(formData)
  })
}
/** 实时预览 */
const realTime = (data: any) => {
  options.previews = data
}
watch(
  () => props.img,
  () => {
    if (props.img) {
      options.options.img = props.img
      options.previews.img = props.img
      options.previews.url = props.img
    }
  }
)
</script>
<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.options.img" title="点击上传头像" class="img-circle img-lg" alt="" />
  </div>
  <el-dialog
    v-model="dialogVisible"
    :title="options.dialogTitle"
    width="800px"
    append-to-body
    style="padding: 30px 20px"
    @opened="modalOpened"
  >
    <el-row>
      <el-col :xs="24" :md="12" style="height: 350px">
        <VueCropper
          ref="cropper"
          :img="options.options.img"
          :info="true"
          :autoCrop="options.options.autoCrop"
          :autoCropWidth="options.options.autoCropWidth"
          :autoCropHeight="options.options.autoCropHeight"
          :fixedBox="options.options.fixedBox"
          @real-time="realTime"
          v-if="cropperVisible"
        />
      </el-col>
      <el-col :xs="24" :md="12" style="height: 350px">
        <div class="avatar-upload-preview">
          <img
            :src="options.previews.url"
            :style="options.previews.img"
            style="!max-width: 100%"
            alt=""
          />
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
