package com.mercer.magic

import java.security.MessageDigest


/**
 * 生成32位md5
 *
 * @param str
 * @return
 */
fun string2Md5(str: String): String {
    return try {
        val md5 = MessageDigest.getInstance("MD5")
        val charArray = str.toCharArray()
        val byteArray = ByteArray(charArray.size)
        for (i in charArray.indices) {
            byteArray[i] = charArray[i].toByte()
        }
        val md5Bytes = md5.digest(byteArray)
        val hexValue = StringBuffer()
        for (i in md5Bytes.indices) {
            val `val` = md5Bytes[i].toInt() and 0xff
            if (`val` < 16) {
                hexValue.append("0")
            }
            hexValue.append(Integer.toHexString(`val`))
        }
        hexValue.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
