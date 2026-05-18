# Fix Class.forName in HomeScreen.kt
sed -i 's/Class.forName("com.rivavafi.universal.ui.elite.EliteLandingActivity")/com.rivavafi.universal.ui.elite.EliteLandingActivity::class.java/g' app/src/main/java/com/rivavafi/universal/ui/home/HomeScreen.kt

# Fix string interpolation in EliteLandingActivity.kt
sed -i 's/${"$"}finalUserName/$finalUserName/g' app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt
sed -i 's/${"$"}finalUserEmail/$finalUserEmail/g' app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt
sed -i 's/${"$"}uid/$uid/g' app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt
sed -i 's/${"$"}currentTime/$currentTime/g' app/src/main/java/com/rivavafi/universal/ui/elite/EliteLandingActivity.kt
