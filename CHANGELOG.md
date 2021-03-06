# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

## 1.6

### Added

- added ```Settings.encrypt``` value to the ```custom.ini``` file layout. This controls if settings encryption is used or not
- added an error message when incorrect authentication type is set at login
- added [AuthenticationReset](https://github.com/eau-claire-energy-cooperative/simple-inventory/wiki/Recovering-Login) command line option to regain access on lost logins
- added module and log level arguments to AppShell->dblog() function
- added documentation and code links to the login page

### Fixed

- fixed issue with settings encryption. this can now be toggled via a true/false value. Also included better instructions on this
- fixed warning where the AppShell->log() function conflicted with the CakePHP default. Renamed to dblog()
- fixed issue with deleting a computer, method was tied to a POST request even though button was issuing a GET request. 

## 1.5

### Added

- added the ability to set a license as "unassigned" to a specific computer. This way the key can be saved if deploying later. 
- added ```custom.ini.default``` file and moved the encryption_key setting to this file, loaded at runtime. Allows for custom setting of encryption key
- added put current version number in footer

### Changed

- made the decommission date of a computer more prominent on the listing screen and the detail screen
- updated the look of the decommission process screen, including the error message listing

## 1.4

### Fixed

- fixed error with /api/add_log function where the Security class wasn't found. Added import to main AppController class

## 1.3

### Added

- encrypt all settings at rest in the database. Use the ```app/Config/bootstrap.php``` file to set the Settings.encryption_key value 
- added link to documentation GitHub in the footer

### Changed

- refer to Computer Detail pages consistently, some places called it Computer Info
- minor costmetic changes 

## 1.2

### Added

- added support for the update script sending the ipv6 address found on the main connected interface. Can be togged on/off in the settings as with the other fields. 
- added an API authentication key setting and support for it in the updater script and API controller flows. This stops random operations from being done on the API without some kind of authentication first. 

### Changed

- updated the UI to use a new theme based on Bootstrap and JQuery. The menu system and overall look have been cleaned, updated, and made consistant overall.

## 1.1

### Added

- added a "DNS Search Domain" field to the settings. Computer names will have this added to create a fully qualified domain name for "is alive" ping requests

## 1.0

### Added

- started Changelog for this project. For historical changes see the [commit history](https://github.com/eau-claire-energy-cooperative/simple-inventory/commits/master). Using 1.0 as version since this is a mature project with no tagged versions prior. 
- added auth check to AjaxController, these should check authentication the same as any other page

### Changed

- moved Licenses, Programs, and Scheduled Tasks into their own controller, away from the Admin controller

### Removed

- removed now-defunct Dashboard and all files needed with it