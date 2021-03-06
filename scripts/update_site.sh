#Run this script from this directory to update the site files
#format is sudo ./update_site /path/to/www/dir

COPYDIR=$1

if [ ! -e $COPYDIR ]; then
	echo "Destination does not exist"
	exit 1
fi

DBFILE="$COPYDIR/app/Config/database.php"
if [ -e $DBFILE ]; then
	echo "Copy Database File"
	mv $DBFILE ./
fi

BOOTSTRAPFILE="$COPYDIR/app/Config/custom.ini"
if [ -e $BOOTSTRAPFILE ]; then
	echo "Copy custom.ini"
	mv $BOOTSTRAPFILE ./
fi

#copy the contents of the www directory
echo "Copy site files"
rm -R $COPYDIR/*
cp -R ../www/. $COPYDIR

#set the permissions
chown -R www-data:www-data $COPYDIR
chmod +x $COPYDIR/lib/Cake/Console
chmod +x $COPYDIR/app/Console/cake
chmod -R 777 $COPYDIR/app/tmp

if [ -e "database.php" ]; then
	echo "Restoring database file"
	mv "./database.php" $DBFILE
fi

if [ -e "custom.ini" ]; then
	echo "Restoring custom.ini file"
	mv "./custom.ini" $BOOTSTRAPFILE
fi

#copy the current updater script
cp ../updater/inventory_updater.ps1 $COPYDIR/app/webroot/files/

echo "Website Updated"
