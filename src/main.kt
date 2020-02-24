package com.example.lib

import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

class OutputLibrary(val id: Int, val books: ArrayList<Int>) {
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
    init {
        books.sortByDescending { book -> book.score }
    }
    fun removeBook(book: Book) {
        books.remove(book)
    }
    fun pointsBeforeEnd(remainingDays: Int): Int {
        var currDay = nDays
        var totalScore = 0
        var addedBookIndex = 0
        while (currDay < remainingDays && addedBookIndex < books.size) {
            for (i in 1..nBooksPerDay) {
                totalScore += books[addedBookIndex].score
                addedBookIndex ++
                if (addedBookIndex >= books.size)
                    break
            }
            currDay++
        }
        return totalScore
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

fun main(args: Array<String>) {
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
                            libraryIndex,
                            line.split(' ')[0].toInt(),
                            line.split(' ')[1].toInt(),
                            line.split(' ')[2].toInt(),
                            ArrayList(fileRows[index + 1].split(' ').map { value ->
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

        val readBooks = arrayListOf<Book>()
        val signedLibraries = arrayListOf<Library>()
        var nextLibraryToSign: Library? = null
        var currDay = 0
        while (currDay < nDays) {

            // read book
            signedLibraries.forEach { library ->
                for (i in 1..library.nBooksPerDay) {
                    if (library.books.isEmpty()) break
                    val currBook = library.books.first()
                    if (readBooks.indexOf(currBook) == -1) {
                        readBooks.add(currBook)
                        Output.signedLibraries.find { outputLibrary -> outputLibrary.id == library.id }
                            ?.books?.add(currBook.id)
                        signedLibraries.forEach { it.removeBook(currBook) }
                        nextLibraryToSign?.removeBook(currBook)
                        notSignedLibraries.forEach { it.removeBook(currBook) }
                    }
                }
            }

            // sign library
            if (nextLibraryToSign == null) {
                if (notSignedLibraries.isNotEmpty()) {
                    nextLibraryToSign = notSignedLibraries.maxBy { it.pointsBeforeEnd(nDays) }
                    notSignedLibraries.remove(nextLibraryToSign)
                    nextLibraryToSign!!.nDays--
                }
            } else {
                nextLibraryToSign.nDays--
                if (nextLibraryToSign.nDays <= 0) {
                    signedLibraries.add(nextLibraryToSign)
                    Output.signedLibraries.add(OutputLibrary(nextLibraryToSign.id, arrayListOf()))
                    nextLibraryToSign = null
                }
            }
            currDay++
            println(currDay)
        }
        writeFile(Output.toString(), fileLetter)
        Output.reset()
    }
}