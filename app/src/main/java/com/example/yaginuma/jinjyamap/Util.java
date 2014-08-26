package com.example.yaginuma.jinjyamap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by yaginuma on 14/08/20.
 */
public class Util {
    public static String encode(String target) {
        String result = "";
        try {
            result = URLEncoder.encode(target, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // nothing to do
        }
        return result;
    }
}

