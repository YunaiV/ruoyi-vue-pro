package cn.iocoder.yudao.framework.ai.midjourney.demo.jad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)

public class DiscordAccount extends DomainObject {

	
	private String guildId;
	
	private String channelId;
	
	private String userToken;
	
	private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;

	
	private boolean enable = true;

	
	private int coreSize = 3;
	
	private int queueSize = 10;
	
	private int timeoutMinutes = 5;

	@JsonIgnore
	public String getDisplay() {
		return this.channelId;
	}
}
