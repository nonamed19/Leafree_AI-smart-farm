import sounddevice as sd
import numpy as np
import librosa
import time
import soundfile as sf
from faster_whisper import WhisperModel
import os
import threading
import io

class STTServer:
    CMD_PIPE = "/tmp/stt_cmd"
    RESP_PIPE = "/tmp/stt_resp"

    def __init__(self, debug=True):
        self.debug = debug
        self.model = WhisperModel("tiny", device="cpu", compute_type="int8")
        self.mic_index = 3
        self.mic_sample_rate = 48000
        self.whisper_sample_rate = 16000
        self.record_duration = 3
        self.play_waiting_sound = False

        if not self.debug:
            self.setup_pipes()

    def setup_pipes(self):
        if not os.path.exists(self.CMD_PIPE):
            os.mkfifo(self.CMD_PIPE)
        if not os.path.exists(self.RESP_PIPE):
            os.mkfifo(self.RESP_PIPE)

    def play_wait_sound(self):
        while self.play_waiting_sound:
            os.system("aplay -D plughw:3,0 wait.wav")

    def play_success_sound(self):
        os.system("aplay -D plughw:3,0 success.wav")

    def play_start_sound(self):
        os.system("aplay -D plughw:3,0 start.wav")
    
    def record_audio(self):
        audio = sd.rec(int(self.record_duration * self.mic_sample_rate),
                       samplerate=self.mic_sample_rate,
                       channels=1,
                       dtype='float32',
                       device=self.mic_index)
        sd.wait()
        return audio.flatten()

    def transcribe_audio(self, audio_data):
        audio_resampled = librosa.resample(audio_data, orig_sr=self.mic_sample_rate, target_sr=self.whisper_sample_rate)
        buffer = io.BytesIO()
        sf.write(buffer, audio_resampled, self.whisper_sample_rate, format='WAV')
        buffer.seek(0)

        print("🚀 [INFO] 추론 시작...")

        self.play_waiting_sound = True
        wait_thread = threading.Thread(target=self.play_wait_sound)
        wait_thread.start()

        start_time = time.time()
        segments, _ = self.model.transcribe(
            buffer,
            language="ko",
            beam_size=1,
            without_timestamps=True
        )
        segments_list = list(segments)
        end_time = time.time()

        self.play_waiting_sound = False
        wait_thread.join()

        print(f"✅ [INFO] 추론 완료! (소요 시간: {end_time - start_time:.2f}초)")

        result_text = ""
        if segments_list:
            self.play_success_sound()
            for segment in segments_list:
                result_text += segment.text + " "
            print(f"📝 Transcription: {result_text.strip()}")
        else:
            result_text = "[인식 실패]"
            print("⚠️ [INFO] 추론은 완료됐지만 인식된 텍스트가 없습니다.")

        return result_text.strip()

    def process_command(self, command):
        if command == "START":
            print("▶️ [INFO] START 명령 수신, STT 실행")
            threading.Thread(target=self.play_start_sound, daemon=True).start()
            audio_data = self.record_audio()
            result = self.transcribe_audio(audio_data)
            return result
        return ""

    def run_pipe_mode(self):
        print("📡 [INFO] Named Pipe 모드 대기 중...")
        while True:
            with open(self.CMD_PIPE, "r") as cmd_pipe:
                command = cmd_pipe.readline().strip()
                result = self.process_command(command)
                
                if result:
                    with open(self.RESP_PIPE, "w") as resp_pipe:
                        resp_pipe.write(result + "\n")

    def run_debug_mode(self):
        print("🛠️ [DEBUG MODE] Enter 키로 STT 실행. Ctrl+C로 종료.")
        while True:
            input("\n▶️ [대기 중] Enter 키를 누르세요...")
            audio_data = self.record_audio()
            self.transcribe_audio(audio_data)

    def start(self):
        try:
            if self.debug:
                self.run_debug_mode()
            else:
                self.run_pipe_mode()
        except KeyboardInterrupt:
            print("\n🛑 [INFO] 프로그램 종료.")

if __name__ == "__main__":
    server = STTServer(debug=False)  # debug=False로 변경 시 Named Pipe 모드
    server.start()
