/*******************************************************************************
 * Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
 *
 * This file is part of Proxy2Tor.
 * Proxy2Tor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * Proxy2Tor is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package it.uniroma2.sii.repository;

import it.uniroma2.sii.App;
import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.service.tor.OnionBinderService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test su {@link OnionBinderRepository}.
 * 
 * @author Andrea Mayer
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:" + App.APPLICATION_CONTEXT_XML })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class OnionBinderRepositoryTest {
	@Autowired
	private OnionBinderRepository onionBinderRepository;
	@Autowired
	private OnionBinderService onionBinderService;

	@Test
	@Transactional
	public void test() throws InterruptedException, IOException {
		// onionBinderRepository.registerOnionByName("duck.onion");
		// OnionBinder onionBinder = onionBinderRepository
		// .findByOnionName("duck.onion");
		// System.out.println(onionBinder.getExpirationTimestamp());
		//
		// int ndelete = onionBinderRepository
		// .deleteOnionBinderWithExpirationTimeLessOrEqualTo(new Date(
		// System.currentTimeMillis()));
		// System.out.println("cancellati:" + ndelete);
		
		final OnionBinder o = onionBinderService.registerOnionByName("duck.go.onion");
		System.out.println(o.getExpirationTimestamp());
		final OnionBinder o2 = onionBinderService.registerOnionByName("bogo.onion");
		System.out.println(o2.getExpirationTimestamp());
		final InetSocketAddress sockAddress = new InetSocketAddress(o.getInetAddress(), 80);
		SocketAddress sz = onionBinderService.resolveCachedOnionNameByInternalInetSocketAddress(sockAddress);
		System.out.println(sz);
	}
}
