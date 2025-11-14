<script lang="ts" setup>
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { formatDate, isEmpty } from '@vben/utils';

import {
  ElAvatar,
  ElDescriptions,
  ElDescriptionsItem,
  ElDivider,
  ElInput,
  ElMessage,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createBrokerageUser,
  getBrokerageUser,
} from '#/api/mall/trade/brokerage/user';
import { getUser } from '#/api/member/user';
import { DictTag } from '#/components/dict-tag';

import { useCreateFormSchema } from '../data';

defineOptions({ name: 'BrokerageUserCreateForm' });

const emit = defineEmits(['success']);

const formData = ref<any>({
  userId: undefined,
  bindUserId: undefined,
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useCreateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formApi.setValues(formData.value);
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    try {
      await createBrokerageUser(formData.value);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  onOpenChange: async (isOpen: boolean) => {
    if (!isOpen) {
      return;
    }
    // 重置表单
    formData.value = {
      userId: undefined,
      bindUserId: undefined,
    };
    await formApi.setValues(formData.value);
    // 重置用户信息
    user.value = undefined;
    bindUser.value = undefined;
  },
});

/** 用户信息 */
const user = ref<MallBrokerageUserApi.BrokerageUser | undefined>();
const bindUser = ref<MallBrokerageUserApi.BrokerageUser | undefined>();

/** 查询分销员和分销员 */
async function handleSearchUser(id: number, userType: string) {
  if (isEmpty(id)) {
    ElMessage.warning(`请先输入${userType}编号后重试！！！`);
    return;
  }
  if (
    userType === '分销员' &&
    formData.value?.bindUserId === formData.value?.userId
  ) {
    ElMessage.error('不能绑定自己为推广员');
    return;
  }

  const userData =
    userType === '分销员' ? await getUser(id) : await getBrokerageUser(id);
  if (!userData) {
    ElMessage.warning(`${userType}不存在`);
    return;
  }
  if (userType === '分销员') {
    user.value = userData as MallBrokerageUserApi.BrokerageUser;
  } else {
    bindUser.value = userData as MallBrokerageUserApi.BrokerageUser;
  }
}
</script>

<template>
  <Modal title="创建分销员" class="w-1/3">
    <Form>
      <template #userId>
        <div class="flex items-center gap-2">
          <ElInput
            v-model="formData.userId"
            placeholder="请输入分销员编号"
            class="flex-1"
          >
            <template #append>
              <ElButton
                type="primary"
                @click="handleSearchUser(formData.userId, '分销员')"
              >
                <IconifyIcon icon="lucide:search" :size="15" />
              </ElButton>
            </template>
          </ElInput>
        </div>
      </template>
      <template #bindUserId>
        <div class="flex items-center gap-2">
          <ElInput
            v-model="formData.bindUserId"
            placeholder="请输入上级分销员编号"
            class="flex-1"
          >
            <template #append>
              <ElButton
                type="primary"
                @click="handleSearchUser(formData.bindUserId, '上级分销员')"
              >
                <IconifyIcon icon="lucide:search" :size="15" />
              </ElButton>
            </template>
          </ElInput>
        </div>
      </template>
    </Form>

    <!-- 分销员信息展示 -->
    <ElDivider v-if="user" />
    <ElDescriptions v-if="user" title="分销员信息" :column="1" border>
      <ElDescriptionsItem label="头像">
        <ElAvatar :src="user?.avatar" />
      </ElDescriptionsItem>
      <ElDescriptionsItem label="昵称">
        {{ user?.nickname }}
      </ElDescriptionsItem>
    </ElDescriptions>
    <!-- 上级推广员信息展示 -->
    <ElDivider v-if="bindUser" />
    <ElDescriptions v-if="bindUser" title="上级推广员信息" :column="1" border>
      <ElDescriptionsItem label="头像">
        <ElAvatar :src="bindUser?.avatar" />
      </ElDescriptionsItem>
      <ElDescriptionsItem label="昵称">
        {{ bindUser?.nickname }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="分销资格">
        <DictTag
          :type="DICT_TYPE.INFRA_BOOLEAN_STRING"
          :value="bindUser?.brokerageEnabled"
        />
      </ElDescriptionsItem>
      <ElDescriptionsItem label="成为分销员的时间">
        {{ formatDate(bindUser?.brokerageTime) }}
      </ElDescriptionsItem>
    </ElDescriptions>
  </Modal>
</template>
