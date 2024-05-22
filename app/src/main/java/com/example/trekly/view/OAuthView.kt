package com.example.trekly.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Snackbar
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.R
import com.example.trekly.viewmodel.OAuthResult
import com.example.trekly.viewmodel.OAuthViewModel

@Composable
fun OAuthLoginPage(
    viewModel: OAuthViewModel,
    navigateToPreferences: () -> Unit,
    navigateToHome: () -> Unit
) {
    val signUpResult by remember { viewModel.signUpResult }
    val isSigningUp by remember { viewModel.isSigningUp }
    val errorMessage by remember { viewModel.errorMessage }
    // Input states
    var firstName by remember { viewModel.firstName }
    var lastName by remember { viewModel.lastName }
    var emailInput by remember { viewModel.emailInput }
    var passwordInput by remember { viewModel.passwordInput }

    if (signUpResult == OAuthResult.None) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.trekly_logo_foreground),
                contentDescription = "Trekly logo"
            )
            Text(
                if (isSigningUp) "Sign up" else "Log in",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isSigningUp) {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),

                )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isSigningUp) {
                        viewModel.signUp(
                            firstName,
                            lastName,
                            emailInput,
                            passwordInput,
                            navigateToPreferences
                        )
                    } else {
                        viewModel.signIn(emailInput, passwordInput, navigateToHome)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSigningUp) "Sign Up" else "Log In")
            }
            errorMessage?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    backgroundColor = Color.Transparent,
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(it, color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { viewModel.toggleSignup() }) {
                Text(if (isSigningUp) "Already have an account? Log in" else "Don't have an account? Sign up")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (signUpResult == OAuthResult.Success) "Sign Up Successful" else "Sign Up Not Successful")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.resetSignup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Log in")
            }
        }
    }
}