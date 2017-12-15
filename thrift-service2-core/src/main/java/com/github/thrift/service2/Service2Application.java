package com.github.thrift.service2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.github.thrift.consumer.ThriftConsumerConfiguration;

@SpringBootApplication
@ComponentScan
@Import(ThriftConsumerConfiguration.class)
public class Service2Application
{
	public static void main(String[] args)
	{
		SpringApplication.run(Service2Application.class, args);
	}
}
