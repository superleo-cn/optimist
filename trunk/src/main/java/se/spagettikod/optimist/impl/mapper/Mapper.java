package se.spagettikod.optimist.impl.mapper;

import java.sql.Connection;
import java.sql.SQLException;

import se.spagettikod.optimist.impl.EntityWrapper;

public interface Mapper {

	public boolean isCompatible(Connection connection);

	public Object getCurrentEntityVersionInDatabase(Connection connection,
			EntityWrapper entityWrapper) throws SQLException;

}