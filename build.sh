#!/usr/bin/env bash
# Build one of the fix mods: ./build.sh meadowfix|mtrfix|airwavesfix|paladinfix|hypertubefix
# Compiles against the Minecraft 1.21.1 (mojmap) jars, NeoForge, the target mods and Mixin/MixinExtras from the
# local Prism install.
set -euo pipefail

name=${1:?usage: build.sh meadowfix|mtrfix|airwavesfix|paladinfix|hypertubefix}
here=$(cd "$(dirname "$0")" && pwd)
dir=$here/$name
prism=${PRISM:-$HOME/.local/share/PrismLauncher}
instance=${INSTANCE:-$prism/instances/New YuruMC Modded/minecraft}
java_bin=${JAVA_BIN:-$HOME/.sdkman/candidates/java/25-graal/bin}
libs=$prism/libraries
neoforge=$libs/net/neoforged/neoforge/21.1.262

version=$(sed -n 's/^version = "\(.*\)"/\1/p' "$dir/res/META-INF/neoforge.mods.toml")
case $name in
	meadowfix) jar_name=yurumc-meadowfix-$version.jar ;;
	mtrfix) jar_name=yurumc-mtrfix-$version.jar ;;
	airwavesfix) jar_name=yurumc-airwavesfix-$version.jar ;;
	paladinfix) jar_name=yurumc-paladinfix-$version.jar ;;
	hypertubefix) jar_name=yurumc-hypertubefix-$version.jar ;;
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

# MixinExtras is jar-in-jar inside NeoForge
mixinextras() {
	(cd "$work" && "$java_bin/jar" xf "$neoforge/neoforge-21.1.262-universal.jar" META-INF/jarjar/mixinextras-neoforge-0.5.5.jar)
	echo "$work/META-INF/jarjar/mixinextras-neoforge-0.5.5.jar"
}

if [[ $name == mtrfix ]]; then
	mtr_jar=$(ls "$instance"/mods/MTR-neoforge-4.1.0-beta.2+1.21.1.jar)
	classpath=$classpath:$mtr_jar:$(mixinextras)
fi

# NeoForge itself, with its patched Minecraft classes ahead of the vanilla ones
neoforge_cp() {
	local neo_libs
	neo_libs=$(python3 - "$prism" <<'EOF'
import json, os, sys
prism = sys.argv[1]
libs = os.path.join(prism, 'libraries')
meta = json.load(open(os.path.join(prism, 'meta/net.neoforged/21.1.262.json')))
cp = []
for lib in meta['libraries']:
    group, artifact, ver, *classifier = lib['name'].split(':')
    suffix = f'-{classifier[0]}' if classifier else ''
    path = os.path.join(libs, group.replace('.', '/'), artifact, ver, f'{artifact}-{ver}{suffix}.jar')
    if os.path.exists(path):
        cp.append(path)
print(':'.join(cp))
EOF
)
	echo "$neoforge/neoforge-21.1.262-client.jar:$classpath:$neoforge/neoforge-21.1.262-universal.jar:$neo_libs"
}

if [[ $name == airwavesfix ]]; then
	# A real (javafml) mod
	mods=$instance/mods
	airwaves_jar=$mods/airwaves-1.21.1-0.1.1-atm10.jar
	create_jar=$mods/create-1.21.1-6.0.10.jar
	etched_jar=$mods/etched-5.1.0.jar
	(cd "$work" && "$java_bin/jar" xf "$create_jar" META-INF/jarjar && mv META-INF/jarjar create-jarjar)
	(cd "$work" && "$java_bin/jar" xf "$airwaves_jar" META-INF/jarjar && mv META-INF/jarjar airwaves-jarjar)
	classpath=$(neoforge_cp)
	classpath=$classpath:$(mixinextras):$airwaves_jar:$create_jar:$etched_jar
	classpath=$classpath:$(ls "$work"/create-jarjar/*.jar "$work"/airwaves-jarjar/*.jar | paste -sd:)
fi

if [[ $name == paladinfix ]]; then
	# Paladin's Furniture plus Forgified Fabric API's renderer API module (jar-in-jar inside FFAPI)
	mods=$instance/mods
	pfm_jar=$(ls "$mods"/paladin-furniture-mod-*-neoforge-mc1.21.1.jar)
	ffapi_jar=$(ls "$mods"/forgified-fabric-api-*.jar)
	(cd "$work" && "$java_bin/jar" xf "$ffapi_jar" META-INF/jars && mv META-INF/jars ffapi-jars)
	classpath=$(neoforge_cp):$pfm_jar:$(ls "$work"/ffapi-jars/fabric-renderer-api-v1-*.jar)
fi

if [[ $name == hypertubefix ]]; then
	# Create: Hypertube, plus Create (its blocks extend Create's)
	mods=$instance/mods
	classpath=$(neoforge_cp):$(ls "$mods"/create_hypertube-*-NEOFORGE.jar):$mods/create-1.21.1-6.0.10.jar
fi

mkdir -p "$work/out" "$here/build"
"$java_bin/javac" --release 21 -nowarn -cp "$classpath" -d "$work/out" $(find "$dir/src" -name '*.java')
cp -r "$dir/res/." "$work/out/"
"$java_bin/jar" --create --file "$here/build/$jar_name" -C "$work/out" .
echo "built $here/build/$jar_name"
