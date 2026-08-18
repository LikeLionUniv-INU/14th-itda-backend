#!/bin/bash
# ============================================
# Upload placeholder wireframe images to MinIO
# Run after docker-compose is up (MinIO on localhost:9000)
# Usage: bash scripts/upload-dummy-images.sh
# ============================================

MINIO_URL="${MINIO_URL:-http://localhost:9000}"
MINIO_USER="${MINIO_USER:-minioadmin}"
MINIO_PASS="${MINIO_PASS:-minioadmin}"
BUCKET="itda"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}=== MinIO Dummy Image Uploader ===${NC}"

# Check if MinIO is reachable
if ! curl -s --connect-timeout 3 "$MINIO_URL/minio/health/live" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: MinIO is not reachable at $MINIO_URL${NC}"
    echo "Make sure docker-compose is running: docker compose up -d"
    exit 1
fi

# Install mc (MinIO Client) if not available
if ! command -v mc &> /dev/null; then
    echo -e "${YELLOW}Installing MinIO Client (mc)...${NC}"
    curl -sSL https://dl.min.io/client/mc/release/linux-amd64/mc -o /tmp/mc
    chmod +x /tmp/mc
    MC="/tmp/mc"
else
    MC="mc"
fi

# Configure mc alias
$MC alias set local "$MINIO_URL" "$MINIO_USER" "$MINIO_PASS" --api S3v4 2>/dev/null

# Create bucket if not exists
$MC mb "local/$BUCKET" 2>/dev/null
# Set bucket policy to public read (so image URLs work without auth)
$MC anonymous set download "local/$BUCKET" 2>/dev/null

TMPDIR=$(mktemp -d)

# Function to generate a simple placeholder PNG with text
generate_placeholder() {
    local filepath="$1"
    local width="$2"
    local height="$3"
    local label="$4"
    local color="$5"

    if command -v python3 &> /dev/null; then
        python3 - "$filepath" "$width" "$height" "$label" "$color" << 'PYEOF'
import sys, struct, zlib

filepath, w, h, label, color = sys.argv[1], int(sys.argv[2]), int(sys.argv[3]), sys.argv[4], sys.argv[5]

# Parse hex color
colors = {
    "blue":   (66, 133, 244),
    "green":  (52, 168, 83),
    "red":    (234, 67, 53),
    "yellow": (251, 188, 4),
    "purple": (142, 68, 173),
    "gray":   (180, 180, 180),
}
r, g, b = colors.get(color, (100, 100, 100))

# Create raw pixel data (simple colored rectangle with border)
raw = b''
for y in range(h):
    raw += b'\x00'  # filter byte
    for x in range(w):
        # Draw a border
        if x < 2 or x >= w-2 or y < 2 or y >= h-2:
            raw += bytes([80, 80, 80])
        # Draw a center cross/marker
        elif abs(x - w//2) < 20 and abs(y - h//2) < 2:
            raw += bytes([255, 255, 255])
        elif abs(y - h//2) < 20 and abs(x - w//2) < 2:
            raw += bytes([255, 255, 255])
        else:
            # Slight gradient for visual interest
            shade = int((y / h) * 40)
            raw += bytes([max(0, r - shade), max(0, g - shade), max(0, b - shade)])

compressed = zlib.compress(raw)

def chunk(ctype, data):
    c = ctype + data
    return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)

with open(filepath, 'wb') as f:
    f.write(b'\x89PNG\r\n\x1a\n')
    f.write(chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)))
    f.write(chunk(b'IDAT', compressed))
    f.write(chunk(b'IEND', b''))

print(f"  Generated: {w}x{h} {color} PNG")
PYEOF
    else
        # Fallback: create a minimal 1x1 PNG and skip fancy generation
        printf '\x89PNG\r\n\x1a\n' > "$filepath"
        echo -e "  ${YELLOW}(python3 not found, using minimal PNG)${NC}"
    fi
}

echo ""
echo -e "${GREEN}Generating placeholder wireframe images...${NC}"

# Image definitions: s3_key width height label color
declare -a IMAGES=(
    # Project 101, Doc 101 (메인화면), version 102 pages
    "wireframes/101/101/104/original_1723900000000.png 960 540 Home blue"
    "wireframes/101/101/105/original_1723900001000.png 960 540 Search green"
    "wireframes/101/101/106/original_1723900002000.png 960 540 MyPage purple"
    "wireframes/101/101/107/original_1723900003000.png 960 540 Notifications yellow"
    # Project 101, Doc 102 (로그인), version 104 pages
    "wireframes/101/102/112/original_1723900004000.png 720 450 Login blue"
    "wireframes/101/102/113/original_1723900005000.png 720 450 Signup green"
    "wireframes/101/102/114/original_1723900006000.png 720 450 ResetPW red"
    "wireframes/101/102/118/original_1723900007000.png 720 450 SocialLogin purple"
    # Project 102, Doc 103 (ProductPage), version 106 pages
    "wireframes/102/103/119/original_1723900008000.png 960 540 ProductList blue"
    "wireframes/102/103/120/original_1723900009000.png 960 540 ProductDetail green"
    "wireframes/102/103/121/original_1723900010000.png 960 540 Cart yellow"
    "wireframes/102/103/125/original_1723900011000.png 960 540 Reviews purple"
    "wireframes/102/103/126/original_1723900012000.png 960 540 Checkout red"
)

UPLOADED=0
FAILED=0

for entry in "${IMAGES[@]}"; do
    read -r s3key width height label color <<< "$entry"
    local_file="$TMPDIR/$(basename "$s3key")"

    echo -e "\n${YELLOW}[$label]${NC} $s3key"
    generate_placeholder "$local_file" "$width" "$height" "$label" "$color"

    if $MC cp "$local_file" "local/$BUCKET/$s3key" > /dev/null 2>&1; then
        echo -e "  ${GREEN}Uploaded to MinIO${NC}"
        ((UPLOADED++))
    else
        echo -e "  ${RED}Upload failed${NC}"
        ((FAILED++))
    fi
done

# Cleanup
rm -rf "$TMPDIR"

echo ""
echo -e "${GREEN}=== Done ===${NC}"
echo -e "  Uploaded: ${GREEN}$UPLOADED${NC}"
echo -e "  Failed:   ${RED}$FAILED${NC}"
echo ""
echo "Images are accessible at: $MINIO_URL/$BUCKET/wireframes/..."
