// uniapp在H5中各API的z-index值如下：
/**
 * actionsheet: 999
 * modal: 999
 * navigate: 998
 * tabbar: 998
 * toast: 999
 */

export default {
  toast: 10090,
  noNetwork: 10080,
  popup: 10075, // popup包含popup，actionsheet，keyboard，picker的值
  mask: 10070,
  navbar: 980,
  topTips: 975,
  sticky: 970,
  indexListSticky: 965,
  popover: 960,
};
