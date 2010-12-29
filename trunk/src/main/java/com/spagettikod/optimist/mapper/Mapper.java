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

package com.spagettikod.optimist.mapper;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spagettikod.optimist.EntityWrapper;

/**
 * 
 * @author Roland Bali
 * 
 */
public abstract class Mapper {

	private static Logger log = LoggerFactory.getLogger(Mapper.class);

	public abstract Object getCurrentEntityVersionInDatabase(
			Connection connection, EntityWrapper entityWrapper)
			throws SQLException;

	public static Mapper getMapper(Connection connection) {
		try {
			Class<? extends Mapper> mapperClass = null;
			String dbName = connection.getMetaData().getDatabaseProductName();
			Integer dbMajorVersion = connection.getMetaData()
					.getDatabaseMajorVersion();
			Integer dbMinorVersion = connection.getMetaData()
					.getDatabaseMinorVersion();
			if ("MySQL".equals(dbName)) {
				mapperClass = MySqlMapper.class;
			}
			if (mapperClass == null) {
				throw new RuntimeException(
						"Could not find appropriate mapper for database "
								+ dbName + ", major version " + dbMajorVersion
								+ ", minor version " + dbMinorVersion);
			}
			return getMapper(mapperClass);
		} catch (SQLException e) {
			log.error("Could not retrive database metadata.", e);
		}
		return null;
	}

	public static Mapper getMapper(Class<? extends Mapper> mapperClass) {
		log.info("Setting up interceptor, loading mapper class: "
				+ mapperClass.getName());
		try {
			return (Mapper) mapperClass.newInstance();
		} catch (InstantiationException e) {
			log.error("Could not instantiate mapper class.", e);
		} catch (IllegalAccessException e) {
			log.error("Could not instantiate mapper class.", e);
		}
		return null;
	}

	public static Mapper getMapper(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<Mapper> mapperClass = (Class<Mapper>) Class
					.forName(className);
			return getMapper(mapperClass);
		} catch (ClassNotFoundException e) {
			log.error("Could not load mapper class.", e);
		}
		return null;
	}

}
