interface TreeHelperConfig {
  id: string;
  children: string;
  pid: string;
}

const DEFAULT_CONFIG: TreeHelperConfig = {
  id: 'id',
  children: 'children',
  pid: 'pid',
};
export const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  isLeaf: 'leaf',
  emitPath: false, // 用于 cascader 组件：在选中节点改变时，是否返回由该节点所在的各级菜单的值所组成的数组，若设置 false，则只返回该节点的值
};

const getConfig = (config: Partial<TreeHelperConfig>) =>
  Object.assign({}, DEFAULT_CONFIG, config);

// tree from list
export const listToTree = <T = any>(
  list: any[],
  config: Partial<TreeHelperConfig> = {},
): T[] => {
  const conf = getConfig(config) as TreeHelperConfig;
  const nodeMap = new Map();
  const result: T[] = [];
  const { id, children, pid } = conf;

  for (const node of list) {
    node[children] = node[children] || [];
    nodeMap.set(node[id], node);
  }
  for (const node of list) {
    const parent = nodeMap.get(node[pid]);
    (parent ? parent.children : result).push(node);
  }
  return result;
};

export const treeToList = <T = any>(
  tree: any,
  config: Partial<TreeHelperConfig> = {},
): T => {
  config = getConfig(config);
  const { children } = config;
  const result: any = [...tree];
  for (let i = 0; i < result.length; i++) {
    const childNodes = result[i][children];
    if (!childNodes) continue;
    result.splice(i + 1, 0, ...childNodes);
  }
  return result;
};

export const findNode = <T = any>(
  tree: any,
  func: Fn,
  config: Partial<TreeHelperConfig> = {},
): null | T => {
  config = getConfig(config);
  const { children } = config;
  const list = [...tree];
  for (const node of list) {
    if (func(node)) return node;
    const childNodes = node[children];
    if (childNodes) {
      list.push(...childNodes);
    }
  }
  return null;
};

export const findNodeAll = <T = any>(
  tree: any,
  func: Fn,
  config: Partial<TreeHelperConfig> = {},
): T[] => {
  config = getConfig(config);
  const { children } = config;
  const list = [...tree];
  const result: T[] = [];
  for (const node of list) {
    func(node) && result.push(node);
    const childNodes = node[children];
    if (childNodes) {
      list.push(...childNodes);
    }
  }
  return result;
};

export const findPath = <T = any>(
  tree: any,
  func: Fn,
  config: Partial<TreeHelperConfig> = {},
): null | T | T[] => {
  config = getConfig(config);
  const path: T[] = [];
  const list = [...tree];
  const visitedSet = new Set();
  const { children } = config;
  while (list.length > 0) {
    const node = list[0];
    if (visitedSet.has(node)) {
      path.pop();
      list.shift();
    } else {
      visitedSet.add(node);
      const childNodes = node[children];
      if (childNodes) {
        list.unshift(...childNodes);
      }
      path.push(node);
      if (func(node)) {
        return path;
      }
    }
  }
  return null;
};

export const findPathAll = (
  tree: any,
  func: Fn,
  config: Partial<TreeHelperConfig> = {},
) => {
  config = getConfig(config);
  const path: any[] = [];
  const list = [...tree];
  const result: any[] = [];
  const visitedSet = new Set();
  const { children } = config;
  while (list.length > 0) {
    const node = list[0];
    if (visitedSet.has(node)) {
      path.pop();
      list.shift();
    } else {
      visitedSet.add(node);
      const childNodes = node[children];
      if (childNodes) {
        list.unshift(...childNodes);
      }
      path.push(node);
      func(node) && result.push([...path]);
    }
  }
  return result;
};

export const filter = <T = any>(
  tree: T[],
  func: (n: T) => boolean,
  config: Partial<TreeHelperConfig> = {},
): T[] => {
  config = getConfig(config);
  const children = config.children as string;

  function listFilter(list: T[]) {
    return list
      .map((node: any) => ({ ...node }))
      .filter((node) => {
        node[children] = node[children] && listFilter(node[children]);
        return func(node) || node[children]?.length > 0;
      });
  }

  return listFilter(tree);
};

export const forEach = <T = any>(
  tree: T[],
  func: (n: T) => any,
  config: Partial<TreeHelperConfig> = {},
): void => {
  config = getConfig(config);
  const list: any[] = [...tree];
  const { children } = config;
  for (let i = 0; i < list.length; i++) {
    // func 返回true就终止遍历，避免大量节点场景下无意义循环，引起浏览器卡顿
    if (func(list[i])) {
      return;
    }
    children &&
      list[i][children] &&
      list.splice(i + 1, 0, ...list[i][children]);
  }
};

/**
 * @description: Extract tree specified structure
 */
export const treeMap = <T = any>(
  treeData: T[],
  opt: { children?: string; conversion: Fn },
): T[] => {
  return treeData.map((item) => treeMapEach(item, opt));
};

/**
 * @description: Extract tree specified structure
 */
export const treeMapEach = (
  data: any,
  { children = 'children', conversion }: { children?: string; conversion: Fn },
) => {
  const haveChildren =
    Array.isArray(data[children]) && data[children].length > 0;
  const conversionData = conversion(data) || {};
  return haveChildren
    ? {
        ...conversionData,
        [children]: data[children].map((i: number) =>
          treeMapEach(i, {
            children,
            conversion,
          }),
        ),
      }
    : {
        ...conversionData,
      };
};

/**
 * 递归遍历树结构
 * @param treeDatas 树
 * @param callBack 回调
 * @param parentNode 父节点
 */
export const eachTree = (treeDatas: any[], callBack: Fn, parentNode = {}) => {
  treeDatas.forEach((element) => {
    const newNode = callBack(element, parentNode) || element;
    if (element.children) {
      eachTree(element.children, callBack, newNode);
    }
  });
};

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
export const handleTree = (
  data: any[],
  id?: string,
  parentId?: string,
  children?: string,
) => {
  if (!Array.isArray(data)) {
    console.warn('data must be an array');
    return [];
  }
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children',
  };

  const childrenListMap = {};
  const nodeIds = {};
  const tree: any[] = [];

  for (const d of data) {
    const parentId = d[config.parentId];
    if (
      childrenListMap[parentId] === null ||
      childrenListMap[parentId] === undefined
    ) {
      childrenListMap[parentId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[parentId].push(d);
  }

  for (const d of data) {
    const parentId = d[config.parentId];
    if (nodeIds[parentId] === null || nodeIds[parentId] === undefined) {
      tree.push(d);
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t);
  }

  function adaptToChildrenList(o) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]];
    }
    if (o[config.childrenList]) {
      for (const c of o[config.childrenList]) {
        adaptToChildrenList(c);
      }
    }
  }

  return tree;
};

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 * @param {*} rootId 根Id 默认 0
 */
// @ts-ignore: 遗留函数，保持原有逻辑不变
export const handleTree2 = (data, id, parentId, children, rootId) => {
  id = id || 'id';
  parentId = parentId || 'parentId';
  // children = children || 'children'
  rootId =
    rootId ||
    Math.min(
      ...data.map((item) => {
        return item[parentId];
      }),
    ) ||
    0;
  // 对源数据深度克隆
  const cloneData = structuredClone(data);
  // 循环所有项
  const treeData = cloneData.filter((father) => {
    const branchArr = cloneData.filter((child) => {
      // 返回每一项的子级数组
      return father[id] === child[parentId];
    });
    branchArr.length > 0 ? (father.children = branchArr) : '';
    // 返回第一层
    return father[parentId] === rootId;
  });
  return treeData === '' ? data : treeData;
};

/**
 * 校验选中的节点，是否为指定 level
 *
 * @param tree 要操作的树结构数据
 * @param nodeId 需要判断在什么层级的数据
 * @param level 检查的级别, 默认检查到二级
 * @return true 是；false 否
 */
export const checkSelectedNode = (
  tree: any[],
  nodeId: any,
  level = 2,
): boolean => {
  if (tree === undefined || !Array.isArray(tree) || tree.length === 0) {
    console.warn('tree must be an array');
    return false;
  }

  // 校验是否是一级节点
  if (tree.some((item) => item.id === nodeId)) {
    return false;
  }

  // 递归计数
  let count = 1;

  // 深层次校验
  function performAThoroughValidation(arr: any[]): boolean {
    count += 1;
    for (const item of arr) {
      if (item.id === nodeId) {
        return true;
      } else if (
        item.children !== undefined &&
        item.children.length > 0 &&
        performAThoroughValidation(item.children)
      ) {
        return true;
      }
    }
    return false;
  }

  for (const item of tree) {
    count = 1;
    if (
      performAThoroughValidation(item.children) && // 找到后对比是否是期望的层级
      count >= level
    ) {
      return true;
    }
  }

  return false;
};

/**
 * 获取节点的完整结构
 * @param tree 树数据
 * @param nodeId 节点 id
 */
export const treeToString = (tree: any[], nodeId) => {
  if (tree === undefined || !Array.isArray(tree) || tree.length === 0) {
    console.warn('tree must be an array');
    return '';
  }
  // 校验是否是一级节点
  const node = tree.find((item) => item.id === nodeId);
  if (node !== undefined) {
    return node.name;
  }
  let str = '';

  function performAThoroughValidation(arr) {
    if (arr === undefined || !Array.isArray(arr) || arr.length === 0) {
      return false;
    }
    for (const item of arr) {
      if (item.id === nodeId) {
        str += ` / ${item.name}`;
        return true;
      } else if (item.children !== undefined && item.children.length > 0) {
        str += ` / ${item.name}`;
        if (performAThoroughValidation(item.children)) {
          return true;
        }
      }
    }
    return false;
  }

  for (const item of tree) {
    str = `${item.name}`;
    if (performAThoroughValidation(item.children)) {
      break;
    }
  }
  return str;
};
