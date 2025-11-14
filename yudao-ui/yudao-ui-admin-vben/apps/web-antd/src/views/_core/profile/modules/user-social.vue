<script setup lang="tsx">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemSocialUserApi } from '#/api/system/social/user';

import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { confirm } from '@vben/common-ui';
import { DICT_TYPE, SystemUserSocialTypeEnum } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { getUrlValue } from '@vben/utils';

import { Button, Card, Image, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { socialAuthRedirect } from '#/api/core/auth';
import {
  getBindSocialUserList,
  socialBind,
  socialUnbind,
} from '#/api/system/social/user';
import { $t } from '#/locales';

const emit = defineEmits<{
  (e: 'update:activeName', v: string): void;
}>();

const route = useRoute();
/** 已经绑定的平台 */
const bindList = ref<SystemSocialUserApi.SocialUser[]>([]);
const allBindList = computed<any[]>(() => {
  return Object.values(SystemUserSocialTypeEnum).map((social) => {
    const socialUser = bindList.value.find((item) => item.type === social.type);
    return {
      ...social,
      socialUser,
    };
  });
});

function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'type',
      title: '绑定平台',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SOCIAL_TYPE },
      },
    },
    {
      field: 'openid',
      title: '标识',
      minWidth: 180,
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 180,
    },
    {
      field: 'operation',
      title: '操作',
      minWidth: 80,
      align: 'center',
      fixed: 'right',
      slots: {
        default: ({ row }: { row: SystemSocialUserApi.SocialUser }) => {
          return (
            <Button onClick={() => onUnbind(row)} type="link">
              解绑
            </Button>
          );
        },
      },
    },
  ];
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    minHeight: 0,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async () => {
          bindList.value = await getBindSocialUserList();
          return bindList.value;
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions<SystemSocialUserApi.SocialUser>,
});

/** 解绑账号 */
function onUnbind(row: SystemSocialUserApi.SocialUser) {
  confirm({
    content: `确定解绑[${getDictLabel(DICT_TYPE.SYSTEM_SOCIAL_TYPE, row.type)}]平台的[${row.openid}]账号吗？`,
  }).then(async () => {
    await socialUnbind({ type: row.type, openid: row.openid });
    // 提示成功
    message.success($t('ui.actionMessage.operationSuccess'));
    await gridApi.reload();
  });
}

/** 绑定账号（跳转授权页面） */
async function onBind(bind: any) {
  const type = bind.type;
  if (type <= 0) {
    return;
  }
  try {
    // 计算 redirectUri
    // tricky: type 需要先 encode 一次，否则钉钉回调会丢失。配合 getUrlValue() 使用
    const redirectUri = `${location.origin}/profile?${encodeURIComponent(`type=${type}`)}`;

    // 进行跳转
    window.location.href = await socialAuthRedirect(type, redirectUri);
  } catch (error) {
    console.error('社交绑定处理失败:', error);
  }
}

/** 监听路由变化，处理社交绑定回调 */
async function bindSocial() {
  // 社交绑定
  const type = Number(getUrlValue('type'));
  const code = route.query.code as string;
  const state = route.query.state as string;
  if (!code) {
    return;
  }
  await socialBind({ type, code, state });
  // 提示成功
  message.success('绑定成功');
  emit('update:activeName', 'userSocial');
  await gridApi.reload();
  // 清理 URL 参数，避免刷新重复触发
  window.history.replaceState({}, '', location.pathname);
}

/** 初始化 */
onMounted(() => {
  bindSocial();
});
</script>

<template>
  <div class="flex flex-col">
    <Grid />

    <div class="pb-3">
      <div
        class="grid grid-cols-1 gap-2 px-2 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3 2xl:grid-cols-3"
      >
        <Card v-for="item in allBindList" :key="item.type" class="!mb-2">
          <div class="flex w-full items-center gap-4">
            <Image
              :src="item.img"
              :width="40"
              :height="40"
              :alt="item.title"
              :preview="false"
            />
            <div class="flex flex-1 items-center justify-between">
              <div class="flex flex-col">
                <h4 class="mb-1 text-sm text-black/85 dark:text-white/85">
                  {{ getDictLabel(DICT_TYPE.SYSTEM_SOCIAL_TYPE, item.type) }}
                </h4>
                <span class="text-black/45 dark:text-white/45">
                  <template v-if="item.socialUser">
                    {{ item.socialUser?.nickname || item.socialUser?.openid }}
                  </template>
                  <template v-else>
                    绑定
                    {{ getDictLabel(DICT_TYPE.SYSTEM_SOCIAL_TYPE, item.type) }}
                    账号
                  </template>
                </span>
              </div>
              <Button
                :disabled="!!item.socialUser"
                size="small"
                type="link"
                @click="onBind(item)"
              >
                {{ item.socialUser ? '已绑定' : '绑定' }}
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>
