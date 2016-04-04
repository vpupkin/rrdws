package org.jrobin.mrtg.server;

import org.jrobin.mrtg.MrtgException;

public class NoRouterException extends MrtgException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7577835081757372106L;

	public NoRouterException(String cause) {
		super(cause); 
	}
 
}
