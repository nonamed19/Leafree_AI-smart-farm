## Upstage Document Parser using Upstage API requires credits.

from pathlib import Path
from dotenv import load_dotenv
import requests
import os
import json

load_dotenv()
UPSTAGE_API_KEY = os.getenv("UPSTAGE_API_KEY")

current_dir = Path(os.path.dirname(os.path.abspath(__file__)))

save_fp = current_dir / 'parsed_data/upstage_dp'
# pdf_path = current_dir
os.makedirs(save_fp, exist_ok=True)  # 폴더가 존재하지 않을 경우 폴더 생성

data = {
    "output_formats": "['html', 'text', 'markdown']",  # 원하는 출력 형식 설정 (HTML, Text, Markdown)
    "base64_encoding": "['table']",                   # 테이블 데이터를 Base64로 인코딩
    "ocr": "force",                                   # OCR 강제 사용 설정
    "coordinates": "true",                            # 결과에 좌표 정보 포함
}  # REST API 요청에 포함할 데이터

count = 0
url = "https://api.upstage.ai/v1/document-ai/document-parse"  # REST API의 엔드포인트 URL
headers = {"Authorization": f"Bearer {UPSTAGE_API_KEY}"}  # 인증을 위한 API 키 포함

for manual_pdf in Path(current_dir).glob('*.pdf'):
    filename = manual_pdf.name
    files = {"document": open(manual_pdf, "rb")}

    response = requests.post(url, headers=headers, files=files, data=data)  # POST 요청 전송
    if response.status_code == 200:
        response_data = response.json()
        temp_fp = Path(save_fp) / Path(f'{manual_pdf.stem}.json')  # 결과를 저장할 JSON 파일 경로 설정
        with open(temp_fp, "w", encoding="utf-8") as json_file:
            print(f'Writing parsed {manual_pdf} file to JSON.')
            json.dump(response_data, json_file, ensure_ascii=False, indent=4)
            print(f'{manual_pdf} file saved as {temp_fp}')
    else:
        print(f"{response.status_code}, {response.text}")
