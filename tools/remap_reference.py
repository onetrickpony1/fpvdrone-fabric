#!/usr/bin/env python3
"""Rename Forge SRG names (m_12345_, f_12345_) in decompiled 1.20.1 sources to Mojang's names.

For reading the original mod's code side by side with the port; the result is a local reference only.

    python3 tools/remap_reference.py <client.txt> <joined.tsrg> <source dir>

client.txt: Mojang's 1.20.1 client mappings (from the version manifest).
joined.tsrg: MCPConfig 1.20.1's config/joined.tsrg (tsrg2: obfuscated -> SRG -> id).
Methods are matched on their exact parameter types, so overloads get the right name.
Files are rewritten in place. Parameter names (p_12345_) are left alone.
"""
import re
import sys
from pathlib import Path

PRIMITIVES = {"boolean": "Z", "byte": "B", "char": "C", "short": "S", "int": "I", "long": "J",
              "float": "F", "double": "D", "void": "V"}


def read_mojang(path):
    """Mojang's class names -> obfuscated names, and their members by obfuscated class."""
    classes = {}
    members = []  # (obf class, mojang name, mojang param types or None for a field, obf member)
    cls = None
    for line in open(path, encoding="utf-8"):
        if line.startswith("#"):
            continue
        if not line.startswith(" "):
            named, obf = line.rstrip(":\n").split(" -> ")
            classes[named] = obf
            cls = obf
            continue
        left, obf = line.strip().split(" -> ")
        if "(" in left:
            head, params = left.split("(", 1)
            params = params.rstrip(")")
            members.append((cls, head.split()[-1], [p for p in params.split(",") if p], obf))
        else:
            members.append((cls, left.split()[-1], None, obf))
    return classes, members


def descriptor(java_type, classes):
    dims = java_type.count("[]")
    base = java_type.replace("[]", "")
    if base in PRIMITIVES:
        d = PRIMITIVES[base]
    else:
        d = "L" + classes.get(base, base).replace(".", "/") + ";"
    return "[" * dims + d


def build(client, tsrg):
    classes, members = read_mojang(client)
    methods, fields = {}, {}
    for cls, name, params, obf in members:
        if params is None:
            fields[(cls, obf)] = name
        else:
            args = "".join(descriptor(p, classes) for p in params)
            methods[(cls, obf, args)] = name
    out = {}
    cls = None
    for line in open(tsrg, encoding="utf-8"):
        if line.startswith("tsrg") or line.startswith("\t\t"):
            continue
        if not line.startswith("\t"):
            cls = line.split()[0]
            continue
        parts = line.split()
        if len(parts) >= 3 and parts[1].startswith("("):
            obf, desc, sid = parts[:3]
            name = methods.get((cls, obf, desc[1:desc.index(")")]))
        elif len(parts) in (2, 3):
            obf, sid = parts[:2]
            name = fields.get((cls, obf))
        else:
            continue
        if name and (sid.startswith("m_") or sid.startswith("f_")):
            out.setdefault(sid, name)
    return out


def main():
    client, tsrg, root = sys.argv[1:4]
    table = build(client, tsrg)
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
