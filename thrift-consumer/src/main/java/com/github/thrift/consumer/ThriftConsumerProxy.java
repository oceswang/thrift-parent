package com.github.thrift.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ThriftConsumerProxy
{

	public Object proxy(Class<?> iFaceInterface, String host, int port)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<?> factoryClass = getFactoryClass(iFaceInterface);
		String serviceName = iFaceInterface.getDeclaringClass().getName();
		return Proxy.newProxyInstance(classLoader, new Class[] { iFaceInterface }, new InvocationHandler() {

			@SuppressWarnings("unchecked")
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				TServiceClientFactory<TServiceClient> clientFactory = (TServiceClientFactory<TServiceClient>) factoryClass.newInstance();
				TSocket tsocket = new TSocket(host, port);
				TTransport transport = new TFramedTransport(tsocket);
				TProtocol protocol = new TBinaryProtocol(transport);
				TMultiplexedProtocol mpProtocol = new TMultiplexedProtocol(protocol, serviceName);
				TServiceClient client = clientFactory.getClient(mpProtocol);
				transport.open();
				Object result = null;
				try
				{
					result = method.invoke(client, args);
				} catch (Exception e)
				{
					if(!InvocationTargetException.class.isInstance(e))
					{
						throw e;
					}
					InvocationTargetException inve = (InvocationTargetException) e;
					if(!TApplicationException.class.isInstance(inve.getTargetException()))
					{
						throw e;
					}
					
					TApplicationException appe = (TApplicationException)inve.getTargetException();
					if(appe.getType() != TApplicationException.MISSING_RESULT)
					{
						throw e;
					}
					result = null;
				}
				return result;
			}

		});
	}

	private Class<?> getFactoryClass(Class<?> iFaceInterface)
	{
		Class<?> factoryClass = null;
		try
		{
			Class<?> serviceClass = iFaceInterface.getDeclaringClass();
			factoryClass = iFaceInterface.getClassLoader().loadClass(serviceClass.getName() + "$Client$Factory");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return factoryClass;
	}
}
