#!/bin/bash
find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/Icons.AutoMirrored.Filled.TrendingUp/Icons.Default.TrendingUp/g' \
  -e 's/Icons.AutoMirrored.Filled.OpenInNew/Icons.Default.OpenInNew/g' {} +
