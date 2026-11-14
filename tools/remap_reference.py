#!/usr/bin/env python3
"""Rename Forge SRG names (m_12345_, f_12345_) in decompiled 1.20.1 sources to Mojang's names.

For reading the original mod's code side by side with the port; the result is a local reference only.

    python3 tools/remap_reference.py <client.txt> <joined.tsrg> <source dir>

client.txt: Mojang's 1.20.1 client mappings (from the version manifest).
joined.tsrg: MCPConfig 1.20.1's config/joined.tsrg (tsrg2: obfuscated -> SRG -> id).
Files are rewritten in place. Parameter names (p_12345_) are left alone.
"""
import re
import sys
from pathlib import Path


def mojang(path):
    """Mojang mappings: (obf class, obf member, kind) -> Mojang member name."""
    names = {}
    cls = None
    for line in open(path, encoding="utf-8"):
        if line.startswith("#"):
            continue
        if not line.startswith(" "):
            # net.minecraft.Foo -> abc:
            cls = line.split(" -> ")[1].rstrip(":\n")
            continue
        left, obf = line.strip().split(" -> ")
        if "(" in left:
            name = left.split("(")[0].split()[-1]
            names[(cls, obf, "m", left.split("(")[1].split(")")[0].count(",") + (0 if left.split("(")[1].startswith(")") else 1))] = name
            names.setdefault((cls, obf, "m*"), set()).add(name)
        else:
            names[(cls, obf, "f")] = left.split()[-1]
    return names


def srg(path, moj):
    """SRG id -> Mojang name, joined on the obfuscated class and member."""
    out = {}
    cls = None
    for line in open(path, encoding="utf-8"):
        if line.startswith("tsrg") or line.startswith("\t\t"):
            continue
        if not line.startswith("\t"):
            cls = line.split()[0].replace("/", ".")
            continue
        parts = line.split()
        if len(parts) >= 3 and parts[1].startswith("("):  # method: obf desc srg [id]
            obf, desc, sid = parts[:3]
            argc = len(re.findall(r"\[*(?:L[^;]+;|[ZBCSIJFD])", desc[1:desc.index(")")]))
            name = moj.get((cls, obf, "m", argc))
            if name is None:
                cands = moj.get((cls, obf, "m*"), set())
                name = next(iter(cands)) if len(cands) == 1 else None
        elif len(parts) in (2, 3):  # field: obf srg [id]
            obf, sid = parts[:2]
            name = moj.get((cls, obf, "f"))
        else:
            continue
        if name and (sid.startswith("m_") or sid.startswith("f_")):
            out.setdefault(sid, name)
    return out


def main():
    client, tsrg, root = sys.argv[1:4]
    table = srg(tsrg, mojang(client))
    pattern = re.compile(r"\b[mf]_\d+_\b")
    changed = left = 0
    for file in Path(root).rglob("*.java"):
        text = file.read_text(encoding="utf-8")
        new = pattern.sub(lambda m: table.get(m.group(0), m.group(0)), text)
        if new != text:
            file.write_text(new, encoding="utf-8")
            changed += 1
        left += len(pattern.findall(new))
    print(f"{len(table)} names known; {changed} files changed; {left} SRG names left")


if __name__ == "__main__":
    main()
