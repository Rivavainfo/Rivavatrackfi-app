#!/bin/bash
find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/Icons.AutoMirrored.Filled/Icons.Default/g' \
  -e 's/Icons.AutoMirrored.Outlined/Icons.Outlined/g' {} +

find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/import androidx.compose.material.icons.automirrored.*//g' {} +
