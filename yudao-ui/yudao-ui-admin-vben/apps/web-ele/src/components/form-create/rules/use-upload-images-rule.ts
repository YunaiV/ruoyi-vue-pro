import { buildUUID } from '@vben/utils';

import {
  localeProps,
  makeRequiredRule,
} from '#/components/form-create/helpers';

export const useUploadImagesRule = () => {
  const label = '多图上传';
  const name = 'ImagesUpload';
  return {
    icon: 'icon-image',
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
          type: 'switch',
          field: 'drag',
          title: '拖拽上传',
          value: false,
        },
        {
          type: 'select',
          field: 'fileType',
          title: '图片类型限制',
          value: ['image/jpeg', 'image/png', 'image/gif'],
          options: [
            { label: 'image/apng', value: 'image/apng' },
            { label: 'image/bmp', value: 'image/bmp' },
            { label: 'image/gif', value: 'image/gif' },
            { label: 'image/jpeg', value: 'image/jpeg' },
            { label: 'image/pjpeg', value: 'image/pjpeg' },
            { label: 'image/svg+xml', value: 'image/svg+xml' },
            { label: 'image/tiff', value: 'image/tiff' },
            { label: 'image/webp', value: 'image/webp' },
            { label: 'image/x-icon', value: 'image/x-icon' },
          ],
          props: {
            multiple: true,
            maxNumber: 5,
          },
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
          type: 'input',
          field: 'height',
          title: '组件高度',
          value: '150px',
        },
        {
          type: 'input',
          field: 'width',
          title: '组件宽度',
          value: '150px',
        },
        {
          type: 'input',
          field: 'borderradius',
          title: '组件边框圆角',
          value: '8px',
        },
      ]);
    },
  };
};
