import constant from './constant'

// 存储变量名
let storageKey = 'storage_data'

// 存储节点变量名
let storageNodeKeys = [constant.avatar, constant.name, constant.roles, constant.permissions]

// 存储的数据
let storageData = uni.getStorageSync(storageKey) || {}

const storage = {
  set: function(key, value) {
    if (storageNodeKeys.indexOf(key) != -1) {
      let tmp = uni.getStorageSync(storageKey)
      tmp = tmp ? tmp : {}
      tmp[key] = value
      uni.setStorageSync(storageKey, tmp)
    }
  },
  get: function(key) {
    return storageData[key] || ""
  },
  remove: function(key) {
    delete storageData[key]
    uni.setStorageSync(storageKey, storageData)
  },
  clean: function() {
    uni.removeStorageSync(storageKey)
  }
}

export default storage
