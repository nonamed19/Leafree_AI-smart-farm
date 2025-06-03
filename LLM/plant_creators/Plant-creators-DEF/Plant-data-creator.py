from dotenv import load_dotenv
import os
import json
from pathlib import Path
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

# 스키마 정의
plant_data_schema = {
    "name": "generate_plant_environment",
    "description": "Provide detailed optimal environmental conditions for growing the specified plant.",
    "parameters": {
        "type": "object",
        "properties": {
            "AirTemperature": {
                "type": "string",
                "description": "Optimal air temperature in °C (e.g., '20')"
            },
            "AirHumidity": {
                "type": "string",
                "description": "Optimal air humidity in percentage (e.g., '60')"
            },
            "Quantum": {
                "type": "string",
                "description": "Optimal photosynthetically active radiation (PAR) in μmol/m²/s (e.g., '200')"
            },
            "HighSoilHumi": {
                "type": "string",
                "description": "Top soil optimal moisture percentage (e.g., '40')"
            },
            "HighEC": {
                "type": "string",
                "description": "Top soil electrical conductivity in dS/m (e.g., '1.5')"
            },
            "HighPH": {
                "type": "string",
                "description": "Top soil pH value (e.g., '6.0')"
            },
            "LowSoilHumi": {
                "type": "string",
                "description": "Bottom soil optimal moisture percentage (e.g., '30')"
            },
            "LowSoilEC": {
                "type": "string",
                "description": "Bottom soil electrical conductivity in dS/m (e.g., '1.5')"
            },
            "LowSoilPH": {
                "type": "string",
                "description": "Bottom soil pH value (e.g., '6.0')"
            },
            "AmtIrrigation": {
                "type": "string",
                "description": "Recommended daily irrigation amount in ml (e.g., '400')"
            },
            "IrrigationInterval": {
                "type": "string",
                "description": "Recommended irrigation interval in min (e.g., '720')"
            },
            "Photoperiod": {
                "type": "string",
                "description": "Recommended daily light time in min (e.g., '720')"
            },
            "CO2Level": {
                "type": "string",
                "description": "Optimal CO₂ concentration in ppm (e.g., '400')"
            },
            "HarvestTime": {
                "type": "string",
                "description": "Estimated time from seeding to harvest in days (e.g., '70')"
            }
        },
        "required": [
            "AirTemperature", "AirHumidity", "Quantum", "HighSoilHumi", "HighEC",
            "HighPH", "LowSoilHumi", "LowSoilEC", "LowSoilPH", "AmtIrrigation", 
            "IrrigationInterval", "Photoperiod", "CO2Level", "HarvestTime"
        ]
    }
}

# 프롬프트 정의
prompt_template_search = ChatPromptTemplate.from_messages([
    ("system",
     "You are an expert in plant cultivation and environmental conditions.\n"
     "You will provide detailed optimal growing conditions for the plant named '{PlantName}'.\n"
     "'PlantName' may be provided in any language (e.g., Korean, Japanese, German, etc.).\n"
     "You must internally translate or interpret it into English and verify if it refers to a real plant.\n"
     "You are given a JSON schema below that you must follow strictly when returning data:\n\n{schema}\n\n\n"
     "If the plant is valid, generate optimal growing conditions using your web-trained knowledge.\n"
     "Do not provide any other explanation, comment, or text outside the JSON object."
    ),
    ("user", "{raw_data}")
])

prompt_template_parsing = ChatPromptTemplate.from_messages([
    ("system",
     "You are an expert in plant cultivation and environmental conditions.\n"
     "You will provide detailed optimal growing conditions for the plant named '{PlantName}'.\n"
     "'PlantName' may be provided in any language (e.g., Korean, Japanese, German, etc.).\n"
     "You must internally translate or interpret it into English and verify if it refers to a real plant.\n\n"
     "You are given a JSON schema below that you must follow strictly when returning data:\n\n{schema}\n\n"
     "If the data is given in ranges, you must convert them into a single value.\n"
     "If '{PlantName}' is not a valid plant (e.g., fictional, technology, object, nonsense), respond **exactly** with this JSON object:\n"
     "{{ \"error\": \"식물 이름이 잘못되었습니다.\" }}\n\n"
     "Output format policy:\n"
     "1. If valid → output a **pure JSON** object that matches the schema exactly.\n"
     "2. If invalid → output the **exact error JSON** shown above.\n"
     "Do not provide any other explanation, comment, or text outside the JSON object."
    ),
    ("user", "{raw_data}")
])

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# LLM 객체 생성
llm = ChatOpenAI(
    openai_api_key=OPENAI_API_KEY,
    model_name="gpt-4o",
    temperature=0.2,
    )

# JSON 파서 생성
parser = JsonOutputParser()

# Langchain(LLM Workflow Orchestration Framework) 객체 생성
chain_search = prompt_template_search | llm | parser
chain_parsing = prompt_template_parsing | llm | parser

# LLM을 통한 data searching 실행
def search_plant_data(plant_name_input):
    search_data = chain_search.invoke({
        "PlantName": plant_name_input,
        "schema": json.dumps(plant_data_schema["parameters"], ensure_ascii=False, indent=2),
        "raw_data": "",
    })
    return search_data

# LLM을 통한 data parsing 실행
def parsing_plant_data(plant_name_input, raw_data):
    search_data = chain_parsing.invoke({
        "PlantName": plant_name_input,
        "schema": json.dumps(plant_data_schema["parameters"], ensure_ascii=False, indent=2),
        "raw_data": raw_data,
    })
    return search_data

## ---------------------------------------------------------- ##

# 키워드 입력
plant_name_input = input("확인할 식물의 이름을 입력하세요: ").strip()

# Database Load
current_dir = Path(os.path.dirname(os.path.abspath(__file__)))
database_dir = os.path.join(current_dir, "plant_datasets.json")
with open(database_dir, "r", encoding='utf-8') as f:
    plant_datasets = json.load(f)

matched_idx = next((i for i, plant in enumerate(plant_datasets)
                    if plant.get("PlantName", "").lower() == plant_name_input.lower()), None)

if matched_idx is not None:
    raw_data = json.dumps(plant_datasets[matched_idx], ensure_ascii=False, indent=2)
else:  # if matched_idx is None
    raw_data = search_plant_data(plant_name_input)

# LLM을 통한 data parsing 실행
response = parsing_plant_data(plant_name_input, raw_data)
print(json.dumps(response, ensure_ascii=False, indent=2))

# Database Update
updated_plant = {"PlantName": plant_name_input}
updated_plant.update(response)

if matched_idx is not None:
    plant_datasets[matched_idx] = updated_plant
else:
    plant_datasets.append(updated_plant)

with open(database_dir, "w", encoding="utf-8") as f:
    json.dump(plant_datasets, f, ensure_ascii=False, indent=2)
    print(f"Database updated with new data for '{plant_name_input}'.")
