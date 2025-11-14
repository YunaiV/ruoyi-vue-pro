<script setup lang="ts">
import type { AuthPageLayoutType } from '@vben/types';

import type { VbenDropdownMenuItem } from '@vben-core/shadcn-ui';

import { computed } from 'vue';

import { InspectionPanel, PanelLeft, PanelRight } from '@vben/icons';
import { $t } from '@vben/locales';
import {
  preferences,
  updatePreferences,
  usePreferences,
} from '@vben/preferences';

import { VbenDropdownRadioMenu, VbenIconButton } from '@vben-core/shadcn-ui';

defineOptions({
  name: 'AuthenticationLayoutToggle',
});

const menus = computed((): VbenDropdownMenuItem[] => [
  {
    icon: PanelLeft,
    label: $t('authentication.layout.alignLeft'),
    value: 'panel-left',
  },
  {
    icon: InspectionPanel,
    label: $t('authentication.layout.center'),
    value: 'panel-center',
  },
  {
    icon: PanelRight,
    label: $t('authentication.layout.alignRight'),
    value: 'panel-right',
  },
]);

const { authPanelCenter, authPanelLeft, authPanelRight } = usePreferences();

function handleUpdate(value: string | undefined) {
  if (!value) return;
  updatePreferences({
    app: {
      authPageLayout: value as AuthPageLayoutType,
    },
  });
}
</script>

<template>
  <VbenDropdownRadioMenu
    :menus="menus"
    :model-value="preferences.app.authPageLayout"
    @update:model-value="handleUpdate"
  >
    <VbenIconButton>
      <PanelRight v-if="authPanelRight" class="size-4" />
      <PanelLeft v-if="authPanelLeft" class="size-4" />
      <InspectionPanel v-if="authPanelCenter" class="size-4" />
    </VbenIconButton>
  </VbenDropdownRadioMenu>
</template>
