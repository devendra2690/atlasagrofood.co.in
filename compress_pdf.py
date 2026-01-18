import os
from pypdf import PdfReader, PdfWriter

PDF_PATH = "assets/brochure.pdf"

def get_size_mb(path):
    if not os.path.exists(path): return 0
    return os.path.getsize(path) / (1024 * 1024)

def compress_pdf():
    if not os.path.exists(PDF_PATH):
        print(f"Error: {PDF_PATH} not found.")
        return

    original_size = get_size_mb(PDF_PATH)
    print(f"Original PDF size: {original_size:.2f}MB")
    
    try:
        reader = PdfReader(PDF_PATH)
        writer = PdfWriter()

        for page in reader.pages:
            writer.add_page(page)

        for page in writer.pages:
            page.compress_content_streams()

        output_path = "assets/brochure_optimized.pdf"
        with open(output_path, "wb") as f:
            writer.write(f)
            
        new_size = get_size_mb(output_path)
        print(f"Optimized PDF size: {new_size:.2f}MB")
        
        if new_size < original_size:
            print(f"Success! Reduced by {original_size - new_size:.2f}MB")
            os.replace(output_path, PDF_PATH)
        else:
            print("No reduction achieved. Keeping original.")
            os.remove(output_path)

    except Exception as e:
        print(f"Error compressing PDF: {e}")

if __name__ == "__main__":
    compress_pdf()
