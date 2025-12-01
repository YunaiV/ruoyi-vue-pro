/** 商品统计折线图配置 */
export function getProductSummaryChartOptions(data: any[]): any {
  // 处理数据：将金额从分转换为元
  const processedData = data.map((item) => ({
    ...item,
    orderPayPrice: Number((item.orderPayPrice / 100).toFixed(2)),
    afterSaleRefundPrice: Number((item.afterSaleRefundPrice / 100).toFixed(2)),
  }));

  return {
    dataset: {
      dimensions: [
        'time',
        'browseCount',
        'browseUserCount',
        'orderPayPrice',
        'afterSaleRefundPrice',
      ],
      source: processedData,
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
    series: [
      {
        name: '商品浏览量',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#B37FEB' },
      },
      {
        name: '商品访客数',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#FFAB2B' },
      },
      {
        name: '支付金额',
        type: 'bar',
        smooth: true,
        yAxisIndex: 1,
        itemStyle: { color: '#1890FF' },
      },
      {
        name: '退款金额',
        type: 'bar',
        smooth: true,
        yAxisIndex: 1,
        itemStyle: { color: '#00C050' },
      },
    ],
    toolbox: {
      feature: {
        // 数据区域缩放
        dataZoom: {
          yAxisIndex: false, // Y轴不缩放
        },
        brush: {
          type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
        },
        saveAsImage: {
          show: true,
          name: '商品状况',
        }, // 保存为图片
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
      boundaryGap: true,
      axisTick: {
        show: false,
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '金额',
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          color: '#7F8B9C',
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: '#F5F7F9',
          },
        },
      },
      {
        type: 'value',
        name: '数量',
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          color: '#7F8B9C',
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: '#F5F7F9',
          },
        },
      },
    ],
  };
}
