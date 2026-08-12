# 250520 updated to recent version

import os
import json
from pathlib import Path
from plant_data_creator import PlantDataCreator
from plant_image_creator import PlantImageCreator

def main():
    # KEYWORD INPUT(NAME OF A PLANT)
    plant_name = input("식물 이름을 입력하세요: ").strip()
    print("")

    # DATABASE LOAD
    init_plants = PlantDataCreator()
    current_dir = Path(os.path.dirname(os.path.abspath(__file__)))
    db_path = current_dir / "datasets.json"

    if db_path.exists():
        with open(db_path, "r", encoding="utf-8") as f:
            plant_datasets = json.load(f)
    else:
        plant_datasets = []

    # DATA SEARCH
    print(f"(1/4) Searching environmental condition for {plant_name}...")
    matched = next((i for i, p in enumerate(plant_datasets) if p["plant_name"].lower() == plant_name.lower()), None)

    if matched is not None:
        raw_data = plant_datasets[matched]
    else:  # if matched is None
        raw_data = init_plants.plant_data(plant_name)

    print(raw_data)
    print("")

    # DATA PARSING
    print(f"(2/4) Parsing environmental condition for {plant_name}...")
    # response = init_plants.parse_data(plant_name, raw_data)
    updated_plant = {"plant_name": plant_name}
    updated_plant.update(raw_data)
    updated_plant["plant_name"] = plant_name

    print("")

    if matched is not None:
        plant_datasets[matched] = updated_plant
    else:
        plant_datasets.append(updated_plant)

    with open(db_path, "w", encoding="utf-8") as f:
        json.dump(plant_datasets, f, ensure_ascii=False, indent=2)

    # DATA VALIDATION CHECK
    if "error" in raw_data:
        print(f"(ERR) Sorry, we found no environmental condition for {plant_name}.")
        exit(1)
    else:
        image_path = current_dir / f"{plant_name}.png"
        if image_path.exists():
            print(f"(3/4) Image for '{plant_name}' already exists.")
        else:
            print(f"(3/4) Generating image for {plant_name}...")
            img_creator = PlantImageCreator()
            img_creator.create_image(plant_name, image_path)

        # FINAL OUTPUT
        print("(4/4) All tasks completed successfully.")

if __name__ == "__main__":
    main()
