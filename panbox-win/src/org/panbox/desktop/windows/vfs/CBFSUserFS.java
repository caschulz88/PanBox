package org.panbox.desktop.windows.vfs;

import java.io.File;
import java.util.Date;

import org.panbox.desktop.common.vfs.AbstractFileInfo;
import org.panbox.desktop.common.vfs.PanboxFS;
import org.panbox.desktop.common.vfs.PanboxFSAdapter;

import eldos.cbfs.CallbackFileSystem;
import eldos.cbfs.ECBFSError;
import eldos.cbfs.ICbFsDirectoryEnumerationInfo;
import eldos.cbfs.ICbFsEnumerateEvents;
import eldos.cbfs.ICbFsFileEvents;
import eldos.cbfs.ICbFsFileInfo;
import eldos.cbfs.ICbFsHandleInfo;
import eldos.cbfs.ICbFsStorageEvents;
import eldos.cbfs.ICbFsVolumeEvents;
import eldos.cbfs.ServiceStatus;
import eldos.cbfs.boolRef;
import eldos.cbfs.byteArrayRef;
import eldos.cbfs.dateRef;
import eldos.cbfs.intRef;
import eldos.cbfs.longRef;
import eldos.cbfs.stringRef;

public class CBFSUserFS implements PanboxFSAdapter, ICbFsStorageEvents, ICbFsVolumeEvents, ICbFsEnumerateEvents, ICbFsFileEvents {

	private static final String PRODUCT_NAME = "PanBox-WindowsClient";
	
	private static final String REGISTRATION_KEY = "XXX";
	
	private static final String MOUNT_POINT = "P:";
	
	public CBFSUserFS() throws ECBFSError {
		CallbackFileSystem cbfs = new CallbackFileSystem( this );
        cbfs.initialize(PRODUCT_NAME);
        cbfs.setRegistrationKey(REGISTRATION_KEY);
        
        // install driver
		boolean supportPnP = false;
		int modulesToInstall = CallbackFileSystem.CBFS_MODULE_MOUNT_NOTIFIER_DLL | CallbackFileSystem.CBFS_MODULE_NET_REDIRECTOR_DLL;
		boolRef rebootNeeded = new boolRef();
		try {
			String CabFileName = "C:\\Program Files\\EldoS\\Callback File System\\Drivers\\cbfs.cab";
			cbfs.install(CabFileName, PRODUCT_NAME, "C:\\Windows\\System32", supportPnP, false, modulesToInstall, rebootNeeded);
		} catch (ECBFSError e) {
			e.printStackTrace();
		}
		
		// check
        boolRef installed = new boolRef();
        longRef highVersion = new longRef();
        longRef lowVersion = new longRef();
        ServiceStatus status = new ServiceStatus();
        try {
            cbfs.getModuleStatus(PRODUCT_NAME, CallbackFileSystem.CBFS_MODULE_DRIVER, installed, highVersion, lowVersion, status);
            if (!installed.getValue()) {
                System.out.println("driver is not installed");
            } else {
                System.out.println("driver version " + highVersion.getValue() + ":" + lowVersion.getValue() + " is installed");
                System.out.println("driver status: " + status.CurrentState);
            }
        } catch (ECBFSError ecbfsError) {
            System.out.println("failed to check module status");
            ecbfsError.printStackTrace();
        }
        
        cbfs.setSerializeCallbacks(true);
        //cbfs.setThreadPoolSize(1);
        cbfs.setProcessRestrictionsEnabled(false);
        cbfs.createStorage();
        //cbfs.disableMetaDataCache(true);
        cbfs.addMountingPoint(MOUNT_POINT);
        cbfs.mountMedia(0);
	}
	
	@Override
	public boolean userfs_mount(PanboxFS panboxFS, File mountPoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean userfs_unmount(File mountPoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractFileInfo createFileInfo(String fileName, boolean isDirectory, long fileSize, long creationTime,
			long lastAccessTime, long lastWriteTime, long attr, boolean isSymbolic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCanFileBeDeleted(CallbackFileSystem arg0, ICbFsFileInfo arg1, ICbFsHandleInfo arg2, boolRef arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCleanupFile(CallbackFileSystem arg0, ICbFsFileInfo arg1, ICbFsHandleInfo arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCloseFile(CallbackFileSystem arg0, ICbFsFileInfo arg1, ICbFsHandleInfo arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateFile(CallbackFileSystem arg0, String arg1, long arg2, long arg3, long arg4, ICbFsFileInfo arg5,
			ICbFsHandleInfo arg6) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteFile(CallbackFileSystem arg0, ICbFsFileInfo arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFlushFile(CallbackFileSystem arg0, ICbFsFileInfo arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetFileInfo(CallbackFileSystem arg0, String arg1, boolRef arg2, dateRef arg3, dateRef arg4,
			dateRef arg5, dateRef arg6, longRef arg7, longRef arg8, longRef arg9, longRef arg10, longRef arg11,
			stringRef arg12, stringRef arg13) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetFileNameByFileId(CallbackFileSystem arg0, long arg1, String arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpenFile(CallbackFileSystem arg0, String arg1, long arg2, long arg3, long arg4, ICbFsFileInfo arg5,
			ICbFsHandleInfo arg6) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReadFile(CallbackFileSystem arg0, ICbFsFileInfo arg1, long arg2, byteArrayRef arg3, int arg4,
			intRef arg5) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRenameOrMove(CallbackFileSystem arg0, ICbFsFileInfo arg1, String arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetAllocationSize(CallbackFileSystem arg0, ICbFsFileInfo arg1, long arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetEndOfFile(CallbackFileSystem arg0, ICbFsFileInfo arg1, long arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetFileAttributes(CallbackFileSystem arg0, ICbFsFileInfo arg1, ICbFsHandleInfo arg2, Date arg3,
			Date arg4, Date arg5, Date arg6, long arg7) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetValidDataLength(CallbackFileSystem arg0, ICbFsFileInfo arg1, long arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWriteFile(CallbackFileSystem arg0, ICbFsFileInfo arg1, long arg2, byteArrayRef arg3, int arg4,
			intRef arg5) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCloseDirectoryEnumeration(CallbackFileSystem arg0, ICbFsFileInfo arg1,
			ICbFsDirectoryEnumerationInfo arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnumerateDirectory(CallbackFileSystem arg0, ICbFsFileInfo arg1, ICbFsHandleInfo arg2,
			ICbFsDirectoryEnumerationInfo arg3, String arg4, int arg5, boolean arg6, boolRef arg7, stringRef arg8,
			stringRef arg9, dateRef arg10, dateRef arg11, dateRef arg12, dateRef arg13, longRef arg14, longRef arg15,
			longRef arg16, longRef arg17) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIsDirectoryEmpty(CallbackFileSystem arg0, ICbFsFileInfo arg1, String arg2, boolRef arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetVolumeID(CallbackFileSystem arg0, intRef arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetVolumeLabel(CallbackFileSystem arg0, stringRef arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetVolumeSize(CallbackFileSystem arg0, longRef arg1, longRef arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetVolumeLabel(CallbackFileSystem arg0, String arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMount(CallbackFileSystem arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStorageEjected(CallbackFileSystem arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnmount(CallbackFileSystem arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
