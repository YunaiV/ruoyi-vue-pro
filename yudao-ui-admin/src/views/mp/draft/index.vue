<!--
MIT License

Copyright (c) 2020 www.joolun.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
  芋道源码：
  ① 优化代码，和项目的代码保持一致
  ② 清理冗余代码，保证代码整洁
  ③ 增加注释，提升可读性
-->
<template>
  <div class="app-container">
    <doc-alert title="公众号图文" url="https://doc.iocoder.cn/mp/article/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['mp:draft:create']">新增
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <div class="waterfall" v-loading="loading">
      <div v-if="item.content && item.content.newsItem" class="waterfall-item" v-for="item in list"
           :key='item.articleId'>
        <wx-news :articles="item.content.newsItem" />
        <!-- 操作按钮 -->
        <el-row class="ope-row">
          <el-button type="success" circle @click="handlePublish(item)" v-hasPermi="['mp:free-publish:submit']">发布</el-button>
          <el-button type="primary" icon="el-icon-edit" circle @click="handleUpdate(item)" v-hasPermi="['mp:draft:update']" />
          <el-button type="danger" icon="el-icon-delete" circle @click="handleDelete(item)" v-hasPermi="['mp:draft:delete']" />
        </el-row>
      </div>
    </div>
    <!-- 分页记录 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 添加或修改草稿对话框 -->
    <el-dialog :title="operateMaterial === 'add' ? '新建图文' : '修改图文'"
               append-to-body width="80%" top="20px" :visible.sync="dialogNewsVisible"
               :before-close="dialogNewsClose" :close-on-click-modal="false">
      <div class="left">
        <div class="select-item">
          <div v-for="(news, index) in articlesAdd" :key='news.id'>
            <div class="news-main father" v-if="index === 0" :class="{'activeAddNews': isActiveAddNews === index}"
                 @click="activeNews(index)">
              <div class="news-content">
                <img class="material-img" v-if="news.thumbUrl" :src="news.thumbUrl"/>
                <div class="news-content-title">{{news.title}}</div>
              </div>
              <div class="child" v-if="articlesAdd.length>1">
                <el-button type="mini" icon="el-icon-sort-down" @click="downNews(index)">下移</el-button>
                <el-button v-if="operateMaterial === 'add'" type="mini" icon="el-icon-delete"
                           @click="minusNews(index)">删除
                </el-button>
              </div>
            </div>
            <div class="news-main-item father" v-if="index>0" :class="{'activeAddNews': isActiveAddNews === index}"
                 @click="activeNews(index)">
              <div class="news-content-item">
                <div class="news-content-item-title ">{{news.title}}</div>
                <div class="news-content-item-img">
                  <img class="material-img" v-if="news.thumbUrl" :src="news.thumbUrl" height="100%"/>
                </div>
              </div>
              <div class="child">
                <el-button v-if="articlesAdd.length > index+1" type="mini" icon="el-icon-sort-down"
                           @click="downNews(index)">下移
                </el-button>
                <el-button type="mini" icon="el-icon-sort-up" @click="upNews(index)">上移</el-button>
                <el-button v-if="operateMaterial=== 'add'" type="mini" icon="el-icon-delete"
                           @click="minusNews(index)">删除
                </el-button>
              </div>
            </div>
          </div>
          <div class="news-main-plus" @click="plusNews" v-if="articlesAdd.length<8 && operateMaterial==='add'">
            <i class="el-icon-circle-plus icon-plus"></i>
          </div>
        </div>
      </div>
      <div class="right" v-loading="addMaterialLoading" v-if="articlesAdd.length > 0">
        <br /> <br /> <br /> <br />
        <!-- 标题、作者、原文地址 -->
        <el-input v-model="articlesAdd[isActiveAddNews].title" placeholder="请输入标题（必填）" />
        <el-input v-model="articlesAdd[isActiveAddNews].author" placeholder="请输入作者" style="margin-top: 5px;" />
        <el-input v-model="articlesAdd[isActiveAddNews].contentSourceUrl" placeholder="请输入原文地址" style="margin-top: 5px;" />
        <!-- 封面和摘要 -->
        <div class="input-tt">封面和摘要：</div>
        <div>
          <div class="thumb-div">
            <img class="material-img" v-if="articlesAdd[isActiveAddNews].thumbUrl"
                 :src="articlesAdd[isActiveAddNews].thumbUrl" :class="isActiveAddNews === 0 ? 'avatar':'avatar1'">
            <i v-else class="el-icon-plus avatar-uploader-icon"
               :class="isActiveAddNews === 0 ? 'avatar':'avatar1'"></i>
            <div class="thumb-but">
              <el-upload :action="actionUrl" :headers="headers" multiple :limit="1" :file-list="fileList" :data="uploadData"
                         :before-upload="beforeThumbImageUpload" :on-success="handleUploadSuccess">
                <el-button slot="trigger" size="mini" type="primary">本地上传</el-button>
                <el-button size="mini" type="primary" @click="openMaterial" style="margin-left: 5px">素材库选择</el-button>
                <div slot="tip" class="el-upload__tip">支持 bmp/png/jpeg/jpg/gif 格式，大小不超过 2M</div>
              </el-upload>
            </div>
            <el-dialog title="选择图片" :visible.sync="dialogImageVisible" width="80%" append-to-body>
              <wx-material-select ref="materialSelect" :objData="{type: 'image', accountId: this.queryParams.accountId}"
                                  @selectMaterial="selectMaterial" />
            </el-dialog>
          </div>
          <el-input :rows="8" type="textarea" v-model="articlesAdd[isActiveAddNews].digest" placeholder="请输入摘要"
                    class="digest" maxlength="120" style="float: right" />
        </div>
        <!--富文本编辑器组件-->
        <el-row>
          <wx-editor v-model="articlesAdd[isActiveAddNews].content" :account-id="this.uploadData.accountId"
                     v-if="hackResetEditor"/>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogNewsVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import WxEditor from '@/views/mp/components/wx-editor/WxEditor.vue';
import WxNews from '@/views/mp/components/wx-news/main.vue';
import WxMaterialSelect from '@/views/mp/components/wx-material-select/main.vue'
import { getAccessToken } from '@/utils/auth'
import {createDraft, deleteDraft, getDraftPage, updateDraft} from "@/api/mp/draft";
import { getSimpleAccounts } from "@/api/mp/account";
import {deleteFreePublish, submitFreePublish} from "@/api/mp/freePublish";

export default {
  name: 'mpDraft',
  components: {
    WxEditor,
    WxNews,
    WxMaterialSelect
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 数据列表
      list: [],
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        accountId: undefined,
      },

      // ========== 文件上传 ==========
      actionUrl: process.env.VUE_APP_BASE_API + "/admin-api/mp/material/upload-permanent", // 上传永久素材的地址
      headers: { Authorization: "Bearer " + getAccessToken() }, // 设置上传的请求头部
      fileList: [],
      uploadData: {
        "type": 'image',
        // "accountId": 1,
      },

      // ========== 草稿新建 or 修改 ==========
      dialogNewsVisible: false,
      addMaterialLoading: false, // 添加草稿的 loading 标识
      articlesAdd: [],
      isActiveAddNews: 0,
      dialogImageVisible: false,
      operateMaterial: 'add',
      articlesMediaId: '',
      hackResetEditor: false,

      // 公众号账号列表
      accounts: [],
    }
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id);
      }
      // 加载数据
      this.getList();
    })
  },
  methods: {
    // ======================== 列表查询 ========================
    /** 设置账号编号 */
    setAccountId(accountId) {
      this.queryParams.accountId = accountId;
      this.uploadData.accountId = accountId;
    },
    /** 查询列表 */
    getList() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.queryParams.accountId) {
        this.$message.error('未选中公众号，无法查询草稿箱')
        return false
      }

      this.loading = true
      getDraftPage(this.queryParams).then(response => {
        // 将 thumbUrl 转成 picUrl，保证 wx-news 组件可以预览封面
        response.data.list.forEach(item => {
          const newsItem = item.content.newsItem;
          newsItem.forEach(article => {
            article.picUrl = article.thumbUrl;
          })
        })
        this.list = response.data.list
        this.total = response.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      // 默认选中第一个
      if (this.queryParams.accountId) {
        this.setAccountId(this.queryParams.accountId)
      }
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id)
      }
      this.handleQuery()
    },

    // ======================== 新增/修改草稿 ========================
    /** 新增按钮操作 */
    handleAdd() {
      this.resetEditor();
      this.reset();
      // 打开表单，并设置初始化
      this.operateMaterial = 'add'
      this.dialogNewsVisible = true
    },
    /** 更新按钮操作 */
    handleUpdate(item){
      this.resetEditor();
      this.reset();
      this.articlesMediaId = item.mediaId
      this.articlesAdd = JSON.parse(JSON.stringify(item.content.newsItem))
      // 打开表单，并设置初始化
      this.operateMaterial = 'edit'
      this.dialogNewsVisible = true
    },
    /** 提交按钮 */
    submitForm() {
      this.addMaterialLoading = true
      if (this.operateMaterial === 'add') {
        createDraft(this.queryParams.accountId, this.articlesAdd).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.dialogNewsVisible = false;
          this.getList()
        }).finally(() => {
          this.addMaterialLoading = false
        })
      } else {
        updateDraft(this.queryParams.accountId, this.articlesMediaId, this.articlesAdd).then(response => {
          this.$modal.msgSuccess("更新成功");
          this.dialogNewsVisible = false;
          this.getList()
        }).finally(() => {
          this.addMaterialLoading = false
        })
      }
    },
    // 关闭弹窗
    dialogNewsClose(done) {
      this.$modal.confirm('修改内容可能还未保存，确定关闭吗?').then(() => {
       this.reset()
        this.resetEditor()
        done()
      }).catch(() => {})
    },
    // 表单重置
    reset() {
      this.isActiveAddNews = 0
      this.articlesAdd = [this.buildEmptyArticle()]
    },
    // 表单 Editor 重置
    resetEditor() {
      this.hackResetEditor = false // 销毁组件
      this.$nextTick(() => {
        this.hackResetEditor = true // 重建组件
      })
    },
    // 将图文向下移动
    downNews(index) {
      let temp = this.articlesAdd[index]
      this.articlesAdd[index] = this.articlesAdd[index+1]
      this.articlesAdd[index + 1] = temp
      this.isActiveAddNews = index + 1
    },
    // 将图文向上移动
    upNews(index) {
      let temp = this.articlesAdd[index]
      this.articlesAdd[index] = this.articlesAdd[index - 1]
      this.articlesAdd[index - 1] = temp
      this.isActiveAddNews = index - 1
    },
    // 选中指定 index 的图文
    activeNews(index) {
      this.resetEditor();
      this.isActiveAddNews = index
    },
    // 删除指定 index 的图文
    minusNews(index) {
      this.$modal.confirm('确定删除该图文吗?').then(() => {
        this.articlesAdd.splice(index,1);
        if (this.isActiveAddNews ===  index) {
          this.isActiveAddNews = 0
        }
      }).catch(() => {})
    },
    // 添加一个图文
    plusNews() {
      this.articlesAdd.push(this.buildEmptyArticle())
      this.isActiveAddNews = this.articlesAdd.length - 1
    },
    // 创建空的 article
    buildEmptyArticle() {
      return {
        "title": '',
        "thumbMediaId": '',
        "author": '',
        "digest": '',
        "showCoverPic": '',
        "content": '',
        "contentSourceUrl": '',
        "needOpenComment":'',
        "onlyFansCanComment":'',
        "thumbUrl":''
      }
    },

    // ======================== 文件上传 ========================
    beforeThumbImageUpload(file) {
      this.addMaterialLoading = true
      const isType = file.type === 'image/jpeg'
          || file.type === 'image/png'
          || file.type === 'image/gif'
          || file.type === 'image/bmp'
          || file.type === 'image/jpg';
      if (!isType) {
        this.$message.error('上传图片格式不对!')
        this.addMaterialLoading = false
        return false;
      }
      const isLt = file.size / 1024 / 1024 < 2
      if (!isLt) {
        this.$message.error('上传图片大小不能超过 2M!')
        this.addMaterialLoading = false
        return false;
      }
      // 校验通过
      return true;
    },
    handleUploadSuccess(response, file, fileList) {
      this.addMaterialLoading = false // 关闭 loading
      if (response.code !== 0) {
        this.$message.error('上传出错：' + response.msg)
        return false;
      }

      // 重置上传文件的表单
      this.fileList = []

      // 设置草稿的封面字段
      this.articlesAdd[this.isActiveAddNews].thumbMediaId = response.data.mediaId
      this.articlesAdd[this.isActiveAddNews].thumbUrl = response.data.url
    },
    // 选择 or 上传完素材，设置回草稿
    selectMaterial(item) {
      this.dialogImageVisible = false
      this.articlesAdd[this.isActiveAddNews].thumbMediaId = item.mediaId
      this.articlesAdd[this.isActiveAddNews].thumbUrl = item.url
    },
    // 打开素材选择
    openMaterial() {
      this.dialogImageVisible = true
      try {
        this.$refs['materialSelect'].queryParams.accountId = this.queryParams.accountId // 强制设置下 accountId，避免二次查询不对
        this.$refs['materialSelect'].handleQuery(); // 刷新列表，失败也无所谓
      } catch (e) {}
    },

    // ======================== 草稿箱发布 ========================
    handlePublish(item) {
      const accountId = this.queryParams.accountId;
      const mediaId = item.mediaId;
      const content = '你正在通过发布的方式发表内容。 发布不占用群发次数，一天可多次发布。已发布内容不会推送给用户，也不会展示在公众号主页中。 发布后，你可以前往发表记录获取链接，也可以将发布内容添加到自定义菜单、自动回复、话题和页面模板中。';
      this.$modal.confirm(content).then(function() {
        return submitFreePublish(accountId, mediaId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("发布成功");
      }).catch(() => {});
    },
    handleDelete(item) {
      const accountId = this.queryParams.accountId;
      const mediaId = item.mediaId;
      this.$modal.confirm('此操作将永久删除该草稿, 是否继续?').then(function() {
        return deleteDraft(accountId, mediaId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
  }
}
</script>

<style lang="scss" scoped>
.pagination {
  float: right;
  margin-right: 25px;
}

.add_but {
  padding: 10px;
}

.ope-row {
  margin-top: 5px;
  text-align: center;
  border-top: 1px solid #eaeaea;
  padding-top: 5px;
}

.item-name {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.el-upload__tip {
  margin-left: 5px;
}

/*新增图文*/
.left {
  display: inline-block;
  width: 35%;
  vertical-align: top;
  margin-top: 200px;
}

.right {
  display: inline-block;
  width: 60%;
  margin-top: -40px;
}

.avatar-uploader {
  width: 20%;
  display: inline-block;
}

.avatar-uploader .el-upload {
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  text-align: unset !important;
}

.avatar-uploader .el-upload:hover {
  border-color: #165dff;
}

.avatar-uploader-icon {
  border: 1px solid #d9d9d9;
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 230px;
  height: 120px;
}

.avatar1 {
  width: 120px;
  height: 120px;
}

.digest {
  width: 60%;
  display: inline-block;
  vertical-align: top;
}

/*新增图文*/
/*瀑布流样式*/
.waterfall {
  width: 100%;
  column-gap: 10px;
  column-count: 5;
  margin: 0 auto;
}

.waterfall-item {
  padding: 10px;
  margin-bottom: 10px;
  break-inside: avoid;
  border: 1px solid #eaeaea;
}

p {
  line-height: 30px;
}

@media (min-width: 992px) and (max-width: 1300px) {
  .waterfall {
    column-count: 3;
  }
  p {
    color: red;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .waterfall {
    column-count: 2;
  }
  p {
    color: orange;
  }
}

@media (max-width: 767px) {
  .waterfall {
    column-count: 1;
  }
}

/*瀑布流样式*/
.news-main {
  background-color: #FFFFFF;
  width: 100%;
  margin: auto;
  height: 120px;
}

.news-content {
  background-color: #acadae;
  width: 100%;
  height: 120px;
  position: relative;
}

.news-content-title {
  display: inline-block;
  font-size: 15px;
  color: #FFFFFF;
  position: absolute;
  left: 0px;
  bottom: 0px;
  background-color: black;
  width: 98%;
  padding: 1%;
  opacity: 0.65;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  height: 25px;
}

.news-main-item {
  background-color: #FFFFFF;
  padding: 5px 0px;
  border-top: 1px solid #eaeaea;
  width: 100%;
  margin: auto;
}

.news-content-item {
  position: relative;
  margin-left: -3px
}

.news-content-item-title {
  display: inline-block;
  font-size: 12px;
  width: 70%;
}

.news-content-item-img {
  display: inline-block;
  width: 25%;
  background-color: #acadae
}

.input-tt {
  padding: 5px;
}

.activeAddNews {
  border: 5px solid #2bb673;
}

.news-main-plus {
  width: 280px;
  text-align: center;
  margin: auto;
  height: 50px;
}

.icon-plus {
  margin: 10px;
  font-size: 25px;
}

.select-item {
  width: 60%;
  padding: 10px;
  margin: 0 auto 10px auto;
  border: 1px solid #eaeaea;
}

.father .child {
  display: none;
  text-align: center;
  position: relative;
  bottom: 25px;
}

.father:hover .child {
  display: block;
}

.thumb-div {
  display: inline-block;
  width: 30%;
  text-align: center;
}

.thumb-but {
  margin: 5px;
}

.material-img {
  width: 100%;
  height: 100%;
}
</style>
