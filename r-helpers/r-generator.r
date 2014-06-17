args <- commandArgs(TRUE)
packageNames <- .packages(all = TRUE)
searchPath <- search()

is.identifier <- function(str) {
 return(grepl("^([[:alpha:]]|_|\\.)([[:alpha:]]|[[:digit:]]|_|\\.)*$", str) == TRUE)
}

for (name in packageNames) {
    loadLibrary = FALSE
    pName = paste("package", name, sep=":")
    print(pName)
    if (!pName %in% searchPath)
        loadLibrary = TRUE

    if (loadLibrary) {
        library(package=name, character.only=TRUE)
    }

    symbolList <- ls(pName)

    for(symbol in symbolList) {
        obj <- get(symbol)
        dirName = paste(args[1], name, sep="/")
        dir.create(dirName)
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
            # print("HERE")
            # print(line)
            cat(line, append=TRUE)
            cat("\n", append=TRUE)
        }
        sink()
    }

    if (loadLibrary) {
        detach(pName, character.only=TRUE)
        diff <- setdiff(search(), searchPath)
        for (p in diff) {
            detach(p, character.only=TRUE)
        }
    }
}

