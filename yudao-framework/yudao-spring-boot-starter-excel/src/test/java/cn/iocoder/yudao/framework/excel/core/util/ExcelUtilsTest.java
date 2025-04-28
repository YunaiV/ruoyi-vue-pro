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
    void readCsv() throws IOException, ParseException {
        List<DemoData> list = ExcelUtils.read(new ClassPathResource("example.csv").getInputStream(), DemoData.class);
        assertFalse(list.isEmpty());
        DemoData data = list.getFirst();
        assertEquals(1, data.getAnInt());
        assertEquals(1.1, data.getADouble());
        assertEquals("hello", data.getString());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2023-10-01");
        assertEquals(date, data.getDate());
        assertTrue(data.isABoolean());
    }
}