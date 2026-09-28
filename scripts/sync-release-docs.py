#!/usr/bin/env python3
"""Syncs version numbers in the docs with the real build configuration.

Sources of truth:
  - Active line:      gradle.properties / build.gradle in the working tree
  - Maintenance line: gradle.properties / build.gradle of the latest git tag
                      matching MAINTENANCE_TAG_GLOB (e.g. v2.0.2)

Rewrites every block between `<!-- AUTO:<name> -->` and `<!-- /AUTO:<name> -->`
in README.md, docs/index.html and docs/curseforge-description.html, updates the
schema.org version and meta descriptions of the website, and regenerates
docs/modrinth-description.md.

Usage:
  scripts/sync-release-docs.py           # rewrite files in place
  scripts/sync-release-docs.py --check   # exit 1 if any file is out of date
"""

import os
import re
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

# Tag pattern of the maintenance line. Set to "" when there is no maintenance line.
MAINTENANCE_TAG_GLOB = os.environ.get("MAINTENANCE_TAG_GLOB", "v2.0.*")

ROOT = Path(__file__).resolve().parent.parent
README = ROOT / "README.md"
INDEX = ROOT / "docs" / "index.html"
CURSEFORGE = ROOT / "docs" / "curseforge-description.html"
MODRINTH_SCRIPT = ROOT / "scripts" / "build-modrinth-description.sh"

CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/enchant-transfer"
CURSEFORGE_ID = "1540807"
MODRINTH_URL = "https://modrinth.com/mod/enchant-transfer"
MODRINTH_SLUG = "enchant-transfer"


@dataclass
class VersionLine:
    mod: str
    minecraft: str
    java: str
    loader: str
    fabric_api: str
    loom: str
    yarn: Optional[str]
    maintenance: bool

    @property
    def series(self) -> str:
        major, minor = self.mod.split(".")[:2]
        return f"{major}.{minor}.x"

    @property
    def status(self) -> str:
        return "Maintenance (critical bug fixes only)" if self.maintenance else "Active development"


def parse_properties(text: str) -> dict:
    props = {}
    for line in text.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def loom_version(props: dict, build_gradle: str) -> str:
    raw = props.get("loom_version")
    if not raw:
        match = re.search(r"id\s+['\"](?:net\.fabricmc\.)?fabric-loom['\"]\s+version\s+['\"]([^'\"]+)['\"]", build_gradle)
        raw = match.group(1) if match else "?"
    return raw.replace("-SNAPSHOT", "")


def version_line(gradle_properties: str, build_gradle: str, maintenance: bool) -> VersionLine:
    props = parse_properties(gradle_properties)
    return VersionLine(
        mod=props["mod_version"],
        minecraft=props["minecraft_version"],
        java=props["java_version"],
        loader=props["loader_version"],
        fabric_api=props["fabric_version"],
        loom=loom_version(props, build_gradle),
        yarn=props.get("yarn_mappings"),
        maintenance=maintenance,
    )


def git(*args: str) -> str:
    return subprocess.run(["git", *args], cwd=ROOT, check=True, capture_output=True, text=True).stdout


def load_lines() -> list:
    lines = [version_line((ROOT / "gradle.properties").read_text(), (ROOT / "build.gradle").read_text(), False)]
    if MAINTENANCE_TAG_GLOB:
        tags = git("tag", "--list", MAINTENANCE_TAG_GLOB, "--sort=-v:refname").split()
        if not tags:
            sys.exit(f"No tag matches MAINTENANCE_TAG_GLOB={MAINTENANCE_TAG_GLOB!r} (are tags fetched?)")
        tag = tags[0]
        lines.append(version_line(git("show", f"{tag}:gradle.properties"), git("show", f"{tag}:build.gradle"), True))
    return lines


# ── Content generators ───────────────────────────────────────────────────────

def shield(text: str) -> str:
    """Escapes a static shields.io badge segment."""
    return text.replace("-", "--").replace("_", "__").replace(" ", "_")


def joined(lines: list, attr: str, prefix: str = "") -> str:
    return "_%7C_".join(shield(prefix + getattr(line, attr)) for line in lines)


def badges(lines: list) -> list:
    """(alt, image url, link or None) for every header badge."""
    cur = lines[0]
    return [
        ("Version", f"https://img.shields.io/badge/version-{shield(cur.mod)}-d2962a", None),
        ("Minecraft", f"https://img.shields.io/badge/minecraft-{joined(lines, 'minecraft')}-62b47a?logo=minecraft&logoColor=white", None),
        ("Fabric", f"https://img.shields.io/badge/fabric_loader-{joined(lines, 'loader', '≥')}-dbd0b4", None),
        ("Java", f"https://img.shields.io/badge/java-{joined(lines, 'java')}-ED8B00?logo=openjdk&logoColor=white", None),
        ("License", "https://img.shields.io/badge/license-CC0--1.0-lightgrey", None),
        ("CurseForge", f"https://img.shields.io/curseforge/dt/{CURSEFORGE_ID}?logo=curseforge&label=curseforge&color=F16436", CURSEFORGE_URL),
        ("Modrinth", f"https://img.shields.io/modrinth/dt/{MODRINTH_SLUG}?logo=modrinth&label=modrinth&color=00AF5C", MODRINTH_URL),
    ]


def md_badges(lines: list) -> str:
    out = []
    for alt, img, link in badges(lines):
        out.append(f"[![{alt}]({img})]({link})" if link else f"![{alt}]({img})")
    return "\n".join(out)


def html_badges(lines: list) -> str:
    out = ['<p class="badges">']
    for alt, img, link in badges(lines):
        tag = f'<img src="{img.replace("&", "&amp;")}" alt="{alt}">'
        out.append(f'  <a href="{link}">{tag}</a>' if link else f"  {tag}")
    out.append("</p>")
    return "\n".join(out)


def md_version_table(lines: list) -> str:
    rows = ["| Mod version | Minecraft | Status |", "|-------------|-----------|--------|"]
    rows += [f"| **{l.series}** | {l.minecraft} | {l.status} |" for l in lines]
    return "\n".join(rows)


def md_installation(lines: list) -> str:
    out = []
    for l in lines:
        suffix = " (maintenance)" if l.maintenance else ""
        out += [
            f"### Minecraft {l.minecraft} — mod v{l.series}{suffix}",
            "",
            f"1. Make sure Minecraft runs with **Java {l.java}**",
            f"2. Install [Fabric Loader](https://fabricmc.net/use/) **≥ {l.loader}** for Minecraft {l.minecraft}",
            f"3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for {l.minecraft} and place it in the `mods/` folder",
            f"4. Download the Enchant Transfer **{l.series}** JAR and place it in `mods/` as well",
            "5. Launch Minecraft with the Fabric profile",
            "",
        ]
    out.append("> **Fabric API is required.** The mod will not start without it. "
               "Make sure the mod JAR, Fabric API, and Fabric Loader all match your Minecraft version.")
    return "\n".join(out)


def md_requirements(lines: list) -> str:
    header = "| Dependency | " + " | ".join(f"Minecraft {l.minecraft} (v{l.series})" for l in lines) + " |"
    sep = "|------------|" + "|".join("-" * (len(f" Minecraft {l.minecraft} (v{l.series}) ")) for l in lines) + "|"
    rows = [
        "| Java | " + " | ".join(l.java for l in lines) + " |",
        "| Fabric Loader | " + " | ".join(f"≥ {l.loader}" for l in lines) + " |",
        "| Fabric API | " + " | ".join(l.fabric_api for l in lines) + " |",
    ]
    return "\n".join([header, sep, *rows])


def md_gradle_properties(lines: list) -> str:
    cur = lines[0]
    # Keep the dependency sections of gradle.properties verbatim, dropping JVM
    # args and the mod metadata (version/group/archive name).
    blocks = re.split(r"\n\s*\n", (ROOT / "gradle.properties").read_text().strip())
    kept = [b for b in blocks if "org.gradle.jvmargs" not in b and "mod_version" not in b]
    out = [
        f"All versions are centralized in `gradle.properties` (current values on `master`, Minecraft {cur.minecraft}):",
        "",
        "```properties",
        "\n\n".join(kept),
        "```",
        "",
    ]
    notes = []
    if not cur.yarn:
        notes.append(f"Minecraft {cur.minecraft} uses the official **Mojang mappings**, so there is no `yarn_mappings` property.")
    for l in lines[1:]:
        mappings = f"Yarn (`yarn_mappings={l.yarn}`)" if l.yarn else "the official Mojang mappings"
        notes.append(f"The {l.minecraft} version (tag `v{l.mod}`) uses {mappings}, Fabric Loom {l.loom}, and Java {l.java}.")
    if notes:
        out.append("> " + " ".join(notes))
    return "\n".join(out)


def html_requires(lines: list) -> str:
    versions = " | ".join(l.minecraft for l in lines)
    return f'<p class="requires"><strong>Requires:</strong> Fabric Loader and Fabric API · Minecraft {versions}</p>'


def html_installation(lines: list) -> str:
    out = [
        '<div class="card-table-wrap">',
        '  <table class="card-table">',
        "    <thead>",
        "      <tr>",
        *(f"        <th>{h}</th>" for h in ("Mod version", "Minecraft", "Java", "Fabric Loader", "Fabric API", "Status")),
        "      </tr>",
        "    </thead>",
        "    <tbody>",
    ]
    for l in lines:
        out += [
            "      <tr>",
            f"        <td><strong>{l.series}</strong></td>",
            f"        <td>{l.minecraft}</td>",
            f"        <td>{l.java}</td>",
            f"        <td>≥ {l.loader}</td>",
            f"        <td>{l.fabric_api}</td>",
            f"        <td>{l.status}</td>",
            "      </tr>",
        ]
    files = ", ".join(f"{l.series} for {l.minecraft}" for l in lines)
    out += [
        "    </tbody>",
        "  </table>",
        "</div>",
        "",
        "<ol>",
        "  <li>Make sure Minecraft runs with the Java version listed for your Minecraft version</li>",
        '  <li>Install <a href="https://fabricmc.net/use/">Fabric Loader</a> for your Minecraft version</li>',
        '  <li>Download <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> for the same Minecraft version and place it in the <code>mods/</code> folder</li>',
        f"  <li>Download the matching Enchant Transfer file ({files}) and place it in <code>mods/</code> as well</li>",
        "  <li>Launch Minecraft with the Fabric profile</li>",
        "</ol>",
    ]
    return "\n".join(out)


def curseforge_versions_table(lines: list) -> str:
    rows = "\n".join(
        f"<tr><td><b>{l.series}</b></td><td>{l.minecraft}</td><td>{l.java}</td>"
        f"<td>≥ {l.loader}</td><td>{l.fabric_api}</td><td>{l.status}</td></tr>"
        for l in lines
    )
    return (
        "<table>\n<thead>\n"
        "<tr><th>Mod version</th><th>Minecraft</th><th>Java</th><th>Fabric Loader</th><th>Fabric API</th><th>Status</th></tr>\n"
        "</thead>\n<tbody>\n" + rows + "\n</tbody>\n</table>"
    )


# ── File rewriting ───────────────────────────────────────────────────────────

def replace_block(text: str, name: str, content: str, path: Path) -> str:
    pattern = re.compile(rf"(?P<indent>[ \t]*)(?P<open><!-- AUTO:{name} -->)\n.*?\n[ \t]*(?P<close><!-- /AUTO:{name} -->)", re.S)
    match = pattern.search(text)
    if not match:
        sys.exit(f"{path.relative_to(ROOT)}: missing <!-- AUTO:{name} --> block")
    indent = match.group("indent")
    body = "\n".join(indent + l if l else l for l in content.splitlines())
    replacement = f"{indent}{match.group('open')}\n{body}\n{indent}{match.group('close')}"
    return text[:match.start()] + replacement + text[match.end():]


def replace_regex(text: str, pattern: str, repl: str, path: Path) -> str:
    new, count = re.subn(pattern, repl, text)
    if count == 0:
        sys.exit(f"{path.relative_to(ROOT)}: pattern not found: {pattern}")
    return new


def render(lines: list) -> dict:
    cur = lines[0]
    mc_list = " and ".join(l.minecraft for l in lines)

    readme = README.read_text()
    readme = replace_block(readme, "badges", md_badges(lines), README)
    readme = replace_block(readme, "version-table", md_version_table(lines), README)
    readme = replace_block(readme, "installation", md_installation(lines), README)
    readme = replace_block(readme, "requirements", md_requirements(lines), README)
    readme = replace_block(readme, "gradle-properties", md_gradle_properties(lines), README)

    index = INDEX.read_text()
    index = replace_block(index, "badges", html_badges(lines), INDEX)
    index = replace_block(index, "requires", html_requires(lines), INDEX)
    index = replace_block(index, "installation", html_installation(lines), INDEX)
    index = replace_regex(index, r'"softwareVersion": "[^"]*"', f'"softwareVersion": "{cur.mod}"', INDEX)
    index = replace_regex(index, r"Fabric mod for Minecraft \d+(?:\.\d+)+(?: and \d+(?:\.\d+)+)*", f"Fabric mod for Minecraft {mc_list}", INDEX)

    curseforge = CURSEFORGE.read_text()
    curseforge = replace_block(curseforge, "versions-table", curseforge_versions_table(lines), CURSEFORGE)

    return {README: readme, INDEX: index, CURSEFORGE: curseforge}


def main() -> None:
    check = "--check" in sys.argv[1:]
    lines = load_lines()
    outputs = render(lines)

    changed = [path for path, text in outputs.items() if path.read_text() != text]
    if check:
        # The Modrinth description derives from README.md, so it can only be
        # compared once the README itself is up to date.
        if README not in changed:
            modrinth = ROOT / "docs" / "modrinth-description.md"
            with tempfile.NamedTemporaryFile(suffix=".md") as expected:
                subprocess.run([str(MODRINTH_SCRIPT), expected.name], check=True, capture_output=True)
                if not modrinth.exists() or Path(expected.name).read_text() != modrinth.read_text():
                    changed.append(modrinth)
        for path in changed:
            print(f"out of date: {path.relative_to(ROOT)}")
        sys.exit(1 if changed else 0)

    for path, text in outputs.items():
        path.write_text(text)
    subprocess.run([str(MODRINTH_SCRIPT)], check=True)

    summary = ", ".join(f"{l.series} → MC {l.minecraft}" for l in lines)
    print(f"Synced docs for {summary}")
    if f"## Changelog — {lines[0].mod}" not in outputs[README]:
        print(f"::warning::README.md has no '## Changelog — {lines[0].mod}' section; write it by hand.")


if __name__ == "__main__":
    main()
