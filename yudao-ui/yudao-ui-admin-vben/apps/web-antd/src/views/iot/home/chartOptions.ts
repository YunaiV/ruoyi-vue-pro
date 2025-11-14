/**
 * 设备数量饼图配置
 */
export function getDeviceCountChartOptions(
  productCategoryDeviceCounts: Record<string, number>,
): any {
  const data = Object.entries(productCategoryDeviceCounts).map(
    ([name, value]) => ({ name, value }),
  );

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 个 ({d}%)',
    },
    legend: {
      top: '5%',
      right: '10%',
      orient: 'vertical',
    },
    series: [
      {
        name: '设备数量',
        type: 'pie',
        radius: ['50%', '80%'],
        center: ['30%', '50%'],
        data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
        label: {
          show: true,
          formatter: '{b}: {c}',
        },
      },
    ],
  };
}

/**
 * 仪表盘图表配置
 */
export function getGaugeChartOptions(
  value: number,
  max: number,
  color: string,
  title: string,
): any {
  return {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max,
        center: ['50%', '70%'],
        radius: '120%',
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
            color: [[1, '#E5E7EB']],
          },
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        pointer: { show: false },
        detail: {
          valueAnimation: true,
          fontSize: 24,
          fontWeight: 'bold',
          color,
          offsetCenter: [0, '-20%'],
          formatter: '{value}',
        },
        title: {
          show: true,
          offsetCenter: [0, '20%'],
          fontSize: 14,
          color: '#666',
        },
        data: [{ value, name: title }],
      },
    ],
  };
}
