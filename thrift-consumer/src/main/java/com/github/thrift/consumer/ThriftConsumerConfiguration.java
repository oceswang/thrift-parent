package com.github.thrift.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ThriftConsumerConfiguration
{
	@Value("${thrift.zk.list}")
	private String zkHosts;
	
	@Bean
	public BeanPostProcessor beanPostProcessor(ThriftServerDiscovery discovery, ThriftConsumerProxy proxy)
	{
		ThriftRefAnnBeanPostProcessor processor = new ThriftRefAnnBeanPostProcessor();
		processor.setDiscovery(discovery);
		processor.setProxy(proxy);
		return processor;
	}
	@Bean
	public ThriftServerDiscovery discovery()
	{
		ThriftServerDiscovery discovery = new ThriftServerDiscovery();
		discovery.setServerList(zkHosts);
		return discovery;
	}
	@Bean
	public ThriftConsumerProxy proxy()
	{
		ThriftConsumerProxy proxy = new ThriftConsumerProxy();
		return proxy;
	}
}
