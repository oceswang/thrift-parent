package com.github.thrift.server;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
/**
 * 注册服务提供者<br>
 * 使用{@link TMultiplexedProcessor}发布多个服务<br>
 * 格式:<br>
 * |rpc<br>
 * |- com.github.thrift.service1.user.UserService_1.1.1<br>
 * |-- 192.168.1.21:8089:3<br>
 * |-- 192.168.1.22:8089:2
 * @author wangsong
 *
 */
public class ThriftServerRegistry implements InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(ThriftServerRegistry.class);
	private ServerThread thread;
	private String serverIp;
	private int serverPort;
	private String serverList;
	private ZkClient zkClient;
	@Autowired
	ApplicationContext applicationContext;

	private Map<String, ThriftService> serviceMap = new HashMap<>();
	@Override
	public void afterPropertiesSet() throws Exception
	{
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ThriftService.class);
		if (beans == null || beans.size() == 0)
		{
			throw new IllegalStateException("No Thrift Service Found.");
		}
		zkClient = new ZkClient(serverList);

		Set<Map.Entry<String, Object>> beanEntrySet = beans.entrySet();
		for (Map.Entry<String, Object> entry : beanEntrySet)
		{
			Object bean = entry.getValue();
			ThriftService ann = bean.getClass().getAnnotation(ThriftService.class);
			Class<?> ifaceClass = getIfaceClass(bean);
			String serviceName = ifaceClass.getEnclosingClass().getName();
			serviceMap.put(serviceName, ann);
		}
		thread = new ServerThread(beans, serverPort);
		thread.start();

		//启动时注册
		register();
		
		zkClient.subscribeStateChanges(new IZkStateListener() {
			@Override
			public void handleStateChanged(KeeperState state) throws Exception
			{
				//断开重连之后从新注册
				if(state.equals(KeeperState.SyncConnected))
				{
					register();
				}
			}
			@Override
			public void handleNewSession() throws Exception
			{
			}

			@Override
			public void handleSessionEstablishmentError(Throwable error) throws Exception
			{
			}
		});
	}

	@Override
	public void destroy() throws Exception
	{
		if (thread != null)
		{
			thread.stopServer();
		}
	}

	private void register()
	{
		Set<Map.Entry<String, ThriftService>> annEntrySet = serviceMap.entrySet();
		for (Map.Entry<String, ThriftService> entry : annEntrySet)
		{
			ThriftService ann = entry.getValue();
			String serviceName = entry.getKey();
			String version = ann.version();
			String address = serverIp + ":" + serverPort + ":" + ann.weight();
			String servicePath = "/rpc/" + serviceName + "_" + version;
			String addressPath = servicePath + "/" + address;
			
			if (!zkClient.exists(servicePath))
			{
				zkClient.createPersistent(servicePath, true);
			}
			zkClient.createEphemeral(addressPath);
			if (logger.isInfoEnabled())
			{
				logger.info(String.format("Service provider (%s) registed", addressPath));
			}
		}
	}

	private static final class ServerThread extends Thread
	{
		private TServer server;

		ServerThread(Map<String, Object> beans, int port) throws TTransportException
		{
			TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
			TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
			TMultiplexedProcessor processor = new TMultiplexedProcessor();
			
			Set<Map.Entry<String, Object>> beanEntrySet = beans.entrySet();
			for (Map.Entry<String, Object> entry : beanEntrySet)
			{
				Object bean = entry.getValue();
				Class<?> ifaceClass = getIfaceClass(bean);
				TProcessor processItem = getServiceProcessor(bean, ifaceClass);
				String serviceName = ifaceClass.getEnclosingClass().getName();
				processor.registerProcessor(serviceName, processItem);
			}
			
			TProcessorFactory processorFactory = new TProcessorFactory(processor);
			tArgs.processorFactory(processorFactory);
			tArgs.transportFactory(new TFramedTransport.Factory());
			tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			server = new TThreadedSelectorServer(tArgs);
		}

		@Override
		public void run()
		{
			if (server != null)
			{
				server.serve();
			}
		}

		public void stopServer()
		{
			if (server != null)
			{
				server.stop();
			}
		}
	}

	private static Class<?> getIfaceClass(Object bean)
	{
		Class<?>[] allInterfaces = ClassUtils.getAllInterfaces(bean);
		for (Class<?> item : allInterfaces)
		{
			if (!item.getSimpleName().equals("Iface"))
			{
				continue;
			}
			return item;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked" })
	private static TProcessor getServiceProcessor(Object bean, Class<?> ifaceClazz)
	{
		try
		{
			Class<TProcessor> processorClazz = null;
			Class<?> clazz = ifaceClazz.getDeclaringClass();
			Class<?>[] classes = clazz.getDeclaredClasses();
			for (Class<?> innerClazz : classes)
			{
				if (!innerClazz.getName().endsWith("$Processor"))
				{
					continue;
				}

				if (!TProcessor.class.isAssignableFrom(innerClazz))
				{
					continue;
				}
				processorClazz = (Class<TProcessor>) innerClazz;
				break;
			}
			if (processorClazz == null)
			{
				throw new IllegalStateException("No TProcessor Found.");
			}
			Constructor<TProcessor> contructor = processorClazz.getConstructor(ifaceClazz);
			return BeanUtils.instantiateClass(contructor, bean);
		} catch (Exception e)
		{
			e.printStackTrace();
		} 
		return null;
	}

	public String getServerIp()
	{
		return serverIp;
	}

	public void setServerIp(String serverIp)
	{
		this.serverIp = serverIp;
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
	}

	public String getServerList()
	{
		return serverList;
	}

	public void setServerList(String serverList)
	{
		this.serverList = serverList;
	}
}
