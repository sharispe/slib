#! /usr/bin/Rscript

# Arguments
# [1] input file i.e. discriminative power scores
# [2] output i.e pdf file name
# [3] max disciminative power value: Large values can appears in order to plot results you can set them to a specific value e.g. 30
# [4] setSize number of measure evaluation per plot

setMax <- function(vals, maxValue)
{
	for(i in 1:length(vals)){
	
		if(vals[i] > maxValue){
			vals[i] <- maxValue
		}
	}
	return(vals)
}


version	  <- "0.0.1"
cat("---------------------------------------------------\n")
cat("SMF-sme: Discriminative Power Plotter version",version,"\n")
cat("---------------------------------------------------\n")

options <- commandArgs(trailingOnly = T)

if(length(options) != 5 | options[1] == "-help"){
	cat("- Arguments -\n")
	cat("- [1] input file i.e. discriminative power scores\n")
	cat("- [2] output i.e pdf file name\n")
	cat("- [3] output i.e tabular file with rankink based on discriminative power sums\n")
	cat("- [4] max disciminative power value: Large values can appears, you have set a maximal value e.g. 30 -> all values > 30 will be set to 30 \n")
	cat("- [5] number of measure evaluation per plot e.g. 6\n")
	quit();
}


input      <- options[1]
pdfOutput  <- options[2]
textOutput <- options[3]
maxVal 	   <- as.numeric(options[4])
setSize    <- as.numeric(options[5])


cat("input             : ",input,"\n")
cat("output (txt)      : ",textOutput,"\n")
cat("output (pdf)      : ",pdfOutput,"\n")
cat("max               : ",maxVal,"\n")
cat("nb curve per plot : ",setSize,"\n")
cat("processing...\n")




dp_scores  <- read.csv(file=input,head=TRUE,sep="\t")



pdf(pdfOutput)
par(mfrow=c(2,2))

methodLabels <- names(dp_scores)[-1]

# Compute Discriminative Score sums to order methods ------
sumDS <- c();


for(i in 1: length(methodLabels)){
	sumDS[i] <- 0
	for(j in 1:length(dp_scores[,methodLabels[i]])){
		sumDS[i] <- sumDS[i] + dp_scores[,methodLabels[i]][j]
	}
	
}

# Set value > max to max ----------------------------------------

for(i in 1: length(methodLabels)){
	dp_scores[,methodLabels[i]] <- setMax(dp_scores[,methodLabels[i]],maxVal)
}



names(sumDS) <- c(methodLabels)
methodLabels <- names(sort(sumDS, decreasing = TRUE))



# compute max values foreach set-------------------------------

count <- 1
setNb <- 1
maxValues <- c()


fileConn<-file(textOutput,"w")

for(i in 1: length(methodLabels)){
	
	cat(i,"\t",sumDS[methodLabels[i]],methodLabels[i],"\n");
	writeLines(paste(methodLabels[i],"\t",sumDS[methodLabels[i]]), fileConn,sep="\n")
	
	if(count==1){
		maxValues[setNb] <- range(dp_scores[,methodLabels[i]])[2]
	}
	else{
		maxtmp <- range(dp_scores[,methodLabels[i]])[2]
		if(maxValues[setNb] < maxtmp ){
			maxValues[setNb] <- maxtmp
		}
	}
	
	count <- count + 1;
	
	if(count == setSize+1 ){
		count <- 1
		setNb <- setNb + 1
	}
	
}

close(fileConn)

# Plot results -------------------------------

colorsP <- c("blue","forestgreen","darkorange2","firebrick1","antiquewhite4","black","yellow1")

count <- 1
setNb <- 1
legendNames <- c()

for(i in 1: length(methodLabels)){
	
	legendNames[count] <-  methodLabels[i]
	if(count==1){
			
		plot(dp_scores[,methodLabels[i]],axes=FALSE,xlab="",ylab="Discriminative Power",type="l",ylim=c(0,maxValues[setNb]+maxValues[setNb]/2),col=colorsP[count])
		axis(1, labels = FALSE)
		axis(2)
		
		
		 
		labelValues <- dp_scores[,"Clan"]

		text(1:length(labelValues), par("usr")[3] + (par("usr")[3])*2, srt = 45, adj = 1,
        labels = labelValues, xpd = TRUE,cex=0.5)
        
        if(maxValues[setNb] == maxVal){
			abline(h=maxVal,lty=3)
        }
		box()
	}
	else{
		lines(dp_scores[,methodLabels[i]],col=colorsP[count])
	}
	
	count <- count + 1;
	
	if(count == setSize+1){
	
		legend("top",
			   legend = legendNames,
				fill = colorsP,
				bg = "white", ncol = 2,
				cex = 0.5)
				
		plot(sumDS[methodLabels[c((i-setSize+1):i)]],axes=FALSE,xlab="",ylab="Sum Discriminative Power");
		#axis(1, labels = FALSE)
		axis(2)
		text(1:setSize, (par("usr")[3])-0.03, srt = 45, adj = 1, labels = methodLabels[c((i-setSize+1):i)], xpd = TRUE,cex=0.5)
		box();
		
		legendNames <- c()
		
		count <- 1
		setNb <- setNb + 1
	}
	
}
	if(count != 1){
		legend("top",
			   legend = legendNames,
				fill = colorsP,
				bg = "white", ncol = 2,
				cex = 0.5)
		legendNames <- c()
		count <- 1
		setNb <- setNb + 1
	}
cat("------------------------------------------------\n")
cat("done, consult: ",pdfOutput," anc ",textOutput,"\n")
cat("------------------------------------------------\n")
