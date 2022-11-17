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
            <el-select v-model="queryParams.orderSource" clearable style="width: 240px">
              <el-option v-for="dict in dicData.orderSource" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="付款方式">
            <el-select v-model="queryParams.payWay" clearable style="width: 240px">
              <el-option v-for="dict in dicData.payWay" v-bind="dict" :key="dict.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" :xs="24">
          <el-form-item label="营销类型">
            <el-select v-model="queryParams.marketingType" clearable style="width: 240px">
              <el-option v-for="dict in dicData.marketingType" v-bind="dict" :key="dict.value"/>
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
    <el-radio-group v-model="activeTabName">
      <el-radio-button v-for="tabPane in tabPanes" :label="tabPane.label">{{tabPane.text}}</el-radio-button>
    </el-radio-group>

    <!-- table -->
    <el-table :data="tableData" :show-header="false" class="order-table">
      <el-table-column label="订单信息">
        <template slot-scope="{ row }">
          <el-row>
            <el-col :span="5">
              订单号：{{row.orderNo}}
              <el-popover title="支付流水号：" :content="row.payNo" ref="popover" placement="right" width="200" trigger="click"/>
              <el-button type="text" v-popover:popover>更多</el-button>
            </el-col>
            <el-col :span="5">下单时间：{{row.time}}</el-col>
            <el-col :span="4">订单来源：{{row.orderSource}}</el-col>
            <el-col :span="4">支付方式：{{row.payWay}}</el-col>
            <el-col :span="6" align="right" type="flex">
              <el-button type="text">关闭订单</el-button>
              <el-button type="text">修改地址</el-button>
              <el-button type="text">调整价格</el-button>
              <el-dropdown style="margin-left: 10px">
                <el-button type="text">
                  更多操作<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item><el-button type="text">打印发货单</el-button></el-dropdown-item>
                  <el-dropdown-item><el-button type="text" @click="goToDetail(row)">详情</el-button></el-dropdown-item>
                  <el-dropdown-item><el-button type="text">备注</el-button></el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>
          </el-row>
          <!-- 订单下的商品 -->
          <el-table :data="row.goods" border>
            <el-table-column label="商品" prop="goods" header-align="center" width="360">
              <template slot-scope="{ row, $index }">
                <div class="goods-info">
                  <img :src="row.picture"/>
                  <span class="ellipsis-2" :title="row.name">{{row.name}}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价(元)/数量" prop="fee" align="center" width="115">
              <template slot-scope="{ row }">
                <div>{{row.price}}</div>
                <div>{{row.count}}</div>
              </template>
            </el-table-column>
            <el-table-column label="维权" prop="safeguard" align="center" width="115"/>
            <el-table-column label="实付金额(元)" prop="amount" align="center" width="115"/>
            <el-table-column label="买家/收货人" prop="buyer" header-align="center" width="360">
              <template slot-scope="{ row }">
                <div>{{row.buyer}}</div>
                <div>{{row.receiver}}{{row.tel}}</div>
                <div class="ellipsis-2" :title="row.address">{{row.address}}</div>
              </template>
            </el-table-column>
            <el-table-column label="配送方式" prop="sendWay" align="center" width="115"/>
            <el-table-column label="交易状态" prop="status" align="center"/>
          </el-table>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
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
    orderType: [
      { label: '全部', value: 'qb' },
      { label: '物流订单', value: 'wldd' },
      { label: '自提订单', value: 'ztdd' },
      { label: '外卖订单', value: 'wmdd' },
      { label: '虚拟订单', value: 'xndd' },
      { label: '收银订单', value: 'sydd' }
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
    orderSource: [
      { label: '全部', value: 'qb' },
      { label: '微信公众号', value: 'wxgzh' },
      { label: '微信小程序', value: 'wxxcx' },
      { label: 'PC', value: 'pc' },
      { label: 'H5', value: 'h5' },
      { label: 'APP', value: 'app' },
      { label: '收银台', value: 'syt' },
      { label: '代客下单', value: 'dkxd' }
    ],
    payWay: [
      { label: '全部', value: 'qb' },
      { label: '在线支付', value: 'zxzf' },
      { label: '余额支付', value: 'yezf' },
      { label: '线下支付', value: 'xxzf' },
      { label: '积分兑换', value: 'jfdh' },
      { label: '支付宝支付', value: 'zfbzf' },
      { label: '微信支付', value: 'wxzf' }
    ],
    marketingType: [
      { label: '全部', value: 'qb' },
      { label: '一口价', value: 'ykj' },
      { label: '专题', value: 'zt' },
      { label: '团购', value: 'tg' },
      { label: '拼团', value: 'pt' },
      { label: '拼团返利', value: 'ptfl' },
      { label: '盲盒', value: 'mh' },
      { label: '砍价', value: 'kj' },
      { label: '礼品卡优惠', value: 'lpkyh' },
      { label: '秒杀', value: 'ms' },
      { label: '积分兑换', value: 'jfdh' },
      { label: '组合套餐', value: 'zhtc' },
      { label: '预售', value: 'ys' }
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
        queryParams: {
          searchType: 'ddh',
          orderType: ''
        },
        activeTabName: 'all',
        tabPanes: [
          { text: '全部', label: 'all' },
          { text: '待支付', label: 'toBePay' },
          { text: '待发货', label: 'toBeSend' },
          { text: '已发货', label: 'send' },
          { text: '已收货', label: 'received' },
          { text: '已完成', label: 'finished' },
          { text: '已关闭', label: 'closed' },
          { text: '退款中', label: 'refund' }
        ],
        tableData: [
          {
            orderInfo: '',
            orderNo: '20221026220424001',
            payNo: '20221026220424001',
            time: '2022-10-26 22:04:20',
            orderSource: 'PC',
            payWay: '微信支付',
            goods: [
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                picture: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                price: '199',
                count: '5件',
                amount: 460,
                safeguard: '主动退款',
                buyer: '小明',
                receiver: '小花',
                tel: '15823655095',
                address: '北京市-北京市-东城区 观音桥',
                sendWay: '物流配送',
                status: '已完成'
              },
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                picture: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                price: '199',
                count: '5件',
                amount: 460,
                safeguard: '主动退款',
                buyer: '小明',
                receiver: '小花',
                tel: '15823655095',
                address: '北京市-北京市-东城区 观音桥',
                sendWay: '物流配送',
                status: '已完成'
              }
            ]
          },
          {
            orderInfo: '',
            orderNo: '20221026220424001',
            payNo: '20221026220424001',
            time: '2022-10-26 22:04:20',
            orderSource: 'PC',
            payWay: '微信支付',
            goods: [
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                picture: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                price: '199',
                count: '5件',
                amount: 460,
                safeguard: '主动退款',
                buyer: '小明',
                receiver: '小花',
                tel: '15823655095',
                address: '北京市-北京市-东城区 观音桥',
                sendWay: '物流配送',
                status: '已完成'
              },
              {
                name: '颜衫短袖男polo衫夏季翻领衣服潮牌休闲上衣夏天翻领半袖男士t恤',
                picture: 'https://b2c-v5-yanshi.oss-cn-hangzhou.aliyuncs.com/upload/1/common/images/20220723/20220723115621165854858145027_SMALL.webp',
                price: '199',
                count: '5件',
                amount: 460,
                safeguard: '主动退款',
                buyer: '小明',
                receiver: '小花',
                tel: '15823655095',
                address: '北京市-北京市-东城区 观音桥',
                sendWay: '物流配送',
                status: '已完成'
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
  :deep(.order-table){
    margin-top: 20px;
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
