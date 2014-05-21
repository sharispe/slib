#!/usr/bin/Rscript

version	  <- "0.0"
cat("------------------------------------------------\n")
cat("SMF-sme: Correlation version ",version,"\n")
cat("------------------------------------------------\n")

options <- commandArgs(trailingOnly = T)

if(length(options) != 3 | options[1] == "-help"){
	cat("- Arguments -\n")
	cat("- [1] base results (column header)\n")
	cat("- [2] csv file (header + first 2 column dedicated to the entities couple)\n")
	cat("- [3] output i.e pdf file name\n")
	quit();
}


args <- commandArgs()

column_field = args[6]
csv_file   = args[7]
out_pdf	   = args[8]

cat("Loading data...","\n");
cor_meth <- read.csv(csv_file, sep="\t")
cat("Removing entry fields...","\n");
cor_meth <- cor_meth[,3:ncol(cor_meth)]#[,3:ncol(cor_meth)]

cat("Retrieving labels...","\n");
labels <- names(cor_meth);

cat("Considering ",column_field,"\n");

corr <- c();
corr_absolute <- c();

if(!(column_field  %in% labels)){
	cat("Cannot find specified column, please select one in : \n");
	for(i in 1: length(labels)){
		cat(labels[i],"\t");
	}
	stop("Cannot find specified column field ",column_field," ...");
}

cat("Computing correlation...","\n");
for(i in 1: length(labels)){
	
	x <- cor(cor_meth[column_field],cor_meth[labels[i]], use="complete.obs");
	corr_rounded <- round(x[,1],6);
	#print(paste(corr_rounded,labels[i]));
	corr[i] <- corr_rounded; 
	corr_absolute[i] <- abs(corr_rounded);
}

cat("Processing results...","\n");
names(corr_absolute) <- c(labels);
names(corr) <- c(labels);
methodLabels <- names(sort(corr_absolute, decreasing = TRUE))

for(i in 1: length(methodLabels)){
	if(methodLabels[i] != column_field){
		cat(as.character(i-1),"/",as.character(length(methodLabels)-1),"\t",paste(corr[methodLabels[i]],methodLabels[i]),"\n");
	}
}

#print(correlation["ec_score"])
#names(cor_meth)
