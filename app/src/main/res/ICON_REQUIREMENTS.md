# Android App Icon Requirements

## Required PNG Files

You need to create PNG images with your icon design (green circle, white house, yellow calculator) in the following sizes:

### Standard Launcher Icons (ic_launcher.png)
- **mipmap-mdpi**: 48x48 px
- **mipmap-hdpi**: 72x72 px  
- **mipmap-xhdpi**: 96x96 px
- **mipmap-xxhdpi**: 144x144 px
- **mipmap-xxxhdpi**: 192x192 px

### Round Launcher Icons (ic_launcher_round.png)
Same sizes as above, but with circular cropping in mind.

### Adaptive Icon Components (API 26+)
For devices running Android 8.0+, you can use:
- **ic_launcher_foreground.png**: 432x432 px (108dp at xxxhdpi)
- **ic_launcher_background.png**: 432x432 px (or use solid color)

## How to Generate Icons

### Option 1: Online Tools
1. **Android Asset Studio**: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
   - Upload your design
   - It generates all required sizes
   - Download and extract to res folder

2. **Apetools Image Resizer**: https://apetools.webprofusion.com/app/#/tools/imageresizer
   - Upload a high-res version (512x512 or 1024x1024)
   - Select Android icon preset

### Option 2: Design Tools
1. **Figma/Sketch**: Export at different sizes
2. **Photoshop**: Use Image > Image Size
3. **GIMP** (free): Scale and export

### Option 3: Using Your Current Design
Convert your vector to PNG:
1. Open the vector in Android Studio's preview
2. Take a screenshot or export
3. Resize for each density

## File Placement
Place the generated PNG files in:
```
app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-hdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xxhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
└── mipmap-xxxhdpi/
    ├── ic_launcher.png
    └── ic_launcher_round.png
```

## Design Tips
- Keep 10% padding around the icon
- Ensure the design is visible at small sizes
- Test on different backgrounds
- Use PNG-24 with transparency for best quality
