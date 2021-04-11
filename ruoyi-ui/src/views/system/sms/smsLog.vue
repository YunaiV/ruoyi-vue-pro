<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="短信渠道编号" prop="channelId">
        <el-input v-model="queryParams.channelId" placeholder="请输入短信渠道编号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="模板编号" prop="templateId">
        <el-input v-model="queryParams.templateId" placeholder="请输入模板编号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="发送状态" prop="sendStatus">
        <el-select v-model="queryParams.sendStatus" placeholder="请选择发送状态" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送时间">
        <el-date-picker v-model="dateRangeSendTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="接收状态" prop="receiveStatus">
        <el-select v-model="queryParams.receiveStatus" placeholder="请选择接收状态" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="接收时间">
        <el-date-picker v-model="dateRangeReceiveTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:sms-log:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['system:sms-log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="短信渠道编号" align="center" prop="channelId" />
      <el-table-column label="短信渠道编码" align="center" prop="channelCode" />
      <el-table-column label="模板编号" align="center" prop="templateId" />
      <el-table-column label="模板编码" align="center" prop="templateCode" />
      <el-table-column label="短信类型" align="center" prop="templateType" />
      <el-table-column label="短信内容" align="center" prop="templateContent" />
      <el-table-column label="短信参数" align="center" prop="templateParams" />
      <el-table-column label="短信 API 的模板编号" align="center" prop="apiTemplateId" />
      <el-table-column label="手机号" align="center" prop="mobile" />
      <el-table-column label="用户编号" align="center" prop="userId" />
      <el-table-column label="用户类型" align="center" prop="userType" />
      <el-table-column label="发送状态" align="center" prop="sendStatus" />
      <el-table-column label="发送时间" align="center" prop="sendTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.sendTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发送结果的编码" align="center" prop="sendCode" />
      <el-table-column label="发送结果的提示" align="center" prop="sendMsg" />
      <el-table-column label="短信 API 发送结果的编码" align="center" prop="apiSendCode" />
      <el-table-column label="短信 API 发送失败的提示" align="center" prop="apiSendMsg" />
      <el-table-column label="短信 API 发送返回的唯一请求 ID" align="center" prop="apiRequestId" />
      <el-table-column label="短信 API 发送返回的序号" align="center" prop="apiSerialNo" />
      <el-table-column label="接收状态" align="center" prop="receiveStatus" />
      <el-table-column label="接收时间" align="center" prop="receiveTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.receiveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="API 接收结果的编码" align="center" prop="apiReceiveCode" />
      <el-table-column label="API 接收结果的说明" align="center" prop="apiReceiveMsg" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:sms-log:update']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->

  </div>
</template>

<script>
import { getSmsLogPage, exportSmsLogExcel } from "@/api/system/sms/smsLog";

export default {
  name: "SmsLog",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 短信日志列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRangeSendTime: [],
      dateRangeReceiveTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        channelId: null,
        templateId: null,
        mobile: null,
        sendStatus: null,
        receiveStatus: null,
      },
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
      this.addBeginAndEndTime(params, this.dateRangeSendTime, 'sendTime');
      this.addBeginAndEndTime(params, this.dateRangeReceiveTime, 'receiveTime');
      // 执行查询
      getSmsLogPage(params).then(response => {
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
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeSendTime = [];
      this.dateRangeReceiveTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.addBeginAndEndTime(params, this.dateRangeSendTime, 'sendTime');
      this.addBeginAndEndTime(params, this.dateRangeReceiveTime, 'receiveTime');
      // 执行导出
      this.$confirm('是否确认导出所有短信日志数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return exportSmsLogExcel(params);
      }).then(response => {
        this.downloadExcel(response, '短信日志.xls');
      })
    }
  }
};
</script>
