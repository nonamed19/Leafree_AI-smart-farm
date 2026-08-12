from selenium import webdriver
from bs4 import BeautifulSoup
import requests
import json
import time
import os

# website URL
url = 'https://www.thespruce.com/plants-a-to-z-5116344'

driver = webdriver.Chrome()
driver.get(url)

# wait for the page to load
time.sleep(3)

html = driver.page_source
soup = BeautifulSoup(html, 'html.parser')

# search for <alphabetical-card__title> tags
card_titles = soup.find_all('div', class_='alphabetical-card__title')
plant_names = [title.get_text(strip=True) for title in card_titles]

# 1. data셋 생성
plant_dataset = plant_names

current_dir = os.path.dirname(os.path.abspath(__file__))
save_path = os.path.join(current_dir, 'theSpurce_dataset.json')

# 2. crawling이 끝나면 파일을 json 형태로 변환 후 파일로 저장하기
with open(save_path, 'w', encoding='utf-8') as f:
    json.dump(plant_dataset, f, ensure_ascii=False, indent=4)

print("Crawling completed and data saved to theSpurce_dataset.json")

# quit the driver after scraping
driver.quit()
