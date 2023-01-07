<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="demonstration">时间范围</span>
        <el-date-picker v-model="date" style="width: 260px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                        :picker-options="datePickerOptions" :default-time="['00:00:00', '23:59:59']"
                        @change="changeDate"  >
        </el-date-picker>
      </div>
<!--      <el-row>-->
<!--        <el-col :span="12">-->
<!--          <div id="interfaceSummaryChart" :style="{width: '80%', height: '500px'}"></div>-->
<!--        </el-col>-->
<!--        <el-col :span="12">-->
<!--          <div id="interfaceSummaryChart2" :style="{width: '80%', height: '500px'}"></div>-->
<!--        </el-col>-->
<!--      </el-row>-->

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
      </el-row>
    </el-card>
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

import {getInterfaceSummary, getUserSummary, getUserCumulate, getUpstreamMessage} from '@/api/mp/statistics'
import { datePickerOptions } from "@/utils/constants";
import {addTime, beginOfDay, betweenDay, endOfDay, formatDate} from "@/utils/dateUtils";

export default {
  name: 'mpStatistics',
  data() {
    return {
      date : [beginOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24 * 7)), // -7 天
        endOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24))], // -1 天
      accountId: 1,

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

      // 静态变量
      datePickerOptions: datePickerOptions,
    }
  },
  created() {

  },
  mounted: function() {
    this.getSummary()
  },
  computed: {

  },
  methods: {
    changeDate() {
      this.getSummary()
    },
    getSummary() {
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
      //
      // //获取接口数据
      // getInterfaceSummary({
      //   startDate: this.startDate,
      //   endDate: this.endDate
      // }).then(response => {
      //   response.data.forEach((item, index, arr) => {
      //     this.$set(this.seriesData1, index, item.callbackCount)
      //     this.$set(this.seriesData2, index, item.maxTimeCost)
      //     this.$set(this.seriesData3, index, item.totalTimeCost)
      //     this.$set(this.seriesData4, index, item.failCount)
      //   })
      //   // 基于准备好的dom，初始化echarts实例
      //   let interfaceSummaryChart = echarts.init(document.getElementById('interfaceSummaryChart'))
      //   // 绘制图表
      //   interfaceSummaryChart.setOption({
      //     title: { text: '接口分析数据' },
      //     color: ['#67C23A', '#e5323e'],
      //     legend: {
      //       data: ['被动回复用户消息的次数','失败次数']
      //     },
      //     tooltip: {},
      //     xAxis: {
      //       data: this.xAxisData
      //     },
      //     yAxis: {},
      //     series: [{
      //       name: '被动回复用户消息的次数',
      //       type: 'bar',
      //       label: {
      //         normal: {
      //           show: true
      //         }
      //       },
      //       barGap: 0,
      //       data: this.seriesData1
      //     },
      //       {
      //         name: '失败次数',
      //         type: 'bar',
      //         label: {
      //           normal: {
      //             show: true
      //           }
      //         },
      //         data: this.seriesData4
      //       }]
      //   })
      //
      //   // 基于准备好的dom，初始化echarts实例
      //   let interfaceSummaryChart2 = echarts.init(document.getElementById('interfaceSummaryChart2'))
      //   // 绘制图表
      //   interfaceSummaryChart2.setOption({
      //     title: { text: '接口分析数据' },
      //     color: ['#E6A23C', '#409EFF'],
      //     legend: {
      //       data: ['最大耗时','总耗时']
      //     },
      //     tooltip: {},
      //     xAxis: {
      //       data: this.xAxisData
      //     },
      //     yAxis: {},
      //     series: [
      //       {
      //         name: '最大耗时',
      //         type: 'bar',
      //         label: {
      //           normal: {
      //             show: true
      //           }
      //         },
      //         data: this.seriesData2
      //       },
      //       {
      //         name: '总耗时',
      //         type: 'bar',
      //         label: {
      //           normal: {
      //             show: true
      //           }
      //         },
      //         data: this.seriesData3
      //       }]
      //   })
      // }).catch(() => {
      // })
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
    }
  }
}
</script>

<style lang="scss" scoped>
.demonstration{
  font-size: 15px;
  margin-right: 10px;
}
</style>
