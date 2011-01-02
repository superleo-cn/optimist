/*
 * Copyright (C) 2010 Roland Bali
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.spagettikod.optimist.impl;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spagettikod.optimist.ModifiedByAnotherUserException;
import com.spagettikod.optimist.OptimisticLocking;
import com.spagettikod.optimist.RemovedByAnotherUserException;

/**
 * Plug in to intercept inserts, updates and deletes for objects implementing
 * {@link OptimisticLocking} interface. When updating or deleting it tries to
 * lock the row being updated. If object can not be found a
 * {@link RemovedByAnotherUserException} exception is thrown. If locking
 * succeeds the version number is compared to the one in the database. If it
 * differs a {@link ModifiedByAnotherUserException} exception is thrown.
 * 
 * @author Roland Bali
 * @see OptimisticLockable
 * @see ModifiedByAnotherUserException
 * @see RemovedByAnotherUserException
 * 
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = {
		MappedStatement.class, Object.class }) })
public class OptimisticLockingInterceptor implements Interceptor {

	private Logger log = LoggerFactory
			.getLogger(OptimisticLockingInterceptor.class);

	private Mapper mapper;

	private void updateImpl(Connection connection, EntityWrapper entityWrapper)
			throws Throwable {
		//
		// Retrieve the version currently in the database.
		//
		Object currentEntityVersion = mapper.getCurrentEntityVersionInDatabase(
				connection, entityWrapper);

		if (currentEntityVersion == null) {
			//
			// If current version can not be found entity has been
			// removed by another user.
			//
			throw new RemovedByAnotherUserException();
		} else {
			//
			// Check if version being saved is stale. If it is it
			// has been modified by another user.
			//
			if (entityWrapper.isStale(currentEntityVersion)) {
				throw new ModifiedByAnotherUserException();
			} else {
				entityWrapper.incrementVersion();
			}
		}
	}

	private void deleteImpl(Connection connection, EntityWrapper entityWrapper)
			throws Throwable {
		//
		// Retrieve the version currently in the database.
		//
		Object currentEntityVersion = mapper.getCurrentEntityVersionInDatabase(
				connection, entityWrapper);
		//
		// If current version can not be found entity has been
		// removed by another user.
		//
		if (currentEntityVersion == null) {
			throw new RemovedByAnotherUserException();
		}
		//
		// If version being saved is stale. If it is it has been modified by
		// another user.
		//
		else if (entityWrapper.isStale(currentEntityVersion)) {
			throw new ModifiedByAnotherUserException();
		}
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		EntityWrapper entityWrapper = null;
		MappedStatement ms = null;

		Executor executor = (Executor) invocation.getTarget();
		Connection connection = executor.getTransaction().getConnection();

		if (mapper == null) {
			log.info("Mapper not initialized, trying to find appropriate mapper using database metadata.");
			mapper = Mapper.getMapper(connection);
		}

		if (invocation.getArgs() != null && invocation.getArgs().length > 0
				&& invocation.getArgs()[0] != null
				&& invocation.getArgs()[0] instanceof MappedStatement) {

			ms = (MappedStatement) invocation.getArgs()[0];
			Object obj = invocation.getArgs()[1];

			if (EntityWrapper.hasOptimisticLockingAnnotation(obj)) {
				//
				// Increment the version number when intercepting an UPDATE
				// statement.
				//
				if (ms.getSqlCommandType() == SqlCommandType.UPDATE) {
					entityWrapper = new EntityWrapper(obj);
					updateImpl(connection, entityWrapper);
				}
				//
				// Version number set to 0 during insert.
				//
				else if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
					entityWrapper = new EntityWrapper(obj);
					entityWrapper.initVersion();
				}
				//
				// Make sure the object is not stale or has been removed when
				// deleting.
				//
				else if (ms.getSqlCommandType() == SqlCommandType.DELETE) {
					entityWrapper = new EntityWrapper(obj);
					deleteImpl(connection, entityWrapper);
				}
			} else if (ms.getSqlCommandType() == SqlCommandType.DELETE
					|| ms.getSqlCommandType() == SqlCommandType.INSERT
					|| ms.getSqlCommandType() == SqlCommandType.UPDATE) {
				log.debug("Optimistic locking can not be applied! Parameter type '"
						+ obj.getClass().getName()
						+ "' does not have the annotation @OptimisticLocking.");
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object arg0) {
		return Plugin.wrap(arg0, this);
	}

	@Override
	public void setProperties(Properties arg0) {
		if (arg0.getProperty("mapper") != null) {
			log.info("Found mapper property, using class: "
					+ arg0.getProperty("mapper"));
			mapper = Mapper.getMapper(arg0.getProperty("mapper"));
		}
	}
}