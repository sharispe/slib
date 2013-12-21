#!/usr/bin/Rscript

args <- commandArgs()

graph_name = args[6]
csv_file   = args[7]
out_pdf	   = args[8]


data <- read.table(csv_file, header=TRUE,sep="\t")

attach(data)
#print(data)

pdf(out_pdf)

#V_name	ICi_basic	ICi_resnik_1995	ICi_sanchez_2011_a	ICi_sanchez_2011_b	ICi_seco_2004


hist(data$ICi_basic,xlab="ICi_basic",ylab="Nb Vertices", main =paste(graph_name," ICi_basic"))
hist(data$ICi_resnik_1995,xlab="ICi_resnik_1995",ylab="Nb Vertices", main =paste(graph_name,"ICi_resnik_1995"))
hist(data$ICi_sanchez_2011_b,xlab="ICi_sanchez_2011_b",ylab="Nb Vertices", main =paste(graph_name,"ICi_sanchez_2011_b"))
hist(data$ICi_seco_2004,xlab="ICi_seco_2004",ylab="Nb Vertices", main =paste(graph_name,"ICi_seco_2004"))
hist(data$ICi_sanchez_2011_a,xlab="ICi_sanchez_2011_a",ylab="Nb Vertices", main =paste(graph_name,"ICi_sanchez_2011_a"))



plot(sort(data$ICi_basic),type='l',xlab="ICi_basic",ylab="Vertices")
plot(sort(data$ICi_resnik_1995),type='l',xlab="ICi_resnik_1995",ylab="Vertices")
plot(sort(data$ICi_sanchez_2011_b),type='l',xlab="ICi_sanchez_2011_b",ylab="Vertices")
plot(sort(data$ICi_seco_2004),type='l',xlab="ICi_seco_2004",ylab="Vertices")
plot(sort(data$ICi_sanchez_2011_a),type='l',xlab="ICi_sanchez_2011_a",ylab="Vertices")
#plot(c(1,length(data$Concept_1)),c(0,yrange),type="n",xlab="Concept couple",ylab="Similarity")


dOrdered  <- data[order(ICi_resnik_1995) , ]
plot(dOrdered$ICi_resnik_1995,type='l',xlab="ICi_resnik_1995",ylab="Vertices")
lines(dOrdered$ICi_sanchez_2011_b,col = 'red')
lines(dOrdered$ICi_seco_2004,col = 'blue')

