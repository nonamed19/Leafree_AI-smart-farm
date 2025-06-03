import os
import json
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

class PlantDataCreator:
    def __init__(self):
        load_dotenv()
        OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
        self.llm = ChatOpenAI(
            openai_api_key=OPENAI_API_KEY,
            model_name="gpt-4o",
            temperature=0.1,
            )

        self.parser = JsonOutputParser()

        self.schema = {
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
                    "Quantum_lux": {
                        "type": "string",
                        "description": "Optimal light intensity in lux divided by 12 hours (e.g., '1000')"
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

        # self.prompt_search = ChatPromptTemplate.from_messages([
        #     ("system",
        #     "You are an expert in plant cultivation and environmental conditions.\n"
        #     "You will provide detailed optimal growing conditions for the plant named '{plant_name}'.\n"
        #     "'{plant_name}' may be provided in any language (e.g., Korean, Japanese, German, etc.).\n"
        #     "You must internally translate or interpret it into English and verify if it refers to a real plant.\n"
        #     "You are given a JSON schema below that you must follow strictly when returning data:\n\n{schema}\n\n\n"
        #     "If the plant is valid, generate optimal growing conditions using your web-trained knowledge.\n"
        #     "Do not provide any other explanation, comment, or text outside the JSON object."
        #     ),
        #     ("user", "{raw_data}")
        #     # ("system",
        #     # "You are an expert in botany and plant environmental requirements.\n"
        #     # "Your task is to analyze the following input sentence and identify if it contains the name of any plant.\n"
        #     # "You must extract the plant name(s), translate them into English if necessary, and validate whether they are real plants.\n"
        #     # "If a valid plant name is found, return the optimal environmental growing conditions for it.\n"
        #     # "Your output must follow this JSON schema:\n\n{schema}\n\n\n"
        #     # "Do not provide any explanations, comments, or text outside of the JSON object.\n"
        #     # "Only if no valid plant is found in the sentence, return the following JSON:\n"
        #     # "{{ \"error\": \"식물 이름이 잘못되었습니다.\" }}\n\n"
        #     # ),
        #     # ("user", "{raw_data}")
        # ])

        # self.prompt_parsing = ChatPromptTemplate.from_messages([
        #     ("system",
        #     "You are an expert in botany and plant environmental requirements.\n"
        #     "Your task is to analyze the following input sentence and identify if it contains the name of any plant.\n"
        #     "You must extract the plant name(s), translate them into English if necessary, and validate whether they are real plants.\n"
        #     "If a valid plant name is found, return the optimal environmental growing conditions for it.\n"
        #     "Your output must follow this JSON schema:\n\n{schema}\n\n\n"
        #     "If the data is given in ranges, you must convert them into a single value.\n"
        #     "Do not provide any explanations, comments, or text outside of the JSON object.\n"
        #     "If no valid plant is found in the sentence, return the following JSON:\n"
        #     "{{ \"error\": \"식물 이름이 잘못되었습니다.\" }}\n\n"
        #     "Output format policy:\n"
        #     "1. If valid → output a **pure JSON** object that matches the schema exactly.\n"
        #     "2. If invalid → output the **exact error JSON** shown above.\n"
        #     ),
        #     ("user", "{raw_data}")
        # ])

        # self.chain_search = self.prompt_search | self.llm | self.parser
        # self.chain_parsing = self.prompt_parsing | self.llm | self.parser

        self.prompt = ChatPromptTemplate.from_messages([
            ("system",
            "You are a professional horticulturist and data analyst.\n"
            "Your task is to process the input text to extract a valid **plant name**, "
            "even if it is written in a language other than English (e.g., Korean, Japanese, German).\n"
            "\n"
            "Steps:\n"
            "1. Translate the input to English if necessary.\n"
            "2. Check if the translated name corresponds to a real plant.\n"
            "3. If valid, generate optimal growing conditions for the plant using the schema below.\n"
            "4. If not valid, return the exact error JSON.\n"
            "\n"
            "JSON schema:\n{schema}\n\n"
            "- Convert ranges (e.g., '20~25°C') into a single representative value.\n"
            "- Ensure values are numeric and properly formatted.\n"
            "\n"
            "If the input is not a real plant, return this exactly:\n"
            "{{ \"error\": \"식물 이름이 잘못되었습니다.\" }}\n"
            "\n"
            "Output policy:\n"
            "1. Only return a valid JSON object.\n"
            "2. Do not include any additional text, explanation, or formatting."
            ),
            ("user", "{plant_name}")
        ])

        self.chain = self.prompt | self.llm | self.parser

    # def search_data(self, plant_name):
    #     return self.chain_search.invoke({
    #         "plant_name": plant_name,
    #         "schema": json.dumps(self.schema["parameters"], ensure_ascii=False, indent=2),
    #         "raw_data": ""
    #     })

    # def parse_data(self, plant_name, raw_data):
    #     return self.chain_parsing.invoke({
    #         "plant_name": plant_name,
    #         "schema": json.dumps(self.schema["parameters"], ensure_ascii=False, indent=2),
    #         "raw_data": raw_data
    #     })

    def plant_data(self, plant_name):
        return self.chain.invoke({
            "plant_name": plant_name,
            "schema": json.dumps(self.schema["parameters"], ensure_ascii=False, indent=2),
        })
