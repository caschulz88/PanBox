package org.panbox.desktop.common.recovery;

import org.panbox.desktop.common.recovery.RecoveryManagerImpl.RecoveryException;

public interface IRecoveryManager {

	boolean isFileAlreadyMarkedAsMissing(String filePath) throws RecoveryException;

	void markMissingIVForFile(String filePath) throws RecoveryException;

}