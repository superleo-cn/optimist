package com.spagettikod.optimist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityWrapperTest {

	Logger log = LoggerFactory.getLogger(EntityWrapperTest.class);

	//
	// Classes with erroneous annotations.
	//

	class MockEntityWithoutOptimisticLockingAnnotation {

	}

	@OptimisticLocking("my_table")
	class MockEntityWithoutIdentityAnnotation {

	}

	@OptimisticLocking("my_table")
	class MockEntityWithMultipleIdentityAnnotation {
		@Identity("id")
		Long firstIdentity;

		@Identity("id")
		Long secondIdentity;
	}

	@OptimisticLocking("my_table")
	class MockEntityWithWrongIdentityType {
		@Identity("id")
		String firstIdentity;
	}

	@OptimisticLocking("my_table")
	class MockEntityWithMissingVersionAnnotation {
		@Identity("id")
		Long identity;
	}

	@OptimisticLocking("my_table")
	class MockEntityWithMultipleVersionAnnotation {
		@Identity("id")
		Long identity;

		@Version("version")
		Integer version;

		@Version("version")
		Integer versionTwo;
	}

	//
	// Classes with correct annotations that should work without errors.
	//

	@OptimisticLocking("my_table")
	class MockEntity_IdentityLong {

		@SuppressWarnings("unused")
		@Identity("id")
		private Long id;

		@SuppressWarnings("unused")
		@Version("version")
		private Integer version;

	}

	@OptimisticLocking("my_table")
	class MockEntity_IdentityInteger {

		@SuppressWarnings("unused")
		@Identity("id")
		private Integer id;

		@SuppressWarnings("unused")
		@Version("version")
		private Integer version;

	}

	@OptimisticLocking("my_table")
	class MockEntity_LongVersion {

		@SuppressWarnings("unused")
		@Identity("id")
		private Long id;

		@Version("version")
		private Long version;

		public Long getVersion() {
			return version;
		}

	}

	@OptimisticLocking("my_table")
	class MockEntity_IntegerVersion {

		@SuppressWarnings("unused")
		@Identity("id")
		private Long id;

		@Version("version")
		private Integer version;

		public Integer getVersion() {
			return version;
		}

	}

	@OptimisticLocking("my_table")
	class MockEntity {

		@SuppressWarnings("unused")
		@Identity("id")
		private Long id;

		@Version("version")
		private Long version;

		public Long getVersion() {
			return version;
		}

	}

	//
	// Table annotation tests
	//

	@Test(expected = AnnotationException.class)
	public void constructor_MissingOptimisticLockingAnnotation() {
		MockEntityWithoutOptimisticLockingAnnotation obj = new MockEntityWithoutOptimisticLockingAnnotation();
		new EntityWrapper(obj);
	}

	//
	// Identity annotation tests
	//

	@Test(expected = AnnotationException.class)
	public void constructor_MissingIdentityAnnotation() {
		MockEntityWithoutIdentityAnnotation obj = new MockEntityWithoutIdentityAnnotation();
		new EntityWrapper(obj);
	}

//	@Test(expected = AnnotationException.class)
//	public void constructor_MultipleIdentityAnnotation() {
//		MockEntityWithMultipleIdentityAnnotation obj = new MockEntityWithMultipleIdentityAnnotation();
//		new EntityWrapper(obj);
//	}

	@Test(expected = AnnotationException.class)
	public void constructor_MockEntityWithWrongIdentityType() {
		MockEntityWithWrongIdentityType obj = new MockEntityWithWrongIdentityType();
		new EntityWrapper(obj);
	}

	@Test
	public void constructor_MockEntity_IdentityInteger() {
		MockEntity_IdentityInteger obj = new MockEntity_IdentityInteger();
		new EntityWrapper(obj);
	}

	@Test
	public void constructor_MockEntity_IdentityLongObj() {
		MockEntity_IdentityLong obj = new MockEntity_IdentityLong();
		new EntityWrapper(obj);
	}

	//
	// Version annotation tests
	//

	@Test(expected = AnnotationException.class)
	public void constructor_MockEntityWithMissingVersionAnnotation() {
		MockEntityWithMissingVersionAnnotation obj = new MockEntityWithMissingVersionAnnotation();
		new EntityWrapper(obj);
	}

//	@Test(expected = AnnotationException.class)
//	public void constructor_MockEntityWithMultipleVersionAnnotation() {
//		MockEntityWithMultipleVersionAnnotation obj = new MockEntityWithMultipleVersionAnnotation();
//		new EntityWrapper(obj);
//	}

	//
	// Test constructor with a valid object
	//

	@Test
	public void constructor() {
		MockEntity obj = new MockEntity();
		new EntityWrapper(obj);
	}

	//
	// Test method incrementVersion
	//

	@Test
	public void incrementVersion_LongVersion() {
		MockEntity_LongVersion obj = new MockEntity_LongVersion();
		EntityWrapper wrapper = new EntityWrapper(obj);
		wrapper.incrementVersion();
		assertEquals("Wrong version number", new Long(0), obj.getVersion());
		wrapper.incrementVersion();
		assertEquals("Wrong version number", new Long(1), obj.getVersion());
	}

	//
	// Test method hasOptimisticLockingAnnotation
	//
	@Test
	public void hasOptimisticLockingAnnotation() {
		MockEntity obj = new MockEntity();
		assertEquals(true, EntityWrapper.hasOptimisticLockingAnnotation(obj));
		MockEntityWithoutOptimisticLockingAnnotation obj2 = new MockEntityWithoutOptimisticLockingAnnotation();
		assertEquals(false, EntityWrapper.hasOptimisticLockingAnnotation(obj2));
	}

	//
	// Test method getTableName
	//
	@Test
	public void getTableName() {
		MockEntity obj = new MockEntity();
		EntityWrapper wrapper = new EntityWrapper(obj);
		assertEquals("my_table", wrapper.getTableName());
	}
}
