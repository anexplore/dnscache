package com.fd;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DNS Item
 * 
 * @author caoliuyi
 *
 */
public class DNS implements Serializable{
	private static final long serialVersionUID = 1098572221246444542L;
	private InetAddress[] addrs;
	private String host;
	private long ttl; // seconds
	private AtomicLong usedCounter;

	public final static DNS WAITING_DNS = new DNS(null, null, 0);
	public final static DNS FAILED_DNS = new DNS(null, null, 0);

	/**
	 * constructor
	 * 
	 * @param addrs
	 *            InetAddrss array
	 * @param host
	 *            host name
	 * @param ttl
	 *            time to live, seconds
	 */
	public DNS(InetAddress[] addrs, String host, long ttl) {
		this.addrs = addrs;
		this.host = host;
		this.ttl = ttl;
		usedCounter = new AtomicLong(0);
	}

	/**
	 * get this dns used counter
	 * 
	 * @return used counter
	 */
	public long incrementUsedCounter() {
		return usedCounter.getAndIncrement();
	}
	
	public long getUsedCounter(){
		return usedCounter.get();
	}
	
	public InetAddress[] getAddrs() {
		return addrs;
	}

	public void setAddrs(InetAddress[] addrs) {
		this.addrs = addrs;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	@Override
	public String toString() {
		return "DNS [addrs=" + Arrays.toString(addrs) + ", host=" + host
				+ ", ttl=" + ttl + ", usedCounter=" + usedCounter + "]";
	}

	
}
