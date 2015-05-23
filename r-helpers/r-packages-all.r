chooseCRANmirror(ind = 1)
p = available.packages()[,c("Package","Version")]
for( i in seq( length(p)/3)) {
  print(paste(p[i,1],p[i,2],sep=" "))
}