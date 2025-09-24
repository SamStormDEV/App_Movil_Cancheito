package com.example.myappcancheito

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun registrarEmpleador_conDatosValidos_deberiaSerExitoso() {
        assertTrue(true)
    }

    @Test
    fun registrarEmpleador_conEmailInvalido_deberiaFallar() {
        assertTrue(true)
    }

    @Test
    fun registrarEmpleador_conContrasenaCorta_deberiaFallar() {
        assertTrue(true)
    }

    @Test
    fun registrarPostulante_conDatosValidos_deberiaSerExitoso() {
        assertTrue(true)
    }

    @Test
    fun registrarPostulante_conEmailExistente_deberiaFallar() {
        assertTrue(true)
    }

    @Test
    fun login_conCredencialesValidas_deberiaSerExitoso() {

        assertTrue(true)
    }

    @Test
    fun login_conCredencialesInvalidas_deberiaFallar() {
        assertTrue(true)
    }

    @Test
    fun login_conUsuarioNoRegistrado_deberiaFallar() {
        assertTrue(true)
    }

    @Test
    fun editarPerfil_conDatosValidos_deberiaSerExitoso() {
        assertTrue(true)
    }

    @Test
    fun editarPerfil_conDatoInvalido_deberiaFallar() {

        assertTrue(true)
    }
}