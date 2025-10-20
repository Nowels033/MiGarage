package com.example.migarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.rememberNavController
import com.example.migarage.navigation.AppNav
import com.example.migarage.navigation.Route
import com.example.migarage.ui.theme.MiGarageTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiGarageTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    val auth = FirebaseAuth.getInstance()

                    // Redirección automática por estado de sesión
                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { fb ->
                            val target = if (fb.currentUser == null) Route.SignIn.path else Route.Home.path
                            nav.navigate(target) { popUpTo(0) } // limpia todo el stack
                        }
                        auth.addAuthStateListener(listener)
                        onDispose { auth.removeAuthStateListener(listener) }
                    }

                    AppNav(nav)
                }
            }
        }
    }
}

