# Car Dark Icons: iOS-dark-style icon pack for the Yato B100 head unit.
# usage: python gen_icons.py <font.ttf> <codepoints> <out-res-dir>
import sys, os, math
from PIL import Image, ImageDraw, ImageFont, ImageFilter
font_path, cp_path, out = sys.argv[1:4]
cps = dict(l.split() for l in open(cp_path) if l.strip())
S = 192          # xxhdpi 48dp
SS = 4           # supersample
BG_TOP, BG_BOT, RIM = (46,46,48), (24,24,26), (64,64,68)
# label, component, glyph, accent
ICONS = [
 ("carplay",   "app.carplayscreen/app.carplayscreen.MainActivity",                  "phone_iphone", (48,209,88)),
 ("maps",      "com.google.android.apps.maps/com.google.android.maps.MapsActivity", "near_me",   (48,209,88)),
 ("autokit",   "cn.manstep.phonemirrorBox/cn.manstep.phonemirrorBox.MainActivity",  "phonelink", (48,209,88)),
 ("easyconn",  "net.easyconn/net.easyconn.ui.Ljzl01MainActivity",                   "cast",      (48,209,88)),
 ("camera",    "com.bw.adas/com.bw.adas.reverse.CameraActivity",                     "videocam",  (229,229,234)),
 ("music",     "com.bw.mediaplayer/com.bw.mediaplayer.ui.MusicActivity",             "library_music",(255,55,95)),
 ("radio",     "com.bw.mediaplayer/com.bw.mediaplayer.ui.RadioActivity",             "radio",     (255,159,10)),
 ("video",     "com.bw.mediaplayer/com.bw.mediaplayer.ui.VideoListActivity",         "movie",     (191,90,242)),
 ("images",    "com.bw.mediaplayer/com.bw.mediaplayer.ui.ImageListActivity",         "image",     (255,214,10)),
 ("aux",       "com.bw.mediaplayer/com.bw.mediaplayer.ui.AuxActivity",               "cable",     (142,142,147)),
 ("btmusic",   "com.bw.mediaplayer/com.bw.mediaplayer.ui.BtActivity",                "music_note",(255,55,95)),
 ("phone",     "com.bw.phone/com.bw.phone.MainActivity",                             "bluetooth", (10,132,255)),
 ("ipod",      "com.bw.ipod/com.bw.ipod.MainActivity",                               "library_music",(255,55,95)),
 ("settings",  "com.bw.settings/com.bw.settings.MainActivity",                       "settings",  (142,142,147)),
 ("charge",    "com.bw.settings/com.bw.settings.charge.QuickChargeActivity",         "bolt",      (255,214,10)),
 ("carsetting","com.bw.carsetting/com.bw.carsetting.MainActivity",                   "directions_car",(10,132,255)),
 ("personal",  "com.bw.personalization/com.bw.personalization.MainActivity",         "palette",   (191,90,242)),
 ("mute",      "com.bw.personalization/com.bw.personalization.MuteActivity",         "speaker",   (142,142,147)),
 ("navi",      "com.bw.personalization/com.bw.personalization.NaviActivity",         "navigation",(48,209,88)),
 ("screen",    "com.bw.personalization/com.bw.personalization.ScreenProtectActivity","smartphone",(142,142,147)),
 ("voice",     "com.bw.asr/com.bw.asr.MainActivity",                                 "mic",       (255,69,58)),
 ("voicecmd",  "com.bw.asr/com.bw.asr.VoiceCommandActivity",                         "mic",       (255,69,58)),
 ("guide",     "com.bw.userguide/com.bw.userguide.MainActivity",                     "menu_book", (100,210,255)),
 ("androidset","com.android.settings/com.android.settings.Settings",                 "tune",      (142,142,147)),
 ("browser",   "com.android.browser/com.android.browser.BrowserActivity",            "public",    (10,132,255)),
 ("files",     "com.android.documentsui/com.android.documentsui.LauncherActivity",   "folder",    (100,210,255)),
 ("playstore", "com.android.vending/com.android.vending.AssetBrowserActivity",       "shop",      (48,209,88)),
 ("calc",      "com.google.android.calculator/com.android.calculator2.Calculator",   "calculate", (255,159,10)),
 ("keyboard",  "com.android.inputmethod.latin/com.android.inputmethod.latin.setup.SetupActivity","keyboard",(142,142,147)),
 ("stk",       "com.android.stk/com.android.stk.StkMain",                            "sell",      (142,142,147)),
 ("google",    "com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchActivity","search",(10,132,255)),
 ("mdm",       "mdm.tw.com.mdm/mdm.tw.com.mdm.ScreenSplash",                         "info",      (142,142,147)),
 ("nova",      "com.teslacoilsw.launcher/com.teslacoilsw.launcher.preferences.SettingsActivity","apps",(255,69,58)),
 ("gmail",     "com.google.android.gm/com.google.android.gm.ConversationListActivityGmail","sell",(255,69,58)),
]
def squircle_mask(size, n=5.0, inset=0.0):
    m = Image.new("L", (size, size), 0); px = m.load(); r = size/2 - inset
    for y in range(size):
        for x in range(size):
            dx, dy = (x+0.5-size/2)/r, (y+0.5-size/2)/r
            if abs(dx)**n + abs(dy)**n <= 1.0: px[x, y] = 255
    return m
def backplate(size):
    big = size*SS
    grad = Image.new("RGBA", (big, big))
    d = ImageDraw.Draw(grad)
    for y in range(big):
        t = y/big
        c = tuple(int(BG_TOP[i]*(1-t)+BG_BOT[i]*t) for i in range(3)) + (255,)
        d.line([(0, y), (big, y)], fill=c)
    rim = Image.new("RGBA", (big, big), RIM+(255,))
    mask_o = squircle_mask(big); mask_i = squircle_mask(big, inset=SS*1.2)
    img = Image.new("RGBA", (big, big), (0,0,0,0))
    img.paste(rim, mask=mask_o); img.paste(grad, mask=mask_i)
    # soft top highlight
    hl = Image.new("RGBA", (big, big), (255,255,255,0)); hd = ImageDraw.Draw(hl)
    hd.ellipse([-big*0.2, -big*0.55, big*1.2, big*0.45], fill=(255,255,255,18))
    hl.putalpha(Image.composite(hl.split()[3], Image.new("L",(big,big),0), mask_i))
    img = Image.alpha_composite(img, hl)
    return img.resize((size, size), Image.LANCZOS), mask_o.resize((size,size), Image.LANCZOS)
def glyph(name, color, size):
    big = size*SS
    f = ImageFont.truetype(font_path, int(big*0.52))
    try: f.set_variation_by_axes([1, 0, 48, 400])   # FILL, GRAD, opsz, wght
    except Exception as e: print("variation not applied:", e, file=sys.stderr)
    ch = chr(int(cps[name], 16))
    layer = Image.new("RGBA", (big, big), (0,0,0,0)); d = ImageDraw.Draw(layer)
    bb = d.textbbox((0,0), ch, font=f); w, h = bb[2]-bb[0], bb[3]-bb[1]
    x, y = (big-w)/2 - bb[0], (big-h)/2 - bb[1]
    sh = Image.new("RGBA", (big, big), (0,0,0,0)); ImageDraw.Draw(sh).text((x, y+SS*2), ch, font=f, fill=(0,0,0,120))
    sh = sh.filter(ImageFilter.GaussianBlur(SS*2))
    d.text((x, y), ch, font=f, fill=color+(255,))
    return Image.alpha_composite(sh, layer).resize((size, size), Image.LANCZOS)
os.makedirs(f"{out}/drawable-nodpi", exist_ok=True); os.makedirs(f"{out}/xml", exist_ok=True); os.makedirs(f"{out}/values", exist_ok=True)
plate, mask = backplate(S)
plate.save(f"{out}/drawable-nodpi/iconback.png")
Image.merge("RGBA", (mask, mask, mask, mask)).save(f"{out}/drawable-nodpi/iconmask.png")
Image.new("RGBA", (S, S), (0,0,0,0)).save(f"{out}/drawable-nodpi/iconupon.png")
items, names = [], []
for name, comp, g, col in ICONS:
    img = Image.alpha_composite(plate, glyph(g, col, S)); img.save(f"{out}/drawable-nodpi/ic_{name}.png")
    items.append(f'  <item component="ComponentInfo{{{comp}}}" drawable="ic_{name}"/>'); names.append(f'  <item drawable="ic_{name}"/>')
pack_icon = Image.alpha_composite(plate, glyph("directions_car", (255,69,58), S)); pack_icon.save(f"{out}/drawable-nodpi/ic_pack.png")
open(f"{out}/xml/appfilter.xml","w").write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n  <iconback img1="iconback"/>\n  <iconmask img1="iconmask"/>\n  <iconupon img1="iconupon"/>\n  <scale factor="0.62"/>\n' + "\n".join(items) + "\n</resources>\n")
open(f"{out}/xml/drawable.xml","w").write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n  <version>1</version>\n  <category title="Car"/>\n' + "\n".join(names) + "\n</resources>\n")
open(f"{out}/values/strings.xml","w").write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n  <string name="app_name">Car Dark Icons</string>\n</resources>\n')
print("icons:", len(ICONS))
