import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

function areaReplace(areaName: string) {
  if (!areaName) {
    return areaName;
  }
  return areaName
    .replace('维吾尔自治区', '')
    .replace('壮族自治区', '')
    .replace('回族自治区', '')
    .replace('自治区', '')
    .replace('省', '');
}

export function getChartOptions(activeTabName: any, res: any): any {
  switch (activeTabName) {
    case 'area': {
      const data = res.map((item: any) => {
        return {
          ...item,
          areaName: areaReplace(item.areaName),
        };
      });
      let leftMin = 0;
      let leftMax = 0;
      let rightMin = 0;
      let rightMax = 0;
      data.forEach((item: any) => {
        leftMin = Math.min(leftMin, item.customerCount || 0);
        leftMax = Math.max(leftMax, item.customerCount || 0);
        rightMin = Math.min(rightMin, item.dealCount || 0);
        rightMax = Math.max(rightMax, item.dealCount || 0);
      });
      return {
        left: {
          title: {
            text: '全部客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
            showDelay: 0,
            transitionDuration: 0.2,
          },
          visualMap: {
            text: ['高', '低'],
            realtime: false,
            calculable: true,
            top: 'middle',
            inRange: {
              color: ['yellow', 'lightskyblue', 'orangered'],
            },
            min: leftMin,
            max: leftMax,
          },
          series: [
            {
              name: '客户地域分布',
              type: 'map',
              map: 'china',
              roam: false,
              selectedMode: false,
              data: data.map((item: any) => {
                return {
                  name: item.areaName,
                  value: item.customerCount || 0,
                };
              }),
            },
          ],
        },
        right: {
          title: {
            text: '成交客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
            showDelay: 0,
            transitionDuration: 0.2,
          },
          visualMap: {
            text: ['高', '低'],
            realtime: false,
            calculable: true,
            top: 'middle',
            inRange: {
              color: ['yellow', 'lightskyblue', 'orangered'],
            },
            min: rightMin,
            max: rightMax,
          },
          series: [
            {
              name: '客户地域分布',
              type: 'map',
              map: 'china',
              roam: false,
              selectedMode: false,
              data: data.map((item: any) => {
                return {
                  name: item.areaName,
                  value: item.dealCount || 0,
                };
              }),
            },
          ],
        },
      };
    }
    case 'industry': {
      return {
        left: {
          title: {
            text: '全部客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '全部客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '全部客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(
                    DICT_TYPE.CRM_CUSTOMER_INDUSTRY,
                    r.industryId,
                  ),
                  value: r.customerCount,
                };
              }),
            },
          ],
        },
        right: {
          title: {
            text: '成交客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '成交客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '成交客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(
                    DICT_TYPE.CRM_CUSTOMER_INDUSTRY,
                    r.industryId,
                  ),
                  value: r.dealCount,
                };
              }),
            },
          ],
        },
      };
    }
    case 'level': {
      return {
        left: {
          title: {
            text: '全部客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '全部客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '全部客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(DICT_TYPE.CRM_CUSTOMER_LEVEL, r.level),
                  value: r.customerCount,
                };
              }),
            },
          ],
        },
        right: {
          title: {
            text: '成交客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '成交客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '成交客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(DICT_TYPE.CRM_CUSTOMER_LEVEL, r.level),
                  value: r.dealCount,
                };
              }),
            },
          ],
        },
      };
    }
    case 'source': {
      return {
        left: {
          title: {
            text: '全部客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '全部客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '全部客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(DICT_TYPE.CRM_CUSTOMER_SOURCE, r.source),
                  value: r.customerCount,
                };
              }),
            },
          ],
        },
        right: {
          title: {
            text: '成交客户',
            left: 'center',
          },
          tooltip: {
            trigger: 'item',
          },
          legend: {
            orient: 'vertical',
            left: 'left',
          },
          toolbox: {
            feature: {
              saveAsImage: { show: true, name: '成交客户' }, // 保存为图片
            },
          },
          series: [
            {
              name: '成交客户',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 40,
                  fontWeight: 'bold',
                },
              },
              labelLine: {
                show: false,
              },
              data: res.map((r: any) => {
                return {
                  name: getDictLabel(DICT_TYPE.CRM_CUSTOMER_SOURCE, r.source),
                  value: r.dealCount,
                };
              }),
            },
          ],
        },
      };
    }
    default: {
      return {};
    }
  }
}
