/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.jrobin.core;

import java.io.IOException;

class RrdInt extends RrdPrimitive {
	private int cache;
	private boolean cached = false;

	RrdInt(RrdUpdater updater, boolean isConstant) throws IOException {
		super(updater, RrdPrimitive.RRD_INT, isConstant);
	}

	RrdInt(RrdUpdater updater) throws IOException {
		this(updater, false);
	}

	void set(int value) throws IOException {
		if (!isCachingAllowed()) {
			writeInt(value);
		}
		// caching allowed
		else if (!cached || cache != value) {
			// update cache
			writeInt(cache = value);
			cached = true;
		}
	}

	int get() throws IOException {
		return cached ? cache : readInt();
	}
}