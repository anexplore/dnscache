package com.fd.dnscache;

import java.net.InetAddress;

import com.fd.dnscache.DNS;
import com.fd.dnscache.DnsCache;

import junit.framework.TestCase;

public class TestDnsCache extends TestCase {

	public void testDnsCache() throws Exception {
		DnsCache cache = new DnsCache("cache");
		InetAddress inet = cache.getAndPutInetAddressInCache("www.baidu.com");
		assertTrue(inet != null);
		System.out.println(inet.toString());
		DNS dns = cache.getAndPutDNSInCache("www.baidu.com");
		System.out.println(dns.getUsedCounter());
		assertTrue(dns.getUsedCounter() == 1);
		System.out.println(dns.toString());
		DNS dns1 = cache.getAndPutDNSInCache("www.baidu.com");
		assertTrue(dns == dns1);
	}
}
