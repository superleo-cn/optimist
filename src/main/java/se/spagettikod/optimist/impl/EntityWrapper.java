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

package se.spagettikod.optimist.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.spagettikod.optimist.Identity;
import se.spagettikod.optimist.OptimisticLocking;
import se.spagettikod.optimist.Version;

/**
 * Wrapper class used internally by Optimist to manage objects using optimistic
 * locking. When the interceptor detects actions such as update, insert or
 * delete the object is wrapped in an <code>EntityWrapper</code>. The wrapper
 * exposes the identity and version fields to Optimist enabling it to retrieve
 * and modify those values.
 * 
 * @author Roland Bali
 * 
 */
public class EntityWrapper {

	private final Logger log = LoggerFactory.getLogger(EntityWrapper.class);

	private Object entityObject;

	private Class<?> entityClass;

	private String tableName = null;

	private String identityColumnName = null;

	private String versionColumnName = null;

	private Field identityField = null;

	private Field versionField = null;

	static boolean hasOptimisticLockingAnnotation(Object entityObject) {
		if (entityObject == null) {
			Logger log = LoggerFactory.getLogger(EntityWrapper.class);
			log.debug("Object is null, can not check for OptimisticLocking annotation!");
			return false;
		}
		return entityObject.getClass().isAnnotationPresent(
				OptimisticLocking.class);
	}

	EntityWrapper(Object entityObject) {
		log.debug("Trying to wrap object {}.", entityObject.getClass()
				.getName());
		this.entityObject = entityObject;
		entityClass = this.entityObject.getClass();
		validateAndExtractAnnotations();
	}

	String getTableName() {
		return tableName;
	}

	String getIdentityColumnName() {
		return identityColumnName;
	}

	String getVersionColumnName() {
		return versionColumnName;
	}

	void setIdentity(Object identity) {
		boolean accessChanged = false;
		if (!identityField.isAccessible()) {
			identityField.setAccessible(true);
			accessChanged = true;
		}
		try {
			if (identity instanceof Long || identity instanceof Integer) {
				identityField.set(entityObject, identity);
			} else {
				throw new RuntimeException(
						"Type '"
								+ identity.getClass().getName()
								+ "' is neither java.lang.Long or java.lang.Integer, unable to set identity.");
			}
		} catch (IllegalArgumentException e) {
			log.error("Could not set identity field.", e);
		} catch (IllegalAccessException e) {
			log.error("Could not set identity field.", e);
		} finally {
			if (accessChanged) {
				identityField.setAccessible(false);
			}
		}
	}

	/**
	 * Updates the version field when update occur.
	 */
	void incrementVersion() {
		boolean accessChanged = false;
		if (!versionField.isAccessible()) {
			versionField.setAccessible(true);
			accessChanged = true;
		}
		try {
			if (isVersionLong()) {
				Long value = (Long) versionField.get(entityObject);
				if (value == null) {
					value = 0L;
				} else {
					value++;
				}
				versionField.set(entityObject, value);
			} else if (isVersionInteger()) {
				Integer value = (Integer) versionField.get(entityObject);
				if (value == null) {
					value = 0;
				} else {
					value++;
				}
				versionField.set(entityObject, value);
			}
		} catch (IllegalArgumentException e) {
			log.error("Unable to increment version number.", e);
		} catch (IllegalAccessException e) {
			log.error("Unable to increment version number.", e);
		} finally {
			if (accessChanged) {
				versionField.setAccessible(false);
			}
		}
	}

	void initVersion() {
		boolean accessChanged = false;
		if (!versionField.isAccessible()) {
			versionField.setAccessible(true);
			accessChanged = true;
		}
		try {
			if (isVersionInteger()) {
				versionField.set(entityObject, new Integer(0));
			} else if (isVersionLong()) {
				versionField.set(entityObject, new Long(0));
			}
		} catch (IllegalArgumentException e) {
			log.error("Could not initialize version number.", e);
		} catch (IllegalAccessException e) {
			log.error("Could not initialize version number.", e);
		} finally {
			if (accessChanged) {
				versionField.setAccessible(false);
			}
		}
	}

	Object getVersion() {
		boolean accessChanged = false;
		if (!versionField.isAccessible()) {
			versionField.setAccessible(true);
			accessChanged = true;
		}
		try {
			return versionField.get(entityObject);
		} catch (IllegalArgumentException e) {
			log.error("Could not get version value.", e);
		} catch (IllegalAccessException e) {
			log.error("Could not get version value.", e);
		} finally {
			if (accessChanged) {
				versionField.setAccessible(false);
			}
		}
		return null;
	}

	Object getIdentity() {
		boolean accessChanged = false;
		if (!identityField.isAccessible()) {
			identityField.setAccessible(true);
			accessChanged = true;
		}
		try {
			return identityField.get(entityObject);
		} catch (IllegalArgumentException e) {
			log.error("Could not get identity value.", e);
		} catch (IllegalAccessException e) {
			log.error("Could not get identity value.", e);
		} finally {
			if (accessChanged) {
				identityField.setAccessible(false);
			}
		}
		return null;
	}

	boolean isStale(Object currentVersion) {
		Long thisVersion = null;
		Long currentValueLong = null;

		if (isVersionInteger()) {
			thisVersion = new Long((Integer) getVersion());
		} else if (isVersionLong()) {
			thisVersion = (Long) getVersion();
		} else if (versionField != null) {
			log.warn("Version type is neither Long or Integer: "
					+ versionField.getType());
		}

		if (currentVersion instanceof Long) {
			currentValueLong = (Long) currentVersion;
		} else if (currentVersion instanceof Integer) {
			currentValueLong = new Long((Integer) currentVersion);
		} else {
			throw new RuntimeException(
					"Version value type '"
							+ currentVersion.getClass().getName()
							+ "' is neither java.lang.Long or java.lang.Integer, unable to verify of object is stale.");
		}
		if (currentValueLong == null || thisVersion == null) {
			log.warn("Can not determine if object is stale, currentVersion="
					+ currentValueLong + ";thisVersion=" + thisVersion
					+ ". Returning not stale.");
			return false;
		}
		return currentValueLong > thisVersion;
	}

	private boolean isVersionInteger() {
		return Integer.class.equals(versionField.getType());
	}

	private boolean isVersionLong() {
		return Long.class.equals(versionField.getType());
	}

	private Field recursiveFieldFinder(Class<?> annotatedClass,
			Class<? extends Annotation> annotationClass) {
		for (Field f : annotatedClass.getDeclaredFields()) {
			if (f.isAnnotationPresent(annotationClass)) {
				return f;
			}
		}
		if (annotatedClass.getSuperclass() != null) {
			return recursiveFieldFinder(annotatedClass.getSuperclass(),
					annotationClass);
		}
		return null;
	}

	/**
	 * Validate class to make sure all annotations exist that are required by
	 * the Optimist framework.
	 */
	private void validateAndExtractAnnotations() {
		//
		// Make sure class has the OptimisticLocking annotation
		//
		if (!entityClass.isAnnotationPresent(OptimisticLocking.class)) {
			throw new AnnotationException("Class '" + entityClass.getName()
					+ "' is missing the @" + OptimisticLocking.class.getName()
					+ " annotation.");
		}

		//
		// Extract the table name
		//
		tableName = entityClass.getAnnotation(OptimisticLocking.class).value();

		//
		// Find field with Identity annotation.
		//
		identityField = recursiveFieldFinder(entityClass, Identity.class);

		//
		// Make sure Identity annotation is not missing
		//
		if (identityField == null) {
			throw new AnnotationException("Class '" + entityClass.getName()
					+ "' is missing the @" + Identity.class.getName()
					+ " annotation.");
		} else {
			identityColumnName = identityField.getAnnotation(Identity.class)
					.value();
		}

		//
		// Check Identity annotated field if of supported type
		//
		if (!identityField.getType().equals(Integer.class)
				&& !identityField.getType().equals(Long.class)) {
			throw new AnnotationException(
					"Field '"
							+ entityClass.getName()
							+ "."
							+ identityField.getName()
							+ "' is the type '"
							+ identityField.getType().getName()
							+ "'. That type is not a valid type for the @"
							+ Identity.class.getName()
							+ " annotation. Please use one of java.lang.Integer or java.lang.Long.");
		}

		//
		// Validate Version annotation
		//
		versionField = recursiveFieldFinder(entityClass, Version.class);

		//
		// Make sure Version annotation is not missing
		//
		if (versionField == null) {
			throw new AnnotationException("Class '" + entityClass.getName()
					+ "' is missing the @" + Version.class.getName()
					+ " annotation.");
		} else {
			versionColumnName = versionField.getAnnotation(Version.class)
					.value();
		}

		//
		// Validation annotated Version field type
		//
		if (!identityField.getType().equals(Integer.class)
				&& !identityField.getType().equals(Long.class)) {
			throw new AnnotationException(
					"Field '"
							+ entityClass.getName()
							+ "."
							+ identityField.getName()
							+ "' is the type '"
							+ identityField.getType().getName()
							+ "'. That type is not a valid type for the @"
							+ Version.class.getName()
							+ " annotation. Please use one of java.lang.Integer or java.lang.Long.");
		}
	}
}
