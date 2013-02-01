#!/usr/bin/Rscript



version	  <- "0.0.1"
cat("------------------------------------------------\n")
cat("SML-sme: ROC analysis ",version,"\n")
cat("------------------------------------------------\n")

options <- commandArgs(trailingOnly = T)

if(length(options) != 5 | options[1] == "-help"){
	cat("- Arguments -\n")
	cat("- [1] similarities results positive set\n")
	cat("- [2] similarities results negative set\n")
	cat("- [3] output i.e tabular file with AUC values\n")
	cat("- [4] output i.e pdf file name\n")
	cat("- [5] number of measure evaluation per plot e.g. 6\n")
	quit();
}


file_simScores_positive     <- options[1]
file_simScores_negative 	<- options[2]
csvOutput <- options[3]
pdfOutput <- options[4]
setSize   <- as.numeric(options[5])


cat("input +           : ",file_simScores_positive,"\n")
cat("input -           : ",file_simScores_negative,"\n")
cat("output           : ",pdfOutput,"\n")
cat("nb curve per plot: ",setSize,"\n")
cat("processing...\n")


showError <- FALSE

rocR <- function(mLabelsOrder,ROCR_arg1,ROCR_arg2,setSize){

	cat("-- Computing ",ROCR_arg1,"/",ROCR_arg2,"\n")
	
	legendPos <- "bottomright"
	
	if(ROCR_arg1 != "tpr"){
		legendPos <- "bottomleft"
	}
	addV <- FALSE
	
	LOG <-TRUE
	
	colorsP <- c("blue","forestgreen","darkorange2","yellow1","antiquewhite4","firebrick1")

	count <- 1
	legendNames <- c()

	for(i in 1:length(mLabelsOrder)){

		#print(class(simScores[,i]))
		mLabel <- mLabelsOrder[i]

		values_positive  <- c(simScores_positive[,mLabel])
		values_negative  <- c(simScores_negative[,mLabel])
		classif_pos <- c()
		classif_neg <- c()
		
		legendNames[count] <- mLabel
		
		if(length(values_positive) != length(values_negative)){
			
			if(length(values_positive) > length(values_negative)){
				values_positive <- values_positive[1:length(values_negative)]
			}
			else{
				values_negative <- values_negative[1:length(values_positive)]
			}
			
			if(LOG){
				cat("Different number of values -> positive",length(values_positive)," negative",length(values_negative),"\n")
				cat("reducted set ",length(values_positive)," negative",length(values_negative),"\n")
				LOG <-FALSE
			}
		}

		
		for(j in 1: length(values_positive)){
			
			classif_pos[j] <- 1
			classif_neg[j] <- 0
		}
		
		values <- c(values_positive,values_negative)
		classif <- c(classif_pos,classif_neg)
		
		pred <- prediction(values,classif)
		perf <- performance(pred,ROCR_arg1,ROCR_arg2)
		
		plot(perf,add=addV,col=colorsP[count],pch=1:2)
		
		if(count == setSize || i == ncol(simScores_positive)){
				
				legend(legendPos,
					legend = legendNames,
					fill = colorsP,
					bg = "white", ncol = 2,
					cex = 0.5)
					
				count <- 1
				legendNames <- c()
				addV <- FALSE
		}
		else{
			count <- count +1
		}

		if(addV == FALSE && count !=1){
			addV <- TRUE
		}
		
		
	}
}


computeAUC <- function(){

	cat("-- Computing AUC\n	")
	LOG <-TRUE
	aucs <- c()

	for(i in 1:ncol(simScores_positive)){

		#print(class(simScores[,i]))
		values_positive  <- c(simScores_positive[,i])
		values_negative  <- c(simScores_negative[,i])
		classif_pos <- c()
		classif_neg <- c()

		
		if(length(values_positive) != length(values_negative)){
			

			
			if(length(values_positive) > length(values_negative)){
				values_positive <- values_positive[1:length(values_negative)]
			}
			else{
				values_negative <- values_negative[1:length(values_positive)]
			}
			
			if(LOG){
				cat("Different number of values -> positive",length(values_positive)," negative",length(values_negative),"\n")
				cat("reducted set ",length(values_positive)," negative",length(values_negative),"\n")
				LOG <-FALSE
			}
		}

		
		for(j in 1: length(values_positive)){
			
			classif_pos[j] <- 1
			classif_neg[j] <- 0
		}
		
		values <- c(values_positive,values_negative)
		classif <- c(classif_pos,classif_neg)
		
		pred <- prediction(values,classif)
		auc_c <- performance(pred,"auc")
		aucs[i] <- auc_c@y.values[[1]]
		
		
	}

	names(aucs) <- names(simScores_positive);
	aucs <- sort(aucs, decreasing = TRUE)
	#print(aucs)
	return(aucs)
}

plotDensity <- function(labelM1){


		valuesPositiveM1  <- c(simScores_positive[,labelM1])
		valuesNegativeM1  <- c(simScores_negative[,labelM1])
	
		hist(valuesPositiveM1,xlab=labelM1,main=paste(labelM1," +"), breaks = 40, col = "lightblue",prob=TRUE)
		lines(density(valuesPositiveM1, adjust=2),col="red") 
		
		hist(valuesNegativeM1,xlab=labelM1,main=paste(labelM1," -"), breaks = 40, col = "lightblue",prob=TRUE)
		
		#TODO Remove NA ommit
		lines(density(na.omit(valuesNegativeM1), adjust=2),col="red") 
		
}

plotSpot <- function(labelM1,labelM2){

		valuesPositiveM1  <- c(simScores_positive[,labelM1])
		valuesPositiveM2  <- c(simScores_positive[,labelM2])
		
		valuesNegativeM1  <- c(simScores_negative[,labelM1])
		valuesNegativeM2  <- c(simScores_negative[,labelM2])
		
		plot(valuesPositiveM1,valuesPositiveM2,xlab=labelM1,ylab=labelM2,col="green")
		points(valuesNegativeM1, valuesNegativeM2, col="red")
}


library(ROCR)


simScores_positive  <- read.csv(file=file_simScores_positive,head=TRUE,sep="\t")
simScores_negative <-  read.csv(file=file_simScores_negative,head=TRUE,sep="\t")

simScores_positive <- simScores_positive[,3:ncol(simScores_positive)]
simScores_negative <- simScores_negative[,3:ncol(simScores_negative)]


pdf(pdfOutput)

par(mfrow=c(2,2))

if(ncol(simScores_positive) != ncol(simScores_negative)){
	print("Error abnormal number of column")
	quit()
}






aucValues <- computeAUC()


methodLabels <- names(aucValues)
# plot AUCs

cat("- 	AUC ranking  -\n")
cat("----------------------------------\n")

fileConn<-file(csvOutput,"w")

for(i in 1:length(aucValues)){
	cat(i,"- ",methodLabels[i],": ",aucValues[i],"\n")
	writeLines(paste(methodLabels[i],"\t",aucValues[i]), fileConn,sep="\n")
}
close(fileConn)

## true positive rate/false positive rate (x-axis: tpr, y-axis: fpr)
rocR(methodLabels,"tpr","fpr",setSize)

## precision/recall curve (x-axis: recall, y-axis: precision)
rocR(methodLabels,"prec", "rec",setSize)

## sensitivity/specificity curve (x-axis: specificity, y-axis: sensitivity)
rocR(methodLabels,"sens", "spec",setSize)

rocR(methodLabels,"lift", "rpp",setSize)

for(i in 1:length(methodLabels)){
	plotDensity(methodLabels[i])
}


cat("Consult +           : ",csvOutput,"\n")
cat("Consult +           : ",pdfOutput,"\n")









