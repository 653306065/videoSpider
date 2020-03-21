package com.spider.conf;

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ProxyConf {

	@Value("${proxy.host}")
	private String host;
	
	@Value("${proxy.port}")
	private int port;
	
	@Bean
    public Proxy getProxy() {
		Proxy proxy=new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host,port));
		return proxy;
	}
}
