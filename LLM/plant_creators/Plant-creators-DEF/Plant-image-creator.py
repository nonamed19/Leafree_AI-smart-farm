from dotenv import load_dotenv
import os
import requests
from pathlib import Path
import openai

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

def create_plant_image(plant_name, save_path):
    # 프롬프트 정의
    prompt_template = (
        f"You are an expert in drawing cartoons in children's book.\n"
        "Create a cute and cartoon-style image of the following plant: \"{plant_name}\".\n"
        "The image should be colorful, child-friendly, and charming—similar to a character from a children's book.\n"
        "Include small facial features like eyes and a smile to make it look adorable.\n"
        "Do not include text or background."
    )

    # 이미지 객체 생성
    response = openai.images.generate(
        prompt = prompt_template,
        model = "dall-e-3",
        n = 1,
        size = "1024x1024",
    )

    # 이미지 저장
    image_url = response.data[0].url
    img_data = requests.get(image_url).content
    with open(save_path, 'wb') as handler:
        handler.write(img_data)

    print(f"Image created and saved as '{plant_name_input}'.png")

## ---------------------------------------------------------- ##

# 키워드 입력
plant_name_input = input("확인할 식물의 이름을 입력하세요: ").strip()

# image create and save
current_dir = Path(os.path.dirname(os.path.abspath(__file__)))
create_plant_image(plant_name_input, current_dir / f"{plant_name_input}.png")
