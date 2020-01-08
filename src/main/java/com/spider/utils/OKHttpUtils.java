package com.spider.utils;

import java.io.InputStream;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpUtils {

	public static String get(String url, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request request = new Request.Builder().get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String html = response.body().string();
				response.body().close();
				return html;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String get(String url, Map<String, String> header, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request.Builder builder = new Request.Builder();
			for (Map.Entry<String, String> entry : header.entrySet()) {
				builder.addHeader(entry.getKey(), entry.getValue());
			}
			Request request = builder.get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String html = response.body().string();
				response.body().close();
				return html;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getRedirectUrl(String url, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request request = new Request.Builder().get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String redirectUrl = response.request().url().toString();
				response.body().close();
				return redirectUrl;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream getInputStream(String url, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request request = new Request.Builder().get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				return response.body().byteStream();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Response getResponse(String url, Map<String, String> header, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request.Builder builder = new Request.Builder();
			if (header != null) {
				for (String key : header.keySet()) {
					builder.addHeader(key, header.get(key));
				}
			}
			Request request = builder.get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream getInputStream(String url, Map<String, String> header, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request.Builder builder = new Request.Builder();
			if (header != null) {
				for (String key : header.keySet()) {
					builder.addHeader(key, header.get(key));
				}
			}
			Request request = builder.get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				return response.body().byteStream();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getBytes(String url, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request request = new Request.Builder().get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				byte[] bytes = response.body().bytes();
				response.body().close();
				return bytes;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getBytes(String url, Map<String, String> header, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			Request.Builder builder = new Request.Builder();
			if (header != null) {
				for (String key : header.keySet()) {
					builder.addHeader(key, header.get(key));
				}
			}
			Request request = builder.get().url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				byte[] bytes = response.body().bytes();
				response.body().close();
				return bytes;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String post(String url, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).build();
			RequestBody requestBody = new FormBody.Builder().build();
			Request request = new Request.Builder().addHeader("Connection", "close").post(requestBody).url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String html = response.body().string();
				response.body().close();
				return html;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String postJson(String url, Map<String, String> header, String json, Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).proxy(proxy)
					.readTimeout(10, TimeUnit.SECONDS).build();
			RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
			Request.Builder requestBuilder = new Request.Builder();
			if (header != null) {
				for (Map.Entry<String, String> entry : header.entrySet()) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}
			Request request = requestBuilder.post(body).url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String html = response.body().string();
				response.body().close();
				return html;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String postFormData(String url, Map<String, String> params,Proxy proxy) {
		try {
			OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).proxy(proxy).readTimeout(10, TimeUnit.SECONDS).build();
			Request.Builder requestBuilder = new Request.Builder();
			FormBody.Builder formBodyBuilder =new FormBody.Builder();
			for(Map.Entry<String, String> entry:params.entrySet()) {
				formBodyBuilder.add(entry.getKey(),entry.getValue());
			}
			Request request = requestBuilder.post(formBodyBuilder.build()).url(url).build();
			Response response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String html = response.body().string();
				response.body().close();
				return html;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
