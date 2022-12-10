<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <!-- TODO: inline 看看是不是需要; v-show= 那块逻辑还是要的 -->
    <el-row :gutter="20">
      <el-form :model="queryParams" label-width="68px" size="small">
        <el-col :span="6" :xs="24">
          <el-form-item label="搜索方式">
            <el-input style="width: 240px">
              <el-select v-model="queryParams.searchType" slot="prepend" clearable style="width: 100px">
                <el-option v-for="dict in dicData.searchType" v-bind="dict" :key="dict.value"/>
              </el-select>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单类型">
            <el-select v-model="queryParams.orderType" clearable style="width: 240px">
              <el-option v-for="dict in dicData.orderType" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单状态">
            <el-select v-model="queryParams.orderStatus" clearable style="width: 240px">
              <el-option v-for="dict in dicData.orderStatus" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单来源">
            <el-select v-model="queryParams.terminal" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.TERMINAL)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="支付方式">
            <el-select v-model="queryParams.payChannelCode" clearable style="width: 240px">
              <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PAY_CHANNEL_CODE_TYPE)"
                         :key="dict.value" :label="dict.label" :value="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="下单时间">
            <el-date-picker v-model="queryParams.date" type="daterange" range-separator="至"
                            start-placeholder="开始日期" end-placeholder="结束日期" :picker-options="datePickerOptions" style="width: 240px"/>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24" style="line-height: 32px">
          <el-button type="primary" icon="el-icon-search" size="mini">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini">重置</el-button>
        </el-col>
      </el-form>
    </el-row>

    <!-- tab切换-->
    <el-tabs v-model="activeTabName" type="card">
      <el-tab-pane v-for="tabPane in tabPanes" :label="tabPane.text" :name="tabPane.name">
        <!-- table -->
        <el-table :data="list" :show-header="false" class="order-table">
          <el-table-column>
            <template slot-scope="{ row }">
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
                  <el-dropdown style="margin-left: 10px">
                    <el-button type="text">
                      更多操作<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <!-- TODO @芋艿：【暂未开发】关闭订单 -->
                      <el-dropdown-item><el-button type="text">关闭订单</el-button></el-dropdown-item>
                      <!-- TODO @芋艿：【暂未开发】修改地址 -->
                      <el-dropdown-item><el-button type="text">修改地址</el-button></el-dropdown-item>
                      <!-- TODO @芋艿：【暂未开发】调整价格 -->
                      <el-dropdown-item><el-button type="text">调整价格</el-button></el-dropdown-item>
                      <!-- TODO @芋艿：【暂未开发】备注 -->
                      <el-dropdown-item><el-button type="text">备注</el-button></el-dropdown-item>
                      <!-- TODO @芋艿：【暂未开发】关闭订单 -->
                      <el-dropdown-item><el-button type="text">打印发货单</el-button></el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </el-col>
              </el-row>
              <!-- 订单下的商品 -->
              <el-table :data="row.items" border :show-header="true">
                <el-table-column label="商品" prop="goods" header-align="center" width="auto" min-width="300">
                  <template slot-scope="{ row, $index }">
                    <div class="goods-info">
                      <img :src="row.picUrl"/>
                      <span class="ellipsis-2" :title="row.spuName">{{row.spuName}}</span>
                      <!-- TODO @小程：下面是商品属性，想当度一行，放在商品名下面 -->
                      <el-tag size="medium" v-for="property in row.properties">{{property.propertyName}}：{{property.valueName}}</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="单价(元)/数量" prop="fee" align="center" width="115">
                  <template slot-scope="{ row }">
                    <div>￥{{ (row.originalUnitPrice / 100.0).toFixed(2) }}</div>
                    <div>{{row.count}} 件</div>
                  </template>
                </el-table-column>
                <!-- TODO @小程：这里应该是一个订单下，多个商品，只展示订单上的总金额，就是 order.payPrice -->
                <el-table-column label="实付金额(元)" prop="amount" align="center" width="100"/>
                <!-- TODO @小程：这里应该是一个订单下，多个商品，只展示订单上的收件信息；使用 order.receiverXXX 开头的字段 -->
                <el-table-column label="买家/收货人" prop="buyer" header-align="center" width="auto" min-width="300">
                  <template slot-scope="{ row }">
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
  </div>
</template>

<script>
import { getOrderPage } from "@/api/mall/trade/order";
import { datePickerOptions } from "@/utils/constants";

const dicData = {
    searchType: [
      { label: '订单号', value: 'ddh' },
      { label: '交易流水号', value: 'jylsh' },
      { label: '订单备注', value: 'ddbz' },
      { label: '收货人姓名', value: 'shrxm' },
      { label: '商品名称', value: 'spmc' },
      { label: '收货人电话', value: 'shrdh' },
      { label: '会员昵称', value: 'hync' },
      { label: '商品编号', value: 'spbh' }
    ],
    orderStatus: [
      { label: '全部', value: 'qb' },
      { label: '待支付', value: 'dzf' },
      { label: '待发货', value: 'dfh' },
      { label: '已发货', value: 'yfh' },
      { label: '已收货', value: 'ysh' },
      { label: '已完成', value: 'ywc' },
      { label: '已关闭', value: 'ygb' },
      { label: '退款中', value: 'tkz' }
    ],
  }
  export default {
    name: "index",
    data () {
      return {
        dicData,
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
          searchType: 'ddh',
          orderType: ''
        },
        activeTabName: 'all',
        tabPanes: [
          { text: '全部', name: 'all' },
          { text: '待支付', name: 'toBePay' },
          { text: '待发货', name: 'toBeSend' },
          { text: '已发货', name: 'send' },
          { text: '已收货', name: 'received' },
          { text: '已完成', name: 'finished' },
          { text: '已关闭', name: 'closed' },
          { text: '退款中', name: 'refund' }
        ],
        // 静态变量
        datePickerOptions: datePickerOptions
      }
    },
    created() {
      this.getList();
      // 设置 statuses 过滤
      // for (const dict of getDictDatas(DICT_TYPE.TRADE_AFTER_SALE_STATUS)) {
      //   this.statusTabs.push({
      //     label: dict.label,
      //     value: dict.value
      //   })
      // }
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
      goToDetail (row) {
        this.$router.push({ path: '/mall/trade/order/detail', query: { orderNo: row.orderNo }})
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
