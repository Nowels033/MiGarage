package com.example.migarage.ui.signin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.migarage.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    vm: SignInViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Verifica si ya hay sesión iniciada
    LaunchedEffect(Unit) { vm.checkSession() }
    LaunchedEffect(state.isSignedIn) { if (state.isSignedIn) onSignedIn() }

    // Configuración de Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val gsc = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) vm.signInWithGoogleIdToken(idToken)
        } catch (_: ApiException) { }
    }

    // 🎨 Fondo degradado azul metalizado
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF001F3F), Color(0xFF0074D9))
    )

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {

                // ✨ Logo animado
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_migarage),
                        contentDescription = "Logo MiGarage",
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 24.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Nombre de la app
                Text(
                    text = "MiGarage",
                    fontSize = 40.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tu coche, tu mundo.",
                    color = Color(0xFFBFD8FF),
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(60.dp))

                // 🔘 Botón de inicio de sesión con Google
                Button(
                    onClick = { launcher.launch(gsc.signInIntent) },
                    enabled = !state.loading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_migarage),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (state.loading) "Conectando..." else "Iniciar sesión con Google",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text("Error: $it", color = Color.Red)
                }
            }
        }
    }
}
