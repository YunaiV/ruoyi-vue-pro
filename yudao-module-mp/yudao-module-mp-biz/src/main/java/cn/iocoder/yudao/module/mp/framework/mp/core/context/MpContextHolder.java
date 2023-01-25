/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package cn.iocoder.yudao.module.mp.framework.mp.core.context;

import cn.iocoder.yudao.module.mp.controller.admin.open.vo.MpOpenHandleMessageReqVO;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;

/**
 * 微信上下文 Context
 *
 * 目的：解决微信多公众号的问题，在 {@link WxMpMessageHandler} 实现类中，可以通过 {@link #getAppId()} 获取到当前的 appId
 *
 * @see cn.iocoder.yudao.module.mp.controller.admin.open.MpOpenController#handleMessage(String, String, MpOpenHandleMessageReqVO)
 *
 * @author 芋道源码
 */
public class MpContextHolder {

    /**
     * 微信公众号的 appId 上下文
     */
	private static final ThreadLocal<String> APPID = new TransmittableThreadLocal<>();

	public static void setAppId(String appId) {
		APPID.set(appId);
	}

	public static String getAppId() {
		return APPID.get();
	}

	public static void clear() {
		APPID.remove();
	}

}
