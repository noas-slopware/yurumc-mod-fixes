#!/usr/bin/env bash
# Build one of the fix mods: ./build.sh meadowfix|mtrfix
# Compiles against the Minecraft 1.21.1 (mojmap) jars, MTR and Mixin/MixinExtras from the local Prism install.
set -euo pipefail

name=${1:?usage: build.sh meadowfix|mtrfix}
here=$(cd "$(dirname "$0")" && pwd)
dir=$here/$name
prism=${PRISM:-$HOME/.local/share/PrismLauncher}
instance=${INSTANCE:-$prism/instances/1.21.1/minecraft}
java_bin=${JAVA_BIN:-$HOME/.sdkman/candidates/java/25-graal/bin}
libs=$prism/libraries

version=$(sed -n 's/^version = "\(.*\)"/\1/p' "$dir/res/META-INF/neoforge.mods.toml")
case $name in
	meadowfix) jar_name=yurumc-meadowfix-$version.jar ;;
	mtrfix) jar_name=yurumc-mtrfix-$version.jar ;;
	*) echo "unknown fix: $name" >&2; exit 1 ;;
esac

work=$(mktemp -d)
trap 'rm -rf -- "$work"' EXIT

# Minecraft client (mojmap) + its libraries, from Prism's metadata for 1.21.1
classpath=$(python3 - "$prism" <<'EOF'
import json, os, sys
prism = sys.argv[1]
libs = os.path.join(prism, 'libraries')
meta = json.load(open(os.path.join(prism, 'meta/net.minecraft/1.21.1.json')))
cp = [os.path.join(libs, 'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar')]
for lib in meta['libraries']:
    if 'natives' in lib['name'] or 'rules' in lib:
        continue
    group, artifact, ver = lib['name'].split(':')[:3]
    path = os.path.join(libs, group.replace('.', '/'), artifact, ver, f'{artifact}-{ver}.jar')
    if os.path.exists(path):
        cp.append(path)
print(':'.join(cp))
EOF
)

mixin_jar=$libs/net/fabricmc/sponge-mixin/0.15.5+mixin.0.8.7/sponge-mixin-0.15.5+mixin.0.8.7.jar
classpath=$classpath:$mixin_jar

if [[ $name == mtrfix ]]; then
	mtr_jar=$(ls "$instance"/mods/MTR-neoforge-4.1.0-beta.2+1.21.1.jar)
	# MixinExtras is jar-in-jar inside NeoForge
	(cd "$work" && "$java_bin/jar" xf "$libs/net/neoforged/neoforge/21.1.262/neoforge-21.1.262-universal.jar" META-INF/jarjar/mixinextras-neoforge-0.5.5.jar)
	classpath=$classpath:$mtr_jar:$work/META-INF/jarjar/mixinextras-neoforge-0.5.5.jar
fi

mkdir -p "$work/out" "$here/build"
"$java_bin/javac" --release 21 -nowarn -cp "$classpath" -d "$work/out" $(find "$dir/src" -name '*.java')
cp -r "$dir/res/." "$work/out/"
"$java_bin/jar" --create --file "$here/build/$jar_name" -C "$work/out" .
echo "built $here/build/$jar_name"
