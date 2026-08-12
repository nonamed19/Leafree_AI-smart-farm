#!/usr/bin/env bash
# ────────────────────────────────────────────
# stt_start.sh  –  Leafree STTServer 런처
# 1) /tmp 파이프 정리  2) venv 활성화
# 3) 새 파이프 생성   4) STTServer.py 실행
# ────────────────────────────────────────────
set -e          # 오류 발생 시 즉시 종료
set -u          # 정의되지 않은 변수 사용 시 오류

# -----  경로 설정  ------------------------------------------------------------
BASE_DIR="$HOME/Desktop/Leafree/STT"          # STT 프로젝트 폴더
VENV_DIR="$BASE_DIR/whisper-venv"             # 가상환경 경로
PY="$VENV_DIR/bin/python3"                    # 가상환경 파이썬
APP="STTServer.py"                            # 실행할 파이썬 파일명

# -----  1. 기존 파이프 삭제  --------------------------------------------------
CMD_PIPE="/tmp/stt_cmd"
RESP_PIPE="/tmp/stt_resp"

rm -f "$CMD_PIPE" "$RESP_PIPE"

# -----  2. 가상환경 활성화  ---------------------------------------------------
if [ ! -x "$PY" ]; then
  echo "❌  가상환경을 찾을 수 없습니다: $PY"
  exit 1
fi
source "$VENV_DIR/bin/activate"

# -----  3. 새 파이프 생성  ----------------------------------------------------
mkfifo "$CMD_PIPE"
mkfifo "$RESP_PIPE"

# -----  4. STTServer 실행  ----------------------------------------------------
exec "$PY" "$BASE_DIR/$APP" "$@"
