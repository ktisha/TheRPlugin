# This file contains skeletons for base package.


## @type recursive : logical
## @rule (... : T, recursive = FALSE) -> max(T)
c <- function (..., recursive = FALSE)  .Primitive("c")

## @return numeric
length <- function (x)  .Primitive("length")