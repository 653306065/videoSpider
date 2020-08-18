package com.spider.utils;

import okhttp3.*;

import java.io.InputStream;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OKHttpUtils {

    static OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

    static OkHttpClient proxyHttpClient = null;

    static {
        Proxy proxy = SpringContentUtil.getBean(Proxy.class);
        proxyHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
    }

    public static String get(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String get(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            Request request = builder.get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getRedirectUrl(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String redirectUrl = response.request().url().toString();
                response.body().close();
                response.close();
                return redirectUrl;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream getInputStream(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                return response.body().byteStream();
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Response getResponse(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            if (header != null) {
                for (String key : header.keySet()) {
                    builder.addHeader(key, header.get(key));
                }
            }
            Request request = builder.get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream getInputStream(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            if (header != null) {
                for (String key : header.keySet()) {
                    builder.addHeader(key, header.get(key));
                }
            }
            Request request = builder.get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                return response.body().byteStream();
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytes(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                byte[] bytes = response.body().bytes();
                response.body().close();
                response.close();
                return bytes;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytes(String url) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] bytes = response.body().bytes();
                response.body().close();
                response.close();
                return bytes;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytes(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            if (header != null) {
                for (String key : header.keySet()) {
                    builder.addHeader(key, header.get(key));
                }
            }
            Request request = builder.get().url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                byte[] bytes = response.body().bytes();
                response.body().close();
                response.close();
                return bytes;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String post(String url, Boolean isProxy) {
        try {
            RequestBody requestBody = new FormBody.Builder().build();
            Request request = new Request.Builder().addHeader("Connection", "close").post(requestBody).url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String postJson(String url, Map<String, String> header, String json, Boolean isProxy) {
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            Request.Builder requestBuilder = new Request.Builder();
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = requestBuilder.post(body).url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String postFormData(String url, Map<String, String> params, Boolean isProxy) {
        try {
            Request.Builder requestBuilder = new Request.Builder();
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
            Request request = requestBuilder.post(formBodyBuilder.build()).url(url).build();
            Response response = null;
            if (isProxy) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
