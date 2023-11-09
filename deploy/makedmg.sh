if [ ! -f ../target/scala-*/Aliaser.jar ]; then
  cd ../
  sbt assembly
  cd deploy/
fi

jpackage \
  --name Aliaser \
  --input ../target/scala-*/ \
  --main-jar Aliaser.jar \
  --resource-dir . \
  --type dmg \
  --icon Aliaser.icns
mv Aliaser-*.dmg Aliaser.dmg
