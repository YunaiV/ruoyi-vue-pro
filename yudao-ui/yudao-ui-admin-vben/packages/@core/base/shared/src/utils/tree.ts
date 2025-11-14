interface TreeConfigOptions {
  // 子属性的名称，默认为'children'
  childProps: string;
}

interface TreeNode {
  [key: string]: any;
  children?: TreeNode[];
}

/**
 * @zh_CN 遍历树形结构，并返回所有节点中指定的值。
 * @param tree 树形结构数组
 * @param getValue 获取节点值的函数
 * @param options 作为子节点数组的可选属性名称。
 * @returns 所有节点中指定的值的数组
 */
function traverseTreeValues<T, V>(
  tree: T[],
  getValue: (node: T) => V,
  options?: TreeConfigOptions,
): V[] {
  const result: V[] = [];
  const { childProps } = options || {
    childProps: 'children',
  };

  const dfs = (treeNode: T) => {
    const value = getValue(treeNode);
    result.push(value);
    const children = (treeNode as Record<string, any>)?.[childProps];
    if (!children) {
      return;
    }
    if (children.length > 0) {
      for (const child of children) {
        dfs(child);
      }
    }
  };

  for (const treeNode of tree) {
    dfs(treeNode);
  }
  return result.filter(Boolean);
}

/**
 * 根据条件过滤给定树结构的节点，并以原有顺序返回所有匹配节点的数组。
 * @param tree 要过滤的树结构的根节点数组。
 * @param filter 用于匹配每个节点的条件。
 * @param options 作为子节点数组的可选属性名称。
 * @returns 包含所有匹配节点的数组。
 */
function filterTree<T extends Record<string, any>>(
  tree: T[],
  filter: (node: T) => boolean,
  options?: TreeConfigOptions,
): T[] {
  const { childProps } = options || {
    childProps: 'children',
  };

  const _filterTree = (nodes: T[]): T[] => {
    return nodes.filter((node: Record<string, any>) => {
      if (filter(node as T)) {
        if (node[childProps]) {
          node[childProps] = _filterTree(node[childProps]);
        }
        return true;
      }
      return false;
    });
  };

  return _filterTree(tree);
}

/**
 * 根据条件重新映射给定树结构的节
 * @param tree 要过滤的树结构的根节点数组。
 * @param mapper 用于map每个节点的条件。
 * @param options 作为子节点数组的可选属性名称。
 */
function mapTree<T, V extends Record<string, any>>(
  tree: T[],
  mapper: (node: T) => V,
  options?: TreeConfigOptions,
): V[] {
  const { childProps } = options || {
    childProps: 'children',
  };
  return tree.map((node) => {
    const mapperNode: Record<string, any> = mapper(node);
    if (mapperNode[childProps]) {
      mapperNode[childProps] = mapTree(mapperNode[childProps], mapper, options);
    }
    return mapperNode as V;
  });
}

/**
 * 构造树型结构数据
 *
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
function handleTree(
  data: TreeNode[],
  id: string = 'id',
  parentId: string = 'parentId',
  children: string = 'children',
): TreeNode[] {
  if (!Array.isArray(data)) {
    console.warn('data must be an array');
    return [];
  }
  const config = {
    id,
    parentId,
    childrenList: children,
  };
  const childrenListMap: Record<number | string, TreeNode[]> = {};
  const nodeIds: Record<number | string, TreeNode> = {};
  const tree: TreeNode[] = [];

  // 1. 数据预处理
  // 1.1 第一次遍历，生成 childrenListMap 和 nodeIds 映射
  for (const d of data) {
    const pId = d[config.parentId];
    if (childrenListMap[pId] === undefined) {
      childrenListMap[pId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[pId].push(d);
  }
  // 1.2 第二次遍历，找出根节点
  for (const d of data) {
    const pId = d[config.parentId];
    if (nodeIds[pId] === undefined) {
      tree.push(d);
    }
  }

  // 2. 构建树结：递归构建子节点
  const adaptToChildrenList = (node: TreeNode): void => {
    const nodeId = node[config.id];
    if (childrenListMap[nodeId]) {
      node[config.childrenList] = childrenListMap[nodeId];
      // 递归处理子节点
      for (const child of node[config.childrenList]) {
        adaptToChildrenList(child);
      }
    }
  };

  // 3. 从根节点开始构建完整树
  for (const rootNode of tree) {
    adaptToChildrenList(rootNode);
  }

  return tree;
}

/**
 * 获取节点的完整结构
 * @param tree 树数据
 * @param nodeId 节点 id
 */
function treeToString(tree: any[], nodeId: number | string) {
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

  function performAThoroughValidation(arr: any[]) {
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
}

export { filterTree, handleTree, mapTree, traverseTreeValues, treeToString };
