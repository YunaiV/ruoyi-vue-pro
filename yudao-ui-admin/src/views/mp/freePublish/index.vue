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

    <!-- 列表 -->
    <div class="waterfall" v-loading="loading">
      <div v-if="item.content && item.content.newsItem" class="waterfall-item" v-for="item in list"
           :key='item.articleId'>
        <wx-news :articles="item.content.newsItem" />
        <!-- 操作 -->
        <el-row class="ope-row">
          <el-button type="danger" icon="el-icon-delete" circle @click="handleDelete(item)"
                     v-hasPermi="['mp:free-publish:delete']" />
        </el-row>
      </div>
    </div>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { getFreePublishPage, deleteFreePublish } from "@/api/mp/freePublish";
import { getSimpleAccounts } from "@/api/mp/account";
import WxNews from '@/views/mp/components/wx-news/main.vue';

export default {
  name: 'mpDraft',
  components: {
    WxNews
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 已发表列表
      list: [],
      // 查询参数
      queryParams: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        queryParamsSize: 10 // 每页显示多少条
      },

      // 公众号账号列表
      accounts: [],
    }
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      // 加载数据
      this.getList();
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.queryParams.accountId) {
        this.$message.error('未选中公众号，无法查询已发表图文')
        return false
      }

      this.loading = true;
      getFreePublishPage(this.queryParams).then(response => {
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
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      this.handleQuery();
    },
    /** 删除按钮操作 */
    handleDelete(item){
      const articleId = item.articleId;
      const accountId = this.queryParams.accountId;
      this.$modal.confirm('删除后用户将无法访问此页面，确定删除？').then(function() {
        return deleteFreePublish(accountId, articleId);
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
