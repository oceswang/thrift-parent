package com.github.thrift.consumer;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;

public class ThriftSocketPoolFactory extends BaseKeyedPooledObjectFactory<String, TSocket>
{
	@Override
	public TSocket create(String key) throws Exception
	{
		String[] str = key.split(":");
		String host = str[0];
		Integer port = Integer.valueOf(str[1]);
		TSocket tsocket = new TSocket(host, port);
		tsocket.open();
		return tsocket;
	}

	@Override
	public PooledObject<TSocket> wrap(TSocket value)
	{
		return new DefaultPooledObject<>(value);
	}

	@Override
	public void destroyObject(String key, PooledObject<TSocket> p) throws Exception
	{
		p.getObject().close();
	}

	@Override
	public boolean validateObject(String key, PooledObject<TSocket> p)
	{
		return p.getObject().isOpen();
	}
	

}
