if [ ! -f Aliaser.dmg ]; then
  bash makedmg.sh
fi

shasum -a 256 Aliaser.dmg | cut -d " " -f 1 >.checksum
sshpass -p "$ALIASER_SRV_PWD" scp Aliaser.dmg "$ALIASER_SRV_USER"@45.8.248.195:/storage/
