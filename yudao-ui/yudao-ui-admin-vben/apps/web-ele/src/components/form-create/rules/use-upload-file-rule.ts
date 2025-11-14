import { buildUUID } from '@vben/utils';

import {
  localeProps,
  makeRequiredRule,
} from '#/components/form-create/helpers';

export const useUploadFileRule = () => {
  const label = '文件上传';
  const name = 'FileUpload';
  return {
    icon: 'icon-upload',
    label,
    name,
    rule() {
      return {
        type: name,
        field: buildUUID(),
        title: label,
        info: '',
        $required: false,
      };
    },
    props(_: any, { t }: any) {
      return localeProps(t, `${name}.props`, [
        makeRequiredRule(),
        {
          type: 'select',
          field: 'fileType',
          title: '文件类型',
          value: ['doc', 'xls', 'ppt', 'txt', 'pdf'],
          options: [
            { label: 'doc', value: 'doc' },
            { label: 'xls', value: 'xls' },
            { label: 'ppt', value: 'ppt' },
            { label: 'txt', value: 'txt' },
            { label: 'pdf', value: 'pdf' },
          ],
          props: {
            multiple: true,
          },
        },
        {
          type: 'switch',
          field: 'autoUpload',
          title: '是否在选取文件后立即进行上传',
          value: true,
        },
        {
          type: 'switch',
          field: 'drag',
          title: '拖拽上传',
          value: false,
        },
        {
          type: 'switch',
          field: 'isShowTip',
          title: '是否显示提示',
          value: true,
        },
        {
          type: 'inputNumber',
          field: 'fileSize',
          title: '大小限制(MB)',
          value: 5,
          props: { min: 0 },
        },
        {
          type: 'inputNumber',
          field: 'limit',
          title: '数量限制',
          value: 5,
          props: { min: 0 },
        },
        {
          type: 'switch',
          field: 'disabled',
          title: '是否禁用',
          value: false,
        },
      ]);
    },
  };
};
