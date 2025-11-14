<script lang="ts" setup>
import { reactive } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { MenuBadge } from '@vben-core/menu-ui';

import { Button, Card, Radio, RadioGroup } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';

const colors = [
  { label: '预设：默认', value: 'default' },
  { label: '预设：关键', value: 'destructive' },
  { label: '预设：主要', value: 'primary' },
  { label: '预设：成功', value: 'success' },
  { label: '自定义', value: 'bg-gray-200 text-black' },
];

const route = useRoute();
const accessStore = useAccessStore();
const menu = accessStore.getMenuByPath(route.path);
const badgeProps = reactive({
  badge: menu?.badge as string,
  badgeType: menu?.badge ? 'normal' : (menu?.badgeType as 'dot' | 'normal'),
  badgeVariants: menu?.badgeVariants as string,
});

const [Form] = useVbenForm({
  handleValuesChange(values) {
    badgeProps.badge = values.badge;
    badgeProps.badgeType = values.badgeType;
    badgeProps.badgeVariants = values.badgeVariants;
  },
  schema: [
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '点徽标', value: 'dot' },
          { label: '文字徽标', value: 'normal' },
        ],
        optionType: 'button',
      },
      defaultValue: badgeProps.badgeType,
      fieldName: 'badgeType',
      label: '类型',
    },
    {
      component: 'Input',
      componentProps: {
        maxLength: 4,
        placeholder: '请输入徽标内容',
        style: { width: '200px' },
      },
      defaultValue: badgeProps.badge,
      fieldName: 'badge',
      label: '徽标内容',
    },
    {
      component: 'RadioGroup',
      defaultValue: badgeProps.badgeVariants,
      fieldName: 'badgeVariants',
      label: '颜色',
    },
    {
      component: 'Input',
      fieldName: 'action',
    },
  ],
  showDefaultActions: false,
});

function updateMenuBadge() {
  if (menu) {
    menu.badge = badgeProps.badge;
    menu.badgeType = badgeProps.badgeType;
    menu.badgeVariants = badgeProps.badgeVariants;
  }
}
</script>

<template>
  <Page
    description="菜单项上可以显示徽标，这些徽标可以主动更新"
    title="菜单徽标"
  >
    <Card title="徽标更新">
      <Form>
        <template #badgeVariants="slotProps">
          <RadioGroup v-bind="slotProps">
            <Radio
              v-for="color in colors"
              :key="color.value"
              :value="color.value"
            >
              <div
                :title="color.label"
                class="flex h-[14px] w-[50px] items-center justify-start"
              >
                <MenuBadge
                  v-bind="{ ...badgeProps, badgeVariants: color.value }"
                />
              </div>
            </Radio>
          </RadioGroup>
        </template>
        <template #action>
          <Button type="primary" @click="updateMenuBadge">更新徽标</Button>
        </template>
      </Form>
    </Card>
  </Page>
</template>
