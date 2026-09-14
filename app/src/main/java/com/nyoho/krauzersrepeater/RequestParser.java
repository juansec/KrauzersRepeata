package com.nyoho.krauzersrepeater;

public class RequestParser {

    public static ParsedRequest parse(String rawRequest) {

        ParsedRequest result = new ParsedRequest();


        String[] lines = rawRequest.split("\\r?\\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("empty requesta may frend");
        }

        String firstLine = lines[0].trim();

        String[] parts = firstLine.split("\\s+");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Primera linea HTTP invalida papu: " + firstLine);
        }

        result.method = parts[0];
        result.path = parts[1];
        result.httpVersion = parts[2];

        int bodyStartIndex = -1;

        for (int i = 1; i < lines.length; i++) {

            String line = lines[i];

            if (line.trim().isEmpty()) {
                bodyStartIndex = i + 1;
                break;
            }

            int colonIndex = line.indexOf(':');

            if (colonIndex == -1) {
                throw new IllegalArgumentException("Header invalido papu Uwu: " + line);
            }

            String headerName = line.substring(0, colonIndex).trim();
            String headerValue = line.substring(colonIndex + 1).trim();

            result.headers.put(headerName, headerValue);

            if (headerName.equalsIgnoreCase("Host")) {
                result.host = headerValue;
            }

        }

        if (bodyStartIndex != -1 && bodyStartIndex < lines.length) {

            StringBuilder bodyBuilder = new StringBuilder();

            for (int i = bodyStartIndex; i < lines.length; i++) {

                bodyBuilder.append(lines[i]);

                if (i < lines.length - 1) {
                    bodyBuilder.append("\n");
                }
            }

            result.body = bodyBuilder.toString();

        } else {
            result.body = "";
        }

        return result;
    }
}
