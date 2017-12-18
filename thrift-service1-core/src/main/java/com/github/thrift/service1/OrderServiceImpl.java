package com.github.thrift.service1;

import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.thrift.provider.ThriftService;
import com.github.thrift.service1.order.OrderDTO;
import com.github.thrift.service1.order.OrderQuery;
import com.github.thrift.service1.order.OrderService;
@ThriftService(version="1.1.2", weight=3)
@Service
public class OrderServiceImpl implements OrderService.Iface
{
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	@Override
	public OrderDTO getById(long id) throws TException
	{
		logger.info("getById "+id);
		return null;
	}

	@Override
	public List<OrderDTO> search(OrderQuery query) throws TException
	{
		logger.info("search ");
		return null;
	}

}
