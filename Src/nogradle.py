# Костыльнее этого кала нет ничего, но градл совсем умер;(

import os
import sys
import subprocess
import shutil
import hashlib
import zipfile
import configparser
import logging
import requests
import tempfile
import base64
from pathlib import Path
from datetime import datetime
from urllib.parse import urlparse

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler("build.log", mode='w', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def initialize_project():
    logger.info("[+] Начало инициализации нового проекта...")
    if Path('AndroidManifest.xml').exists() or Path('java').exists():
        logger.error("[-] Ошибка: Файл 'AndroidManifest.xml' или папка 'java' уже существуют.")
        return

    package_name, app_name = "com.example.myapp", "MyFirstApp"
    package_path = Path('java') / package_name.replace('.', '/')
    logger.info("[*] Создание структуры директорий...")
    package_path.mkdir(parents=True, exist_ok=True)

    for d in ['res/layout', 'res/values', 'assets', 'jni', 'libs', 'jniLibs']:
        Path(d).mkdir(parents=True, exist_ok=True)

    placeholder_icon_dir = Path("res/drawable-mdpi")
    placeholder_icon_dir.mkdir(exist_ok=True)
    icon_data = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=")
    with open(placeholder_icon_dir / "ic_launcher.png", "wb") as f:
        f.write(icon_data)
    logger.info("[*] Создана иконка-заглушка в res/drawable-mdpi/")
    
    default_platform = "34"
    min_sdk = "24"

    local_properties_content = f"""[DEFAULT]
sdk.dir=PATH_TO_YOUR_ANDROID_SDK
ndk.dir=PATH_TO_YOUR_ANDROID_NDK
java.home=PATH_TO_YOUR_JDK_HOME
build.tools.version=34.0.0
android.platform={default_platform}
"""
    with open('local.properties', 'w') as f:
        f.write(local_properties_content)
    logger.info("[*] Создан 'local.properties'. Не забудьте указать в нем пути!")
    with open('import.config', 'w') as f:
        f.write("[repositories]\n\n[dependencies]\n\n[jni]\n")
    with open('proguard-rules.pro', 'w') as f:
        f.write("-keep public class * extends android.app.Activity\n-keep public class * extends android.app.Application")
    
    manifest_content = f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="{package_name}">

    <uses-sdk android:minSdkVersion="{min_sdk}" android:targetSdkVersion="{default_platform}" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@drawable/ic_launcher"
        android:theme="@android:style/Theme.Material.Light">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"""
    with open('AndroidManifest.xml', 'w') as f:
        f.write(manifest_content)
    logger.info("[*] Создан AndroidManifest.xml с targetSdkVersion.")

    with open('res/values/strings.xml', 'w') as f:
        f.write(f'<resources>\n    <string name="app_name">{app_name}</string>\n    <string name="hello_world">Hello from Python Build Script!</string>\n</resources>')
    with open('res/layout/activity_main.xml', 'w') as f:
        f.write('<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent" android:layout_height="match_parent" android:gravity="center"><TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/hello_world" android:textSize="24sp" /></RelativeLayout>')
    with open(package_path / 'MainActivity.java', 'w') as f:
        f.write(f"""package {package_name};

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {{
    @Override
    protected void onCreate(Bundle savedInstanceState) {{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }}
}}""")

    logger.info("\n" + "="*60 + "\n[OK] ИНИЦИАЛИЗАЦИЯ ПРОЕКТА ЗАВЕРШЕНА!\n1. Отредактируйте 'local.properties'.\n2. (Опционально) Замените иконку-заглушку: python nogradle.py create-icon path/to/icon.png\n3. Запустите сборку: python nogradle.py\n" + "="*60)

def create_adaptive_icons(source_image_path):
    try:
        from PIL import Image
    except ImportError:
        logger.error("[-] Ошибка: требуется Pillow. Установите: pip install Pillow")
        sys.exit(1)
    
    source_path = Path(source_image_path)
    if not source_path.is_file():
        raise FileNotFoundError(f"Файл не найден: {source_path}")
    ICON_DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    with Image.open(source_path) as source_img:
        for density, size in ICON_DENSITIES.items():
            target_dir = Path("res") / f"drawable-{density}"
            target_dir.mkdir(exist_ok=True)
            resized_img = source_img.resize((size, size), Image.Resampling.LANCZOS)
            resized_img.save(target_dir / "ic_launcher.png", "PNG")
    logger.info("[OK] Все иконки успешно созданы.")

def load_config():
    configs = {}
    local_props = configparser.ConfigParser()
    
    if not local_props.read("local.properties"):
        raise FileNotFoundError("local.properties не найден. Запустите 'python nogradle.py init'")
    
    configs["local"] = {k: v for k, v in local_props.items("DEFAULT")}
    
    if any(v.startswith('PATH_TO_') for v in configs["local"].values()):
        logger.warning("ВНИМАНИЕ: 'local.properties' содержит пути-заглушки.")
    
    if os.path.exists("import.config"):
        import_config = configparser.ConfigParser()
        import_config.read("import.config")
        configs["import"] = {s: dict(import_config.items(s)) for s in import_config.sections()}
    
    return configs

config = None
SDK_PATH, NDK_PATH, JAVA_HOME, BUILD_TOOLS_VERSION, ANDROID_PLATFORM = [None] * 5
BUILD_TOOLS_DIR, AAPT2, D8, R8_JAR, ZIPALIGN, APKSIGNER = [None] * 6
SRC_JAVA, SRC_RES, SRC_ASSETS, SRC_JNI = Path("java"), Path("res"), Path("assets"), Path("jni")
LIBS_DIR, JNI_LIBS_DIR, MANIFEST, PROGUARD_RULES = Path("libs"), Path("jniLibs"), Path("AndroidManifest.xml"), Path("proguard-rules.pro")
BUILD_DIR, TOOLS_DIR = Path("build"), Path("build") / "tools"
OBJ_DIR, GEN_DIR, CLASSES_DIR, APK_DIR = BUILD_DIR/"obj", BUILD_DIR/"gen", BUILD_DIR/"classes", BUILD_DIR/"apk"
CACHE_DIR, COMPILED_JNI_DIR, DOWNLOAD_CACHE, UNPACKED_AARS_DIR = BUILD_DIR/"cache", BUILD_DIR/"compiledJni", BUILD_DIR/"download_cache", BUILD_DIR/"unpacked_aars"

def setup_paths():
    global config, SDK_PATH, NDK_PATH, JAVA_HOME, BUILD_TOOLS_VERSION, ANDROID_PLATFORM, BUILD_TOOLS_DIR, AAPT2, D8, R8_JAR, ZIPALIGN, APKSIGNER
    config = load_config()
    local_cfg = config["local"]
    SDK_PATH = Path(local_cfg["sdk.dir"])
    NDK_PATH = Path(local_cfg["ndk.dir"])
    JAVA_HOME = Path(local_cfg["java.home"])
    BUILD_TOOLS_VERSION = local_cfg["build.tools.version"]
    ANDROID_PLATFORM = local_cfg["android.platform"]
    
    BUILD_TOOLS_DIR = SDK_PATH / "build-tools" / BUILD_TOOLS_VERSION
    is_windows = sys.platform == "win32"
    AAPT2 = BUILD_TOOLS_DIR/("aapt2.exe" if is_windows else "aapt2")
    D8 = BUILD_TOOLS_DIR/("d8.bat" if is_windows else "d8")
    ZIPALIGN = BUILD_TOOLS_DIR/("zipalign.exe" if is_windows else "zipalign")
    APKSIGNER = BUILD_TOOLS_DIR/("apksigner.bat" if is_windows else "apksigner")
    R8_JAR = TOOLS_DIR / "r8.jar"

def log_step(step_name):
    def decorator(func):
        def wrapper(*args, **kwargs):
            logger.info(f"[*] Начало: {step_name}")
            start_time = datetime.now()
            try:
                result = func(*args, **kwargs)
                logger.info(f"[OK] Успешно: {step_name} (затрачено: {datetime.now() - start_time})")
                return result
            except Exception as e:
                logger.error(f"[-] Ошибка в '{step_name}': {e}")
                raise
        
        return wrapper
    
    return decorator

def download_file(url, dest_path):
    dest_path.parent.mkdir(parents=True, exist_ok=True)
    cache_path = DOWNLOAD_CACHE / dest_path.name
    if cache_path.exists():
        shutil.copy(cache_path, dest_path)
        return
    
    logger.info(f"Скачивание: {url} -> {dest_path}")
    with requests.get(url, stream=True) as r:
        r.raise_for_status()
        with open(dest_path, "wb") as f:
            for chunk in r.iter_content(chunk_size=8192):
                f.write(chunk)
    
    shutil.copy(dest_path, cache_path)

def ensure_r8_is_available():
    if R8_JAR.exists():
        return
    
    logger.info("R8 не найден, скачиваем...")
    r8_version = "8.3.36" 
    download_file(f"https://repo1.maven.org/maven2/com/android/tools/r8/{r8_version}/r8-{r8_version}.jar", R8_JAR)

@log_step("Распаковка AAR")
def unpack_aar(aar_path):
    unpack_dir = UNPACKED_AARS_DIR / aar_path.stem
    if unpack_dir.exists():
        return
    
    unpack_dir.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(aar_path, "r") as zip_ref:
        zip_ref.extractall(unpack_dir)

@log_step("Загрузка зависимостей")
def download_dependencies():
    if not config.get("import"):
        return
    
    deps = config["import"].get("dependencies", {})
    
    for lib, coord in deps.items():
        group, artifact, version = coord.split(":")
        repo_url = config["import"].get("repositories", {}).get("maven.google", "https://maven.google.com/")
        artifact_path = f"{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}"
    
        for ext in ['.aar', '.jar']:
            try:
                dest_path = LIBS_DIR / f"{artifact}-{version}{ext}"
                download_file(f"{repo_url.rstrip('/')}/{artifact_path}{ext}", dest_path)
                if ext == '.aar':
                    unpack_aar(dest_path)
                break 
            except Exception:
                if ext == '.jar':
                    logger.warning(f"Не удалось скачать {lib}")

@log_step("Компиляция JNI")
def compile_jni():
    if not SRC_JNI.exists() or not any(SRC_JNI.iterdir()):
        return
    
    COMPILED_JNI_DIR.mkdir(exist_ok=True)
    
    if (SRC_JNI / "Android.mk").exists():
        ndk_build_cmd = "ndk-build.cmd" if sys.platform == "win32" else "ndk-build"
        result = subprocess.run([
            str(NDK_PATH / ndk_build_cmd), 
            f"NDK_PROJECT_PATH={SRC_JNI.resolve()}", 
            f"APP_BUILD_SCRIPT={SRC_JNI.resolve() / 'Android.mk'}", 
            f"NDK_OUT={COMPILED_JNI_DIR.resolve() / 'obj'}", 
            f"NDK_LIBS_OUT={COMPILED_JNI_DIR.resolve() / 'libs'}"
        ], check=False, capture_output=True, text=True, encoding='utf-8')
        
        if result.returncode != 0:
            raise subprocess.CalledProcessError(result.returncode, result.args, result.stdout, result.stderr)

@log_step("Генерация R.java и компиляция ресурсов")
def process_resources():
    GEN_DIR.mkdir(exist_ok=True)
    OBJ_DIR.mkdir(exist_ok=True)
    APK_DIR.mkdir(exist_ok=True)
    
    with open(MANIFEST, "r+") as f:
        import re
        content = f.read()
        # content = re.sub(r'android:targetSdkVersion="(\d+)"', f'android:targetSdkVersion="{ANDROID_PLATFORM}"', content)
        f.seek(0)
        f.write(content)
        f.truncate()

    subprocess.run([str(AAPT2), "compile", "--dir", str(SRC_RES), "-o", str(OBJ_DIR)], check=True)
    link_cmd = [
        str(AAPT2), "link", "-o", str(APK_DIR / "resources.apk"), 
        "--manifest", str(MANIFEST), 
        "-I", str(SDK_PATH / "platforms" / f"android-{ANDROID_PLATFORM}" / "android.jar"), 
        "--java", str(GEN_DIR), 
        "--auto-add-overlay"
    ]
    res_files = [item for p in OBJ_DIR.glob("*.flat") for item in ["-R", str(p)]]
    lib_files = [item for p in LIBS_DIR.glob("*.aar") for item in ["-I", str(p)]]
    subprocess.run(link_cmd + res_files + lib_files, check=True)

@log_step("Компиляция Java-кода")
def compile_java():
    CLASSES_DIR.mkdir(exist_ok=True)
    java_files = list(SRC_JAVA.rglob("*.java")) + list(GEN_DIR.rglob("*.java"))
    if not java_files:
        return
    
    classpath_items = [
        SDK_PATH / "platforms" / f"android-{ANDROID_PLATFORM}" / "android.jar",
        *LIBS_DIR.glob("*.jar"),
        *LIBS_DIR.glob("*.aar")
    ]
    
    subprocess.run([
        str(JAVA_HOME / "bin" / "javac"), 
        "-d", str(CLASSES_DIR), 
        "-classpath", os.pathsep.join(map(str, classpath_items)), 
        "-source", "1.8",
        "-target", "1.8",
        "-encoding", "UTF-8",
        *[str(f) for f in java_files]
    ], check=True)

@log_step("Конвертация в DEX (D8)")
def convert_to_dex():
    if not any(CLASSES_DIR.rglob('*.class')):
        return
    d8_cmd = [
        str(D8),
        "--classpath", str(SDK_PATH / "platforms" / f"android-{ANDROID_PLATFORM}" / "android.jar"),
        "--output", str(APK_DIR),
        *[str(p) for p in CLASSES_DIR.rglob("*.class")],
        *[str(p) for p in LIBS_DIR.glob("*.jar")],
        *[str(p) for p in LIBS_DIR.glob("*.aar")]
    ]
    subprocess.run(d8_cmd, check=True)

@log_step("Оптимизация и конвертация в DEX (R8)")
def run_r8():
    if not any(CLASSES_DIR.rglob('*.class')):
        return
    
    ensure_r8_is_available()
    
    pg_conf = str(PROGUARD_RULES) if PROGUARD_RULES.exists() else str(Path(os.devnull))
    r8_cmd = [
        str(JAVA_HOME / "bin" / "java"),
        "-cp", str(R8_JAR),
        "com.android.tools.r8.R8",
        "--lib", str(SDK_PATH / "platforms" / f"android-{ANDROID_PLATFORM}" / "android.jar"),
        "--output", str(APK_DIR),
        "--pg-conf", pg_conf,
        *[str(p) for p in CLASSES_DIR.rglob("*.class")],
        *[str(p) for p in LIBS_DIR.glob("*.jar")],
        *[str(p) for p in LIBS_DIR.glob("*.aar")]
    ]
    
    if not PROGUARD_RULES.exists():
        logger.warning(f"Файл {PROGUARD_RULES} не найден.")
    
    subprocess.run(r8_cmd, check=True)

@log_step("Сборка APK")
def build_apk(is_release=False):
    unsigned_apk = APK_DIR / "app-unsigned.apk"
    shutil.copy(APK_DIR / "resources.apk", unsigned_apk)
    
    with zipfile.ZipFile(unsigned_apk, "a", compression=zipfile.ZIP_DEFLATED) as apk:
        if (APK_DIR / "classes.dex").exists():
            apk.write(APK_DIR / "classes.dex", "classes.dex")
        
        if SRC_ASSETS.exists():
            for p in SRC_ASSETS.rglob("*"):
                if p.is_file():
                    apk.write(p, f"assets/{p.relative_to(SRC_ASSETS)}")
        
        for aar_dir in UNPACKED_AARS_DIR.glob("*"):
            if not aar_dir.is_dir():
                continue
            
            for asset_path in (aar_dir / "assets").rglob("*"):
                if asset_path.is_file():
                    apk.write(asset_path, f"assets/{asset_path.relative_to(aar_dir / 'assets')}")
            
            for so_file in (aar_dir / "jni").rglob("*.so"):
                apk.write(so_file, f"lib/{so_file.parent.name}/{so_file.name}")
        
        for jni_dir in [COMPILED_JNI_DIR / "libs", JNI_LIBS_DIR]:
            if jni_dir.exists():
                for so_file in jni_dir.rglob("*.so"):
                    apk.write(so_file, f"lib/{so_file.parent.name}/{so_file.name}")
    
    final_apk_name = f"app-{'release' if is_release else 'debug'}.apk"
    aligned_apk = APK_DIR / f"{final_apk_name}-unaligned"
    
    subprocess.run([
        str(ZIPALIGN), 
        "-f", "4", 
        str(unsigned_apk), 
        str(aligned_apk)
    ], check=True)
    
    final_apk = Path(final_apk_name)
    debug_keystore = Path.home() / ".android" / "debug.keystore"
    
    if not debug_keystore.exists():
        logger.info("[*] Создание debug keystore...")
        subprocess.run([
            str(JAVA_HOME / "bin" / "keytool"),
            "-genkey",
            "-v",
            "-keystore", str(debug_keystore),
            "-alias", "androiddebugkey",
            "-storepass", "android",
            "-keypass", "android",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-dname", "CN=Android Debug,O=Android,C=US"
        ], check=True)
    
    logger.info("[*] Подписание APK...")
    subprocess.run([
        str(APKSIGNER),
        "sign",
        "--ks", str(debug_keystore),
        "--ks-pass", "pass:android",
        "--out", str(final_apk),
        str(aligned_apk)
    ], check=True)
    
    logger.info(f"[OK] Готовый APK: {final_apk.resolve()}")

def clean():
    if BUILD_DIR.exists():
        shutil.rmtree(BUILD_DIR)
    
    for f in Path(".").glob("app-*.apk"):
        os.remove(f)
    
    logger.info("Очистка завершена.")

def main():
    args = sys.argv[1:]
    
    if "init" in args:
        initialize_project()
        return
    
    if "clean" in args:
        clean()
        return
    
    if "create-icon" in args:
        try:
            create_adaptive_icons(args[args.index("create-icon") + 1])
        except (IndexError, FileNotFoundError) as e:
            logger.error(f"[-] Ошибка: Укажите правильный путь к PNG файлу. {e}")
        return

    try:
        setup_paths()
        for tool in [AAPT2, D8, ZIPALIGN, APKSIGNER]:
            if not tool.exists():
                logger.error(f"Не найден инструмент: {tool}. Проверьте local.properties.")
                return
        
        build_type = "release" if "release" in args else "debug"
        logger.info(f"===== НАЧАЛО СБОРКИ ({build_type.upper()}) =====")
        
        for d in [LIBS_DIR, JNI_LIBS_DIR, BUILD_DIR, TOOLS_DIR, DOWNLOAD_CACHE]:
            d.mkdir(exist_ok=True)
        
        if build_type == "release":
            ensure_r8_is_available()
        
        download_dependencies()
        compile_jni()
        process_resources()
        compile_java()
        
        if build_type == "release":
            run_r8()
        else:
            convert_to_dex()
        
        build_apk(is_release=(build_type == "release"))
        logger.info(f"===== СБОРКА ({build_type.upper()}) ЗАВЕРШЕНА УСПЕШНО! =====")
    except Exception as e:
        import traceback
        
        logger.error(f"===== СБОРКА ПРЕРВАНА С ОШИБКОЙ: {e} =====")
        logger.debug(traceback.format_exc())
        sys.exit(1)

if __name__ == "__main__":
    main()