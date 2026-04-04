#!/bin/bash

# A memory guideline states: "Avoid assuming all directional or system icons have AutoMirrored variants in the current Compose version... use Icons.Default.ReceiptLong instead of Icons.AutoMirrored.Filled.ReceiptLong to prevent build failures."
# This matches the compile errors! Jetpack Compose currently does not have AutoMirrored variants for all these icons.
# We will revert them to Icons.Default.<Name> or Icons.Outlined.<Name>.

find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/Icons.AutoMirrored.Filled.Sort/Icons.Default.Sort/g' \
  -e 's/Icons.AutoMirrored.Filled.ReceiptLong/Icons.Default.ReceiptLong/g' \
  -e 's/Icons.AutoMirrored.Outlined.ArrowForward/Icons.Outlined.ArrowForward/g' \
  -e 's/Icons.AutoMirrored.Filled.ArrowForwardIos/Icons.Default.ArrowForwardIos/g' \
  -e 's/Icons.AutoMirrored.Filled.OpenInNew/Icons.Default.OpenInNew/g' \
  -e 's/Icons.AutoMirrored.Filled.TrendingUp/Icons.Default.TrendingUp/g' \
  -e 's/Icons.AutoMirrored.Filled.HelpOutline/Icons.Default.HelpOutline/g' \
  -e 's/Icons.AutoMirrored.Filled.Logout/Icons.Default.Logout/g' {} +
