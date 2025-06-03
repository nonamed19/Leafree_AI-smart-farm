#!/bin/bash

LOG=/home/pi/autostart.log

echo "AUTOSTART: $(date)" >> /home/pi/autostart.log

export LD_LIBRARY_PATH=/usr/local/qt6/lib/:$LD_LIBRARY_PATH
export DISPLAY=:0
export XAUTHORITY=/home/pi/.Xauthority

amixer set Master 100%

# 1) STT 프로그램 실행
/home/pi/Desktop/Leafree/STT/start_stt.sh &    \
  echo "Python program run: $(date)"        >> $LOG

# 2) 충분히 기다리기 (GUI 완전 준비 대기)
sleep 3
echo "Sleep Done: $(date)"                   >> $LOG

# 3) leafree 실행 (백그라운드, 로그 남기기)
/home/pi/leafree >> $LOG 2>&1 &              
echo "QT program launched: $(date)"           >> $LOG

exit 0