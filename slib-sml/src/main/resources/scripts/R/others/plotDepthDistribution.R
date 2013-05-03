#!/usr/bin/Rscript

args <- commandArgs()

graph_name = args[6]
csv_file   = args[7]
out_pdf	   = args[8]


data <- read.csv(csv_file, header=TRUE,sep="\t")

#print(data)

pdf(out_pdf)

hist(data$min_depth,xlab="Min Depths",ylab="Nb Vertices", main =paste(graph_name," Min depths"))
hist(data$max_depth,xlab="Max Depths",ylab="Nb Vertices", main =paste(graph_name,"Max depths"))
hist(data$diff_min_max,xlab="Max -Min ",ylab="Nb Vertices", main =paste(graph_name,"Max-Min depths"))

#plot(h,xlab="Depths",ylab="Nb Vertices")
#plot(c(1,length(data$Concept_1)),c(0,yrange),type="n",xlab="Concept couple",ylab="Similarity")




