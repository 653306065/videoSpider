package com.spider.utils;

import java.io.InputStream;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpUtils {

    public static String get(String url,Proxy proxy) {
    	try {
			OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
			Request request=new Request.Builder().get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().string();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    
    public static String getRedirectUrl(String url,Proxy proxy) {
    	try {
			OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
			Request request=new Request.Builder().get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				String redirectUrl=response.request().url().toString();
				response.close();
				return redirectUrl;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static InputStream getInputStream(String url,Proxy proxy) {
    	try {
    		OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
			Request request=new Request.Builder().get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().byteStream();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static Response getResponse(String url,Map<String,String> header,Proxy proxy) {
    	try {
    		OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
    		Request.Builder builder=new Request.Builder();
    		if(header!=null) {
    			for(String key:header.keySet()) {
        			builder.addHeader(key, header.get(key));
        		}
    		}
			Request request=builder.get().url(url).build();
			Response response=httpClient.newCall(request).execute();
		    return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    
    public static InputStream getInputStream(String url,Map<String,String> header,Proxy proxy) {
    	try {
    		OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
    		Request.Builder builder=new Request.Builder();
    		if(header!=null) {
    			for(String key:header.keySet()) {
        			builder.addHeader(key, header.get(key));
        		}
    		}
			Request request=builder.get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().byteStream();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    
    public static byte[] getBytes(String url,Proxy proxy) {
    	try {
    		OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
			Request request=new Request.Builder().get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().bytes();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static byte[] getBytes(String url,Map<String,String> header,Proxy proxy) {
    	try {
    		OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
    		Request.Builder builder=new Request.Builder();
    		if(header!=null) {
    			for(String key:header.keySet()) {
        			builder.addHeader(key, header.get(key));
        		}
    		}
			Request request=builder.get().url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().bytes();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static String post(String url,Proxy proxy) {
    	try {
			OkHttpClient httpClient=new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).proxy(proxy).build();
			RequestBody requestBody=new FormBody.Builder().build();
			Request request=new Request.Builder().post(requestBody).url(url).build();
			Response response=httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				return response.body().string();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
}
