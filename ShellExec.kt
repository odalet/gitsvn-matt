package fr.yodamad.svn2git.service

import java.io.*
import java.util.concurrent.Executors
import java.util.function.Consumer

class ShellExec {
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ShellExec().execCommand(true, "c:/temp", "git svn ...", usePowershell = true)
        }
    }

    @JvmOverloads
    @Throws(InterruptedException::class, IOException::class)
    fun execCommand(isWindows: Boolean, directory: String, command: String?, securedCommandToPrint: String? = command, usePowershell: Boolean = false): Int {
        val builder = ProcessBuilder()
        val execDir = formatDirectory(isWindows, directory)
        if (isWindows) {
            if (usePowershell) builder.command(command)
            else builder.command("cmd.exe", "/c", command)
        } else {
            builder.command("sh", "-c", command)
        }
        builder.directory(File(execDir))
        println(String.format("Exec command : %s", securedCommandToPrint))
        println(String.format("in %s", execDir))
        val process = builder.start()
        val streamGobbler = StreamGobbler(process.inputStream) { s: String? -> println(s) }
        Executors.newSingleThreadExecutor().submit(streamGobbler)
        val errorStreamGobbler = StreamGobbler(process.errorStream) { s: String? -> println(s) }
        Executors.newSingleThreadExecutor().submit(errorStreamGobbler)
        val exitCode = process.waitFor()
        println(String.format("Exit : %d", exitCode))
        if (exitCode != 0) {
            // trace failed commands
            throw RuntimeException("")
        }
        return exitCode
    }

    private class StreamGobbler constructor(private val inputStream: InputStream, private val consumer: Consumer<String?>) : Runnable {
        override fun run() {
            BufferedReader(InputStreamReader(inputStream)).lines()
                .forEach(consumer)
        }
    }

    /**
     * Format directory to fit f*** windows behavior
     * @param directory Directory to format
     * @return formatted directory
     */
    fun formatDirectory(isWindows: Boolean, directory: String): String {
        var execDir = directory
        if (isWindows) {
            execDir = if (directory.startsWith("/")) String.format("%s%s", System.getenv("SystemDrive"), directory).replace("/".toRegex(), "\\\\") else directory
        }
        return execDir
    }
}
