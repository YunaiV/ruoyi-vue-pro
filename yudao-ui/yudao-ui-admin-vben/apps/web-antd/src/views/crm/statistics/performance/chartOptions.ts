export function getChartOptions(activeTabName: any, res: any): any {
  switch (activeTabName) {
    case 'ContractCountPerformance': {
      return {
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '当月合同数量（个）',
            type: 'line',
            data: res.map((s: any) => s.currentMonthCount),
          },
          {
            name: '上月合同数量（个）',
            type: 'line',
            data: res.map((s: any) => s.lastMonthCount),
          },
          {
            name: '去年同月合同数量（个）',
            type: 'line',
            data: res.map((s: any) => s.lastYearCount),
          },
          {
            name: '环比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastMonthCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastMonthCount) /
                      s.lastMonthCount) *
                    100
                  ).toFixed(2),
            ),
          },
          {
            name: '同比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastYearCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastYearCount) /
                      s.lastYearCount) *
                    100
                  ).toFixed(2),
            ),
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              xAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '客户总量分析' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '数量（个）',
            axisTick: {
              show: false,
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
          {
            type: 'value',
            name: '',
            axisTick: {
              alignWithLabel: true,
              lineStyle: {
                width: 0,
              },
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}%',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          data: res.map((s: any) => s.time),
        },
      };
    }
    case 'ContractPricePerformance': {
      return {
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '当月合同金额（元）',
            type: 'line',
            data: res.map((s: any) => s.currentMonthCount),
          },
          {
            name: '上月合同金额（元）',
            type: 'line',
            data: res.map((s: any) => s.lastMonthCount),
          },
          {
            name: '去年同月合同金额（元）',
            type: 'line',
            data: res.map((s: any) => s.lastYearCount),
          },
          {
            name: '环比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastMonthCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastMonthCount) /
                      s.lastMonthCount) *
                    100
                  ).toFixed(2),
            ),
          },
          {
            name: '同比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastYearCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastYearCount) /
                      s.lastYearCount) *
                    100
                  ).toFixed(2),
            ),
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              xAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '客户总量分析' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '金额（元）',
            axisTick: {
              show: false,
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
          {
            type: 'value',
            name: '',
            axisTick: {
              alignWithLabel: true,
              lineStyle: {
                width: 0,
              },
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}%',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          data: res.map((s: any) => s.time),
        },
      };
    }
    case 'ReceivablePricePerformance': {
      return {
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '当月回款金额（元）',
            type: 'line',
            data: res.map((s: any) => s.currentMonthCount),
          },
          {
            name: '上月回款金额（元）',
            type: 'line',
            data: res.map((s: any) => s.lastMonthCount),
          },
          {
            name: '去年同月回款金额（元）',
            type: 'line',
            data: res.map((s: any) => s.lastYearCount),
          },
          {
            name: '环比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastMonthCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastMonthCount) /
                      s.lastMonthCount) *
                    100
                  ).toFixed(2),
            ),
          },
          {
            name: '同比增长率（%）',
            type: 'line',
            yAxisIndex: 1,
            data: res.map((s: any) =>
              s.lastYearCount === 0
                ? 'NULL'
                : (
                    ((s.currentMonthCount - s.lastYearCount) /
                      s.lastYearCount) *
                    100
                  ).toFixed(2),
            ),
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              xAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '客户总量分析' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '金额（元）',
            axisTick: {
              show: false,
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
          {
            type: 'value',
            name: '',
            axisTick: {
              alignWithLabel: true,
              lineStyle: {
                width: 0,
              },
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}%',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: {
                color: '#BDBDBD',
              },
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: '#e6e6e6',
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          data: res.map((s: any) => s.time),
        },
      };
    }
    default: {
      return {};
    }
  }
}
