package cn.iocoder.yudao.framework.ai.midjourney.demo.wss;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordHelper {
	/**
	 * DISCORD_SERVER_URL.
	 */
	public static final String DISCORD_SERVER_URL = "https://discord.com";
	/**
	 * DISCORD_CDN_URL.
	 */
	public static final String DISCORD_CDN_URL = "https://cdn.discordapp.com";
	/**
	 * DISCORD_WSS_URL.
	 */
	public static final String DISCORD_WSS_URL = "wss://gateway.discord.gg";
	/**
	 * DISCORD_UPLOAD_URL.
	 */
	public static final String DISCORD_UPLOAD_URL = "https://discord-attachments-uploads-prd.storage.googleapis.com";

	public String getServer() {
		return DISCORD_SERVER_URL;
	}

	public String getCdn() {
		return DISCORD_CDN_URL;
	}

	public String getWss() {
		return DISCORD_WSS_URL;
	}

	public String getMessageHash(String imageUrl) {
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return null;
		}
		if (CharSequenceUtil.endWith(imageUrl, "_grid_0.webp")) {
			int hashStartIndex = imageUrl.lastIndexOf("/");
			if (hashStartIndex < 0) {
				return null;
			}
			return CharSequenceUtil.sub(imageUrl, hashStartIndex + 1, imageUrl.length() - "_grid_0.webp".length());
		}
		int hashStartIndex = imageUrl.lastIndexOf("_");
		if (hashStartIndex < 0) {
			return null;
		}
		return CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
	}

}
