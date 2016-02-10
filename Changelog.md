PanBox OSE Changelog
====================

Version 1.1.3
-------------

Released on 10th February 2016 for Windows and Linux

Features:

* Added version check to Desktop client and show information message if newer version is available.

Bugfixes:

* Fixed bug in network/bluetooth based pairing of a new slave device were encryption and signature certificates were interchanged in Desktop version
* Fixed missing German translation for remove device/contact button on share list entry
* Fixed remove device button on device management, which was clickable even if it was disabled

Version 1.1.2
-------------

Released on 10th December 2015 for Windows

* Windows 10/Server 2016: Fixed issue that no files/folders can be deleted

Version 1.1.1
-------------

Released on 17th November 2015 for Windows

* Windows: Fixed issue on some systems 'Not enough disk space on the destination drive.'

Version 1.1
-----------

Released on 11th November 2015 for Windows, Linux and Android

Desktop:

* In case of installed Dropbox client the path to the synchronization directory is pre-defined in Dropbox wizard dialog
* Introduced auto-detection of best LAN interface to be used for LAN pairings instead of using 127.0.0.1 as default
* Device file pairing dialog order has changed so that file name can be pre-defined with a default file name
* Disable add device/contact in case no share exists
* Fixed corrupt configurations tab and GUI in case of system default locale was set to de_AT, de_CH, en_UZ, en_GB, etc.
* E-Mail verification regex has been replaced with RFC 2822 regex to better support special DNS domains
* Introduced remove devices from 'My Devices' tab
* The current device is now named 'This Device' on 'My Devices' tab
* Fixed error message in a case a non-PanBox-vCard should be imported
* Added start-up option to start PanBox minimized (Append "minimized" to the PanBox shortcut to let PanBox start minimized - This won't open the PanBox GUI on every start automatically)
* Fixed splashscreen issue when splashscreen was already closed and a close operation was executed
* Added check for Master device pairings by pairing file that the entered password is PKCS#12 encoded + Don't use identity password as pairing password for Master device pairings by pairing file by default
* Reverted SQLight 3.8.7 to 3.8.6 since 3.8.7 Linux 32bit version is corrupt
* Updated provided Windows JRE to 1.8.0_66
* Support of new Dropbox 3.6.9+ client on a system-wide installation to %PROGRAMFILES% directory on Windows
* Support of new Dropbox 3.9.0+ client on a per-user installation to %LOCALAPPDATA% directory on Windows
* Added implementation of setFileTime methods to support more cloud storage provider and backup tools
* Windows virtual file system now also works with Paint, Wordpad, Windows Reader, etc. due to case sensitivity enforcement
* Master device pairing to file now needs a separated pairing password, since PKCS#12 only supports non-ASCII characters, which could be used in the identity password
* Fixed issue on Windows operation system where the length of the backend path exeeded the max of 255 supported characters (Introduced FilenameExceededRangeException)
* Windows installer adjusts Windows firewall for LAN pairing automatically
* Added PanBox logo to installed programs list entry of PanBox on Windows
* Windows does not provide a service any more:
 ** No restart on installation is needed any more
 ** Windows client will now also access backend files with user privileges instead of using local SYSTEM account
 ** Direct access to CIFS/SMB shares on network drives or NAS devices
 ** Direct access to Google Drive synchronization folder
* Fixed several translation errors

Android:

* Added file upload feature to Dropbox shares
* Updated Dropbox SDK to 1.6.3

Version 1.0
-----------

Released on 10th March 2015 for Windows, Linux and Android

* Initial release of PanBox