import { cloneDeep } from '@vben/utils';

export function getChartOptions(activeTabName: any, res: any): any {
  switch (activeTabName) {
    case 'contactCountRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '新增联系人数排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '新增联系人数排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '新增联系人数（个）',
        },
        yAxis: {
          type: 'category',
          name: '创建人',
        },
      };
    }
    case 'contractCountRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '签约合同排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '签约合同排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '签约合同数（个）',
        },
        yAxis: {
          type: 'category',
          name: '签订人',
        },
      };
    }
    case 'contractPriceRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '合同金额排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '合同金额排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '合同金额（元）',
        },
        yAxis: {
          type: 'category',
          name: '签订人',
        },
      };
    }
    case 'customerCountRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '新增客户数排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '新增客户数排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '新增客户数（个）',
        },
        yAxis: {
          type: 'category',
          name: '创建人',
        },
      };
    }
    case 'followCountRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '跟进次数排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '跟进次数排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '跟进次数（次）',
        },
        yAxis: {
          type: 'category',
          name: '员工',
        },
      };
    }
    case 'followCustomerCountRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '跟进客户数排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '跟进客户数排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '跟进客户数（个）',
        },
        yAxis: {
          type: 'category',
          name: '员工',
        },
      };
    }
    case 'productSalesRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '产品销量排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '产品销量排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '产品销量',
        },
        yAxis: {
          type: 'category',
          name: '员工',
        },
      };
    }
    case 'receivablePriceRank': {
      return {
        dataset: {
          dimensions: ['nickname', 'count'],
          source: cloneDeep(res).reverse(),
        },
        grid: {
          left: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          top: 50,
        },
        series: [
          {
            name: '回款金额排行',
            type: 'bar',
          },
        ],
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false, // 数据区域缩放：Y 轴不缩放
            },
            brush: {
              type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
            },
            saveAsImage: { show: true, name: '回款金额排行' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        xAxis: {
          type: 'value',
          name: '回款金额（元）',
        },
        yAxis: {
          type: 'category',
          name: '签订人',
          nameGap: 30,
        },
      };
    }
    default: {
      return {};
    }
  }
}
