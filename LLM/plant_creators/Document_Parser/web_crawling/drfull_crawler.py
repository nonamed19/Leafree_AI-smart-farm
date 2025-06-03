from bs4 import BeautifulSoup
import requests
import json
import time

# website URL
url = 'https://www.fuleaf.com/plants'
response = requests.get(url)
soup = BeautifulSoup(response.text, 'html.parser')

# search for <h3> tags
h3_tags = soup.find_all('h3')
plant_names = [tag.get_text(strip=True) for tag in h3_tags]
# 0. 첫 2개는 임의로 제거(전처리리)
plant_names = plant_names[2:] if len(plant_names) > 2 else plant_names

# 1. data셋 생성
plant_dataset = {}

# 2. 웹사이트 crawling을 통해 json data 형식으로 자료 저장
for idx, plant_name in enumerate(plant_names, 1):
    print(f"<{plant_name}> crawling ...")
    detail_url = f"https://drfull.im/plants/detail/{plant_name}"
    response = requests.get(detail_url)

    if response.status_code != 200:
        print(f"Status Code: {response.status_code})")
        continue

    plant_soup = BeautifulSoup(response.text, 'html.parser')

    plant_info = {
        'plant_english': None,
        'plant_intro': None,
        'water_period': None,
        'sunlight_period': None,
        'humidity': None,
        'temperature': None,
    }

    # search for simpleinfo-plantname_english mainColorTitle
    english_tag = plant_soup.find('p', class_='simpleinfo-plantname_english mainColorTitle')
    if english_tag:
        plant_info['plant_english'] = english_tag.get_text(strip=True)

    # search for simpleinfo-plantname_intro
    intro_tag = plant_soup.find('p', class_='simpleinfo-plantname_intro')
    if intro_tag:
        plant_info['plant_intro'] = intro_tag.get_text(strip=True)

    # search for details__tab-title
    tab_titles = plant_soup.find_all('p', class_='details__tab-title')
    if tab_titles:
        tab_title_temp = ['water_period', 'sunlight_period', 'humidity', 'temperature']
        for tab_title, key in zip(tab_titles, tab_title_temp):
            plant_info[key] = tab_title.get_text(strip=True)
    
    plant_dataset[plant_name] = plant_info
    time.sleep(0.5)

# 3. crawling이 끝나면 파일을 json 형태로 변환 후 파일로 저장하기
with open('drfull_dataset.json', 'w', encoding='utf-8') as f:
    json.dump(plant_dataset, f, ensure_ascii=False, indent=4)

print("Crawling completed and data saved to plant_dataset.json")
