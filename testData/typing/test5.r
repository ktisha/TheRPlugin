f <- function(x) {
    y <- "text"
}

## @type x : numeric
bar <- function(x) {
    x
}

<warning descr="x expected to be of type numeric, found type character">bar(f(1))</warning>