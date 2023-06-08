<template>
  <div class="editPage__video">
      <el-upload
        class="uploader"
        list-type="picture-card"
        :action="uploadUrl"
        :on-success="handleSuccess"
        :before-upload="beforeUpload"
        :headers="headers"
        :on-error="handleError"
        :show-file-list="false"
      >

        <div v-if="uploadFlag" @mouseenter="mouseover" @mouseleave="mouseout">
          <i class="el-icon-success success-icon"></i>
          <div :class="{'hide': activeHover, 'success': !activeHover}">
            <span class="item-actions">
              <span
                class="item-preview"
                @click.stop="handlePreview()"
              >
                <i class="el-icon-zoom-in"></i>
              </span>
              <span
                class="item-delete"
                @click.stop="handleRemove()"
              >
                <i class="el-icon-delete"></i>
              </span>
            </span>
          </div>
        </div>
        <i v-else-if="uploadFlag === null" class="el-icon-plus uploader-icon"></i>
        <i v-else-if="!uploadFlag" class="el-icon-circle-close uploader-icon" style="color: red"></i>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" slot="tip" v-if="showTip">
      请上传
      <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
      <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
      的文件
    </div>

    <el-dialog :visible.sync="dialogVisible" append-to-body  width="800"  title="预览">
      <video width="100%" v-if="videoUrl" controls="controls" :key="menuKey">
          <source :src="videoUrl" type="video/mp4" />
      </video>
    </el-dialog>

  </div>
</template>

<script>

import { getAccessToken } from "@/utils/auth";

export default {
  props: {
    value: [String, Object],
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 300,
    },
    // 文件类型, 例如"video/mp4"
    fileType: {
      type: [String, Array],
      default: () =>["video/mp4"],
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      uploadFlag: null,
      activeHover: true,
      dialogVisible: false,
      videoUrl: null,
      // 视频上传
      uploadUrl: process.env.VUE_APP_BASE_API + "/admin-api/infra/file/upload", // 请求地址
      headers: { Authorization: "Bearer " + getAccessToken() }, // 设置上传的请求头部
      // 应付多个组件的情况 记录当前组件的key值
      menuKey: 1, // 用来强制刷新,
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          this.videoUrl = val;
          this.uploadFlag = true;
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    // 是否显示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize);
    },
  },
  methods: {
     // 上传成功的函数
     handleSuccess(res) {
      ++this.menuKey;
      if(res.code === 0){
        this.uploadFlag = true;
        this.videoUrl = res.data;
        this.$emit("input", this.videoUrl);
      }else{
        this.uploadFlag = false;
        this.$message.error("错误！"+ res.msg);
      }
    },
    handleError(){
      this.uploadFlag = false;
    },
    beforeUpload(file) {
      const isMp4 = this.fileType.includes(file.type);
      const isLt300MB = file.size / 1024 / 1024 < 300;
      if (!isMp4) {
        this.$message.error("视频只能是"+ this.fileType.join("/") +"格式!");
      }
      if (!isLt300MB) {
        this.$message.error("上传视频大小不能超过 300MB!");
      }
      return isMp4 && isLt300MB;
    },
     // 预览
     handlePreview() {
      this.dialogVisible = true;
    },
     // 删除视频
    handleRemove() {
      this.videoUrl = null;
      this.uploadFlag = null;
      this.$emit("input", null);
    },
    mouseover(){
      this.activeHover = false;
    },
    mouseout(){
      this.activeHover = true;
    }
  }
}
</script>


<style lang="scss">

  .editPage__video {
    .hide{
      visibility:hidden;
    }
    .success{
      position: relative;
      width: 78px;
      height: 78px;
      line-height: 78px;
      background-color: rgba(0,0,0,.5);
      transition: opacity .3s;
      cursor: default;

      .item-preview .el-icon-zoom-in{
        width: 30px;
        font-size: 20px;
        color: #f2f2f2;
        cursor: pointer;
      }
      .item-delete .el-icon-delete{
        width: 30px;
        font-size: 20px;
        color: #f2f2f2;
        cursor: pointer;
      }
    }

    .uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 80px;
      height: 80px;
      line-height: 80px;
      text-align: center;
      position: absolute;
      left: 0;
    }
    .success-icon {
      font-size: 28px;
      color: green;
      width: 80px;
      height: 80px;
      line-height: 80px;
      text-align: center;
      position: absolute;
      left: 0;
    }

    .el-upload{
      width: 80px;
      height: 80px;
    }

  }
</style>
