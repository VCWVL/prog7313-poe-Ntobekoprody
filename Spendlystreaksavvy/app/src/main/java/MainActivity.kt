package com.example.spendly

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.spendly.navigation.AppNavGraph
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appViewModel: AppViewModel = viewModel(
                factory = AppViewModelFactory(application)
            )

            val isDarkMode by appViewModel.isDarkMode

            SpendlyTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        appViewModel.xpEvents.collect { amount ->
                            scope.launch {
                                snackbarHostState.showSnackbar("+$amount XP!")
                            }
                        }
                    }

                    Box {
                        AppNavGraph(navController = navController, viewModel = appViewModel)
                        SnackbarHost(hostState = snackbarHostState)
                    }
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


class AppViewModelFactory(private val application: android.app.Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
