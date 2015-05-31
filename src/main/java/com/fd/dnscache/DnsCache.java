package com.fd.dnscache;

import java.net.InetAddress;
import java.util.ArrayList;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.fd.simplecache.Cache;
import com.fd.simplecache.Element;

/**
 * Dns cache supply get dns, cache dns
 * 
 * @author caoliuyi
 *
 */
public class DnsCache {
	private String cacheName;
	private int maxElementCount = 10000;
	private Cache cache = null;

	/**
	 * constructor
	 * 
	 * @param name
	 *            cache name
	 */
	public DnsCache(String name) {
		this(name, 10000);
	}

	/**
	 * 
	 * @param name
	 *            cache name
	 * @param maxCount
	 *            max items in cache
	 */
	public DnsCache(String name, int maxCount) {
		this.cacheName = name;
		this.maxElementCount = maxCount;
		cache = new Cache(cacheName, maxElementCount);
	}

	/**
	 * @param host
	 * @return InetAddress null if error
	 * @throws Exception
	 */
	public InetAddress getAndPutInetAddressInCache(String host)
			throws Exception {
		return getAndPutInetAddressInCache(host, -1);
	}

	/**
	 * 
	 * @param host
	 * @param ttl
	 *            if ttl < 0, use dns's ttl, else use ttl
	 * @return InetAddress null if error
	 * @throws Exception
	 */
	public InetAddress getAndPutInetAddressInCache(String host, long ttl)
			throws Exception {
		DNS dns = getAndPutDNSInCache(host, ttl);
		if (dns == null) {
			return null;
		}
		return getInetAddressInDnsByRound(dns);
	}

	/**
	 * @param host
	 * @return InetAddress[], null if error
	 * @throws Exception
	 */
	public InetAddress[] getAndPutInetAddressArrayInCache(String host)
			throws Exception {
		return getAndPutInetAddressArrayInCache(host, -1);
	}

	/**
	 * @param host
	 * @param ttl
	 * @return InetAddress[], null if error
	 * @throws Exception
	 */
	public InetAddress[] getAndPutInetAddressArrayInCache(String host, long ttl)
			throws Exception {
		DNS dns = getAndPutDNSInCache(host, ttl);
		if (dns == null) {
			return null;
		}
		return dns.getAddrs();
	}

	/**
	 * return DNS of host if dns not in cache, will try get get and put dns to
	 * cache
	 * 
	 * @param host
	 * @return DNS
	 * @throws Exception
	 */
	public DNS getAndPutDNSInCache(String host) throws Exception {
		return getAndPutDNSInCache(host, -1);
	}

	/**
	 * return DNS of host if dns in cache; if dns not in cache, will try get get
	 * and put dns to cache
	 * 
	 * @param host
	 * @param defaultTimeToLive
	 *            if < 0, then use dns's ttl, else use defaultTimeToLive
	 * @return null if error
	 * @throws Exception
	 */
	public DNS getAndPutDNSInCache(String host, long defaultTimeToLive)
			throws Exception {
		if (host == null || host.isEmpty()) {
			return null;
		}
		final Element ele = cache.get(host);
		DNS dns = null;
		// expired or not in cache
		if (ele == null) {
			dns = getDns(host);
			if (dns == null) {
				cache.put(new Element(host, DNS.FAILED_DNS, 60000));
			} else {
				// put to cache
				if (defaultTimeToLive >= 0) {
					cache.put(new Element(host, dns, defaultTimeToLive * 1000));
				} else {
					cache.put(new Element(host, dns, dns.getTtl() * 1000));
				}
			}
		} else {
			// exists in cache
			dns = (DNS) ele.getValue();
			// if is failed, do not try again
			if (dns == DNS.FAILED_DNS) {
				return null;
			}
			dns.incrementUsedCounter();
		}

		return dns;
	}

	/**
	 * Get Dns of host
	 * 
	 * @param host
	 * @return null if error, else DNS instance
	 */
	public DNS getDns(String host) {
		if (host == null || host.isEmpty()) {
			return null;
		}
		DNS res = getDnsByJavaDns(host);
		if (res == null) {
			res = getDnsBySystem(host);
		}
		return res;
	}

	private InetAddress getInetAddressInDnsByRound(DNS dns) {
		if (dns == null) {
			return null;
		}
		InetAddress[] addrs = dns.getAddrs();
		int idx = (int) (dns.getUsedCounter() % addrs.length);
		return addrs[idx];
	}

	private DNS getDnsBySystem(String host) {
		InetAddress[] addrs = getInetAddressBySystem(host);
		if (addrs == null) {
			System.err.println("get Dns error:" + host);
			return null;
		}
		return new DNS(addrs, host, 3600);
	}

	private DNS getDnsByJavaDns(String host) {
		if (host == null || host.isEmpty()) {
			return null;
		}
		long maxTtl = 0;
		String hostTmp = host.endsWith(".") ? host : host + ".";
		Record[] recordSet = null;
		try {
			recordSet = (new Lookup(hostTmp, Type.A, DClass.IN)).run();
		} catch (TextParseException e) {
		}
		ArrayList<InetAddress> addrs = new ArrayList<InetAddress>(3);
		if (recordSet != null) {
			for (Record record : recordSet) {
				if (record.getType() != Type.A) {
					continue;
				}
				ARecord arecord = (ARecord) record;
				addrs.add(arecord.getAddress());
				if (arecord.getTTL() > maxTtl) {
					maxTtl = arecord.getTTL();
				}
			}
		}
		if (addrs.size() == 0) {
			return null;
		}
		return new DNS(addrs.toArray(new InetAddress[0]), host, maxTtl);
	}

	/**
	 * @param host
	 *            host name
	 * @return InetAddress array, null if error
	 */
	public InetAddress[] getInetAddressBySystem(String host) {
		if (host == null || host.isEmpty()) {
			return null;
		}
		try {
			return InetAddress.getAllByName(host);
		} catch (Exception ignore) {
		}
		return null;
	}

	/**
	 * 
	 * @param host
	 *            host name
	 * @return InetAddress array, null if error
	 */
	public InetAddress[] getInetAddressByJavaDns(String host) {
		if (host == null || host.isEmpty()) {
			return null;
		}
		host = host.endsWith(".") ? host : host + ".";
		Record[] recordSet = null;
		try {
			recordSet = (new Lookup(host, Type.A, DClass.IN)).run();
		} catch (TextParseException e) {
		}
		ArrayList<InetAddress> addrs = new ArrayList<InetAddress>(3);
		if (recordSet != null) {
			for (Record record : recordSet) {
				if (record.getType() != Type.A) {
					continue;
				}
				ARecord arecord = (ARecord) record;
				addrs.add(arecord.getAddress());
			}
		}
		if (addrs.size() == 0) {
			return null;
		}
		return addrs.toArray(new InetAddress[0]);
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public int getMaxElementCount() {
		return maxElementCount;
	}

	public void setMaxElementCount(int maxElementCount) {
		this.maxElementCount = maxElementCount;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

}
