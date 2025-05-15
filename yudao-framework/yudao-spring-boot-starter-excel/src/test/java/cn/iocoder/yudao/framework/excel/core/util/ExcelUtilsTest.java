package cn.iocoder.yudao.framework.excel.core.util;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilsTest {

    @Test
    void readCsvAsJavaDataBean() throws IOException, ParseException {
        List<JavaDataBean> list = ExcelUtils.read(new ClassPathResource("example.csv").getInputStream(), JavaDataBean.class);
        assertFalse(list.isEmpty());
        JavaDataBean data = list.get(0);
        assertEquals(1.1, data.getNumber());
        assertEquals("hello", data.getString());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2023-10-01");
        assertEquals(date, data.getDate());
        assertTrue(data.isABoolean());
    }

    @Test
    void readCsvAsLombokDataBean() throws IOException, ParseException {
        List<LombokDataBean> list = ExcelUtils.read(new ClassPathResource("example.csv").getInputStream(), LombokDataBean.class);
        assertFalse(list.isEmpty());
        LombokDataBean data = list.get(0);
        assertEquals(1.1, data.getNumber());
        assertEquals("hello", data.getString());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2023-10-01");
        assertEquals(date, data.getDate());
        assertTrue(data.getAreYouOk());
    }
}