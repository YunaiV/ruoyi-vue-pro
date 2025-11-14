import type {
  ComponentInternalInstance,
  VNode,
  VNodeChild,
  VNodeNormalizedChildren,
} from 'vue';

import { isVNode } from 'vue';

type VNodeChildAtom = Exclude<VNodeChild, Array<any>>;
type RawSlots = Exclude<VNodeNormalizedChildren, Array<any> | null | string>;

type FlattenVNodes = Array<RawSlots | VNodeChildAtom>;

/**
 * @zh_CN Find the parent component upward
 * @param instance
 * @param parentNames
 */
function findComponentUpward(
  instance: ComponentInternalInstance,
  parentNames: string[],
) {
  let parent = instance.parent;
  while (parent && !parentNames.includes(parent?.type?.name ?? '')) {
    parent = parent.parent;
  }
  return parent;
}

const flattedChildren = (
  children: FlattenVNodes | VNode | VNodeNormalizedChildren,
): FlattenVNodes => {
  const vNodes = Array.isArray(children) ? children : [children];
  const result: FlattenVNodes = [];

  vNodes.forEach((child) => {
    if (Array.isArray(child)) {
      result.push(...flattedChildren(child));
    } else if (isVNode(child) && Array.isArray(child.children)) {
      result.push(...flattedChildren(child.children));
    } else {
      result.push(child);
      if (isVNode(child) && child.component?.subTree) {
        result.push(...flattedChildren(child.component.subTree));
      }
    }
  });
  return result;
};

export { findComponentUpward, flattedChildren };
