/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (c) 2004-2007 Paul Ferraro
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
package net.sf.hajdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.cache.ForeignKeyConstraint;
import net.sf.hajdbc.cache.ForeignKeyConstraintImpl;
import net.sf.hajdbc.cache.QualifiedName;
import net.sf.hajdbc.cache.SequenceProperties;
import net.sf.hajdbc.cache.TableProperties;

import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 *
 */
@SuppressWarnings("nls")
public class MaxDBDialectTest extends StandardDialectTest
{
	public MaxDBDialectTest()
	{
		super(DialectFactoryEnum.MAXDB);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSequenceSupport()
	 */
	@Override
	public void getSequenceSupport()
	{
		Assert.assertSame(this.dialect, this.dialect.getSequenceSupport());
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getCreateForeignKeyConstraintSQL()
	 */
	@Override
	public void getCreateForeignKeyConstraintSQL() throws SQLException
	{
		ForeignKeyConstraint key = new ForeignKeyConstraintImpl("name", "table");
		key.getColumnList().add("column1");
		key.getColumnList().add("column2");
		key.setForeignTable("foreign_table");
		key.getForeignColumnList().add("foreign_column1");
		key.getForeignColumnList().add("foreign_column2");
		key.setDeferrability(DatabaseMetaData.importedKeyInitiallyDeferred);
		key.setDeleteRule(DatabaseMetaData.importedKeyCascade);
		key.setUpdateRule(DatabaseMetaData.importedKeyRestrict);
		
		String result = this.dialect.getCreateForeignKeyConstraintSQL(key);
		
		Assert.assertEquals("ALTER TABLE table ADD CONSTRAINT name FOREIGN KEY (column1, column2) REFERENCES foreign_table (foreign_column1, foreign_column2) ON DELETE CASCADE", result);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSequences()
	 */
	@Override
	public void getSequences() throws SQLException
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		Connection connection = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		
		when(metaData.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
		when(statement.executeQuery("SELECT SEQUENCE_NAME, INCREMENT_BY FROM USER_SEQUENCES")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
		when(resultSet.getString(1)).thenReturn("sequence1").thenReturn("sequence2");
		when(resultSet.getInt(2)).thenReturn(1).thenReturn(2);
		
		Map<QualifiedName, Integer> result = this.dialect.getSequenceSupport().getSequences(metaData);

		verify(statement).close();
		
		Assert.assertEquals(2, result.size());
		
		Iterator<Map.Entry<QualifiedName, Integer>> entries = result.entrySet().iterator();
		Map.Entry<QualifiedName, Integer> entry = entries.next();

		Assert.assertNull(entry.getKey().getSchema());
		Assert.assertEquals("sequence1", entry.getKey().getName());
		Assert.assertEquals(1, entry.getValue().intValue());
		
		entry = entries.next();

		Assert.assertNull(entry.getKey().getSchema());
		Assert.assertEquals("sequence2", entry.getKey().getName());
		Assert.assertEquals(2, entry.getValue().intValue());
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSimpleSQL()
	 */
	@Override
	public void getSimpleSQL() throws SQLException
	{
		Assert.assertEquals("SELECT SYSDATE FROM DUAL", this.dialect.getSimpleSQL());
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getTruncateTableSQL()
	 */
	@Override
	public void getTruncateTableSQL() throws SQLException
	{
		TableProperties table = mock(TableProperties.class);
		
		when(table.getName()).thenReturn("table");
		
		String result = this.dialect.getTruncateTableSQL(table);
		
		Assert.assertEquals("TRUNCATE TABLE table", result);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		Assert.assertEquals("sequence", support.parseSequence("SELECT sequence.nextval"));
		Assert.assertEquals("sequence", support.parseSequence("SELECT sequence.currval"));
		Assert.assertEquals("sequence", support.parseSequence("SELECT sequence.nextval, * FROM table"));
		Assert.assertEquals("sequence", support.parseSequence("SELECT sequence.currval, * FROM table"));
		Assert.assertEquals("sequence", support.parseSequence("INSERT INTO table VALUES (sequence.nextval, 0)"));
		Assert.assertEquals("sequence", support.parseSequence("INSERT INTO table VALUES (sequence.currval, 0)"));
		Assert.assertEquals("sequence", support.parseSequence("UPDATE table SET id = sequence.nextval"));
		Assert.assertEquals("sequence", support.parseSequence("UPDATE table SET id = sequence.currval"));
		Assert.assertNull(support.parseSequence("SELECT NEXT VALUE FOR sequence"));
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getNextSequenceValueSQL()
	 */
	@Override
	public void getNextSequenceValueSQL() throws SQLException
	{
		SequenceProperties sequence = mock(SequenceProperties.class);
		
		when(sequence.getName()).thenReturn("sequence");
		
		String result = this.dialect.getSequenceSupport().getNextSequenceValueSQL(sequence);

		Assert.assertEquals("SELECT sequence.NEXTVAL FROM DUAL", result);
	}
}