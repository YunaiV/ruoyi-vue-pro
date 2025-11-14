// #ifdef H5
import service from './officialAccount';
// #endif

// #ifdef MP-WEIXIN
import service from './miniProgram';
// #endif

// #ifdef APP-PLUS
import service from './openPlatform';
// #endif

const wechat = service;

export default wechat;
