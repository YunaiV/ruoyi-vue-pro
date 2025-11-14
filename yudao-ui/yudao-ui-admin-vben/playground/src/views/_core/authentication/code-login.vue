<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, ref, useTemplateRef } from 'vue';

import { AuthenticationCodeLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

defineOptions({ name: 'CodeLogin' });

const loading = ref(false);
const CODE_LENGTH = 6;
const loginRef =
  useTemplateRef<InstanceType<typeof AuthenticationCodeLogin>>('loginRef');
function sendCodeApi(phoneNumber: string) {
  message.loading({
    content: $t('page.auth.sendingCode'),
    duration: 0,
    key: 'sending-code',
  });
  return new Promise((resolve) => {
    setTimeout(() => {
      message.success({
        content: $t('page.auth.codeSentTo', [phoneNumber]),
        duration: 3,
        key: 'sending-code',
      });
      resolve({ code: '123456', phoneNumber });
    }, 3000);
  });
}
const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.mobile'),
      },
      fieldName: 'phoneNumber',
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
        handleSendCode: async () => {
          // 模拟发送验证码
          // Simulate sending verification code
          loading.value = true;
          const formApi = loginRef.value?.getFormApi();
          if (!formApi) {
            loading.value = false;
            throw new Error('formApi is not ready');
          }
          await formApi.validateField('phoneNumber');
          const isPhoneReady = await formApi.isFieldValid('phoneNumber');
          if (!isPhoneReady) {
            loading.value = false;
            throw new Error('Phone number is not Ready');
          }
          const { phoneNumber } = await formApi.getValues();
          await sendCodeApi(phoneNumber);
          loading.value = false;
        },
        placeholder: $t('authentication.code'),
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
  // eslint-disable-next-line no-console
  console.log(values);
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
