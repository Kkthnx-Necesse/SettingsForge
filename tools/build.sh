#!/usr/bin/env bash
# Builds the mod jar without Gradle, for shells where Gradle cannot start.
# Mirrors what SettingsForge/build.gradle does. Run from anywhere: tools/build.sh [install]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd -W)"
PROJ="$ROOT/SettingsForge"
GRADLE="$PROJ/build.gradle"
GAME="${NECESSE_DIR:-D:/SteamLibrary/steamapps/common/Necesse}"
MODS_DIR="${NECESSE_MODS:-$(cygpath -m "$APPDATA")/Necesse/mods}"

prop() { sed -n "s/^project.ext.$1 = \"\(.*\)\".*/\1/p" "$GRADLE" | head -1; }
ID="$(prop modID)"; NAME="$(prop modName)"; VERSION="$(prop modVersion)"
GAMEVER="$(prop gameVersion)"; DESC="$(prop modDescription)"; AUTHOR="$(prop author)"
JAR="${NAME// /}-$GAMEVER-$VERSION.jar"

OUT="$PROJ/build/mod"
TOOLS="$PROJ/build/tools"
rm -rf "$PROJ/build"
mkdir -p "$OUT" "$TOOLS" "$PROJ/build/jar"

CP="$GAME/Necesse.jar;$GAME/lib/*"
echo "compile"
javac -encoding UTF-8 --release 8 -cp "$CP" -d "$OUT" $(find "$PROJ/src/main/java" -name '*.java')

echo "resources"
mkdir -p "$OUT/resources"
cp -r "$PROJ/src/main/resources/." "$OUT/resources/"

echo "mod.info"
java -cp "$GAME/Necesse.jar" CreateModInfoFile -file "$OUT/mod.info" -id "$ID" -name "$NAME" \
    -version "$VERSION" -gameVersion "$GAMEVER" -description "$DESC" -author "$AUTHOR" \
    -clientside true -dependencies "" -optionalDependencies ""

echo "weave check"
javac -encoding UTF-8 -cp "$CP;$OUT" -d "$TOOLS" "$ROOT/tools/WeaveCheck.java" "$ROOT/tools/OptionsCheck.java"
PATCHES=$(cd "$OUT" && find . -path '*patches*' -name '*.class' | sed 's#^\./##; s#\.class$##; s#/#.#g')
java -Xverify:all -Djava.awt.headless=true -cp "$CP;$OUT;$TOOLS" WeaveCheck "$GAME/Necesse.jar" $PATCHES
java -Djava.awt.headless=true -cp "$CP;$OUT;$TOOLS" OptionsCheck > /dev/null

echo "jar"
jar cf "$PROJ/build/jar/$JAR" -C "$OUT" .
echo "built $PROJ/build/jar/$JAR"

if [ "${1:-}" = "install" ]; then
    mkdir -p "$MODS_DIR"
    cp "$PROJ/build/jar/$JAR" "$MODS_DIR/$JAR"
    echo "installed to $MODS_DIR/$JAR"
fi
