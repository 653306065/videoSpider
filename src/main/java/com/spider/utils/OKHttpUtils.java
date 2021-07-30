package com.spider.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class OKHttpUtils implements ApplicationRunner {

    private static final Logger logger=LoggerFactory.getLogger(OKHttpUtils.class);

    static OkHttpClient httpClient = new OkHttpClient.Builder().writeTimeout(10,TimeUnit.SECONDS).callTimeout(10,TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

    static OkHttpClient proxyHttpClient = null;

    public static String get(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static String get(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            if (!CollectionUtils.isEmpty(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static String getRedirectUrl(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }


    public static String getRedirectUrl(String url, Map<String, String> header, Boolean isProxy) {
        try {
            Request.Builder builder = new Request.Builder();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            Request request = builder.get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static InputStream getInputStream(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            return response;
        } catch (Exception e) {
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static byte[] getBytes(String url, Boolean isProxy) {
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                byte[] bytes = response.body().bytes();
                response.body().close();
                response.close();
                return bytes;
            }else{
                String body = null;
                if(Objects.nonNull(response.body())){
                    body=response.body().string();
                }
                logger.info("code:{},url:{},body:{}",response.code(),url,body);
            }
            response.close();
            return null;
        } catch (Exception e) {
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static String post(String url, Boolean isProxy) {
        try {
            RequestBody requestBody = new FormBody.Builder().build();
            Request request = new Request.Builder().addHeader("Connection", "close").post(requestBody).url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
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
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
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
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }

    public static String postFormObjectData(String url, Map<String, Object> params, Boolean isProxy) {
        try {
            Request.Builder requestBuilder = new Request.Builder();
            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof String) {
                    multipartBodyBuilder.addFormDataPart(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof byte[]) {
                    multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getKey(), RequestBody.create((byte[])(entry.getValue())));
                } else {
                    multipartBodyBuilder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            Request request = requestBuilder.post(multipartBodyBuilder.build()).url(url).build();
            Response response;
            if (isProxy && Objects.nonNull(proxyHttpClient)) {
                response = proxyHttpClient.newCall(request).execute();
            } else {
                response = httpClient.newCall(request).execute();
            }
            if (response.isSuccessful()) {
                String html = response.body().string();
                response.body().close();
                response.close();
                return html;
            } else {
                System.out.println(response.body().string());
            }
            response.close();
            return null;
        } catch (Exception e) {
            logger.error("{},访问失败,e:{}",url,e.getMessage());
            return null;
        }
    }


    public static String get(String url, Map<String, String> params, Map<String, String> header, Boolean isProxy) {
        return get(getUrlWithQueryString(url, params), header, isProxy);
    }

    public static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }
        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }
            if (i != 0) {
                builder.append('&');
            }
            builder.append(key);
            builder.append('=');
            builder.append(encode(value));
            i++;
        }
        return builder.toString();
    }

    public static String encode(String input) {
        if (input == null) {
            return "";
        }
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    @Override
    public void run(ApplicationArguments args) {
        Proxy proxy = SpringContentUtil.getBean(Proxy.class);
        proxyHttpClient = new OkHttpClient.Builder().proxy(proxy).writeTimeout(10,TimeUnit.SECONDS).callTimeout(10,TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    }
}
