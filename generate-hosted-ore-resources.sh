#!/usr/bin/env bash
# Generate model/state/tag/loot data for the canonical host-aware ore blocks.
# The block ID identifies the mineral; the HOST state selects the GeoStrata
# stone model, keeping a tin or zinc vein visually continuous with its layer.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
res="$root/src/main/resources"
materials=(coal iron copper gold diamond lapis emerald redstone tin lead zinc nickel lithium uranium osmium fluorite silver sulfur saltpeter galena bauxite lignite)
rocks=(amphibolite andesite basalt basaltic_glass conglomerate diabase diorite dolomite gabbro gneiss granite hornfels kimberlite limestone marble novaculite pegmatite peridotite phyllite quartzite rhyolite rock_salt schist scoria shale siltstone slate tuff)

for material in "${materials[@]}"; do
  blockstate="$res/assets/realgeology/blockstates/${material}_ore.json"
  mkdir -p "$(dirname "$blockstate")" "$res/assets/realgeology/models/block/${material}_ore"
  printf '{\n  "variants": {\n' > "$blockstate"
  for i in "${!rocks[@]}"; do
    rock=${rocks[$i]}
    comma=,
    [[ $i -eq $((${#rocks[@]} - 1)) ]] && comma=''
    printf '    "host=%s": {"model":"realgeology:block/%s_ore/%s"}%s\n' "$rock" "$material" "$rock" "$comma" >> "$blockstate"
    stone="geostrata:block/$rock"
    [[ "$rock" == kimberlite ]] && stone="realgeology:block/kimberlite"
    printf '{"parent":"geostrata:block/ore_block","textures":{"ore":"realgeology:block/%s_ore_overlay","stone":"%s"}}\n' \
      "$material" "$stone" > "$res/assets/realgeology/models/block/${material}_ore/${rock}.json"
  done
  printf '  }\n}\n' >> "$blockstate"

  # Keep the canonical worldgen block discoverable to all mod recipes that
  # use the modern c: material convention.
  for kind in blocks items; do
    tag="$res/data/c/tags/$kind/ores/${material}.json"
    mkdir -p "$(dirname "$tag")"
    printf '{"replace":false,"values":["realgeology:%s_ore"]}\n' "$material" > "$tag"
  done
  loot="$res/data/realgeology/loot_tables/blocks/${material}_ore.json"
  mkdir -p "$(dirname "$loot")"
  printf '{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"realgeology:%s_ore"}]}]}\n' "$material" > "$loot"
done

printf 'Generated %d host-aware ore blockstate families across %d rocks.\n' "${#materials[@]}" "${#rocks[@]}"
