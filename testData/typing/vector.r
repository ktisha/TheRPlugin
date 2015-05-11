## @type recursive : logical
## @rule (... : T, recursive = FALSE) -> max(T)
c <- function (..., recursive = FALSE)  .Primitive("c")

x <- c(1, 2, 3)

x[4]  <- "bug"

##@type x : numeric
f <- function(x)x

<warning descr="x expected to be of type numeric, found type character">f(x)</warning>
