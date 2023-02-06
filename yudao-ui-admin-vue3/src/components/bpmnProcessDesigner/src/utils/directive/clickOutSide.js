//outside.js

const ctx = '@@clickoutsideContext'

export default {
  bind(el, binding, vnode) {
    const ele = el
    const documentHandler = (e) => {
      if (!vnode.context || ele.contains(e.target)) {
        return false
      }
      // 调用指令回调
      if (binding.expression) {
        vnode.context[el[ctx].methodName](e)
      } else {
        el[ctx].bindingFn(e)
      }
    }
    // 将方法添加到ele
    ele[ctx] = {
      documentHandler,
      methodName: binding.expression,
      bindingFn: binding.value
    }

    setTimeout(() => {
      document.addEventListener('touchstart', documentHandler) // 为document绑定事件
    })
  },
  update(el, binding) {
    const ele = el
    ele[ctx].methodName = binding.expression
    ele[ctx].bindingFn = binding.value
  },
  unbind(el) {
    document.removeEventListener('touchstart', el[ctx].documentHandler) // 解绑
    delete el[ctx]
  }
}
