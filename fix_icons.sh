#!/bin/bash

FILES=$(grep -rl "Icons.AutoMirrored" app/src/main/java/com/rivavafi/)

for file in $FILES; do
    if ! grep -q "import androidx.compose.material.icons.automirrored" "$file"; then
        sed -i '/import androidx.compose.material.icons.Icons/a import androidx.compose.material.icons.automirrored.filled.*' "$file"
        sed -i '/import androidx.compose.material.icons.Icons/a import androidx.compose.material.icons.automirrored.outlined.*' "$file"
    fi
done

echo "Done"
