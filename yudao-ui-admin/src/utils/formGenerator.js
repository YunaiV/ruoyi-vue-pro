/**
 * 将服务端返回的 fields 字符串数组，解析成 JSON 数组
 *
 * @param fields JSON 字符串数组
 * @returns {*[]} JSON 数组
 */
export function decodeFields(fields) {
  const drawingList = []
  fields.forEach(item => {
    drawingList.push(JSON.parse(item))
  })
  return drawingList
}
