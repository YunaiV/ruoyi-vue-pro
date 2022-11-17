<template>
  <div class="app-container order-detail-page">
    <template v-for="(group, index) in detailGroups">
      <el-descriptions v-bind="group.groupProps" :key="`group_${index}`" :title="group.title">
        <!-- 商品信息 -->
        <el-descriptions-item  v-if="group.key === 'goodsInfo'" labelClassName="no-colon">
          <el-table border>
            <el-table-column prop="date" label="商品" width="180"/>
            <el-table-column prop="jg" label="价格"/>
            <el-table-column prop="spbm" label="商品编码"/>
            <el-table-column prop="xl" label="数量"/>
            <el-table-column prop="xj" label="小计（元）"/>
            <el-table-column prop="tkzt" label="退款状态"/>
            <el-table-column prop="zt" label="状态"/>
          </el-table>
        </el-descriptions-item>

        <!-- 订单操作日志 -->
        <el-descriptions-item  v-if="group.key === 'orderLog'" labelClassName="no-colon">
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in detailInfo[group.key]"
              :key="index"
              :timestamp="activity.timestamp"
            >
              {{activity.content}}
            </el-timeline-item>
          </el-timeline>
        </el-descriptions-item>

        <!-- 物流信息 -->
        <el-descriptions-item v-if="group.key === 'expressInfo'" labelClassName="no-colon">
          <el-tabs type="card">
            <!-- 循环包裹物流信息 -->
            <el-tab-pane v-for="(pkgInfo, pInIdx) in detailInfo[group.key]" :key="`pkgInfo_${pInIdx}`" :label="pkgInfo.label">
              <!-- 包裹详情 -->
              <el-descriptions>
                <el-descriptions-item v-for="(pkgChild, pkgCIdx) in group.children" v-bind="pkgChild.childProps" :key="`pkgChild_${pkgCIdx}`" :label="pkgChild.label">
                  <!-- 包裹商品列表 -->
                  <template v-if="pkgChild.valueKey === 'goodsList' && pkgInfo[pkgChild.valueKey]">
                    <div v-for="(goodInfo, goodInfoIdx) in pkgInfo[pkgChild.valueKey]" :key="`goodInfo_${goodInfoIdx}`" style="display: flex;">
                      <el-image
                        style="width: 100px;height: 100px;flex: none"
                        :src="goodInfo.imgUrl">
                      </el-image>
                      <el-descriptions :column="1">
                        <el-descriptions-item labelClassName="no-colon">{{goodInfo.name}}</el-descriptions-item>
                        <el-descriptions-item label="数量">{{goodInfo.count}}</el-descriptions-item>
                      </el-descriptions>
                    </div>
                  </template>

                  <!-- 包裹物流详情 -->
                  <el-timeline v-else-if="pkgChild.valueKey==='wlxq'">
                    <el-timeline-item
                      v-for="(activity, index) in pkgInfo[pkgChild.valueKey]"
                      :key="index"
                      :timestamp="activity.timestamp"
                    >
                      {{activity.content}}
                    </el-timeline-item>
                  </el-timeline>

                  <template v-else>
                    {{pkgInfo[pkgChild.valueKey]}}
                  </template>
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>
        </el-descriptions-item>

        <!--订单详情、订单状态-->
        <el-descriptions-item v-else v-for="(child, cIdx) in group.children" v-bind="child.childProps" :key="`child_${cIdx}`" :label="child.label">

          <!-- 操作按钮(订单状态)-->
          <template v-if="group.key === 'orderStatus' && child.valueKey === 'actions'" slot="label">
            <el-button type="primary">备注</el-button>
            <el-button type="primary">打印发货单</el-button>
          </template>

          <!-- 内容 -->
          <template v-else>
            {{detailInfo[child.valueKey]}}
            <!--复制地址(订单详情) -->
            <el-link v-if="child.valueKey==='shdz'" v-clipboard:copy="detailInfo[child.valueKey]" v-clipboard:success="clipboardSuccess" icon="el-icon-document-copy" type="primary"/>
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </div>
</template>

<script>
export default {
  name: "detail",
  data () {
    return {
      detailGroups: [
        {
          title: '订单详情',
          children: [
            { label: '交易流水号', valueKey: 'jylsh'},
            { label: '配送方式', valueKey: 'psfs'},
            { label: '营销活动', valueKey: 'yxhd'},
            { label: '订单编号', valueKey: 'ddbh'},
            { label: '收货人', valueKey: 'shr'},
            { label: '买家留言', valueKey: 'mjly'},
            { label: '订单类型', valueKey: 'ddlx'},
            { label: '联系电话', valueKey: 'lxdh'},
            { label: '备注', valueKey: 'bz'},
            { label: '订单来源', valueKey: 'ddly'},
            { label: '付款方式', valueKey: 'fkfs'},
            { label: '买家', valueKey: 'mj'},
            { label: '收货地址', valueKey: 'shdz'}
          ]
        },
        {
          title: '订单状态',
          key: 'orderStatus',
          groupProps: {
            column: 1
          },
          children: [
            { label: '订单状态', valueKey: 'ddzt', childProps: { contentStyle: { color: 'red' }}},
            { label: '', valueKey: 'actions', childProps: { labelClassName: 'no-colon'}},
            { label: '提醒', valueKey: 'tx', childProps: { labelStyle: { color: 'red' }}}
          ]
        },
        {
          title: '物流信息',
          key: 'expressInfo',
          children: [
            { label: '发货时间', valueKey: 'fhsj'},
            { label: '物流公司', valueKey: 'wlgs'},
            { label: '运单号', valueKey: 'ydh'},
            { label: '商品信息', valueKey: 'goodsList', childProps: { span: 3 }},
            { label: '物流状态', valueKey: 'wlzt', childProps: { span: 3 }},
            { label: '物流详情', valueKey: 'wlxq'}
          ]
        },
        {
          title: '商品信息',
          key: 'goodsInfo'
        },
        {
          title: '订单操作日志',
          key: 'orderLog'
        }
      ],
      detailInfo: {
        jylsh: '16674653573152181000',
        psfs: '物流配送',
        yxhd: '',
        ddbh: '20221103164918001',
        shr: '',
        mjly: '',
        ddlx: '',
        lxdh: '',
        bz: '',
        ddly: '',
        shdz: '陕西省-西安市-莲湖区-九座花园西区(莲湖区二环南路西段202)陕西省-西安市-莲湖区-九座花园西区(莲湖区二环南路西段202)',
        fkfs: '',
        mj: '',
        ddzt: '已完成',
        tx: '买家付款成功后，货款将直接进入您的商户号（微信、支付宝）请及时关注你发出的包裹状态，确保可以配送至买家手中如果买家表示没收到货或货物有问题，请及时联系买家处理，友好协商',
        expressInfo: [ // 物流信息
          {
            label: '包裹1',
            name: 'bg1',
            fhsj: '2022-11-03 16:50:45',
            wlgs: '极兔',
            ydh: '2132123',
            wlzt: '不支持此快递公司',
            wlxq:  [
              {
                content: '正在派送途中,请您准备签收(派件人:王涛,电话:13854563814)',
                timestamp: '2018-04-15 15:00:16'
              },
              {
                content: '快件到达 【烟台龙口东江村委营业点】',
                timestamp: '2018-04-13 14:54:19'
              },
              {
                content: '快件已发车',
                timestamp: '2018-04-11 12:55:52'
              },
              {
                content: '快件已发车',
                timestamp: '2018-04-11 12:55:52'
              },
              {
                content: '快件已发车',
                timestamp: '2018-04-11 12:55:52'
              }
            ],
            goodsList: [ // 包裹下的商品列表
              {
                name: 'Otic 巴拉啦小魔仙联名麦克风儿童早教家用一体卡拉OK宝宝话筒唱歌 魔仙粉',
                count: 6,
                imgUrl: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
              }
            ]
          },
          {
            label: '包裹2',
            name: 'bg1',
            fhsj: '',
            wlgs: '',
            ydh: '',
            wlzt: '',
            goodsInfo: {}
          },
          {
            label: '包裹3',
            name: 'bg1',
            fhsj: '',
            wlgs: '',
            ydh: '',
            wlzt: '',
            goodsInfo: {}
          }
        ],
        orderLog: [ // 订单操作日志
          {
            content: '买家【乌鸦】关闭了订单',
            timestamp: '2018-04-15 15:00:16'
          },
          {
            content: '买家【乌鸦】下单了',
            timestamp: '2018-04-15 15:00:16'
          }
        ],
        goodsInfo: [] // 商品详情tableData
      }
    }
  },
  methods: {
    clipboardSuccess() {
      this.$modal.msgSuccess("复制成功");
    }
  }
}
</script>

<style lang="scss" scoped>
  :deep(.el-descriptions){
    &:not(:nth-child(1)) {
      margin-top: 20px;
    }
    .el-descriptions__title{
      display: flex;
      align-items: center;
      &::before{
        content: '';
        display: inline-block;
        margin-right: 10px;
        width: 3px;
        height: 20px;
        background-color: #409EFF;
      }
    }
    .el-descriptions-item__container{
      margin: 0 10px;
      .no-colon{
        margin: 0;
        &::after{
          content: ''
        }
      }
    }
  }
</style>
