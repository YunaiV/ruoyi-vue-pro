import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';

import { h } from 'vue';

import { setupVbenVxeTable, useVbenVxeGrid } from '@vben/plugins/vxe-table';
import {
  erpCountInputFormatter,
  erpNumberFormatter,
  fenToYuan,
  formatPast2,
} from '@vben/utils';

import { NButton, NImage, NImageGroup, NSwitch, NTag } from 'naive-ui';

import { $t } from '#/locales';

import DictTag from '../components/dict-tag/dict-tag.vue';
import { useVbenForm } from './form';

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: 'center',
        border: false,
        columnConfig: {
          resizable: true,
        },
        minHeight: 180,
        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        toolbarConfig: {
          import: false, // 是否导入
          export: false, // 是否导出
          refresh: true, // 是否刷新
          print: false, // 是否打印
          zoom: true, // 是否缩放
          custom: true, // 是否自定义配置
        },
        customConfig: {
          mode: 'modal',
        },
        proxyConfig: {
          autoLoad: true,
          response: {
            result: 'list',
            total: 'total',
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        pagerConfig: {
          enabled: true,
        },
        sortConfig: {
          multiple: true,
        },
        round: true,
        showOverflow: true,
        size: 'small',
      } as VxeTableGridOptions,
    });

    // 表格配置项可以用 cellRender: { name: 'CellImage' },
    vxeUI.renderer.add('CellImage', {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        return h(NImage, { src: row[column.field] });
      },
    });

    vxeUI.renderer.add('CellImages', {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        if (column && column.field && row[column.field]) {
          return h(NImageGroup, { srcList: row[column.field] });
        }
        return '';
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add('CellLink', {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          NButton,
          { size: 'small', type: 'primary', quaternary: true },
          { default: () => props?.text },
        );
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellTag' },
    vxeUI.renderer.add('CellTag', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        return h(NTag, { color: props?.color }, () => row[column.field]);
      },
    });

    vxeUI.renderer.add('CellTags', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        if (!row[column.field] || !Array.isArray(row[column.field])) {
          return '';
        }
        return h(
          'div',
          { class: 'flex items-center justify-center' },
          {
            default: () =>
              row[column.field].map((item: any) =>
                h(NTag, { color: props?.color }, { default: () => item }),
              ),
          },
        );
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellDict', props:{dictType: ''} },
    vxeUI.renderer.add('CellDict', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        if (!props) {
          return '';
        }
        // 使用 DictTag 组件替代原来的实现
        return h(DictTag, {
          type: props.type,
          value: row[column.field]?.toString(),
        });
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellSwitch', props: { beforeChange: () => {} } },
    // add by 芋艿：from https://github.com/vbenjs/vue-vben-admin/blob/main/playground/src/adapter/vxe-table.ts#L97-L123
    vxeUI.renderer.add('CellSwitch', {
      renderTableDefault({ attrs, props }, { column, row }) {
        const loadingKey = `__loading_${column.field}`;
        const finallyProps = {
          inlinePrompt: true,
          checkedValue: 0,
          uncheckedValue: 1,
          ...props,
          value: row[column.field],
          loading: row[loadingKey] ?? false,
          'onUpdate:value': onChange,
        };

        async function onChange(newVal: any) {
          row[loadingKey] = true;
          try {
            const result = await attrs?.beforeChange?.(newVal, row);
            if (result !== false) {
              row[column.field] = newVal;
            }
          } finally {
            row[loadingKey] = false;
          }
        }

        return h(NSwitch, finallyProps, {
          checked: () => h('p', props?.checkedChildren ?? $t('common.enabled')),
          unchecked: () =>
            h('p', props?.uncheckedChildren ?? $t('common.disabled')),
        });
      },
    });

    // 这里可以自行扩展 vxe-table 的全局配置，比如自定义格式化
    // vxeUI.formats.add
    vxeUI.formats.add('formatPast2', {
      tableCellFormatMethod({ cellValue }) {
        return formatPast2(cellValue);
      },
    });

    // add by 星语：数量格式化，保留 3 位
    vxeUI.formats.add('formatAmount3', {
      tableCellFormatMethod({ cellValue }) {
        if (cellValue === null || cellValue === undefined) {
          return '';
        }
        return erpCountInputFormatter(cellValue);
      },
    });
    // add by 星语：数量格式化，保留 2 位
    vxeUI.formats.add('formatAmount2', {
      tableCellFormatMethod({ cellValue }, digits = 2) {
        return `${erpNumberFormatter(cellValue, digits)}`;
      },
    });

    vxeUI.formats.add('formatFenToYuanAmount', {
      tableCellFormatMethod({ cellValue }, digits = 2) {
        return `${erpNumberFormatter(fenToYuan(cellValue), digits)}`;
      },
    });

    vxeUI.formats.add('formatFileSize', {
      tableCellFormatMethod({ cellValue }, digits = 2) {
        if (!cellValue) return '0 B';
        const unitArr = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
        const index = Math.floor(Math.log(cellValue) / Math.log(1024));
        const size = cellValue / 1024 ** index;
        const formattedSize = size.toFixed(digits);
        return `${formattedSize} ${unitArr[index]}`;
      },
    });
  },
  useVbenForm,
});

export { useVbenVxeGrid };

export * from '#/components/table-action';
export type * from '@vben/plugins/vxe-table';
