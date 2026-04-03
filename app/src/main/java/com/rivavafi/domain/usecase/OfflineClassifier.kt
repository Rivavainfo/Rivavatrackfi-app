package com.rivavafi.domain.usecase

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class OfflineClassifier @Inject constructor(@ApplicationContext private val context: Context) {
    private var interpreter: Interpreter? = null
    private var vocab: Map<String, Int> = emptyMap()
    private val maxLen = 20

    private val categories = listOf(
        "EXPENSE", "INCOME", "INVESTMENT", "MANDATE", "BILL",
        "SUBSCRIPTION", "VOUCHER", "SELF_TRANSFER", "CREDIT_CARD", "OTP/SPAM"
    )

    init {
        try {
            interpreter = Interpreter(loadModelFile())
            loadVocab()
        } catch (e: Exception) {
            Log.e("OfflineClassifier", "Error initializing classifier", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("sms_classifier.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadVocab() {
        try {
            val jsonString = context.assets.open("sms_vocab.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, Int>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getInt(key)
            }
            vocab = map
        } catch (e: Exception) {
            Log.e("OfflineClassifier", "Error loading vocab", e)
        }
    }

    open fun classify(text: String): Pair<String, Float> {
        if (interpreter == null || vocab.isEmpty()) return Pair("UNKNOWN", 0f)

        try {
            // Preprocess
            val cleanText = text.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
            val words = cleanText.split("\\s+".toRegex()).filter { it.isNotEmpty() }

            // The model was trained with input_dim=len(vocab), max_len=20
            // and sequence of word ids. Wait, the model expects integer indices!
            // But we created it with input_length=20 and input of shape (1, 20) with int32 or float32?
            // The python model uses X = np.array([...]) where items are ints.

            val input = FloatArray(maxLen) { 0f }
            for (i in 0 until minOf(words.size, maxLen)) {
                input[i] = (vocab[words[i]] ?: vocab["<UNK>"] ?: 1).toFloat()
            }

            // We need to pass float array or int array depending on python model.
            // Let's check Python code again. The model takes float32 because we didn't specify dtype.
            // Actually, embeddings in TF default to taking ints or floats.
            val inputArray = arrayOf(input)
            val outputArray = arrayOf(FloatArray(categories.size))

            interpreter?.run(inputArray, outputArray)

            val probabilities = outputArray[0]
            var maxProb = -1f
            var maxIndex = -1

            for (i in probabilities.indices) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i]
                    maxIndex = i
                }
            }

            if (maxIndex != -1) {
                return Pair(categories[maxIndex], maxProb)
            }

        } catch (e: Exception) {
            Log.e("OfflineClassifier", "Classification error", e)
        }

        return Pair("UNKNOWN", 0f)
    }

    fun close() {
        interpreter?.close()
    }
}
