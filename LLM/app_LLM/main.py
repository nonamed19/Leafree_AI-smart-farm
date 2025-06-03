# v1.4 : Update image download method
# v1.5 : Update image download directory for EC2
# v1.6 : Update image download method -> direct download
# v1.7 : Update prompts for data and image generation
# v2.0 : Update prompts for data generation, Update image and datasets

from fastapi import FastAPI
from app_llm.endpoints import router

app = FastAPI(title="Plant API", description="API for plant data and images", version="2.0")
app.include_router(router)
