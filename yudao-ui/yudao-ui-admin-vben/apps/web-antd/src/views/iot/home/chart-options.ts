/** 消息趋势图表配置 */
export function getMessageTrendChartOptions(
  times: string[],
  upstreamData: number[],
  downstreamData: number[],
): any {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['上行消息', '下行消息'],
      top: '5%',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true,
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: times,
      },
    ],
    yAxis: [
      {
        type: 'value',
        name: '消息数量',
      },
    ],
    series: [
      {
        name: '上行消息',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
        },
        emphasis: {
          focus: 'series',
        },
        data: upstreamData,
        itemStyle: {
          color: '#1890ff',
        },
      },
      {
        name: '下行消息',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
        },
        emphasis: {
          focus: 'series',
        },
        data: downstreamData,
        itemStyle: {
          color: '#52c41a',
        },
      },
    ],
  };
}

/**
 * 设备状态仪表盘图表配置
 */
export function getDeviceStateGaugeChartOptions(
  value: number,
  max: number,
  color: string,
  title: string,
): any {
  return {
    series: [
      {
        type: 'gauge',
        startAngle: 225,
        endAngle: -45,
        min: 0,
        max,
        center: ['50%', '50%'],
        radius: '80%',
        progress: {
          show: true,
          width: 12,
          itemStyle: {
            color,
          },
        },
        axisLine: {
          lineStyle: {
            width: 12,
            color: [[1, '#E5E7EB']] as [number, string][],
          },
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        pointer: { show: false },
        title: {
          show: true,
          offsetCenter: [0, '80%'],
          fontSize: 14,
          color: '#666',
        },
        detail: {
          valueAnimation: true,
          fontSize: 32,
          fontWeight: 'bold',
          color,
          offsetCenter: [0, '10%'],
          formatter: (val: number) => `${val} 个`,
        },
        data: [{ value, name: title }],
      },
    ],
  };
}

/**
 * 设备数量饼图配置
 */
export function getDeviceCountPieChartOptions(
  data: Array<{ name: string; value: number }>,
): any {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 个 ({d}%)',
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: '10px',
      left: 'center',
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12,
      textStyle: {
        fontSize: 12,
      },
      pageButtonPosition: 'end',
      pageIconSize: 12,
      pageTextStyle: {
        fontSize: 12,
      },
      pageFormatter: '{current}/{total}',
    },
    series: [
      {
        name: '设备数量',
        type: 'pie',
        radius: ['35%', '55%'],
        center: ['50%', '40%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
        labelLine: {
          show: false,
        },
        data,
      },
    ],
  };
}
