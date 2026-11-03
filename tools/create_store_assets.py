from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


OUT = Path(__file__).resolve().parents[1] / "docs" / "store-assets"
OUT.mkdir(parents=True, exist_ok=True)
BLUE = (33, 102, 165)
GREEN = (27, 138, 90)
INK = (20, 39, 63)
WHITE = (255, 255, 255)


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    name = "arialbd.ttf" if bold else "arial.ttf"
    return ImageFont.truetype(f"C:/Windows/Fonts/{name}", size=size)


def icon() -> None:
    image = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((24, 24, 488, 488), radius=112, fill=BLUE)
    draw.rounded_rectangle((108, 104, 404, 410), radius=34, fill=WHITE)
    draw.rounded_rectangle((108, 104, 404, 179), radius=34, fill=GREEN)
    draw.rectangle((108, 143, 404, 179), fill=GREEN)
    draw.rounded_rectangle((170, 72, 205, 139), radius=16, fill=WHITE)
    draw.rounded_rectangle((307, 72, 342, 139), radius=16, fill=WHITE)
    draw.line((175, 284, 224, 332, 340, 226), fill=GREEN, width=30, joint="curve")
    image.save(OUT / "studymate-icon-512.png")


def feature_graphic() -> None:
    width, height = 1024, 500
    image = Image.new("RGB", (width, height))
    pixels = image.load()
    for x in range(width):
        for y in range(height):
            blend = x / width
            pixels[x, y] = (
                int(INK[0] * (1 - blend) + BLUE[0] * blend),
                int(INK[1] * (1 - blend) + BLUE[1] * blend),
                int(INK[2] * (1 - blend) + BLUE[2] * blend),
            )
    draw = ImageDraw.Draw(image, "RGBA")
    draw.ellipse((720, -140, 1160, 320), fill=(255, 255, 255, 18))
    draw.ellipse((650, 265, 950, 565), fill=(27, 138, 90, 110))

    # Phone-shaped dashboard preview uses generic sample content, not personal data.
    draw.rounded_rectangle((682, 42, 925, 458), radius=38, fill=(250, 252, 255, 255))
    draw.rounded_rectangle((708, 78, 899, 119), radius=12, fill=(230, 240, 248, 255))
    draw.rounded_rectangle((708, 139, 899, 245), radius=18, fill=(230, 240, 248, 255))
    draw.rounded_rectangle((727, 157, 823, 169), radius=6, fill=(33, 102, 165, 255))
    draw.rounded_rectangle((727, 181, 868, 190), radius=5, fill=(106, 126, 145, 255))
    draw.rounded_rectangle((727, 204, 840, 213), radius=5, fill=(106, 126, 145, 255))
    draw.rounded_rectangle((708, 264, 899, 361), radius=18, fill=(228, 244, 235, 255))
    draw.rounded_rectangle((727, 283, 819, 295), radius=6, fill=(27, 138, 90, 255))
    draw.rounded_rectangle((727, 308, 865, 317), radius=5, fill=(106, 126, 145, 255))
    draw.rounded_rectangle((727, 330, 847, 339), radius=5, fill=(106, 126, 145, 255))
    draw.rounded_rectangle((754, 425, 854, 433), radius=4, fill=(185, 197, 207, 255))

    draw.text((70, 96), "StudyMate", font=font(68, True), fill=WHITE)
    draw.rounded_rectangle((72, 188, 170, 197), radius=4, fill=GREEN)
    draw.text((70, 226), "Plan your study life", font=font(34), fill=(235, 243, 250))
    draw.text((70, 286), "Assignments  •  Timetable  •  Progress", font=font(22), fill=(210, 225, 240))
    image.save(OUT / "studymate-feature-1024x500.png", quality=95)


if __name__ == "__main__":
    icon()
    feature_graphic()
