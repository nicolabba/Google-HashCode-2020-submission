package improved

import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import kotlin.math.pow

const val DECLINE_RATE = 1.025

class OutputLibrary(private val id: Int, private val books: ArrayList<Int>) {
    override fun toString(): String {
        return id.toString() + " " + books.size + "\n" + books.joinToString(" ")
    }
}

object Output {
    var signedLibraries = arrayListOf<OutputLibrary>()

    override fun toString(): String {
        return signedLibraries.size.toString() + "\n" + signedLibraries.map { it.toString() }.joinToString("\n")
    }

    fun reset() {
        signedLibraries = arrayListOf()
    }
}

data class Book(val id: Int, val score: Int)

data class Library(val id: Int, var nBooks: Int, var nDays: Int, var nBooksPerDay: Int, var books: ArrayList<Book>) {
    var readableBooks: MutableList<Book> = arrayListOf()
    init {
        books.sortByDescending { book -> book.score }
    }
    fun updateReadableBooksAndGetScore(remainingDays: Int): Double {
        val maxNumberOfReadableBooks: Long = (remainingDays.toLong() - nDays.toLong()) * nBooksPerDay.toLong()
        return if (remainingDays > nDays && books.size > 0) {
            readableBooks = if (maxNumberOfReadableBooks < books.size)
                books.subList(0, (maxNumberOfReadableBooks - 1).toInt())
            else books
            readableBooks.sumBy { it.score } / DECLINE_RATE.pow(nDays.toDouble())
        } else {
            readableBooks = arrayListOf()
            0.0
        }
    }
}

fun readFile(name: String): List<String> {
    val inputString =
        FileInputStream("./input/$name").readBytes().toString(Charset.defaultCharset())
    return inputString.split('\n')
}

fun writeFile(data: String, fileName: String) {
    val fo = FileOutputStream("./output/$fileName" + "_output.txt")
    fo.write(data.toByteArray(Charset.defaultCharset()))
    fo.close()
}

fun fileLetters(): List<String> {
    return listOf("a","b","c","d","e","f")
}

fun main() {
    fileLetters().forEach { fileLetter ->
        val fileRows = readFile("$fileLetter.txt")
        val nDays = fileRows[0].split(' ')[2].toInt()
        val bookScores = fileRows[1].split(' ').map { value -> value.toInt() }
        val notSignedLibraries = arrayListOf<Library>()
        var libraryIndex = 0
        fileRows.forEachIndexed { index, line ->
            if (index > 1 && line != "") {
                if (index % 2 == 0) {
                    notSignedLibraries.add(
                        Library(
                            id = libraryIndex,
                            nBooks = line.split(' ')[0].toInt(),
                            nDays= line.split(' ')[1].toInt(),
                            nBooksPerDay = line.split(' ')[2].toInt(),
                            books = ArrayList(fileRows[index + 1].split(' ').map { value ->
                                Book(
                                    id = value.toInt(),
                                    score = bookScores[value.toInt()]
                                )
                            })
                        )
                    )
                    libraryIndex++
                }
            }
        }

        // val signedLibraries = arrayListOf<Library>()
        var currDay = 0
        var time = System.nanoTime()
        while (currDay < nDays && notSignedLibraries.isNotEmpty()) {
            notSignedLibraries.maxBy {
                it.updateReadableBooksAndGetScore(nDays - currDay)
            }?.let { signingLibrary ->
                // signedLibraries.add(signingLibrary)
                if (signingLibrary.readableBooks.size > 0) {
                    Output.signedLibraries.add(
                        OutputLibrary(
                            signingLibrary.id,
                            ArrayList(signingLibrary.readableBooks.map { it.id })
                        )
                    )
                    notSignedLibraries.remove(signingLibrary)
                        notSignedLibraries.forEach {
                                if (it.books.isNotEmpty())
                                    it.books.removeAll(signingLibrary.readableBooks)
                                else
                                    arrayListOf<Book>()
                            }
                }
                currDay += signingLibrary.nDays
            }
            if (Output.signedLibraries.size % 15 == 0) {
                print(fileLetter + ": " + Output.signedLibraries.size)
                System.nanoTime().apply { println("    %.3fs".format(((this - time) / 1000000000.0))) }.also { time = it }
            }
        }
        writeFile(
            Output.toString(),
            fileLetter
        )
        Output.reset()
    }
}