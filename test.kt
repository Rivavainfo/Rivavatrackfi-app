import java.util.regex.Pattern

fun main() {
    val text = "some text here coil.compose.AsyncImage something ContentScale.Fit other text"
    val newText = text.replace(Regex("coil\\.compose\\.AsyncImage\\([\\s\\S]*?contentScale = androidx\\.compose\\.ui\\.layout\\.ContentScale\\.Fit,[\\s\\S]*?\\)"), "REPLACED")
    println(newText)
}
