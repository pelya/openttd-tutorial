#!/bin/sh

adb install -r talker.apk
adb shell rm -rf /sdcard/Talk
adb shell mkdir -p /sdcard/Talk
COUNTER=0

cat subtitles.txt | \
while read LINE; do
echo "$LINE"
[ -z "$LINE" ] && continue
COUNTER=`expr $COUNTER '+' 1`
adb shell 'rm -f /sdcard/Talk/*'
adb shell "am start -n com.nolanlawson.android.simpletalker/.MainActivity -e text '$LINE'"
while adb shell ps | grep com.nolanlawson.android.simpletalker > /dev/null; do sleep 1; done
rm -rf xxx
mkdir -p xxx
adb pull /sdcard/Talk xxx
mv xxx/* "talk/`printf '%02d' $COUNTER`-`ls xxx`.wav"
rm -rf xxx
done
