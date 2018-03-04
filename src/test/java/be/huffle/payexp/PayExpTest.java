package be.huffle.payexp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

public class PayExpTest
{
	@SuppressWarnings("unused")
	private ServerMock server;
	@SuppressWarnings("unused")
	private PayExp plugin;

	@Before
	public void setUp() throws Exception
	{
		server = MockBukkit.mock();
		plugin = MockBukkit.load(PayExp.class);
	}
	
	@After
	public void tearDown()
	{
		MockBukkit.unload();
	}

	@Test
	public void test()
	{
		fail("Not implemented");
	}

}
