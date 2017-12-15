package com.github.thrift.service1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.github.thrift.server.ThriftServerConfiguration;

@SpringBootApplication
@ComponentScan
@Import(ThriftServerConfiguration.class)
public class Service1Application
{
	public static void main(String[] args)
	{
		SpringApplication.run(Service1Application.class, args);
	}
}
