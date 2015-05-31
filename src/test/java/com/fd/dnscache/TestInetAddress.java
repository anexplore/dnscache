package com.fd.dnscache;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class TestInetAddress extends TestCase {
	public void testGetDns() throws UnknownHostException {
		InetAddress[] addrs = InetAddress.getAllByName("www.baidu.com.");
		for (InetAddress addr : addrs) {
			System.out.println(addr.getCanonicalHostName() + " "
					+ addr.getHostAddress());
		}
	}
}
