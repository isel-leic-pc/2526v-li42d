package pt.isel.pc.utils

import java.io.BufferedWriter

/**
 * auxiliary function to append a newLine to the line
 * send to the BufferedWriter
 */
fun BufferedWriter.writeLine(line: String) {
    write(line)
    newLine()
    flush()
}