oddcount <- function(x) {
  k <- 0

  for (n in x) {
    if (n %% 2 == 1) k <- k + 1
  }

  return(k)
}

a <- oddcount(c(1,3,5))