<template>
  <div class="app-container">
    <doc-alert title="公众号统计" url="https://doc.iocoder.cn/mp/statistics/" />

    <!-- 搜索工作栏 -->
    <el-form ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="accountId" @change="getSummary">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="date">
        <el-date-picker v-model="date" style="width: 260px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                        :picker-options="datePickerOptions" :default-time="['00:00:00', '23:59:59']"
                        @change="getSummary">
        </el-date-picker>
      </el-form-item>
    </el-form>

    <!-- 图表 -->
    <el-row>
      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>用户增减数据</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="userSummaryChart" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>累计用户数据</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="userCumulateChart" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>消息概况数据</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="upstreamMessageChart" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>接口分析数据</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="interfaceSummaryChart" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
// 引入基本模板
import * as echarts from 'echarts'
// 引入柱状图组件
require('echarts/lib/chart/bar')
// 引入柱拆线组件
require('echarts/lib/chart/line')
// 引入提示框和title组件
require('echarts/lib/component/tooltip')
require('echarts/lib/component/title')
require('echarts/lib/component/legend')

import { getInterfaceSummary, getUserSummary, getUserCumulate, getUpstreamMessage} from '@/api/mp/statistics'
import { datePickerOptions } from "@/utils/constants";
import {addTime, beginOfDay, betweenDay, endOfDay, formatDate} from "@/utils/dateUtils";
import { getSimpleAccounts } from "@/api/mp/account";

export default {
  name: 'mpStatistics',
  data() {
    return {
      date : [beginOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24 * 7)), // -7 天
        endOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24))], // -1 天
      accountId: undefined,
      accounts: [],

      xAxisDate: [], // X 轴的日期范围
      userSummaryOption: { // 用户增减数据
        color: ['#67C23A', '#e5323e'],
        legend: {
          data: ['新增用户','取消关注的用户']
        },
        tooltip: {},
        xAxis: {
          data: [] // X 轴的日期范围
        },
        yAxis: {
          minInterval: 1
        },
        series: [{
          name: '新增用户',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          barGap: 0,
          data: [] // 新增用户的数据
        }, {
          name: '取消关注的用户',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 取消关注的用户的数据
        }]
      },
      userCumulateOption: { // 累计用户数据
        legend: {
          data: ['累计用户量']
        },
        xAxis: {
          type: 'category',
          data: []
        },
        yAxis: {
          minInterval: 1
        },
        series: [{
          name:'累计用户量',
          data: [], // 累计用户量的数据
          type: 'line',
          smooth: true,
          label: {
            normal: {
              show: true
            }
          }
        }]
      },
      upstreamMessageOption: { // 消息发送概况数据
        color: ['#67C23A', '#e5323e'],
        legend: {
          data: ['用户发送人数', '用户发送条数']
        },
        tooltip: {},
        xAxis: {
          data: [] // X 轴的日期范围
        },
        yAxis: {
          minInterval: 1
        },
        series: [{
          name: '用户发送人数',
          type: 'line',
          smooth: true,
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 用户发送人数的数据
        }, {
          name: '用户发送条数',
          type: 'line',
          smooth: true,
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 用户发送条数的数据
        }]
      },
      interfaceSummaryOption: {  // 接口分析况数据
        color: ['#67C23A', '#e5323e', '#E6A23C', '#409EFF'],
        legend: {
          data: ['被动回复用户消息的次数','失败次数', '最大耗时','总耗时']
        },
        tooltip: {},
        xAxis: {
          data: [] // X 轴的日期范围
        },
        yAxis: {},
        series: [{
          name: '被动回复用户消息的次数',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          barGap: 0,
          data: [] // 被动回复用户消息的次数的数据
        }, {
          name: '失败次数',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 失败次数的数据
        }, {
          name: '最大耗时',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 最大耗时的数据
        }, {
          name: '总耗时',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          data: [] // 总耗时的数据
        }]
      },

      // 静态变量
      datePickerOptions: datePickerOptions,
    }
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.accountId = this.accounts[0].id;
      }
      // 加载数据
      this.getSummary();
    })
  },
  methods: {
    getSummary() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.accountId) {
        this.$message.error('未选中公众号，无法统计数据')
        return false
      }
      // 必须选择 7 天内，因为公众号有时间跨度限制为 7
      if (betweenDay(this.date[0], this.date[1]) >= 7) {
        this.$message.error('时间间隔 7 天以内，请重新选择')
        return false
      }
      this.xAxisDate = []
      const days = betweenDay(this.date[0], this.date[1]) // 相差天数
      for(let i = 0; i <= days; i++){
        this.xAxisDate.push(formatDate(addTime(this.date[0], 3600 * 1000 * 24 * i), 'yyyy-MM-dd'));
      }

      // 初始化图表
      this.initUserSummaryChart();
      this.initUserCumulateChart();
      this.initUpstreamMessageChart();
      this.interfaceSummaryChart();
    },
    initUserSummaryChart() {
      this.userSummaryOption.xAxis.data = [];
      this.userSummaryOption.series[0].data = [];
      this.userSummaryOption.series[1].data = [];
      getUserSummary({
        accountId: this.accountId,
        date: [formatDate(this.date[0], 'yyyy-MM-dd HH:mm:ss'), formatDate(this.date[1], 'yyyy-MM-dd HH:mm:ss'),]
      }).then(response => {
        this.userSummaryOption.xAxis.data = this.xAxisDate;
        // 处理数据
        this.xAxisDate.forEach((date, index) => {
          response.data.forEach((item) => {
            // 匹配日期
            const refDate = formatDate(new Date(item.refDate), 'yyyy-MM-dd');
            if (refDate.indexOf(date) === -1) {
              return;
            }
            // 设置数据到对应的位置
            this.userSummaryOption.series[0].data[index] = item.newUser;
            this.userSummaryOption.series[1].data[index] = item.cancelUser;
          })
        })
        // 绘制图表
        const userSummaryChart = echarts.init(this.$refs.userSummaryChart);
        userSummaryChart.setOption(this.userSummaryOption)
      }).catch(() => {})
    },
    initUserCumulateChart() {
      this.userCumulateOption.xAxis.data = [];
      this.userCumulateOption.series[0].data = [];
      // 发起请求
      getUserCumulate({
        accountId: this.accountId,
        date: [formatDate(this.date[0], 'yyyy-MM-dd HH:mm:ss'), formatDate(this.date[1], 'yyyy-MM-dd HH:mm:ss'),]
      }).then(response => {
        this.userCumulateOption.xAxis.data = this.xAxisDate;
        // 处理数据
        response.data.forEach((item, index) => {
          this.userCumulateOption.series[0].data[index] = item.cumulateUser;
        })
        // 绘制图表
        const userCumulateChart = echarts.init(this.$refs.userCumulateChart);
        userCumulateChart.setOption(this.userCumulateOption)
      }).catch(() => {})
    },
    initUpstreamMessageChart() {
      this.upstreamMessageOption.xAxis.data = [];
      this.upstreamMessageOption.series[0].data = [];
      this.upstreamMessageOption.series[1].data = [];
      // 发起请求
      getUpstreamMessage({
        accountId: this.accountId,
        date: [formatDate(this.date[0], 'yyyy-MM-dd HH:mm:ss'), formatDate(this.date[1], 'yyyy-MM-dd HH:mm:ss'),]
      }).then(response => {
        this.upstreamMessageOption.xAxis.data = this.xAxisDate;
        // 处理数据
        response.data.forEach((item, index) => {
          this.upstreamMessageOption.series[0].data[index] = item.messageUser;
          this.upstreamMessageOption.series[1].data[index] = item.messageCount;
        })
        // 绘制图表
        const upstreamMessageChart = echarts.init(this.$refs.upstreamMessageChart);
        upstreamMessageChart.setOption(this.upstreamMessageOption);
      }).catch(() => {})
    },
    interfaceSummaryChart() {
      this.interfaceSummaryOption.xAxis.data = [];
      this.interfaceSummaryOption.series[0].data = [];
      this.interfaceSummaryOption.series[1].data = [];
      this.interfaceSummaryOption.series[2].data = [];
      this.interfaceSummaryOption.series[3].data = [];
      // 发起请求
      getInterfaceSummary({
        accountId: this.accountId,
        date: [formatDate(this.date[0], 'yyyy-MM-dd HH:mm:ss'), formatDate(this.date[1], 'yyyy-MM-dd HH:mm:ss'),]
      }).then(response => {
        this.interfaceSummaryOption.xAxis.data = this.xAxisDate;
        // 处理数据
        response.data.forEach((item, index) => {
          this.interfaceSummaryOption.series[0].data[index] = item.callbackCount;
          this.interfaceSummaryOption.series[1].data[index] = item.failCount;
          this.interfaceSummaryOption.series[2].data[index] = item.maxTimeCost;
          this.interfaceSummaryOption.series[3].data[index] = item.totalTimeCost;
        })
        // 绘制图表
        const interfaceSummaryChart = echarts.init(this.$refs.interfaceSummaryChart);
        interfaceSummaryChart.setOption(this.interfaceSummaryOption);
      }).catch(() => {})
    }
  }
}
</script>
