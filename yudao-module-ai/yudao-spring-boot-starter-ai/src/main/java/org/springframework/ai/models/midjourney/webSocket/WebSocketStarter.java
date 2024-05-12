package org.springframework.ai.models.midjourney.webSocket;


public interface WebSocketStarter {

	void start(WssNotify wssNotify) throws Exception;

}
