# Modrinth 업로드 가이드 (복붙용)

## 빌드
```
./gradlew build          # Windows: gradlew.bat build  (JDK 25 필요)
```
업로드할 파일: `build/libs/trollmod-1.0.0.jar`  (`-sources.jar` 는 올리지 마세요)

## 프로젝트 만들기 (modrinth.com → Create a project)
- Name: Troll Mod
- URL(slug): trollmod  (이미 쓰이면 trollmod-shorts 등으로)
- Summary: 바닐라처럼 보이지만 안 되는 게 되는 숏츠용 트롤 모드 — 네더 침대, 눈 마주치면 멈추는 크리퍼, 정제된 다이아몬드
- Project type: Mod / Categories: Game Mechanics, Utility (필요 시 Adventure)
- Client side: Optional / Server side: Required
- License: 본인 선택 (모르면 MIT)
- Icon: src/main/resources/assets/trollmod/icon.png

## Description (본문)
바닐라 플레이처럼 보이지만 특정 상황에서만 "절대 안 되는 일"이 되는 연출용 모드입니다. 설정·명령어·키 없이 설치만 하면 바로 동작하고, 채팅에 디버그 메시지를 띄우지 않습니다.

- 네더/엔드 침대: 폭발하지 않고 정상적으로 누워 자며 "리스폰 지점이 설정되었습니다" 출력
- 크리퍼 눈싸움: 8블록 이내에서 크리퍼를 정면으로 바라보면 멈춰 서서 쳐다보고 폭발 카운트다운이 초기화, 시선을 돌리면 원래대로
- 정제된 다이아몬드: 용암에 던진 다이아몬드는 타지 않고 0.4초 뒤 "정제된 다이아몬드"(반짝임)로 변해 플레이어 쪽으로 튀어 오름

요구 사항: Fabric Loader 0.19.5+, Fabric API, Java 25, Minecraft 26.3

## 버전 업로드 (Versions → Create version)
- Version number: 1.0.0
- Version title: Troll Mod 1.0.0
- Release channel: Release (처음이라 불안하면 Beta)
- Loaders: Fabric
- Game versions: 26.3
- Dependencies: Fabric API (Required)
- Changelog: 첫 릴리스 — 네더/엔드 침대, 크리퍼 시선 정지, 정제된 다이아몬드

## .mrpack 만들기
`./gradlew build` 후 `python3 make_mrpack.py` → `trollmod-1.0.0.mrpack`
(Fabric API는 Modrinth API에서 URL·해시를 받아 자동 기록, Troll Mod jar는 overrides/mods 에 포함)
참고: .mrpack 은 '모드팩' 프로젝트용입니다. 모드 단독 프로젝트에는 jar 를 그대로 올리세요.
