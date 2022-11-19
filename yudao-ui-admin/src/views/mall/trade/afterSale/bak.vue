<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <el-row :gutter="20">
      <el-form :model="queryParams" label-width="68px" size="small">
        <el-col :span="6" :xs="24">
          <el-form-item label="下单时间">
            <el-date-picker v-model="queryParams.date" type="daterange" range-separator="至"
                            start-placeholder="开始日期" end-placeholder="结束日期" :picker-options="rangePickerOptions" style="width: 240px"/>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24" style="line-height: 32px">
          <el-button type="primary" icon="el-icon-search" size="mini">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini">重置</el-button>
          <el-button icon="el-icon-document" size="mini">导出订单</el-button>
        </el-col>
      </el-form>
    </el-row>

    <!-- tab切换-->
    <el-tabs v-model="activeTabName" type="card">
      <el-tab-pane v-for="tabPane in tabPanes" :label="tabPane.text" :name="tabPane.name">
        <!-- table -->
        <el-table :data="tableData" :show-header="false" class="table-wrapper">
          <el-table-column>
            <template slot-scope="{ row }">
              <!-- 订单下的商品 -->
              <el-table :data="row.goods" border>
                <el-table-column label="商品信息" prop="spxx" header-align="center" width="auto" min-width="300">
                  <div slot-scope="{ row, $index }" class="goods-info">
                    <img :src="row.img"/>
                    <span class="ellipsis-2" :title="row.name">{{row.name}}</span>
                  </div>
                </el-table-column>
                <el-table-column label="订单金额" prop="ddje" align="center" width="100"/>
                <el-table-column label="买家" prop="mj" align="center" width="100"/>
                <el-table-column label="退款金额" prop="tkje" align="center" width="100"/>
                <el-table-column label="申请时间" prop="sqsj" align="center" width="180"/>
                <el-table-column label="退款状态" prop="tkzt" align="center" width="100"/>
                <el-table-column label="操作" align="center" width="100" fixed="right" >
                  <el-button slot-scope="{row}" type="text">详情</el-button>
                </el-table-column>
              </el-table>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
const dicData = {
  refundStatus: [
    { label: '全部', value: 'qb' },
    { label: '申请维权', value: 'sqwq' },
    { label: '待转账', value: 'dzz' },
    { label: '维权结束', value: 'wqjs' },
    { label: '买家待退货', value: 'mjdth' },
    { label: '卖家待收货', value: 'mjdsh' },
    { label: '卖家已收货', value: 'mjysh' },
    { label: '卖家拒绝', value: 'mjjj' }
  ],
  refundWay: [
    { label: '全部', value: 'qb' },
    { label: '仅退款', value: 'jtk' },
    { label: '退货退款', value: 'thtk' }
  ],
  refundType: [
    { label: '全部', value: 'qb' },
    { label: '订单退款', value: 'ddtk' },
    { label: '售后退款', value: 'shtk' }
  ]
}

export default {
    name: "index",
    data () {
      return {
        dicData,
        rangePickerOptions,
        queryParams: {},
        activeTabName: 'all',
        tabPanes: [
          { text: '全部', name: 'all' },
          { text: '申请维权', name: 'sqwq' },
          { text: '待转账', name: 'dzz' },
          { text: '维权结束', name: 'wqjs' },
          { text: '买家待退货', name: 'mjdth' },
          { text: '卖家待收货', name: 'mjdsh' },
          { text: '卖家已收货', name: 'mjysh' },
          { text: '卖家拒绝', name: 'mjjj' }
        ],
        tableData: [
          {
            tkbh: '20221026220424001',
            ddbh: '20221026220424001',
            goods: [
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                img: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                ddje: '199',
                mj: '张三',
                tkje: 460,
                sqsj: '2022-11-19',
                tkzt: '申请维权(仅退款)'
              }
            ]
          },
          {
            tkbh: '20221026220424001',
            ddbh: '20221026220424001',
            goods: [
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                img: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                ddje: '199',
                mj: '张三',
                tkje: 460,
                sqsj: '2022-11-19',
                tkzt: '申请维权(仅退款)'
              }
            ]
          },
          {
            tkbh: '20221026220424001',
            ddbh: '20221026220424001',
            goods: [
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                img: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                ddje: '199',
                mj: '张三',
                tkje: 460,
                sqsj: '2022-11-19',
                tkzt: '申请维权(仅退款)'
              }
            ]
          }
        ]
      }
    },
    methods: {
      goToDetail (row) {
        this.$router.push({ path: '/mall/trade/order/detail', query: { orderNo: row.orderNo }})
      }
    }
  }
</script>
