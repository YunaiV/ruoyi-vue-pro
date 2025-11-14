import type { DefaultProps, Props } from 'tippy.js';

import type { App, SetupContext } from 'vue';

import { h, watchEffect } from 'vue';
import { setDefaultProps, Tippy as TippyComponent } from 'vue-tippy';

import { usePreferences } from '@vben-core/preferences';

import useTippyDirective from './directive';

import 'tippy.js/dist/tippy.css';
import 'tippy.js/dist/backdrop.css';
import 'tippy.js/themes/light.css';
import 'tippy.js/animations/scale.css';
import 'tippy.js/animations/shift-toward.css';
import 'tippy.js/animations/shift-away.css';
import 'tippy.js/animations/perspective.css';

const { isDark } = usePreferences();
export type TippyProps = Partial<
  Props & {
    animation?:
      | 'fade'
      | 'perspective'
      | 'scale'
      | 'shift-away'
      | 'shift-toward'
      | boolean;
    theme?: 'auto' | 'dark' | 'light';
  }
>;

export function initTippy(app: App<Element>, options?: DefaultProps) {
  setDefaultProps({
    allowHTML: true,
    delay: [500, 200],
    theme: isDark.value ? '' : 'light',
    ...options,
  });
  if (!options || !Reflect.has(options, 'theme') || options.theme === 'auto') {
    watchEffect(() => {
      setDefaultProps({ theme: isDark.value ? '' : 'light' });
    });
  }

  app.directive('tippy', useTippyDirective(isDark));
}

export const Tippy = (props: any, { attrs, slots }: SetupContext) => {
  let theme: string = (attrs.theme as string) ?? 'auto';
  if (theme === 'auto') {
    theme = isDark.value ? '' : 'light';
  }
  if (theme === 'dark') {
    theme = '';
  }
  return h(
    TippyComponent,
    {
      ...props,
      ...attrs,
      theme,
    },
    slots,
  );
};
