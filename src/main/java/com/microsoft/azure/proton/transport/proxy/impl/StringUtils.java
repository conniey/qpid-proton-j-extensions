package com.microsoft.azure.proton.transport.proxy.impl;

class StringUtils {
    static final String NEW_LINE = "\r\n";

    static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
