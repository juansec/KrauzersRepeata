package com.nyoho.krauzersrepeater;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParsedRequest {

    public String method;
    public String path;
    public String httpVersion;
    public String host;
    public String body;

    public Map<String, String> headers = new LinkedHashMap<>();


}
