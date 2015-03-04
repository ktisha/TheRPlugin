# @type x : numeric

f <- function(x) {
  x + 1
}

<warning descr="x expected to be of type numeric, found type character">f("d")</warning>