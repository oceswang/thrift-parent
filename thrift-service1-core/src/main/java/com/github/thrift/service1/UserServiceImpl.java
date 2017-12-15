package com.github.thrift.service1;

import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.thrift.server.ThriftService;
import com.github.thrift.service1.user.UserDTO;
import com.github.thrift.service1.user.UserQuery;
import com.github.thrift.service1.user.UserService;

@ThriftService(version="1.1.1", weight=4)
@Service
public class UserServiceImpl implements UserService.Iface
{
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Override
	public UserDTO getById(long id) throws TException
	{
		logger.info("getById "+id);
		return null;
	}

	@Override
	public List<UserDTO> search(UserQuery query) throws TException
	{
		logger.info("search ");
		return null;
	}

}
