from typing import List, Dict
from dotenv import load_dotenv
import openai
import json
import time
import os

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

client = openai.OpenAI(api_key=OPENAI_API_KEY)

# plants 리스트를 JSON 파일에서 읽어오기
current_dir = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(current_dir, 'theSpurce_dataset.json')

with open(json_path, 'r', encoding='utf-8') as f:
    plants = json.load(f)
print(plants)

result_data: List[Dict] = []

# %중요% function schema for the plant data
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

def fetch_plant_data(plant: str) -> Dict:
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": "You are a professional plant biologist."},
            {"role": "user", "content": f"Provide the optimal environmental data for '{plant}'."}
        ],
        functions=[plant_data_schema],
        function_call={"name": "generate_plant_environment"},
        temperature=0.2,
    )

    arguments = response.choices[0].message.function_call.arguments
    data = json.loads(arguments)
    data['PlantName'] = plant
    return data

def main():
    for idx, plant in enumerate(plants, start=1):
        try:
            print(f"Processing ({idx}/{len(plants)}): {plant}")
            data = fetch_plant_data(plant)
            result_data.append(data)

            time.sleep(0.5)

        except Exception as e:
            print(f"Error with {plant}: {e}")
            continue

    current_dir = os.path.dirname(os.path.abspath(__file__))
    save_path = os.path.join(current_dir, 'plant_environment_data.json')

    with open(save_path, 'w', encoding='utf-8') as f:
        json.dump(result_data, f, ensure_ascii=False, indent=4)

    print("All plant data collection complete")

if __name__ == "__main__":
    main()
