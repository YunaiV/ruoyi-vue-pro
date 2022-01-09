/**
 * 将毫秒，转换成时间字符串。例如说，xx 分钟
 *
 * @param ms 毫秒
 * @returns {string} 字符串
 */
export function getDate(ms) {
  const day = Math.floor(ms / (24 * 60 * 60 * 1000));
  const hour =  Math.floor((ms / (60 * 60 * 1000) - day * 24));
  const minute =  Math.floor(((ms / (60 * 1000)) - day * 24 * 60 - hour * 60));
  const second =  Math.floor((ms / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60));
  if (day > 0) {
    return day + "天" + hour + "小时" + minute + "分钟";
  }
  if (hour > 0) {
    return hour + "小时" + minute + "分钟";
  }
  if (minute > 0) {
    return minute + "分钟";
  }
  if (second > 0) {
    return second + "秒";
  } else {
    return 0 + "秒";
  }
}
