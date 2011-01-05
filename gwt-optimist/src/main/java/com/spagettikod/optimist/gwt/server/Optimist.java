package com.spagettikod.optimist.gwt.server;

import javax.servlet.ServletContext;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Optimist {

	private static final Logger log = LoggerFactory.getLogger(Optimist.class);

	public static final String OPTIMIST_MYBATIS_SESSION_FACTORY_KEY = "com.spagettikod.optimist.myBatisSessionFactory";

	public static void initOptimist(ServletContext ctx,
			SqlSessionFactory factory) {
		log.info("Adding MyBatis SqlSessionFactory attribute to servlet context using key '"
				+ OPTIMIST_MYBATIS_SESSION_FACTORY_KEY + "'");
		ctx.setAttribute(OPTIMIST_MYBATIS_SESSION_FACTORY_KEY, factory);
	}

}
