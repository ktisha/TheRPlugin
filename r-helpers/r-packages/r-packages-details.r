args <- commandArgs(TRUE)
chooseCRANmirror(ind = args[2])
number.defaults = as.numeric(args[3])
defauls = if(number.defaults>0){
  args[4:(3+number.defaults)]
}
additional = if((length(args)-3-number.defaults)>0){
                args[(4+number.defaults):length(args)]}
setRepositories(FALSE,defauls,additional)
p = available.packages()
p [args[1],]