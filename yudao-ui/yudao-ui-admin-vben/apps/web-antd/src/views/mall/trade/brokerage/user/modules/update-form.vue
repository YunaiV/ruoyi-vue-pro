<script lang="ts" setup>
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { $t } from '@vben/locales';
import { formatDate, isEmpty } from '@vben/utils';

import {
  Avatar,
  Descriptions,
  DescriptionsItem,
  Divider,
  InputSearch,
  message,
} from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  getBrokerageUser,
  updateBindUser,
} from '#/api/mall/trade/brokerage/user';
import { DictTag } from '#/components/dict-tag';

import { useUpdateFormSchema } from '../data';

defineOptions({ name: 'BrokerageUserUpdateForm' });

const emit = defineEmits(['success']);

const formData = ref<any>({
  id: undefined,
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
  schema: useUpdateFormSchema(),
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
    try {
      await updateBindUser(formData.value);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  onOpenChange: async (isOpen: boolean) => {
    if (!isOpen) {
      return;
    }
    const data = modalApi.getData<MallBrokerageUserApi.BrokerageUser>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = {
        id: data.id,
        bindUserId: data.bindUserId,
      };
      await formApi.setValues(formData.value);
      if (data.bindUserId) {
        await handleSearchUser(data.bindUserId, '上级分销员');
      }
    } finally {
      modalApi.unlock();
    }
  },
});

/** 绑定用户信息 */
const bindUser = ref<MallBrokerageUserApi.BrokerageUser | undefined>();

/** 查询分销员 */
async function handleSearchUser(id: number, userType: string) {
  if (isEmpty(id)) {
    message.warning(`请先输入${userType}编号后重试！！！`);
    return;
  }
  if (formData.value?.bindUserId === formData.value?.id) {
    message.error('不能绑定自己为推广人');
    return;
  }

  const userData = await getBrokerageUser(id);
  if (!userData) {
    message.warning(`${userType}不存在`);
    return;
  }
  bindUser.value = userData as MallBrokerageUserApi.BrokerageUser;
}
</script>

<template>
  <Modal title="修改上级推广人" class="w-1/3">
    <Form>
      <template #bindUserId>
        <InputSearch
          v-model:value="formData.bindUserId"
          placeholder="请输入上级分销员编号"
          @search="handleSearchUser(formData.bindUserId, '上级分销员')"
        />
      </template>
    </Form>

    <!-- 上级推广员信息展示 -->
    <Divider v-if="bindUser" />
    <Descriptions v-if="bindUser" title="上级推广员信息" :column="1" bordered>
      <DescriptionsItem label="头像">
        <Avatar :src="bindUser?.avatar" />
      </DescriptionsItem>
      <DescriptionsItem label="昵称">
        {{ bindUser?.nickname }}
      </DescriptionsItem>
      <DescriptionsItem label="分销资格">
        <DictTag
          :type="DICT_TYPE.INFRA_BOOLEAN_STRING"
          :value="bindUser?.brokerageEnabled"
        />
      </DescriptionsItem>
      <DescriptionsItem label="成为分销员的时间">
        {{ formatDate(bindUser?.brokerageTime) }}
      </DescriptionsItem>
    </Descriptions>
  </Modal>
</template>
