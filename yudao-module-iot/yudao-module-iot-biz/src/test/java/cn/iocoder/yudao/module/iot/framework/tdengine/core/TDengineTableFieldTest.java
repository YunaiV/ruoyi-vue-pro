package cn.iocoder.yudao.module.iot.framework.tdengine.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TDengineTableFieldTest {

    @Test
    public void testBuildFieldName() {
        assertEquals("ua", TDengineTableField.buildFieldName("Ua"));
        assertEquals("pf_t", TDengineTableField.buildFieldName("PfT"));
        assertEquals("pt", TDengineTableField.buildFieldName("PT"));
        assertEquals("pa", TDengineTableField.buildFieldName("PA"));
        assertEquals("geo_location", TDengineTableField.buildFieldName("GeoLocation"));
        assertEquals("light_status", TDengineTableField.buildFieldName("LightStatus"));
    }

    @Test
    public void testBuildFieldName_blank() {
        assertNull(TDengineTableField.buildFieldName(null));
        assertEquals("", TDengineTableField.buildFieldName(""));
        assertEquals(" ", TDengineTableField.buildFieldName(" "));
    }

}
