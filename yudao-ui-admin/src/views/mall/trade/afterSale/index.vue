<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="spuName">
        <el-input v-model="queryParams.spuName" placeholder="请输入商品 SPU 名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="退款编号" prop="no">
        <el-input v-model="queryParams.no" placeholder="请输入退款编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="售后状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择售后状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TRADE_AFTER_SALE_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="售后方式" prop="way">
        <el-select v-model="queryParams.way" placeholder="请选择售后方式" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TRADE_AFTER_SALE_WAY)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="售后类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择售后类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TRADE_AFTER_SALE_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                        :picker-options="datePickerOptions" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Tab 选项：真正的内容在 Table -->
    <el-tabs v-model="activeTab" type="card" @tab-click="tabClick" style="margin-top: -40px;">
      <el-tab-pane v-for="tab in statusTabs" :key="tab.value" :label="tab.label" :name="tab.value" />
    </el-tabs>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="退款编号" align="center" prop="no" />
      <el-table-column label="订单编号" align="center" prop="orderNo" /> <!-- TODO 芋艿：未来要加个订单链接 -->
      <el-table-column label="商品信息" align="center" prop="spuName" width="auto" min-width="300">
        <!-- TODO @小红：样式不太对，辛苦改改 -->
<!--        <div v-slot="{ row }" class="goods-info">-->
<!--          <img :src="row.picUrl"/>-->
<!--          <span class="ellipsis-2" :title="row.name">{{row.name}}</span>-->
<!--        </div>-->
      </el-table-column>
      <el-table-column label="订单金额" align="center" prop="refundPrice">
        <template v-slot="scope">
          <span>￥{{ (scope.row.refundPrice / 100.0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="买家" align="center" prop="user.nickname" /> <!-- TODO 芋艿：未来要加个会员链接 -->
      <el-table-column label="申请时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="售后状态" align="center">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.TRADE_AFTER_SALE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="售后方式" align="center">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.TRADE_AFTER_SALE_WAY" :value="scope.row.way" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-thumb"
                     >处理退款</el-button>
<!--      @click="handleUpdate(scope.row)"    v-hasPermi="['trade:after-sale:update']"-->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { getAfterSalePage } from "@/api/mall/trade/afterSale";
import { datePickerOptions } from "@/utils/constants";
import { DICT_TYPE, getDictDatas } from "@/utils/dict";

export default {
  name: "AfterSale",
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
      // 交易售后列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        no: null,
        status: null,
        orderNo: null,
        spuName: null,
        createTime: [],
        way: null,
        type: null,
      },
      // Tab 筛选
      activeTab: 'all',
      statusTabs: [{
        label: '全部',
        value: 'all'
      }],
      // 静态变量
      datePickerOptions: datePickerOptions
    };
  },
  created() {
    this.getList();
    // 设置 statuses 过滤
    for (const dict of getDictDatas(DICT_TYPE.TRADE_AFTER_SALE_STATUS)) {
      this.statusTabs.push({
        label: dict.label,
        value: dict.value
      })
    }
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getAfterSalePage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.activeTab = this.queryParams.status ? this.queryParams.status : 'all'; // 处理 tab
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.activeTab = 'all'; // 处理 tab
      this.handleQuery();
    },
    /** tab 切换 */
    tabClick(tab) {
      this.queryParams.status = tab.name === 'all' ? undefined : tab.name;
      this.getList();
    },
    goToDetail (row) {
      this.$router.push({ path: '/mall/trade/order/detail', query: { orderNo: row.orderNo }})
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .table-wrapper {
  .el-table__row{
    .el-table__cell {
      border-bottom: none;
      .cell{
        .el-table {
          .el-table__row {
            >.el-table__cell {
              .goods-info{
                display: flex;
                img{
                  margin-right: 10px;
                  width: 60px;
                  height: 60px;
                  border: 1px solid #e2e2e2;
                }
              }
              .ellipsis-2 {
                display: -webkit-box;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: normal;
                -webkit-line-clamp: 2; /* 要显示的行数 */
                -webkit-box-orient: vertical;
                word-break: break-all;
                line-height: 22px !important;
                max-height: 44px !important;
              }
            }
          }
        }
      }
    }
  }
}
</style>
