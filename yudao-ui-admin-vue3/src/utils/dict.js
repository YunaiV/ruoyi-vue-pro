import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({});
  return (() => {
    args.forEach((d, index) => {
      res.value[d] = [];
      getDicts(d).then(resp => {
        res.value[d] = resp.data.map(p => ({ label: p.dictLabel, value: p.dictValue, elTagType: p.listClass }))
      })
    })
    return toRefs(res.value);
  })()
}