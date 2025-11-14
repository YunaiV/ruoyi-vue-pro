import { DEFAULT_NAMESPACE } from '@vben-core/shared/constants';

/**
 * @see copy https://github.com/element-plus/element-plus/blob/dev/packages/hooks/use-namespace/index.ts
 */

const statePrefix = 'is-';

const _bem = (
  namespace: string,
  block: string,
  blockSuffix: string,
  element: string,
  modifier: string,
) => {
  let cls = `${namespace}-${block}`;
  if (blockSuffix) {
    cls += `-${blockSuffix}`;
  }
  if (element) {
    cls += `__${element}`;
  }
  if (modifier) {
    cls += `--${modifier}`;
  }
  return cls;
};

const is: {
  (name: string): string;
  // eslint-disable-next-line @typescript-eslint/unified-signatures
  (name: string, state: boolean | undefined): string;
} = (name: string, ...args: [] | [boolean | undefined]) => {
  const state = args.length > 0 ? args[0] : true;
  return name && state ? `${statePrefix}${name}` : '';
};

const useNamespace = (block: string) => {
  const namespace = DEFAULT_NAMESPACE;
  const b = (blockSuffix = '') => _bem(namespace, block, blockSuffix, '', '');
  const e = (element?: string) =>
    element ? _bem(namespace, block, '', element, '') : '';
  const m = (modifier?: string) =>
    modifier ? _bem(namespace, block, '', '', modifier) : '';
  const be = (blockSuffix?: string, element?: string) =>
    blockSuffix && element
      ? _bem(namespace, block, blockSuffix, element, '')
      : '';
  const em = (element?: string, modifier?: string) =>
    element && modifier ? _bem(namespace, block, '', element, modifier) : '';
  const bm = (blockSuffix?: string, modifier?: string) =>
    blockSuffix && modifier
      ? _bem(namespace, block, blockSuffix, '', modifier)
      : '';
  const bem = (blockSuffix?: string, element?: string, modifier?: string) =>
    blockSuffix && element && modifier
      ? _bem(namespace, block, blockSuffix, element, modifier)
      : '';

  // for css var
  // --el-xxx: value;
  const cssVar = (object: Record<string, string>) => {
    const styles: Record<string, string> = {};
    for (const key in object) {
      if (object[key]) {
        styles[`--${namespace}-${key}`] = object[key];
      }
    }
    return styles;
  };
  // with block
  const cssVarBlock = (object: Record<string, string>) => {
    const styles: Record<string, string> = {};
    for (const key in object) {
      if (object[key]) {
        styles[`--${namespace}-${block}-${key}`] = object[key];
      }
    }
    return styles;
  };

  const cssVarName = (name: string) => `--${namespace}-${name}`;
  const cssVarBlockName = (name: string) => `--${namespace}-${block}-${name}`;

  return {
    b,
    be,
    bem,
    bm,
    // css
    cssVar,
    cssVarBlock,
    cssVarBlockName,
    cssVarName,
    e,
    em,
    is,
    m,
    namespace,
  };
};

type UseNamespaceReturn = ReturnType<typeof useNamespace>;

export type { UseNamespaceReturn };
export { useNamespace };
