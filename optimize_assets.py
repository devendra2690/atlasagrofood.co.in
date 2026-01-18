import os
from PIL import Image
from pypdf import PdfReader, PdfWriter

ASSETS_DIR = "assets"
MAX_WIDTH = 1920
QUALITY = 80
PDF_FILE = "brochure.pdf"

def get_size_mb(path):
    if not os.path.exists(path): return 0
    return os.path.getsize(path) / (1024 * 1024)

def optimize_images(directory):
    print(f"Scanning {directory} for images...")
    for root, _, files in os.walk(directory):
        for file in files:
            if file.lower().endswith(('.png', '.jpg', '.jpeg', '.webp')):
                filepath = os.path.join(root, file)
                
                try:
                    with Image.open(filepath) as img:
                        original_size = get_size_mb(filepath)
                        
                        # Resize if too big
                        if img.width > MAX_WIDTH:
                            print(f"[RESIZING] {file} (Width: {img.width} -> {MAX_WIDTH})")
                            ratio = MAX_WIDTH / img.width
                            new_height = int(img.height * ratio)
                            img = img.resize((MAX_WIDTH, new_height), Image.Resampling.LANCZOS)
                        
                        # Convert PNG to WebP or optimize JPG
                        # Strategy: If PNG is photo-like (no transparency), convert to JPG/WebP.
                        # For safety, let's keep format but optimize.
                        # Actually, converting everything to WebP is better but might break hardcoded HTML links.
                        # Plan: Overwrite in place with optimized version of SAME format to avoid breaking links for now.
                        
                        # If it's PNG and super large, it might be a photo saved as PNG.
                        # BUT we can't change extension without checking HTML. 
                        # So just optimize in place.
                        
                        if file.lower().endswith('.png'):
                            # Optimize PNG
                            # Check if we can save significant space
                             img.save(filepath, optimize=True, quality=QUALITY)
                        elif file.lower().endswith(('.jpg', '.jpeg')):
                             img.save(filepath, optimize=True, quality=QUALITY)

                        new_size = get_size_mb(filepath)
                        if original_size > 0.1: # Only log significant files
                             print(f"Optimized {file}: {original_size:.2f}MB -> {new_size:.2f}MB")

                except Exception as e:
                    print(f"Error optimizing {file}: {e}")

def compress_pdf(pdf_path):
    if not os.path.exists(pdf_path):
        print(f"PDF not found: {pdf_path}")
        return

    original_size = get_size_mb(pdf_path)
    print(f"Compressing PDF: {pdf_path} ({original_size:.2f}MB)")
    
    try:
        reader = PdfReader(pdf_path)
        writer = PdfWriter()

        for page in reader.pages:
            writer.add_page(page)

        # pypdf compression is limited, but let's try basic stream compression
        for page in writer.pages:
            page.compress_content_streams()  # This provides lossless compression

        output_path = "brochure_optimized.pdf"
        with open(output_path, "wb") as f:
            writer.write(f)
            
        new_size = get_size_mb(output_path)
        print(f"PDF Compression Result: {original_size:.2f}MB -> {new_size:.2f}MB")
        
        # Verify if it actually helped
        if new_size < original_size:
            print("Replacing original PDF with optimized version...")
            os.replace(output_path, pdf_path)
        else:
            print("Compression didn't save space. Keeping original.")
            os.remove(output_path)

    except Exception as e:
        print(f"Error compressing PDF: {e}")

if __name__ == "__main__":
    optimize_images(ASSETS_DIR)
    compress_pdf(PDF_FILE)
