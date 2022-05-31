<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户标识" prop="openid">
        <el-input v-model="queryParams.openid" placeholder="请输入用户标识" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="订阅状态，0未关注，1已关注" prop="subscribeStatus">
        <el-select v-model="queryParams.subscribeStatus" placeholder="请选择订阅状态，0未关注，1已关注" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="订阅时间">
        <el-date-picker v-model="dateRangeSubscribeTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="性别，1男，2女，0未知" prop="gender">
        <el-input v-model="queryParams.gender" placeholder="请输入性别，1男，2女，0未知" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="语言" prop="language">
        <el-input v-model="queryParams.language" placeholder="请输入语言" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="国家" prop="country">
        <el-input v-model="queryParams.country" placeholder="请输入国家" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-input v-model="queryParams.province" placeholder="请输入省份" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-input v-model="queryParams.city" placeholder="请输入城市" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="头像地址" prop="headimgUrl">
        <el-input v-model="queryParams.headimgUrl" placeholder="请输入头像地址" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="queryParams.remark" placeholder="请输入备注" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="微信公众号ID" prop="wxAccountId">
        <el-input v-model="queryParams.wxAccountId" placeholder="请输入微信公众号ID" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="微信公众号appid" prop="wxAccountAppid">
        <el-input v-model="queryParams.wxAccountAppid" placeholder="请输入微信公众号appid" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
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
                   v-hasPermi="['wechatMp:wx-account-fans:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['wechatMp:wx-account-fans:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户标识" align="center" prop="openid" />
      <el-table-column label="订阅状态，0未关注，1已关注" align="center" prop="subscribeStatus" />
      <el-table-column label="订阅时间" align="center" prop="subscribeTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.subscribeTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="昵称" align="center" prop="nickname" />
      <el-table-column label="性别，1男，2女，0未知" align="center" prop="gender" />
      <el-table-column label="语言" align="center" prop="language" />
      <el-table-column label="国家" align="center" prop="country" />
      <el-table-column label="省份" align="center" prop="province" />
      <el-table-column label="城市" align="center" prop="city" />
      <el-table-column label="头像地址" align="center" prop="headimgUrl" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="微信公众号ID" align="center" prop="wxAccountId" />
      <el-table-column label="微信公众号appid" align="center" prop="wxAccountAppid" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['wechatMp:wx-account-fans:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['wechatMp:wx-account-fans:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户标识" prop="openid">
          <el-input v-model="form.openid" placeholder="请输入用户标识" />
        </el-form-item>
        <el-form-item label="订阅状态，0未关注，1已关注" prop="subscribeStatus">
          <el-radio-group v-model="form.subscribeStatus">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订阅时间" prop="subscribeTime">
          <el-date-picker clearable v-model="form.subscribeTime" type="date" value-format="timestamp" placeholder="选择订阅时间" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别，1男，2女，0未知" prop="gender">
          <el-input v-model="form.gender" placeholder="请输入性别，1男，2女，0未知" />
        </el-form-item>
        <el-form-item label="语言" prop="language">
          <el-input v-model="form.language" placeholder="请输入语言" />
        </el-form-item>
        <el-form-item label="国家" prop="country">
          <el-input v-model="form.country" placeholder="请输入国家" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="头像地址" prop="headimgUrl">
          <el-input v-model="form.headimgUrl" placeholder="请输入头像地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="微信公众号ID" prop="wxAccountId">
          <el-input v-model="form.wxAccountId" placeholder="请输入微信公众号ID" />
        </el-form-item>
        <el-form-item label="微信公众号appid" prop="wxAccountAppid">
          <el-input v-model="form.wxAccountAppid" placeholder="请输入微信公众号appid" />
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
import { createWxAccountFans, updateWxAccountFans, deleteWxAccountFans, getWxAccountFans, getWxAccountFansPage, exportWxAccountFansExcel } from "@/api/wechatMp/wxAccountFans";

export default {
  name: "WxAccountFans",
  components: {
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
      // 微信公众号粉丝列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRangeSubscribeTime: [],
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        openid: null,
        subscribeStatus: null,
        nickname: null,
        gender: null,
        language: null,
        country: null,
        province: null,
        city: null,
        headimgUrl: null,
        remark: null,
        wxAccountId: null,
        wxAccountAppid: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
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
      this.addBeginAndEndTime(params, this.dateRangeSubscribeTime, 'subscribeTime');
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getWxAccountFansPage(params).then(response => {
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
        openid: undefined,
        subscribeStatus: undefined,
        subscribeTime: undefined,
        nickname: undefined,
        gender: undefined,
        language: undefined,
        country: undefined,
        province: undefined,
        city: undefined,
        headimgUrl: undefined,
        remark: undefined,
        wxAccountId: undefined,
        wxAccountAppid: undefined,
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
      this.dateRangeSubscribeTime = [];
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加微信公众号粉丝";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getWxAccountFans(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改微信公众号粉丝";
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
          updateWxAccountFans(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createWxAccountFans(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除微信公众号粉丝编号为"' + id + '"的数据项?').then(function() {
          return deleteWxAccountFans(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.addBeginAndEndTime(params, this.dateRangeSubscribeTime, 'subscribeTime');
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行导出
      this.$modal.confirm('是否确认导出所有微信公众号粉丝数据项?').then(() => {
          this.exportLoading = true;
          return exportWxAccountFansExcel(params);
        }).then(response => {
          this.$download.excel(response, '微信公众号粉丝.xls');
          this.exportLoading = false;
        }).catch(() => {});
    }
  }
};
</script>
