import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

export function getChartOptions(activeTabName: any, res: any): any {
  switch (activeTabName) {
    case 'conversionStat': {
      return {
        grid: {
          left: 20,
          right: 40, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '客户转化率',
            type: 'line',
            data: res.map((item: any) => {
              return {
                name: item.time,
                value: item.customerCreateCount
                  ? (
                      (item.customerDealCount / item.customerCreateCount) *
                      100
                    ).toFixed(2)
                  : 0,
              };
            }),
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
            saveAsImage: { show: true, name: '客户转化率分析' }, // 保存为图片
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        yAxis: {
          type: 'value',
          name: '转化率(%)',
        },
        xAxis: {
          type: 'category',
          name: '日期',
          data: res.map((s: any) => s.time),
        },
      };
    }
    case 'customerSummary': {
      return {
        grid: {
          bottom: '5%',
          containLabel: true,
          left: '5%',
          right: '5%',
          top: '5 %',
        },
        legend: {},
        series: [
          {
            name: '新增客户数',
            type: 'bar',
            yAxisIndex: 0,
            data: res.map((item: any) => item.customerCreateCount),
          },
          {
            name: '成交客户数',
            type: 'bar',
            yAxisIndex: 1,
            data: res.map((item: any) => item.customerDealCount),
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
            name: '新增客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '成交客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          data: res.map((item: any) => item.time),
        },
      };
    }
    case 'dealCycleByArea': {
      const data = res.map((s: any) => {
        return {
          areaName: s.areaName,
          customerDealCycle: s.customerDealCycle,
          customerDealCount: s.customerDealCount,
        };
      });
      return {
        grid: {
          left: 20,
          right: 40, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '成交周期(天)',
            type: 'bar',
            data: data.map((s: any) => s.customerDealCycle),
            yAxisIndex: 0,
          },
          {
            name: '成交客户数',
            type: 'bar',
            data: data.map((s: any) => s.customerDealCount),
            yAxisIndex: 1,
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
            saveAsImage: { show: true, name: '成交周期分析' }, // 保存为图片
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
            name: '成交周期(天)',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '成交客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '区域',
          data: data.map((s: any) => s.areaName),
        },
      };
    }
    case 'dealCycleByProduct': {
      const data = res.map((s: any) => {
        return {
          productName: s.productName ?? '未知',
          customerDealCycle: s.customerDealCount,
          customerDealCount: s.customerDealCount,
        };
      });
      return {
        grid: {
          left: 20,
          right: 40, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '成交周期(天)',
            type: 'bar',
            data: data.map((s: any) => s.customerDealCycle),
            yAxisIndex: 0,
          },
          {
            name: '成交客户数',
            type: 'bar',
            data: data.map((s: any) => s.customerDealCount),
            yAxisIndex: 1,
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
            saveAsImage: { show: true, name: '成交周期分析' }, // 保存为图片
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
            name: '成交周期(天)',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '成交客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '产品名称',
          data: data.map((s: any) => s.productName),
        },
      };
    }
    case 'dealCycleByUser': {
      const customerDealCycleByDate = res.customerDealCycleByDate;
      const customerDealCycleByUser = res.customerDealCycleByUser;
      return {
        grid: {
          left: 20,
          right: 40, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '成交周期(天)',
            type: 'bar',
            data: customerDealCycleByDate.map((s: any) => s.customerDealCycle),
            yAxisIndex: 0,
          },
          {
            name: '成交客户数',
            type: 'bar',
            data: customerDealCycleByUser.map((s: any) => s.customerDealCount),
            yAxisIndex: 1,
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
            saveAsImage: { show: true, name: '成交周期分析' }, // 保存为图片
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
            name: '成交周期(天)',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '成交客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          data: customerDealCycleByDate.map((s: any) => s.time),
        },
      };
    }
    case 'followUpSummary': {
      return {
        grid: {
          left: 20,
          right: 30, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '跟进客户数',
            type: 'bar',
            yAxisIndex: 0,
            data: res.map((s: any) => s.followUpCustomerCount),
          },
          {
            name: '跟进次数',
            type: 'bar',
            yAxisIndex: 1,
            data: res.map((s: any) => s.followUpRecordCount),
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
            saveAsImage: { show: true, name: '客户跟进次数分析' }, // 保存为图片
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
            name: '跟进客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '跟进次数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
              },
            },
          },
        ],
        xAxis: {
          type: 'category',
          name: '日期',
          axisTick: {
            alignWithLabel: true,
          },
          data: res.map((s: any) => s.time),
        },
      };
    }
    case 'followUpType': {
      return {
        title: {
          text: '客户跟进方式分析',
          left: 'center',
        },
        legend: {
          orient: 'vertical',
          left: 'left',
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c}% ',
        },
        toolbox: {
          feature: {
            saveAsImage: { show: true, name: '客户跟进方式分析' }, // 保存为图片
          },
        },
        series: [
          {
            name: '跟进方式',
            type: 'pie',
            radius: '50%',
            data: res.map((s: any) => {
              return {
                name: getDictLabel(
                  DICT_TYPE.CRM_FOLLOW_UP_TYPE,
                  s.followUpType,
                ),
                value: s.followUpRecordCount,
              };
            }),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
              },
            },
          },
        ],
      };
    }
    case 'poolSummary': {
      return {
        grid: {
          left: 20,
          right: 40, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '进入公海客户数',
            type: 'bar',
            yAxisIndex: 0,
            data: res.map((s: any) => s.customerPutCount),
          },
          {
            name: '公海领取客户数',
            type: 'bar',
            yAxisIndex: 1,
            data: res.map((s: any) => s.customerTakeCount),
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
            saveAsImage: { show: true, name: '公海客户分析' }, // 保存为图片
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
            name: '进入公海客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '公海领取客户数',
            min: 0,
            minInterval: 1, // 显示整数刻度
            splitLine: {
              lineStyle: {
                type: 'dotted', // 右侧网格线虚化, 减少混乱
                opacity: 0.7,
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
