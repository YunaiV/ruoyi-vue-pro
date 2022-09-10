/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 * @param {*} rootId 根Id 默认 0
 */
export function handleTree(data, id, parentId, children, rootId) {
  id = id || 'id'
  parentId = parentId || 'parentId'
  children = children || 'children'
  rootId = rootId || Math.min.apply(Math, data.map(item => {
    return item[parentId]
  })) || 0
  //对源数据深度克隆
  const cloneData = JSON.parse(JSON.stringify(data))
  //循环所有项
  const treeData = cloneData.filter(father => {
    let branchArr = cloneData.filter(child => {
      //返回每一项的子级数组
      return father[id] === child[parentId]
    });
    branchArr.length > 0 ? father.children = branchArr : '';
    //返回第一层
    return father[parentId] === rootId;
  });
  return treeData !== '' ? treeData : data;
}

/**
 * 树形结构进行删除深度不够的分支
 * 目前只删除了不够三层的分支
 * 对于高于三层的部分目前不做处理
 * todo 暴力遍历，可用递归修改
 * @param {*} data 树形结构
 */
export function convertTree(data) {
   //对源数据深度克隆
  const cloneData = JSON.parse(JSON.stringify(data))
  // 遍历克隆数据，对源数据进行删除操作
  for (let first = 0; first < cloneData.length; first++) {
    for (let second = 0; second < cloneData[first].children.length; second++) {
      for (let three = 0; three < cloneData[first].children[second].children.length; three++) {
        if (data[first].children[second].children[three].children == undefined ||
          data[first].children[second].children[three].children === 0) {
          data[first].children[second].children.splice(second, 1);
        }
      }
      if (data[first].children[second].children == undefined || data[first].children[second].children === 0) {
        data[first].children.splice(second, 1);
      }
    }
    if (data[first].children == undefined || data[first].children.length === 0) {
      data.splice(first, 1);
    }
  }
  return data;
}
