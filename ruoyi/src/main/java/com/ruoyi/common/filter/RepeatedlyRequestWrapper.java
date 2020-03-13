package com.ruoyi.common.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * 构建可重复读取inputStream的request
 * 
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper
{
    private final byte[] body;

    public RepeatedlyRequestWrapper(HttpServletRequest request) throws IOException
    {
        super(request);
        body = readBytes(request.getReader(), "utf-8");
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream()
        {

            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener)
            {

            }

            @Override
            public int read() throws IOException
            {
                return bais.read();
            }
        };
    }

    /**
     * 通过BufferedReader和字符编码集转换成byte数组
     */
    private byte[] readBytes(BufferedReader br, String encoding) throws IOException
    {
        String str = null, retStr = "";
        while ((str = br.readLine()) != null)
        {
            retStr += str;
        }
        if (StringUtils.isNotBlank(retStr))
        {
            return retStr.getBytes(Charset.forName(encoding));
        }
        return null;
    }
}
