package com.github.thrift.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThriftServerConfiguration
{
	@Value("${thrift.server.ip}")
	private String serverIp;
	
	@Value("${thrift.server.port}")
	private int serverPort;

	@Value("${thrift.zk.list}")
	private String zkHosts;
	
	@Bean
	public ThriftServerRegistry register()
	{
		ThriftServerRegistry register = new ThriftServerRegistry();
		register.setServerList(zkHosts);
		register.setServerIp(serverIp);
		register.setServerPort(serverPort);
		return register;
	}
}
