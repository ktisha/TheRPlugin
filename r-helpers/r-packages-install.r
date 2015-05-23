chooseCRANmirror(ind = 1)
args <- commandArgs(TRUE)
install.packages(args[1], dependencies = TRUE,verbose=FALSE)