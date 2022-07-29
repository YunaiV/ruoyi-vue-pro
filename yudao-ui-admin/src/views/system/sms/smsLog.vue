<template>
  <div class="app-container">
    <doc-alert title="短信配置" url="https://doc.iocoder.cn/sms/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="短信渠道" prop="channelId">
        <el-select v-model="queryParams.channelId" placeholder="请选择短信渠道" clearable>
          <el-option v-for="channel in channelOptions"
                     :key="channel.id" :value="channel.id"
                     :label="channel.signature + '【' + getDictDataLabel(DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE, channel.code) + '】'" />
        </el-select>
      </el-form-item>
      <el-form-item label="模板编号" prop="templateId">
        <el-input v-model="queryParams.templateId" placeholder="请输入模板编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="发送状态" prop="sendStatus">
        <el-select v-model="queryParams.sendStatus" placeholder="请选择发送状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_SMS_SEND_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发送时间" prop="sendTime">
        <el-date-picker v-model="queryParams.sendTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="接收状态" prop="receiveStatus">
        <el-select v-model="queryParams.receiveStatus" placeholder="请选择接收状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="接收时间" prop="receiveTime">
        <el-date-picker v-model="queryParams.receiveTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['system:sms-log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" prop="mobile" width="120">
        <template slot-scope="scope">
          <div>{{ scope.row.mobile }}</div>
          <div v-if="scope.row.userType && scope.row.userId">
            <dict-tag :type="DICT_TYPE.USER_TYPE" :value="scope.row.userType"/>{{ '(' + scope.row.userId + ')' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="短信内容" align="center" prop="templateContent" width="300" />
      <el-table-column label="发送状态" align="center" width="180">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_SMS_SEND_STATUS" :value="scope.row.sendStatus"/>
          <div>{{ parseTime(scope.row.sendTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="接收状态" align="center" width="180">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS" :value="scope.row.receiveStatus"/>
          <div>{{ parseTime(scope.row.receiveTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="短信渠道" align="center" width="120">
        <template slot-scope="scope">
          <div>{{ formatChannelSignature(scope.row.channelId) }}</div>
          <dict-tag :type="DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE" :value="scope.row.channelCode"/>
        </template>
      </el-table-column>
      <el-table-column label="模板编号" align="center" prop="templateId" />
      <el-table-column label="短信类型" align="center" prop="templateType">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE" :value="scope.row.templateType"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row,scope.index)"
                     v-hasPermi="['system:sms-log:query']">详细</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 短信日志详细 -->
    <el-dialog title="短信日志详细" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="140px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志主键：">{{ form.id }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短信渠道：">
              {{formatChannelSignature(form.channelId) }}
              <dict-tag :type="DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE" :value="form.channelCode"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短信模板：">
              {{ form.templateId }} | {{ form.templateCode }}
              <dict-tag :type="DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE" :value="form.templateType"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="API 的模板编号：">{{ form.apiTemplateId }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用户信息：">{{ form.mobile }}
              <span v-if="form.userType && form.userId">
                <dict-tag :type="DICT_TYPE.USER_TYPE" :value="form.userType"/>({{ form.userId }})
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短信内容：">{{ form.templateContent }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短信参数：">{{ form.templateParams }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="创建时间：">{{ parseTime(form.createTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发送状态：">
              <dict-tag :type="DICT_TYPE.SYSTEM_SMS_SEND_STATUS" :value="form.sendStatus"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发送时间：">{{ parseTime(form.sendTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发送结果：">{{ form.sendCode }} | {{ form.sendMsg }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="API 发送结果：">{{ form.apiSendCode }} | {{ form.apiSendMsg }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="API 短信编号：">{{ form.apiSerialNo }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="API 请求编号：">{{ form.apiRequestId }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接收状态：">
              <dict-tag :type="DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS" :value="form.receiveStatus"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接收时间：">{{ parseTime(form.receiveTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="API 接收结果：">{{ form.apiReceiveCode }} | {{ form.apiReceiveMsg }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getSmsLogPage, exportSmsLogExcel } from "@/api/system/sms/smsLog";
import {  getSimpleSmsChannels } from "@/api/system/sms/smsChannel";

export default {
  name: "SmsLog",
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
      // 短信日志列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        channelId: null,
        templateId: null,
        mobile: null,
        sendStatus: null,
        receiveStatus: null,
        sendTime: [],
        receiveTime: []
      },
      // 短信渠道
      channelOptions: [],
    };
  },
  created() {
    this.getList();
    // 获得短信渠道
    getSimpleSmsChannels().then(response => {
      this.channelOptions = response.data;
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getSmsLogPage(this.queryParams).then(response => {
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$modal.confirm('是否确认导出所有短信日志数据项?').then(() => {
        this.exportLoading = true;
        return exportSmsLogExcel(params);
      }).then(response => {
        this.$download.excel(response, '短信日志.xls');
        this.exportLoading = false;
      }).catch(() => {});
    },
    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    /** 格式化短信渠道 */
    formatChannelSignature(channelId) {
      for (const channel of this.channelOptions) {
        if (channel.id === channelId) {
          return channel.signature;
        }
      }
      return '找不到签名：' + channelId;
    }
  }
};
</script>
