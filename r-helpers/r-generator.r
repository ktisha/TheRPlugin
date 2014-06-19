args <- commandArgs(TRUE)
packageNames <- .packages(all = TRUE)
searchPath <- search()

is.identifier <- function(str) {
 return(grepl("^([[:alpha:]]|_|\\.)([[:alpha:]]|[[:digit:]]|_|\\.)*$", str) == TRUE)
}

for (name in packageNames) {
    if (name == "base") next
    shouldLoadLibrary = FALSE
    pName = paste("package", name, sep=":")
    if (!pName %in% searchPath)
        shouldLoadLibrary = TRUE
    if (shouldLoadLibrary) {
        library(package=name, character.only=TRUE)
    }

    symbolList <- ls(pName)

    dirName = paste(args[1], name, sep="/")
    dir.create(dirName)

    for(symbol in symbolList) {
        obj <- get(symbol)
        fileName <- paste(paste(dirName, symbol, sep="/"), "r", sep=".")
        sink("tmp")
        if (is.identifier(symbol))
            cat(symbol)
        else {
            cat("\"")
            cat(symbol)
            cat("\"")
        }
        cat(" <- ")
        print(obj)
        sink()

        fileObj <- file("tmp")
        lines <- readLines(fileObj)
        close(fileObj)

        sink(fileName)
        for (line in lines) {
            sub <- substring(line, 0, 10)
            if (sub == "<bytecode:"  || sub == "<environme") break
            cat(line, append=TRUE)
            cat("\n", append=TRUE)
        }
        sink()
    }
    if (shouldLoadLibrary) {
        detach(pName, character.only=TRUE)
        diff <- setdiff(search(), searchPath)
        for (p in diff) {
            detach(p, character.only=TRUE)
        }
    }
}

