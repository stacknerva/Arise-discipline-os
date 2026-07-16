import math
from PIL import Image, ImageDraw

def draw_pixel_icon(size=512):
    # 32x32 grid
    grid_size = 32
    pixel_size = size // grid_size
    
    img = Image.new('RGBA', (size, size), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    
    def draw_pixel(gx, gy, color=(255, 255, 255, 255)):
        x0 = gx * pixel_size
        y0 = gy * pixel_size
        x1 = x0 + pixel_size - 1
        y1 = y0 + pixel_size - 1
        draw.rectangle([x0, y0, x1, y1], fill=color)

    # Draw vertical dashed line
    for y in range(2, 30):
        if y % 4 != 0: # gap every 4th pixel
            draw_pixel(15, y)
            draw_pixel(16, y) # 2 pixels wide for symmetry? Let's use 1 pixel wide, at x=15.5? 
            # If grid is 32, center is between 15 and 16. Let's make it 2 pixels wide
            
    # Circle
    center_x, center_y = 15.5, 15.5
    radius = 11
    for angle in range(0, 360):
        rad = math.radians(angle)
        x = int(center_x + radius * math.cos(rad))
        y = int(center_y + radius * math.sin(rad))
        draw_pixel(x, y)
        draw_pixel(x+1, y) # thick circle?

    # Actually let's just make it visually similar to the reference
    img.save("base_icon.png")

draw_pixel_icon()
