<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="应用编号" prop="appId">
        <el-select clearable v-model="queryParams.appId" filterable placeholder="请选择应用信息">
          <el-option v-for="item in appList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="渠道编码" prop="channelCode">
        <el-select v-model="queryParams.channelCode" placeholder="请输入渠道编码" clearable
                   @clear="()=>{queryParams.channelCode = null}">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_CHANNEL_CODE)" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="商户订单编号" prop="merchantOrderId">
        <el-input v-model="queryParams.merchantOrderId" placeholder="请输入商户订单编号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="渠道订单号" prop="channelOrderNo">
        <el-input v-model="queryParams.channelOrderNo" placeholder="请输入渠道订单号" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="支付状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择支付状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_ORDER_STATUS)" :key="parseInt(dict.value)"
                     :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="退款状态" prop="refundStatus">
        <el-select v-model="queryParams.refundStatus" placeholder="请选择退款状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_ORDER_REFUND_STATUS)" :key="parseInt(dict.value)"
                     :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="回调商户状态" prop="notifyStatus">
        <el-select v-model="queryParams.notifyStatus" placeholder="请选择订单回调商户状态" clearable>
          <el-option v-for="dict in payOrderNotifyDictDatum" :key="parseInt(dict.value)"
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
                   v-hasPermi="['pay:order:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="订单编号" align="center" prop="id" width="80"/>
      <el-table-column label="支付渠道" align="center" width="130">
        <template v-slot="scope">
          <el-popover trigger="hover" placement="top">
            <p>应用名称: {{ scope.row.appName }}</p>
            <p>渠道名称: {{ scope.row.channelCodeName }}</p>
            <div slot="reference" class="name-wrapper">
              {{ scope.row.channelCodeName }}
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="支付订单" align="left" width="280">
        <template v-slot="scope">
          <p class="order-font"><el-tag size="mini">商户单号</el-tag> {{scope.row.merchantOrderId}}</p>
          <p class="order-font"><el-tag size="mini" type="warning">支付单号</el-tag> {{scope.row.channelOrderNo}}</p>
        </template>
      </el-table-column>
      <el-table-column label="商品标题" align="center" prop="subject" width="180" :show-overflow-tooltip="true"/>
      <el-table-column label="支付金额" align="center" prop="price" width="100">
        <template v-slot="scope">
          ￥{{ parseFloat(scope.row.price / 100).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="手续金额" align="center" prop="channelFeePrice" width="100">
        <template v-slot="scope">
          ￥{{ parseFloat(scope.row.channelFeePrice / 100).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="退款金额" align="center" prop="refundPrice" width="100">
        <template v-slot="scope">
          ￥{{ parseFloat(scope.row.refundPrice / 100).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_ORDER_STATUS" :value="scope.row.status" />
        </template>

      </el-table-column>
      <!-- TODO 芋艿：要放开 -->
<!--      <el-table-column label="退款状态" align="center" prop="refundStatus">-->
<!--        <template v-slot="scope">-->
<!--          <span>{{ getDictDataLabel(DICT_TYPE.PAY_ORDER_REFUND_STATUS, scope.row.refundStatus) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="回调状态" align="center" prop="notifyStatus" width="100">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PAY_ORDER_NOTIFY_STATUS" :value="scope.row.notifyStatus" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="100">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="successTime" width="100">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.successTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-search" @click="handleDetail(scope.row)"
                     v-hasPermi="['pay:order:query']">查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(详情) -->
    <el-dialog title="订单详情" :visible.sync="open" width="50%">
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="应用名称">{{ orderDetail.appName }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ orderDetail.subject }}</el-descriptions-item>
      </el-descriptions>
      <el-divider></el-divider>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="商户订单号">
          <el-tag size="small">{{ orderDetail.merchantOrderId }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="渠道订单号">
          <el-tag class="tag-purple" size="small">{{ orderDetail.channelOrderNo }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付订单号">
          <el-tag v-if="orderDetail.extension.no !== ''" class="tag-pink" size="small">
            {{ orderDetail.extension.no }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          <el-tag type="success" size="small">{{ parseFloat(orderDetail.price / 100, 2) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手续费">
          <el-tag type="warning" size="small">{{ parseFloat(orderDetail.channelFeePrice / 100, 2) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手续费比例">
          {{ parseFloat(orderDetail.channelFeeRate / 100, 2) }}%
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <dict-tag :type="DICT_TYPE.PAY_ORDER_STATUS" :value="orderDetail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="回调状态">
          <dict-tag :type="DICT_TYPE.PAY_ORDER_NOTIFY_STATUS" :value="orderDetail.notifyStatus" />
        </el-descriptions-item>
        <el-descriptions-item label="回调地址">{{ orderDetail.notifyUrl }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(orderDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(orderDetail.successTime) }}</el-descriptions-item>
        <el-descriptions-item label="失效时间">{{ parseTime(orderDetail.expireTime) }}</el-descriptions-item>
        <el-descriptions-item label="通知时间">{{ parseTime(orderDetail.notifyTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider></el-divider>
      <el-descriptions :column="2" label-class-name="desc-label">
        <el-descriptions-item label="支付渠道">
          <dict-tag :type="DICT_TYPE.PAY_CHANNEL_CODE" :value="orderDetail.channelCode" />
        </el-descriptions-item>
        <el-descriptions-item label="支付IP">{{ orderDetail.userIp }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <dict-tag :type="DICT_TYPE.PAY_ORDER_REFUND_STATUS" :value="orderDetail.refundStatus" />
        </el-descriptions-item>
        <el-descriptions-item label="退款次数">{{ orderDetail.refundTimes }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <el-tag type="warning">
            {{ parseFloat(orderDetail.refundPrice / 100, 2) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider></el-divider>
      <el-descriptions :column="1" label-class-name="desc-label" direction="vertical" border>
        <el-descriptions-item label="商品描述">
          {{ orderDetail.body }}
        </el-descriptions-item>
        <el-descriptions-item label="支付通道异步回调内容">
          {{ orderDetail.extension.channelNotifyData }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderDetail, getOrderPage, exportOrderExcel } from "@/api/pay/order";
import { DICT_TYPE, getDictDatas } from "@/utils/dict";

export default {
  name: "PayOrder",
  components: {},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 支付订单列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数 TODO 芋艿：多余字段的清理
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        appId: null,
        channelId: null,
        channelCode: null,
        merchantOrderId: null,
        subject: null,
        body: null,
        notifyUrl: null,
        notifyStatus: null,
        price: null,
        channelFeeRate: null,
        channelFeePrice: null,
        status: null,
        userIp: null,
        successExtensionId: null,
        refundStatus: null,
        refundTimes: null,
        refundPrice: null,
        channelUserId: null,
        channelOrderNo: null,
        expireTime: [],
        successTime: [],
        notifyTime: [],
        createTime: []
      },

      // 支付应用列表集合
      appList: [],
      // 订单回调商户状态字典数据集合
      payOrderNotifyDictDatum: getDictDatas(DICT_TYPE.PAY_ORDER_NOTIFY_STATUS),

      // 订单详情
      orderDetail: {
        extension: {}
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
      // 执行查询
      getOrderPage(this.queryParams).then(response => {
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
    /** 详情按钮操作 */
    handleDetail(row) {
      this.orderDetail = {};
      getOrderDetail(row.id).then(response => {
        // 设置值
        this.orderDetail = response.data;
        if (!this.orderDetail.extension) {
          this.orderDetail.extension = {}
        }
        // 弹窗打开
        this.open = true;
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$modal.confirm('是否确认导出所有支付订单数据项?').then(function () {
        return exportOrderExcel(params);
      }).then(response => {
        this.$download.excel(response, '支付订单.xls');
      }).catch(() => {});
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

.order-font{
  font-size: 12px;
  padding: 2px 0;
}
</style>
