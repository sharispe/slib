#!/usr/bin/python

import os,re

# ex human
#p_ids = ["hsa00040","hsa00920","hsa00140","hsa00290","hsa00563","hsa00670","hsa00232","hsa03022","hsa03020","hsa04130","hsa03450","hsa03430","hsa04950"]

p_ids = ["sce00562","sce00920","sce00600","sce00300","sce00410","sce00514","sce00670","sce00903","sce03022","sce04130","sce03450","sce04070","sce04140"]

s = "style=\"width:5em\"><nobr><a href=\"/dbget-bin/www_bget?sce:YGL180W\">YGL180W"

#<a href="/dbget-bin/www_bget?sce:YGL180W">
"""
r = re.findall('.*<nobr><a href=\"\\/dbget-bin\\/www_bget\?(\w+:\w+)\">\w+.*', s)
if(len(r) == 1):
	print(r[0])
	
exit()
"""

outputFile = "pathways_prots"
OUTPUT = open(outputFile,"w")



for p_id in p_ids:
	pathway_id = p_id
	output = "out_"+pathway_id

	url = "http://www.genome.jp/dbget-bin/www_bget?pathway+"+pathway_id
	os.system("wget -O "+output+" "+url)

	genes_line = False

	#<nobr><a href="/dbget-bin/www_bget?hsa:9061">
	p = re.compile('.*<nobr><a href=\"(\\/dbget-bin\\/www_bget\?.*:\d+)\">.*')
	#p = re.compile('.*bget.*')

	geneList = []
	allUniprotIds = []
	if(os.path.exists(output)):
		
		for line in open(output):
			line = line.strip()
			
			if(not genes_line and "<nobr>Gene</nobr>" in line):
				genes_line = True
			elif(genes_line and len(line) != 0):
				data = line.split("<tr>")
				genes_line = False
				
				for d in data:
					
					r = re.findall('.*<nobr><a href=\"\\/dbget-bin\\/www_bget\?(\w+:\w+)\">\w+.*', d)
					if(len(r) == 1):
						geneList.append(r[0])
						
		print "Found "+str(len(geneList))+" genes"
		print geneList	
		
		print "Try to get their UniprotIDs"
		
		for gene in geneList:
			print "processing: "+gene
			url = "http://www.genome.jp/dbget-bin/get_linkdb?-t+uniprot+"+gene
			print "\t"+url
			output_gene = "gene_"+gene
			os.system("wget -O "+output_gene+" "+url)
			
			uniprotIds = []
			
			for line in open(output_gene):
				r = re.findall('.*<a href=\"\\/dbget-bin\\/www_bget\?uniprot:(.+)\">.*', line)
				if(len(r) == 1):
					uniprotIds.append(r[0])
			
			os.remove(output_gene)
			
			allUniprotIds.extend(uniprotIds)
			print "\t"+str(uniprotIds)
		
		print "-------------------"
		print pathway_id
		print allUniprotIds
		print "-------------------"
		idsString = ','.join(allUniprotIds)
		OUTPUT.write(pathway_id+"\t"+idsString+"\n")
		
		os.remove(output)
	else:
		print "Cannot locate file please check "+url
		
print "Consult: "+outputFile
OUTPUT.close()
