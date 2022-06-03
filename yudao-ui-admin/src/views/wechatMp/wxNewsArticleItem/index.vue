<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="摘要" prop="digest">
        <el-input v-model="queryParams.digest" placeholder="请输入摘要" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="queryParams.author" placeholder="请输入作者" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="是否展示封面图片（0/1）" prop="showCoverPic">
        <el-input v-model="queryParams.showCoverPic" placeholder="请输入是否展示封面图片（0/1）" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="上传微信，封面图片标识" prop="thumbMediaId">
        <el-input v-model="queryParams.thumbMediaId" placeholder="请输入上传微信，封面图片标识" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="内容链接" prop="contentSourceUrl">
        <el-input v-model="queryParams.contentSourceUrl" placeholder="请输入内容链接" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="文章排序" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入文章排序" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="图片路径" prop="picPath">
        <el-input v-model="queryParams.picPath" placeholder="请输入图片路径" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="是否可以留言" prop="needOpenComment">
        <el-input v-model="queryParams.needOpenComment" placeholder="请输入是否可以留言" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="是否仅粉丝可以留言" prop="onlyFansCanComment">
        <el-input v-model="queryParams.onlyFansCanComment" placeholder="请输入是否仅粉丝可以留言" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="图文ID" prop="newsId">
        <el-input v-model="queryParams.newsId" placeholder="请输入图文ID" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="微信账号ID" prop="wxAccountId">
        <el-input v-model="queryParams.wxAccountId" placeholder="请输入微信账号ID" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
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
                   v-hasPermi="['wechatMp:wx-news-article-item:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['wechatMp:wx-news-article-item:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="主键" align="center" prop="id"/>
      <el-table-column label="标题" align="center" prop="title"/>
      <el-table-column label="摘要" align="center" prop="digest"/>
      <el-table-column label="作者" align="center" prop="author"/>
      <el-table-column label="是否展示封面图片（0/1）" align="center" prop="showCoverPic"/>
      <el-table-column label="上传微信，封面图片标识" align="center" prop="thumbMediaId"/>
      <el-table-column label="内容" align="center" prop="content"/>
      <el-table-column label="内容链接" align="center" prop="contentSourceUrl"/>
      <el-table-column label="文章排序" align="center" prop="orderNo"/>
      <el-table-column label="图片路径" align="center" prop="picPath"/>
      <el-table-column label="是否可以留言" align="center" prop="needOpenComment"/>
      <el-table-column label="是否仅粉丝可以留言" align="center" prop="onlyFansCanComment"/>
      <el-table-column label="图文ID" align="center" prop="newsId"/>
      <el-table-column label="微信账号ID" align="center" prop="wxAccountId"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['wechatMp:wx-news-article-item:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['wechatMp:wx-news-article-item:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题"/>
        </el-form-item>
        <el-form-item label="摘要" prop="digest">
          <el-input v-model="form.digest" placeholder="请输入摘要"/>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者"/>
        </el-form-item>
        <el-form-item label="是否展示封面图片（0/1）" prop="showCoverPic">
          <el-input v-model="form.showCoverPic" placeholder="请输入是否展示封面图片（0/1）"/>
        </el-form-item>
        <el-form-item label="上传微信，封面图片标识" prop="thumbMediaId">
          <el-input v-model="form.thumbMediaId" placeholder="请输入上传微信，封面图片标识"/>
        </el-form-item>
        <el-form-item label="内容">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="内容链接" prop="contentSourceUrl">
          <el-input v-model="form.contentSourceUrl" placeholder="请输入内容链接"/>
        </el-form-item>
        <el-form-item label="文章排序" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入文章排序"/>
        </el-form-item>
        <el-form-item label="图片路径" prop="picPath">
          <el-input v-model="form.picPath" placeholder="请输入图片路径"/>
        </el-form-item>
        <el-form-item label="是否可以留言" prop="needOpenComment">
          <el-input v-model="form.needOpenComment" placeholder="请输入是否可以留言"/>
        </el-form-item>
        <el-form-item label="是否仅粉丝可以留言" prop="onlyFansCanComment">
          <el-input v-model="form.onlyFansCanComment" placeholder="请输入是否仅粉丝可以留言"/>
        </el-form-item>
        <el-form-item label="图文ID" prop="newsId">
          <el-input v-model="form.newsId" placeholder="请输入图文ID"/>
        </el-form-item>
        <el-form-item label="微信账号ID" prop="wxAccountId">
          <el-input v-model="form.wxAccountId" placeholder="请输入微信账号ID"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {
    createWxNewsArticleItem,
    updateWxNewsArticleItem,
    deleteWxNewsArticleItem,
    getWxNewsArticleItem,
    getWxNewsArticleItemPage,
    exportWxNewsArticleItemExcel
  } from "@/api/wechatMp/wxNewsArticleItem";
  import Editor from '@/components/Editor';

  export default {
    name: "WxNewsArticleItem",
    components: {
      Editor,
    },
    data() {
      return {
        // 遮罩层
        loading: true,
        // 导出遮罩层
        exportLoading: false,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 图文消息文章列表表 列表
        list: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        dateRangeCreateTime: [],
        // 查询参数
        queryParams: {
          pageNo: 1,
          pageSize: 10,
          title: null,
          digest: null,
          author: null,
          showCoverPic: null,
          thumbMediaId: null,
          content: null,
          contentSourceUrl: null,
          orderNo: null,
          picPath: null,
          needOpenComment: null,
          onlyFansCanComment: null,
          newsId: null,
          wxAccountId: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {}
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询列表 */
      getList() {
        this.loading = true;
        // 处理查询参数
        let params = {...this.queryParams};
        this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
        // 执行查询
        getWxNewsArticleItemPage(params).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        });
      },
      /** 取消按钮 */
      cancel() {
        this.open = false;
        this.reset();
      },
      /** 表单重置 */
      reset() {
        this.form = {
          id: undefined,
          title: undefined,
          digest: undefined,
          author: undefined,
          showCoverPic: undefined,
          thumbMediaId: undefined,
          content: undefined,
          contentSourceUrl: undefined,
          orderNo: undefined,
          picPath: undefined,
          needOpenComment: undefined,
          onlyFansCanComment: undefined,
          newsId: undefined,
          wxAccountId: undefined,
        };
        this.resetForm("form");
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNo = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.dateRangeCreateTime = [];
        this.resetForm("queryForm");
        this.handleQuery();
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加图文消息文章列表表 ";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id;
        getWxNewsArticleItem(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改图文消息文章列表表 ";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (!valid) {
            return;
          }
          // 修改的提交
          if (this.form.id != null) {
            updateWxNewsArticleItem(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
            return;
          }
          // 添加的提交
          createWxNewsArticleItem(this.form).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const id = row.id;
        this.$modal.confirm('是否确认删除图文消息文章列表表 编号为"' + id + '"的数据项?').then(function () {
          return deleteWxNewsArticleItem(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
      },
      /** 导出按钮操作 */
      handleExport() {
        // 处理查询参数
        let params = {...this.queryParams};
        params.pageNo = undefined;
        params.pageSize = undefined;
        this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
        // 执行导出
        this.$modal.confirm('是否确认导出所有图文消息文章列表表 数据项?').then(() => {
          this.exportLoading = true;
          return exportWxNewsArticleItemExcel(params);
        }).then(response => {
          this.$download.excel(response, '图文消息文章列表表 .xls');
          this.exportLoading = false;
        }).catch(() => {
        });
      }
    }
  };
</script>
