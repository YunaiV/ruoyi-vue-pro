<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <!-- 搜索工作栏 -->
    <!-- TODO: inline 看看是不是需要; v-show= 那块逻辑还是要的 -->
    <el-row :gutter="20">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
        <el-col :span="6" :xs="24">
          <el-form-item label="搜索方式" prop="searchValue">
            <el-input v-model="queryParams.searchValue" style="width: 240px">
              <el-select v-model="queryParams.searchType" slot="prepend" style="width: 100px">
                <el-option v-for="dict in searchTypes" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单类型" prop="type">
            <el-select v-model="queryParams.type" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TRADE_ORDER_TYPE)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单状态" prop="status">
            <el-select v-model="queryParams.status" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TRADE_ORDER_STATUS)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单来源" prop="terminal">
            <el-select v-model="queryParams.terminal" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TERMINAL)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="支付方式" prop="payChannelCode">
            <el-select v-model="queryParams.payChannelCode" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_CHANNEL_CODE_TYPE)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="下单时间" prop="createTime">
            <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                            range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                            :picker-options="datePickerOptions" :default-time="['00:00:00', '23:59:59']" />
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24" style="line-height: 32px">
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-col>
      </el-form>
    </el-row>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- tab切换 -->
    <!-- TODO @小程：看看能不能往上挪 -40px，和【隐藏搜索】【刷新】对齐 -->
    <el-tabs v-model="activeTab" type="card" @tab-click="tabClick">
      <el-tab-pane v-for="tab in statusTabs" :key="tab.value" :label="tab.label" :name="tab.value">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list" :show-header="false" class="order-table">
          <el-table-column>
            <template v-slot="{ row }">
              <el-row type="flex" align="middle">
                <el-col :span="5">
                  订单号：{{row.no}}
                  <el-popover title="支付单号：" :content="row.payOrderId + ''" placement="right" width="200" trigger="click">
                    <el-button slot="reference" type="text">更多</el-button>
                  </el-popover>
                </el-col>
                <el-col :span="5">下单时间：{{ parseTime(row.createTime) }}</el-col>
                <el-col :span="4">订单来源：
                  <dict-tag :type="DICT_TYPE.TERMINAL" :value="row.terminal" />
                </el-col>
                <el-col :span="4">支付方式：
                  <dict-tag v-if="row.payChannelCode" :type="DICT_TYPE.PAY_CHANNEL_CODE_TYPE" :value="row.payChannelCode" />
                  <span v-else>未支付</span>
                </el-col>
                <el-col :span="6" align="right">
                  <el-button type="text" @click="goToDetail(row)">详情</el-button>
                </el-col>
              </el-row>
              <!-- 订单下的商品 -->
              <el-table :data="row.items" border :show-header="true">
                <el-table-column label="商品" prop="goods" header-align="center" width="auto" min-width="300">
                  <template v-slot="{ row, $index }">
                    <div class="goods-info">
                      <img :src="row.picUrl"/>
                      <span class="ellipsis-2" :title="row.spuName">{{row.spuName}}</span>
                      <!-- TODO @小程：下面是商品属性，想当度一行，放在商品名下面 -->
                      <el-tag size="medium" v-for="property in row.properties" :key="property.propertyId">
                        {{property.propertyName}}：{{property.valueName}}</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="单价(元)/数量" prop="fee" align="center" width="115">
                  <template v-slot="{ row }">
                    <div>￥{{ (row.originalUnitPrice / 100.0).toFixed(2) }}</div>
                    <div>{{row.count}} 件</div>
                  </template>
                </el-table-column>
                <!-- TODO @小程：这里应该是一个订单下，多个商品，只展示订单上的总金额，就是 order.payPrice -->
                <el-table-column label="实付金额(元)" prop="amount" align="center" width="100"/>
                <!-- TODO @小程：这里应该是一个订单下，多个商品，只展示订单上的收件信息；使用 order.receiverXXX 开头的字段 -->
                <el-table-column label="买家/收货人" prop="buyer" header-align="center" width="auto" min-width="300">
                  <template v-slot="{ row }">
                    <!-- TODO @芋艿：以后增加一个会员详情界面 -->
                    <div>{{row.buyer}}</div>
                    <div>{{row.receiver}}{{row.tel}}</div>
                    <div class="ellipsis-2" :title="row.address">{{row.address}}</div>
                  </template>
                </el-table-column>
                <!-- TODO @小程：这里应该是一个订单下，多个商品，交易状态是统一的；使用 order.status 字段 -->
                <el-table-column label="交易状态" prop="status" align="center" width="100"/>
              </el-table>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { getOrderPage } from "@/api/mall/trade/order";
import { datePickerOptions } from "@/utils/constants";
import { DICT_TYPE, getDictDatas } from "@/utils/dict";

export default {
  name: "index",
  data () {
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
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        searchType: 'no',
        searchValue: '',
        type: null,
        status: null,
        payChannelCode: null,
        createTime: [],
      },
      // Tab 筛选
      activeTab: 'all',
      statusTabs: [{
        label: '全部',
        value: 'all'
      }],
      // 静态变量
      datePickerOptions: datePickerOptions,
      searchTypes: [
        { label: '订单号', value: 'no' },
        { label: '会员编号', value: 'userId' },
        { label: '会员昵称', value: 'userNickname' },
        { label: '会员手机号', value: 'userMobile' },
        { label: '收货人姓名', value: 'receiverName' },
        { label: '收货人手机号码', value: 'receiverMobile' },
      ],
    }
  },
  created() {
    this.getList();
    // 设置 statuses 过滤
    for (const dict of getDictDatas(DICT_TYPE.TRADE_ORDER_STATUS)) {
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
      getOrderPage({
        ...this.queryParams,
        searchType: undefined,
        searchValue: undefined,
        no: this.queryParams.searchType === 'no' ? this.queryParams.searchValue : undefined,
        userId: this.queryParams.searchType === 'userId' ? this.queryParams.searchValue : undefined,
        userNickname: this.queryParams.searchType === 'userNickname' ? this.queryParams.searchValue : undefined,
        userMobile: this.queryParams.searchType === 'userMobile' ? this.queryParams.searchValue : undefined,
        receiverName: this.queryParams.searchType === 'receiverName' ? this.queryParams.searchValue : undefined,
        receiverMobile: this.queryParams.searchType === 'receiverMobile' ? this.queryParams.searchValue : undefined,
      }).then(response => {
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
      this.handleQuery();
    },
    /** tab 切换 */
    tabClick(tab) {
      this.queryParams.status = tab.name === 'all' ? undefined : tab.name;
      this.getList();
    },
    goToDetail (row) {
      this.$router.push({ path: '/trade/order/detail', query: { id: row.id }})
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .order-table{
  border-bottom: none;
  &::before{
    height: 0;
  }
  .el-table__row{
    .el-table__cell{
      border-bottom: none;
      .cell{
        .el-table {
          .el-table__row{
            >.el-table__cell{
              .goods-info{
                display: flex;
                img{
                  margin-right: 10px;
                  width: 60px;
                  height: 60px;
                  border: 1px solid #e2e2e2;
                }
              }
              .ellipsis-2{
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
