package com.example.myapplication.Encryption

import android.util.Log
import com.example.myapplication.Constants.ConstantValues
import com.scottyab.aescrypt.AESCrypt
import java.security.GeneralSecurityException

class Encryption {
    companion object {
        fun aesEncryption(text: String) : String {
            var encryptedText: String ="-1"
            if(text != "-1") {
                try {
                    encryptedText = AESCrypt.encrypt(ConstantValues.AES_ENCRIPTION_KEY_FOR_USER_PASSWORD, text)
                } catch(e: GeneralSecurityException) {
                    Log.v(ConstantValues.LOGCAT_TEST, "AES Encryption failed")
                    e.printStackTrace()
                }
            }
            return encryptedText
        }

        fun aesDecryption(text: String): String {
            var decryptedText: String ="-1"
            if(text != "-1") {
                try {
                    decryptedText = AESCrypt.decrypt(ConstantValues.AES_ENCRIPTION_KEY_FOR_USER_PASSWORD, text)
                } catch(e: GeneralSecurityException) {
                    Log.v(ConstantValues.LOGCAT_TEST, "AES Encryption failed")
                    e.printStackTrace()
                }
            }
            return decryptedText
        }
    }
}