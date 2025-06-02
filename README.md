# HoloFrames

A Minecraft Fabric mod that allows you to display custom 3D hologram models in item frames using OBJ files and textures.

## Features
- Place hologram models in item frames using the Hologram item
- Supports custom OBJ models and PNG textures
- In-game GUI for selecting hologram models
- Server-client synchronization of model data

## Screenshots
![HoloFrames Demo](https://raw.githubusercontent.com/JacksonHoggard/hologrammc/refs/heads/main/demo/demo.png)
![HoloFrames Demo 1](https://raw.githubusercontent.com/JacksonHoggard/hologrammc/refs/heads/main/demo/demo_1.png)

## Getting Started

### Requirements
- Minecraft 1.21.5
- Fabric Loader 0.16.14 or newer
- Fabric API

### Installation
1. Download the latest release of HoloFrames from [GitHub Releases](https://github.com/JacksonHoggard/hologrammc/releases).
2. Place the mod JAR file in your `mods` folder.
3. Start Minecraft with the Fabric profile.

### Adding Custom Hologram Models
1. Place your `.obj` model files and corresponding texture files (PNG) in:
   ```
   config/holoframes/models/
   ```
2. The texture file name must match the reference in the OBJ file (e.g., `mtllib`/`usemtl`).
3. Restart Minecraft or reload the mod to load new models.

## Usage
- Craft the Hologram item using glass and lapis lazuli.
- Right-click with the Hologram item to open the model selection screen.
- Place the Hologram item in an item frame to display the selected hologram.

## Development
### Build
1. Clone the repository:
   ```
   git clone https://github.com/JacksonHoggard/hologrammc.git
   ```
2. Open in your preferred IDE (IntelliJ IDEA recommended).
3. Run:
   ```
   ./gradlew build
   ```
4. The built JAR will be in `build/libs/`.

### Contributing
Pull requests and issues are welcome! See [issues](https://github.com/JacksonHoggard/hologrammc/issues).

## License
All rights reserved. See [LICENSE.txt](LICENSE.txt).

## Credits
- [FabricMC](https://fabricmc.net/)
- [Jackson Hoggard](mailto:jhoggard0129@gmail.com)

