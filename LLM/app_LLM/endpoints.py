from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse
from app_llm.plant_data_creator import PlantDataCreator
from app_llm.plant_image_creator import PlantImageCreator
from pathlib import Path
import json

router = APIRouter()
data_creator = PlantDataCreator()
img_creator = PlantImageCreator()

DATA_PATH = Path(__file__).parent / "datasets.json"
IMG_DIR = Path(__file__).parent / "images"
IMG_DIR.mkdir(exist_ok=True)

# GET: environmental condition data
@router.get("/data/{plant_name}")
async def get_plant_data(plant_name: str):
    plant_name = plant_name.strip()
    
    # Load dataset
    if DATA_PATH.exists():
        with open(DATA_PATH, "r", encoding="utf-8") as f:
            plant_datasets = json.load(f)
    else:
        plant_datasets = []

    # DATA SEARCH
    matched = next((i for i, p in enumerate(plant_datasets) if p["plant_name"].lower() == plant_name.lower()), None)
    
    if matched is not None:
        raw_data = plant_datasets[matched]
    else:
        raw_data = data_creator.plant_data(plant_name)

    # DATA PARSING
    updated_plant = {"plant_name": plant_name}
    updated_plant.update(raw_data)
    updated_plant["plant_name"] = plant_name

    if matched is not None:
        plant_datasets[matched] = updated_plant
    else:
        plant_datasets.append(updated_plant)

    # DATA VALIDATION CHECK
    if "error" in raw_data:
        raise HTTPException(status_code=404, detail="식물 이름이 잘못되었습니다.")

    with open(DATA_PATH, "w", encoding="utf-8") as f:
        json.dump(plant_datasets, f, ensure_ascii=False, indent=2)
    return {
        "plant_name": plant_name,
        "environment": raw_data,
    }

# POST: plant image
@router.post("/image/{plant_name}")
async def post_plant_image(plant_name: str):
    # # directory for images in docker
    DOCKER_DIR = IMG_DIR / f"{plant_name}.png"
    
    # # directory for images in EC2
    # EC2_DIR = Path("/app/images") / f"{plant_name}.png"

    if not DOCKER_DIR.exists():
        img_creator.create_image(plant_name, DOCKER_DIR)

    return FileResponse(
        path=DOCKER_DIR,
        media_type="image/png",
        filename=f"{plant_name}.png"
    )
