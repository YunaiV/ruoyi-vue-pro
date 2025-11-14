<script lang="ts" setup>
import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@vben-core/shadcn-ui';

interface Tenant {
  id?: number;
  name: string;
  packageId: number;
  contactName: string;
  contactMobile: string;
  accountCount: number;
  expireTime: Date;
  websites: string[];
  status: number;
}

defineOptions({
  name: 'TenantDropdown',
});

const props = defineProps<{
  tenantList?: Tenant[];
  visitTenantId?: null | number;
}>();

const emit = defineEmits(['success']);

// 租户列表
const tenants = computed(() => props.tenantList ?? []);

async function handleChange(id: number | undefined) {
  if (!id) {
    return;
  }
  const tenant = tenants.value.find((item) => item.id === id);

  emit('success', tenant);
}
</script>
<template>
  <DropdownMenu>
    <DropdownMenuTrigger>
      <Button
        variant="outline"
        class="hover:bg-accent ml-1 mr-2 h-8 w-32 cursor-pointer rounded-full p-1.5"
      >
        <IconifyIcon icon="lucide:align-justify" class="mr-4" />
        {{
          tenants.find((item) => item.id === visitTenantId)?.name ||
          $t('page.tenant.placeholder')
        }}
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent class="w-40 p-0 pb-1">
      <DropdownMenuGroup>
        <DropdownMenuItem
          v-for="tenant in tenants"
          :key="tenant.id"
          :disabled="tenant.id === visitTenantId"
          class="mx-1 flex cursor-pointer items-center rounded-sm py-1 leading-8"
          @click="handleChange(tenant.id)"
        >
          <template v-if="tenant.id === visitTenantId">
            <IconifyIcon icon="lucide:check" class="mr-2" />
            {{ tenant.name }}
          </template>
          <template v-else>
            {{ tenant.name }}
          </template>
        </DropdownMenuItem>
      </DropdownMenuGroup>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
