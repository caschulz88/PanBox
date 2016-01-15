/*
 * 
 *               Panbox - encryption for cloud storage 
 *      Copyright (C) 2014-2015 by Fraunhofer SIT and Sirrix AG 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additonally, third party code may be provided with notices and open source
 * licenses from communities and third parties that govern the use of those
 * portions, and any licenses granted hereunder do not alter any rights and
 * obligations you may have under such open source licenses, however, the
 * disclaimer of warranty and limitation of liability provisions of the GPLv3 
 * will apply to all the product.
 * 
 */
package org.panbox.desktop.windows.vfs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.panbox.Settings;
import org.panbox.desktop.common.vfs.PanboxFS;

import eldos.cbfs.ECBFSError;

public class VFSManager {

	private static final Logger logger = Logger
			.getLogger("org.panbox");

	private static VFSManager instance;

	private PanboxCBFSWindows vfs;

	private final String mountpoint;

	private VFSManager() throws ConfigurationException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		mountpoint = Settings.getInstance().getMountDir();
	}

	public static VFSManager getInstance() throws ConfigurationException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (instance == null)
			instance = new VFSManager();
		return instance;
	}
	
	public PanboxFS getVFS() {
		return vfs;
	}

	public synchronized void startVFS() {
		try {
			vfs = new PanboxCBFSWindows( new CBFSUserFS() );
	
			File mountFile = new File(mountpoint);
			
			if( vfs.mount(mountFile, false, null) )
			{
				WindowsOSIntegration.registerVFS(mountpoint.substring(0, 1));
			}
		} catch( ECBFSError ex ) {
			vfs = null;
			logger.error( "Failed to create/start VFS: ", ex );
		}
	}

	public synchronized void stopVFS() {
		if (vfs != null) {
			vfs.unmount();
			WindowsOSIntegration.unregisterVFS(mountpoint.substring(0, 1));

			vfs = null;
		}
	}

	public static synchronized boolean isRunning() {
		if (instance == null) {
			return false;
		} else if (instance.vfs == null) {
			return false;
		} else {
			return true;
		}
	}

}
