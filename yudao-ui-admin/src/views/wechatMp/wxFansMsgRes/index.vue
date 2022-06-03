<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="粉丝消息ID" prop="fansMsgId">
        <el-input v-model="queryParams.fansMsgId" placeholder="请输入粉丝消息ID" clearable @keyup.enter.native="handleQuery"/>
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
                   v-hasPermi="['wechatMp:wx-fans-msg-res:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['wechatMp:wx-fans-msg-res:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="主键" align="center" prop="id"/>
      <el-table-column label="粉丝消息ID" align="center" prop="fansMsgId"/>
      <el-table-column label="回复内容" align="center" prop="resContent"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['wechatMp:wx-fans-msg-res:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['wechatMp:wx-fans-msg-res:delete']">删除
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
        <el-form-item label="粉丝消息ID" prop="fansMsgId">
          <el-input v-model="form.fansMsgId" placeholder="请输入粉丝消息ID"/>
        </el-form-item>
        <el-form-item label="回复内容">
          <editor v-model="form.resContent" :min-height="192"/>
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
    createWxFansMsgRes,
    updateWxFansMsgRes,
    deleteWxFansMsgRes,
    getWxFansMsgRes,
    getWxFansMsgResPage,
    exportWxFansMsgResExcel
  } from "@/api/wechatMp/wxFansMsgRes";
  import Editor from '@/components/Editor';

  export default {
    name: "WxFansMsgRes",
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
        // 回复粉丝消息历史表 列表
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
          fansMsgId: null,
          resContent: null,
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
        getWxFansMsgResPage(params).then(response => {
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
          fansMsgId: undefined,
          resContent: undefined,
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
        this.title = "添加回复粉丝消息历史表 ";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id;
        getWxFansMsgRes(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改回复粉丝消息历史表 ";
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
            updateWxFansMsgRes(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
            return;
          }
          // 添加的提交
          createWxFansMsgRes(this.form).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const id = row.id;
        this.$modal.confirm('是否确认删除回复粉丝消息历史表 编号为"' + id + '"的数据项?').then(function () {
          return deleteWxFansMsgRes(id);
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
        this.$modal.confirm('是否确认导出所有回复粉丝消息历史表 数据项?').then(() => {
          this.exportLoading = true;
          return exportWxFansMsgResExcel(params);
        }).then(response => {
          this.$download.excel(response, '回复粉丝消息历史表 .xls');
          this.exportLoading = false;
        }).catch(() => {
        });
      }
    }
  };
</script>
