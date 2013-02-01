#!/usr/bin/Rscript

version	  <- "0.0"
cat("------------------------------------------------\n")
cat("SMF-sme: HeatMap version ",version,"\n")
cat("------------------------------------------------\n")

options <- commandArgs(trailingOnly = T)

if(length(options) != 3 | options[1] == "-help"){
	cat("- Arguments -\n")
	cat("- [1] graph name\n")
	cat("- [2] csv file (header + first 2 column dedicated to the entities couple)\n")
	cat("- [3] output i.e pdf file name\n")
	quit();
}


args <- commandArgs()

graph_name = args[6]
csv_file   = args[7]
out_pdf	   = args[8]

pdf(out_pdf)

cor_meth <- read.csv(csv_file, sep="\t")
cor_meth <- cor_meth[,3:ncol(cor_meth)]#[,3:ncol(cor_meth)]
print("----")
print(cor_meth)
names(cor_meth)

cor_mat <- round(cor(cor_meth),6) 


print(cor_mat)

cor_meth_heatmap <- heatmap(cor_mat, col = heat.colors(256), scale="column", margins=c(20,20),symm=TRUE,keep.dendro = TRUE, cexRow = 0.7, cexCol = 0.7)
#image(cor_meth_matrix,labels   = names( cor_meth$Methods ))
