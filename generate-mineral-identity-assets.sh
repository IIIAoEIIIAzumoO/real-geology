#!/usr/bin/env bash
# Generate the repetitive model/state assets for Real Geology's canonical
# mineral blocks.  These blocks use the same rock-host state as the existing
# industrial ores, so one material texture works over every GeoStrata rock.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
geopack="$root/../geostrata-hd-overlay-128x/assets/geostrata/textures/block"
assets="$root/src/main/resources/assets/realgeology"
textures="$assets/textures/block"
models="$assets/models/block"
states="$assets/blockstates"
hosts=(amphibolite andesite basalt basaltic_glass conglomerate diabase diorite dolomite gabbro gneiss granite hornfels kimberlite limestone marble novaculite pegmatite peridotite phyllite quartzite rhyolite rock_salt schist scoria shale siltstone slate tuff)
materials=(coal iron copper gold diamond lapis emerald redstone)
data="$root/src/main/resources/data"

mkdir -p "$textures" "$models" "$states"

for material in "${materials[@]}"; do
  for suffix in '' _n _s; do
    cp "$geopack/${material}_ore_overlay${suffix}.png" "$textures/${material}_ore_overlay${suffix}.png"
  done

  {
    printf '{\n  "variants": {\n'
    first=true
    for host in "${hosts[@]}"; do
      "$first" || printf ',\n'
      first=false
      printf '    "host=%s": {"model":"realgeology:block/%s_ore/%s"}' "$host" "$material" "$host"
    done
    printf '\n  }\n}\n'
  } > "$states/${material}_ore.json"

  mkdir -p "$models/${material}_ore"
  for host in "${hosts[@]}"; do
    stone="geostrata:block/$host"
    [[ "$host" == kimberlite ]] && stone="realgeology:block/kimberlite"
    printf '{"parent":"geostrata:block/ore_block","textures":{"ore":"realgeology:block/%s_ore_overlay","stone":"%s"}}\n' \
      "$material" "$stone" > "$models/${material}_ore/${host}.json"
  done
done

# The block IDs above are the authoritative ore blocks, but industrial mods
# talk in material tags.  Add each canonical block to both block and item c:
# tags so processing recipes remain compatible with vanilla and mod machinery.
for material in "${materials[@]}"; do
  mkdir -p "$data/c/tags/blocks/ores" "$data/c/tags/items/ores" "$data/realgeology/loot_tables/blocks"
  printf '{"replace":false,"values":["realgeology:%s_ore"]}\n' "$material" > "$data/c/tags/blocks/ores/$material.json"
  printf '{"replace":false,"values":["realgeology:%s_ore"]}\n' "$material" > "$data/c/tags/items/ores/$material.json"
  printf '{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"realgeology:%s_ore"}]}]}\n' \
    "$material" > "$data/realgeology/loot_tables/blocks/${material}_ore.json"
done

# Kimberlite is a dark ultramafic volcanic host.  It begins with the existing
# high-detail peridotite base and is muted/blue-green so it does not masquerade
# as ordinary basalt or stone.  Matching PBR maps preserve the shader response.
convert "$geopack/peridotite.png" -modulate 78,88,92 -colorspace sRGB "$textures/kimberlite.png"
cp "$geopack/peridotite_n.png" "$textures/kimberlite_n.png"
cp "$geopack/peridotite_s.png" "$textures/kimberlite_s.png"
printf '{"variants":{"":{"model":"realgeology:block/kimberlite"}}}\n' > "$states/kimberlite.json"
printf '{"parent":"minecraft:block/cube_all","textures":{"all":"realgeology:block/kimberlite"}}\n' > "$models/kimberlite.json"
mkdir -p "$data/realgeology/loot_tables/blocks"
printf '{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"realgeology:kimberlite"}]}]}\n' > "$data/realgeology/loot_tables/blocks/kimberlite.json"

printf 'Generated mineral-identity assets for %s hosted minerals plus kimberlite.\n' "${#materials[@]}"
