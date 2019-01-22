package com.nono.apkkiller.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunTimeUtil {

    public static String call(String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        InputStream is = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer stringBuffer = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            stringBuffer.append(line).append("\n");
        }
        return stringBuffer.toString();
    }
}