package org.panbox.desktop.windows.vfs;

import org.panbox.desktop.common.vfs.PanboxFS;
import org.panbox.desktop.common.vfs.PanboxFSAdapter;

public class PanboxCBFSWindows extends PanboxFS {

	public PanboxCBFSWindows( PanboxFSAdapter backend ) {
		super(backend);
	}
}
