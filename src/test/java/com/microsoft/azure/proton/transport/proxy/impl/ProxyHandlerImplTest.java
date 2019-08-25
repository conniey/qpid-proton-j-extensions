/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.proton.transport.proxy.impl;

import com.microsoft.azure.proton.transport.proxy.HttpStatusLine;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.ProxyResponse;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static com.microsoft.azure.proton.transport.proxy.impl.StringUtils.NEW_LINE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyHandlerImplTest {
    @Test
    public void testCreateProxyRequest() {
        final String hostName = "testHostName";
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("header1", "headervalue1");
        headers.put("header2", "headervalue2");

        final ProxyHandlerImpl proxyHandler = new ProxyHandlerImpl();
        final String actualProxyRequest = proxyHandler.createProxyRequest(hostName, headers);

        final String expectedProxyRequest = "CONNECT testHostName HTTP/1.1\r\n" +
                "Host: testHostName\r\n" +
                "Connection: Keep-Alive\r\n" +
                "header2: headervalue2\r\n" +
                "header1: headervalue1\r\n" +
                "\r\n";

        Assert.assertEquals(expectedProxyRequest, actualProxyRequest);
    }

    @Test
    public void testValidateProxyResponseOnSuccess() {
        // Arrange
        final HttpStatusLine statusLine = HttpStatusLine.create("HTTP/1.1 200 Connection Established");
        final ProxyResponse response = mock(ProxyResponse.class);
        when(response.isMissingContent()).thenReturn(false);
        when(response.getStatus()).thenReturn(statusLine);
        final ProxyHandlerImpl proxyHandler = new ProxyHandlerImpl();

        // Act
        final ProxyHandler.ProxyResponseResult responseResult = proxyHandler.validateProxyResponse(response);

        // Assert
        Assert.assertTrue(responseResult.isSuccess());
        Assert.assertSame(response, responseResult.getResponse());
        Assert.assertNull(responseResult.getError());
    }

    @Test
    public void testValidateProxyResponseOnFailure() {
        // Arrange
        final HttpStatusLine statusLine = HttpStatusLine.create("HTTP/1.1 407 Proxy Auth Required");
        final String contents = "<html><body>[Fiddler] Proxy Authentication Required.<BR></body></html>";
        final ByteBuffer encoded = UTF_8.encode(contents);
        final ProxyResponse response = mock(ProxyResponse.class);
        when(response.isMissingContent()).thenReturn(false);
        when(response.getStatus()).thenReturn(statusLine);
        when(response.getContents()).thenReturn(encoded);
        when(response.getError()).thenReturn(contents);

        final ProxyHandlerImpl proxyHandler = new ProxyHandlerImpl();

        // Act
        final ProxyHandler.ProxyResponseResult responseResult = proxyHandler.validateProxyResponse(response);

        // Assert
        Assert.assertFalse(responseResult.isSuccess());
        Assert.assertEquals(contents, responseResult.getError());
    }

    @Test
    public void testValidateProxyResponseOnEmptyResponse() {
        final String emptyResponse = NEW_LINE + NEW_LINE;
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(emptyResponse.getBytes(UTF_8));
        buffer.flip();

        final ProxyResponse response = mock(ProxyResponse.class);
        when(response.isMissingContent()).thenReturn(false);
        when(response.getStatus()).thenReturn(null);
        when(response.getContents()).thenReturn(buffer);
        when(response.getError()).thenReturn(emptyResponse);

        final ProxyHandlerImpl proxyHandler = new ProxyHandlerImpl();

        // Act
        ProxyHandler.ProxyResponseResult responseResult = proxyHandler.validateProxyResponse(response);

        // Assert
        Assert.assertFalse(responseResult.isSuccess());
        Assert.assertEquals(emptyResponse, responseResult.getError());
        Assert.assertSame(buffer, response.getContents());
    }

}
