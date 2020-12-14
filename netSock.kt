package com.example.suspiciousapp.netSock

import java.net.Socket

object netSock
{
    val targetHost = "receiver.yourcloudserver.com"
    val targetPort: Int = 9999
    fun ncSend(mesg: String): String {
        val client = Socket(targetHost, targetPort)
        //Set socket timeout
        client.setSoTimeout(2)
        client.outputStream.write(mesg.toByteArray())
        client.close()
        var ack = "sent"
        return ack
   }
}
