#!/bin/bash
find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/import androidx.compose.material.icons.automirrored.filled.*//g' \
  -e 's/import androidx.compose.material.icons.automirrored.outlined.*//g' {} +
