package cn.iocoder.yudao.framework.ai.midjourney;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.ai.midjourney.interactions.MidjourneyInteractions;
import cn.iocoder.yudao.framework.ai.midjourney.vo.Attachments;
import cn.iocoder.yudao.framework.ai.midjourney.vo.Describe;
import cn.iocoder.yudao.framework.ai.midjourney.vo.ReRoll;
import cn.iocoder.yudao.framework.ai.midjourney.vo.UploadAttachmentsRes;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mj
 *
 * author: fansili
 * time: 2024/4/4 18:59
 */
public class MidjourneyInteractionsTests {

    private MidjourneyConfig midjourneyConfig;
    @Before
    public void setup() {
        String token =  "OTcyNzIxMzA0ODkxNDUzNDUw.G_vMOz.BO_Q0sXAD80u5ZKIHPNYDTRX_FgeKL3cKFc53I";
        Map<String, String> requestTemplates = new HashMap<>();
        List<File> files = FileUtil.loopFiles("/Users/fansili/projects/github/ruoyi-vue-pro/yudao-module-ai/yudao-spring-boot-starter-ai/src/main/resources/http-body");
        for (File file : files) {
            requestTemplates.put(file.getName().replace(".json", ""), FileUtil.readUtf8String(file));
        }
        this.midjourneyConfig = new MidjourneyConfig(token, "1225608134878302329", "1225608134878302332", requestTemplates);
    }

    @Test
    public void mjImageTest() {
        MidjourneyInteractions mjImagineInteractions = new MidjourneyInteractions(midjourneyConfig);
        mjImagineInteractions.imagine("童话里应该是什么样子？");
    }


    @Test
    public void reRollTest() {
        MidjourneyInteractions mjImagineInteractions = new MidjourneyInteractions(midjourneyConfig);
        mjImagineInteractions.reRoll(new ReRoll()
                .setMessageId("1226165117448753243")
                .setCustomId("MJ::JOB::upsample::3::2aeefbef-43e2-4057-bcf1-43b5f39ab6f7"));
    }

    @Test
    public void uploadAttachmentsTest() {
        MidjourneyInteractions mjImagineInteractions = new MidjourneyInteractions(midjourneyConfig);
        UploadAttachmentsRes res = mjImagineInteractions.uploadAttachments(
                new Attachments().setFileSystemResource(
                        new FileSystemResource(new File("/Users/fansili/Downloads/DSC01402.JPG")))
        );
        System.err.println(JSON.toJSONString(res));
    }

    @Test
    public void describeTest() {
        MidjourneyInteractions mjImagineInteractions = new MidjourneyInteractions(midjourneyConfig);
        mjImagineInteractions.describe(new Describe()
                .setFileName("DSC01402.JPG")
                .setFinalFileName("16826931-2873-45ec-8cfb-0ad81f1a075f/DSC01402.JPG")
        );
    }
}
