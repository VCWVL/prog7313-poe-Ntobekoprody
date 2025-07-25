package com.example.spendly.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthUtil {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, callback: (Boolean, FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, auth.currentUser)
                } else {
                    callback(false, null)
                }
            }
    }

    fun login(email: String, password: String, callback: (Boolean, FirebaseUser?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, auth.currentUser)
                } else {
                    callback(false, null)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
