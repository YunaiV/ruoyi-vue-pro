<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { AuthenticationAuthTitle, VbenButton } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { authorize, getAuthorize } from '#/api/system/oauth2/open';

defineOptions({ name: 'SSOLogin' });

const { query } = useRoute(); // 路由参数

const client = ref({
  name: '',
  logo: '',
}); // 客户端信息

const queryParams = reactive({
  responseType: '',
  clientId: '',
  redirectUri: '',
  state: '',
  scopes: [] as string[], // 优先从 query 参数获取；如果未传递，从后端获取
}); // URL 上的 client_id、scope 等参数

const loading = ref(false); // 表单是否提交中

/** 初始化授权信息 */
async function init() {
  // 防止在没有登录的情况下循环弹窗
  if (query.client_id === undefined) {
    return;
  }
  // 解析参数
  // 例如说【自动授权不通过】：client_id=default&redirect_uri=https%3A%2F%2Fwww.iocoder.cn&response_type=code&scope=user.read%20user.write
  // 例如说【自动授权通过】：client_id=default&redirect_uri=https%3A%2F%2Fwww.iocoder.cn&response_type=code&scope=user.read
  queryParams.responseType = query.response_type as string;
  queryParams.clientId = query.client_id as string;
  queryParams.redirectUri = query.redirect_uri as string;
  queryParams.state = query.state as string;
  if (query.scope) {
    queryParams.scopes = (query.scope as string).split(' ');
  }

  // 如果有 scope 参数，先执行一次自动授权，看看是否之前都授权过了。
  if (queryParams.scopes.length > 0) {
    const data = await doAuthorize(true, queryParams.scopes, []);
    if (data) {
      location.href = data;
      return;
    }
  }

  // 1.1 获取授权页的基本信息
  const data = await getAuthorize(queryParams.clientId);
  client.value = data.client;
  // 1.2 解析 scope
  let scopes;
  // 如果 params.scope 非空，则过滤下返回的 scopes
  if (queryParams.scopes.length > 0) {
    scopes = data.scopes.filter((scope) =>
      queryParams.scopes.includes(scope.key),
    );
    // 如果 params.scope 为空，则使用返回的 scopes 设置它
  } else {
    scopes = data.scopes;
    queryParams.scopes = scopes.map((scope) => scope.key);
  }

  // 2.设置表单的初始值
  formApi.setFieldValue(
    'scopes',
    scopes.filter((scope) => scope.value).map((scope) => scope.key),
  );
}

/** 处理授权的提交 */
async function handleSubmit(approved: boolean) {
  // 计算 checkedScopes + uncheckedScopes
  let checkedScopes: string[];
  let uncheckedScopes: string[];
  if (approved) {
    // 同意授权，按照用户的选择
    const res = await formApi.getValues();
    checkedScopes = res.scopes;
    uncheckedScopes = queryParams.scopes.filter(
      (item) => !checkedScopes.includes(item),
    );
  } else {
    // 拒绝，则都是取消
    checkedScopes = [];
    uncheckedScopes = queryParams.scopes;
  }

  // 提交授权的请求
  loading.value = true;
  try {
    const data = await doAuthorize(false, checkedScopes, uncheckedScopes);
    if (!data) {
      return;
    }
    // 跳转授权成功后的回调地址
    location.href = data;
  } finally {
    loading.value = false;
  }
}

/** 调用授权 API 接口 */
const doAuthorize = (
  autoApprove: boolean,
  checkedScopes: string[],
  uncheckedScopes: string[],
) => {
  return authorize(
    queryParams.responseType,
    queryParams.clientId,
    queryParams.redirectUri,
    queryParams.state,
    autoApprove,
    checkedScopes,
    uncheckedScopes,
  );
};

/** 格式化 scope 文本 */
function formatScope(scope: string) {
  // 格式化 scope 授权范围，方便用户理解。
  // 这里仅仅是一个 demo，可以考虑录入到字典数据中，例如说字典类型 "system_oauth2_scope"，它的每个 scope 都是一条字典数据。
  switch (scope) {
    case 'user.read': {
      return '访问你的个人信息';
    }
    case 'user.write': {
      return '修改你的个人信息';
    }
    default: {
      return scope;
    }
  }
}

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      fieldName: 'scopes',
      label: '授权范围',
      component: 'CheckboxGroup',
      componentProps: {
        options: queryParams.scopes.map((scope) => ({
          label: formatScope(scope),
          value: scope,
        })),
        class: 'flex flex-col gap-2',
      },
    },
  ];
});

const [Form, formApi] = useVbenForm(
  reactive({
    commonConfig: {
      hideLabel: true,
      hideRequiredMark: true,
    },
    schema: formSchema,
    showDefaultActions: false,
  }),
);

/** 初始化 */
onMounted(() => {
  init();
});
</script>

<template>
  <div @keydown.enter.prevent="handleSubmit(true)">
    <AuthenticationAuthTitle>
      <slot name="title">
        {{ `${client.name} 👋🏻` }}
      </slot>
      <template #desc>
        <span class="text-muted-foreground">
          此第三方应用请求获得以下权限：
        </span>
      </template>
    </AuthenticationAuthTitle>

    <Form />

    <div class="flex gap-2">
      <VbenButton
        :class="{
          'cursor-wait': loading,
        }"
        :loading="loading"
        aria-label="login"
        class="w-2/3"
        @click="handleSubmit(true)"
      >
        同意授权
      </VbenButton>
      <VbenButton
        :class="{
          'cursor-wait': loading,
        }"
        :loading="loading"
        aria-label="login"
        class="w-1/3"
        variant="outline"
        @click="handleSubmit(false)"
      >
        拒绝
      </VbenButton>
    </div>
  </div>
</template>
