import os
from PIL import Image

ASSETS_DIR = "assets"

def get_size_mb(path):
    if not os.path.exists(path): return 0
    return os.path.getsize(path) / (1024 * 1024)

def fix_images(directory):
    print(f"Scanning {directory} for images to fix...")
    for root, _, files in os.walk(directory):
        for file in files:
            filepath = os.path.join(root, file)
            
            # Focus on fixing PNGs that might have bloated
            if file.lower().endswith('.png'):
                try:
                    with Image.open(filepath) as img:
                        original_size = get_size_mb(filepath)
                        
                        # Attempt 1: Quantize (256 colors) - Great for logos/icons
                        if img.mode != 'P':
                            img_quantized = img.quantize(colors=256, method=2)
                        else:
                            img_quantized = img

                        # Save to temp to check size
                        temp_path = filepath + ".temp.png"
                        img_quantized.save(temp_path, optimize=True, quality=80)
                        
                        new_size = get_size_mb(temp_path)
                        
                        if new_size < original_size:
                            print(f"[FIXED] {file}: {original_size:.2f}MB -> {new_size:.2f}MB")
                            os.replace(temp_path, filepath)
                        else:
                            # If quantizing didn't help (maybe it's a photo), try just optimizing the original mode
                            os.remove(temp_path)
                            # print(f"[SKIPPED] {file}: Quantization increased size or no change.")

                except Exception as e:
                    print(f"Error fixing {file}: {e}")

if __name__ == "__main__":
    fix_images(ASSETS_DIR)
