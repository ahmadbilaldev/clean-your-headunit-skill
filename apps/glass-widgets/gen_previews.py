# Preview images for the widget picker (the real widgets render themselves on the device).
import sys
from PIL import Image, ImageDraw, ImageFont
out = sys.argv[1]
W, H = 504, 157
def plate():
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle((1, 1, W-2, H-2), 40, fill=(255, 255, 255, 46), outline=(255, 255, 255, 80), width=2)
    return im, d
def font(sz, bold=False):
    for p in (["/System/Library/Fonts/SFNS.ttf"] if not bold else ["/System/Library/Fonts/SFNS.ttf"]):
        try: return ImageFont.truetype(p, sz)
        except Exception: pass
    return ImageFont.load_default()
im, d = plate()
d.text((28, 28), "12:19", font=font(60), fill=(255,255,255,255)); d.text((188, 50), "PM", font=font(22), fill=(255,255,255,185))
d.text((30, 104), "Sunday, 16 November", font=font(24), fill=(255,255,255,185))
im.save(f"{out}/preview_clock.png")
im, d = plate()
d.rounded_rectangle((20, 20, 137, 137), 16, fill=(255,255,255,46))
d.ellipse((60, 90, 84, 108), fill=(255,255,255,200)); d.rectangle((80, 50, 85, 100), fill=(255,255,255,200))
d.text((158, 46), "Song title", font=font(30), fill=(255,255,255,255)); d.text((158, 86), "Artist", font=font(24), fill=(255,255,255,185))
for i, hh in enumerate((16, 28, 22)): d.rounded_rectangle((440+i*11, 92-hh, 446+i*11, 92), 3, fill=(255,255,255,255))
im.save(f"{out}/preview_now.png")
