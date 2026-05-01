package com.example.peka.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Stan informujący o tym, czy proces logowania jest w toku
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Stan informujący o sukcesie lub błędzie logowania
    private val _authResult = MutableStateFlow<Boolean?>(null)
    val authResult: StateFlow<Boolean?> = _authResult

    fun signInWithGoogleToken(idToken: String) {
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Sprawdzamy, czy to pierwsze logowanie użytkownika (rejestracja)
                    if (authResult.additionalUserInfo?.isNewUser == true) {
                        createNewUserProfile(user.uid, user.email, user.displayName)
                    } else {
                        // Użytkownik już istnieje
                        _isLoading.value = false
                        _authResult.value = true
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Auth", "Błąd logowania: ${e.message}")
                _isLoading.value = false
                _authResult.value = false
            }
    }

    private fun createNewUserProfile(uid: String, email: String?, name: String?) {
        // Tworzymy mapę danych dla nowego profilu
        val userMap = hashMapOf(
            "email" to (email ?: ""),
            "name" to (name ?: "Użytkownik"),
            "favorite_stops" to emptyList<String>() // Pusta tablica na start
        )

        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("Auth", "Profil utworzony pomyślnie")
                _isLoading.value = false
                _authResult.value = true
            }
            .addOnFailureListener {
                Log.e("Auth", "Błąd tworzenia profilu")
                _isLoading.value = false
                _authResult.value = false
            }
    }
}