<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
  芋道源码：
  ① 移除多余的 rep 为前缀的变量，让 message 消息更简单
  ② 代码优化，补充注释，提升阅读性
  ③ 优化消息的临时缓存策略，发送消息时，只清理被发送消息的 tab，不会强制切回到 text 输入
  ④ 支持发送【视频】消息时，支持新建视频
-->
<template>
  <el-tabs type="border-card" v-model="objData.type" @tab-click="handleClick">
    <!-- 类型 1：文本 -->
    <el-tab-pane name="text">
      <span slot="label"><i class="el-icon-document"></i> 文本</span>
      <el-input type="textarea" :rows="5" placeholder="请输入内容" v-model="objData.content" @input="inputContent" />
    </el-tab-pane>
    <!-- 类型 2：图片 -->
    <el-tab-pane name="image">
      <span slot="label"><i class="el-icon-picture"></i> 图片</span>
      <el-row>
        <!-- 情况一：已经选择好素材、或者上传好图片 -->
        <div class="select-item" v-if="objData.url">
          <img class="material-img" :src="objData.url">
          <p class="item-name" v-if="objData.name">{{objData.name}}</p>
          <el-row class="ope-row">
            <el-button type="danger" icon="el-icon-delete" circle @click="deleteObj"></el-button>
          </el-row>
        </div>
        <!-- 情况二：未做完上述操作 -->
        <div v-else>
          <el-row style="text-align: center">
            <!-- 选择素材 -->
            <el-col :span="12" class="col-select">
              <el-button type="success" @click="openMaterial">
                素材库选择<i class="el-icon-circle-check el-icon--right"></i>
              </el-button>
              <el-dialog title="选择图片" :visible.sync="dialogImageVisible" width="90%" append-to-body>
                <wx-material-select :obj-data="objData" @selectMaterial="selectMaterial" />
              </el-dialog>
            </el-col>
            <!-- 文件上传 -->
            <el-col :span="12" class="col-add">
              <el-upload :action="actionUrl" :headers="headers" multiple :limit="1" :file-list="fileList" :data="uploadData"
                         :before-upload="beforeImageUpload" :on-success="handleUploadSuccess">
                <el-button type="primary">上传图片</el-button>
                <div slot="tip" class="el-upload__tip">支持 bmp/png/jpeg/jpg/gif 格式，大小不超过 2M</div>
              </el-upload>
            </el-col>
          </el-row>
        </div>
      </el-row>
    </el-tab-pane>
    <!-- 类型 3：语音 -->
    <el-tab-pane name="voice">
      <span slot="label"><i class="el-icon-phone"></i> 语音</span>
      <el-row>
        <div class="select-item2" v-if="objData.url">
          <p class="item-name">{{objData.name}}</p>
          <div class="item-infos">
            <wx-voice-player :url="objData.url" />
          </div>
          <el-row class="ope-row">
            <el-button type="danger" icon="el-icon-delete" circle @click="deleteObj"></el-button>
          </el-row>
        </div>
        <div v-else>
          <el-row style="text-align: center">
            <!-- 选择素材 -->
            <el-col :span="12" class="col-select">
              <el-button type="success" @click="openMaterial">
                素材库选择<i class="el-icon-circle-check el-icon--right"></i>
              </el-button>
              <el-dialog title="选择语音" :visible.sync="dialogVoiceVisible" width="90%" append-to-body>
                <WxMaterialSelect :objData="objData" @selectMaterial="selectMaterial"></WxMaterialSelect>
              </el-dialog>
            </el-col>
            <!-- 文件上传 -->
            <el-col :span="12" class="col-add">
              <el-upload :action="actionUrl" :headers="headers" multiple :limit="1" :file-list="fileList" :data="uploadData"
                         :before-upload="beforeVoiceUpload" :on-success="handleUploadSuccess">
                <el-button type="primary">点击上传</el-button>
                <div slot="tip" class="el-upload__tip">格式支持 mp3/wma/wav/amr，文件大小不超过 2M，播放长度不超过 60s</div>
              </el-upload>
            </el-col>
          </el-row>
        </div>
      </el-row>
    </el-tab-pane>
    <!-- 类型 4：视频 -->
    <el-tab-pane name="video">
      <span slot="label"><i class="el-icon-share"></i> 视频</span>
      <el-row>
        <el-input v-model="objData.title" placeholder="请输入标题" @input="inputContent" />
        <div style="margin: 20px 0;"></div>
        <el-input v-model="objData.description" placeholder="请输入描述" @input="inputContent" />
        <div style="margin: 20px 0;"></div>
        <div style="text-align: center;">
          <wx-video-player v-if="objData.url" :url="objData.url" />
        </div>
        <div style="margin: 20px 0;"></div>
        <el-row style="text-align: center">
          <!-- 选择素材 -->
          <el-col :span="12">
            <el-button type="success" @click="openMaterial">
              素材库选择<i class="el-icon-circle-check el-icon--right"></i>
            </el-button>
            <el-dialog title="选择视频" :visible.sync="dialogVideoVisible" width="90%" append-to-body>
              <wx-material-select :objData="objData" @selectMaterial="selectMaterial" />
            </el-dialog>
          </el-col>
          <!-- 文件上传 -->
          <el-col :span="12">
            <el-upload :action="actionUrl" :headers="headers" multiple :limit="1" :file-list="fileList" :data="uploadData"
                       :before-upload="beforeVideoUpload" :on-success="handleUploadSuccess">
              <el-button type="primary">新建视频<i class="el-icon-upload el-icon--right"></i></el-button>
            </el-upload>
          </el-col>
        </el-row>
      </el-row>
    </el-tab-pane>
    <!-- 类型 5：图文 -->
    <el-tab-pane name="news">
      <span slot="label"><i class="el-icon-news"></i> 图文</span>
      <el-row>
        <div class="select-item" v-if="objData.articles">
          <wx-news :articles="objData.articles" />
          <el-row class="ope-row">
            <el-button type="danger" icon="el-icon-delete" circle @click="deleteObj" />
          </el-row>
        </div>
        <!-- 选择素材 -->
        <div v-if="!objData.content">
          <el-row style="text-align: center">
            <el-col :span="24">
              <el-button type="success" @click="openMaterial">{{newsType === '1' ? '选择已发布图文' : '选择草稿箱图文'}}<i class="el-icon-circle-check el-icon--right"></i></el-button>
            </el-col>
          </el-row>
        </div>
        <el-dialog title="选择图文" :visible.sync="dialogNewsVisible" width="90%" append-to-body>
          <wx-material-select :objData="objData" @selectMaterial="selectMaterial" :newsType="newsType" />
        </el-dialog>
      </el-row>
    </el-tab-pane>
    <!-- 类型 6：音乐 -->
    <el-tab-pane name="music">
      <span slot="label"><i class="el-icon-service"></i> 音乐</span>
      <el-row>
        <el-col :span="6">
          <div class="thumb-div">
            <img style="width: 100px" v-if="objData.thumbMediaUrl" :src="objData.thumbMediaUrl">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            <div class="thumb-but">
              <el-upload :action="actionUrl" :headers="headers" multiple :limit="1" :file-list="fileList" :data="uploadData"
                         :before-upload="beforeThumbImageUpload" :on-success="handleUploadSuccess">
                <el-button slot="trigger" size="mini" type="text">本地上传</el-button>
                <el-button size="mini" type="text" @click="openMaterial" style="margin-left: 5px">素材库选择</el-button>
              </el-upload>
            </div>
          </div>
          <el-dialog title="选择图片" :visible.sync="dialogThumbVisible" width="80%" append-to-body>
            <wx-material-select :objData="{type:'image', accountId: objData.accountId}" @selectMaterial="selectMaterial" />
          </el-dialog>
        </el-col>
        <el-col :span="18">
          <el-input v-model="objData.title" placeholder="请输入标题" @input="inputContent" />
          <div style="margin: 20px 0;"></div>
          <el-input v-model="objData.description" placeholder="请输入描述" @input="inputContent" />
        </el-col>
      </el-row>
      <div style="margin: 20px 0;"></div>
      <el-input v-model="objData.musicUrl" placeholder="请输入音乐链接" @input="inputContent" />
      <div style="margin: 20px 0;"></div>
      <el-input v-model="objData.hqMusicUrl" placeholder="请输入高质量音乐链接" @input="inputContent" />
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import WxNews from '@/views/mp/components/wx-news/main.vue'
import WxMaterialSelect from '@/views/mp/components/wx-material-select/main.vue'
import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';

import { getAccessToken } from '@/utils/auth'

export default {
  name: "wxReplySelect",
  components: {
    WxNews,
    WxMaterialSelect,
    WxVoicePlayer,
    WxVideoPlayer
  },
  props: {
    objData: { // 消息对象。
      type: Object, // 设置为 Object 的原因，方便属性的传递
      required: true,
    },
    newsType:{ // 图文类型：1、已发布图文；2、草稿箱图文
      type: String,
      default: "1"
    },
  },
  data() {
    return {
      tempPlayerObj: {
        type: '2'
      },

      tempObj: new Map().set( // 临时缓存，切换消息类型的 tab 的时候，可以保存对应的数据；
          this.objData.type, // 消息类型
          Object.assign({}, this.objData)), // 消息内容

      // ========== 素材选择的弹窗，是否可见 ==========
      dialogNewsVisible: false, // 图文
      dialogImageVisible: false, // 图片
      dialogVoiceVisible: false, // 语音
      dialogVideoVisible: false, // 视频
      dialogThumbVisible: false, // 缩略图

      // ========== 文件上传（图片、语音、视频） ==========
      fileList: [], // 文件列表
      uploadData: {
        "accountId": undefined,
        "type": this.objData.type,
        "title":'',
        "introduction":''
      },
      actionUrl: process.env.VUE_APP_BASE_API + '/admin-api/mp/material/upload-temporary',
      headers: { Authorization: "Bearer " + getAccessToken() }, // 设置上传的请求头部
    }
  },
  methods:{
    beforeThumbImageUpload(file){
      const isType = file.type === 'image/jpeg'
          || file.type === 'image/png'
          || file.type === 'image/gif'
          || file.type === 'image/bmp'
          || file.type === 'image/jpg';
      if (!isType) {
        this.$message.error('上传图片格式不对!');
        return false;
      }
      const isLt = file.size / 1024 / 1024 < 2;
      if (!isLt) {
        this.$message.error('上传图片大小不能超过 2M!');
        return false;
      }
      this.uploadData.accountId = this.objData.accountId;
      return true;
    },
    beforeVoiceUpload(file){
      // 校验格式
      const isType = file.type === 'audio/mp3'
          || file.type === 'audio/mpeg'
          || file.type === 'audio/wma'
          || file.type === 'audio/wav'
          || file.type === 'audio/amr';
      if (!isType) {
        this.$message.error('上传语音格式不对!' + file.type);
        return false;
      }
      // 校验大小
      const isLt = file.size / 1024 / 1024 < 2;
      if (!isLt) {
        this.$message.error('上传语音大小不能超过 2M!');
        return false;
      }
      this.uploadData.accountId = this.objData.accountId;
      return true;
    },
    beforeImageUpload(file) {
      // 校验格式
      const isType = file.type === 'image/jpeg'
          || file.type === 'image/png'
          || file.type === 'image/gif'
          || file.type === 'image/bmp'
          || file.type === 'image/jpg';
      if (!isType) {
        this.$message.error('上传图片格式不对!');
        return false;
      }
      // 校验大小
      const isLt = file.size / 1024 / 1024 < 2;
      if (!isLt) {
        this.$message.error('上传图片大小不能超过 2M!');
        return false;
      }
      this.uploadData.accountId = this.objData.accountId;
      return true;
    },
    beforeVideoUpload(file){
      // 校验格式
      const isType = file.type === 'video/mp4';
      if (!isType) {
        this.$message.error('上传视频格式不对!');
        return false;
      }
      // 校验大小
      const isLt = file.size / 1024 / 1024 < 10;
      if (!isLt) {
        this.$message.error('上传视频大小不能超过 10M!');
        return false;
      }
      this.uploadData.accountId = this.objData.accountId;
      return true;
    },
    handleUploadSuccess(response, file, fileList) {
      if (response.code !== 0) {
        this.$message.error('上传出错：' + response.msg)
        return false;
      }

      // 清空上传时的各种数据
      this.fileList = []
      this.uploadData.title = ''
      this.uploadData.introduction = ''

      // 上传好的文件，本质是个素材，所以可以进行选中
      let item = response.data
      this.selectMaterial(item)
    },
    /**
     * 切换消息类型的 tab
     *
     * @param tab tab
     */
    handleClick(tab) {
      // 设置后续文件上传的文件类型
      this.uploadData.type = this.objData.type;
      if (this.uploadData.type === 'music') { // 【音乐】上传的是缩略图
        this.uploadData.type = 'thumb';
      }

      // 从 tempObj 临时缓存中，获取对应的数据，并设置回 objData
      let tempObjItem = this.tempObj.get(this.objData.type)
      if (tempObjItem) {
        this.objData.content = tempObjItem.content ? tempObjItem.content : null
        this.objData.mediaId = tempObjItem.mediaId ? tempObjItem.mediaId : null
        this.objData.url = tempObjItem.url ? tempObjItem.url : null
        this.objData.name = tempObjItem.url ? tempObjItem.name : null
        this.objData.title = tempObjItem.title ? tempObjItem.title : null
        this.objData.description = tempObjItem.description ? tempObjItem.description : null
        return;
      }
      // 如果获取不到，需要把 objData 复原
      // 必须使用 $set 赋值，不然 input 无法输入内容
      this.$set(this.objData, 'content', '');
      this.$delete(this.objData, 'mediaId');
      this.$delete(this.objData, 'url');
      this.$set(this.objData, 'title', '');
      this.$set(this.objData, 'description', '');

    },
    /**
     * 选择素材，将设置设置到 objData 变量
     *
     * @param item 素材
     */
    selectMaterial(item) {
      // 选择好素材，所以隐藏弹窗
      this.closeMaterial();

      // 创建 tempObjItem 对象，并设置对应的值
      let tempObjItem = {}
      tempObjItem.type = this.objData.type;
      if (this.objData.type === 'news') {
        tempObjItem.articles = item.content.newsItem
        this.objData.articles = item.content.newsItem
      } else if (this.objData.type === 'music') { // 音乐需要特殊处理，因为选择的是图片的缩略图
        tempObjItem.thumbMediaId = item.mediaId
        this.objData.thumbMediaId = item.mediaId
        tempObjItem.thumbMediaUrl = item.url
        this.objData.thumbMediaUrl = item.url
        // title、introduction、musicUrl、hqMusicUrl：从 objData 到 tempObjItem，避免上传素材后，被覆盖掉
        tempObjItem.title = this.objData.title || ''
        tempObjItem.introduction = this.objData.introduction  || ''
        tempObjItem.musicUrl = this.objData.musicUrl  || ''
        tempObjItem.hqMusicUrl = this.objData.hqMusicUrl  || ''
      } else if (this.objData.type === 'image'
          || this.objData.type === 'voice') {
        tempObjItem.mediaId = item.mediaId
        this.objData.mediaId = item.mediaId
        tempObjItem.url = item.url;
        this.objData.url = item.url;
        tempObjItem.name = item.name
        this.objData.name = item.name
      } else if (this.objData.type === 'video') {
        tempObjItem.mediaId = item.mediaId
        this.objData.mediaId = item.mediaId
        tempObjItem.url = item.url;
        this.objData.url = item.url;
        tempObjItem.name = item.name
        this.objData.name = item.name
        // title、introduction：从 item 到 tempObjItem，因为素材里有 title、introduction
        if (item.title) {
          this.objData.title = item.title || ''
          tempObjItem.title = item.title || ''
        }
        if (item.introduction) {
          this.objData.description = item.introduction || '' // 消息使用的是 description，素材使用的是 introduction，所以转换下
          tempObjItem.description = item.introduction || ''
        }
      } else if (this.objData.type === 'text') {
        this.objData.content = item.content || ''
      }
      // 最终设置到临时缓存
      this.tempObj.set(this.objData.type, tempObjItem)
    },
    openMaterial() {
      if (this.objData.type === 'news') {
        this.dialogNewsVisible = true
      } else if(this.objData.type === 'image') {
        this.dialogImageVisible = true
      } else if(this.objData.type === 'voice') {
        this.dialogVoiceVisible = true
      } else if(this.objData.type === 'video') {
        this.dialogVideoVisible = true
      } else if(this.objData.type === 'music') {
        this.dialogThumbVisible = true
      }
    },
    closeMaterial() {
      this.dialogNewsVisible = false
      this.dialogImageVisible = false
      this.dialogVoiceVisible = false
      this.dialogVideoVisible = false
      this.dialogThumbVisible = false
    },
    deleteObj() {
      if (this.objData.type === 'news') {
        this.$delete(this.objData, 'articles');
      } else if(this.objData.type === 'image') {
        this.objData.mediaId = null
        this.$delete(this.objData, 'url');
        this.objData.name = null
      } else if(this.objData.type === 'voice') {
        this.objData.mediaId = null
        this.$delete(this.objData, 'url');
        this.objData.name = null
      } else if(this.objData.type === 'video') {
        this.objData.mediaId = null
        this.$delete(this.objData, 'url');
        this.objData.name = null
        this.objData.title = null
        this.objData.description = null
      } else if(this.objData.type === 'music') {
        this.objData.thumbMediaId = null
        this.objData.thumbMediaUrl = null
        this.objData.title = null
        this.objData.description = null
        this.objData.musicUrl = null
        this.objData.hqMusicUrl = null
      } else if(this.objData.type === 'text') {
        this.objData.content = null
      }
      // 覆盖缓存
      this.tempObj.set(this.objData.type, Object.assign({}, this.objData));
    },
    /**
     * 输入时，缓存每次 objData 到 tempObj 中
     *
     * why？不确定为什么 v-model="objData.content" 不能自动缓存，所以通过这样的方式
     */
    inputContent(str) {
      // 覆盖缓存
      this.tempObj.set(this.objData.type, Object.assign({}, this.objData));
    }
  }
};
</script>

<style lang="scss" scoped>
.public-account-management{
  .el-input{
    width: 70%;
    margin-right: 2%;
  }
}
.pagination{
  text-align: right;
  margin-right: 25px;
}
.select-item{
  width: 280px;
  padding: 10px;
  margin: 0 auto 10px auto;
  border: 1px solid #eaeaea;
}
.select-item2{
  padding: 10px;
  margin: 0 auto 10px auto;
  border: 1px solid #eaeaea;
}
.ope-row{
  padding-top: 10px;
  text-align: center;
}
.item-name{
  font-size: 12px;
  overflow: hidden;
  text-overflow:ellipsis;
  white-space: nowrap;
  text-align: center;
}
.el-form-item__content{
  line-height:unset!important;
}
.col-select{
  border: 1px solid rgb(234, 234, 234);
  padding: 50px 0px;
  height: 160px;
  width: 49.5%;
}
.col-select2{
  border: 1px solid rgb(234, 234, 234);
  padding: 50px 0px;
  height: 160px;
}
.col-add{
  border: 1px solid rgb(234, 234, 234);
  padding: 50px 0px;
  height: 160px;
  width: 49.5%;
  float: right
}
.avatar-uploader-icon {
  border: 1px solid #d9d9d9;
  font-size: 28px;
  color: #8c939d;
  width: 100px!important;
  height: 100px!important;
  line-height: 100px!important;
  text-align: center;
}
.material-img {
  width: 100%;
}
.thumb-div{
  display: inline-block;
  text-align: center;
}
.item-infos{
  width: 30%;
  margin: auto
}
</style>
