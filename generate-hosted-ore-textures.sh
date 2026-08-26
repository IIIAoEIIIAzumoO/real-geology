#!/usr/bin/env bash
# Build mineral-only overlays for Real Geology's host-aware ore states.  The
# model supplies the GeoStrata host separately, so these textures never bake a
# grey/deepslate background into lead, tin, zinc or nickel.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
industrial="$root/../modern-industry-materials-hd-128x/sources"
create="$root/../create-tfmg-geology-hd-128x/sources"
out="$root/src/main/resources/assets/realgeology/textures/block"
work=$(mktemp -d)
trap 'rm -rf -- "$work"' EXIT
mkdir -p "$out"

source_for() {
  case "$1" in
    tin) echo "$industrial/cassiterite-tin.png" ;;
    lead|galena|silver) echo "$industrial/galena-lead-v2.png" ;;
    zinc) echo "$industrial/sphalerite-zinc.png" ;;
    nickel) echo "$industrial/pentlandite-nickel.png" ;;
    lithium|saltpeter) echo "$industrial/spodumene-lithium.png" ;;
    uranium) echo "$industrial/uraninite-uranium-v2.png" ;;
    osmium) echo "$industrial/iridosmine-osmium.png" ;;
    fluorite) echo "$industrial/fluorite.png" ;;
    sulfur) echo "$industrial/native-sulfur.png" ;;
    bauxite) echo "$industrial/bauxite.png" ;;
    lignite) echo "$create/lignite.png" ;;
  esac
}

# Each mask selects the real mineral colour from its macro source and leaves
# the surrounding source rock transparent. The masks are intentionally sparse
# and irregular: deposits should be embedded grains/veins, not a centre dot.
mask_for() {
  case "$1" in
    tin)       echo '((r<0.24)&&(g<0.22)&&(b<0.20)) ? 0.86 : 0' ;;
    lead|galena|silver) echo '((r>0.42)&&(b>r*1.035)&&(b>g*1.025)) ? 0.86 : 0' ;;
    osmium)    echo '((r>0.47)&&(g>0.47)&&(b>0.49)&&((b-r)<0.18)) ? 0.82 : 0' ;;
    zinc)      echo '((r>g*1.08)&&(g>b*1.05)&&(r>0.24)) ? 0.84 : 0' ;;
    nickel)    echo '((r>g*1.06)&&(g>b*1.07)&&(r>0.32)) ? 0.88 : 0' ;;
    lithium|saltpeter) echo '((r>0.62)&&(g>0.58)&&(b>0.52)) ? 0.64 : 0' ;;
    uranium)   echo '((g>r*0.88)&&(g>b*1.08)&&(g<0.62)) ? 0.88 : 0' ;;
    fluorite)  echo '((b>r*1.05)||(r>b*1.16)) ? 0.72 : 0' ;;
    sulfur)    echo '((r>0.52)&&(g>0.40)&&(b<g*0.72)) ? 0.90 : 0' ;;
    bauxite)   echo '((r>g*1.12)&&(g>b*1.05)&&(r>0.36)) ? 0.84 : 0' ;;
    lignite)   echo '((r<0.30)&&(g<0.27)&&(b<0.24)) ? 0.88 : 0' ;;
  esac
}

normal_map() {
  local in=$1 out_file=$2 key=$3
  convert "$in" -alpha extract -blur '0x0.55' "$work/$key-h.png"
  convert "$work/$key-h.png" \( "$work/$key-h.png" -roll '+1+0' \) \( "$work/$key-h.png" -roll '-1+0' \) \
    -fx '0.5+(u[1]-u[2])*1.20' "$work/$key-x.png"
  convert "$work/$key-h.png" \( "$work/$key-h.png" -roll '+0+1' \) \( "$work/$key-h.png" -roll '+0-1' \) \
    -fx '0.5+(u[1]-u[2])*1.20' "$work/$key-y.png"
  convert -size 128x128 xc:'#ffffff' "$work/$key-z.png"
  convert "$work/$key-x.png" "$work/$key-y.png" "$work/$key-z.png" -set colorspace sRGB -combine "$out_file"
}

specular_map() {
  local material=$1 mask=$2 out_file=$3 smooth=155 response=100
  case "$material" in
    tin) smooth=220; response=255 ;; lead|galena) smooth=232; response=235 ;; silver) smooth=240; response=237 ;;
    zinc|nickel|osmium|uranium) smooth=218; response=255 ;; fluorite) smooth=175; response=110 ;;
    sulfur) smooth=125; response=55 ;; bauxite) smooth=135; response=105 ;; lignite) smooth=50; response=10 ;;
  esac
  convert -size 128x128 "xc:rgb($smooth,$response,0)" "$work/$material-spec.png"
  convert "$work/$material-spec.png" "$mask" -alpha off -compose CopyOpacity -composite "$out_file"
}

for material in tin lead zinc nickel lithium uranium osmium fluorite silver sulfur saltpeter galena bauxite lignite; do
  source=$(source_for "$material")
  colour="$work/$material-colour.png" mask="$work/$material-mask.png" target="$out/${material}_ore_overlay.png"
  convert "$source" -crop '1060x1060+95+100' +repage -resize '128x128!' -unsharp '0x0.45' "$colour"
  convert "$colour" -fx "$(mask_for "$material")" -morphology Open Diamond:1 -morphology Dilate Diamond:1 -blur '0x0.12' "$mask"
  convert "$colour" "$mask" -alpha off -compose CopyOpacity -composite "$target"
  normal_map "$target" "${target%.png}_n.png" "$material"
  specular_map "$material" "$mask" "${target%.png}_s.png"
done

printf 'Created host-aware overlays for 14 mineral families.\n'
