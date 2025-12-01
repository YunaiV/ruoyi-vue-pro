import dayjs from 'dayjs';

/** 时间范围类型枚举 */
export enum TimeRangeTypeEnum {
  DAY30 = 1,
  MONTH = 30,
  WEEK = 7,
  YEAR = 365,
}

/** 会员统计图表配置 */
export function getMemberStatisticsChartOptions(list: any[]): any {
  return {
    dataset: {
      dimensions: ['date', 'count'],
      source: list,
    },
    grid: {
      left: 20,
      right: 20,
      bottom: 20,
      top: 80,
      containLabel: true,
    },
    legend: {
      top: 50,
    },
    series: [{ name: '注册量', type: 'line', smooth: true, areaStyle: {} }],
    toolbox: {
      feature: {
        // 数据区域缩放
        dataZoom: {
          yAxisIndex: false, // Y轴不缩放
        },
        brush: {
          type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
        },
        saveAsImage: { show: true, name: '会员统计' }, // 保存为图片
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
      },
      padding: [5, 10],
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisTick: {
        show: false,
      },
      axisLabel: {
        formatter: (date: string) => dayjs(date).format('MM-DD'),
      },
    },
    yAxis: {
      axisTick: {
        show: false,
      },
    },
  };
}
