package se.spagettikod.optimist.testutil.mockups;

public interface EntityMapper {

	public Entity getById(Long id);

	public void insertEntity(Entity entity);

	public void updateEntity(Entity entity);

	public void deleteEntity(Entity entity);
	
	public void deleteAll();

}
