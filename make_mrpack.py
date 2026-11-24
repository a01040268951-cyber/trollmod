#!/usr/bin/env python3
"""./gradlew build 후 실행:  python3 make_mrpack.py
Modrinth API에서 Fabric API(26.3)의 URL/해시를 받아 .mrpack 을 만든다. (표준 라이브러리만 사용, 인터넷 필요)"""
import glob, hashlib, json, os, sys, urllib.parse, urllib.request, zipfile

MC, LOADER, VERSION = "26.3", "0.19.5", "1.0.0"
NAME = "Troll Mod Pack"
SUMMARY = "Troll Mod + Fabric API (Minecraft 26.3)"

def fetch_fabric_api():
    q = urllib.parse.urlencode({"game_versions": json.dumps([MC]), "loaders": json.dumps(["fabric"])})
    req = urllib.request.Request("https://api.modrinth.com/v2/project/fabric-api/version?" + q,
                                 headers={"User-Agent": "trollmod-mrpack/1.0"})
    versions = json.load(urllib.request.urlopen(req, timeout=30))
    if not versions:
        sys.exit("Fabric API 26.3 버전을 Modrinth에서 찾지 못했습니다.")
    v = next((x for x in versions if x["version_type"] == "release"), versions[0])
    f = next((x for x in v["files"] if x.get("primary")), v["files"][0])
    return {
        "path": "mods/" + f["filename"],
        "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
        "env": {"client": "required", "server": "required"},
        "downloads": [f["url"]],
        "fileSize": f["size"],
    }

def main():
    jars = [j for j in glob.glob("build/libs/trollmod-*.jar") if not j.endswith("-sources.jar")]
    if not jars:
        sys.exit("build/libs 에 jar 가 없습니다. 먼저 ./gradlew build 를 실행하세요.")
    jar = jars[0]
    index = {
        "formatVersion": 1, "game": "minecraft", "versionId": VERSION, "name": NAME, "summary": SUMMARY,
        "files": [fetch_fabric_api()],
        "dependencies": {"minecraft": MC, "fabric-loader": LOADER},
    }
    out = f"trollmod-{VERSION}.mrpack"
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("modrinth.index.json", json.dumps(index, indent=2))
        z.write(jar, "overrides/mods/" + os.path.basename(jar))
    print("생성 완료:", out)

if __name__ == "__main__":
    main()
