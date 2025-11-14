import { buildUUID } from '@vben/utils';

import {
  localeProps,
  makeRequiredRule,
} from '#/components/form-create/helpers';

export const useEditorRule = () => {
  const label = '富文本';
  const name = 'Tinymce';
  return {
    icon: 'icon-editor',
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
          type: 'input',
          field: 'height',
          title: '高度',
        },
        { type: 'switch', field: 'readonly', title: '是否只读' },
      ]);
    },
  };
};
