package cn.iocoder.yudao.framework.ai.midjourney.wss;

import cn.iocoder.yudao.framework.ai.midjourney.jad.DiscordAccount;
import cn.iocoder.yudao.framework.ai.midjourney.wss.user.SpringUserWebSocketStarter;
import cn.iocoder.yudao.framework.ai.midjourney.wss.user.UserMessageListener;

import java.util.Scanner;

/**
 * author: fansili
 * time: 2024/4/3 16:40
 */
public class Main {

    public static void main(String[] args) {
        String token =  "NTY5MDY4NDAxNzEyOTU1Mzky.G4-Fu0.MzD-7ll-ElbXTTgDPHF-WS_UyhMAfbKN3WyyBc";


        DiscordHelper discordHelper = new DiscordHelper();
        DiscordAccount discordAccount = new DiscordAccount();
        discordAccount.setUserToken(token);
        discordAccount.setGuildId("1221445697157468200");
        discordAccount.setChannelId("1221445862962630706");


        var messageListener = new UserMessageListener();
        var webSocketStarter = new SpringUserWebSocketStarter(discordHelper.getWss(), null, discordAccount, messageListener);

        try {
            webSocketStarter.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
