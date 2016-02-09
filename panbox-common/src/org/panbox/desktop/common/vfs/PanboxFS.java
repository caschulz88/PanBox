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
package org.panbox.desktop.common.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.panbox.core.exception.ObfuscationException;
import org.panbox.core.vfs.backend.VirtualFile;
import org.panbox.desktop.common.gui.PanboxDesktopGUIConstants;
import org.panbox.desktop.common.vfs.backend.IRootVolume;
import org.panbox.desktop.common.vfs.backend.VirtualRootVolume;
import org.panbox.desktop.common.vfs.backend.exceptions.SecretKeyNotFoundException;

/**
 * @author palige
 * 
 *         Abstract class with platform independent code for the VFS. Actual VFS
 *         API calls are to be implemented by platform-specific subclasses.
 */
public abstract class PanboxFS {

	private static final Logger logger = Logger
			.getLogger("org.panbox.desktop.common");

	protected final PanboxFSAdapter vfsAdapter;
	private File mountPoint = null;

	public final IRootVolume backingStorage;

	public PanboxFS(PanboxFSAdapter thisFS) {
		this.vfsAdapter = thisFS;
		this.backingStorage = VirtualRootVolume.getInstance();
	}

	private final boolean _mount(final File mountPoint,
			final boolean wasBlocking) {
		this.mountPoint = SymlinkResolver.resolveSymlinks(mountPoint);
		if (UnconsequentialFiles.clearUnconsequentialFiles(this.mountPoint)) {
			log("Mount",
					"Some unconsequential files were automatically deleted from mountpoint: "
							+ this.mountPoint);
		}
		log("Mount", "Mounting to: " + this.mountPoint);
		final long beforeTime = System.nanoTime();
		final boolean result = vfsAdapter.userfs_mount(this, mountPoint);
		final long time = (System.nanoTime() - beforeTime) / 1000000L;
		log("Mount", (wasBlocking ? "B" : "Non-b")
				+ "locking attempt to mount at '" + this.mountPoint + "' ("
				+ time + "ms ago) finished; resulted in "
				+ (result ? "success" : "failure") + ".");
		return result;
	}

	final boolean _onUnmount() {
		log("Unmount", "Unmounting.");
		return onUnmount(mountPoint);
	}

	public final File getMountpoint() {
		return mountPoint;
	}

	// protected abstract int setChown(String path, long uid, long gid);

	final void implLog(final String method, final String message) {
		logger.debug("PanboxFS [Backend] : " + method + " : " + message);
	}

	private final void log(final String method, final String message) {
		logger.debug("PanboxFS : " + method + " : " + message);
	}

	public final boolean mount(final File mountPoint, final boolean blocking,
			final MountFailureHandler failureHandler) {
		if (!blocking) {
			new Thread() {
				@Override
				public void run() {
					if (!_mount(mountPoint, false)) {
						if (failureHandler != null) {
							failureHandler.exec();
						}
					}
				}
			}.start();
			return true;
		}
		return _mount(mountPoint, true);
	}

	protected boolean onUnmount(final File mountPoint) {
		return true;
	}

	public final boolean unmount() {
		return vfsAdapter.userfs_unmount(mountPoint);
	}

	/**
	 * This will return the name of the file system for the Panbox VFS
	 * 
	 * @return String name of the Panbox VFS
	 */
	protected String getFilesystemName() {
		return PanboxDesktopGUIConstants.PANBOX_VIRTUAL_FILESYSTEM_IDENTIFIER;
	}

	/**
	 * This will return the bytes that are free for the file or directory
	 * 
	 * @return long Number of free bytes
	 */
	protected long getFreeBytes() {
		return backingStorage.getFreeSpace();
	}

	/**
	 * This will return the total bytes that available for the file or directory
	 * 
	 * @return long Number of total bytes
	 */
	protected long getTotalBytes() {
		return backingStorage.getTotalSpace();
	}

	/**
	 * This will return the bytes that are usable for the file or directory
	 * 
	 * @return long Number of usable bytes
	 */
	protected long getUsableBytes() {
		return backingStorage.getUsableSpace();
	}

	/**
	 * This will return the volume name of the Panbox VFS drive
	 * 
	 * @return String volume name of the Panbox VFS
	 */
	protected String getVolumeName() {
		// The name will be configured via system-specific configuration
		return "";
	}

	protected synchronized VirtualFile getVirtualFileForFileName(
			final String fileName, boolean createIV)
			throws SecretKeyNotFoundException, FileNotFoundException,
			ObfuscationException {
		String realFileName = fileName;
		try {
			realFileName = backingStorage.obfuscatePath(fileName, createIV);
		} catch (SecretKeyNotFoundException ex) {
			if (!fileName.equals(File.separator)) {
				throw ex;
			}
		}
		try {
			return backingStorage.getFile(realFileName);
			// // if (ret instanceof VirtualRandomAccessFile) {
			// return (VirtualRandomAccessFile) ret;
		} catch (IOException e) {
			logger.error("PanboxFS: getVirtualFileForFileName : Exception in getFile: "
					+ e.getMessage());
			return null;
		}
	}

	/**
	 * getFileInfo will get a FileInfo object for the specified file in the VFS
	 * with or without obfuscation. The file information could be obtained from
	 * a file or a directory and will contain at least the filename and the size
	 * of a file.
	 * 
	 * @param fileName
	 *            Relative path to the file or directory for which the file
	 *            information should be read
	 * @param alreadyObfuscated
	 *            true if the relative path in fileName is already obfuscated
	 *            else false
	 * @param outputObfuscated
	 *            true if the filename that will be returned in the FileInfo
	 *            object should be obfuscated else false
	 * @return
	 * @throws IOException
	 */
	// @SuppressWarnings("resource")
	protected synchronized AbstractFileInfo getFileInfo(final String fileName,
			boolean alreadyObfuscated, boolean outputObfuscated)
			throws IOException {
		logger.debug("getFileInfo : " + fileName + ", alreadyObfuscated: "
				+ alreadyObfuscated + ", outputObfuscated:  "
				+ outputObfuscated);

		VirtualFile backing;
		try {
			backing = alreadyObfuscated ? backingStorage.getFile(fileName)
					: getVirtualFileForFileName(fileName, false);
		} catch (ObfuscationException e1) {
			throw new IOException("Obfuscation for filename " + fileName
					+ " failed!");
		}

		boolean symbolic = backing.isSymbolic();

		if (!symbolic && !backing.exists()) {
			// do NOT close backing file as it may not have been opened at this
			// point
			// backing.close();
			throw new FileNotFoundException(
					"PanboxFS : getFileInfo : Can not get file info for non-existing files!");
		}

		long createTime = backing.getCreationTime();
		long lastAccess = backing.getLastAccessTime();
		long lastWrite = backing.getLastWriteTime();

		AbstractFileInfo ret;

		String deobfPath = null;
		if (fileName.equals("/")) {
			deobfPath = fileName;
		} else {
			if (!outputObfuscated && alreadyObfuscated) {

				try {
					deobfPath = backingStorage.deobfuscatePath(backing);
				} catch (ObfuscationException e) {
					// TODO better handling in case of filenames which could not
					// be deobfuscated. E.g. lost+found folder, or something
					// similar
					throw new FileNotFoundException("Deobfuscation error: "
							+ e.getMessage());
				}
			} else {
				deobfPath = fileName;
			}
		}

		if (backing.isDirectory()) {

			if (!outputObfuscated) {
				ret = this.vfsAdapter.createFileInfo(deobfPath, true, 0,
						createTime, lastAccess, lastWrite, backing.getAttr(),
						symbolic);
			} else {
				ret = this.vfsAdapter.createFileInfo(
						backingStorage.getRelativePathForFile(backing), true,
						0, createTime, lastAccess, lastWrite,
						backing.getAttr(), symbolic);
			}
		} else {
			long vlen = backing.length();
			if (!outputObfuscated) {
				ret = this.vfsAdapter.createFileInfo(deobfPath, false, vlen,
						createTime, lastAccess, lastWrite, backing.getAttr(),
						symbolic);
			} else {
				ret = this.vfsAdapter.createFileInfo(
						backingStorage.getRelativePathForFile(backing), false,
						vlen, createTime, lastAccess, lastWrite,
						backing.getAttr(), symbolic);
			}
		}
		logger.debug("getFileInfo, GetFileInfo returned : " + ret);
		return ret;
	}
}
