<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import type { AuthApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { AuthenticationCodeLogin, z } from '@vben/common-ui';
import { isTenantEnable } from '@vben/hooks';
import { $t } from '@vben/locales';
import { useAccessStore } from '@vben/stores';

import { message } from '#/adapter/naive';
import { sendSmsCode } from '#/api';
import { getTenantByWebsite, getTenantSimpleList } from '#/api/core/auth';
import { useAuthStore } from '#/store';

defineOptions({ name: 'CodeLogin' });

const authStore = useAuthStore();
const accessStore = useAccessStore();
const tenantEnable = isTenantEnable();

const loading = ref(false);
const CODE_LENGTH = 4;

const loginRef = ref();

/** 获取租户列表，并默认选中 */
const tenantList = ref<AuthApi.TenantResult[]>([]); // 租户列表
async function fetchTenantList() {
  if (!tenantEnable) {
    return;
  }
  try {
    // 获取租户列表、域名对应租户
    const websiteTenantPromise = getTenantByWebsite(window.location.hostname);
    tenantList.value = await getTenantSimpleList();

    // 选中租户：域名 > store 中的租户 > 首个租户
    let tenantId: null | number = null;
    const websiteTenant = await websiteTenantPromise;
    if (websiteTenant?.id) {
      tenantId = websiteTenant.id;
    }
    // 如果没有从域名获取到租户，尝试从 store 中获取
    if (!tenantId && accessStore.tenantId) {
      tenantId = accessStore.tenantId;
    }
    // 如果还是没有租户，使用列表中的第一个
    if (!tenantId && tenantList.value?.[0]?.id) {
      tenantId = tenantList.value[0].id;
    }

    // 设置选中的租户编号
    accessStore.setTenantId(tenantId);
    loginRef.value.getFormApi().setFieldValue('tenantId', tenantId?.toString());
  } catch (error) {
    console.error('获取租户列表失败:', error);
  }
}

/** 组件挂载时获取租户信息 */
onMounted(() => {
  fetchTenantList();
});

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenSelect',
      componentProps: {
        options: tenantList.value.map((item) => ({
          label: item.name,
          value: item.id.toString(),
        })),
        placeholder: $t('authentication.tenantTip'),
      },
      fieldName: 'tenantId',
      label: $t('authentication.tenant'),
      rules: z.string().min(1, { message: $t('authentication.tenantTip') }),
      dependencies: {
        triggerFields: ['tenantId'],
        if: tenantEnable,
        trigger(values) {
          if (values.tenantId) {
            accessStore.setTenantId(Number(values.tenantId));
          }
        },
      },
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.mobile'),
      },
      fieldName: 'mobile',
      label: $t('authentication.mobile'),
      rules: z
        .string()
        .min(1, { message: $t('authentication.mobileTip') })
        .refine((v) => /^\d{11}$/.test(v), {
          message: $t('authentication.mobileErrortip'),
        }),
    },
    {
      component: 'VbenPinInput',
      componentProps: {
        codeLength: CODE_LENGTH,
        createText: (countdown: number) => {
          const text =
            countdown > 0
              ? $t('authentication.sendText', [countdown])
              : $t('authentication.sendCode');
          return text;
        },
        placeholder: $t('authentication.code'),
        handleSendCode: async () => {
          loading.value = true;
          try {
            const formApi = loginRef.value?.getFormApi();
            if (!formApi) {
              throw new Error('表单未准备好');
            }
            // 验证手机号
            await formApi.validateField('mobile');
            const isMobileValid = await formApi.isFieldValid('mobile');
            if (!isMobileValid) {
              throw new Error('请输入有效的手机号码');
            }

            // 发送验证码
            const { mobile } = await formApi.getValues();
            const scene = 21; // 场景：短信验证码登录
            await sendSmsCode({ mobile, scene });
            message.success('验证码发送成功');
          } finally {
            loading.value = false;
          }
        },
      },
      fieldName: 'code',
      label: $t('authentication.code'),
      rules: z.string().length(CODE_LENGTH, {
        message: $t('authentication.codeTip', [CODE_LENGTH]),
      }),
    },
  ];
});
/**
 * 异步处理登录操作
 * Asynchronously handle the login process
 * @param values 登录表单数据
 */
async function handleLogin(values: Recordable<any>) {
  try {
    await authStore.authLogin('mobile', values);
  } catch (error) {
    console.error('Error in handleLogin:', error);
  }
}
</script>

<template>
  <AuthenticationCodeLogin
    ref="loginRef"
    :form-schema="formSchema"
    :loading="loading"
    @submit="handleLogin"
  />
</template>
