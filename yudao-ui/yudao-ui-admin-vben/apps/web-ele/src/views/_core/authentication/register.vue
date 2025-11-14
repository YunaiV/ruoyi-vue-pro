<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';

import type { AuthApi } from '#/api/core/auth';

import { computed, h, onMounted, ref } from 'vue';

import { AuthenticationRegister, Verification, z } from '@vben/common-ui';
import { isCaptchaEnable, isTenantEnable } from '@vben/hooks';
import { $t } from '@vben/locales';
import { useAccessStore } from '@vben/stores';

import {
  checkCaptcha,
  getCaptcha,
  getTenantByWebsite,
  getTenantSimpleList,
} from '#/api/core/auth';
import { useAuthStore } from '#/store';

defineOptions({ name: 'Register' });

const loading = ref(false);

const accessStore = useAccessStore();
const authStore = useAuthStore();
const tenantEnable = isTenantEnable();
const captchaEnable = isCaptchaEnable();

const registerRef = ref();
const verifyRef = ref();

const captchaType = 'blockPuzzle'; // 验证码类型：'blockPuzzle' | 'clickWord'

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
    registerRef.value
      .getFormApi()
      .setFieldValue('tenantId', tenantId?.toString());
  } catch (error) {
    console.error('获取租户列表失败:', error);
  }
}

/** 执行注册 */
async function handleRegister(values: any) {
  // 如果开启验证码，则先验证验证码
  if (captchaEnable) {
    verifyRef.value.show();
    return;
  }

  // 无验证码，直接登录
  await authStore.authLogin('register', values);
}

/** 验证码通过，执行注册 */
const handleVerifySuccess = async ({ captchaVerification }: any) => {
  try {
    await authStore.authLogin('register', {
      ...(await registerRef.value.getFormApi().getValues()),
      captchaVerification,
    });
  } catch (error) {
    console.error('Error in handleRegister:', error);
  }
};

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
        placeholder: $t('authentication.usernameTip'),
      },
      fieldName: 'username',
      label: $t('authentication.username'),
      rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.nicknameTip'),
      },
      fieldName: 'nickname',
      label: $t('authentication.nickname'),
      rules: z.string().min(1, { message: $t('authentication.nicknameTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: $t('authentication.password'),
      },
      fieldName: 'password',
      label: $t('authentication.password'),
      renderComponentContent() {
        return {
          strengthText: () => $t('authentication.passwordStrength'),
        };
      },
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.confirmPassword'),
      },
      dependencies: {
        rules(values) {
          const { password } = values;
          return z
            .string({ required_error: $t('authentication.passwordTip') })
            .min(1, { message: $t('authentication.passwordTip') })
            .refine((value) => value === password, {
              message: $t('authentication.confirmPasswordTip'),
            });
        },
        triggerFields: ['password'],
      },
      fieldName: 'confirmPassword',
      label: $t('authentication.confirmPassword'),
    },
    {
      component: 'VbenCheckbox',
      fieldName: 'agreePolicy',
      renderComponentContent: () => ({
        default: () =>
          h('span', [
            $t('authentication.agree'),
            h(
              'a',
              {
                class: 'vben-link ml-1 ',
                href: '',
              },
              `${$t('authentication.privacyPolicy')} & ${$t('authentication.terms')}`,
            ),
          ]),
      }),
      rules: z.boolean().refine((value) => !!value, {
        message: $t('authentication.agreeTip'),
      }),
    },
  ];
});
</script>

<template>
  <div>
    <AuthenticationRegister
      ref="registerRef"
      :form-schema="formSchema"
      :loading="loading"
      @submit="handleRegister"
    />
    <Verification
      ref="verifyRef"
      v-if="captchaEnable"
      :captcha-type="captchaType"
      :check-captcha-api="checkCaptcha"
      :get-captcha-api="getCaptcha"
      :img-size="{ width: '400px', height: '200px' }"
      mode="pop"
      @on-success="handleVerifySuccess"
    />
  </div>
</template>
