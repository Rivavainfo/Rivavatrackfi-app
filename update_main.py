import re

with open('app/src/main/java/com/trackfi/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace padding values being passed to NavHost to remove extra space.
# We modify NavHost's modifier to ignore bottom padding from the Scaffold.
content = re.sub(
    r'NavHost\(\s*navController = navController,\s*startDestination = if \(hasCompletedOnboarding\) Screen\.Home\.route else Screen\.Welcome\.route,\s*modifier = Modifier\.padding\(paddingValues\),',
    r'NavHost(\n            navController = navController,\n            startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Welcome.route,\n            modifier = Modifier.padding(bottom = 0.dp), // Removed scaffold padding to fix black space',
    content
)

with open('app/src/main/java/com/trackfi/MainActivity.kt', 'w') as f:
    f.write(content)
