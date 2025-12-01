import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';

import { h } from 'vue';

import {
  AsyncVxeColumn,
  AsyncVxeTable,
  createRequiredValidation,
  setupVbenVxeTable,
  useVbenVxeGrid,
} from '@vben/plugins/vxe-table';
import {
  erpCountInputFormatter,
  erpNumberFormatter,
  fenToYuan,
  formatFileSize,
  formatPast2,
} from '@vben/utils';

import { Button, Image, ImageViewer, Switch, Tag } from 'tdesign-vue-next';

import { DictTag } from '#/components/dict-tag';
import { $t } from '#/locales';

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
        return h(Image, { src: row[column.field] });
      },
    });

    vxeUI.renderer.add('CellImages', {
      renderTableDefault(_renderOpts, params) {
        const { column, row } = params;
        if (column && column.field && row[column.field]) {
          return h(ImageViewer, {}, () => {
            return row[column.field].map((item: any) =>
              h(Image, { src: item }),
            );
          });
        }
        return '';
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add('CellLink', {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          Button,
          { size: 'small', variant: 'text' },
          { default: () => props?.text },
        );
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellTag' },
    vxeUI.renderer.add('CellTag', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        return h(Tag, { color: props?.color }, () => row[column.field]);
      },
    });

    vxeUI.renderer.add('CellTags', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        if (!row[column.field] || row[column.field].length === 0) {
          return '';
        }
        return h(
          'div',
          { class: 'flex items-center justify-center' },
          {
            default: () =>
              row[column.field].map((item: any) =>
                h(Tag, { color: props?.color }, { default: () => item }),
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
          checkedChildren: $t('common.enabled'),
          checkedValue: 1,
          unCheckedChildren: $t('common.disabled'),
          unCheckedValue: 0,
          ...props,
          checked: row[column.field],
          loading: row[loadingKey] ?? false,
          'onUpdate:checked': onChange,
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

        return h(Switch, finallyProps);
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

    // add by 星语：文件大小格式化
    vxeUI.formats.add('formatFileSize', {
      tableCellFormatMethod({ cellValue }, digits = 2) {
        return formatFileSize(cellValue, digits);
      },
    });
  },
  useVbenForm,
});

export { createRequiredValidation, useVbenVxeGrid };

export const [VxeTable, VxeColumn] = [AsyncVxeTable, AsyncVxeColumn];

export * from '#/components/table-action';

export type * from '@vben/plugins/vxe-table';
