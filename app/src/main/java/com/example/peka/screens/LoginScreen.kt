package com.example.peka.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.peka.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val authResult by authViewModel.authResult.collectAsState()

    // Reakcja na sukces logowania - przekierowanie do głównego ekranu
    LaunchedEffect(authResult) {
        if (authResult == true) {
            navController.navigate("dashboard_screen") {
                popUpTo("login_screen") { inclusive = true } // Usuwamy ekran logowania ze stosu
            }
        }
    }

    // Rejestrator wyniku okienka Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Wydobywamy Token Google
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    // Przekazujemy token do ViewModelu
                    authViewModel.signInWithGoogleToken(token)
                }
            } catch (e: ApiException) {
                // Obsługa błędu zamknięcia okienka lub braku internetu
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    // Konfiguracja opcji logowania.
                    // Wymagany jest tu "Web Client ID" z konsoli Google Cloud
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("177511652913-qfcksr47smn85s34g0v71eb2ni0btunj.apps.googleusercontent.com") // <- PAMIĘTAJ O TYM!
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)

                    // Wymuszenie wylogowania przed ponownym pokazaniem okna (opcjonalne, zapobiega auto-logowaniu na złe konto)
                    googleSignInClient.signOut().addOnCompleteListener {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                }
            ) {
                Text("Zaloguj przez Google")
            }

            if (authResult == false) {
                Text("Błąd logowania", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }


//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("E-mail") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Hasło") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                // Miejsce na wywołanie funkcji logowania
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Zaloguj")
//        }
//
//        Button(onClick = { navController.navigate("dashboard_screen") }) {
//            Text("DASHBOARD")
//        }
//
//        Button(onClick = { navController.navigate("api_screen") }) {
//            Text("API")
//        }
//    }
}