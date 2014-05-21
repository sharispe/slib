#!/usr/bin/python

import sys

help ="""
-----------------------------
Uniprot KB Pfam mapping v 0.0
-----------------------------
Script used to build a mapping between Uniprot KB ids and Pfam Clans

Arguments :
\t[1] uniprot mapping file i.e. file mapping uniprotkb ids to Pfam families e.g. uniprot_sprot.dat extracted from uniprot_sprot.dat.gz (ftp://ftp.sanger.ac.uk/pub/databases/Pfam/current_release)

\t[2] Pfam clan file i.e. file specifing families associated to a Pfam clan e.g. Pfam-C extracted from Pfam-C.gz (ftp://ftp.sanger.ac.uk/pub/databases/Pfam/current_release)

\t[3] output file (see below)

A tabular file will be generated as output
ID_UNIPROT_KB	TAXON_IDS*	PFAM_ID*	PFAM_CLAN*
Fields are separate by tabs
* multiple values separated by comma

Example:
Q91G88	176652,6997,168631,6999,58607,7108	PF12299,PF04383	
Q91G67	176652,6997,168631,6999,58607,7108	PF12299	
Q197B6	345201,7163,42431,332058,310513,329105,7183	PF00069	CL0016.20
...
"""
if(len(sys.argv) >= 2 and (sys.argv[1] == "-h" or sys.argv[1] == "--help")):
	print help
	exit()
if(len(sys.argv) != 4):
	print help;
	print "----------------------------------------------";
	print "<!> Incorrect parameters, please consult help";
	exit()



uniprot_mapping = sys.argv[1] #"uniprot_sprot.dat"
pfam_clan 		= sys.argv[2]
out_file 		= sys.argv[3]

print "Building Mapping Uniprot KB ids Pfam clans\n"
print "uniprot mapping file :",uniprot_mapping
print "Pfam file :",pfam_clan
print "out_file  :",out_file
print "\nprocessing... \n"


format_clans = "other"

#######################################################################
# Build clans mapping
#######################################################################

if(format_clans == "tsv"):
	pfam_clan = "pfam-A.clans.tsv"
	mapping_pFam_clan = {}

	for line in open(file_path):
		info = line.strip().split("\t")
		fam  =  info[0]
		clan =  info[1]
		
		if(clan != "\\N"):
			#print fam+"\t"+clan
			
			if(fam in mapping_pFam_clan):
				print "Error: Two clans defined for "+fam
				exit()
			mapping_pFam_clan[fam] = clan
			
elif(format_clans == "other"):
	pfam_clan = "Pfam-C"
	mapping_pFam_clan = {}


	pfam_ids = []
	
	for line in open(pfam_clan):
		
		
	
		data = line.rstrip().split("   ")
		flag =  data[0]
		
		if(flag == "#=GF AC"):
			clan = data[1]
		elif(flag == "#=GF MB"):
			pfam_ids.append(data[1].replace(";",""))
			
		elif(flag == "//"): # end of clan spec
			
			for fam in pfam_ids :
				if(fam in mapping_pFam_clan):
					print "Error: Two clans defined for "+fam
					exit()
				mapping_pFam_clan[fam] = clan
			
			pfam_ids = []
			


#######################################################################
# Build uniprot mapping
#######################################################################



uniprot_ids = []
pfam_ids 	= []
tax_ids 	= []

OUT = open(out_file,"w")

for line in open(uniprot_mapping):
	
	data = line.rstrip().split("   ")
	flag =  data[0]
    
	if(flag == "AC"):
		uniprotIds =  data[1].split(";")
		uniprotIds.pop()
		
		for i in range(0,len(uniprotIds)):
			uniprotIds[i] = uniprotIds[i].strip()
		
		
		
	elif(flag == "DR"):
		info =  data[1].split(";")
		
		if(info[0] == "Pfam"):
			pfam_ids.append(info[1].strip())
			
	elif(flag == "OX" or flag == "OH"):
		info =  data[1].split(";")
		tax_ids.append( info[0].split("=")[1])
		
		if(info[0] == "Pfam"):
			pfam_ids.append(info[1].strip())
			
	elif(flag == "//"): # end of protein spec
		
		if(len(pfam_ids) != 0):
			
			pfamAsString = pfam_ids[0]
			
			for i in range(1,len(pfam_ids)):
					pfamAsString += ","+pfam_ids[i]
					
			tax = tax_ids[0]
			for i in range(1,len(tax_ids)):
				tax += ","+tax_ids[i]
			
			clans = []
				
			for i in range(0,len(pfam_ids)):
				
					if(pfam_ids[i] in mapping_pFam_clan and not(mapping_pFam_clan[ pfam_ids[i] ] in clans) ):
						clans.append( mapping_pFam_clan[ pfam_ids[i] ] )
			
			clansAsString = ""
			
			if(len(clans) != 0):
				clansAsString = clans[0]
				
				for i in range(1,len(clans)):
						clansAsString += ","+clans[i]
						
				
			
			for pID in uniprotIds:
				
				OUT.write( pID+"\t"+tax+"\t"+pfamAsString+"\t"+clansAsString+"\n" );

		uniprot_ids = []
		pfam_ids 	= []
		tax_ids 	= []
		
OUT.close()

print "Done, consult",out_file
