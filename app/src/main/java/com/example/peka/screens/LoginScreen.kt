package com.example.peka.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.peka.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.peka.BuildConfig

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val authResult by authViewModel.authResult.collectAsState()

    LaunchedEffect(authResult) {
        if (authResult == true) {
            Log.e("Zalogowano","Udało się")
            navController.navigate("dashboard_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
        }
        else if (authResult == false) {
            //Text("Błąd logowania", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
            Log.e("Error Logowania","Błąd logowania")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    authViewModel.signInWithGoogleToken(token)
                }

            } catch (e: ApiException) {
                // Obsługa błędu zamknięcia okienka lub braku internetu
                Log.e("Error Logowania","Błąd logowania")
            }
        }
        else {
            Log.d("LOGIN SCREEN",result.resultCode.toString() )
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
                    Log.d("LOGIN SCREEN", "KLIKNIETO")
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .requestEmail()
                        .build()
                    Log.d("LOGIN SCREEN", "ZBUDOWANO")
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    Log.d("LOGIN SCREEN", "WZIĘTO KLIENTA")
                    googleSignInClient.signOut().addOnCompleteListener {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                }
            ) {
                Text("Zaloguj przez Google")
            }

            if (authResult == false) {
                Text("Błąd logowania", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
                Log.e("Error Logowania","Błąd logowania")
            }
            Button(onClick = { navController.navigate("dashboard_screen") }) {
                Text("wróć")
            }
        }
    }

}