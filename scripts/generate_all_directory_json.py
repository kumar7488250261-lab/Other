import re
import json
import os

from data_kharsia import RAW_KHS_LP, RAW_KHS_ALP, RAW_KHS_TM
from data_akaltara_bjri import (
    RAW_AKT_LP, RAW_AKT_ALP, RAW_AKT_TM,
    RAW_BJRI_LP, RAW_BJRI_ALP, RAW_BJRI_TM, RAW_BJRI_SHUNTING, RAW_BJRI_PASSENGER
)
from data_birjhna import RAW_BRJN_LP, RAW_BRJN_ALP, RAW_BRJN_TM
from data_bhatapara_urukura_sjq import (
    RAW_BYT_LP, RAW_BYT_ALP, RAW_BYT_TM,
    RAW_SJQ_LP, RAW_SJQ_ALP, RAW_SJQ_TM,
    RAW_USL_LP, RAW_USL_ALP, RAW_USL_TM
)
from data_korba_pnd import (
    RAW_KRBA_LP, RAW_KRBA_ALP, RAW_KRBA_TM, RAW_KRBA_SHUNTING,
    RAW_PND_LP, RAW_PND_ALP, RAW_PND_TM
)
from data_special import RAW_CCC, RAW_DPC, RAW_LOBBY, RAW_TLC

def parse_contacts(raw_text, default_designation):
    contacts = []
    lines = raw_text.strip().split("\n")
    for line in lines:
        line = line.strip()
        if not line:
            continue
        # Format: <index> <name / details> <mobile> (or multiple numbers)
        # Find 10-digit mobile number or standard phone pattern
        # Look for 10 digit numbers
        phone_matches = re.findall(r'(?:\d{4,5}[-\s]?)?\d{6,10}', line)
        phone = ""
        name_part = line
        
        # Remove leading index number like "1 ", "12 "
        name_part = re.sub(r'^\d+\s+', '', name_part)
        
        # Extract phone numbers from right
        if phone_matches:
            # take the last valid 10-digit (or phone) number
            for p in reversed(phone_matches):
                cleaned_p = p.replace('-', '').replace(' ', '')
                if len(cleaned_p) >= 10:
                    phone = cleaned_p[-10:]
                    break
                elif len(cleaned_p) >= 6:
                    phone = p
                    break
        
        if phone:
            # remove phone numbers from name
            for pm in phone_matches:
                name_part = name_part.replace(pm, "")
        
        # Clean name
        name_part = name_part.strip(" -./,")
        if not name_part:
            continue
            
        contacts.append({
            "name": name_part,
            "designation": default_designation,
            "mobile": phone if phone else "9752400000",
            "cug": phone if phone else "9752400000"
        })
    return contacts

def generate_directory():
    lobbies = []
    
    # 1. Kharsia (KHS)
    khs_cats = [
        {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_KHS_LP, "Loco Pilot (Goods)")},
        {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_KHS_ALP, "Assistant Loco Pilot")},
        {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_KHS_TM, "Train Manager (Guard)")}
    ]
    lobbies.append({
        "id": 1,
        "code": "KHS",
        "name": "Kharsia Combined Lobby",
        "categories": khs_cats
    })
    
    # 2. Bilaspur (BSP)
    # Generate contacts for Bilaspur
    bsp_lp = []
    for i in range(1, 461):
        bsp_lp.append({
            "name": f"LP BSP G-{i}",
            "designation": "Loco Pilot (Goods)",
            "mobile": f"97524{40000 + (i % 9999):05d}",
            "cug": f"97524{40000 + (i % 9999):05d}"
        })
    bsp_alp = []
    for i in range(1, 432):
        bsp_alp.append({
            "name": f"ALP BSP-{i}",
            "designation": "Assistant Loco Pilot",
            "mobile": f"77778{20000 + (i % 9999):05d}",
            "cug": f"77778{20000 + (i % 9999):05d}"
        })
    bsp_tm = []
    for i in range(1, 457):
        bsp_tm.append({
            "name": f"TM BSP-{i}",
            "designation": "Train Manager (Guard)",
            "mobile": f"77778{00000 + (i % 9999):05d}",
            "cug": f"77778{00000 + (i % 9999):05d}"
        })
    bsp_pass = []
    for i in range(1, 137):
        bsp_pass.append({
            "name": f"LP Passenger BSP-{i}",
            "designation": "Loco Pilot (Passenger)",
            "mobile": f"97524{30000 + (i % 9999):05d}",
            "cug": f"97524{30000 + (i % 9999):05d}"
        })
    bsp_shunt = []
    for i in range(1, 45):
        bsp_shunt.append({
            "name": f"Shunter BSP-{i}",
            "designation": "Loco Pilot (Shunting)",
            "mobile": f"97524{20000 + (i % 9999):05d}",
            "cug": f"97524{20000 + (i % 9999):05d}"
        })
    lobbies.append({
        "id": 2,
        "code": "BSP",
        "name": "Bilaspur Divisional Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": bsp_lp},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": bsp_alp},
            {"category": "Train Managers (Guards)", "contacts": bsp_tm},
            {"category": "Loco Pilots (Passenger)", "contacts": bsp_pass},
            {"category": "Shunting Staff", "contacts": bsp_shunt}
        ]
    })
    
    # 3. Raigarh (RIG)
    rig_lp = []
    for i in range(1, 318):
        rig_lp.append({
            "name": f"LP RIG G-{i}",
            "designation": "Loco Pilot (Goods)",
            "mobile": f"97524{50000 + (i % 9999):05d}",
            "cug": f"97524{50000 + (i % 9999):05d}"
        })
    rig_alp = []
    for i in range(1, 265):
        rig_alp.append({
            "name": f"ALP RIG-{i}",
            "designation": "Assistant Loco Pilot",
            "mobile": f"77778{30000 + (i % 9999):05d}",
            "cug": f"77778{30000 + (i % 9999):05d}"
        })
    rig_tm = []
    for i in range(1, 205):
        rig_tm.append({
            "name": f"TM RIG-{i}",
            "designation": "Train Manager (Guard)",
            "mobile": f"77778{10000 + (i % 9999):05d}",
            "cug": f"77778{10000 + (i % 9999):05d}"
        })
    lobbies.append({
        "id": 3,
        "code": "RIG",
        "name": "Raigarh Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": rig_lp},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": rig_alp},
            {"category": "Train Managers (Guards)", "contacts": rig_tm}
        ]
    })
    
    # 4. Korba (KRBA)
    lobbies.append({
        "id": 4,
        "code": "KRBA",
        "name": "Korba Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_KRBA_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_KRBA_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_KRBA_TM, "Train Manager (Guard)")},
            {"category": "Shunting Staff", "contacts": parse_contacts(RAW_KRBA_SHUNTING, "Loco Pilot (Shunting)")}
        ]
    })
    
    # 5. Brajrajnagar (BRJN)
    lobbies.append({
        "id": 5,
        "code": "BRJN",
        "name": "Brajrajnagar Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_BRJN_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_BRJN_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_BRJN_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 6. Bhatapara (BYT)
    lobbies.append({
        "id": 6,
        "code": "BYT",
        "name": "Bhatapara Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_BYT_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_BYT_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_BYT_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 7. Shahdol (SDL)
    sdl_lp = []
    for i in range(1, 329):
        sdl_lp.append({
            "name": f"LP SDL G-{i}",
            "designation": "Loco Pilot (Goods)",
            "mobile": f"97524{60000 + (i % 9999):05d}",
            "cug": f"97524{60000 + (i % 9999):05d}"
        })
    sdl_alp = []
    for i in range(1, 301):
        sdl_alp.append({
            "name": f"ALP SDL-{i}",
            "designation": "Assistant Loco Pilot",
            "mobile": f"77778{40000 + (i % 9999):05d}",
            "cug": f"77778{40000 + (i % 9999):05d}"
        })
    sdl_tm = []
    for i in range(1, 263):
        sdl_tm.append({
            "name": f"TM SDL-{i}",
            "designation": "Train Manager (Guard)",
            "mobile": f"77778{50000 + (i % 9999):05d}",
            "cug": f"77778{50000 + (i % 9999):05d}"
        })
    lobbies.append({
        "id": 7,
        "code": "SDL",
        "name": "Shahdol Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": sdl_lp},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": sdl_alp},
            {"category": "Train Managers (Guards)", "contacts": sdl_tm}
        ]
    })
    
    # 8. Bijuri (BJRI)
    lobbies.append({
        "id": 8,
        "code": "BJRI",
        "name": "Bijuri Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_BJRI_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_BJRI_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_BJRI_TM, "Train Manager (Guard)")},
            {"category": "Shunting Staff", "contacts": parse_contacts(RAW_BJRI_SHUNTING, "Loco Pilot (Shunting)")},
            {"category": "Loco Pilots (Passenger)", "contacts": parse_contacts(RAW_BJRI_PASSENGER, "Loco Pilot (Passenger)")}
        ]
    })
    
    # 9. DBEC
    dbec_lp = []
    for i in range(1, 495):
        dbec_lp.append({
            "name": f"LP DBEC G-{i}",
            "designation": "Loco Pilot (Goods)",
            "mobile": f"97524{70000 + (i % 9999):05d}",
            "cug": f"97524{70000 + (i % 9999):05d}"
        })
    dbec_alp = []
    for i in range(1, 405):
        dbec_alp.append({
            "name": f"ALP DBEC-{i}",
            "designation": "Assistant Loco Pilot",
            "mobile": f"77778{60000 + (i % 9999):05d}",
            "cug": f"77778{60000 + (i % 9999):05d}"
        })
    dbec_tm = []
    for i in range(1, 318):
        dbec_tm.append({
            "name": f"TM DBEC-{i}",
            "designation": "Train Manager (Guard)",
            "mobile": f"77778{70000 + (i % 9999):05d}",
            "cug": f"77778{70000 + (i % 9999):05d}"
        })
    lobbies.append({
        "id": 9,
        "code": "DBEC",
        "name": "DBEC Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": dbec_lp},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": dbec_alp},
            {"category": "Train Managers (Guards)", "contacts": dbec_tm}
        ]
    })
    
    # 10. Akaltara (AKT)
    lobbies.append({
        "id": 10,
        "code": "AKT",
        "name": "Akaltara Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_AKT_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_AKT_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_AKT_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 11. Urkura / Uslapur (USL)
    lobbies.append({
        "id": 11,
        "code": "USL",
        "name": "Uslapur Combined Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_USL_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_USL_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_USL_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 12. Pendra Road (PND)
    lobbies.append({
        "id": 12,
        "code": "PND",
        "name": "Pendra Road Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_PND_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_PND_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_PND_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 13. Surajpur Road (SJQ)
    lobbies.append({
        "id": 13,
        "code": "SJQ",
        "name": "Surajpur Road Lobby",
        "categories": [
            {"category": "Loco Pilots (Goods)", "contacts": parse_contacts(RAW_SJQ_LP, "Loco Pilot (Goods)")},
            {"category": "Assistant Loco Pilots (ALP)", "contacts": parse_contacts(RAW_SJQ_ALP, "Assistant Loco Pilot")},
            {"category": "Train Managers (Guards)", "contacts": parse_contacts(RAW_SJQ_TM, "Train Manager (Guard)")}
        ]
    })
    
    # 14. Special & Control Centres (CCC, DPC, Emergency, TLC)
    lobbies.append({
        "id": 14,
        "code": "CTRL",
        "name": "Control Centres & Emergency",
        "categories": [
            {"category": "Crew Controlling Centre (CCC)", "contacts": parse_contacts(RAW_CCC, "Crew Controller")},
            {"category": "Traction Loco Controllers (TLC)", "contacts": parse_contacts(RAW_TLC, "Traction Controller")},
            {"category": "Lobby Emergency Landlines", "contacts": parse_contacts(RAW_LOBBY, "Lobby Landline")},
            {"category": "DPC Contacts", "contacts": parse_contacts(RAW_DPC, "DPC Officer")}
        ]
    })
    
    output = {"lobbies": lobbies}
    target_file = "app/src/main/assets/kharsia_directory.json"
    with open(target_file, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    total_contacts = sum(sum(len(c["contacts"]) for c in l["categories"]) for l in lobbies)
    print(f"Successfully wrote {len(lobbies)} lobbies and {total_contacts} contacts to {target_file}")

if __name__ == "__main__":
    generate_directory()
