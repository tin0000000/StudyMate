package com.st10457568.studymate.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    // A unique random salt stops identical passwords from producing identical stored hashes.
    fun newSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, salt: String): String {
        val decodedSalt = Base64.decode(salt, Base64.NO_WRAP)
        // PBKDF2 makes each password check deliberately expensive compared with a fast digest.
        val spec = PBEKeySpec(password.toCharArray(), decodedSalt, 120_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }
}
