/*
 * HA-JDBC: High-Availablity JDBC
 * Copyright 2004-Sep 14, 2009 Paul Ferraro
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
 */
package net.sf.hajdbc.lock.distributed;

import java.util.concurrent.locks.Lock;

/**
 * @author paul
 *
 */
public class CoordinatorReleaseCommand extends CoordinatorLockCommand<Void>
{
	private static final long serialVersionUID = -925862880236957178L;
	
	public CoordinatorReleaseCommand(RemoteLockDescriptor descriptor)
	{
		super(descriptor);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.lock.distributed.CoordinatorLockCommand#execute(net.sf.hajdbc.distributable.jgroups.LockCommandContext, java.util.concurrent.locks.Lock)
	 */
	@Override
	protected Void execute(Lock lock)
	{
		lock.unlock();
		
		return null;
	}

	@Override
	public Void unmarshalResult(Object result)
	{
		return (Void) result;
	}

	@Override
	public Object marshalResult(Void result)
	{
		return result;
	}
}