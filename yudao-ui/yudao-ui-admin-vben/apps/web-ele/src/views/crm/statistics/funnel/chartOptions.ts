import { erpCalculatePercentage } from '@vben/utils';

export function getChartOptions(
  activeTabName: any,
  active: boolean,
  res: any,
): any {
  switch (activeTabName) {
    case 'businessInversionRateSummary': {
      return {
        color: ['#6ca2ff', '#6ac9d7', '#ff7474'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow', // 默认为直线，可选为：'line' | 'shadow'
          },
        },
        legend: {
          data: ['赢单转化率', '商机总数', '赢单商机数'],
          bottom: '0px',
          itemWidth: 14,
        },
        grid: {
          top: '40px',
          left: '40px',
          right: '40px',
          bottom: '40px',
          containLabel: true,
          borderColor: '#fff',
        },
        xAxis: [
          {
            type: 'category',
            data: res.map((s: any) => s.time),
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 },
            },
            axisLabel: {
              color: '#BDBDBD',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' },
            },
            splitLine: {
              show: false,
            },
          },
        ],
        yAxis: [
          {
            type: 'value',
            name: '赢单转化率',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 },
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}%',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' },
            },
            splitLine: {
              show: false,
            },
          },
          {
            type: 'value',
            name: '商机数',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 },
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}个',
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' },
            },
            splitLine: {
              show: false,
            },
          },
        ],
        series: [
          {
            name: '赢单转化率',
            type: 'line',
            yAxisIndex: 0,
            data: res.map((s: any) =>
              erpCalculatePercentage(s.businessWinCount, s.businessCount),
            ),
          },
          {
            name: '商机总数',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 15,
            data: res.map((s: any) => s.businessCount),
          },
          {
            name: '赢单商机数',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 15,
            data: res.map((s: any) => s.businessWinCount),
          },
        ],
      };
    }
    case 'businessSummary': {
      return {
        grid: {
          left: 30,
          right: 30, // 让 X 轴右侧显示完整
          bottom: 20,
          containLabel: true,
        },
        legend: {},
        series: [
          {
            name: '新增商机数量',
            type: 'bar',
            yAxisIndex: 0,
            data: res.map((s: any) => s.businessCreateCount),
          },
          {
            name: '新增商机金额',
            type: 'bar',
            yAxisIndex: 1,
            data: res.map((s: any) => s.totalPrice),
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
            saveAsImage: { show: true, name: '新增商机分析' }, // 保存为图片
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
            name: '新增商机数量',
            min: 0,
            minInterval: 1, // 显示整数刻度
          },
          {
            type: 'value',
            name: '新增商机金额',
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
    case 'funnel': {
      // tips：写死 value 值是为了保持漏斗顺序不变
      const list: { name: string; value: number }[] = [];
      if (active) {
        list.push(
          { value: 60, name: `客户-${res.customerCount || 0}个` },
          { value: 40, name: `商机-${res.businessCount || 0}个` },
          { value: 20, name: `赢单-${res.businessWinCount || 0}个` },
        );
      } else {
        list.push(
          {
            value: res.customerCount || 0,
            name: `客户-${res.customerCount || 0}个`,
          },
          {
            value: res.businessCount || 0,
            name: `商机-${res.businessCount || 0}个`,
          },
          {
            value: res.businessWinCount || 0,
            name: `赢单-${res.businessWinCount || 0}个`,
          },
        );
      }
      return {
        title: {
          text: '销售漏斗',
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}',
        },
        toolbox: {
          feature: {
            dataView: { readOnly: false },
            restore: {},
            saveAsImage: {},
          },
        },
        legend: {
          data: ['客户', '商机', '赢单'],
        },
        series: [
          {
            name: '销售漏斗',
            type: 'funnel',
            left: '10%',
            top: 60,
            bottom: 60,
            width: '80%',
            min: 0,
            max: 100,
            minSize: '0%',
            maxSize: '100%',
            sort: 'descending',
            gap: 2,
            label: {
              show: true,
              position: 'inside',
            },
            labelLine: {
              length: 10,
              lineStyle: {
                width: 1,
                type: 'solid',
              },
            },
            itemStyle: {
              borderColor: '#fff',
              borderWidth: 1,
            },
            emphasis: {
              label: {
                fontSize: 20,
              },
            },
            data: list,
          },
        ],
      };
    }
    default: {
      return {};
    }
  }
}
