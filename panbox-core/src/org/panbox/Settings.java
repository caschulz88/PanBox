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
package org.panbox;

import org.apache.log4j.Logger;
import org.panbox.core.pairing.PAKCorePairingHandler.PairingType;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {

	private static final Logger logger = Logger.getLogger("org.panbox");

	private static Settings instance = null;

	private String language;
	private String panboxMountDir;
	private String panboxConfDir;
	private String deviceName;
	private boolean expertMode;
	private InetAddress pairingAddress;
	private NetworkInterface pairingInterface;
	private PairingType pairingType;

	private String dropboxAccessToken;
	private String dropboxSynchronizationDir;

	private boolean uriHandlerSupported;
	private boolean mailtoSchemeSupported;
	private boolean clipboardHandlerSupported;

	/*
	 * In case protected device key option has been activated the device key
	 * will also be encrypted with the identity password instead of using a well
	 * known password - this gives some extra security, but lowers the usability
	 * since identity password is needed to get entered on each PanBox app
	 * startup. Default value is false.
	 */
	// TODO: We need some extra check if old value was false and it has been set
	// to true afterwards - Re-encryption of the device key is needed!
	private boolean protectedDeviceKey;

	private final Preferences prefs;
	private final static String PANBOX_DEFAULT_CONF_DIR = System
			.getProperty("user.home") + File.separator + ".panbox";

	private Settings(Preferences prefs) {
		this.prefs = prefs;
		language = prefs.get("language", Locale.getDefault().toString());

		protectedDeviceKey = Boolean.valueOf(prefs.get("protectedDeviceKey",
				"false"));

		if(OS.getOperatingSystem().isWindows()) {
			panboxMountDir = prefs.get("mountDir", "P:\\");
		} else if(OS.getOperatingSystem().isLinux()) {
			panboxMountDir = prefs.get("mountDir", System.getProperty("user.home")
					+ File.separator + "panbox");
			if (!dirExists(panboxMountDir)) {
				logger.warn("Panbox mount-directory (" + panboxMountDir
						+ ") does not exist. Will create it now!");
				new File(panboxMountDir).mkdir();
			}
		}

		panboxConfDir = prefs.get("confDir", "");

		if (!dirExists(panboxConfDir)) {
			logger.error("Panbox directory for configuration (" + panboxConfDir
					+ ") does not exists, reset to default: "
					+ PANBOX_DEFAULT_CONF_DIR);
			panboxConfDir = PANBOX_DEFAULT_CONF_DIR;
		}

		// Make sure that config directory exists when accessing PANBOX_CONF_DIR
		File pbConf = new File(panboxConfDir);
		if (!pbConf.exists()) {
			pbConf.mkdir();
			prefs.put("confDir", panboxConfDir);
		}

		String hostname = "panbox-device";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warn("Unable to read default device name! (UnknownHostException)");
		} catch (Exception e) {
			logger.warn("Unable to read default device name! (RuntimeException)");
		}

		deviceName = prefs.get("deviceName", hostname);
		expertMode = Boolean.valueOf(prefs.get("expertMode", "false"));
		dropboxAccessToken = prefs.get("dropboxAccessToken", "");
		dropboxSynchronizationDir = prefs.get("dropboxSynchronizationDir", "");

		mailtoSchemeSupported = Boolean.valueOf(prefs.get(
				"mailtoSchemeSupported", "true"));
		uriHandlerSupported = Boolean.valueOf(prefs.get("uriHandlerSupported",
				"true"));
		clipboardHandlerSupported = Boolean.valueOf(prefs.get(
				"clipboardHandlerSupported", "true"));

		String pairingAddressStr = prefs.get("pairingAddress", "127.0.0.1");

		if (pairingAddressStr.equals("127.0.0.1")) {
			setPairingAddressAndInterface();
		} else {
			try {
				pairingAddress = InetAddress.getByName(pairingAddressStr);
				pairingInterface = NetworkInterface
						.getByInetAddress(pairingAddress);
			} catch (UnknownHostException | SocketException e) {
				logger.warn("Old network configuration has changed. Will reconfigure it now!");
				setPairingAddressAndInterface();
			}
		}

		// Set pairingType at default to SLAVE. We will set it to master when
		// pairing has been finished and was MASTER.
		pairingType = PairingType.valueOf(prefs.get("pairingType",
				PairingType.SLAVE.toString()));
	}

	/**
	 * Determines best network connection for pairing
	 * 
	 * Source:
	 * http://stackoverflow.com/questions/8462498/how-to-determine-internet
	 * -network-interface-in-java
	 */
	private void setPairingAddressAndInterface() {
		// iterate over the network interfaces known to java
		Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			logger.warn("Could not get list of host interfaces. Pairing might not work!");
			return;
		}
		OUTER: for (NetworkInterface interface_ : Collections.list(interfaces)) {
			// we shouldn't care about loopback addresses
			try {
				if (interface_.isLoopback())
					continue;
			} catch (SocketException e) {
				logger.warn("Problems determining if loopback device. Will ignore it.");
				continue;
			}
			
			// we should skip interfaces from VirtualBox
			if(interface_.toString().contains("VirtualBox")) {
				continue;
			}

			// if you don't expect the interface to be up you can skip this
			// though it would question the usability of the rest of the code
			try {
				if (!interface_.isUp())
					continue;
			} catch (SocketException e) {
				logger.warn("Problems determining if device is up. Will ignore it.");
				continue;
			}

			// iterate over the addresses associated with the interface
			Enumeration<InetAddress> addresses = interface_.getInetAddresses();
			for (InetAddress address : Collections.list(addresses)) {
				// look only for ipv4 addresses
				if (address instanceof Inet6Address)
					continue;

				// use a timeout big enough for your needs
				try {
					if (!address.isReachable(3000))
						continue;
				} catch (IOException e) {
					logger.warn("Problems determining if loopback device. Will ignore it.");
					continue;
				}

				// java 7's try-with-resources statement, so that
				// we close the socket immediately after use
				try (SocketChannel socket = SocketChannel.open()) {
					// again, use a big enough timeout
					socket.socket().setSoTimeout(3000);
					//socket.socket().setReuseAddress(true);
					socket.socket().setKeepAlive(false);
					socket.configureBlocking(false);

					// bind the socket to your local interface
					socket.bind(new InetSocketAddress(address, 8080));

					// try to connect to *somewhere*
					socket.connect(new InetSocketAddress("panbox.org", 80));
					
					socket.close();
				} catch (IOException ex) {
					logger.warn(
							"Could not use network address "
									+ address.getHostAddress(), ex);
					continue;
				}

				pairingInterface = interface_;
				pairingAddress = address;

				// stops at the first *working* solution
				break OUTER;
			}
		}
	}

	private Settings() {
		this.prefs = null;
	}

	public static Settings getInstance() {
		if (instance == null) {
			Preferences prefs = null;
			if (OS.getOperatingSystem().isWindows()) {
				prefs = new PreferencesRegistryWrapper();
			} else {
				prefs = Preferences.userNodeForPackage(Settings.class);
			}
			instance = new Settings(prefs);
		}
		return instance;
	}

	private boolean dirExists(String dir) {
		File f = new File(dir);
		return (f.exists() && f.isDirectory());
	}

	public String getLanguage() {
		return language;
	}

	public Locale getLocale() {
		if (!language.equals("system_default")) {
			String[] split = language.split("_");
			return new Locale(split[0], split[1]);
		} else {
			return Locale.getDefault();
		}
	}

	/* LANGUAGE */
	public void setLanguage(String lang) {
		language = lang;
		prefs.put("language", lang);
	}

	public String getMountDir() {
		return panboxMountDir;
	}

	/* MOUNT_DIR */
	public void setMountDir(String dir) {
//		if (dirExists(dir)) {
			panboxMountDir = dir;
			prefs.put("mountDir", dir);
//		} else {
//			logger.warn("[setMountDir] Directory '" + dir + "' does not exists");
//		}
	}

	public String getConfDir() {
		return panboxConfDir;
	}

	public void setConfDir(String dir) {
		if (dirExists(dir)) {
			panboxConfDir = dir;
			prefs.put("confDir", dir);
		} else {
			logger.warn("[setConfDir] Directory '" + dir + "' does not exists");
		}
	}

	public String getKeystorePath() {
		return panboxConfDir + File.separator + "keystore.jks";
	}

	public String getIdentityPath() {
		return panboxConfDir + File.separator + "identity.db";
	}

	public String getAdressbook() {
		return panboxConfDir + File.separator + "addressbook.db";
	}

	public String getSharesDBPath() {
		return panboxConfDir + File.separator + "shares.db";
	}

	public String getDevicesDBPath() {
		return panboxConfDir + File.separator + "devices.db";
	}

	public String getRecoveryDBPath() {
		return panboxConfDir + File.separator + "recovery.db";
	}

	public String getDeviceName() {
		return deviceName;
	}

	/* Device name shall not be changed after creation of the identity */
	public void setDeviceName(String panboxDeviceName) {
		this.deviceName = panboxDeviceName;
		prefs.put("deviceName", panboxDeviceName);
	}

	public void setExpertMode(boolean panboxExpertMode) {
		expertMode = panboxExpertMode;
		prefs.put("expertMode", Boolean.toString(panboxExpertMode));
	}

	public boolean getExpertMode() {
		return expertMode;
	}

	public boolean isUriHandlerSupported() {
		return uriHandlerSupported;
	}

	public void setUriHandlerSupported(boolean uriHandlerSupported) {
		this.uriHandlerSupported = uriHandlerSupported;
		prefs.put("uriHandlerSupported",
				Boolean.toString(this.uriHandlerSupported));
	}

	public boolean isMailtoSchemeSupported() {
		return mailtoSchemeSupported;
	}

	public void setMailtoSchemeSupported(boolean mailtoSchemeSupported) {
		this.mailtoSchemeSupported = mailtoSchemeSupported;
		prefs.put("mailtoSchemeSupported",
				Boolean.toString(this.mailtoSchemeSupported));
	}

	public boolean isClipboardHandlerSupported() {
		return clipboardHandlerSupported;
	}

	public void setClipboardHandlerSupported(boolean clipboardHandlerSupported) {
		this.clipboardHandlerSupported = clipboardHandlerSupported;
		prefs.put("clipboardHandlerSupported",
				Boolean.toString(this.clipboardHandlerSupported));
	}

	public String getDropboxAccessToken() {
		return dropboxAccessToken;
	}

	public void setDropboxAccessToken(String dropboxAccessToken) {
		this.dropboxAccessToken = dropboxAccessToken;
		prefs.put("dropboxAccessToken", dropboxAccessToken);
	}

	public String getDropboxSynchronizationDir() {
		return dropboxSynchronizationDir;
	}

	public void setDropboxSynchronizationDir(String dropboxSynchronizationDir) {
		this.dropboxSynchronizationDir = dropboxSynchronizationDir;
		prefs.put("dropboxSynchronizationDir", dropboxSynchronizationDir);
	}

	public InetAddress getPairingAddress() {
		return pairingAddress;
	}

	public NetworkInterface getPairingInterface() {
		return pairingInterface;
	}

	public void setPairingAddress(InetAddress pairingAddress) {
		this.pairingAddress = pairingAddress;
		prefs.put("pairingAddress", pairingAddress.getHostAddress());
	}

	public void flush() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public PairingType getPairingType() {
		return pairingType;
	}

	public void setPairingType(PairingType pairingType) {
		this.pairingType = pairingType;
		prefs.put("pairingType", pairingType.toString());
	}

	public boolean isSlave() {
		return !isMaster();
	}

	public boolean isMaster() {
		return pairingType == PairingType.MASTER;
	}

	public boolean isProtectedDeviceKey() {
		return protectedDeviceKey;
	}

	public void setProtectedDeviceKey(boolean protectedDeviceKey) {
		this.protectedDeviceKey = protectedDeviceKey;
		prefs.put("protectedDeviceKey",
				Boolean.toString(this.protectedDeviceKey));
	}
}
