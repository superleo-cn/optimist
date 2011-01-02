package com.spagettikod.optimist.testutil.mockups;

public interface InheritedEntityMapper {

	public InheritedEntity getById(Long id);

	public void insertEntity(InheritedEntity entity);

	public void updateEntity(InheritedEntity entity);

	public void deleteEntity(InheritedEntity entity);

}
