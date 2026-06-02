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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peka.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.peka.BuildConfig
import com.example.peka.R
import com.example.peka.ui.theme.DarkAccent
import com.example.peka.ui.theme.DarkBackground
import com.example.peka.ui.theme.DarkSelectedAccent
import com.example.peka.ui.theme.neumorphicShadow

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
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 150.dp)) {
                Image(painter = painterResource(R.drawable.vimo), contentDescription = "Ikona", modifier = Modifier.padding(horizontal = 80.dp))
                Text(text = "ViMo",
                    fontWeight = FontWeight.Bold,
                    color = DarkAccent,
                    fontSize = 80.sp,
                    letterSpacing = 6.sp,
                    modifier = Modifier.padding(0.dp),
            )
            }

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
                },
                modifier = Modifier.height(50.dp).neumorphicShadow(20.dp,10.dp, darkShadowColor = DarkSelectedAccent, lightShadowColor = DarkAccent),
                colors = ButtonColors(
                    containerColor = DarkAccent,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkAccent,
                    disabledContentColor = DarkBackground
                )
            ) {
                Text("Zaloguj przez Google", fontSize = 16.sp,modifier =  Modifier.padding(12.dp,6.dp))
            }

            if (authResult == false) {
                Text("Błąd logowania", color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
                Log.e("Error Logowania","Błąd logowania")
            }
//            Button(onClick = { navController.navigate("dashboard_screen") },
//                ) {
//                Text("Kontynuuj bez logowania (debug)")
//            }
        }
    }

}