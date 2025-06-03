import os
import requests
from dotenv import load_dotenv
import openai

class PlantImageCreator:
    def __init__(self):
        load_dotenv()
        self.api_key = os.getenv("OPENAI_API_KEY")
        openai.api_key = self.api_key

    def create_image(self, plant_name, save_path):
        prompt = (
            "You are an expert caricature artist.\n"
            f"You will guess the plant name from the given sentence: {plant_name}.\n"
            "Create a cute, cartoon-style drawing of the plant.\n"
            "The plant should appear exactly once, positioned precisely at the center of the image.\n"
            "It should be designed as a simple, friendly character suitable with small facial features like eyes and a smile.\n"
            "The background must be a solid, light color with no patterns, textures, decorations, or additional elements—completely plain."
        )
        response = openai.images.generate(
            prompt = prompt,
            model = "dall-e-3",
            n = 1,
            size = "1024x1024",
            # quality = "hd",
        )

        image_url = response.data[0].url
        img_data = requests.get(image_url).content

        with open(save_path, 'wb') as handler:
            handler.write(img_data)
