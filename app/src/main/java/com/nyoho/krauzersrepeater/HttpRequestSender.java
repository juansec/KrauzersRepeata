package com.nyoho.krauzersrepeater;

import java.util.Map;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import java.util.concurrent.TimeUnit;

public class HttpRequestSender {

    private static final OkHttpClient client =
            new OkHttpClient.Builder()


                    .followRedirects(false)
                    .followSslRedirects(false)


                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .callTimeout(30, TimeUnit.SECONDS)

                    .build();


    public static void sendRequest(ParsedRequest parsed, boolean useHttps, Callback callback) {

        if (parsed.host == null || parsed.host.isEmpty()) {
            throw new IllegalArgumentException("Requesta sin host papu, nonas.");
        }

        if (parsed.host.matches(
                ".*[\\s/@?#\\\\].*"
        )) {

            throw new IllegalArgumentException(
                    "Host inválido: "
                            + parsed.host
            );
        }

        String scheme = useHttps ? "https://" : "http://";
        String url = scheme + parsed.host + parsed.path;

        Request.Builder builder = new Request.Builder().url(url);

        String method = parsed.method.toUpperCase();

        String contentType = "application/octet-stream";

        for (Map.Entry<String, String> header : parsed.headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase("Content-Type")) {
                contentType = header.getValue();
                break;
            }
        }

        MediaType mediaType = MediaType.parse(contentType);

        RequestBody requestBody = null;

        if (parsed.body != null && !parsed.body.isEmpty()) {
            requestBody = RequestBody.create(parsed.body, mediaType);
        }

        switch (method) {
            case "GET":
                builder.get();
                break;

            case "HEAD":
                builder.head();
                break;

            case "POST":
            case "PUT":
            case "PATCH":

                if (requestBody == null) {
                    requestBody = RequestBody.create("", mediaType);
                }

                builder.method(method, requestBody);
                break;

            case "DELETE":

                if (requestBody != null) {
                    builder.method("DELETE", requestBody);
                } else {
                    builder.delete();
                }

                break;


            case "OPTIONS":
                builder.method("OPTIONS", requestBody);
                break;

            default:

                throw new IllegalArgumentException(
                        "Q paso wachin? El metodo es invalidooo: " + method
                );

        }

        for (Map.Entry<String, String> header : parsed.headers.entrySet()) {

            String name = header.getKey();
            String value = header.getValue();

            if (name.equalsIgnoreCase("Host")) {
                continue;
            }

            if (name.equalsIgnoreCase("Content-Length")) {
                continue;
            }

            builder.addHeader(name, value);
        }

        Request request = builder.build();

        client.newCall(request).enqueue(callback);
    }
}
