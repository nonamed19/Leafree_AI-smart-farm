import fitz  # PyMuPDF의 모듈 이름: PDF와 같은 문서 파일을 읽고 처리할 수 있게 해줌
import json  # JSON 데이터를 파싱하거나 생성하는데 사용
from markdownify import markdownify as md
from pathlib import Path
import os


def pdf_to_html(pdf_path, output_html_path):
    doc = fitz.open(pdf_path)

    with open(output_html_path, 'w', encoding='utf-8') as html_file:
        for page_num in range(len(doc)):
            page = doc[page_num]
            html_content = page.get_text("html")  # PDF 페이지를 HTML 형식으로 변환
            html_file.write(html_content)

    print("PDF가 HTML로 저장되었습니다")


current_dir = Path(os.path.dirname(os.path.abspath(__file__)))

save_path = current_dir / 'parsed_data_html'  # HTML 파일을 저장할 경로
os.makedirs(save_path, exist_ok=True)  # 폴더가 존재하지 않을 경우 폴더 생성

for pdf_file in Path(current_dir).glob('*.pdf'):
    output_html_path = save_path / Path(f'{pdf_file.stem}.html')  # Path 객체 사용
    pdf_to_html(pdf_file, output_html_path)
