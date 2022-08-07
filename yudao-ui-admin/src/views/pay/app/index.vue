<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="应用名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入应用名" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="商户名称" prop="merchantName">
        <el-input v-model="queryParams.merchantName" placeholder="请输入商户名称" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="开启状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择开启状态" clearable>
          <el-option v-for="dict in statusDictDatas" :key="parseInt(dict.value)" :label="dict.label"
                     :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['pay:app:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['pay:app:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="应用编号" align="center" prop="id"/>
      <el-table-column label="应用名" align="center" prop="name"/>
      <el-table-column label="开启状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1"
                     @change="handleStatusChange(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column label="商户名称" align="center" prop="payMerchant.name"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付宝配置" align="center">
        <el-table-column :label="payChannelEnum.ALIPAY_APP.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.ALIPAY_APP.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.ALIPAY_APP.code,payType.ALIPAY)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.ALIPAY_APP.code,payType.ALIPAY)">
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="payChannelEnum.ALIPAY_PC.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.ALIPAY_PC.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.ALIPAY_PC.code,payType.ALIPAY)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.ALIPAY_PC.code,payType.ALIPAY)">
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="payChannelEnum.ALIPAY_WAP.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.ALIPAY_WAP.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.ALIPAY_WAP.code,payType.ALIPAY)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.ALIPAY_WAP.code,payType.ALIPAY)">
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="payChannelEnum.ALIPAY_QR.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.ALIPAY_QR.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.ALIPAY_QR.code,payType.ALIPAY)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.ALIPAY_QR.code,payType.ALIPAY)">
            </el-button>
          </template>
        </el-table-column>
      </el-table-column>
      <el-table-column label="微信配置" align="center">
        <el-table-column :label="payChannelEnum.WX_LITE.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.WX_LITE.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.WX_LITE.code,payType.WECHAT)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.WX_LITE.code,payType.WECHAT)">
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="payChannelEnum.WX_PUB.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.WX_PUB.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.WX_PUB.code,payType.WECHAT)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.WX_PUB.code,payType.WECHAT)">
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="payChannelEnum.WX_APP.name" align="center">
          <template slot-scope="scope">
            <el-button type="success" icon="el-icon-check" circle
                       v-if="judgeChannelExist(scope.row.channelCodes,payChannelEnum.WX_APP.code)"
                       @click="handleUpdateChannel(scope.row,payChannelEnum.WX_APP.code,payType.WECHAT)">
            </el-button>
            <el-button v-else
                       type="danger" icon="el-icon-close" circle
                       @click="handleCreateChannel(scope.row,payChannelEnum.WX_APP.code,payType.WECHAT)">
            </el-button>
          </template>
        </el-table-column>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['pay:app:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['pay:app:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="160px">
        <el-form-item label="应用名" prop="name">
          <el-input v-model="form.name" placeholder="请输入应用名"/>
        </el-form-item>
        <el-form-item label="所属商户" prop="merchantId">
          <el-select
            v-model="form.merchantId"
            filterable
            remote
            reserve-keyword
            placeholder="请选择所属商户"
            :remote-method="handleGetMerchantListByName"
            :loading="loading">
            <el-option
              v-for="item in merchantList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开启状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusDictDatas" :key="parseInt(dict.value)" :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="支付结果的回调地址" prop="payNotifyUrl">
          <el-input v-model="form.payNotifyUrl" placeholder="请输入支付结果的回调地址"/>
        </el-form-item>
        <el-form-item label="退款结果的回调地址" prop="refundNotifyUrl">
          <el-input v-model="form.refundNotifyUrl" placeholder="请输入退款结果的回调地址"/>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <wechat-channel-form :transferParam="channelParam"></wechat-channel-form>
    <ali-pay-channel-form :transferParam="channelParam"></ali-pay-channel-form>
  </div>
</template>

<script>
import {createApp, updateApp, changeAppStatus, deleteApp, getApp, getAppPage, exportAppExcel} from "@/api/pay/app";
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {PayType, PayChannelEnum, CommonStatusEnum} from "@/utils/constants";
import {getMerchantListByName} from "@/api/pay/merchant";
import wechatChannelForm from "@/views/pay/app/components/wechatChannelForm";
import aliPayChannelForm from "@/views/pay/app/components/aliPayChannelForm";

export default {
  name: "App",
  components: {
    "wechatChannelForm": wechatChannelForm,
    "aliPayChannelForm": aliPayChannelForm
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 支付应用信息列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        status: null,
        remark: null,
        payNotifyUrl: null,
        refundNotifyUrl: null,
        merchantName: null,
        createTime: []
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{required: true, message: "应用名不能为空", trigger: "blur"}],
        status: [{required: true, message: "开启状态不能为空", trigger: "blur"}],
        payNotifyUrl: [{required: true, message: "支付结果的回调地址不能为空", trigger: "blur"}],
        refundNotifyUrl: [{required: true, message: "退款结果的回调地址不能为空", trigger: "blur"}],
        merchantId: [{required: true, message: "商户编号不能为空", trigger: "blur"}],
      },
      // 数据字典
      statusDictDatas: getDictDatas(DICT_TYPE.COMMON_STATUS),
      sysCommonStatusEnum: CommonStatusEnum,
      // 支付渠道枚举
      payChannelEnum: PayChannelEnum,
      // 支付类型
      payType: PayType,
      // 商户列表
      merchantList: [],
      // 是否显示支付窗口
      payOpen: false,
      // 微信组件传参参数
      channelParam: {
        // 是否修改
        "edit": false,
        // 微信是否显示
        "wechatOpen": false,
        // 支付宝是否显示
        "aliPayOpen": false,
        // 应用ID
        "appId": null,
        // 渠道编码
        "payCode": null,
        // 商户对象
        "payMerchant": {
          // 编号
          "id": null,
          // 名称
          "name": null
        },
      }
    };
  },
  created() {
    this.getList();
    this.handleGetMerchantListByName(null);
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getAppPage(this.queryParams).then(response => {
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
        name: undefined,
        status: undefined,
        remark: undefined,
        payNotifyUrl: undefined,
        refundNotifyUrl: undefined,
        merchantId: undefined,
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加支付应用信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getApp(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改支付应用信息";
      });
    },
    // 用户状态修改
    handleStatusChange(row) {
      let text = row.status === CommonStatusEnum.ENABLE ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.name + '"应用吗?').then(function () {
        return changeAppStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function () {
        row.status = row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.DISABLE
          : CommonStatusEnum.ENABLE;
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
          updateApp(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createApp(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除支付应用信息编号为"' + row.id + '"的数据项?').then(function () {
        return deleteApp(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$modal.confirm('是否确认导出所有支付应用信息数据项?').then(function () {
        return exportAppExcel(params);
      }).then(response => {
        this.$download.excel(response, '支付应用信息.xls');
      }).catch(() => {});
    },
    /**
     * 根据商户名称模糊匹配商户信息
     * @param name 商户名称
     */
    handleGetMerchantListByName(name) {
      getMerchantListByName(name).then(response => {
        this.merchantList = response.data;
      });
    },
    /**
     * 修改支付渠道信息
     */
    handleUpdateChannel(row, payCode, type) {
      this.settingChannelParam(row, payCode, type)
      this.channelParam.edit = true;
      this.channelParam.loading = true;

    },
    /**
     * 新增支付渠道信息
     */
    handleCreateChannel(row, payCode, type) {
      this.settingChannelParam(row, payCode, type)
      this.channelParam.edit = false;
      this.channelParam.loading = false;
    },
    /**
     * 设置支付渠道信息
     */
    settingChannelParam(row, payCode, type) {
      if (type === PayType.WECHAT) {
        this.channelParam.wechatOpen = true;
        this.channelParam.aliPayOpen = false;
      }
      if (type === PayType.ALIPAY) {
        this.channelParam.aliPayOpen = true;
        this.channelParam.wechatOpen = false;
      }
      this.channelParam.edit = false;
      this.channelParam.loading = false;
      this.channelParam.appId = row.id;
      this.channelParam.payCode = payCode;
      this.channelParam.payMerchant = row.payMerchant;
    },
    /**
     * 根据渠道编码判断渠道列表中是否存在
     * @param channels 渠道列表
     * @param channelCode 渠道编码
     */
    judgeChannelExist(channels, channelCode) {
      return channels.indexOf(channelCode) !== -1;
    },
    refreshTable() {
      this.getList();
    }
  }
};
</script>
