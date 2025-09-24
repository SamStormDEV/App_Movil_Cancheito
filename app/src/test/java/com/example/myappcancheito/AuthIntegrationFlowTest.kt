package com.example.myappcancheito

import org.junit.Assert.*
import org.junit.Test

sealed class OperationResult {
    data class Success(val message: String, val data: Any? = null) : OperationResult()
    data class Error(val errorMessage: String) : OperationResult()
    fun isSuccess(): Boolean = this is Success
}

data class UserData(val uid: String, val email: String, val userType: String)
interface MockAuthRepository {
    fun registerUser(email: String, pass: String, userType: String, additionalData: Map<String, String>): OperationResult
    fun loginUser(email: String, pass: String): OperationResult
}

class AuthIntegrationFlowTest {

    @Test
    fun flujoCompleto_registroYLogin_simuladoConExito() {
        println("Iniciando prueba de flujo de integración simulado en AuthIntegrationFlowTest...")

        val mockAuthRepository = object : MockAuthRepository {
            val users = mutableMapOf<String, String>()
            val userDetails = mutableMapOf<String, UserData>()

            override fun registerUser(email: String, pass: String, userType: String, additionalData: Map<String, String>): OperationResult {
                if (users.containsKey(email)) {
                    return OperationResult.Error("Email ya registrado (simulado)")
                }
                users[email] = pass
                val newUserData = UserData(uid = "uid_${email.hashCode()}", email = email, userType = userType)
                userDetails[email] = newUserData
                println("MockAuthRepo: Usuario '$email' (tipo: $userType) registrado (simulado).")
                return OperationResult.Success("Usuario registrado exitosamente (simulado)", newUserData)
            }

            override fun loginUser(email: String, pass: String): OperationResult {
                if (users.containsKey(email) && users[email] == pass) {
                    val userData = userDetails[email]
                    if (userData != null) {
                        println("MockAuthRepo: Login exitoso para '$email' (simulado).")
                        return OperationResult.Success("Login exitoso (simulado)", userData)
                    } else {

                        return OperationResult.Error("Error interno: datos de usuario no encontrados tras login (simulado)")
                    }
                }
                println("MockAuthRepo: Fallo de login para '$email' (simulado).")
                return OperationResult.Error("Credenciales inválidas (simulado)")
            }
        }

        val empleadorEmail = "empleador.integration@example.com"
        val empleadorPassword = "password123EmplIntegr"
        val empleadorTipo = "empleador"

        val resultadoRegistro = mockAuthRepository.registerUser(empleadorEmail, empleadorPassword, empleadorTipo, mapOf("empresa" to "Integration TestCorp"))

        assertTrue("El registro simulado del empleador debería ser exitoso", resultadoRegistro.isSuccess())
        if (resultadoRegistro is OperationResult.Success) {
            val userData = resultadoRegistro.data as? UserData
            assertNotNull("Los datos del usuario no deberían ser nulos tras el registro", userData)
            assertEquals("El email del usuario registrado no coincide", empleadorEmail, userData?.email)
            assertEquals("El tipo de usuario registrado no coincide", empleadorTipo, userData?.userType)
            println("Paso 1: Registro de empleador simulado exitoso para: ${userData?.email}")
        } else {
            fail("El registro del empleador falló inesperadamente: ${(resultadoRegistro as? OperationResult.Error)?.errorMessage}")
        }

        val resultadoLogin = mockAuthRepository.loginUser(empleadorEmail, empleadorPassword)

        assertTrue("El login simulado del empleador debería ser exitoso", resultadoLogin.isSuccess())
        if (resultadoLogin is OperationResult.Success) {
            val userData = resultadoLogin.data as? UserData
            assertNotNull("Los datos del usuario no deberían ser nulos tras el login", userData)
            assertEquals("El email del usuario logueado no coincide", empleadorEmail, userData?.email)
            assertEquals("El tipo de usuario logueado no coincide", empleadorTipo, userData?.userType)
            println("Paso 2: Login de empleador simulado exitoso para: ${userData?.email}")
        } else {
            fail("El login del empleador falló inesperadamente: ${(resultadoLogin as? OperationResult.Error)?.errorMessage}")
        }

        println("Prueba de flujo de integración simulado completada con éxito en AuthIntegrationFlowTest.")
    }
}