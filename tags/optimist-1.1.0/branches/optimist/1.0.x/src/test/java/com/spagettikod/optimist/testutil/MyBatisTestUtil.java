package com.spagettikod.optimist.testutil;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public abstract class MyBatisTestUtil {

	private static SqlSessionFactory factory;

	static {
		try {
			System.out.print("Configuring MyBatis...");
			Reader reader = Resources
					.getResourceAsReader("Configuration.xml");
			factory = new SqlSessionFactoryBuilder().build(reader, "test");
			System.out.println("done!");
		} catch (Exception e) {
			System.out.println("failed!");
			e.printStackTrace();
		}
	}

	static public SqlSessionFactory getSessionFactory() {
		return factory;
	}

	static public SqlSession getSession() {
		return factory.openSession();
	}

}
