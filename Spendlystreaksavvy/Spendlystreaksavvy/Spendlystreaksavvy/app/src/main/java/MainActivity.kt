package com.example.spendly

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.spendly.navigation.AppNavGraph
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.viewmodel.AppViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendlyTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    val appViewModel: AppViewModel = viewModel()

                    AppNavGraph(navController = navController, viewModel = appViewModel)
                }
            }
        }

        testFirebaseConnection()
    }
}

fun testFirebaseConnection() {
    val db = Firebase.firestore

    val testData = hashMapOf(
        "name" to "Test User",
        "message" to "Firebase is working!"
    )

    db.collection("testCollection")
        .add(testData)
        .addOnSuccessListener {
            Log.d("FIREBASE_TEST", "Document added with ID: ${it.id}")
        }
        .addOnFailureListener {
            Log.e("FIREBASE_TEST", "Error adding document", it)
        }
}
