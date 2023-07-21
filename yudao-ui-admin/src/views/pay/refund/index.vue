<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="应用编号" prop="appId">
        <el-select clearable v-model="queryParams.appId" filterable placeholder="请选择应用信息">
          <el-option v-for="item in appList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="退款渠道" prop="channelCode">
        <el-select v-model="queryParams.channelCode" placeholder="请选择退款渠道" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_CHANNEL_CODE)" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="商户支付单号" prop="merchantOrderId">
        <el-input v-model="queryParams.merchantOrderId" placeholder="请输入商户支付单号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="商户退款单号" prop="merchantRefundId">
        <el-input v-model="queryParams.merchantRefundId" placeholder="请输入商户退款单号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="渠道支付单号" prop="channelOrderNo">
        <el-input v-model="queryParams.channelOrderNo" placeholder="请输入渠道支付单号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="渠道退款单号" prop="channelRefundNo">
        <el-input v-model="queryParams.channelRefundNo" placeholder="请输入渠道退款单号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="退款状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择退款状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_REFUND_STATUS)" :key="parseInt(dict.value)"
                     :label="dict.label" :value="parseInt(dict.value)"/>
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['pay:refund:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付金额" align="center" prop="payPrice" width="100">
        <template v-slot="scope" class="">
          ￥{{ (scope.row.payPrice / 100.0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="退款金额" align="center" prop="refundPrice" width="100">
        <template v-slot="scope">
          ￥{{ (scope.row.refundPrice / 100.0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="退款订单号" align="left" width="300">
        <template v-slot="scope">
          <p class="order-font">
            <el-tag size="mini">商户</el-tag> {{scope.row.merchantRefundId}}
          </p>
          <p class="order-font">
            <el-tag size="mini" type="warning">退款</el-tag> {{scope.row.no}}
          </p>
          <p class="order-font" v-if="scope.row.channelRefundNo">
            <el-tag size="mini" type="success">渠道</el-tag> {{scope.row.channelRefundNo}}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="支付订单号" align="left" width="300">
        <template v-slot="scope">
          <p class="order-font">
            <el-tag size="mini">商户</el-tag> {{scope.row.merchantOrderId}}
          </p>
          <p class="order-font">
            <el-tag size="mini" type="success">渠道</el-tag> {{scope.row.channelOrderNo}}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="退款状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_REFUND_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="退款渠道" align="center" width="140">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_CHANNEL_CODE" :value="scope.row.channelCode" />
        </template>
      </el-table-column>
      <el-table-column label="成功时间" align="center" prop="successTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.successTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付应用" align="center" prop="successTime" width="100">
        <template v-slot="scope">
          <span>{{ scope.row.appName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-search" @click="handleQueryDetails(scope.row)"
                     v-hasPermi="['pay:order:query']">查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(详情) -->
    <el-dialog title="退款订单详情" :visible.sync="open" width="700px" v-dialogDrag append-to-body>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="商户退款单号">
          <el-tag size="small">{{ refundDetail.merchantRefundId }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="渠道退款单号">
          <el-tag type="success" size="small" v-if="refundDetail.channelRefundNo">{{ refundDetail.channelRefundNo }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="商户支付单号">
          <el-tag size="small">{{ refundDetail.merchantOrderId }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="渠道支付单号">
          <el-tag type="success" size="small">{{ refundDetail.channelOrderNo }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="应用编号">{{ refundDetail.appId }}</el-descriptions-item>
        <el-descriptions-item label="应用名称">{{ refundDetail.appName }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="支付金额">
          <el-tag type="success" size="small">￥{{ (refundDetail.payPrice / 100.0).toFixed(2) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <el-tag size="mini" type="danger">￥{{ (refundDetail.refundPrice / 100.0).toFixed(2) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="退款状态">
          <dict-tag :type="DICT_TYPE.PAY_REFUND_STATUS" :value="refundDetail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ parseTime(refundDetail.successTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="创建时间">{{ parseTime(refundDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(refundDetail.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <!-- 分割线 -->
      <el-divider />
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="退款渠道">
          <dict-tag :type="DICT_TYPE.PAY_CHANNEL_CODE" :value="refundDetail.channelCode" />
        </el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ refundDetail.reason }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="退款 IP">{{ refundDetail.userIp }}</el-descriptions-item>
        <el-descriptions-item label="通知 URL">{{ refundDetail.notifyUrl }}</el-descriptions-item>
      </el-descriptions>
      <!-- 分割线 -->
      <el-divider />
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="渠道错误码">{{refundDetail.channelErrorCode}}</el-descriptions-item>
        <el-descriptions-item label="渠道错误码描述">{{refundDetail.channelErrorMsg}}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions :column="1" label-class-name="desc-label" direction="vertical" border>
        <el-descriptions-item label="支付通道异步回调内容">
          {{ refundDetail.channelNotifyData }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getRefundPage, exportRefundExcel, getRefund } from "@/api/pay/refund";
import { getAppList } from "@/api/pay/app";

export default {
  name: "PayRefund",
  components: {},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 退款订单列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        appId: null,
        channelCode: null,
        merchantOrderId: null,
        merchantRefundId: null,
        channelOrderNo: null,
        channelRefundNo: null,
        status: null,
        createTime: []
      },
      // 支付应用列表集合
      appList: [],
      // 退款订单详情
      refundDetail: {},
    };
  },
  created() {
    this.getList();
    // 获得筛选项
    getAppList().then(response => {
      this.appList = response.data;
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getRefundPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
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
      this.$modal.confirm('是否确认导出所有退款订单数据项?').then(function () {
        return exportRefundExcel(params);
      }).then(response => {
        this.$download.excel(response, '退款订单.xls');
      }).catch(() => {});
    },
    /** 详情按钮操作 */
    handleQueryDetails(row) {
      this.refundDetail = {};
      getRefund(row.id).then(response => {
        this.refundDetail = response.data;
        this.open = true;
      });
    },
  }
};
</script>
<style>
.desc-label {
  font-weight: bold;

}

.tag-purple {
  color: #722ed1;
  background: #f9f0ff;
  border-color: #d3adf7;
}

.tag-cyan {
  color: #13c2c2;
  background: #e6fffb;
  border-color: #87e8de;
}

.tag-pink {
  color: #eb2f96;
  background: #fff0f6;
  border-color: #ffadd2;
}

.order-font {
  font-size: 12px;
  padding: 2px 0;
}
</style>
