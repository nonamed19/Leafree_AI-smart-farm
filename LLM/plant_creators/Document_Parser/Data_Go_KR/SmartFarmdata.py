from dotenv import load_dotenv
import requests
import json
import os

load_dotenv()
DATA_GO_KR_API_KEY = os.getenv('DATA_GO_KR_API_KEY')

url = f'http://apis.data.go.kr/1390000/SmartFarmdata/envdatarqst?serviceKey={DATA_GO_KR_API_KEY}&returnType=json&searchFrmhsCode=25'

response = requests.get(url)
if response.status_code == 200:
    data = response.json()
    with open('smartfarm_data.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)
else:
    print('Error:', response.status_code)
