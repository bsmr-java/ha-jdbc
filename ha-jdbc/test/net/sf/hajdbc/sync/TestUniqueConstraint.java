/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (c) 2004-2006 Paul Ferraro
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or (at your 
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact: ferraro@users.sourceforge.net
 */
package net.sf.hajdbc.sync;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import net.sf.hajdbc.EasyMockTestCase;

import org.easymock.EasyMock;

/**
 * @author  Paul Ferraro
 * @since   1.0
 */
public class TestUniqueConstraint extends EasyMockTestCase
{
	private Connection connection = this.control.createMock(Connection.class);
	private DatabaseMetaData metaData = this.control.createMock(DatabaseMetaData.class);
	private ResultSet resultSet = this.control.createMock(ResultSet.class);
	
	/**
	 * Test method for {@link UniqueConstraint#collect(Connection, String, String, String)}
	 */
	public void testCollect()
	{
		try
		{
			EasyMock.expect(this.connection.getMetaData()).andReturn(this.metaData);
			
			EasyMock.expect(this.metaData.getIndexInfo(null, "schema", "table", true, false)).andReturn(this.resultSet);
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);
			
			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("pk");
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);

			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("idx");
			
			EasyMock.expect(this.resultSet.getString("COLUMN_NAME")).andReturn("col1");
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);

			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("idx");
			
			EasyMock.expect(this.resultSet.getString("COLUMN_NAME")).andReturn("col2");

			EasyMock.expect(this.resultSet.next()).andReturn(false);
			
			this.resultSet.close();
			
			this.control.replay();
			
			Collection<UniqueConstraint> collection = UniqueConstraint.collect(this.connection, "schema", "table", "pk");
			
			this.control.verify();
			
			assertNotNull(collection);
			assertEquals(1, collection.size());
			
			UniqueConstraint constraint = collection.iterator().next();
			
			assertEquals("idx", constraint.getName());
			assertEquals("schema", constraint.getSchema());
			assertEquals("table", constraint.getTable());
			
			List<String> columnList = constraint.getColumnList();
			
			assertNotNull(columnList);
			assertEquals(2, columnList.size());
			assertEquals("col1", columnList.get(0));
			assertEquals("col2", columnList.get(1));
		}
		catch (SQLException e)
		{
			fail(e);
		}
	}
	
	/**
	 * Test method for {@link UniqueConstraint#collect(Connection, String, String, String)}
	 */
	public void testCollectNoSchema()
	{
		try
		{
			EasyMock.expect(this.connection.getMetaData()).andReturn(this.metaData);
			
			EasyMock.expect(this.metaData.getIndexInfo(null, null, "table", true, false)).andReturn(this.resultSet);
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);
			
			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("pk");
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);

			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("idx");
			
			EasyMock.expect(this.resultSet.getString("COLUMN_NAME")).andReturn("col1");
			
			EasyMock.expect(this.resultSet.next()).andReturn(true);

			EasyMock.expect(this.resultSet.getString("INDEX_NAME")).andReturn("idx");
			
			EasyMock.expect(this.resultSet.getString("COLUMN_NAME")).andReturn("col2");

			EasyMock.expect(this.resultSet.next()).andReturn(false);
			
			this.resultSet.close();
			
			this.control.replay();
			
			Collection<UniqueConstraint> collection = UniqueConstraint.collect(this.connection, null, "table", "pk");
			
			this.control.verify();
			
			assertNotNull(collection);
			assertEquals(1, collection.size());

			UniqueConstraint constraint = collection.iterator().next();
			
			assertEquals("idx", constraint.getName());
			assertNull(constraint.getSchema());
			assertEquals("table", constraint.getTable());
			
			List<String> columnList = constraint.getColumnList();
			
			assertNotNull(columnList);
			assertEquals(2, columnList.size());
			assertEquals("col1", columnList.get(0));
			assertEquals("col2", columnList.get(1));
		}
		catch (SQLException e)
		{
			fail(e);
		}
	}

	/**
	 * Test method for {@link Constraint#hashCode()}
	 */
	public void testHashCode()
	{
		UniqueConstraint key = new UniqueConstraint("test", null, null);
		
		assertEquals("test".hashCode(), key.hashCode());
	}

	/**
	 * Test method for {@link Constraint#equals(Object)}
	 */
	public void testEqualsObject()
	{
		UniqueConstraint key1 = new UniqueConstraint("test", null, null);
		UniqueConstraint key2 = new UniqueConstraint("test", "", "");
		UniqueConstraint key3 = new UniqueConstraint("testing", "", "");
		
		assertTrue(key1.equals(key2));
		assertFalse(key1.equals(key3));
		assertFalse(key2.equals(key3));
	}
}
