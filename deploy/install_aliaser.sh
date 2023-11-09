rm -f aliaserpkg
mkdir aliaserpkg
cd aliaserpkg
wget https://zerdicorp.ru/Aliaser.zip -O Aliaser.zip
unzip Aliaser.zip
jpackage \
  --name Aliaser \
  --input target/scala-*/ \
  --main-jar Aliaser.jar \
  --resource-dir . \
  --type dmg \
  --icon deploy/Aliaser.icns
mv Aliaser-*.dmg Aliaser.dmg
cd ..
mv aliaserpkg/Aliaser.dmg .
rm -rf aliaserpkg
open Aliaser.dmg
rm -- "$0"