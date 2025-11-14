import type { ComputedRef, Directive } from 'vue';

import { useTippy } from 'vue-tippy';

export default function useTippyDirective(isDark: ComputedRef<boolean>) {
  const directive: Directive = {
    mounted(el, binding, vnode) {
      const opts =
        typeof binding.value === 'string'
          ? { content: binding.value }
          : binding.value || {};

      const modifiers = Object.keys(binding.modifiers || {});
      const placement = modifiers.find((modifier) => modifier !== 'arrow');
      const withArrow = modifiers.includes('arrow');

      if (placement) {
        opts.placement = opts.placement || placement;
      }

      if (withArrow) {
        opts.arrow = opts.arrow === undefined ? true : opts.arrow;
      }

      if (vnode.props && vnode.props.onTippyShow) {
        opts.onShow = function (...args: any[]) {
          return vnode.props?.onTippyShow(...args);
        };
      }

      if (vnode.props && vnode.props.onTippyShown) {
        opts.onShown = function (...args: any[]) {
          return vnode.props?.onTippyShown(...args);
        };
      }

      if (vnode.props && vnode.props.onTippyHidden) {
        opts.onHidden = function (...args: any[]) {
          return vnode.props?.onTippyHidden(...args);
        };
      }

      if (vnode.props && vnode.props.onTippyHide) {
        opts.onHide = function (...args: any[]) {
          return vnode.props?.onTippyHide(...args);
        };
      }

      if (vnode.props && vnode.props.onTippyMount) {
        opts.onMount = function (...args: any[]) {
          return vnode.props?.onTippyMount(...args);
        };
      }

      if (el.getAttribute('title') && !opts.content) {
        opts.content = el.getAttribute('title');
        el.removeAttribute('title');
      }

      if (el.getAttribute('content') && !opts.content) {
        opts.content = el.getAttribute('content');
      }

      useTippy(el, opts);
    },
    unmounted(el) {
      if (el.$tippy) {
        el.$tippy.destroy();
      } else if (el._tippy) {
        el._tippy.destroy();
      }
    },

    updated(el, binding) {
      const opts =
        typeof binding.value === 'string'
          ? { content: binding.value, theme: isDark.value ? '' : 'light' }
          : Object.assign(
              { theme: isDark.value ? '' : 'light' },
              binding.value,
            );

      if (el.getAttribute('title') && !opts.content) {
        opts.content = el.getAttribute('title');
        el.removeAttribute('title');
      }

      if (el.getAttribute('content') && !opts.content) {
        opts.content = el.getAttribute('content');
      }

      if (el.$tippy) {
        el.$tippy.setProps(opts || {});
      } else if (el._tippy) {
        el._tippy.setProps(opts || {});
      }
    },
  };
  return directive;
}
