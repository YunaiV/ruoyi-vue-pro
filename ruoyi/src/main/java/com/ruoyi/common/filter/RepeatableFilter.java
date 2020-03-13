package com.ruoyi.common.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.ruoyi.common.enums.HttpMethod;

/**
 * Repeatable 过滤器
 * 
 * @author ruoyi
 */
public class RepeatableFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        if (HttpMethod.PUT.name().equals(req.getMethod()) || HttpMethod.POST.name().equals(req.getMethod()))
        {
            RepeatedlyRequestWrapper repeatedlyRequest = new RepeatedlyRequestWrapper((HttpServletRequest) request);
            chain.doFilter(repeatedlyRequest, response);
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy()
    {

    }
}
