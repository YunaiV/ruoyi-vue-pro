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
      <el-row>
        <el-col :span="12">
          <div id="userCumulateChart" :style="{width: '80%', height: '500px'}"></div>
        </el-col>
        <el-col :span="12">
          <div id="interfaceSummaryChart" :style="{width: '80%', height: '500px'}"></div>
        </el-col>
        <el-col :span="12">
          <div id="interfaceSummaryChart2" :style="{width: '80%', height: '500px'}"></div>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12" class="card-box">
          <el-card>
            <div slot="header">
              <span>用户增减数据</span>
            </div>
            <div class="el-table el-table--enable-row-hover el-table--medium">
              <div ref="userSummary" style="height: 420px" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12" class="card-box">
          <el-card>
            <div slot="header">
              <span>内存信息</span>
            </div>
            <div class="el-table el-table--enable-row-hover el-table--medium">
              <div ref="usedmemory" style="height: 420px" />
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

import { getInterfaceSummary, getUserSummary, getUserCumulate } from '@/api/mp/statistics'
import { datePickerOptions } from "@/utils/constants";
import {beginOfDay, betweenDay, endOfDay, formatDate} from "@/utils/dateUtils";

export default {
  name: 'mpStatistics',
  data() {
    return {
      date : [beginOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24 * 7)), // -7 天
        endOfDay(new Date(new Date().getTime() - 3600 * 1000 * 24))], // -1 天

      xAxisDate: [], // X 轴的日期范围
      seriesData1: [],
      seriesData2: [],
      seriesData3: [],
      seriesData4: [],
      seriesData5: [],
      seriesData6: [],
      seriesData7: [],

      userSummaryOption: { // 用户增减数据
        color: ['#67C23A', '#e5323e'],
        legend: {
          data: ['新增用户','取消关注的用户']
        },
        tooltip: {},
        xAxis: {
          data: this.xAxisDate
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
          data: []
        }, {
          name: '取消关注的用户',
          type: 'bar',
          label: {
            normal: {
              show: true
            }
          },
          data: []
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



      this.seriesData1 = []
      this.seriesData2 = []
      this.seriesData5 = []
      this.seriesData6 = []

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
        this.xAxisDate.push(formatDate(new Date(this.date[0].getTime() + 3600 * 1000 * 24 * i), 'yyyy-MM-dd'));
        this.seriesData2.push(0)
        this.seriesData5.push(0)
        this.seriesData6.push(0)
      }

      // 用户增减数据
      this.userSummaryOption.xAxis.data = [];
      this.userSummaryOption.series[0].data = [];
      this.userSummaryOption.series[1].data = [];
      getUserSummary({
        id: 1,
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
        let userSummaryChart = echarts.init(this.$refs.userSummary);
        userSummaryChart.setOption(this.userSummaryOption)
      })
          // .catch(() => {})

      // getUserCumulate({
      //   startDate: this.startDate,
      //   endDate: this.endDate
      // }).then(response => {
      //   response.data.forEach((item, index, arr) => {
      //     this.$set(this.seriesData7, index, item.cumulateUser)
      //   })
      //   // 基于准备好的dom，初始化echarts实例
      //   let userCumulateChart = echarts.init(document.getElementById('userCumulateChart'))
      //   // 绘制图表
      //   userCumulateChart.setOption({
      //     title: { text: '累计用户数据' },
      //     legend: {
      //       data: ['累计用户量']
      //     },
      //     xAxis: {
      //       type: 'category',
      //       data: this.xAxisData
      //     },
      //     yAxis: {
      //       type: 'value'
      //     },
      //     series: [{
      //       name:'累计用户量',
      //       data: this.seriesData7,
      //       type: 'line',
      //       smooth: true,
      //       label: {
      //         normal: {
      //           show: true
      //         }
      //       }
      //     }]
      //   })
      // }).catch(() => {
      // })
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
