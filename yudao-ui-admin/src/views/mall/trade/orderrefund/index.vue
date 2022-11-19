<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <el-row :gutter="20">
      <el-form :model="queryParams" label-width="68px" size="small">
        <el-col :span="6" :xs="24">
          <el-form-item label="商品名称">
            <el-input v-model="queryParams.name" style="width: 240px"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="订单编号">
            <el-input v-model="queryParams.No" style="width: 240px"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="退款编号">
            <el-input v-model="queryParams.refundNo" style="width: 240px"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="退款状态">
            <el-select v-model="queryParams.refundStatus" clearable style="width: 240px">
              <el-option v-for="dict in dicData.refundStatus" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="退款方式">
            <el-select v-model="queryParams.refundWay" clearable style="width: 240px">
              <el-option v-for="dict in dicData.refundWay" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="维权类型">
            <el-select v-model="queryParams.refundType" clearable style="width: 240px">
              <el-option v-for="dict in dicData.refundType" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
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
              <div class="table-header">
                退款编号：{{row.tkbh}}
                <el-button type="text" style="margin-left: 10px">
                  订单编号：{{row.ddbh}}
                </el-button>
              </div>
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
const rangePickerOptions = {
  shortcuts: [{
    text: '最近一周',
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      picker.$emit('pick', [start, end]);
    }
  }, {
    text: '最近一个月',
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      picker.$emit('pick', [start, end]);
    }
  }, {
    text: '最近三个月',
    onClick(picker) {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      picker.$emit('pick', [start, end]);
    }
  }]
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
              },
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
              },
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
              },
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

<style lang="scss" scoped>
  ::v-deep .table-wrapper{
    border-bottom: none;
    &::before{
      height: 0;
    }
    .table-header{
      line-height: 36px;
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
