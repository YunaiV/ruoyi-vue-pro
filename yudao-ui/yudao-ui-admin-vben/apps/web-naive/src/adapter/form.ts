import type {
  VbenFormSchema as FormSchema,
  VbenFormProps,
} from '@vben/common-ui';

import type { ComponentType } from './component';

import { setupVbenForm, useVbenForm as useForm, z } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { isMobile } from '@vben/utils';

async function initSetupVbenForm() {
  setupVbenForm<ComponentType>({
    config: {
      // naive-ui组件的空值为null,不能是undefined，否则重置表单时不生效
      emptyStateValue: null,
      baseModelPropName: 'value',
      modelPropNameMap: {
        Checkbox: 'checked',
        Radio: 'checked',
        Upload: 'fileList',
        DatePicker: 'formatted-value',
      },
    },
    defineRules: {
      required: (value, _params, ctx) => {
        if (value === undefined || value === null || value.length === 0) {
          return $t('ui.formRules.required', [ctx.label]);
        }
        return true;
      },
      selectRequired: (value, _params, ctx) => {
        if (value === undefined || value === null) {
          return $t('ui.formRules.selectRequired', [ctx.label]);
        }
        return true;
      },
      // 手机号非必填
      mobile: (value, _params, ctx) => {
        if (value === undefined || value === null || value.length === 0) {
          return true;
        } else if (!isMobile(value)) {
          return $t('ui.formRules.mobile', [ctx.label]);
        }
        return true;
      },
      // 手机号必填
      mobileRequired: (value, _params, ctx) => {
        if (value === undefined || value === null || value.length === 0) {
          return $t('ui.formRules.required', [ctx.label]);
        }
        if (!isMobile(value)) {
          return $t('ui.formRules.mobile', [ctx.label]);
        }
        return true;
      },
    },
  });
}

const useVbenForm = useForm<ComponentType>;

export { initSetupVbenForm, useVbenForm, z };

export type VbenFormSchema = FormSchema<ComponentType>;
export type { VbenFormProps };
