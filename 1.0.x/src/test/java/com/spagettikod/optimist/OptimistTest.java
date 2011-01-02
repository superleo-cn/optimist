package com.spagettikod.optimist;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import com.spagettikod.optimist.testutil.MyBatisTestUtil;
import com.spagettikod.optimist.testutil.mockups.Entity;
import com.spagettikod.optimist.testutil.mockups.EntityMapper;

public class OptimistTest {

	@Before
	public void init() {
		SqlSession session = MyBatisTestUtil.getSession();
		Connection c = session.getConnection();
		Statement s = null;
		try {
			s = c.createStatement();
			s.execute("TRUNCATE TABLE entity");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (s != null && !s.isClosed()) {
					s.close();
				}
				if (c != null && c.isClosed()) {
					c.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (session != null) {
				session.close();
			}
		}
	}

	@Test
	public void insertNewEntity() {
		Entity entity = new Entity();
		entity.setValue("testing");
		SqlSession session = MyBatisTestUtil.getSession();
		EntityMapper mapper = session.getMapper(EntityMapper.class);
		try {
			mapper.insertEntity(entity);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		Assert.assertEquals("testing", entity.getValue());
		Assert.assertEquals(new Integer(0), entity.getVersion());
		Assert.assertEquals(new Long(1), entity.getId());
	}

	@Test
	public void updateEntity() {
		Entity entity = new Entity();
		entity.setValue("testing");
		SqlSession session = MyBatisTestUtil.getSession();
		EntityMapper mapper = session.getMapper(EntityMapper.class);
		try {
			mapper.insertEntity(entity);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		Assert.assertEquals(new Integer(0), entity.getVersion());

		Long identity = entity.getId();
		session = MyBatisTestUtil.getSession();
		mapper = session.getMapper(EntityMapper.class);
		entity = mapper.getById(identity);
		entity.setValue("tjoho");
		try {
			mapper.updateEntity(entity);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		Assert.assertEquals("tjoho", entity.getValue());
		Assert.assertEquals(new Integer(1), entity.getVersion());
	}

	@Test(expected = com.spagettikod.optimist.ModifiedByAnotherUserException.class)
	public void modifiedByAnotherUser() {
		SqlSession firstSession = MyBatisTestUtil.getSession();
		Entity entity = new Entity();
		entity.setValue("firstEdit");
		EntityMapper firstMapper = firstSession.getMapper(EntityMapper.class);
		firstMapper.insertEntity(entity);
		firstSession.commit();
		Assert.assertEquals(new Integer(0), entity.getVersion());

		// Load same entity in second session
		SqlSession secondSession = MyBatisTestUtil.getSession();
		Entity secondEntity = new Entity();
		Long identity = entity.getId();
		EntityMapper secondMapper = secondSession.getMapper(EntityMapper.class);
		secondEntity = secondMapper.getById(identity);
		Assert.assertEquals(new Integer(0), secondEntity.getVersion());

		// Modify entity in first session
		entity = firstMapper.getById(entity.getId());
		entity.setValue("secondEdit");
		firstMapper.updateEntity(entity);
		firstSession.commit();
		firstSession.close();
		Assert.assertEquals(new Integer(1), entity.getVersion());

		secondEntity.setValue("thirdEdit");
		try {
			secondMapper.updateEntity(secondEntity);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ModifiedByAnotherUserException) {
				throw (ModifiedByAnotherUserException) e.getCause();
			} else {
				throw e;
			}
		} finally {
			if (secondSession != null) {
				secondSession.close();
			}
		}
	}

	@Test(expected = com.spagettikod.optimist.ModifiedByAnotherUserException.class)
	public void modifiedByAnotherUserDuringDelete() {
		SqlSession firstSession = MyBatisTestUtil.getSession();
		Entity entity = new Entity();
		entity.setValue("firstEdit");
		EntityMapper firstMapper = firstSession.getMapper(EntityMapper.class);
		firstMapper.insertEntity(entity);
		firstSession.commit();
		Assert.assertEquals(new Integer(0), entity.getVersion());

		// Load same entity in second session
		SqlSession secondSession = MyBatisTestUtil.getSession();
		Entity secondEntity = new Entity();
		Long identity = entity.getId();
		EntityMapper secondMapper = secondSession.getMapper(EntityMapper.class);
		secondEntity = secondMapper.getById(identity);
		Assert.assertEquals(new Integer(0), secondEntity.getVersion());

		// Modify entity in first session
		entity = firstMapper.getById(entity.getId());
		entity.setValue("secondEdit");
		firstMapper.updateEntity(entity);
		firstSession.commit();
		firstSession.close();
		Assert.assertEquals(new Integer(1), entity.getVersion());

		// Delete the entity after first session has modified
		try {
			secondMapper.deleteEntity(secondEntity);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ModifiedByAnotherUserException) {
				throw (ModifiedByAnotherUserException) e.getCause();
			} else {
				throw e;
			}
		} finally {
			if (secondSession != null) {
				secondSession.close();
			}
		}
	}

	@Test(expected = com.spagettikod.optimist.RemovedByAnotherUserException.class)
	public void removedByAnotherUser() {
		SqlSession firstSession = MyBatisTestUtil.getSession();
		Entity entity = new Entity();
		entity.setValue("firstEdit");
		EntityMapper firstMapper = firstSession.getMapper(EntityMapper.class);
		firstMapper.insertEntity(entity);
		firstSession.commit();
		Assert.assertEquals(new Integer(0), entity.getVersion());

		// Load same entity in second session
		SqlSession secondSession = MyBatisTestUtil.getSession();
		Entity secondEntity = new Entity();
		Long identity = entity.getId();
		EntityMapper secondMapper = secondSession.getMapper(EntityMapper.class);
		secondEntity = secondMapper.getById(identity);
		Assert.assertEquals(new Integer(0), secondEntity.getVersion());

		// Delete entity in first session
		entity = firstMapper.getById(entity.getId());
		firstMapper.deleteEntity(entity);
		firstSession.commit();
		firstSession.close();

		secondEntity.setValue("thirdEdit");
		try {
			secondMapper.updateEntity(secondEntity);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof RemovedByAnotherUserException) {
				throw (RemovedByAnotherUserException) e.getCause();
			} else {
				throw e;
			}
		} finally {
			if (secondSession != null) {
				secondSession.close();
			}
		}
	}

	@Test(expected = com.spagettikod.optimist.RemovedByAnotherUserException.class)
	public void removedByAnotherUserDuringDelete() {
		SqlSession firstSession = MyBatisTestUtil.getSession();
		Entity entity = new Entity();
		entity.setValue("firstEdit");
		EntityMapper firstMapper = firstSession.getMapper(EntityMapper.class);
		firstMapper.insertEntity(entity);
		firstSession.commit();
		Assert.assertEquals(new Integer(0), entity.getVersion());

		// Load same entity in second session
		SqlSession secondSession = MyBatisTestUtil.getSession();
		Entity secondEntity = new Entity();
		Long identity = entity.getId();
		EntityMapper secondMapper = secondSession.getMapper(EntityMapper.class);
		secondEntity = secondMapper.getById(identity);
		Assert.assertEquals(new Integer(0), secondEntity.getVersion());

		// Delete entity in first session
		entity = firstMapper.getById(entity.getId());
		firstMapper.deleteEntity(entity);
		firstSession.commit();
		firstSession.close();

		try {
			secondMapper.deleteEntity(secondEntity);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof RemovedByAnotherUserException) {
				throw (RemovedByAnotherUserException) e.getCause();
			} else {
				throw e;
			}
		} finally {
			if (secondSession != null) {
				secondSession.close();
			}
		}
	}
}
