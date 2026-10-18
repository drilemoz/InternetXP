#!/usr/bin/env python3
"""Inspeciona tamanho, ABI nativa e compressão do APK real gerado pela CI.

Falha caso o APK não contenha bibliotecas nativas da arquitetura esperada.
O relatório é apenas de build, não é incluído no APK.
"""
import sys
from collections import defaultdict
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ABIS = {"armeabi-v7a", "arm64-v8a", "x86_64"}


def inspect_apk(path: Path, expected_abi: str) -> dict:
    if expected_abi not in ABIS:
        raise ValueError(f"ABI desconhecida: {expected_abi}")
    stats = defaultdict(lambda: [0, 0])
    native_abis = set()
    native_count = 0
    with ZipFile(path) as apk:
        for member in apk.infolist():
            if member.is_dir():
                continue
            category = "demais"
            if member.filename.startswith("lib/") and member.filename.endswith(".so"):
                parts = member.filename.split("/")
                if len(parts) != 3 or parts[1] not in ABIS:
                    raise ValueError(f"ABI inválida: {member.filename}")
                native_abis.add(parts[1])
                native_count += 1
                category = "bibliotecas .so"
                if member.compress_type != ZIP_DEFLATED:
                    raise ValueError(f"Biblioteca nativa não compactada: {member.filename}")
            elif member.filename.endswith(".dex"):
                category = "código dex"
            elif member.filename.startswith("assets/"):
                category = "assets"
            elif member.filename.startswith("res/"):
                category = "recursos"
            stats[category][0] += member.compress_size
            stats[category][1] += member.file_size
    if native_count == 0:
        raise ValueError("APK sem bibliotecas nativas GeckoView")
    if native_abis != {expected_abi}:
        raise ValueError(f"ABI esperada {expected_abi}, encontrado {sorted(native_abis)}")
    return {"total": path.stat().st_size, "stats": dict(stats), "native_count": native_count}


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Uso: report_apk.py ARQUIVO.apk ABI")
    path = Path(sys.argv[1]); abi = sys.argv[2]
    try:
        info = inspect_apk(path, abi)
    except (OSError, ValueError) as exc:
        raise SystemExit(f"ERRO: {exc}") from exc
    to_mib = lambda n: n / (1024 * 1024)
    print(f"TAMANHO APK {abi}: {to_mib(info['total']):.2f} MiB ({info['total']} bytes)")
    print(f"Bibliotecas nativas verificadas: {info['native_count']}")
    for category, (packed, unpacked) in sorted(info["stats"].items(), key=lambda p: -p[1][0]):
        print(f"  {category}: ZIP {to_mib(packed):.2f} MiB | extraído {to_mib(unpacked):.2f} MiB")


if __name__ == "__main__":
    main()
